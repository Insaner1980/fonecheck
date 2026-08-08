# fonecheck Android Check Scripts Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:executing-plans` inline. The user authorized batch continuation on 2026-08-08: complete all remaining preparation and run every check that does not violate the Gradle or external-AI constraints below.

**Goal:** Varmistaa ja tarvittaessa korjata fonecheckin kaikki yleiset Android-tarkistuskomennot yksi kerrallaan niin, että jokainen käyttää yhteistä Android-check-ydintä, oikeaa projektimanifestia ja turvallista semanttista tulosmallia.

**Architecture:** Yhteinen toteutus ja työkalut ovat repossa `C:\Dev\Android-check`; `C:\Dev\android-project-maintenance` on suunnittelu- ja auditointityötila, ei ajonaikainen skriptijuuri. fonecheck säilyttää vain ohuet `tools\*.ps1`-wrapperit sekä projektikohtaisen `config\android-check.json`-manifestin ja tarkat tarkistuskonfiguraatiot.

**Tech Stack:** PowerShell 7, JSON-manifesti, Gradle Kotlin DSL, Android Lint, ktlint, Detekt, Semgrep, mobsfscan, Gitleaks, TruffleHog, OSV-Scanner, OWASP Dependency-Check, PMD CPD, Compose Stability Analyzer, GitHub CLI ja DeepSec 2.2.9.

## Global Constraints

- Älä aja Gradle-buildia, testejä, linttiä tai stability-tehtäviä Codexista; käyttäjä ajaa varsinaisen testikomennon omassa terminaalissaan CPU-rajoitteen vuoksi.
- Käsittele taskit järjestyksessä, mutta jatka seuraavaan ilman käyttäjän välitestiä. Gradle-pohjaiset varsinaiset ajot jäävät projektiohjeen vuoksi käyttäjälle; muut turvalliset paikalliset ajot agentti saa suorittaa itse.
- Säilytä nykyiset käyttäjän muutokset tiedostoissa `CODE_REVIEW.md`, `DiagnosticDestination.kt`, `RunAllTestsViewModel.kt`, `RunAllTestsViewModelTest.kt` ja `fonecheck-implementation-plan.md`; tämän työn ei pidä koskea niihin.
- Ennen jokaista taskia tarkista `git status --short` sekä kyseisen taskin kohdetiedoston `git diff`.
- Jos wrapper ja manifesti jo täyttävät sopimuksen, taskin oikea tulos on validoitu nollamuutos. Älä kirjoita toimivaa tiedostoa uudelleen.
- Muuta `C:\Dev\Android-check`-repoa vain, jos projektikohtainen testi todistaa yhteisessä ytimessä olevan vian. Tee tällainen korjaus erillisenä taskina, koska se vaikuttaa kaikkiin Android-projekteihin.
- `-PlanOnly` saa ratkaista reitin, manifestin, työkalut, verkon käytön ja raporttipolut, mutta se ei saa käynnistää Gradlea, skanneria, verkkolähetystä tai työkalun asennusta.
- Tulokset ovat `CLEAN/0`, `FINDINGS/1`, `ERROR/2`, `NOT_APPLICABLE/3` ja `UNKNOWN/4`. Todellinen löydös (`1`) voi todistaa skriptin toimivaksi; teknistä virhettä (`2`) tai epävarmaa tulosta (`4`) ei hyväksytä valmiiksi ilman syyn tutkimista.
- Attachmentin `ac`-kuvaus on vanhentunut DeepSecin osalta. `ac` ja `sc -Full` eivät saa käynnistää DeepSeciä; ulkoinen AI kuuluu vain erikseen hyväksyttyyn `ds`-ajoon.
- Älä tee ulkoisia DeepSec-, Sonar- tai muita aineistoa lähettäviä ajoja ilman käyttäjän nimenomaista kyseisen ajon hyväksyntää.
- Älä commitoi tai pushaa tämän suunnitelman toteutusmuutoksia ilman käyttäjän erillistä pyyntöä.
- `rs` ja `rst` ovat tarkoituksella vain KnitToolsissa, joten niitä ei lisätä fonecheckiin.
- `sonar` saa projektikohtaisen wrapperin vain, jos fonecheckille on oikeasti määritetty SonarQube- tai SonarCloud-projekti. Nykyinen lähdekoodi ei sisällä tällaista konfiguraatiota.
- `sentry`-wrapper on jo fonecheckissa, mutta sitä ei ole käyttäjän liittämässä skriptilistassa eikä se kuulu tähän toteutusjonoon.

