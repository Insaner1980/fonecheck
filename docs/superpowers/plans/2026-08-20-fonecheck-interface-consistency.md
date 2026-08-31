# fonecheck Interface Consistency Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Yhdenmukaista 56 ruutukaappauksessa havaittu käyttöliittymä, tehdä dark themen ei-semanttisesta kromista aidosti mustavalkoinen, säilyttää PASS-, ATTENTION- ja FAIL-statusvärit sekä korjata epäselvät termit, tietoyksiköt ja asettelut.

**Architecture:** Värisopimus korjataan ensin teematokeneissa ja Material 3 -värikartassa. Näytöt käyttävät sen jälkeen vain joko neutraaleja toimintorooleja tai `SemanticTone`-statusrooleja. Teksti- ja asettelukorjaukset tehdään olemassa oleviin näyttöihin ja shared-komponentteihin ilman uusia sovelluskerroksia tai muutoksia diagnostiikkadataan.

**Tech Stack:** Kotlin 2.4.10, Jetpack Compose, Material 3, AndroidX Activity edge-to-edge, JUnit 4, Compose UI tests, Android-resurssit EN/FI.

**Spec:** Käyttäjän hyväksymä rajaus 20.8.2026: statusvärit säilyvät; dark theme on muuten mustavalkoinen. Visuaalinen lähtöaineisto on `C:\Users\EmmaH\Downloads\fonecheck-screenshots` (28 light- ja 28 dark-kuvaa). Tämä suunnitelma kattaa kuvista varmennetut 11 havaintoa.

## Global Constraints

- Dark themen statusvärit säilyvät ennallaan: `PASS #3FB98A`, `ATTENTION #E8B04B` ja `FAIL #E8736B`. ATTENTION-statuksen täyttöväri saa säilyä kultaisena, mutta vain `SemanticTone.ATTENTION`-polun kautta.
- Dark themen painikkeet, valitut kytkimet ja valintaruudut, etenemisilmaisimet, logo ja muu ei-semanttinen kromi käyttävät vain tausta-, teksti-, harmaa- ja sääntörooleja. Niissä ei saa näkyä kultaa.
- Display-testin `Green400`- ja `Yellow400`-ärsykevärit eivät ole sovelluskromia. Niitä ei muuteta, koska ne ovat osa testisyötettä.
- Light themen statusvärit säilyvät. Neutraalit etenemis- ja toimintotilat käyttävät samaa roolijakoa kuin dark theme, jotta teemat eivät perustu eri semantiikkaan.
- `DiagnosticStatus.toSemanticTone()` ja `ObservationClassification.toSemanticTone()` säilyvät ainoina statusluokituksen värikäännöksinä. Näytöt eivät saa nimetä raakavärejä.
- Pisteytys, coverage-laskenta, kategorioiden määrä, raporttien sisältö, mittauslogiikka, ViewModel-tilat, tallennusmallit ja navigaatioreitit eivät muutu.
- Suuri readout säilyy vain Storage- ja Full Check -näkymissä, joissa sillä on jo rehellinen kategorian tai raportin päätotuus. Battery- ja Thermal-näkymiin ei lisätä readoutia.
- Jokainen lisätty tai muutettu käyttäjäteksti tehdään samalla muutoksella sekä `values/strings.xml`- että `values-fi/strings.xml`-tiedostoon. Composableihin ei lisätä kovakoodattuja käyttäjätekstejä.
- Lausemuoto säilyy toimintoteksteissä. `SectionHeader` tekee oman visuaalisen uppercase-muunnoksensa.
- Shared-komponentteja käytetään aina, kun nykyinen komponentti sopii. Näyttökohtainen yksityinen composable sallitaan vain, kun esitystapa on aidosti yhden näytön rakenne, kuten Full Check -esitietojen tekstiryhmä.
- 48 dp kosketuskohteet, nykyinen 8 dp spacing-grid, ei kortteja, varjoja, liukuvärejä tai uusia radius-arvoja.
- Toteutus alkaa nykyisestä dirty worktreestä. Suunnitelman kohdetiedostoista ainakin `HomeScreen.kt`, `RunAllManualSteps.kt`, `RunAllResultsScreen.kt`, `MainActivity.kt`, `ButtonTestScreen.kt`, `SensorTestScreen.kt`, `SimTelephonyScreen.kt`, `StorageTestScreen.kt`, `SemanticTone.kt`, molemmat `strings.xml`-tiedostot, `FoundationPreviews.kt` ja niihin liittyviä testejä on jo muokattu. Näitä muutoksia ei saa palauttaa eikä peittää.
- Codex ei aja Gradlea tässä projektissa. Alla olevat Gradle-komennot ovat käyttäjän manuaalisesti ajettavia hyväksymisportteja, yksi komento kerrallaan.
- Commit on jokaisessa tehtävässä ehdollinen: commit tehdään vain, jos juuri tämän suunnitelman hunks voidaan erottaa varmasti aiemmasta käyttäjätyöstä. Muuten muutokset jätetään committoimatta ja raportoidaan diff-tasolla.
- `CODE_REVIEW.md`:stä revalidoidaan toteutuksen aikana vain osuvat kohdat: button state management, Home-kategoriarivien lähde ja EN/FI-pariteetti. Tiedostoa ei päivitetä tämän UI-tehtävän sivuvaikutuksena.

## Verified Finding-to-Task Map

| # | Varmennettu havainto | Korjaava tehtävä | Valmis, kun |
|---|---|---|---|
| 1 | Dark themen painikkeet ja valitut kontrollit ovat kultaisia | Task 1 | Kaikki ei-semanttiset dark-kontrollit ovat valkoinen/musta/harmaa |
| 2 | Etusivun monivärinen merkki rikkoo instrumenttipaletin | Task 1 | Merkki tintataan `textPrimary`-roolilla |
| 3 | `Run All Tests` ja `Full Check` ovat rinnakkaisia nimiä samalle työnkululle | Task 2 | Otsikko on `Full Check`, CTA on `Start Full Check` / `Aloita Full Check` |
| 4 | Home-readout sekoittaa kategoriat, evidenssin coveragen ja evidenssihavainnot | Task 2 | Jokainen luku nimeää oman yksikkönsä |
| 5 | Home-kategoriarivit näyttävät tallennetun Full Checkin tilaa mutta johtavat uuteen mittaukseen | Task 2 | Rivien tietolähde kerrotaan näkyvästi |
| 6 | Full Check -esitiedot ovat seitsemän tasavahvaa pientä Note-kappaletta | Task 3 | Sisältö on kolmessa otsikoidussa tekstiryhmässä normaalilla body-hierarkialla |
| 7 | History yhdistää pitkän raporttitilan ja ajan samaan SectionHeader-riviin | Task 4 | Raportin tyyppi, tila ja valmistumisaika ovat erillisiä kenttiä |
| 8 | Storage-readoutin raakakontekstissa `total` jää yksin riville | Task 5 | Käytetty/vapaa ja yhteensä ovat kaksi determinististä tukiriviä |
| 9 | `Phone count` nimeää Androidin aktiivisten modeemien määrän väärin | Task 6 | Näytössä lukee `Active modems` / `Aktiiviset modeemit` |
| 10 | Volume buttons -nollaus näkyy IDLE-tilassa mutta ei tee mitään | Task 7 | Reset näkyy vain tiloissa, joissa nollattavaa tilaa on |
| 11 | Androidin kolmen painikkeen navigaatiopalkki muodostaa eri sävyisen sauman | Task 8 | API 29+ contrast scrim poistetaan ja laitetarkistus hyväksyy molemmat navigaatiotavat |

