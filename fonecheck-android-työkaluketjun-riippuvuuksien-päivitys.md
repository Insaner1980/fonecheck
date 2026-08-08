# fonecheckin Android-työkaluketjun ja riippuvuuksien päivitys

## Yhteenveto

Päivitetään fonecheck samaan käytännössä toimivaksi todettuun AGP 9 / Kotlin 2.4 -linjaan kuin vertailuprojektit. `compileSdk` nostetaan 37:ään uusimpien AndroidX-kirjastojen vaatimuksesta, mutta `targetSdk` pidetään 36:ssa, joten Android 17:n target-käyttäytymismuutoksia ei oteta käyttöön.

Muutos koskee Gradle- ja CI-konfiguraatiota, dependency verificationia sekä nykytilan dokumentaatiota. Sovelluksen julkiset API:t, lähdekoodi, Room-skeema, manifesti, resurssit ja käyttöliittymä eivät muutu.

### Lukitut tavoiteversiot

| Osa | Nykyinen | Tavoite |
|---|---:|---:|
| Gradle wrapper | 8.11.1 | 9.7.0 |
| Android Gradle Plugin | 8.9.1 | 9.3.1 |
| Kotlin Compose/Serialization plugins | 2.1.0 | 2.4.10 |
| KSP | 2.1.0-1.0.29 | 2.3.11 |
| compileSdk | 36 | 37 |
| targetSdk | 36 | 36 |
| minSdk | 26 | 26 |
| JVM bytecode/source target | 17 | 17 |
| Compose Stability Analyzer | 0.2.20 | 0.12.0 |
| Compose Rules | 0.6.3 | 0.6.4 |
| OWASP Dependency Check | 12.2.2 | 13.0.0 |
| Core KTX | 1.18.0 | 1.19.0 |
| Lifecycle | 2.9.1 | 2.11.0 |
| Activity Compose | 1.12.3 | 1.13.0 |
| Compose BOM | 2026.03.00 | 2026.06.01 |
| Dagger Hilt | 2.57.1 | 2.60.1 |
| AndroidX Hilt Navigation | 1.2.0 | 1.4.0 |
| Navigation Compose | 2.9.7 | 2.9.8 |
| kotlinx.serialization JSON | 1.8.1 | 1.11.0 |
| CameraX | 1.5.1 | 1.6.1 |
| kotlinx-coroutines-test | 1.10.2 | 1.11.0 |

Pidetään ennallaan Detekt 2.0.0-alpha.5, ktlint Gradle 14.2.0, Room 2.8.4, Splashscreen 1.2.0, Biometric 1.1.0, DataStore 1.2.1, JUnit 4.13.2, AndroidX Test Runner 1.7.0, AndroidX Test JUnit 1.3.0 ja Android Security Lints 1.0.4. Detekt alpha.5 on dokumentoitu poikkeus stable-linjaan.