---

## File Map

- Shared runtime, normally read-only: `C:\Dev\Android-check\tools\InvokeProjectCheck.ps1`
- Shared implementation, normally read-only: `C:\Dev\Android-check\tools\AndroidProjectChecks.psm1`
- Shared routing, normally read-only: `C:\Dev\Android-check\tools\ProjectCheckRouting.psm1`
- Project manifest: `config/android-check.json`
- Project exceptions: `config/check-exceptions.json`
- Project Semgrep rules: `config/semgrep/fonecheck-security.yml`
- Project Detekt rules: `config/detekt/detekt.yml`
- Project OWASP suppressions: `config/dependency-check/suppressions.xml`
- Existing wrappers to validate independently: `tools/bc.ps1`, `tools/tc.ps1`, `tools/lc.ps1`, `tools/cr.ps1`, `tools/cs.ps1`, `tools/ga.ps1`, `tools/pc.ps1`, `tools/ms.ps1`, `tools/os.ps1`, `tools/ss.ps1`, `tools/dc.ps1`, `tools/ac.ps1`, `tools/sc.ps1`, `tools/ql.ps1`, `tools/db.ps1` and `tools/ds.ps1`
- Conditional file, currently absent: `tools/sonar.ps1`

## Per-task execution contract

Jokaisessa taskissa agentti tekee samassa järjestyksessä vain nämä asiat:

1. Tarkistaa Git-tilan ja kohdetiedoston diff:n.
2. Vertaa wrapperia taskissa annettuun täsmälliseen sisältöön. Korjaa vain todellinen ero.
3. Parsii yhden PowerShell-tiedoston ilman sen suorittamista tällä istuntokohtaisella apufunktiolla:

```powershell
function Test-PowerShellSyntax {
    param([Parameter(Mandatory)][string]$Path)

    $tokens = $null
    $errors = $null
    [System.Management.Automation.Language.Parser]::ParseFile(
        (Resolve-Path $Path),
        [ref]$tokens,
        [ref]$errors
    ) | Out-Null
    if ($errors.Count -ne 0) { $errors; return $false }
    return $true
}
```

4. Ajaa vain kyseisen wrapperin `-PlanOnly`-tilan ja varmistaa projektiksi `fonecheck`, juureksi nykyisen checkoutin, oikean backend-komennon sekä taskissa luetellun scopen.
5. Ajaa varsinaisen komennon itse, jos se ei käynnistä Gradlea, lähetä lähdekoodia ulkoiseen palveluun tai vaadi puuttuvaa lupaa. Muussa tapauksessa kirjaa tarkka rajoite ja jatka seuraavaan taskiin.

---

### Task 1: `bc` / build-check

**Files:**
- Verify or modify: `tools/bc.ps1`
- Verify: `config/android-check.json`
- Modify only because the shared regression was reproduced: `C:\Dev\Android-check\tools\AndroidProjectChecks.psm1`
- Test: `C:\Dev\Android-check\tests\ProjectCheckManifestEngine.Tests.ps1`

**Interfaces:**
- Consumes: manifestin `buildTasks`-arvon `[":app:assembleDebug"]`
- Produces: `build-check`-entrypoint, jonka käyttäjä voi ajaa komennolla `bc`
- Compatibility rule: rikastettu uusi manifesti käyttää manifestimoottoria; nykyinen project-specific legacy-manifesti käyttää olemassa olevaa legacy build-backendia, kunnes projekti migroidaan kokonaisena erillisenä muutoksena

- [x] **Step 1: Varmista wrapperin täsmällinen sisältö**

```powershell
$ProjectCheckCommand = "build-check"
& "C:\Dev\Android-check\tools\InvokeProjectCheck.ps1" -ProjectCheckCommand $ProjectCheckCommand @args
exit $LASTEXITCODE
```

- [x] **Step 2: Aja `Test-PowerShellSyntax '.\tools\bc.ps1'` ja vaadi tulokseksi `True`**

- [x] **Step 3: Aja `& .\tools\bc.ps1 -PlanOnly`**

Expected: projekti on `fonecheck`, backend on `build-check`, ainoa build-task on `:app:assembleDebug`, eikä Gradlea käynnistetä.

- [x] **Step 4: Anna käyttäjän hyväksyntätestiksi `bc` ja pysähdy**