## Intentional Non-Changes

- Battery- ja Thermal-näkymät jäävät ilman suurta headline-readoutia, koska kummassakaan ei ole yhtä rehellistä koko kategorian päätotuutta.
- Stale Full Check -tulokset säilyvät mutettuina. Korjaus lisää tietolähteen, ei palauta vanhoille tuloksille tuoretta statusväriä.
- Historyn Delete-painike säilyy neutraalina, koska poistaminen varmistetaan erillisessä dialogissa. Punainen varataan virheelle tai FAIL-statukselle.
- Kameran pitkät mittausnimet säilyvät, koska ne ovat sisällöllisesti täsmällisiä ja nykyinen `LongValueRow`/rivitys käsittelee ne.
- Display-testin ärsykevärit, splash, launcher icon ja paketoidut logoassetit eivät kuulu dark themen sovelluskromiin eikä niitä muuteta.
- Kategoriarivien tallennettu Full Check -lähde säilyy nykyisenä tuotepäätöksenä. Tämä työ tekee lähteen näkyväksi; se ei sekoita uudempia category-only-retestejä Home-yhteenvetoon.

## Expected File Scope

**Theme and shared presentation**

- Modify: `app/src/main/java/com/insaner/fonecheck/ui/theme/Color.kt`
- Modify: `app/src/main/java/com/insaner/fonecheck/ui/theme/FonecheckColors.kt`
- Modify: `app/src/main/java/com/insaner/fonecheck/ui/theme/Theme.kt`
- Modify: `app/src/main/java/com/insaner/fonecheck/ui/theme/SemanticTone.kt`
- Modify: `app/src/main/java/com/insaner/fonecheck/ui/components/HeadlineReadout.kt`
- Modify: `app/src/debug/java/com/insaner/fonecheck/ui/preview/FoundationPreviews.kt`

**Screens and navigation**

- Modify: `app/src/main/java/com/insaner/fonecheck/navigation/NavigationChrome.kt`
- Modify: `app/src/main/java/com/insaner/fonecheck/ui/MainActivity.kt`
- Modify: `app/src/main/java/com/insaner/fonecheck/ui/screens/home/HomeScreen.kt`
- Modify: `app/src/main/java/com/insaner/fonecheck/ui/screens/onboarding/OnboardingScreen.kt`
- Modify: `app/src/main/java/com/insaner/fonecheck/ui/screens/sensor/SensorTestScreen.kt`
- Modify: `app/src/main/java/com/insaner/fonecheck/ui/screens/runall/RunAllManualSteps.kt`
- Modify: `app/src/main/java/com/insaner/fonecheck/ui/screens/runall/RunAllResultsScreen.kt`
- Modify: `app/src/main/java/com/insaner/fonecheck/ui/screens/history/HistoryScreen.kt`
- Modify: `app/src/main/java/com/insaner/fonecheck/ui/screens/storage/StorageTestScreen.kt`
- Modify: `app/src/main/java/com/insaner/fonecheck/ui/screens/simtelephony/SimTelephonyScreen.kt`
- Modify: `app/src/main/java/com/insaner/fonecheck/ui/screens/buttons/ButtonTestScreen.kt`

**Resources and tests**

- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/res/values-fi/strings.xml`
- Modify: `app/src/test/java/com/insaner/fonecheck/ui/theme/SemanticColorTest.kt`
- Modify: `app/src/androidTest/java/com/insaner/fonecheck/ui/theme/FonecheckThemeSmokeTest.kt`
- Modify: `app/src/test/java/com/insaner/fonecheck/navigation/NavigationChromeTest.kt`
- Modify: `app/src/androidTest/java/com/insaner/fonecheck/ui/screens/home/HomeContentTest.kt`
- Modify: `app/src/androidTest/java/com/insaner/fonecheck/ui/screens/runall/FullCheckPreflightScreenTest.kt`
- Modify: `app/src/androidTest/java/com/insaner/fonecheck/ui/screens/history/HistoryScreenTest.kt`
- Create: `app/src/androidTest/java/com/insaner/fonecheck/ui/components/HeadlineReadoutTest.kt`
- Modify: `app/src/test/java/com/insaner/fonecheck/ui/screens/buttons/ButtonPresentationPolicyTest.kt`
- Modify: `app/src/test/java/com/insaner/fonecheck/localization/ResourceParityTest.kt`
- Create: `app/src/androidTest/java/com/insaner/fonecheck/ui/MainActivitySystemBarsTest.kt`

---

## Task 0: Protect the Current Source of Truth

**Files:** Read-only inspection of all paths in “Expected File Scope”.

**Purpose:** Varmistaa, että ruutukaappauksia vastaava nykyinen toteutus ja käyttäjän aiemmat muokkaukset säilyvät.

- [ ] Run a path-scoped status snapshot before editing:

  ```powershell
  git status --short -- app/src/main app/src/test app/src/androidTest app/src/debug docs/superpowers/plans
  ```

- [ ] Save a read-only diff inventory for every already modified target file:

  ```powershell
  git diff -- app/src/main/java/com/insaner/fonecheck/ui app/src/main/java/com/insaner/fonecheck/navigation app/src/main/res app/src/test app/src/androidTest app/src/debug
  ```

- [ ] Confirm that the untracked `SimTelephonyPresentationTest.kt` is user work and must remain present.

- [ ] Re-read the current implementations immediately before each edit. Do not copy code from `HEAD` over a dirty file.

- [ ] Record the screenshot pairing rule for final QA: files 1–28 by timestamp are the light sequence, files 29–56 are the corresponding dark sequence.

- [ ] Do not commit in this task.

---

## Task 1: Separate Semantic Status Colour from Monochrome Interaction Colour

**Files:**

- Modify: `app/src/main/java/com/insaner/fonecheck/ui/theme/Color.kt`
- Modify: `app/src/main/java/com/insaner/fonecheck/ui/theme/FonecheckColors.kt`
- Modify: `app/src/main/java/com/insaner/fonecheck/ui/theme/Theme.kt`
- Modify: `app/src/main/java/com/insaner/fonecheck/ui/theme/SemanticTone.kt`
- Modify: `app/src/main/java/com/insaner/fonecheck/ui/screens/onboarding/OnboardingScreen.kt`
- Modify: `app/src/main/java/com/insaner/fonecheck/ui/screens/sensor/SensorTestScreen.kt`
- Modify: `app/src/main/java/com/insaner/fonecheck/ui/screens/home/HomeScreen.kt`
- Modify: `app/src/debug/java/com/insaner/fonecheck/ui/preview/FoundationPreviews.kt`
- Modify: `app/src/test/java/com/insaner/fonecheck/ui/theme/SemanticColorTest.kt`
- Modify: `app/src/androidTest/java/com/insaner/fonecheck/ui/theme/FonecheckThemeSmokeTest.kt`

**Interfaces:**

- Rename `FonecheckColors.accentFill` to `attentionFill`.
- Rename `AccentFillLight` / `AccentFillDark` to `AttentionFillLight` / `AttentionFillDark`.
- Delete `OnAccentDark`; no dark generic control puts text on a gold fill after this task.
- Keep `SemanticTone.ATTENTION.fillColor()` mapped to `attentionFill`.
- Generic progress maps to `primaryButtonBackground`, not a status tone.

### Step 1: Write failing theme-contract tests

- [ ] Add unit assertions proving the dark interaction pair is monochrome while status roles retain their exact values:

  ```kotlin
  @Test
  fun `dark interaction chrome is monochrome while status colours remain semantic`() {
      assertEquals(InkDark, DarkFonecheckColors.primaryButtonBackground)
      assertEquals(PaperDark, DarkFonecheckColors.primaryButtonContent)
      assertEquals(PassDark, DarkFonecheckColors.pass)
      assertEquals(AttentionDark, DarkFonecheckColors.attention)
      assertEquals(AttentionFillDark, DarkFonecheckColors.attentionFill)
      assertEquals(FailDark, DarkFonecheckColors.fail)
  }
  ```

- [ ] Extend `FonecheckThemeSmokeTest` to capture `MaterialTheme.colorScheme` inside `FonecheckTheme(darkTheme = true)` and assert:

  ```kotlin
  assertEquals(InkDark, materialPrimary)
  assertEquals(PaperDark, materialOnPrimary)
  assertEquals(InkDark2, materialSecondary)
  assertEquals(InkDark3, materialTertiary)
  ```

- [ ] User runs the red tests one command at a time. Expected failure: current dark primary is `AccentFillDark`, and secondary/tertiary are `AttentionDark`.

  ```powershell
  .\gradlew.bat :app:testDebugUnitTest --tests "com.insaner.fonecheck.ui.theme.SemanticColorTest"
  ```

  ```powershell
  .\gradlew.bat :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.insaner.fonecheck.ui.theme.FonecheckThemeSmokeTest
  ```

### Step 2: Implement the token split

- [ ] Keep the actual status colors unchanged and make the names state their role:

  ```kotlin
  internal val AttentionFillLight = Color(0xFFB5761A)
  internal val AttentionFillDark = Color(0xFFE8A33D)
  ```

- [ ] Update `FonecheckColors` so its contract says color appears only when it carries diagnostic meaning; a primary action is neutral ink, not an accent.

- [ ] Set the dark control pair exactly as follows:

  ```kotlin
  primaryButtonBackground = InkDark,
  primaryButtonContent = PaperDark,
  ```

- [ ] Project generic Material roles onto monochrome tokens in both themes:

  ```kotlin
  primary = primaryButtonBackground,
  onPrimary = primaryButtonContent,
  inversePrimary = primaryButtonBackground,
  secondary = textSecondary,
  onSecondary = background,
  tertiary = textMuted,
  onTertiary = background,
  error = fail,
  ```

- [ ] Keep `error = fail`; this is an actual semantic status slot. Do not map ATTENTION to Material `secondary` or `tertiary`.

- [ ] Change only the three non-status `attentionFill` consumers:

  ```kotlin
  // Onboarding progress
  color = FonecheckTheme.colors.primaryButtonBackground

  // Sensor challenge before completion
  color = if (challenge.completed) FonecheckTheme.colors.pass else FonecheckTheme.colors.primaryButtonBackground

  // Sensor sampling progress
  color = FonecheckTheme.colors.primaryButtonBackground
  ```

- [ ] Confirm the existing Run All progress indicators already use `primaryButtonBackground`; do not introduce a second progress token.

- [ ] Tint the Home mark instead of changing the packaged bitmap:

  ```kotlin
  Image(
      painter = painterResource(R.drawable.fonecheck_mark),
      contentDescription = null,
      colorFilter = ColorFilter.tint(FonecheckTheme.colors.textPrimary),
      modifier = Modifier.size(FonecheckTheme.spacing.xxl),
  )
  ```

- [ ] Leave `fonecheck_mark.webp`, splash resources and launcher icons unchanged. This plan governs in-app UI, not OS-owned branding surfaces.

- [ ] Update the debug specimen wording from “Run all checks” to “Start Full Check” so the preview demonstrates the final vocabulary.

### Step 3: Prove the color boundary

- [ ] User reruns the two green test commands from Step 1. Expected: pass.

- [ ] Run static role scans:

  ```powershell
  rg -n "AccentFill|accentFill|OnAccentDark" app/src/main app/src/test app/src/androidTest app/src/debug
  ```

  Expected: no matches.

  ```powershell
  rg -n "attentionFill" app/src/main app/src/test app/src/androidTest app/src/debug
  ```

  Expected: palette construction, semantic fill mapping and explicit tests only; no screen imports.

- [ ] Inspect the dark Foundation preview: primary/disabled/secondary actions and progress are grayscale; PASS, ATTENTION and FAIL specimen rows remain green, amber and red.

- [ ] Conditional commit gate:

  ```powershell
  git diff --check -- app/src/main/java/com/insaner/fonecheck/ui/theme app/src/main/java/com/insaner/fonecheck/ui/screens/onboarding app/src/main/java/com/insaner/fonecheck/ui/screens/sensor app/src/main/java/com/insaner/fonecheck/ui/screens/home app/src/debug app/src/test/java/com/insaner/fonecheck/ui/theme app/src/androidTest/java/com/insaner/fonecheck/ui/theme
  ```

  If and only if pre-existing hunks are safely isolated, commit with: `Yhtenäistä tumman teeman toimintovärit`.

---

## Task 2: Standardize Full Check Vocabulary and Make Home Metrics Honest

**Files:**

- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/res/values-fi/strings.xml`
- Modify: `app/src/main/java/com/insaner/fonecheck/navigation/NavigationChrome.kt`
- Modify: `app/src/main/java/com/insaner/fonecheck/ui/screens/home/HomeScreen.kt`
- Modify: `app/src/test/java/com/insaner/fonecheck/navigation/NavigationChromeTest.kt`
- Modify: `app/src/androidTest/java/com/insaner/fonecheck/ui/screens/home/HomeContentTest.kt`
- Modify: `app/src/debug/java/com/insaner/fonecheck/ui/preview/FoundationPreviews.kt`