Versiot perustuvat [Gradle 9.7.0 -julkaisuun](https://gradle.org/releases/), [AGP 9.3 -yhteensopivuustietoihin](https://developer.android.com/build/releases/agp-9-3-0-release-notes), [AndroidX stable -kanavaan](https://developer.android.com/jetpack/androidx/versions/stable-channel), [Compose BOM -metadataan](https://dl.google.com/dl/android/maven2/androidx/compose/compose-bom/maven-metadata.xml) ja [KSP-pluginin metadataan](https://plugins.gradle.org/m2/com/google/devtools/ksp/com.google.devtools.ksp.gradle.plugin/maven-metadata.xml).

## Toteutus

### 1. Esitarkistus ja ympäristö

- Tallenna `git status --short` ja nykyinen diffi. Älä muuta tai palauta käyttäjän keskeneräisiä lähdekoodi-, review-, logo- tai Android-check-muutoksia.
- Aja ennen muutoksia vertailutaso yksi tehtäväryhmä kerrallaan:
  ```powershell
  .\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug --no-daemon --console=plain --max-workers=2
  .\gradlew.bat :app:ktlintCheck :app:detekt :app:stabilityCheck --no-daemon --console=plain --max-workers=2
  ```
- Kirjaa mahdollinen nykytilan virhe ennen päivitystä; sitä ei korjata versiopäivityksen varjolla.
- Asenna paikalliseen SDK:hon Android 17:n compile-platform ja työkalut:
  ```powershell
  sdkmanager.bat "platforms;android-37.0" "build-tools;37.0.0"
  ```
- Älä asenna emulaattoria tai debug-APK:ta tämän työn osana.

### 2. Gradle wrapper ja AGP 9 -migraatio

- Päivitä wrapper Gradle 9.7.0:aan ajamalla wrapper-tehtävä kahdesti, jotta sekä jakeluviite että wrapperin omat tiedostot syntyvät Gradle 9.7:llä:
  ```powershell
  .\gradlew.bat wrapper --gradle-version 9.7.0 --distribution-type bin --gradle-distribution-sha256-sum 84fbba45c7f4c64abc77460e1c00f541e9f960e3c7ed2538f1ede19eacd873ae --no-daemon --max-workers=2
  .\gradlew.bat wrapper --gradle-version 9.7.0 --distribution-type bin --gradle-distribution-sha256-sum 84fbba45c7f4c64abc77460e1c00f541e9f960e3c7ed2538f1ede19eacd873ae --no-daemon --max-workers=2
  ```
- Varmista, että `gradlew` säilyttää executable-bitin ja `gradle-wrapper.properties` sisältää edelleen URL-validoinnin, 10 sekunnin network timeoutin ja virallisen SHA-256:n.
- Päivitä version catalogista AGP, Kotlin, KSP ja Hilt.
- Ota käyttöön AGP 9:n sisäänrakennettu Kotlin:
  - poista `kotlin-android`-pluginin catalog-alias;
  - poista sen `apply false` -määritys root-buildistä;
  - poista plugin app-moduulista;
  - säilytä Kotlin Compose- ja Serialization-pluginien versio 2.4.10;
  - poista vanha `android.kotlinOptions { jvmTarget = "17" }`;
  - säilytä `compileOptions` Java 17:ssä, jolloin built-in Kotlin käyttää samaa JVM-targetia.
- Poista `ksp.useKSP2=true`, koska KSP 2.3.11 käyttää KSP2:ta ilman opt-in-asetusta.
- Säilytä nykyiset buildscriptin security-force-versiot tässä työssä muuttumattomina. Niiden valitut versiot tarkistetaan uudesta classpathista, mutta niitä ei yhdistetä sokkona runtime-kirjastopäivitykseen.
- Päivitä `compileSdk = 37`; pidä `targetSdk = 36` ja `minSdk = 26`.

### 3. Kirjasto- ja analyysityökalupäivitykset

- Päivitä version catalogissa kaikki tavoitetaulukon AndroidX-, Compose-, Hilt-, CameraX-, serialization- ja coroutine-versiot.
- Säilytä BOM-malli: yksittäisille Compose UI/Material/test-artifacteille ei lisätä omia versioita.
- Säilytä CameraX-moduulit yhdessä `camerax`-versiolähteessä.
- Säilytä Hilt runtime ja compiler samassa `hilt`-versiolähteessä.
- Päivitä Stability Analyzer 0.12.0:aan ja säilytä nykyinen `:app:stabilityCheck`-rajapinta sekä `app/stability/app.stability`.
- Päivitä Compose Rules 0.6.4:ään sekä Detekt- että ktlint-rulesetille. Detekt pysyy alpha.5:ssä.
- Päivitä OWASP Dependency Check 13.0.0:aan. Säilytä nykyinen raporttihakemisto, suppression-tiedosto, NVD-asetukset, CVSS-raja ja skannattavat debug/release-konfiguraatiot.
- Älä lisää uusia riippuvuuksia, plugin-abstraktioita tai sovelluskoodia.

### 4. Dependency control, CI ja dokumentaatio

- Generoi `buildscript-gradle.lockfile` uudesta plugin-classpathista Gradlen omalla `--write-locks`-polulla. Älä muokkaa tiedostoa käsin.
- Täydennä `gradle/verification-metadata.xml` vain ratkaistuilla uusilla artefakteilla käyttäen `--write-verification-metadata sha256`.
- Säilytä `verify-metadata=true`, `verify-signatures=true`, nykyinen keyring, suljetut keyserverit ja nykyiset luottamusrajat. Dependency verificationia ei saa poistaa käytöstä edes väliaikaisesti.
- Tarkista metadata- ja lock-diffistä, että muutokset vastaavat AGP/Kotlin/KSP/Hilt/AndroidX/analyysityökalujen uutta graafia. Vanhoja verification-merkintöjä ei tarvitse poistaa vain siisteyden vuoksi.
- Päivitä CI:n molemmat Androidia rakentavat jobit asentamaan:
  ```yaml
  packages: platform-tools platforms;android-37.0 build-tools;37.0.0
  ```
- Päivitä CodeQL `init` ja `analyze` vakaaseen v4.37.5-pinniin, mutta älä ota käyttöön nightly-CodeQL-bundlea ilman todellista stable-bundlen virhettä.
- Päivitä nykytilan versionumerot ja SDK-jako tiedostoihin `AGENTS.md`, `CLAUDE.md` ja `PROJECT.md`. Säilytä fonecheckin yleinen “Codex ei aja Gradlea” -sääntö; tämän työn ajot perustuvat käyttäjän tähän migraatioon antamaan kertaluonteiseen poikkeukseen.
- Päivitä `fonecheck_code_review_questions_400.md`:n versionumeroihin sidotut Gradle/Kotlin/Hilt/KSP/Compose/Navigation/Serialization/CameraX-kysymykset kohdeversioihin. Tiedosto on jo käyttäjän muokkaama, joten koske vain näihin tarkasti rajattuihin riveihin.
- Älä päivitä historiallisen `fonecheck-implementation-plan.md`:n vanhoja versionumeroita. Älä muuta käyttäjän keskeneräistä `CODE_REVIEW.md`:tä tai `config/android-check.json`:ia.

## Tarkistusjärjestys ja hyväksyntä

Kaikki Gradle-ajot tehdään peräkkäin, `--no-daemon --console=plain --max-workers=2` -asetuksilla. `clean`-tehtävää ei ajeta.

1. **Wrapper ja konfiguraatio**
   ```powershell
   .\gradlew.bat --version
   .\gradlew.bat help --warning-mode all --write-locks --write-verification-metadata sha256 --no-daemon --console=plain --max-workers=2
   .\gradlew.bat help --warning-mode all --no-daemon --console=plain --max-workers=2
   ```
   Hyväksyntä: Gradle 9.7.0 käynnistyy, AGP 9.3.1 konfiguroituu, eikä strict dependency verification estä normaalia toistoajoa.

2. **Kotlin, Hilt, Room ja KSP**
   ```powershell
   .\gradlew.bat :app:kspDebugKotlin --write-verification-metadata sha256 --no-daemon --console=plain --max-workers=2
   .\gradlew.bat :app:kspDebugKotlin :app:compileDebugKotlin :app:compileReleaseKotlin --no-daemon --console=plain --max-workers=2
   ```
   Hyväksyntä: built-in Kotlin toimii ilman `kotlin-android`-pluginia, KSP generoi Room- ja Hilt-koodin, eikä lähteissä ilmene compiler/API-virheitä.

3. **Riippuvuusgraafit**
   ```powershell
   .\gradlew.bat :app:dependencies --configuration debugRuntimeClasspath --write-verification-metadata sha256 --no-daemon --console=plain --max-workers=2
   .\gradlew.bat :app:dependencies --configuration releaseRuntimeClasspath --write-verification-metadata sha256 --no-daemon --console=plain --max-workers=2
   .\gradlew.bat :app:dependencies --configuration debugRuntimeClasspath --no-daemon --console=plain --max-workers=2
   .\gradlew.bat :app:dependencies --configuration releaseRuntimeClasspath --no-daemon --console=plain --max-workers=2
   ```
   Hyväksyntä: tavoiteversiot ovat valittuja versioita, vanhat suorat versiot eivät voita niitä ja debug/release-graafeissa ei ole ratkaisemattomia konflikteja.

4. **Build ja testit**
   ```powershell
   .\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:compileDebugAndroidTestKotlin --no-daemon --console=plain --max-workers=2
   .\gradlew.bat :app:lintDebug :app:lintRelease --no-daemon --console=plain --max-workers=2
   ```
   Hyväksyntä: debug APK, koko unit-testisarja, Android-testilähteiden käännös sekä molemmat lint-variantit onnistuvat.

5. **Staattiset tarkistukset**
   ```powershell
   .\gradlew.bat :app:ktlintCheck --no-daemon --console=plain --max-workers=2
   .\gradlew.bat :app:detekt --no-daemon --console=plain --max-workers=2
   .\gradlew.bat :app:compileDebugKotlin :app:stabilityCheck --no-daemon --console=plain --max-workers=2
   .\gradlew.bat :app:dependencyCheckAnalyze --no-daemon --console=plain --max-workers=2
   ```
   Hyväksyntä: ktlint, Detekt alpha.5 + Compose Rules 0.6.4, Stability Analyzer 0.12.0 ja Dependency Check 13.0.0 ovat vihreitä.

6. **Stability-baseline**
   - Jos `stabilityCheck` menee läpi, baselinea ei muuteta.
   - Jos formaatti tai Kotlin/Compose-luokittelu muuttuu, aja `:app:stabilityDump` vasta löydöskohtaisen tarkastuksen jälkeen.
   - Hyväksy generoitu baseline vain, jos erot johtuvat työkaluversion uudesta esitystavasta tai perustellusta compiler-luokittelusta. Lähdekoodin todellista stable → unstable -regressiota ei hyväksytä baselineen.

7. **Lopputarkistus**
   - Aja `actionlint` workflowlle.
   - Aja `git diff --check`.
   - Tarkista `git status --short` ja rajattu diffi.
   - Varmista, ettei käyttäjän aiempiin lähdekoodi-, review-, logo- tai Android-check-muutoksiin tullut sivuvaikutuksia.
   - Varmista, ettei nykyisissä build- tai nykytiladokumenteissa ole vanhoja tavoiteversioita; historialliset lokit saavat säilyttää aikansa versionumerot.

Työ on valmis vasta, kun kaikki normaalit toistoajot onnistuvat ilman `--write-locks`- tai `--write-verification-metadata`-lippuja. Työ ei sisällä commitia, pushia, pull requestia, targetSdk 37 -migraatiota, API 37 -runtime-väitettä eikä fyysisten kamera-, biometria- tai sensoripolkujen uudelleentodentamista.