Expected: onnistunut debug-koonti tuottaa `CLEAN/0`; Gradle- tai konfiguraatiovirhe näkyy `ERROR/2`:na eikä vihreänä ohituksena.

### Task 2: `tc` / test-check

**Files:**
- Verify or modify: `tools/tc.ps1`
- Verify: `config/android-check.json`

**Interfaces:**
- Consumes: manifestin `testTasks`-arvon `[":app:testDebugUnitTest"]`
- Produces: `test-check`-entrypoint, jonka käyttäjä voi ajaa komennolla `tc`

- [x] **Step 1: Varmista wrapperin täsmällinen sisältö**

```powershell
$ProjectCheckCommand = "test-check"
& "C:\Dev\Android-check\tools\InvokeProjectCheck.ps1" -ProjectCheckCommand $ProjectCheckCommand @args
exit $LASTEXITCODE
```

- [x] **Step 2: Aja `Test-PowerShellSyntax '.\tools\tc.ps1'` ja vaadi tulokseksi `True`**

- [x] **Step 3: Aja `& .\tools\tc.ps1 -PlanOnly`**

Expected: projekti on `fonecheck`, backend on `test-check`, ainoa testitask on `:app:testDebugUnitTest`, eikä Gradlea käynnistetä.

- [x] **Step 4: Anna käyttäjän hyväksyntätestiksi `tc` ja pysähdy**

Expected: läpäisevät unit-testit tuottavat `CLEAN/0`; epäonnistuva Gradle-testitask tuottaa `ERROR/2`-tilan ja `reports/test.txt` kuuluu nykyiseen run ID:hen.

### Task 3: `lc` / lint-check

**Files:**
- Verify or modify: `tools/lc.ps1`
- Verify: `config/android-check.json`
- Verify only if a finding proves a config issue: `config/detekt/detekt.yml`
- Modify only because the shared PlanOnly defect was reproduced: `C:\Dev\Android-check\tools\AndroidProjectChecks.psm1`
- Test: `C:\Dev\Android-check\tests\GradleReportReuse.Tests.ps1`

**Interfaces:**
- Consumes: `:app:ktlintCheck`, `:app:detekt` and `:app:lintDebug`
- Produces: `reports/ktlint.txt`, `reports/detekt.txt`, `reports/lint.txt` and `reports/input-state.txt`

- [x] **Step 1: Varmista wrapperin täsmällinen sisältö**

```powershell
$ProjectCheckCommand = "lint-check"
& "C:\Dev\Android-check\tools\InvokeProjectCheck.ps1" -ProjectCheckCommand $ProjectCheckCommand @args
exit $LASTEXITCODE
```

- [x] **Step 2: Aja `Test-PowerShellSyntax '.\tools\lc.ps1'` ja vaadi tulokseksi `True`**

- [x] **Step 3: Aja `& .\tools\lc.ps1 -PlanOnly`**

Expected: suunnitelma näyttää kaikki kolme taskia, tuoreet koneraportit ja input-sormenjäljen; se ei aja Gradlea.

- [x] **Step 4: Anna käyttäjän hyväksyntätestiksi `lc` ja pysähdy**

Expected: `0` tai aidot lint-löydökset `1`; puuttuva, vanha tai väärän scopen raportti on `ERROR/2`.

### Task 4: `cr` / compose-rules

**Files:**
- Verify or modify: `tools/cr.ps1`
- Verify: `config/android-check.json`
- Modify because the shared-snapshot regression was reproduced: `C:\Dev\Android-check\tools\AndroidProjectChecks.psm1`
- Test: `C:\Dev\Android-check\tests\ComposeRules.Tests.ps1`

**Interfaces:**
- Consumes: manifestin ktlint- ja detekt-taskit sekä nykyiset compose-rules-riippuvuudet
- Produces: `reports/compose-rules-ktlint.txt` and `reports/compose-rules-detekt.txt`

- [x] **Step 1: Varmista wrapperin täsmällinen sisältö**

```powershell
$ProjectCheckCommand = "compose-rules"
& "C:\Dev\Android-check\tools\InvokeProjectCheck.ps1" -ProjectCheckCommand $ProjectCheckCommand @args
exit $LASTEXITCODE
```

- [x] **Step 2: Aja `Test-PowerShellSyntax '.\tools\cr.ps1'` ja vaadi tulokseksi `True`**