**Interfaces and locked copy:**

| Key | English | Finnish |
|---|---|---|
| `full_check_title` | `Full Check` | `Full Check` |
| `home_start_full_check` | `Start Full Check` | `Aloita Full Check` |
| `home_latest_passed_label` | `Categories passed` | `Läpäistyt kategoriat` |
| `home_latest_passed_description` | `%1$s of %2$s categories passed` | `Läpäistyjä kategorioita %1$s / %2$s` |
| `home_latest_coverage_value` | `Evidence coverage %1$s` | `Evidenssin kattavuus %1$s` |
| `home_categories_report_source` | `Statuses below come from the latest Full Check. Open a category to view current data.` | `Alla olevat tilat ovat viimeisimmästä Full Check -raportista. Avaa kategoria nähdäksesi ajantasaiset tiedot.` |

Rename the attention plural to `home_latest_evidence_attention_summary`:

- English one/other: `%1$d evidence item needs attention` / `%1$d evidence items need attention`.
- Finnish one/other: `%1$d evidenssikohde vaatii huomiota` / `%1$d evidenssikohdetta vaatii huomiota`.

### Step 1: Write failing navigation and Home assertions

- [ ] Change `NavigationChromeTest` to expect `R.string.full_check_title` for `RunAllTests`.

- [ ] Update the Home Compose test to expect `home_start_full_check`, not the route title.

- [ ] Extend `ResourceParityTest` with a focused exact-vocabulary test that reads both XML files and initially expects the locked `full_check_title` and `home_start_full_check` values. This is a product-language contract, not a broad snapshot of every string.

- [ ] Add one small XML lookup helper to that existing test file:

  ```kotlin
  private fun stringValue(
      file: File,
      key: String,
  ): String {
      val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file)
      val nodes = document.getElementsByTagName("string")
      for (index in 0 until nodes.length) {
          val element = nodes.item(index) as Element
          if (element.getAttribute("name") == key) return element.textContent
      }
      error("String resource $key was not found in $file")
  }
  ```

- [ ] The exact-vocabulary test must compare both locale files for only these deliberate product terms; it must not freeze ordinary explanatory paragraphs.

- [ ] Add assertions for an available report:

  - the readout label says categories;
  - the accessibility state description says categories;
  - coverage explicitly says evidence;
  - attention explicitly counts evidence items;
  - `home_categories_report_source` is visible above category rows.

- [ ] Add an empty-state assertion that the report-source note is absent when there is no saved Full Check.

- [ ] Add or retain a 200% font-scale Home test and require the two metric lines to remain displayed without horizontal clipping.

- [ ] User runs red tests one at a time. Expected: new resource IDs or assertions fail before implementation.

  ```powershell
  .\gradlew.bat :app:testDebugUnitTest --tests "com.insaner.fonecheck.navigation.NavigationChromeTest"
  ```

  ```powershell
  .\gradlew.bat :app:testDebugUnitTest --tests "com.insaner.fonecheck.localization.ResourceParityTest"
  ```

  ```powershell
  .\gradlew.bat :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.insaner.fonecheck.ui.screens.home.HomeContentTest
  ```

### Step 2: Split route title from Home CTA

- [ ] Replace the overloaded `home_run_all` resource with `full_check_title` and `home_start_full_check` in both locales.

- [ ] Map the `RunAllTests` route to `full_check_title` in `NavigationChrome.kt`.

- [ ] Use `home_start_full_check` in both the actual Home CTA and Home preview.

- [ ] Do not rename the Kotlin route `RunAllTests`; it is an internal identifier, not user-facing copy.

### Step 3: Expose metric units without changing calculations

- [ ] Keep `HomeReportPresentation` calculations exactly as they are:

  - `passCount` and `totalCategories` count categories;
  - `report.coverage.percentage` counts applicable/completed evidence;
  - `attentionCount` counts warning/failure evidence items.

- [ ] Change only the visual and accessibility strings listed above.

- [ ] Replace `LatestCheckInfoLine`'s single `Row` with a deterministic two-line `Column` using the current type and color roles:

  ```kotlin
  Column(
      modifier = Modifier.fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(FonecheckTheme.spacing.xs),
  ) {
      Text(
          text = coverage.uppercase(LocalLocale.current.platformLocale),
          style = FonecheckTheme.type.sectionLabel,
          color = FonecheckTheme.colors.textMuted,
          modifier = Modifier.semantics { contentDescription = coverage },
      )
      if (emphasized) {
          StatusText(text = attention, tone = attentionTone)
      } else {
          Text(
              text = attention.uppercase(LocalLocale.current.platformLocale),
              style = FonecheckTheme.type.sectionLabel,
              color = FonecheckTheme.colors.textMuted,
              modifier = Modifier.semantics { contentDescription = attention },
          )
      }
  }
  ```

- [ ] Preserve stale-report muting. A stale result remains neutral/muted rather than carrying old verdict color.

### Step 4: Label the category-row source

- [ ] Under the `Categories` SectionHeader, render `Note(home_categories_report_source)` only when `latestFullCheck is LatestFullCheckState.Available`.

- [ ] Give the note vertical spacing with existing `sm`/`md` tokens; do not add a card, icon or tooltip.

- [ ] Keep category click behavior unchanged. The note explains why a saved-report status and a live category destination coexist.

### Step 5: Verify vocabulary and layout

- [ ] User reruns both test commands. Expected: pass.

- [ ] User runs the localization contract:

  ```powershell
  .\gradlew.bat :app:testDebugUnitTest --tests "com.insaner.fonecheck.localization.ResourceParityTest"
  ```

- [ ] Static vocabulary scan:

  ```powershell
  rg -n "home_run_all|Run All Tests|Aja kaikki testit|checks passed|tarkistuksia.*läpä" app/src/main app/src/test app/src/androidTest app/src/debug
  ```

  Expected: no user-facing stale terminology. Internal `RunAllTests` identifiers are allowed and will appear only if the search includes code symbols.

- [ ] Inspect Home in EN and FI, light and dark, at 100% and 200% font scale. Confirm the longer evidence labels wrap vertically, never overlap.

- [ ] Conditional commit message: `Selkeytä Full Checkin kotinäkymä`.

---

## Task 3: Rebuild Full Check Preflight as a Readable Disclosure Hierarchy

**Files:**

- Modify: `app/src/main/java/com/insaner/fonecheck/ui/screens/runall/RunAllManualSteps.kt`
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/res/values-fi/strings.xml`
- Modify: `app/src/androidTest/java/com/insaner/fonecheck/ui/screens/runall/FullCheckPreflightScreenTest.kt`

**Locked information architecture:**

1. `What happens` / `Mitä tapahtuu`
   - interaction paragraph;
   - conditional audio/vibration paragraph;
   - temporary storage benchmark paragraph.
2. `Permissions and control` / `Luvat ja hallinta`
   - permission timing and denial behavior;
   - unsupported hardware and skip/exit behavior.
3. `Privacy` / `Tietosuoja`
   - local report and excluded raw data;
   - no internet/network-speed test.
4. `Optional tests` / `Valinnaiset testit`
   - the four existing checkbox rows.

**Locked heading keys:**

| Key | English | Finnish |
|---|---|---|
| `run_all_preflight_what_happens_title` | `What happens` | `Mitä tapahtuu` |
| `run_all_preflight_permissions_control_title` | `Permissions and control` | `Luvat ja hallinta` |
| `run_all_preflight_privacy_title` | `Privacy` | `Tietosuoja` |
| `run_all_preflight_choices_title` | `Optional tests` | `Valinnaiset testit` |

### Step 1: Make the current flat disclosure fail its intended hierarchy test

- [ ] Extend `FullCheckPreflightScreenTest` to assert all three group headings, `Optional tests`, the storage disclosure, the privacy disclosure and all four selectable options.

- [ ] Keep the interaction assertion that changing a checkbox is reflected in the selections passed to `onContinue`.

- [ ] Update the `showWarnings = false` test: the three disclosure headings and their body text are absent, but `Optional tests` and the checkboxes remain.

- [ ] User runs the red instrumentation test:

  ```powershell
  .\gradlew.bat :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.insaner.fonecheck.ui.screens.runall.FullCheckPreflightScreenTest
  ```

  Expected: group-heading and renamed-choice assertions fail.

### Step 2: Implement one local disclosure-group composable

- [ ] Replace `PreflightDisclosure(@StringRes textResId)` with a private screen-specific component. It must use a shared `SectionHeader` and ordinary body typography, not `Note`:

  ```kotlin
  @Composable
  private fun PreflightDisclosureGroup(
      title: String,
      paragraphs: List<String>,
  ) {
      Column(verticalArrangement = Arrangement.spacedBy(FonecheckTheme.spacing.sm)) {
          SectionHeader(title)
          paragraphs.forEach { paragraph ->
              Text(
                  text = paragraph,
                  style = FonecheckTheme.type.rowLabel,
                  color = FonecheckTheme.colors.textSecondary,
              )
          }
      }
  }
  ```

- [ ] Call it exactly three times in the order defined above. Keep `run_all_preflight_description` as the short Note under the screen's first header because it is a qualifier, not the disclosure body.

- [ ] Do not add a new shared component. No other screen has this disclosure structure.

### Step 3: Fix unclear copy in both locales

- [ ] Add the three group-heading resources.

- [ ] Rename `run_all_preflight_choices_title` copy to `Optional tests` / `Valinnaiset testit`.

- [ ] Replace the awkward audio paragraph with:

  - EN: `If selected, the speaker test plays a short tone and the microphone test records a brief sample for a relative level reading. Vibration can feel strong.`
  - FI: `Jos valitset äänitestit, kaiutintesti toistaa lyhyen äänen ja mikrofonitesti tallentaa lyhyen näytteen suhteellista tasolukemaa varten. Värinä voi tuntua voimakkaalta.`

- [ ] Preserve every factual disclosure: 64 MiB temporary private-cache file, permission timing, denial semantics, unsupported hardware, skip/exit, on-device report, excluded raw data and absence of a network-speed test.

### Step 4: Validate hierarchy and accessibility

- [ ] User reruns the test and `ResourceParityTest`. Expected: pass.

- [ ] Inspect at 360 dp width and 200% font scale in both locales. Headings must remain distinct; paragraphs must not use muted 12/18 Note typography; checkboxes must retain 48 dp rows.

- [ ] Confirm TalkBack order: screen intro → three disclosure groups → optional tests → Review permissions.

- [ ] Conditional commit message: `Jäsennä Full Checkin esitiedot`.

---

## Task 4: Separate History Report Type, Completion State and Timestamp

**Files:**

- Modify: `app/src/main/java/com/insaner/fonecheck/ui/screens/history/HistoryScreen.kt`
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/res/values-fi/strings.xml`
- Modify: `app/src/androidTest/java/com/insaner/fonecheck/ui/screens/history/HistoryScreenTest.kt`

**Locked presentation:**

- `SectionHeader` contains only report type: `Full Check`, `Category retest · Storage`, or `Unavailable report`.
- A `DataRow` named `Status` / `Tila` contains `Complete`, `Partial`, `Incomplete`, or `Unavailable` and stays neutral because it describes report completeness, not device health.
- A `DataRow` named `Completed` / `Valmistui` contains the formatted timestamp.
- Report ID, score, coverage, issue summary and actions keep their current order after those rows.

### Step 1: Write the failing history layout contract

- [ ] Change the existing partial-report assertion from the combined `history_partial_check` heading to three independent assertions: `Full Check`, `Status`, and `Partial`.

- [ ] Assert the displayed formatted completion time separately.

- [ ] For a category-only report, assert `Category retest · Storage` and `Complete` independently.

- [ ] For an unavailable report, assert the unavailable type and unavailable status without exposing an invalid score.

- [ ] User runs the red test:

  ```powershell
  .\gradlew.bat :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.insaner.fonecheck.ui.screens.history.HistoryScreenTest
  ```

### Step 2: Split kind and state resources

- [ ] Keep `history_full_check` and `history_category_retest`.

- [ ] Remove the combined `history_partial_check` and `history_incomplete_check` resources.

- [ ] Add these paired resources:

  | Key | English | Finnish |
  |---|---|---|
  | `history_status` | `Status` | `Tila` |
  | `history_status_complete` | `Complete` | `Valmis` |
  | `history_status_partial` | `Partial` | `Osittainen` |
  | `history_status_incomplete` | `Incomplete` | `Keskeneräinen` |
  | `history_status_unavailable` | `Unavailable` | `Ei käytettävissä` |
  | `history_completed` | `Completed` | `Valmistui` |