- [x] **Step 3: Aja `& .\tools\cr.ps1 -PlanOnly`**

Expected: suunnitelma käyttää `:app:ktlintCheck`- ja `:app:detekt`-taskeja samasta snapshotista eikä lisää uutta Gradle-pluginia.

- [ ] **Step 4: Anna käyttäjän hyväksyntätestiksi `cr` ja pysähdy**

Expected: Compose-sääntölöydökset ovat `FINDINGS/1`; molempien raporttien pitää kuulua nykyiseen ajoon.

### Task 5: `cs` / compose-stability

**Files:**
- Verify or modify: `tools/cs.ps1`
- Verify: `config/android-check.json`

**Interfaces:**
- Consumes: manifestin `stabilityTasks`-arvon `[":app:stabilityCheck"]`
- Produces: `reports/compose-stability.txt`

- [x] **Step 1: Varmista wrapperin täsmällinen sisältö**

```powershell
$ProjectCheckCommand = "compose-stability"
& "C:\Dev\Android-check\tools\InvokeProjectCheck.ps1" -ProjectCheckCommand $ProjectCheckCommand @args
exit $LASTEXITCODE
```

- [x] **Step 2: Aja `Test-PowerShellSyntax '.\tools\cs.ps1'` ja vaadi tulokseksi `True`**

- [x] **Step 3: Aja `& .\tools\cs.ps1 -PlanOnly`**

Expected: vain `:app:stabilityCheck` on ajossa; `stabilityDump` ei käynnisty implisiittisesti.

- [ ] **Step 4: Anna käyttäjän hyväksyntätestiksi `cs` ja pysähdy**

Expected: stability-analyysi tuottaa nykyajon raportin ja semanttisen exit-koodin.

### Task 6: `ga` / google-android-security

**Files:**
- Verify or modify: `tools/ga.ps1`
- Verify: `config/android-check.json`
- Modify because the PlanOnly scope defect was reproduced: `C:\Dev\Android-check\tools\AndroidProjectChecks.psm1`
- Test: `C:\Dev\Android-check\tests\GoogleAndroidSecurity.Tests.ps1`

**Interfaces:**
- Consumes: Android Lintin `:app:lintDebug`-taskin Google Android Security -säännöt
- Produces: `reports/google-android-security.txt`

- [x] **Step 1: Varmista wrapperin täsmällinen sisältö**

```powershell
$ProjectCheckCommand = "google-android-security"
& "C:\Dev\Android-check\tools\InvokeProjectCheck.ps1" -ProjectCheckCommand $ProjectCheckCommand @args
exit $LASTEXITCODE
```

- [x] **Step 2: Aja `Test-PowerShellSyntax '.\tools\ga.ps1'` ja vaadi tulokseksi `True`**

- [x] **Step 3: Aja `& .\tools\ga.ps1 -PlanOnly`**

Expected: suunnitelma näyttää Android Lintin security-säännöt, debug-variantin ja nykyajon raportin; se ei käytä vanhaa Lint-raporttia.

- [ ] **Step 4: Anna käyttäjän hyväksyntätestiksi `ga` ja pysähdy**

Expected: aidot security-lint-löydökset ovat `FINDINGS/1`; puuttuva tai stale raportti on `ERROR/2`.

### Task 7: `pc` / pmd-check

**Files:**
- Verify or modify: `tools/pc.ps1`

**Interfaces:**
- Consumes: fonecheckin Kotlin- ja Java-lähdepolut
- Produces: `reports/pmd-cpd.txt`

- [x] **Step 1: Varmista wrapperin täsmällinen sisältö**

```powershell
$ProjectCheckCommand = "pmd-check"
& "C:\Dev\Android-check\tools\InvokeProjectCheck.ps1" -ProjectCheckCommand $ProjectCheckCommand @args
exit $LASTEXITCODE
```

- [x] **Step 2: Aja `Test-PowerShellSyntax '.\tools\pc.ps1'` ja vaadi tulokseksi `True`**

- [x] **Step 3: Aja `& .\tools\pc.ps1 -PlanOnly`**

Expected: scope rajoittuu fonecheckin lähteisiin ja generoidut/build-polut on suljettu pois.

- [x] **Step 4: Aja `pc` eräajossa**

Expected: duplikaatit ovat `FINDINGS/1`, puhdas CPD-ajo `CLEAN/0` ja työkaluvika `ERROR/2`.