### Step 3: Implement explicit rows

- [ ] Make `historyKindLabel(report)` branch only on availability/kind, not `scoreState`.

- [ ] Add `historyStatusLabel(report)` that maps `scoreState` to the four locked strings.

- [ ] Replace the trailing timestamp header with:

  ```kotlin
  SectionHeader(label = historyKindLabel(report))
  DataRow(
      label = stringResource(R.string.history_status),
      value = historyStatusLabel(report),
  )
  DataRow(
      label = stringResource(R.string.history_completed),
      value = completedAt,
  )
  ```

- [ ] Keep these rows neutral. Do not color `Complete` green: a complete report may contain warnings or failures.

### Step 4: Verify long-copy behavior

- [ ] User reruns `HistoryScreenTest` and `ResourceParityTest`. Expected: pass.

- [ ] Inspect EN/FI at 360 dp and 200% font scale. No timestamp shares the SectionHeader measurement row, and no type/state wording wraps into an ambiguous phrase.

- [ ] Static obsolete-key scan:

  ```powershell
  rg -n "history_partial_check|history_incomplete_check" app/src
  ```

  Expected: no matches.

- [ ] Conditional commit message: `Selkeytä raporttihistorian otsikot`.

---

## Task 5: Give the Storage Headline Deterministic Supporting Lines

**Files:**

- Modify: `app/src/main/java/com/insaner/fonecheck/ui/components/HeadlineReadout.kt`
- Modify: `app/src/main/java/com/insaner/fonecheck/ui/screens/storage/StorageTestScreen.kt`
- Modify: `app/src/main/java/com/insaner/fonecheck/ui/screens/runall/RunAllResultsScreen.kt`
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/res/values-fi/strings.xml`
- Create: `app/src/androidTest/java/com/insaner/fonecheck/ui/components/HeadlineReadoutTest.kt`

**Interface change:** Replace `rawValues: String` with `supportingLines: List<String>`. Both existing call sites are updated in the same task.

### Step 1: Write a failing two-line component test

- [ ] Render `HeadlineReadout` with `supportingLines = listOf("27.6 GB used · 98.3 GB free", "128 GB total")`.

- [ ] Assert both strings are displayed as independent semantic text nodes and the large `42.1 %` claim remains displayed.

- [ ] Also render a one-line supporting list to preserve the Full Check score use case.

- [ ] User runs the red test. Expected before implementation: the new parameter does not exist.

  ```powershell
  .\gradlew.bat :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.insaner.fonecheck.ui.components.HeadlineReadoutTest
  ```

### Step 2: Implement the shared readout API

- [ ] Replace the single supporting `Text` with an end-aligned column:

  ```kotlin
  Column(horizontalAlignment = Alignment.End) {
      supportingLines.forEach { line ->
          Text(
              text = line,
              style = FonecheckTheme.type.sectionLabel,
              color = FonecheckTheme.colors.textMuted,
              textAlign = TextAlign.End,
          )
      }
  }
  ```

- [ ] Preserve the outer `FlowRow`, readout/value baseline behavior and honest-headline contract.

### Step 3: Split localized Storage context

- [ ] Replace `storage_usage_context` with:

  | Key | English | Finnish |
  |---|---|---|
  | `storage_usage_used_free` | `%1$s used · %2$s free` | `%1$s käytetty · %2$s vapaana` |
  | `storage_usage_total` | `%1$s total` | `%1$s yhteensä` |

- [ ] Call the component with exactly two lines:

  ```kotlin
  supportingLines =
      listOf(
          stringResource(R.string.storage_usage_used_free, used, available),
          stringResource(R.string.storage_usage_total, total),
      ),
  ```

- [ ] Adapt Full Check score without changing its presentation:

  ```kotlin
  supportingLines = listOf(scoreStateLabel(report.score.state)),
  ```

### Step 4: Verify both call sites

- [ ] User reruns `HeadlineReadoutTest` and `ResourceParityTest`. Expected: pass.

- [ ] Static API migration scan:

  ```powershell
  rg -n "rawValues\s*=|storage_usage_context" app/src
  ```

  Expected: no matches.

- [ ] Inspect Storage at 360 dp in EN/FI and both themes: `total` is attached to its value on the second supporting line; no measured value is truncated.

- [ ] Inspect Full Check results: score state still occupies one supporting line and does not gain Storage-specific layout.

- [ ] Conditional commit message: `Vakauta tallennustilan lukeman asettelu`.

---

## Task 6: Name the SIM Modem Count Correctly

**Files:**

- Modify: `app/src/main/java/com/insaner/fonecheck/ui/screens/simtelephony/SimTelephonyScreen.kt`
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/res/values-fi/strings.xml`
- Preserve: `app/src/main/java/com/insaner/fonecheck/ui/screens/simtelephony/SimTelephonyProvider.kt`
- Preserve: `app/src/main/java/com/insaner/fonecheck/ui/screens/simtelephony/SimTelephonyProbe.kt`
- Preserve: `app/src/test/java/com/insaner/fonecheck/ui/screens/simtelephony/SimTelephonyPresentationTest.kt`

**Locked copy:** `Active modems` / `Aktiiviset modeemit`.

### Step 1: Establish the copy contract

- [ ] Extend the exact-vocabulary test in `ResourceParityTest` to expect `label_active_modem_count` as `Active modems` and `Aktiiviset modeemit`.

- [ ] User runs `ResourceParityTest`. Expected red result: the new resource key is missing.

- [ ] Add `label_active_modem_count` to both resource files. Do not create a presentation helper solely to unit-test a resource ID.

### Step 2: Change presentation only

- [ ] Replace `R.string.label_phone_count` with `R.string.label_active_modem_count` in `SimTelephonyScreen`.

- [ ] Delete `label_phone_count` from both locale files after its last use is gone.

- [ ] Do not rename domain/provider property `phoneCount` in this task. The actual value already comes from `TelephonyManager.activeModemCount` on API 30+ and the legacy `phoneCount` fallback on older Android versions. A model-wide rename would add risk without changing the UI truth.

- [ ] Do not alter SIM inventory classification. `Single SIM` and `2 active modems` may coexist because inventory describes active subscriptions while the count describes modem capacity.

### Step 3: Verify wording and unchanged data behavior

- [ ] User runs:

  ```powershell
  .\gradlew.bat :app:testDebugUnitTest --tests "com.insaner.fonecheck.localization.ResourceParityTest"
  ```

  ```powershell
  .\gradlew.bat :app:testDebugUnitTest --tests "com.insaner.fonecheck.ui.screens.simtelephony.SimTelephonyProbeTest"
  ```