Tulos 2026-08-08: `FINDINGS/1`, tarkka source-set-scope, run ID `20260808T195131564-507ba6ac`.

### Task 8: `ms` / mobsf-scan

**Files:**
- Verify or modify: `tools/ms.ps1`
- Verify: `config/check-exceptions.json`

**Interfaces:**
- Consumes: Android-lähteet ja täsmällinen `fonecheck-mobsf-target-sdk`-poikkeus
- Produces: MobSF/mobsfscan-raportit nykyisen run ID:n alle

- [x] **Step 1: Varmista wrapperin täsmällinen sisältö**

```powershell
$ProjectCheckCommand = "mobsf-scan"
& "C:\Dev\Android-check\tools\InvokeProjectCheck.ps1" -ProjectCheckCommand $ProjectCheckCommand @args
exit $LASTEXITCODE
```

- [x] **Step 2: Aja `Test-PowerShellSyntax '.\tools\ms.ps1'` ja vaadi tulokseksi `True`**

- [x] **Step 3: Aja `& .\tools\ms.ps1 -PlanOnly`**

Expected: projekti on `fonecheck`, poikkeus kohdistuu vain sääntöön `android_task_hijacking2` ja tiedostoon `app/src/main/AndroidManifest.xml`, eikä poikkeus peitä muita löydöksiä.

- [x] **Step 4: Aja `ms` eräajossa**

Expected: poikkeuksen ulkopuoliset löydökset jäävät `FINDINGS/1`:ksi; vanhentunut tai liian laaja poikkeus on `ERROR/2`.

Tulos 2026-08-08: `CLEAN/0`, yksi täsmällisesti suppressioitu advisory, run ID `20260808T194219371-d688c129`.

### Task 9: `os` / osv-scan

**Files:**
- Verify or modify: `tools/os.ps1`

**Interfaces:**
- Consumes: wrapperin omasta sijainnista ratkaistu fonecheck-juuri
- Produces: `reports/osv.json` and `reports/osv-filtered.json`

- [x] **Step 1: Varmista wrapperin täsmällinen sisältö**

```powershell
$ProjectCheckCommand = "osv-scan"
$ProjectRoot = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot "..")).Path
& "C:\Dev\Android-check\tools\InvokeProjectCheck.ps1" -ProjectCheckCommand $ProjectCheckCommand -Root $ProjectRoot @args
exit $LASTEXITCODE
```

- [x] **Step 2: Aja `Test-PowerShellSyntax '.\tools\os.ps1'` ja vaadi tulokseksi `True`**

- [x] **Step 3: Aja `Push-Location ..; & .\fonecheck\tools\os.ps1 -PlanOnly; Pop-Location`**

Expected: kohde on silti `C:\Dev\fonecheck`; kutsujan nykyhakemisto ei muuta skannattavaa projektia.

- [x] **Step 4: Aja `os` eräajossa**

Expected: raakaversio säilyttää kaikki löydökset, hyväksytty suodatus näkyy erikseen ja todellinen haavoittuvuus on `FINDINGS/1`.

Tulos 2026-08-08: `FINDINGS/1`, 67 estävää OSV-löydöstä, run ID `20260808T194237420-89e65e2b`.

### Task 10: `ss` / secret-scan

**Files:**
- Verify or modify: `tools/ss.ps1`
- Verify: `config/check-exceptions.json`

**Interfaces:**
- Consumes: working tree, Git-ignoreen osuvat ja generoidut tiedostot sekä kaikki tavoitettavat Git-refit
- Produces: redaktoidut Gitleaks-, TruffleHog- ja Semgrep secret -raportit

- [x] **Step 1: Varmista wrapperin täsmällinen sisältö**

```powershell
$ProjectCheckCommand = "secret-scan"
& "C:\Dev\Android-check\tools\InvokeProjectCheck.ps1" -ProjectCheckCommand $ProjectCheckCommand @args
exit $LASTEXITCODE
```

- [x] **Step 2: Aja `Test-PowerShellSyntax '.\tools\ss.ps1'` ja vaadi tulokseksi `True`**

- [x] **Step 3: Aja `& .\tools\ss.ps1 -PlanOnly`**

Expected: suunnitelma kertoo working tree- ja Git-historiascopen, arkistot ja enkoodatun sisällön; raportteihin ei luvata raakasecret-kenttiä.

- [x] **Step 4: Aja `ss` eräajossa**

Expected: secret-löydös on `FINDINGS/1`; raporteissa ei saa näkyä varsinaista secret-arvoa, `Raw`-kenttää tai komentoriville vuotanutta tokenia.

Tulos 2026-08-08: `FINDINGS/1`; työpuussa 2 Gitleaks- ja 22 vahvistamatonta APK:n TruffleHog-osumaa, Git-historia puhdas, run ID `20260808T194558401-341a2334`.

### Task 11: `dc` / dependency-check

**Files:**
- Verify or modify: `tools/dc.ps1`
- Verify: `config/android-check.json`
- Verify: `config/dependency-check/suppressions.xml`

**Interfaces:**
- Consumes: `debugRuntimeClasspath`, `releaseRuntimeClasspath` and `:app:dependencyCheckAnalyze`
- Produces: Gradle verification-, OSV- ja OWASP Dependency-Check -raportit

- [x] **Step 1: Varmista wrapperin täsmällinen sisältö**

```powershell
$ProjectCheckCommand = "dependency-check"
& "C:\Dev\Android-check\tools\InvokeProjectCheck.ps1" -ProjectCheckCommand $ProjectCheckCommand @args
exit $LASTEXITCODE
```

- [x] **Step 2: Aja `Test-PowerShellSyntax '.\tools\dc.ps1'` ja vaadi tulokseksi `True`**

- [x] **Step 3: Aja `& .\tools\dc.ps1 -PlanOnly`**

Expected: molemmat runtime-konfiguraatiot, Gradle verification, OSV ja täsmälleen `:app:dependencyCheckAnalyze` näkyvät; OWASP-timeout on 2700 sekuntia.

- [ ] **Step 4: Anna käyttäjän hyväksyntätestiksi `dc` ja pysähdy**

Expected: käyttäjälle kerrotaan, että ensimmäinen OWASP/NVD-ajo voi kestää enintään 45 minuuttia; vanhentunut offline-data ei saa tuottaa `CLEAN`-tilaa.

Todellista ajoa ei ajettu: komento käynnistää Gradlen, jonka ajo Codexista on projektiohjeessa kielletty.

### Task 12: `ac` / android-check

**Files:**
- Verify or modify: `tools/ac.ps1`
- Verify: `config/android-check.json`
- Verify: `config/semgrep/fonecheck-security.yml`

**Interfaces:**
- Consumes: projektin Semgrep Android -config and mobsfscan
- Produces: paikallisen Android-security-yhteenvedon ilman DeepSeciä

- [x] **Step 1: Varmista wrapperin täsmällinen sisältö**

```powershell
$ProjectCheckCommand = "android-check"
& "C:\Dev\Android-check\tools\InvokeProjectCheck.ps1" -ProjectCheckCommand $ProjectCheckCommand @args
exit $LASTEXITCODE
```

- [x] **Step 2: Aja `Test-PowerShellSyntax '.\tools\ac.ps1'` ja vaadi tulokseksi `True`**

- [x] **Step 3: Aja `& .\tools\ac.ps1 -PlanOnly`**

Expected: config on `config/semgrep/fonecheck-security.yml`, mobsfscan kuuluu ajoon ja DeepSec ei kuulu ajoon missään muodossa.

- [x] **Step 4: Aja `ac` eräajossa**

Expected: paikalliset Android-security-löydökset ovat `FINDINGS/1`; komento ei pyydä tai käytä ulkoisen AI:n lupaa.

Tulos 2026-08-08: `CLEAN/0`, yksi MobSF-advisory, run ID `20260808T194835882-550c9ecc`.

### Task 13: `sc` / security-check

**Files:**
- Verify or modify: `tools/sc.ps1`
- Verify: `config/android-check.json`

**Interfaces:**
- Consumes: dependency-, working-tree secret- ja kevyt Semgrep -vaiheet; `-Full` lisää `ac`:n
- Produces: `reports/security-summary.txt` and phase-specific current-run reports

- [x] **Step 1: Varmista wrapperin täsmällinen sisältö**

```powershell
$ProjectCheckCommand = "security-check"
& "C:\Dev\Android-check\tools\InvokeProjectCheck.ps1" -ProjectCheckCommand $ProjectCheckCommand @args
exit $LASTEXITCODE
```

- [x] **Step 2: Aja `Test-PowerShellSyntax '.\tools\sc.ps1'` ja vaadi tulokseksi `True`**