- [ ] Static old-label scan:

  ```powershell
  rg -n "label_phone_count|Phone count|Puhelinten määrä" app/src
  ```

  Expected: no matches.

- [ ] Inspect the same SIM screenshots in both locales if available; count value remains unchanged.

- [ ] Conditional commit message: `Nimeä aktiivisten modeemien määrä oikein`.

---

## Task 7: Hide Reset Until the Volume-Button Test Has State to Clear

**Files:**

- Modify: `app/src/main/java/com/insaner/fonecheck/ui/screens/buttons/ButtonTestScreen.kt`
- Modify: `app/src/test/java/com/insaner/fonecheck/ui/screens/buttons/ButtonPresentationPolicyTest.kt`

**Locked policy:**

| Phase | Primary workflow action | Reset visible |
|---|---|---|
| `IDLE` | Start | No |
| `RUNNING` | Stop + Skip | No |
| `COMPLETED` | Start | Yes |
| `TIMED_OUT` | Try again | Yes |
| `SKIPPED` | Start | Yes |

### Step 1: Write the failing reset-visibility policy test

- [ ] Add an internal pure function contract named `buttonResetAvailable(phase)` and test all five phases:

  ```kotlin
  @Test
  fun `reset is offered only after a run has produced state`() {
      assertFalse(buttonResetAvailable(ButtonTestPhase.IDLE))
      assertFalse(buttonResetAvailable(ButtonTestPhase.RUNNING))
      assertTrue(buttonResetAvailable(ButtonTestPhase.COMPLETED))
      assertTrue(buttonResetAvailable(ButtonTestPhase.TIMED_OUT))
      assertTrue(buttonResetAvailable(ButtonTestPhase.SKIPPED))
  }
  ```

- [ ] User runs the red test. Expected: function missing.

  ```powershell
  .\gradlew.bat :app:testDebugUnitTest --tests "com.insaner.fonecheck.ui.screens.buttons.ButtonPresentationPolicyTest"
  ```

### Step 2: Implement the smallest policy seam

- [ ] Add exactly this mapping beside `buttonStatusTone`:

  ```kotlin
  internal fun buttonResetAvailable(phase: ButtonTestPhase): Boolean =
      phase == ButtonTestPhase.COMPLETED ||
          phase == ButtonTestPhase.TIMED_OUT ||
          phase == ButtonTestPhase.SKIPPED
  ```

- [ ] Render Reset only under `if (buttonResetAvailable(phase))`.

- [ ] Do not alter `ButtonTestViewModel.reset()`, phase transitions, hardware key handling or result classification.

### Step 3: Verify the interaction states

- [ ] User reruns `ButtonPresentationPolicyTest`. Expected: pass.

- [ ] Manually verify on device:

  1. Initial screen: Start only.
  2. Running: Stop and Skip only.
  3. After success: Start and Reset.
  4. After timeout: Try again and Reset.
  5. After skip: Start and Reset.

- [ ] Confirm all visible actions still fit or wrap using the existing `FlowRow`/weight behavior at 200% font scale.

- [ ] Conditional commit message: `Piilota tarpeeton nollaustoiminto`.

---

## Task 8: Remove the Three-Button Navigation Scrim Seam

**Files:**

- Modify: `app/src/main/java/com/insaner/fonecheck/ui/MainActivity.kt`
- Create: `app/src/androidTest/java/com/insaner/fonecheck/ui/MainActivitySystemBarsTest.kt`

**Platform contract:** Android 15+ edge-to-edge makes gesture navigation transparent but adds a translucent scrim to three-button navigation by default. Android's official Compose guidance says the scrim can be removed with `window.isNavigationBarContrastEnforced = false`; the property affects three-button navigation only and requires API 29+.

Reference: <https://developer.android.com/develop/ui/compose/system/setup-e2e>

### Step 1: Write a failing Activity-level instrumentation assertion

- [ ] Launch `MainActivity` with `createAndroidComposeRule<MainActivity>()`.

- [ ] On API 29+, assert:

  ```kotlin
  assertFalse(composeRule.activity.window.isNavigationBarContrastEnforced)
  ```

- [ ] Guard the test with `assumeTrue(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)` so older-device runs are skipped, not failed.

- [ ] User runs the red test on the current API 35+ device. Expected: current window keeps contrast enforcement enabled.

  ```powershell
  .\gradlew.bat :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.insaner.fonecheck.ui.MainActivitySystemBarsTest
  ```

### Step 2: Disable only the platform scrim

- [ ] Immediately after the existing `enableEdgeToEdge()` call, add:

  ```kotlin
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      window.isNavigationBarContrastEnforced = false
  }
  ```

- [ ] Do not set deprecated `navigationBarColor` or add a custom bottom bar. The existing `Scaffold` and screen roots already paint the theme background behind system insets.

- [ ] Preserve `ConfigureSystemBars` icon appearance and immersive Display-test hide/show behavior unchanged.

### Step 3: Prove OS-mode behavior on device

- [ ] User reruns `MainActivitySystemBarsTest`. Expected: pass on API 29+.

- [ ] Test all four combinations on the screenshot device:

  | Theme | Navigation mode | Expected |
  |---|---|---|
  | Light | Gesture | transparent bar, dark handle/icons, no new background |
  | Dark | Gesture | transparent bar, light handle/icons, no new background |
  | Light | Three-button | app background continues to screen edge, no gray seam |
  | Dark | Three-button | app background continues to screen edge, no lighter dark seam |

- [ ] Enter and exit the fullscreen Display test in both navigation modes. Bars must hide, return and use the correct icon appearance.

- [ ] Check a scrollable bottom screen so content remains reachable and no last control is occluded.

- [ ] Conditional commit message: `Yhtenäistä järjestelmäpalkkien tausta`.

---

## Task 9: Cross-Screen Regression and Screenshot Acceptance

**Files:** No planned product-code edits. Any discovered regression returns to the owning task instead of being patched here with a one-off workaround.

### Step 1: Run source-level acceptance checks

- [ ] Verify no obsolete vocabulary or color names remain:

  ```powershell
  rg -n "AccentFill|accentFill|OnAccentDark|home_run_all|Run All Tests|Aja kaikki testit|history_partial_check|history_incomplete_check|storage_usage_context|label_phone_count" app/src
  ```

  Expected: no user-facing or token matches. Internal Kotlin type `RunAllTests` is expected only if the expression is broadened to include symbol names.

- [ ] Verify no non-semantic screen reaches ATTENTION fill directly:

  ```powershell
  rg -n "attentionFill|AttentionFillDark|AttentionFillLight" app/src/main app/src/debug
  ```

  Expected: theme definition/construction and `SemanticTone.fillColor()` only.