- [x] **Step 3: Aja `& .\tools\sc.ps1 -PlanOnly` ja `& .\tools\sc.ps1 -Full -PlanOnly`**

Expected: oletus ja Full-scope eroavat vain dokumentoidusti; kumpikaan ei sisällä DeepSeciä, ja samaan ajoon kuuluvia vaiheita ei luvata ajettavaksi kahdesti.

- [ ] **Step 4: Anna käyttäjän hyväksyntätestiksi ensin `sc` ja pysähdy**

Expected: käyttäjä testaa `sc -Full` vasta perusajon jälkeen erikseen; tekninen vaihevirhe nostaa koko tuloksen `ERROR/2`:ksi.

Todellista ajoa ei ajettu: oletus- ja Full-polku sisältävät Gradle-taustaisen dependency-checkin.

### Task 14: `ql` / codeql-check

**Files:**
- Verify or modify: `tools/ql.ps1`
- Verify: `.github/workflows/android.yml`

**Interfaces:**
- Consumes: GitHubin oletushaaran CodeQL-ajo, alertit ja tarvittaessa nykyinen paikallinen Java/Kotlin/Gradle-sisältö
- Produces: CodeQL-tilaraportin, jossa remote- ja local-scope erotetaan

- [x] **Step 1: Varmista wrapperin täsmällinen sisältö**

```powershell
$ProjectCheckCommand = "codeql-check"
& "C:\Dev\Android-check\tools\InvokeProjectCheck.ps1" -ProjectCheckCommand $ProjectCheckCommand @args
exit $LASTEXITCODE
```

- [x] **Step 2: Aja `Test-PowerShellSyntax '.\tools\ql.ps1'` ja vaadi tulokseksi `True`**

- [x] **Step 3: Aja `& .\tools\ql.ps1 -PlanOnly`**

Expected: suunnitelma näyttää remoten oletushaaran, `java-kotlin`-scopen, mahdollisen paikallisen CodeQL-vaiheen, verkon käytön ja lisenssiedellytyksen ilman GitHub API- tai CLI-analyysia.

- [ ] **Step 4: Anna käyttäjän hyväksyntätestiksi `ql` ja pysähdy**

Expected: auth-, permission-, disabled-, no-run-, in-progress-, failure-, findings- ja clean-tilat eivät saa sekoittua toisiinsa.

Todellista ajoa ei ajettu: muuttuneessa työpuussa paikallinen CodeQL-polku käyttää manifestin Gradle-buildTaskeja.

### Task 15: `db` / dependabot-check

**Files:**
- Verify or modify: `tools/db.ps1`
- Verify: `config/android-check.json`

**Interfaces:**
- Consumes: manifestin nykyisen `dependabotEnabled: false` -päätöksen
- Produces: fonecheckissa tarkoituksellisen `NOT_APPLICABLE/3`-tuloksen ilman konfiguraation automaattista lisäämistä

- [x] **Step 1: Varmista wrapperin täsmällinen sisältö**

```powershell
$ProjectCheckCommand = "dependabot-check"
& "C:\Dev\Android-check\tools\InvokeProjectCheck.ps1" -ProjectCheckCommand $ProjectCheckCommand @args
exit $LASTEXITCODE
```

- [x] **Step 2: Aja `Test-PowerShellSyntax '.\tools\db.ps1'` ja vaadi tulokseksi `True`**

- [x] **Step 3: Aja `& .\tools\db.ps1 -PlanOnly`**

Expected: suunnitelma osoittaa Dependabotin olevan manifestissa pois käytöstä eikä lupaa GitHub-muutosta.

- [x] **Step 4: Aja `db` eräajossa**

Expected: nykyisellä manifestilla oikea tulos on `NOT_APPLICABLE/3`; Dependabotin käyttöönotto olisi erillinen käyttäjän päätös, ei tämän wrapperin sivuvaikutus.

Tulos 2026-08-08: `NOT_APPLICABLE/3`, run ID `20260808T194915346-bbef573c`.

### Task 16: `ds` / deep-sec

**Files:**
- Verify or modify: `tools/ds.ps1`
- Verify: `.deepsec/package.json`

**Interfaces:**
- Consumes: projektin paikallisen DeepSec 2.2.9 -asennuksen ja eksplisiittisen ulkoisen AI:n hyväksynnän
- Produces: DeepSec scan/report/custom/revalidate -ajon vain erikseen hyväksytyllä datascopella