- [ ] Verify resource parity with a read-only PowerShell check before Gradle:

  ```powershell
  $en = Select-String -Path 'app/src/main/res/values/strings.xml' -Pattern '<(?:string|plurals) name="([^"]+)"' | ForEach-Object { $_.Matches[0].Groups[1].Value }
  $fi = Select-String -Path 'app/src/main/res/values-fi/strings.xml' -Pattern '<(?:string|plurals) name="([^"]+)"' | ForEach-Object { $_.Matches[0].Groups[1].Value }
  Compare-Object $en $fi | Where-Object InputObject -ne 'app_name'
  ```

  Expected: no output. `app_name` is deliberately non-translatable and is the only raw-name difference if the filter is removed.

- [ ] Run whitespace validation:

  ```powershell
  git diff --check
  ```

### Step 2: User-run focused automated suite, sequentially

- [ ] Theme and semantic colors:

  ```powershell
  .\gradlew.bat :app:testDebugUnitTest --tests "com.insaner.fonecheck.ui.theme.SemanticColorTest" --tests "com.insaner.fonecheck.ui.theme.SemanticToneTest"
  ```

- [ ] Navigation, buttons and localization:

  ```powershell
  .\gradlew.bat :app:testDebugUnitTest --tests "com.insaner.fonecheck.navigation.NavigationChromeTest" --tests "com.insaner.fonecheck.ui.screens.buttons.ButtonPresentationPolicyTest" --tests "com.insaner.fonecheck.localization.ResourceParityTest"
  ```

- [ ] SIM data behavior:

  ```powershell
  .\gradlew.bat :app:testDebugUnitTest --tests "com.insaner.fonecheck.ui.screens.simtelephony.SimTelephonyProbeTest" --tests "com.insaner.fonecheck.ui.screens.simtelephony.SimTelephonyPresentationTest"
  ```

- [ ] Compose UI classes, one class per run to keep device/CPU load bounded:

  ```powershell
  .\gradlew.bat :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.insaner.fonecheck.ui.theme.FonecheckThemeSmokeTest
  ```

  ```powershell
  .\gradlew.bat :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.insaner.fonecheck.ui.screens.home.HomeContentTest
  ```

  ```powershell
  .\gradlew.bat :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.insaner.fonecheck.ui.screens.runall.FullCheckPreflightScreenTest
  ```

  ```powershell
  .\gradlew.bat :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.insaner.fonecheck.ui.screens.history.HistoryScreenTest
  ```

  ```powershell
  .\gradlew.bat :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.insaner.fonecheck.ui.components.HeadlineReadoutTest
  ```

  ```powershell
  .\gradlew.bat :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.insaner.fonecheck.ui.MainActivitySystemBarsTest
  ```

### Step 3: Recapture the complete visual matrix

- [ ] Use the same device, resolution, font scale, locale and navigation mode as the original sequence.

- [ ] Recapture the same 28 screens first in light, then the same 28 in dark, preserving identical states and scroll positions where possible.

- [ ] Pair each new light/dark capture and check:

  1. grid, padding and section rules align;
  2. no measured value is truncated;
  3. no label/value collision occurs;
  4. action labels are consistent;
  5. Home's three metric units are explicit;
  6. saved-versus-current source is explicit;
  7. status colors match across Home, reports and category screens;
  8. dark non-status chrome contains no gold;
  9. Full Check preflight hierarchy is readable;
  10. History type/state/time do not compete;
  11. Storage supporting values form two intentional lines;
  12. system navigation background has no seam.

- [ ] Run a second visual pass at 200% font scale for Home, Full Check preflight, History, Storage, SIM and Volume buttons. These six screens contain every changed copy/layout pattern.

- [ ] Run an accessibility pass with TalkBack for Home report card, preflight checkbox rows, History report actions and the Volume buttons state transition.

### Step 4: Final scope and dirty-tree audit

- [ ] Review only planned files:

  ```powershell
  git diff --stat -- app/src/main app/src/test app/src/androidTest app/src/debug
  ```

- [ ] Review every changed resource key and every direct theme-token consumer.

- [ ] Confirm no build files, dependencies, schemas, reports, generated baselines or packaged logo assets changed.

- [ ] Confirm no background process started for validation remains running.

- [ ] If commits were unsafe because of overlapping pre-existing edits, leave the working tree uncommitted and report that explicitly. Do not stage broad paths.

## Definition of Done

- [ ] Dark themen ainoat kromista erottuvat värit ovat todelliset PASS-, ATTENTION- ja FAIL-statukset sekä Display-testin tarkoitukselliset ärsykevärit.
- [ ] Home-logo, painikkeet, checkboxit, switchit ja neutraali progress ovat dark themessä mustavalkoisia/harmaita.
- [ ] Full Check -terminologia on yhtenäinen kaikessa käyttäjätekstissä.
- [ ] Kategoriamäärä, evidenssin coverage ja evidenssihavaintojen määrä eivät esiinny saman nimikkeen alla.
- [ ] Home kertoo kategoriarivien olevan viimeisimmästä Full Checkistä.
- [ ] Preflight, History ja Storage kestävät EN/FI:n sekä 200% font scale -tarkistuksen ilman epäselvää rivittymistä.
- [ ] SIM-modemimäärä ja Volume Reset kuvaavat todellista toimintaa.
- [ ] Kolmen painikkeen navigaatiopalkin sauma on poistunut eikä gesture/fullscreen-käytös ole regressioitunut.
- [ ] EN/FI-resurssit ovat paritettuja, kohdennetut testit läpäisevät käyttäjän ajamina ja `git diff --check` on puhdas.
- [ ] Aiempi dirty-worktree-työ on säilynyt eikä mitään suunnitelman ulkopuolista ole commitattu.

## Self-Review Checklist

- [ ] Every one of the 11 verified findings maps to one implementation task and one acceptance check.
- [ ] No placeholder markers remain:

  ```powershell
  $markers = @('TO' + 'DO', 'T' + 'BD', 'PLACE' + 'HOLDER', 'fill ' + 'this', 'decide ' + 'later')
  Select-String -Path 'docs/superpowers/plans/2026-08-20-fonecheck-interface-consistency.md' -Pattern $markers
  ```

  Expected: no output.

- [ ] Every named resource key has an EN and FI value.
- [ ] Every renamed Kotlin property has all call sites listed.
- [ ] Every changed shared-component interface has all current consumers listed.
- [ ] Every Gradle command is marked as user-run and can be executed independently.
- [ ] No task requires a new dependency, schema change, PR, push or destructive Git operation.