- [x] **Step 1: Varmista wrapperin täsmällinen sisältö**

```powershell
$ProjectCheckCommand = "deep-sec"
& "C:\Dev\Android-check\tools\InvokeProjectCheck.ps1" -ProjectCheckCommand $ProjectCheckCommand @args
exit $LASTEXITCODE
```

- [x] **Step 2: Aja `Test-PowerShellSyntax '.\tools\ds.ps1'` ja vaadi tulokseksi `True`**

- [x] **Step 3: Aja vain `& .\tools\ds.ps1 -PlanOnly`**

Expected: ilman hyväksyntäparametreja suunnitelma näyttää `BLOCKED`-tilan ja luettelee vaaditut provider-, datascope-, kustannusarvio- ja retention-parametrit; mitään ei lähetetä eikä DeepSec-työkalua käynnistetä.

- [ ] **Step 4: Pysähdy pyytämään käyttäjältä erillinen todellisen DeepSec-ajon hyväksyntä**

Todellinen skannaus ei kuulu tähän taskiin. Jos käyttäjä pyytää sitä myöhemmin erikseen, kyseisessä pyynnössä on saatava eksplisiittinen provider, datascopen hyväksyntä, kustannusarvio ja retention-käytäntö; arvoja ei saa täydentää oletuksilla.

### Task 17: `sonar`-soveltuvuuspäätös

**Files:**
- Confirm absent: `tools/sonar.ps1`
- Inspect: `settings.gradle.kts`, `build.gradle.kts`, `app/build.gradle.kts` and `gradle/libs.versions.toml`

**Interfaces:**
- Consumes: todellisen SonarQube/SonarCloud-projektin avain-, host-, Gradle-plugin- ja Quality Gate -konfiguraation
- Produces: joko dokumentoidun `NOT_APPLICABLE`-päätöksen tai myöhemmin erikseen määriteltävän Sonar-integraatioprojektin

- [x] **Step 1: Todista, ettei projektikohtaista Sonar-entrypointia ole**

Run:

```powershell
Test-Path -LiteralPath '.\tools\sonar.ps1'
rg -n -i 'sonar|sonarqube' settings.gradle.kts build.gradle.kts app/build.gradle.kts gradle/libs.versions.toml
```

Expected: ensimmäinen komento palauttaa `False` eikä toinen löydä Sonar-konfiguraatiota. Generoitu `.sonar`-hakemisto ei yksin todista projektin olevan konfiguroitu.

- [x] **Step 2: Älä luo `tools/sonar.ps1`-tiedostoa nykyisillä tiedoilla**

Expected: fonecheckin Sonar pysyy tarkoituksella soveltumattomana. `sonar`-komentoa ei ajeta, koska PowerShell-profiili voisi pudota globaalin Sonar CLI:n käyttöön ilman projektisopimusta.

- [x] **Step 3: Raportoi päätös käyttäjälle ja pysähdy**

Jos käyttäjä myöhemmin haluaa SonarCloudin tai SonarQuben käyttöön, siitä tehdään erillinen suunnitelma, joka lukitsee palvelun, project keyn, upload-luvan, Quality Gaten ja secretien säilytyspaikan ennen wrapperin luomista.

---

## Final verification after all user-approved tasks

Kun Taskit 1–17 on käsitelty ja käyttäjä on testannut jokaisen soveltuvan komennon:

- [x] Aja vain PowerShell-parseri kaikille `tools\*.ps1`-tiedostoille; älä aja Gradlea tai skannereita.
- [x] Tarkista `git diff -- tools config .github .gitignore` ja varmista, että diff sisältää vain todistettujen vikojen minimaaliset korjaukset.
- [x] Varmista `git diff --check`.
- [x] Varmista jokaiselle todellisuudessa ajetulle komennolle run ID, semanttinen tila, exit-koodi ja nykyajon raporttipolut.
- [x] Varmista, ettei `ac`, `sc` tai `sc -Full` käynnistänyt DeepSeciä.
- [x] Varmista, ettei `rs.ps1`, `rst.ps1` tai perusteeton `sonar.ps1` ilmestynyt fonecheckiin.
- [x] Raportoi lopullisesti, mitkä taskit olivat nollamuutoksia, mitkä vaativat korjauksen ja mitkä jäivät tarkoituksella `NOT_APPLICABLE`-tilaan.
