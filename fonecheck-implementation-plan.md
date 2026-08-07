# FONECHECK_IMPLEMENTATION_PLAN

## 1. Current-state summary

Plan Mode estää tässä vaiheessa repositoryyn kirjoittamisen, joten `FONECHECK_IMPLEMENTATION_PLAN.md`-tiedostoa ei ole vielä luotu. Tämä suunnitelma muodostaa sen hyväksyttävän sisällön. Mitään lähdekoodia, testiä, resurssia, Gradle-tiedostoa, manifestia, skeemaa tai dokumenttia ei muutettu.

### Already implemented

- Android-sovelluksen perusta on Compose-, Material 3-, Hilt-, Room- ja type-safe Navigation -pohjainen.
- Graphite/Aqua-teema, DM Sans- ja JetBrains Mono -fontit sekä keskeiset jaetut UI-komponentit ovat olemassa.
- Home käyttää yhtä keskitettyä 12 diagnostiikkakohteen listaa.
- Device-, Performance-, SIM-, Display-, Audio-, Camera-, Sensors-, Connectivity-, Battery-, Vibration-, Buttons- ja Biometrics-näkymät ovat toimivia mutta eri tavoin keskeneräisiä.
- Full Check suorittaa nykyiset 12 kategoriaa ja muodostaa yhden muistissa olevan `TestSession`-raportin.
- Englannin- ja suomenkieliset merkkijonoresurssit ovat olemassa; avainpariteetti on lähes täydellinen.
- Manifestissa on nykyisten ominaisuuksien käyttöoikeudet ja laitteisto-ominaisuudet. Sovellus ei tällä hetkellä pyydä Internet-oikeutta, varmuuskopiointi on estetty ja raakaa ääni- tai kuvamateriaalia ei tallenneta.

### Partially implemented

- Full Check sisältää automaattisia ja ohjattuja vaiheita, mutta ei preflightia, dynaamista soveltuvuuspohjaista vaihelistaa, kunnollista peruutusta, Thermal- tai Storage-vaiheita eikä pysyvää raporttia.
- Pisteytys jättää `NotAvailable`- ja `NotTested`-tilat pois samasta nimittäjästä eikä laske erillistä coverage-arvoa.
- `RunAllReportBuilder` muodostaa lokalisoituja näyttötekstejä sisältävän raportin Android `Context`-riippuvuudella. Malli ei sovellu pitkäikäiseen tallennukseen tai myöhempään uudelleenlokalisointiin.
- Standalone-lupavirrat ovat epäyhtenäisiä: Connectivity pyytää osan oikeuksista, mutta Camera-, Audio- ja SIM-näkymistä puuttuvat täydet lupupyynnöt ja pysyvästi evätyn luvan palautuspolut.
- Diagnostiikkakategoriat keräävät paljon hyödyllistä tietoa, mutta sisältävät elinkaaririskejä, englanninkielisiin näyttöarvoihin perustuvaa päättelyä, epäselviä mittausväitteitä ja puutteellisia unavailable/error-tiloja.
- Nykyinen visuaalinen järjestelmä on käyttökelpoinen lähtökohta, mutta navigointi, fullscreen-testit, responsiivisuus, semantiikka ja kaikki virhe-/lataus-/tyhjätilat eivät ole valmiita.

### Placeholder or scaffolding

- Room-tietokanta käyttää `PlaceholderEntity`-entiteettiä, sillä ei ole DAO:ta tai repositorya, ja `exportSchema=false`.
- Tarkistettu version 1 skeematiedosto kuvaa tyhjää tietokantaa eikä vastaa annotoitua placeholder-entiteettiä.
- Report-, History- ja Settings-reitit näyttävät vain placeholder-näkymän.
- Thermal ja Storage ovat enum-arvoja mutta niillä ei ole reittiä, näkymää, ViewModelia tai raportti-integraatiota.
- `REPORT` on virheellisesti mukana `TestCategory`-enumissa, vaikka raportti ei ole laitteistodiagnostiikka.

### Absent

- Tuotantokelpoinen raporttien persistence, muuttumattomat raportit, historia, vertailu, poisto, PDF/koneluettava vienti ja turvallinen jakaminen.
- Functional Settings, ensimmäisen käynnistyksen onboarding ja asetusten pysyvä tallennus.
- Score-, coverage-, report schema- ja evidence-versiointi.
- Yhtenäinen lupa- ja unavailable/not-tested-malli.
- Unit-, integration-, Room-, Compose UI- ja instrumentaatiotestit.
- Room-migraatiopolitiikka, FileProvider, tietosuojakäyttöliittymä, lisenssinäkymä, signing-valmistelu ja todistettu release/R8-läpimeno.

### Sequencing risks and technical debt

- Tallennusskeemaa ei saa rakentaa nykyisten lokalisoitujen `TestResult`-tekstien päälle.
- Score- ja coverage-semanttien muuttaminen persistence-työn jälkeen pakottaisi pitkäikäisten raporttien formaatin uudelleensuunnitteluun.
- Full Check omistaa nyt useita ViewModeleja ja käynnistää laiteresursseja Compose-efekteistä; timeout-, myöhäinen callback- ja kaksoissiirtymäriskejä on useita.
- GPS-kuuntelijan poistossa käytetään eri lambda-instanssia kuin rekisteröinnissä. Audio-, Camera-, Sensors-, Vibration- ja Buttons-resurssien omistus tai pysäytys ei ole kaikissa poluissa idempotentti.
- Nykyisessä worktreessä on käyttäjän keskeneräisiä muutoksia (`PROJECT.md`, `gradle/verification-metadata.xml` ja uusi product spec). Ne on säilytettävä.
- `CODE_REVIEW.md`, `TASKS.md` ja `CLAUDE.md` sisältävät osittain vanhentuneita väitteitä. Niiden yksittäiset löydökset on varmennettava koodista ennen muutoksia; vanhat Pro-, monetization- ja sinisen teeman suunnitelmat eivät ohita nykyistä product speciä.
- Codex ei aja Gradle-tehtäviä CPU-rajoituksen vuoksi. Toteutusvaiheissa Codex tekee staattiset tarkistukset ja antaa täsmällisen kevyimmän Gradle-komennon käyttäjän ajettavaksi; hyväksyntä kirjataan vasta saadun tuloksen perusteella.

## 2. Target-state summary

### Final user-visible product

fonecheck on paikallinen, viimeistelty Android-diagnostiikkasovellus, jossa käyttäjä voi:

- suorittaa yksittäisen diagnoosin tai 14 kategorian ohjatun Full Checkin;
- nähdä selvästi automaattisen mittauksen, arvioidun tiedon, käyttäjän vahvistuksen, unavailable-, not-tested-, warning- ja failed-tilan;
- tallentaa valmiin raportin muuttumattomana;
- avata raporttihistorian, poistaa raportteja ja vertailla kahta yhteensopivaa raporttia;
- viedä raportin PDF-muodossa ja mahdollisen hyväksytyn koneluettavan formaatin kautta;
- hallita teemaa, lupia, historiaa, tietosuojaa, onboardingia ja sovellustietoja Settingsissä;
- käyttää koko tuotetta englanniksi ja suomeksi, suurilla fonteilla, TalkBackilla, vaaka-asennossa sekä vaaleassa ja tummassa teemassa.

### Final diagnostic category set

Yksi kanoninen järjestys toimii Homessa, standalone-navigaatiossa, Full Checkissä, raportissa, persistenssissä, vertailussa ja viennissä:

1. Device
2. Performance
3. SIM
4. Display
5. Audio
6. Camera
7. Sensors
8. Connectivity
9. Battery
10. Thermal
11. Storage
12. Vibration
13. Buttons
14. Biometrics

Report, History, Comparison, Settings ja Onboarding ovat tuotenäkymiä, eivät diagnostiikkakategorioita.

### Final report behavior

- Vain eksplisiittisesti valmistunut Full Check tai hyväksytty category-only retest tallentuu.
- Tallennettu raportti on locale-neutral, versioitu ja muuttumaton.
- Raportissa ovat laite- ja sovelluskonteksti, ajankohta, kesto, score-versio, report schema -versio, score-tila, coverage, yhteenvetolaskurit, kaikki kategoriat ja yksittäinen evidence.
- Historiallinen evidence näytetään sellaisena kuin se tallennettiin. Retest luo uuden evidencen eikä koskaan muuta aiempaa raporttia.
- History näyttää uusimmat ensin sekä tukee avaamista, vertailua, vientiä ja vahvistettua poistoa.
- Comparison näyttää vain perusteltavat muutokset eikä tulkitse Androidin ilmoittaman arvon muutosta fyysiseksi kulumiseksi.
- PDF luodaan paikallisesti ja jaetaan rajatun, ei-exported FileProvider-polun kautta.
- Arkaluonteisia sijainti-, verkko-, solu-, ääni- tai kuvaarvoja ei tallenneta eikä viedä oletuksena.

### Final Settings and onboarding behavior

- Settings tarjoaa System/Light/Dark-teeman, testivaroitukset, lupien tilat ja palautuspolut, raporttimäärän, historian poiston, tietosuojan, version, lisenssit, palautekanavan, disclaimerin ja onboardingin uudelleenavauksen.
- Onboarding on ohitettava, tallentaa valmistumisensa, ei pyydä vaarallisia lupia passiivisesti ja selittää Full Checkin, manuaaliset vahvistukset, tietosuojan, raportit ja mittausten rajoitteet.

### Explicit non-goals and prohibited claims

- Ei käyttäjätiliä, pilvisynkronointia, analytiikkaa, mainoksia, tilausta, etähallintaa tai backend-palvelua ilman myöhempää erillistä hyväksyntää.
- Ei raakaa mikrofonitallennetta, kamerakuvaa, tarkkaa GPS-sijaintia, cell ID:tä tai SSID/BSSID-tunnistetta historiaan.
- Ei väitteitä täydellisestä root-/turvallisuustodistuksesta, kalibroidusta äänenpainetasosta, akun tarkasta jäljellä olevasta kapasiteetista, fyysisestä tallennusmediaterveydestä, biometrisen sensorin laadusta tai Androidin piilottamien kameramoduulien testaamisesta.
- Unsupported, permission denied, skipped, estimated, warning ja failed eivät saa sulautua samaan tilaan.
- Network speed ei kuulu oletustavoitteeseen, ellei päätösportissa hyväksytä Internet-oikeutta, palvelua ja tietosuojavaikutusta.

## 3. Dependency and sequencing rationale

1. Tuotepäätökset lukitaan ensin, koska score, coverage, persistence, export ja Full Check eivät voi saada vakaata sopimusta ilman niitä.
2. Locale-neutral domain-, evidence-, category catalog-, score- ja coverage-mallit toteutetaan ennen tietokantaa. Tämä estää lokalisoitujen tekstien ja muuttuvien laskentasääntöjen lukitsemisen pitkäikäisiin raportteihin.
3. Testisaumat, lupa-state ja Room/repository muodostavat perustan ennen kategoriakorjauksia. Diagnostiikat voivat sen jälkeen tuottaa yhtä yhteistä evidence-mallia.
4. Yksittäisten kategorioiden behavior, resource ownership ja standalone-lupavirrat vakautetaan ennen Full Checkin uudelleenrakentamista.
5. Full Check viimeistelee orchestrationin ja tallentaa raportin yhdellä atomisella finalisoinnilla.
6. Report-, History-, Comparison- ja Export-UI rakennetaan vasta vakaan immutable report -sopimuksen päälle.
7. Settings, visuaalinen viimeistely, lokalisaatio ja accessibility valmistellaan behaviorin rinnalla, mutta niiden järjestelmällinen loppuauditointi tehdään vasta päävirtojen vakiinnuttua.
8. Onboarding tehdään päävirtojen jälkeen, jotta sen teksti ja navigointi eivät vanhene toteutuksen aikana.
9. Automatisoitu, instrumentaatio- ja fyysinen laitetestaus sekä release/R8/Play-työ päättävät kokonaisuuden.

### Planned internal interfaces

Sovellukselle ei lisätä ulkoista julkista API:a. Keskeiset sisäiset sopimukset ovat:

- `DiagnosticCategoryId` ja yksi `DiagnosticCatalog` 14 kategorian vakailla tunnisteilla.
- `DiagnosticEvidence`, jossa ovat vakaa check ID, status, confidence, evidence source, applicability, reason code, typed value, unit ja capture timestamp.
- `DiagnosticReport`, `ReportKind`, `ScoreSummary`, `CoverageSummary`, `ReportSchemaVersion` ja `ScoreVersion`.
- Puhdas `ScoreCalculator` ja `ReportAssembler`, jotka eivät riipu Android `Context`ista tai lokalisoiduista teksteistä.
- `ReportRepository`, joka sallii insert/read/observe/delete-operaatiot mutta ei raportin sisällön update-operaatiota.
- Yhteinen `PermissionState` ja lupupolitiikka; itse Activity Result -pyyntö pysyy UI-rajalla.
- Kapeat, kategoriakohtaiset probe-rajapinnat vain Android API -koodille, joka tarvitsee deterministisiä testifakeja.

## 4. Ordered master plan

Kaikkien kohtien alkutila on `Not started`. Kohdan valmistuessa tähän tiedostoon kirjataan tila, päätökset, hyväksyntäkriteerien tulokset, käyttäjän ajamien Gradle-tarkistusten tulokset ja vain välttämättömät uudet jatkotehtävät.

### 1. Lock product, data, and release decisions — Complete

- **Objective:** Poistaa kaikki aidot tuotevalinnat ennen pysyvien sopimusten toteuttamista.
- **Current behavior:** Spec nimeää useita avoimia päätöksiä; koodi käyttää implisiittisiä valintoja.
- **Required final behavior:** Kirjaa hyväksytty päätösloki tähän suunnitelmaan.
- **Dependencies:** Ei teknisiä riippuvuuksia.
- **Likely areas:** Implementation plan, product spec vain jos hyväksytty tavoite muuttuu.
- **Boundaries and invariants:** Päätös ei itsessään muuta tuotantokoodia. Oletusta ei esitetä hyväksyttynä päätöksenä.
- **Recommended decision set:**

| Decision | Recommended default |
|---|---|
| Database baseline | Korvaa julkaisematon placeholder aidolla production v1 -skeemalla; kehityslaitteet tyhjennetään |
| Performance | Säilytä nimi ja lisää lyhyet CPU- ja memory-throughput-mittaukset |
| Network speed | Jätä pois; säilytä no-INTERNET-pinta |
| Score validity | Alle 70 %: incomplete ja numero piiloon; 70–99 %: partial; 100 %: complete |
| Weighting | Yhtä suuret kategoriapainot; informational evidence ei nosta scorea |
| Retest | Salli selvästi merkitty category-only report |
| Machine export | JSON, ei CSV:tä ensimmäisessä julkisessa versiossa |
| Retention | Säilytä paikallisesti, kunnes käyttäjä poistaa |
| Sensitive evidence | Ei opt-in-tallennusta v1:ssä |
| Storage benchmark | 64 MiB sequential app-cache write/read, skip sallittu |
| Thermal workload | Observation only; ei keinotekoista kuormaa |
| Display gradient | Sisällytä grayscale/gradient-kenttä |
| NFC | Capability/enabled-state only |
| Language | Androidin system/per-app locale; ei omaa kielivalitsinta |
| Report labels/notes | Ei v1:ssä |
| Interrupted Full Check | Hylkää keskeneräinen ajo ja aloita uudelleen |
| Full Check audio | Speaker + microphone; stereo/earpiece standalone |
| Full Check camera | Jokainen julkisesti käyttäjän valittavissa oleva kamera; pelkät fyysiset alikamerat metadataa |
| Optional Settings toggles | Ei spekulatiivisia toggles; aktiivinen testi hallitsee näytön hereilläoloa tarvittaessa |
| Release inputs | Lopullinen nimi/package, privacy URL, yhteystieto ja signing-omistajuus vaativat omistajan vahvistuksen |

- **Acceptance criteria:** Jokainen rivi on hyväksytty tai korvattu eksplisiittisellä päätöksellä ja vastuuhenkilöllä.
- **Tests/verification:** Ristiriidattomuustarkistus specin, AGENTS-ohjeiden ja nykyisen manifestin kanssa.
- **Documentation:** Päätösloki tähän tiedostoon; product spec vain hyväksytyn scope-muutoksen tapauksessa.
- **Risks:** Hiljainen päätös voisi muuttaa tietosuojapintaa tai tehdä raporteista yhteensopimattomia.
- **Decision required:** Ei; päätösportti hyväksyttiin 2026-08-07.

#### Approved decision log

- **Owner:** Finnvek / project owner
- **Approval date:** 2026-08-07
- Database baseline: replace the unpublished placeholder with a real production v1 schema; development installations are cleared.
- Performance: keep the name and add short CPU and memory-throughput measurements.
- Network speed: excluded; preserve the no-INTERNET surface.
- Score validity: below 70% is incomplete and hides the numeric score; 70–99% is partial; 100% is complete.
- Weighting: equal category weights; informational evidence does not increase score.
- Retest: allow a clearly labeled category-only report.
- Machine export: JSON, not CSV, for the first public release.
- Retention: keep reports locally until the user deletes them.
- Sensitive evidence: no opt-in persistence in v1.
- Storage benchmark: 64 MiB sequential app-cache write/read; skipping is allowed.
- Thermal workload: observation only; no artificial workload.
- Display gradient: include a grayscale/gradient field.
- NFC: capability/enabled-state only.
- Language: Android system/per-app locale; no in-app language selector.
- Report labels/notes: excluded from v1.
- Interrupted Full Check: discard the incomplete run and restart.
- Full Check audio: speaker and microphone; stereo and earpiece remain standalone.
- Full Check camera: every publicly user-selectable camera; physical sub-cameras are metadata only.
- Optional Settings toggles: no speculative toggles; an active test may keep the display awake when needed.

#### Approved release inputs

- Final app name: `fonecheck`.
- Final package/application ID: `com.insaner.fonecheck` (matches current code).
- Privacy target: `https://finnvek.com/privacy/`; a fonecheck-specific policy must be published there before release. Live retrieval could not be verified in this session, so this plan does not claim the policy is already present.
- Support contact: `contact@finnvek.com`.
- Signing ownership: Finnvek / project owner controls the Google Play Console and keeps the upload key and credentials outside the repository. Use Play App Signing with a Google-managed app signing key unless the owner explicitly changes this before first release.

#### Decision-gate verification

Checked against `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, the `AGENTS.md` instructions supplied for this task, `app/build.gradle.kts`, and `app/src/main/AndroidManifest.xml`. The decisions preserve the local-first/no-INTERNET/privacy boundaries and the package ID matches the existing code. No Gradle command is applicable because this task changes documentation only.

### 2. Define the canonical diagnostic, evidence, score, and coverage contract

- **Objective:** Luoda locale-neutral ja versioitu perusta kaikille diagnooseille ja raporteille.
- **Current behavior:** `TestCategory` sisältää `REPORT`; tulokset tallentavat lokalisoituja nimiä ja tekstejä; score lasketaan ViewModelissa ilman coveragea.
- **Required final behavior:** 14 vakaata category ID:tä, vakaat check ID:t, typed evidence, reason codes, applicability, evidence source, `ScoreVersion`, `ReportSchemaVersion` sekä yksi testattu score/coverage-laskin.
- **Dependencies:** Kohta 1.
- **Likely areas:** `domain/model`, navigation diagnostic catalog, pure report/score package.
- **Boundaries and invariants:** `REPORT` poistuu diagnostiikkasemantiikasta. `NotAvailable` poistuu applicable-nimittäjästä; denied/skipped/error jää `NotTested`-evidencenä coverage-nimittäjään. Info ei kasvata scorea. Eri score-versioiden deltaa ei lasketa.
- **Acceptance criteria:** Home-, standalone-, Full Check- ja raporttijärjestys voidaan johtaa samasta catalogista; score ja coverage ovat deterministisiä; kaikki status- ja reason-arvot ovat locale-neutral.
- **Tests/verification:** Unit-testit kaikille statuksille, painotukselle, osittaiselle coveragelle, unsupported-hardwarelle, tyhjälle raportille ja score-version yhteensopivuudelle.
- **Documentation:** `PROJECT.md`, suunnitelman päätös- ja statusloki; relevantit `CODE_REVIEW.md`-kohdat.
- **Risks:** Liian geneerinen evidence-malli tai näyttötekstin vuotaminen domainiin.
- **Decision required:** Ei kohdan 1 jälkeen.

#### Implementation/status log — 2026-08-07

- Toteutettu: `DiagnosticCategoryId`/`DiagnosticCatalog`, locale-neutral evidence- ja report-sopimukset sekä puhdas `ScoreCalculator` version 1 semantiikalla.
- `diagnosticDestinations` johdetaan catalogista ja suodattaa toteutumattomat Thermal- ja Storage-kategoriat; nykyvirran `SYSTEM`-viittaukset vaihdettiin mekaanisesti `DEVICE`-arvoon.
- Lisätty JUnit 4.13.2 -testit score-, coverage-, validation- ja version-yhteensopivuussopimukselle.
- Staattisesti tarkistettu lähdekoodin viittaukset ja tehtävädiffi. Käyttäjän uusinta-ajo `./gradlew :app:testDebugUnitTest --tests "com.insaner.fonecheck.domain.model.ScoreCalculatorTest"` onnistui: `BUILD SUCCESSFUL in 19s`, `30 actionable tasks: 7 executed, 23 up-to-date`; Task 2:n acceptance on täytetty.
- `PROJECT.md`-päivitys on pending integration käyttäjän omien commitoimattomien muutosten kanssa; tiedostoa ei muokattu tässä tehtävässä.

#### Verification repair log — 2026-08-07

- Käyttäjän ajama rajattu JUnit-komento pysähtyi ennen testejä `:app:kspDebugUnitTestKotlin`-vaiheen dependency verificationiin, koska Task 2:n JUnit 4.13.2- ja transitiviset Hamcrest 1.3 -artefaktit puuttuivat metadatasta.
- Lisätty artifact-level ignored-key- ja SHA-256-pinnit viidelle todistetusti Maven Central -julkaisua vastaavalle artefaktille. Globaaleja avainluottamuksia, signature verification- tai keyserver-asetuksia ei muutettu.
- Käyttäjän uusinta-ajo onnistui korjauksen jälkeen: `BUILD SUCCESSFUL in 19s`, `30 actionable tasks: 7 executed, 23 up-to-date`. Task 2:n acceptance on täytetty.

### 3. Establish deterministic runtime seams and the test foundation

- **Objective:** Mahdollistaa laite-API-koodin, ajastusten ja lifecycle-polkujen todistettava testaaminen.
- **Current behavior:** ViewModelit kutsuvat Android API:a suoraan ja käyttävät reaaliaikaa/UUID:ta; testihakemistoja tai testiriippuvuuksia ei ole.
- **Required final behavior:** Injektoidut dispatcher-, clock- ja ID-providerit vain niitä tarvitseviin kohtiin, kapeat probe-faket sekä minimi unit/instrumentation/Compose/Room-testiasetus.
- **Dependencies:** Kohta 2.
- **Likely areas:** DI, test source sets, version catalog ja test utility -paketti.
- **Boundaries and invariants:** Ei yleistä use-case-kerrosta, service locatoria tai laajaa repository-abstraktiota. Faket suositaan mocking-frameworkin sijaan.
- **Acceptance criteria:** Puhdas domain-testi, coroutine/ViewModel-testi ja yksi instrumentaation smoke-testi ovat määritelty ja käyttäjän ympäristössä ajettavissa.
- **Tests/verification:** Staattinen dependency- ja source-set-tarkistus; käyttäjä ajaa sovitut kevyet testitehtävät.
- **Documentation:** `PROJECT.md` testirakenne ja sallitut komennot; suunnitelman status.
- **Risks:** Testi-infrastruktuurin paisuminen tai uusien riippuvuuksien turha lisääminen.
- **Decision required:** Ei.

#### Implementation/status log — 2026-08-07

- Lisätty funktionaaliset epoch-millis-kello- ja ID-provider-rajapinnat sekä yksi Hilt-moduuli, joka tarjoaa niiden tuotantototeutukset ja kvalifioidun IO-dispatcherin.
- `RunAllTestsViewModel` käyttää injektoitua kelloa ja ID-provideria; `AudioTestViewModel` ja `CameraTestViewModel` käyttävät vain nykyisten IO-töidensä injektoitua dispatcheria. Session idempotenssi ja legacy-scorelogiikka säilyvät ennallaan.
- Lisätty kohdistettu `RunAllTestsViewModelTest`, pieni `FonecheckTheme`-instrumentaatiosmoke sekä vain niiden ja seuraavan Room-skeematehtävän tarvitsemat testiriippuvuudet version catalogiin.
- Tehtävä on tarkistettu staattisesti ilman Gradlea projektisäännön mukaisesti. Rajattu unit-testi ja Android-testin Kotlin-käännös odottavat käyttäjän ajoa; instrumentaatiosmoke tarvitsee myöhemmin emulaattorin tai laitteen.
- Audio/Camera `NEXT TOUCH` -kohdat arvioitiin: mekaaninen dispatcher-korvaus ei muuta resurssien vapautusta, lataustiloja, preview-elinkaarta tai virhepolkuja, joten niiden erillisiä korjauksia ei laajennettu tähän tehtävään.
- `PROJECT.md`-integraatio on siirretty myöhemmäksi, koska käyttäjä omistaa tiedostossa suuren commitoimattoman uudelleenkirjoituksen; tiedostoa ei muokattu tässä tehtävässä.

#### Verification repair log — 2026-08-07

- Käyttäjän molemmat rajatut Task 3 -komennot pysähtyivät ennen testejä `:app:checkDebugAarMetadata`-vaiheessa, koska Compose BOMin valitseman `androidx.compose.ui:ui-test-manifest:1.10.5`-komponentin AAR- ja Gradle module metadata -checksumit puuttuivat tiukasta dependency verification -metadatasta.
- Välimuistin molemmat artefaktit vastaavat tavutasolla Google Mavenista erikseen ladattuja julkaisuja, ja SHA-256-arvot vastaavat Googlen julkaisemia `.sha256`-tiedostoja. Lisätty vain kaksi komponenttikohtaista SHA-256-pinniä; globaalia avainluottamusta tai verification-asetuksia ei muutettu.
- Käyttäjän uusinta-ajo tarvitaan, jotta riippuvuusresoluutio voi edetä seuraavaan vaiheeseen. Tässä korjauksessa ei ajettu Gradlea.
- Käyttäjän kohdennettu `RunAllTestsViewModelTest`-ajo onnistui ensimmäisen korjauksen jälkeen. Android-testin Kotlin-käännös eteni `:app:checkDebugAndroidTestAarMetadata`-vaiheeseen ja paljasti 28 aiemmin ratkaisemattoman AndroidX Test-, Compose UI Test-, Room Test-, Hamcrest- ja JavaWriter-artefaktin verification-puutteet.
- Kaikki 28 välimuistikopiota vastaavat tuoreita Google Maven- tai Maven Central -latauksia sekä julkaisijan SHA-256- tai SHA-1-checksumia. Lisätty täsmälliset SHA-256-pinnit; kolme Maven Central -allekirjoituspoikkeusta rajattiin vain raportissa nimettyihin POM-artefakteihin. Globaalia avainluottamusta tai verification-asetuksia ei muutettu.
- Käyttäjä ilmoitti commitin `e7c31cc` jälkeen ajon `./gradlew.bat :app:compileDebugAndroidTestKotlin` onnistuneen. Yhdessä aiemmin onnistuneeksi ilmoitetun kohdistetun `RunAllTestsViewModelTest`-ajon kanssa Task 3:n acceptance on täytetty. Codex ei ajanut Gradlea.

### 4. Replace the placeholder database with the production report schema

- **Objective:** Määrittää ensimmäisen julkisen version todellinen Room-skeema.
- **Current behavior:** Version 1 placeholder-entiteetti, ei DAO:ta, `exportSchema=false` ja ristiriitainen tyhjä skeematiedosto.
- **Required final behavior:** `ReportEntity`, jossa ovat listaukseen tarvittavat indeksoidut summary-kentät ja locale-neutral, versioitu immutable report payload; production DAO; foreign/format invariants; `exportSchema=true`.
- **Dependencies:** Kohdat 1–3.
- **Likely areas:** `data/local`, database DI, `app/schemas`.
- **Boundaries and invariants:** Ei `PlaceholderEntity`ä. Ei update-DAO:ta raportin sisältöön. Yksi raportti lisätään atomisesti. Valitulla production-v1-linjalla julkaisemattoman dev-kannan dataa ei teeskennellä tuotantodataksi.
- **Acceptance criteria:** Puhdas asennus luo oikean skeeman; payload ja summary-versiot täsmäävät; skeema on versionhallittava.
- **Tests/verification:** Instrumentoitu create/open/schema-testi. Migraatioharness lisätään; jos production v1 hyväksytään, dokumentoidaan ettei julkaisua edeltävälle placeholderille ole release-migraatiota. Jokainen myöhempi bump vaatii migration-testin. Tämä noudattaa Roomin [schema export- ja migration testing -ohjeita](https://developer.android.com/training/data-storage/room/migrating-db-versions).
- **Documentation:** `PROJECT.md`, schema-baseline-päätös ja dev-uninstall-ohje.
- **Risks:** Vanha kehitysasennus kaatuu ilman sovittua datan tyhjennystä; JSON-payloadin decoder-yhteensopivuus.
- **Decision required:** Kyllä, kohdan 1 database baseline.

#### Implementation/status log — 2026-08-07

- Julkaisematon placeholder-kanta on korvattu production version 1 `reports`-taululla. `ReportEntity` validoi suoran konstruktion stable kind/state -koodit, category-only-säännön, aikajärjestyksen, positiiviset versiot, score/coverage-rajat, ei-negatiiviset laskurit sekä applicable-laskurin yhtälön.
- `ReportDao` insertoi yhden immutable raporttirivin `ABORT`-konfliktikäytännöllä, palauttaa Historyä varten vain rajatun newest-first-summaryprojektion, lukee täyden rivin ID:llä ja sallii vain yhden tai kaikkien raporttien poiston. Sisällön update-operaatiota ei ole.
- `FonecheckDatabase` exportoi skeeman ja exposeeraa DAO:n. Stale tyhjä `1.json` poistettiin; käyttäjän onnistunut KSP2-ajo generoi production-v1-skeeman, joka on lisätty versionhallintaan sellaisenaan. Identity hashia tai muuta generated metadataa ei kirjoitettu käsin.
- Lisätty entity-invarianttien unit-testit, in-memory Room DAO -testi insert/read- ja newest-first-summarypoluille sekä Room 2.8.4 `MigrationTestHelper` -harness v1-exportille. Gradlea ei ajettu projektisäännön mukaisesti, joten testien compile/run ja schema-harness odottavat edelleen käyttäjän ajoa.
- Production-v1-päätöksen mukaisesti placeholderille ei ole migrationia tai destructive fallbackia. Vanha kehitysasennus on tyhjennettävä tai poistettava ennen uuden kannan avaamista.
- Kaikki kahdeksan `PRE-PHASE 4` -kohtaa arvioitiin: placeholder ja schema export korjattiin tässä; domain-/DeviceInfo-huomiot ovat nykykäytön ja Task 2:n perusteella stale/resolved; ViewModelien error/event-huomiot eivät blokkaa skeemaa; persistence/Phase 4 -roadmap jatkuu Taskeissa 5–7 eikä sitä merkitty valmiiksi. `PROJECT.md`:ää ei muokattu käyttäjän commitoimattoman uudelleenkirjoituksen vuoksi.

#### Verification repair log — 2026-08-07

- Käyttäjän ensimmäinen `:app:kspDebugKotlin`-ajo generoi Room-lähteet ja production-v1-skeeman mutta kaatui skeeman viennissä `AbstractMethodError`-virheeseen. Kotlinin virheloki osoitti, että Room 2.8.4:n `FieldBundle`-serializer ei saanut buildin lataamalta `GeneratedSerializer`-rajapinnalta tarvitsemaansa oletustoteutusta.
- KSP-processor-classpath käytti Room 2.8.4:n vaatimaa `kotlinx-serialization` 1.8.1:tä, mutta sovelluksen compile-classpath pakotti version 1.7.3. Bytecode-tarkistus vahvisti, että puuttuva oletustoteutus on lisätty versioon 1.8.1.
- Sovelluksen serialization-runtime nostettiin pienimmällä yhteensopivalla muutoksella versioon 1.8.1. Gradlea ei ajettu; käyttäjän uusinta-ajo tarvitaan korjauksen ja generoidun skeeman vahvistamiseksi.
- Uusinta-ajo osoitti saman poikkeuksen, vaikka sekä sovelluksen että Room-processorien ratkaistut classpathit olivat jo versiossa 1.8.1. Kotlin-daemonin todellinen komentorivi paljasti jäljelle jääneen lähteen: käytössä ollut KSP1 compiler plugin injektoi oman serialization 1.6.3:n samaan prosessiin.
- Projektissa jo olevan KSP 2.1.0-1.0.29 -pluginin eristetty KSP2-toteutus otettiin käyttöön `ksp.useKSP2=true`-propertyllä. Tämä on yhden asetuksen korjaus eikä muuta dependency-versioita; käyttäjän uusi `:app:kspDebugKotlin`-ajo tarvitaan edelleen GREEN-vahvistukseksi.
- Ensimmäinen KSP2-ajo pysähtyi ennen prosessointia tiukkaan dependency verificationiin, koska KSP2:n `symbol-processing-aa-embeddable:2.1.0-1.0.29`-JARin ja POMin checksumit puuttuivat. Molemmat välimuistikopiot vastaavat tavutasolla erillisiä Maven Central -latauksia ja julkaisijan SHA-256-tiedostoja; metadataan lisättiin vain nämä kaksi artefaktikohtaista pinniä.
- Käyttäjä ilmoitti commitin `d0b07b9` jälkeen ajon `:app:kspDebugKotlin` onnistuneen. Generoitu version 1 skeema sisältää production `reports` -taulun 17 saraketta, `id`-pääavaimen ja `completedAtEpochMillis`-indeksin eikä sisällä placeholder-taulua. Codex ei ajanut Gradlea.
- Käyttäjä ilmoitti `ReportEntityTest`-unit-testin ja Android-testien Kotlin-käännöksen onnistuneen. Ensimmäinen kohdistettu `connectedDebugAndroidTest` eteni instrumentaatioajoa edeltävään Hilt-vaiheeseen ja paljasti kolmen aiemmin resolvoimattoman Hamcrest/JavaWriter-JARin verification-puutteet. Välimuistikopiot vastaavat tavutasolla tuoreita Maven Central -latauksia ja julkaisijan SHA-1-tiedostoja; lisätyt SHA-256-pinnit ja avainpoikkeukset on rajattu vain näihin kolmeen JARiin.
- Käyttäjä ilmoitti commitin `b918f2b` jälkeen kohdistetun `ReportDaoTest`- ja `FonecheckDatabaseSchemaTest`-instrumentaatioajon onnistuneen. Yhdessä aiemmin onnistuneiden entity-unit-testin ja Android-testien Kotlin-käännöksen kanssa Task 4:n acceptance on täytetty. Codex ei ajanut Gradlea.

### 5. Implement immutable report persistence and mappings

- **Objective:** Tarjota yksi tuotantokelpoinen raporttien tallennusrajapinta.
- **Current behavior:** Raportti jää ViewModel-muistiin; repositorya ei ole.
- **Required final behavior:** `ReportRepository` tukee insert, observe summaries, get by ID, compare-load, delete ja delete-all; entity/domain-mapping validoi schema-version.
- **Dependencies:** Kohta 4.
- **Likely areas:** `data/repository`, Room DAO, DI.
- **Boundaries and invariants:** Tallennuksen jälkeen payloadia ei päivitetä. Poisto on sallittu käyttäjän toiminto. Tuntematon tuleva schema-versio näyttää hallitun unavailable/error-tilan eikä kaada Historyä.
- **Acceptance criteria:** Raportti säilyy prosessin ja sovelluksen uudelleenkäynnistyksen yli; järjestys on newest-first; delete on atominen.
- **Tests/verification:** DAO/repository-testit insert/read/order/delete/delete-all-, duplicate-ID-, corrupt-payload- ja unsupported-version-poluille.
- **Documentation:** `PROJECT.md`, suunnitelman status ja Room-havaintojen kirjaukset.
- **Risks:** Entity-summaryn ja payloadin erkaantuminen; osittainen tallennus.
- **Decision required:** Ei.

#### Implementation/status log — 2026-08-07

- Lisätty tests-first `ReportRepositoryTest`, joka määrittää insert/read-roundtripin kaikkine evidence-arvotyyppeineen, levy-backed kannan uudelleenavauksen, newest-first-summaryt ilman payload-dekoodausta, duplicate-ID-suojan, corrupt/unsupported-lukutilat, comparison-loadin sekä delete/delete-all-käytöksen.
- Käyttäjän kohdistettu Android-testin Kotlin-käännös tuotti odotetun REDin. Gradle-daemonin loki vahvisti virheiden johtuvan vain tarkoituksella puuttuneista repository-symboleista ja niiden metodeista.
- Lisätty `ReportRepository` ja Room-toteutus, joka kirjoittaa yhdestä domain-raportista sekä summary-kentät että versionoidun JSON-payloadin, lukee hallitut available/not-found/unavailable-tulokset, pitää summary-flow'n payloadista erillään ja tukee comparison-load-, delete- ja delete-all-operaatioita.
- Payload-koodaus säilyttää kaikki typed evidence -arvot, `Instant`-tarkkuuden ja `BigDecimal`-esityksen. Lukumapping vertaa payloadista rekonstruoitua raporttia entity-summaryyn; korruptio ja tuleva skeemaversio palautuvat hallittuina tuloksina.
- Database DI tarjoaa DAO:n ja singleton-repositoryn, joten `DI module completeness` -NEXT TOUCH on tämän persistence-rajapinnan osalta käsitelty. `PROJECT.md`-päivitys odottaa käyttäjän keskeneräisen tiedostomuutoksen valmistumista eikä sitä sekoitettu tähän tehtävään.
- Käyttäjä ilmoitti commitin `af0ff26` jälkeen Android-testien Kotlin-käännöksen sekä kohdistetun `ReportRepositoryTest`-instrumentaatioajon onnistuneen. Task 5:n acceptance on täytetty; Codex ei ajanut Gradlea.

### 6. Replace the Android-bound report builder with a pure report assembler

- **Objective:** Rakentaa sama canonical report standalone- ja Full Check -evidencestä.
- **Current behavior:** `RunAllReportBuilder` käyttää `Context`ia, resurssitekstejä ja ruutukohtaisia stateja.
- **Required final behavior:** Puhdas `ReportAssembler` ottaa versionoidut snapshotit ja tuottaa immutable `DiagnosticReport`-arvon; renderöinti lokalisoidaan vasta UI/PDF-rajalla.
- **Dependencies:** Kohdat 2 ja 5.
- **Likely areas:** report domain, category snapshot mappers, Run All.
- **Boundaries and invariants:** Ei resource ID:tä, `Context`ia, näyttötekstiä tai uutta score-laskentaa assemblerissa. Category order tulee catalogista.
- **Acceptance criteria:** Sama syöte tuottaa bittitasolla saman serialisoitavan raportin; raportin summary vastaa payloadia.
- **Tests/verification:** Golden-tyyppiset unit-testit täydelle, osittaiselle, unavailable- ja category-retest-raportille.
- **Documentation:** `PROJECT.md` report data flow; suunnitelman status.
- **Risks:** Nykyisten englanninkielisten näyttöarvojen muuttaminen stable codeiksi paljastaa kategorioiden puutteita.
- **Decision required:** Ei.

#### Implementation/status log — 2026-08-07

- Lisätty tests-first `ReportAssemblerTest` täydelle, 71 prosentin partial-, kokonaan unavailable- ja category-only-raportille. Testi lukitsee catalog-järjestyksen, nykyisen `ScoreCalculator`-semantiikan, snapshot-version ja deterministisen repository-JSON-serialisoinnin.
- Käyttäjän kohdistettu unit-testiajo tuotti odotetun REDin. Gradle-daemonin loki vahvisti virheiden johtuvan vain tarkoituksella puuttuneista snapshot/assembler-symboleista ja niiden seurannaisviittauksista.
- Lisätty Androidista riippumattomat versionoitu snapshot-malli, assembly request ja `ReportAssembler`. Assembler validoi snapshotit, järjestää Full Checkin `DiagnosticCatalog`in mukaan, muodostaa aggregate-statukset ja delegoi score/coverage-laskennan muuttamattomalle `ScoreCalculator`ille.
- Käyttäjä ilmoitti kohdistetun assembler-unit-testin ja debug-unit-testien Kotlin-käännöksen onnistuneen. Pure assembler on GREEN; Codex ei ajanut Gradlea.
- Lisätty tests-first Run All -integraatiotestit catalog-järjestetylle, privacy-safe snapshot-mappaukselle sekä ensimmäisen canonical raportin deterministiselle jäädytykselle. Käyttäjän kohdistettu unit-testiajo tuotti odotetun REDin; daemon-loki vahvisti syyksi vain tarkoituksella puuttuneen mapperin ja ViewModel-rajapinnan seurannaisvirheineen.
- Käyttäjän pyynnöstä Codex ajaa tästä eteenpäin Gradle-verifioinnit. `ReportAssemblerTest`, `RunAllSnapshotMapperTest` ja `RunAllTestsViewModelTest` läpäisivät kohdistetun debug-unit-testiajon.
- Run All jäädyttää nyt yhden canonical `DiagnosticReport`in, ja Compose-tulosnäkymä muodostaa lokalisoidut UI-rivit vasta raportin stable check ID-, status-, reason- ja typed value -kentistä. Android-bound `RunAllReportBuilder` ja legacy `TestSession` -polku poistettiin.
- Canonical tulosnäkymän tests-first Compose-testi tuotti odotetun REDin vanhasta `TestSession`-parametrista, minkä jälkeen tuotanto- ja Android-testien Kotlin-käännös läpäisivät. Varsinainen instrumentaatioajo estyy nykyisellä Android 17 -laitteella ennen assertioita Espresso-virheeseen `InputManager.getInstance`; sama ympäristövirhe toistuu muuttamattomassa `FonecheckThemeSmokeTest`issä.
- `ktlintCheck` ei käynnistynyt, koska ktlintin omista artifacteista puuttuu dependency-verification-tietueita. Käyttäjän keskeneräistä `verification-metadata.xml`-muutosta ei muokattu eikä uusia tarkistussummia hyväksytty automaattisesti. Task 6:n määritetyt unit-acceptance-portit ovat GREEN.
- Commitin `a8e8a82` jälkeen koko `:app:testDebugUnitTest` läpäisi tuoreen ajon. Task 6 on valmis; Codex ajaa jatkossa suunnitelman Gradle-verifioinnit käyttäjän pyynnön mukaisesti.

### 7. Implement a unified permission-state and standalone permission flow

- **Objective:** Tehdä käyttöoikeuksista ennustettavia kaikissa kategorioissa.
- **Current behavior:** Lupapyynnöt ovat ruutukohtaisia ja puutteellisia; rationalea tai settings recoverya ei ole.
- **Required final behavior:** Yhteinen `PermissionState`: not requested, granted, denied, settings recovery, not required, hardware absent ja partial; jaettu rationale/recovery-UI; Activity Result -pyyntö aina aktiivisen ruudun rajalla.
- **Dependencies:** Kohdat 2–3.
- **Likely areas:** UI components, permission policy, Camera/Audio/SIM/Connectivity.
- **Boundaries and invariants:** Settingsin tai onboardingin avaaminen ei pyydä kaikkia lupia. Denied ei muutu Failiksi. Android-versio tarkistetaan ennen permission-sensitive API:a.
- **Acceptance criteria:** Jokaisella vaarallisella luvalla on selitys, pyyntö, denial, permanent denial/settings ja granted-polku.
- **Tests/verification:** Unit-testit versionkohtaiselle politiikalle; Compose/instrumentation-testit grant/deny/revoke/partial-poluille.
- **Documentation:** `PROJECT.md` permission architecture ja privacy explanations.
- **Risks:** “Permanently denied” ei ole kaikissa Android-versioissa yksiselitteisesti pääteltävissä; UI:n tulee puhua settings recovery -tarpeesta.
- **Decision required:** Ei.

#### Implementation/status log — 2026-08-07

- Lisätty tests-first versionkohtaiset permission-policy-testit Bluetoothin API 31 -rajalle, hardware absencelle, granted/denied/settings-recovery-poluille ja approximate-locationin partial grantille. Codexin kohdistettu unit-testiajo tuotti odotetun REDin vain tarkoituksella puuttuvista policy-symboleista.
- `PermissionPolicyTest` ja koko `:app:testDebugUnitTest` läpäisevät. Jaettu controller säilyttää pyyntöhistorian, päivittää tilan lifecycle-resumessa ja jättää Activity Result -launcherit aktiivisten Compose-ruutujen omistukseen.
- Lisätty jaettu lokalisoitu permission status/rationale/recovery -kortti sekä grant-, denial-, settings recovery-, revoke/not-requested- ja partial-polkujen Compose-testit. Android-testien Kotlin-käännös läpäisee; instrumentaatioajo ei käynnisty, koska ympäristössä ei ole yhdistettyä laitetta.
- Audio, Camera, SIM ja Connectivity käyttävät yhteistä tilamallia, pysäyttävät suojatut aktiiviset operaatiot luvan puuttuessa ja virkistävät tiedot Settingsistä palattaessa. Bluetoothin API 31 -suojattu tila, nimi ja bonded-laitteet luetaan vain `BLUETOOTH_CONNECT`-luvalla; vanhemmilla versioilla tila on `NOT_REQUIRED`.
- Full Check näyttää jokaisen tarvittavan luvan perustelun ja käyttäjän käynnistämän pyyntö-/settings-polun ennen jatkamista. Se ei enää avaa lupadialogia automaattisesti, ja käyttäjä voi jatkaa rajatulla kattavuudella ilman että denial tulkitaan testin epäonnistumiseksi.
- `ktlintCheck` estyy ennen linttausta ktlint-artifactien puuttuviin dependency-verification-tietueisiin. Käyttäjän keskeneräistä `verification-metadata.xml`-muutosta ei muokattu.

### 8. Correct and harden Device diagnostics

- **Objective:** Tuottaa totuudenmukainen, ei-blokkaava device snapshot.
- **Current behavior:** Keruu tapahtuu synkronisesti initissä; root on kuuden polun heuristiikka; osa fallback-teksteistä on kovakoodattu.
- **Required final behavior:** Worker-thread collection, refresh/capture timestamp, locale-neutral values, turvalliset patch/DRM/baseband/bootloader-fallbackit ja selvästi rajattu root heuristic.
- **Dependencies:** Kohdat 2–3 ja 6.
- **Likely areas:** Device ViewModel/probe, screen, report mapper.
- **Boundaries and invariants:** Ei väitettä kompromissittomuudesta. Persistoidaan vain raportin tulkintaan tarvittava device context.
- **Acceptance criteria:** Standalone ja report näyttävät saman snapshotin; kaikki unavailable-arvot ovat eksplisiittisiä; resurssit suljetaan poikkeuksissa.
- **Tests/verification:** Normalization/fallback/root-heuristic/DRM-close-yksikkötestit ja fyysinen smoke-testi.
- **Documentation:** `PROJECT.md` Device-status; disclaimer-tekstit.
- **Risks:** OEM-kohtaiset puuttuvat tai muotoilemattomat Build-arvot.
- **Decision required:** Ei.

#### Implementation/status log — 2026-08-07

- Lisätty tests-first normalisointi-, security patch-, rajattu root-artifact-heuristiikka- ja DRM cleanup -testit. Kohdistettu RED johtui vain tarkoituksella puuttuneista probe-symboleista; toteutuksen jälkeen testit ovat GREEN.
- Device-keruu on siirretty injektoidulle IO-dispatcherille ja julkaisee aikaleimatun, päivitettävän `DeviceInfoState`-snapshotin. Onnistunut snapshot säilyy näkyvissä myös myöhemmän refresh-virheen aikana.
- Build-, patch-, kernel-, baseband-, bootloader- ja Widevine-arvot normalisoidaan locale-neutraaliksi raakadataksi; null-, blank-, `unknown`- ja virhepolut käyttävät vakaata `unavailable`-arvoa, jonka standalone-UI lokaloi vasta näyttörajalla. Widevine-istunto suljetaan myös lukupoikkeuksessa ja API 26–27 käyttää yhteensopivaa release-polkuansa.
- Standalone näyttää snapshotin keräysajan, refresh/error-polun sekä root-tarkistuksen rajallisuutta koskevan eksplisiittisen disclaimerin. Havaitsematta jäämistä ei esitetä todisteena kompromissittomuudesta.
- Full Check odottaa Device-keruun valmistumista, käyttää samaa snapshotia ja sen capture-aikaa sekä tallentaa vain raportin tulkintaan tarvittavan device contextin. Root-evidence on `LOW`-confidence-estimaatti: ei havaintoa on `INFO`, tunnettu artifact on `WARNING`.
- `DeviceInfoProbeTest`, `DeviceInfoViewModelTest`, `RunAllSnapshotMapperTest`, koko `:app:testDebugUnitTest` ja Android-testien Kotlin-käännös läpäisevät. Fyysistä smoke-testiä ei voitu ajaa, koska `adb devices -l` ei löytänyt yhdistettyä laitetta.

### 9. Complete Performance information and bounded benchmarks

- **Objective:** Sovittaa kategorian nimi ja evidence yhteen.
- **Current behavior:** Näyttää CPU/RAM/GPU-tietoa, mutta ei suorituskykytestiä; EGL voi käynnistyä pääsäikeellä ja confidence voi olla virheellinen.
- **Required final behavior:** Hardware info sekä käyttäjän käynnistämät, lyhyet, peruutettavat CPU- ja memory-throughput-testit; raakanopeus, olosuhteet ja thermal context ilman terveysrajoja.
- **Dependencies:** Kohdat 1–3, 6 ja Thermal-evidence-sopimus kohdasta 17.
- **Likely areas:** Performance probe/ViewModel/screen, benchmark engine, report mapper.
- **Boundaries and invariants:** Deterministinen rajattu työmäärä; ei universaalia pass/fail-rajaa; EGL vapautuu `finally`-polulla.
- **Acceptance criteria:** Benchmark ei blokkaa UI:ta, voidaan perua ja tulos merkitään informational evidenceksi.
- **Tests/verification:** Workload-, timeout-, cancellation-, low-memory-, confidence- ja EGL-cleanup-testit; fyysinen toistettavuustarkistus.
- **Documentation:** `PROJECT.md`, benchmark limitations.
- **Risks:** Thermal throttling, taustakuorma ja OEM scheduler tekevät vertailusta suuntaa-antavan.
- **Decision required:** Kyllä, Performance-malli kohdassa 1.

#### Implementation/status log — 2026-08-07

- Lisätty tests-first rajatun workloadin, low-memory-polun, CPU-confidence-luokituksen ja EGL-session cleanupin testit. Ensimmäinen kohdistettu ajo oli odotettu RED vain puuttuvista benchmark/probe-symboleista; toteutuksen jälkeen testit ovat GREEN.
- Performance hardware snapshot kerätään injektoidulla IO-dispatcherilla raakamuodossa: RAM tavuina, CPU-taajuudet nullable MHz -arvoina, GPU-arvot vakaalla `unavailable`-koodilla ja capture timestampilla. CPU-confidence on `HIGH` vain jos vähintään yksi taajuusarvo saadaan.
- EGL-resurssit vapautetaan `finally`-polulla myös lukuvirheessä ja osittain epäonnistunut setup purkaa luodut resurssit. Vulkan-teksti kertoo täsmällisesti vain Androidin ilmoittaman feature flagin.
- Käyttäjän käynnistämä benchmark tekee enintään kaksi miljoonaa kiinteää CPU-operaatiota sekä 4 MiB:n puskurilla yhteensä 64 MiB:n memory read/write -työn. Se toimii IO-dispatcherilla, tekee kooperatiiviset cancellation-checkpointit, rajoittuu ViewModelissa viiteen sekuntiin ja palauttaa OOM-tilanteessa CPU-tuloksen ilman memory-väitettä.
- Standalone näyttää raw operations/s- ja MiB/s-tulokset, käsitellyn muistimäärän, keston, ennen/jälkeen thermal-statuskoodit ja selkeän olosuhde/version-rajoitteen ilman pass/fail- tai terveysrajoja. Full Check ajaa saman hyväksytyn rajatun benchmarkin automaattisessa vaiheessa ja tallentaa tulokset `INFO`/`LOW`-confidence-evidenceksi.
- `PerformanceBenchmarkTest`, `PerformanceInfoProbeTest`, `PerformanceInfoViewModelTest`, `RunAllSnapshotMapperTest`, koko `:app:testDebugUnitTest` ja Android-testien Kotlin-käännös läpäisevät. Timeout-, cancellation-, low-memory-, confidence- ja cleanup-polut ovat automaattisesti katettuja; fyysinen toistettavuustarkistus odottaa yhdistettyä laitetta.

### 10. Complete SIM and telephony diagnostics

- **Objective:** Erotella telephony hardware, SIM-tila, permission ja puuttuva tieto.
- **Current behavior:** Perustiedot toimivat, mutta standalone ei pyydä `READ_PHONE_STATE`-lupaa ja report mapping vertailee englanninkielisiä tekstejä.
- **Required final behavior:** Täysi standalone-lupupolku, useful limited mode ilman lupaa, vakaat koodit single/multi-SIM-, eSIM-, inactive-, no-hardware- ja unknown-tiloille.
- **Dependencies:** Kohdat 6–7.
- **Likely areas:** SIM ViewModel/screen/probe, manifest-version guards, report mapper.
- **Boundaries and invariants:** Ei subscription- tai cell ID:tä persistenceen. Denial vähentää coveragea eikä aiheuta hardware Failia.
- **Acceptance criteria:** No telephony, no SIM, inactive SIM, single SIM, multiple SIM ja denied näkyvät erillisinä.
- **Tests/verification:** Unit-faket kaikille tiloille ja API-haaroille; fyysinen single/dual-SIM-tarkistus mahdollisuuksien mukaan.
- **Documentation:** `PROJECT.md`, permission/privacy copy.
- **Risks:** OEM/eSIM-data voi olla rajoitettua tai permission-riippuvaista.
- **Decision required:** Ei.

#### Implementation/status log — 2026-08-07

- Lisätty tests-first vakaat telephony hardware-, inventory-, slot state-, activity-, form factor-, phone type- ja network generation -koodit. Probe-testit erottelevat no-telephony-, no-SIM-, inactive-, single-, multiple- ja unknown-tilat sekä API 28 eSIM- ja API 30 modem count -haarat.
- Android-provider kerää SIM-tiedot injektoidun provider-rajapinnan kautta ViewModelin IO-dispatcherilla. `activeModemCount`/legacy `phoneCount` ja `isEmbedded` on suojattu alustaversioilla; `SecurityException`- ja OEM runtime -virheet palautuvat rajattuna tai unknown-datana.
- Standalone käyttää Task 7:n yhteistä Phone-permission-polkua, näyttää ilman lupaa edelleen hardware-, slot- ja SIM-state-tiedot sekä lokalisoidun limited-mode-ilmoituksen. Operaattori-, maa-, eSIM- ja network-tiedot luetaan vain luvalla; virhetila säilyttää viimeisen onnistuneen snapshotin ja tarjoaa refreshin.
- Report mapping käyttää vain vakaita inventory/network-koodeja. Subscription ID:tä käytetään providerissa vain hetkellisesti slot-kohtaisen managerin luomiseen; subscription- tai cell ID:tä, operaattoria tai maatietoa ei tallenneta evidenceen. Denial tuottaa `NOT_TESTED/PERMISSION_DENIED`-network-evidencen eikä hardware-failia.
- `SimTelephonyProbeTest`, `SimTelephonyViewModelTest`, `RunAllSnapshotMapperTest`, koko `:app:testDebugUnitTest` ja Android-testien Kotlin-käännös läpäisevät. Fyysistä single/dual-SIM-tarkistusta ei voitu ajaa, koska `adb devices -l` ei löytänyt yhdistettyä laitetta.

### 11. Replace Display tests with controlled fullscreen, drag, and multi-touch flows

- **Objective:** Tehdä fyysiset näyttö- ja kosketustestit oikeasti käyttökelpoisiksi.
- **Current behavior:** App bar/system UI rajaavat värikenttää; touch grid rekisteröi vain napautukset; multi-touch-funktiota ei kutsuta; pass/fail-vahvistus puuttuu.
- **Required final behavior:** Hallittu fullscreen, saavutettava exit, RGB/white/black/gray-gradient-kentät, eksplisiittinen pass/fail, drag coverage, samanaikaiset pointerit ja reset/complete.
- **Dependencies:** Kohdat 2–3 ja vakaa navigation shell.
- **Likely areas:** Display screen/ViewModel, MainActivity chrome policy, report mapper.
- **Boundaries and invariants:** Resolution label kertoo lähteen: app window, display mode tai physical metric. Pitkä painallus ei ole ainoa poistumistapa.
- **Acceptance criteria:** Testi kattaa kontrolloidun näyttöalueen; drag täyttää ruudut; pointer count/positions ovat todellisia; tulos tallentuu manual evidenceksi.
- **Tests/verification:** Pointer/drag-unit- ja Compose UI -testit; cutout-, gesture-nav-, landscape-, large-font- ja TalkBack-manuaalitestit.
- **Documentation:** `PROJECT.md`, testiohjeet ja accessibility copy.
- **Risks:** System bar -käyttäytyminen vaihtelee Android-versioittain; burn-in-kenttä ei saa itse jäädä pitkäksi aikaa näytölle.
- **Decision required:** Kyllä, gradient-oletus kohdassa 1.

#### Implementation/status log — 2026-08-08

- Lisätty tests-first kuuden kentän vakaa järjestys (red, green, blue, white, black, gray gradient), drag-segmenttien grid-geometria sekä pointer ID:ihin sidottujen samanaikaisten kosketussijaintien reducer-testit. Ensimmäiset unit- ja Android-testikäännökset olivat odotettu RED vain puuttuvista uusista symboleista.
- Standalone visual flow näyttää kaikki kuusi kenttää, edellinen/seuraava-kontrollit, eksplisiittisen hyväksytty/ongelma-tuloksen, Back-käsittelyn ja aina näkyvän lokalisoidun Exit-painikkeen. Tulos säilyy `VisualTestState.result`-manual-arvona. Kenttä sulkeutuu automaattisesti 120 sekunnin jälkeen.
- Touch flow käyttää koko kontrolloitua Canvas-aluetta: pointer-eventit välittävät todelliset ID:t ja normalisoidut sijainnit, drag täyttää kaikki ylitetyt solut, aktiiviset pointerit piirretään ja peak count säilyy. Reset tyhjentää mittauksen ja Complete jäädyttää tuloksen.
- MainActivity piilottaa testin aikana sovelluspalkin ja system barit sekä sallii transienttien barien palauttamisen pyyhkäisyllä. Standalone- ja Full Check -kontrollit käyttävät safe-drawing-insettejä; poistuttaessa barit palautetaan. Full Check käyttää samaa gradientin sisältävää pattern-sarjaa, säilyttää nykyisen eksplisiittisen manual pass/fail -evidencen ja ohittaa vaiheen 120 sekunnin turvarajalla.
- Resolution-rivi nimeää arvon lähteeksi app window-, display mode- tai physical metrics -haaran. `DisplayInteractionTest`, koko `:app:testDebugUnitTest` ja Android-testien Kotlin-käännös läpäisevät. Compose pointer/drag -testi kääntyy; instrumentaatio sekä cutout-, gesture-nav-, landscape-, large-font- ja TalkBack-manuaalitarkistukset odottavat yhdistettyä laitetta.

### 12. Correct Audio permissions, routing, evidence, and resource ownership

- **Objective:** Poistaa silent no-opit, harhaanjohtava dB-merkintä ja double-release-riskit.
- **Current behavior:** Standalone microphone ei pyydä lupaa; relative RMS esitetään dB-tyyppisenä; worker ja cancellation voivat vapauttaa saman resurssin.
- **Required final behavior:** Täysi microphone-lupupolku, yksi idempotentti omistaja kullekin AudioRecord/AudioTrack-resurssille, audio focus/route handling, relative input level -nimi ja manuaaliset kuunteluvahvistukset.
- **Dependencies:** Kohdat 3, 6–7.
- **Likely areas:** Audio ViewModel/engine/screen, report mapper.
- **Boundaries and invariants:** PCM pysyy muistissa ja vapautuu testin jälkeen. Ei `dB`, `dBA` tai SPL-väitettä. API device detection ei todista kuultua ääntä.
- **Acceptance criteria:** Speaker, stereo, earpiece, recording ja playback voidaan toistaa/perua ilman resurssivuotoa; kaikki manuaaliset tulokset ovat eksplisiittisiä.
- **Tests/verification:** Permission denial, repeated start/stop, cancellation, route change, mute/extreme volume ja double-release-testit; wired/Bluetooth/earpiece-laitekoe.
- **Documentation:** `PROJECT.md`, audio limitation- ja privacy-tekstit.
- **Risks:** Reititys vaihtelee OEM:n ja kytkettyjen laitteiden mukaan.
- **Decision required:** Kyllä, Full Check audio -laajuus kohdassa 1.

#### Implementation/status log — 2026-08-08

- Lisätty tests-first idempotentti `AudioResourceOwner`, audio route -session, recording-policy ja relative PCM input level -laskenta. Ensimmäinen kohdistettu ajo oli odotettu RED vain puuttuvista sopimussymboleista; repeated replace/stop, cancellation-close, permission denial sekä mute/max-amplitudi ovat unit-katettuja.
- Tone-, stereo-, earpiece-, recording- ja playback-polut käyttävät erillisiä yhden omistajan resursseja. Stop/cancellation, workerin `finally` ja `onCleared` voivat kaikki yrittää sulkea resurssin, mutta vain owner vapauttaa sen kerran. Myös AudioTrack-creation-virhe palauttaa focus/route-tilan.
- Output pyytää transientin audio focusin. Media käyttää mediareittiä; earpiece käyttää communication modea ja API 31+:ssa built-in-earpiece communication devicea, jonka jälkeen aiempi mode/reitti palautetaan. Uuden output-testin käynnistys pysäyttää edellisen tone/playback-session.
- Mikrofonin lupa- ja hardware-policy estää silent no-opin ja duplicate startin. Input UI näyttää vain suhteellisen 0–100 % PCM-amplitudin ja eksplisiittisen ei-kalibroidun rajoitteen; PCM säilyy vain muistissa toistettavuutta varten ja tyhjennetään ViewModelin sulkeutuessa.
- Speaker-, stereo-, earpiece- ja recording playback -kuuntelut vaativat standalone-UI:ssa eksplisiittisen onnistui/ongelma-vahvistuksen. Full Checkin microphone sample -evidence on `INFO`, ei automaattinen PASS; kuultu speaker-tulos säilyy user-confirmation-evidencenä.
- `AudioRuntimePolicyTest`, `RunAllSnapshotMapperTest`, koko `:app:testDebugUnitTest` ja Android-testien Kotlin-käännös läpäisevät. Wired-, Bluetooth- ja earpiece-laitekokeita ei voitu ajaa, koska yhdistettyä Android-laitetta ei ole.

### 13. Complete Camera enumeration, permissions, and lifecycle safety

- **Objective:** Testata kaikki julkisesti käyttäjän saavutettavat kamerat ilman kuvahistorian tallennusta.
- **Current behavior:** Standalone-lupupyyntö puuttuu; vain ensimmäinen etu- ja takakamera säilytetään; timeout/late success ja resource cleanup vaativat kovennusta.
- **Required final behavior:** Täysi permission flow, Camera2-ID-luettelo ja logical/physical/external-luokitus, valittavat kamerat, preview/capture/zoom/focus/flash/OIS-metadata sekä idempotentti teardown.
- **Dependencies:** Kohdat 3, 6–7.
- **Likely areas:** Camera ViewModel/screen, CameraX/Camera2 adapter, report mapper.
- **Boundaries and invariants:** Ei kuva- tai thumbnail-bytes persistenceen. Fyysistä alikameraa ei väitetä testatuksi, ellei Android salli sen itsenäistä käyttöä.
- **Acceptance criteria:** Jokainen valittava kamera voidaan previewata ja vahvistaa; historyyn menee vain luokitus, capture success, dimensions, aika ja vahvistus.
- **Tests/verification:** Permission, enumeration mapping, timeout/success race, navigation, torch-off ja executor cleanup; multi-camera/external-camera fyysiset testit.
- **Documentation:** `PROJECT.md`, camera privacy/limitations.
- **Risks:** CameraX-selector ja Camera2-ID-yhteensopivuus; OEM logical camera behavior.
- **Decision required:** Kyllä, Full Check camera -laajuus kohdassa 1.

#### Implementation/status log — 2026-08-08

- Lisätty tests-first Camera2 public-ID -luokittelu standard-, logical-, independently selectable physical- ja external-kameroille sekä token-pohjainen capture gate. Ensimmäinen kohdistettu ajo oli odotettu RED vain puuttuvista mapping/gate-symboleista; hidden physical ID:tä ei luokitella valittavaksi.
- Capability-keruu säilyttää kaikki Androidin `cameraIdList`issä julkaisemat kamerat. Logical physical-ID -metadata kirjataan rajoitteeksi, mutta preview-valinta käyttää CameraX Camera2Interop -filteriä vain julkiselle täsmälliselle camera ID:lle. UI tarjoaa jokaiselle valittavalle ID:lle preview/capture-polun, luokituksen, flash/OIS/zoom/focus-metadatan ja eksplisiittisen vahvistuksen.
- Preview-generation estää myöhäistä provider-callbackia sitomasta poistunutta näkymää. Capture-token hyväksyy vain yhden voimassa olevan success/error-callbackin; kahdeksan sekunnin timeout, navigation teardown ja uusi preview mitätöivät myöhäisen tuloksen.
- Teardown sammuttaa torchin, peruu capture-timerin, mitätöi callbackit, kutsuu `unbindAll()` idempotentisti ja sulkee executorin ViewModelin mukana. `ImageProxy` suljetaan aina; thumbnailia, kuva- tai preview-byteja ei enää rakenneta tai tallenneta.
- Canonical report tallentaa vain julkisten kameroiden määrän, logical-luokitusmäärän, capture-dimensiot ja ajan sekä nykyisen käyttäjän capture-vahvistuksen. Kamera-ID:tä tai kuva-aineistoa ei persistoidu.
- `CameraRuntimePolicyTest`, koko `:app:testDebugUnitTest` ja Android-testien Kotlin-käännös läpäisevät. Multi-camera-, external-camera-, zoom/focus-, torch- ja navigation-laitekokeet odottavat yhdistettyä Android-laitetta.

### 14. Complete guided Sensors diagnostics and listener lifecycle

- **Objective:** Muuttaa sensoriluettelo todellisiksi, rajatuiksi yleisten sensorien testeiksi.
- **Current behavior:** Vain motion-haasteita; challenge-listener voi jäädä rekisteröidyksi; useimmat sensorit ovat vain listassa.
- **Required final behavior:** Inventory sekä accelerometer-, gyroscope-, gravity-, proximity-, light-, magnetometer-, barometer- ja step-testit laitteiston mukaan; sampling; symmetrinen register/unregister.
- **Dependencies:** Kohdat 2–3 ja 6.
- **Likely areas:** Sensors ViewModel/probe/screen, report mapper.
- **Boundaries and invariants:** Absence on `NotAvailable`; derived orientation merkitään derived-evidenceksi; yksiköt ja accuracy resource-backed.
- **Acceptance criteria:** Haasteen clear/complete/skip/navigation pysäyttää sen listenerin; raportti kertoo täsmälleen testatut sensorit.
- **Tests/verification:** Sensor-event-faket, thresholdit, sampling, cancellation ja no-sensor-polut; fyysinen motion/proximity/light-koe.
- **Documentation:** `PROJECT.md`, sensor pass-condition/limitations.
- **Risks:** Sensorien nimet, update rate ja accuracy vaihtelevat OEM:ittäin.
- **Decision required:** Ei.

#### Implementation/status log — 2026-08-08

- Lisätty tests-first kahdeksan vakaan sensorikoodin katalogi sekä no-sensor-, step-prioriteetti-, näytteistys-, muutosraja-, challenge-threshold-, accuracy- ja idempotentti listener-owner -testit. Ensimmäinen kohdistettu ajo oli odotettu RED vain puuttuvista sensoripolitiikan symboleista.
- Standalone-näkymä näyttää accelerometer-, gyroscope-, gravity-, proximity-, light-, magnetometer-, barometer- ja step-testit myös silloin, kun laitteisto puuttuu. Saatavilla oleva testi käynnistää rajatun näytteistyksen, näyttää lokalisoidut yksiköt ja accuracy-tilan sekä erottaa `NotAvailable`-, `NotTested`-, sampling-, skipped- ja pass-tilat.
- Liike-, valo-, proximity-, magnetometer- ja step-testit vaativat mitatun arvomuutoksen; barometri vaatii viisi kelvollista painehavaintoa. Challenge-laskenta on erotettu ViewModelista nimetyillä raja-arvoilla, ja valmis challenge säilyttää mitatun näytemäärän sekä accuracy-koodin.
- Yksi synkronoitu listener-owner omistaa aktiivisen rekisteröinnin. Completion, clear, skip, kortin sulkeminen, Full Check -vaiheen vaihto, composablen dispose ja ViewModelin `onCleared()` poistavat listenerin idempotentisti ilman map-iteraation aikaista muokkausta.
- Canonical report nimeää kaikki kahdeksan ohjattua sensoritestiä ja säilyttää niiden todelliset pass/not-tested/not-available-tulokset. Asentovaste on erillinen low-confidence `DERIVED`-evidence eikä suora laitteistopass; Full Checkin motion-tulos on automatic measurement eikä user confirmation.
- `SensorRuntimePolicyTest`, koko `:app:testDebugUnitTest` ja Android-testien Kotlin-käännös läpäisevät. Fyysiset motion-, proximity- ja light-kokeet odottavat yhdistettyä Android-laitetta; `adb devices -l` ei löytänyt laitetta. Käyttäjän keskeneräistä `PROJECT.md`-muutosta ei koskettu.

### 15. Correct Connectivity permissions, callbacks, claims, and privacy

- **Objective:** Vakauttaa Wi-Fi-, Bluetooth-, NFC-, GNSS- ja mobile-evidence.
- **Current behavior:** Osa state-kentistä jää tyhjäksi; BLE esitetään virheellisesti controller-versiona; Bluetooth-lupupyyntö puuttuu; GPS removal käyttää väärää callback-instanssia.
- **Required final behavior:** Tarkat hardware/enabled/permission/unavailable-tilat, oikea callback ownership, overlapping-search esto, totuudenmukaiset Wi-Fi/Bluetooth-labelit ja sanitisoitu report evidence.
- **Dependencies:** Kohdat 6–7.
- **Likely areas:** Connectivity ViewModel/probes/screen, permission UI, report mapper.
- **Boundaries and invariants:** Ei tarkkaa sijaintia, raw SSID/BSSID:tä, cell ID:tä tai raw operator ID:tä raporttiin. Capability ei todista NFC-radion fyysistä toimintaa.
- **Acceptance criteria:** GNSS listenerit poistuvat fixissä, timeoutissa, cancelissa ja navigationissa; BT-denial ja hardware absent ovat eri tilat; turhat state-kentät joko täytetään oikein tai poistetaan.
- **Tests/verification:** Callback identity/overlap, Android-versiokohtaiset permission-testit, location timeout ja sanitized mapping; fyysiset Wi-Fi/BT/NFC/GNSS-kokeet.
- **Documentation:** `PROJECT.md`, privacy copy ja manifest-vaikutukset.
- **Risks:** SSID- ja network metadata -rajoitukset vaihtelevat Android-versioittain.
- **Decision required:** Kyllä, network speed ja NFC kohdassa 1.

### 16. Correct Battery normalization and unavailable behavior

- **Objective:** Poistaa negatiivinen Home-arvo ja perusteettomat capacity/health-väitteet.
- **Current behavior:** Home voi näyttää `-1%`; current sign/confidence on rajattu heuristiikka; capacity-osio on tyhjä.
- **Required final behavior:** Unavailable epäonnistuneelle lukemalle, dokumentoitu current normalization/plausibility, public API cycle count, OEM confidence ja tyhjän capacity-sisällön poisto tai selitys.
- **Dependencies:** Kohdat 2–3 ja 6.
- **Likely areas:** Battery ViewModel/probe/screen, Home ViewModel, report mapper.
- **Boundaries and invariants:** Ei `PowerProfile`-reflektiota, vendor-file scrapingia tai Android health enumin tulkintaa prosenttiterveydeksi.
- **Acceptance criteria:** Virheellinen arvo ei näy mittauksena; confidence ja caveat säilyvät snapshotissa.
- **Tests/verification:** Charging/discharging, current sign, implausible current, missing value, invalid cycle count ja OEM-profile-testit.
- **Documentation:** `PROJECT.md`, battery limitations.
- **Risks:** OEM current-sign convention ei ole yhdenmukainen.
- **Decision required:** Ei.

### 17. Add the Thermal diagnostic category

- **Objective:** Toteuttaa puuttuva 13.10-specin mukainen Thermal-kategoria.
- **Current behavior:** Vain enum-arvo; ei reittiä, UI:ta, statea tai report evidenceä.
- **Required final behavior:** Standalone-reitti, live Android thermal status, battery temperature, tuettu thermal headroom, throttling/severity-tulkinta ja unsupported-tila. Android tarjoaa thermal status -kyselyn ja listenerin API 29+:ssa [PowerManagerin julkisen API:n kautta](https://developer.android.com/reference/android/os/PowerManager).
- **Dependencies:** Kohdat 2–3 ja 6.
- **Likely areas:** Diagnostic catalog/routes, Thermal probe/ViewModel/screen, report mapper.
- **Boundaries and invariants:** Ei sysfs-scrapingia, keksittyä CPU-lämpöä, cooling-system health -väitettä tai kontrolloimatonta stressiä.
- **Acceptance criteria:** Kategoria näkyy kanonisessa järjestyksessä; listener poistuu lifecycle-stopissa; unsupported API ja normaali thermal state ovat eri tulokset; snapshot on persistence/export-ready.
- **Tests/verification:** Status mapping, API-level, headroom unavailable, listener cleanup ja battery-temperature-yhdistely; fyysinen API 29+/vanhempi laitekoe.
- **Documentation:** `PROJECT.md`, Thermal limitations ja category coverage.
- **Risks:** Headroom-tuki ja näytteen pätevyys vaihtelevat laitteittain.
- **Decision required:** Kyllä, observation-only kohdassa 1.

### 18. Add Storage information and the safe app-private benchmark

- **Objective:** Toteuttaa puuttuva Storage-kategoria ilman käyttäjätiedostojen riskiä.
- **Current behavior:** Vain enum-arvo.
- **Required final behavior:** Total/used/available/usage/mounted/volume/removable-tiedot sekä hyväksytty 64 MiB app-cache sequential write/read -testi, free-space precheck, cancel ja cleanup.
- **Dependencies:** Kohdat 1–3 ja 6.
- **Likely areas:** Diagnostic catalog/routes, Storage probe/benchmark/ViewModel/screen, report mapper.
- **Boundaries and invariants:** Vain app-controlled cache; ei universal pass/fail-rajaa, flash wear- tai physical health -väitettä. Hitaat storage-kutsut ja benchmark ajetaan worker-säikeellä; Android dokumentoi myös joidenkin `StorageManager`-kutsujen voivan kestää sekunteja [StorageManager API:ssa](https://developer.android.com/reference/android/os/storage/StorageManager).
- **Acceptance criteria:** Temp-tiedosto poistuu success/failure/cancel-polussa; liian pieni vapaa tila tuottaa not-tested/unavailable-reasonin; tulos sisältää raw MiB/s ja testiolosuhteet.
- **Tests/verification:** Fake filesystem/free-space, cancellation, exception, cleanup, checksum/data-size ja timing calculations; fyysiset low-space ja normal-space-kokeet.
- **Documentation:** `PROJECT.md`, benchmark specification/limitations.
- **Risks:** OS cache, thermal state ja muu I/O vaikuttavat tulokseen.
- **Decision required:** Kyllä, workload/default Full Check inclusion kohdassa 1.

### 19. Harden Vibration execution and confirmation

- **Objective:** Estää päällekkäiset värinät ja erottaa API support fyysisestä vahvistuksesta.
- **Current behavior:** Pulssit/patternit toimivat, mutta cancel/onCleared-polut puuttuvat.
- **Required final behavior:** Idempotentti `cancel`, lifecycle cleanup, replay/pass/fail/skip, varoitus voimakkaista pattern-valinnoista ja accessible alternative.
- **Dependencies:** Kohdat 2–3 ja 6.
- **Likely areas:** Vibration ViewModel/screen, report mapper.
- **Boundaries and invariants:** Effect support on API evidence; “felt vibration” on manual evidence.
- **Acceptance criteria:** Navigation, uusi pattern, cancel ja ViewModel clear pysäyttävät värinän; no-vibrator on `NotAvailable`.
- **Tests/verification:** Fake vibrator overlap/cancel/support-testit ja fyysinen legacy/amplitude/composition-koe.
- **Documentation:** `PROJECT.md`, vibration warnings.
- **Risks:** Voimakkuus ja supported effect -raportointi vaihtelevat laitteittain.
- **Decision required:** Ei.

### 20. Make Buttons testing bounded and truthful

- **Objective:** Poistaa indefinite polling ja väärä “all buttons” -mielikuva.
- **Current behavior:** Music-stream-volyymia pollataan 100 ms välein; ääriarvot ja ulkoinen muutos voivat vääristää tuloksen.
- **Required final behavior:** Accessible hardware buttons -rajaus, luotettavin sallittu volume up/down -polku, timeout, retry, skip, reset ja varmasti pysähtyvä polling.
- **Dependencies:** Kohdat 2–3 ja 6.
- **Likely areas:** Buttons ViewModel/screen, report mapper.
- **Boundaries and invariants:** Power-painiketta ei väitetä testatuksi. Ohjelmallista tai ulkoista äänenvoimakkuusmuutosta ei merkitä fyysiseksi evidenceksi ilman erottelua.
- **Acceptance criteria:** Kumpikin suunta voidaan suorittaa myös lähtötilan ollessa ääriarvossa; vaihe ei odota rajatta.
- **Tests/verification:** Timeout, min/max, external change, completion ja lifecycle-stop-testit; fyysinen volume-painikekoe.
- **Documentation:** `PROJECT.md`, button limitation copy.
- **Risks:** Android ei tarjoa kaikkien järjestelmäpainikkeiden sieppausta sovellukselle.
- **Decision required:** Ei.

### 21. Complete Biometrics capability and prompt outcomes

- **Objective:** Erotella hardware presence, enrollment, authenticator class ja onnistunut prompt.
- **Current behavior:** Strong/weak/device credential näytetään, mutta fingerprint/face-laitteistoa ei erotella ja nonterminal failure voi jättää Full Checkin odottamaan.
- **Required final behavior:** PackageManager feature -tiedot, eksplisiittinen allowed-authenticator-policy, success/failure/retry/cancel/lockout/no-enrollment/unavailable/terminal-error-tilat.
- **Dependencies:** Kohdat 2–3 ja 6.
- **Likely areas:** Biometrics ViewModel/screen/prompt coordinator, report mapper.
- **Boundaries and invariants:** Ei pääsyä biometrisiin templateihin; onnistuminen ei todista sensorin laatua; device credential ei korvaa biometriaa hiljaisesti.
- **Acceptance criteria:** Jokainen terminal outcome päättää vaiheen kerran; nonterminal failure tarjoaa retry/exit-polun.
- **Tests/verification:** Fake prompt callback -testit, authenticator policy ja feature combinations; fyysinen fingerprint/face/no-enrollment-koe.
- **Documentation:** `PROJECT.md`, biometric disclaimer.
- **Risks:** Face feature flags ja strong/weak-luokitus vaihtelevat OEM:ittäin.
- **Decision required:** Ei.

### 22. Build Full Check preflight and dynamic applicable stage plan

- **Objective:** Korvata välitön permission wall ymmärrettävällä valmistelulla.
- **Current behavior:** Run All pyytää heti puuttuvat luvat yhdessä launcherissa ja käyttää kiinteää stage-enumia.
- **Required final behavior:** Preflight kertoo testit, interaktiot, äänen/värinän/storage-kirjoituksen, luvat, unsupported-käytöksen, paikallisen tallennuksen ja skip-vaihtoehdot; stage list muodostuu catalogista, hardwaresta, luvista ja hyväksytyistä valinnoista.
- **Dependencies:** Kohdat 1–21.
- **Likely areas:** Run All state model/screen, permission coordinator, diagnostic catalog.
- **Boundaries and invariants:** Optional denial ei estä koko ajoa. Progress perustuu todelliseen stage listaan eikä kovakoodattuun määrään.
- **Acceptance criteria:** Käyttäjä näkee ennen lupia mitä tapahtuu; denied/absent/skipped muodostavat oikean evidence- ja coverage-vaikutuksen.
- **Tests/verification:** Stage planner -unit-testit hardware/permission/skip-yhdistelmille ja Compose preflight -testit.
- **Documentation:** `PROJECT.md`, Full Check behavior ja permission rationales.
- **Risks:** Applicability-säännöt voivat erkaantua standalone-kategorioista; niiden tulee käyttää samaa catalog/policy-lähdettä.
- **Decision required:** Kyllä, network/storage/audio/camera-valinnat kohdassa 1.

### 23. Rebuild Full Check orchestration and lifecycle ownership

- **Objective:** Tehdä 14 kategorian state machine idempotentiksi ja peruutettavaksi.
- **Current behavior:** Compose-efektit ohjaavat useita ViewModeleja, kiinteät viiveet ja loopit mahdollistavat late-success-, duplicate-advance- ja indefinite-wait-tilat.
- **Required final behavior:** Yksi orkestroija omistaa stage tokenin, transitionin, timeoutin, retry/skip/fail/unavailable/cancel-polut ja kategoriakohtaisen teardownin.
- **Dependencies:** Kohta 22 ja valmiit kategoriatoiminnot.
- **Likely areas:** Run All ViewModel/orchestrator, category adapters, screen.
- **Boundaries and invariants:** Yksi stage voi päättyä kerran. Cancel pysäyttää äänen, mikrofonin, kameran/torchin, sensorit, GNSS:n, värinän, button pollingin ja storage-työn. Konfiguraatiomuutos ei käynnistä vaihetta uudelleen.
- **Acceptance criteria:** Kaikki 14 kategoriaa ovat mukana; jokaisessa interaktiivisessa vaiheessa on saavutettava skip/recovery; cancel ei luo valmista raporttia.
- **Tests/verification:** State-machine unit-testit duplicate callbackille, timeout/success-racelle, recompositionille, rotationille, backgroundille ja jokaisen vaiheen cancelille.
- **Documentation:** `PROJECT.md` orchestration ja lifecycle contract.
- **Risks:** Monen nykyisen ViewModelin yhtäaikainen omistus; toteutus ei saa synnyttää toista category-listaa.
- **Decision required:** Kyllä, interruption policy kohdassa 1.

### 24. Finalize and persist completed Full Check and retest reports

- **Objective:** Tallentaa vain eksplisiittisesti valmis immutable report.
- **Current behavior:** `completeSession` jää muistiin; “Open test” avaa live-näkymän selittämättä frozen/live-erotusta.
- **Required final behavior:** Completion lukitsee started/completed/duration-, app/device-, score/coverage- ja evidence-snapshotin ja insertoi sen kerran. Retest luo uuden category-only-raportin.
- **Dependencies:** Kohdat 5–6 ja 22–23.
- **Likely areas:** Run All completion, report repository, retest navigation.
- **Boundaries and invariants:** Ei osittaista reporttia cancelista tai process deathistä. Saved evidence ja live retest ovat eri route/context.
- **Acceptance criteria:** Duplicate completion ei lisää kahta raporttia; valmis raportti löytyy restartin jälkeen; vanha raportti ei muutu retestissä.
- **Tests/verification:** Idempotent completion, repository failure/retry, process restart, cancel ja category-retest-integration-testit.
- **Documentation:** `PROJECT.md`, immutable-report/retest behavior.
- **Risks:** Tallennusvirhe tulosruudulla; UI:n on tarjottava retry ilman report payloadin uudelleenmittausta.
- **Decision required:** Kyllä, retest policy kohdassa 1.

### 25. Implement completed and saved Report detail

- **Objective:** Korvata Report-placeholder täydellä raporttinäkymällä.
- **Current behavior:** Full Check näyttää vain nykyisen tulosryhmittelyn; saved Report route on placeholder.
- **Required final behavior:** Device/app/date/duration, score state, coverage, counts, 14 canonical categorya, evidence source/confidence/reasons sekä `View saved evidence` ja `Retest`.
- **Dependencies:** Kohta 24.
- **Likely areas:** Report route/ViewModel/screen, localized evidence renderer.
- **Boundaries and invariants:** Historiallista reporttia ei renderöidä live-stateista. NotAvailable ja NotTested ovat erillisiä.
- **Acceptance criteria:** Täydellinen, partial ja category-only report näkyvät oikein; tuntematon reason/value saa turvallisen fallbackin.
- **Tests/verification:** Renderer-unit-testit ja Compose UI -testit kaikille statuksille, pitkille arvoille, empty/corrupt/loading/error-tiloille.
- **Documentation:** `PROJECT.md`, report UI behavior.
- **Risks:** Suuret raportit ja pitkät lokalisoidut tekstit.
- **Decision required:** Ei.

### 26. Implement History lifecycle, deletion, and entry actions

- **Objective:** Korvata History-placeholder pysyvällä listalla.
- **Current behavior:** Ei listaa, tallennettuja raportteja tai Home-entryä.
- **Required final behavior:** Newest-first list, date/time, valid score, coverage, warning/fail summary, full/partial/category label sekä open/compare/export/delete.
- **Dependencies:** Kohdat 5 ja 24–25.
- **Likely areas:** History route/ViewModel/screen, Home navigation.
- **Boundaries and invariants:** Delete vaatii vahvistuksen; loading, empty, error ja success ovat eksplisiittisiä.
- **Acceptance criteria:** Lista reagoi insert/delete-operaatioihin; virhe ei kadota jo renderöityä dataa; Home tarjoaa selkeän historian sisäänkäynnin.
- **Tests/verification:** Repository flow -unit-testit ja Compose list/empty/error/delete-confirmation-testit.
- **Documentation:** `PROJECT.md`, history behavior.
- **Risks:** Aikaleiman locale/timezone-esitys ja suuret listat.
- **Decision required:** Ei.

### 27. Implement version-aware report comparison

- **Objective:** Verrata kahta immutable reporttia ilman perusteettomia terveysväitteitä.
- **Current behavior:** Ei vertailua.
- **Required final behavior:** Report/app/score-versiot, yhteensopiva score/coverage-delta, category/check-statusmuutokset, newly available/unavailable, not-run sekä ilmestyneet/poistuneet warning/fail-tilat.
- **Dependencies:** Kohdat 2, 5 ja 26.
- **Likely areas:** Pure comparison engine, comparison route/ViewModel/screen.
- **Boundaries and invariants:** Stable IDs yhdistävät evidencen. Eri score-versioiden numerodelta estetään. Mittausarvon muutos ei tarkoita fyysistä kulumista.
- **Acceptance criteria:** Järjestys on canonical; missing check ja NotAvailable erotetaan; incompatible-versio näkyy selityksenä.
- **Tests/verification:** Pure diff-testit added/removed/changed/same/incompatible-version-tiloille ja Compose comparison -testit.
- **Documentation:** `PROJECT.md`, comparison semantics/disclaimer.
- **Risks:** Kategoriakohtainen evidence voi muuttua schema-versioiden välillä.
- **Decision required:** Ei.

### 28. Implement local PDF generation and secure sharing

- **Objective:** Tuottaa viimeistelty, tietosuojattu ihmisluettava raportti.
- **Current behavior:** Ei exportia, FileProvideria tai share flowta.
- **Required final behavior:** Monisivuinen PDF, branding/app/device/date/duration/score/coverage/counts/categories/evidence/disclaimer/report ID/versions; export-cache ja Sharesheet.
- **Dependencies:** Kohdat 24–25 ja localization-renderer.
- **Likely areas:** PDF renderer, manifest/FileProvider XML, report actions.
- **Boundaries and invariants:** Ei arkaluonteisia raaka-arvoja. Provider on `exported=false`, path rajataan vain export-alihakemistoon ja URI saa vain väliaikaisen read grantin. Androidin `PdfDocument` vaatii sivujen järjestelmällisen finish/write/close-elinkaaren [virallisen API:n mukaan](https://developer.android.com/reference/android/graphics/pdf/PdfDocument), ja turvallinen jakaminen tehdään `content://`-URIlla [FileProviderin kautta](https://developer.android.com/reference/androidx/core/content/FileProvider).
- **Acceptance criteria:** PDF avautuu vähintään kahdessa vastaanottavassa sovelluksessa; cache-retention toimii; provider ei paljasta muita tiedostoja.
- **Tests/verification:** Renderer pagination/golden-tarkistus, FileProvider path/grant instrumentaatiotesti, FI/EN pitkä sisältö, cache cleanup ja R8-smoke.
- **Documentation:** `PROJECT.md`, export/privacy behavior.
- **Risks:** Fonttien upotus, sivutus, receiving app -yhteensopivuus.
- **Decision required:** Ei PDF:lle; retention policy vahvistetaan kohdassa 1.

### 29. Implement the approved machine-readable export

- **Objective:** Tarjota vakaa automatisoitava vienti ilman uutta tietomallia.
- **Current behavior:** Ei koneluettavaa exportia.
- **Required final behavior:** Hyväksytyn oletuksen mukaan versionoitu JSON, jossa stable IDs, statuses, confidence, evidence source, typed values, timestamps, report schema ja score version.
- **Dependencies:** Kohdat 2, 24 ja 28.
- **Likely areas:** Report serializer, export service, share UI.
- **Boundaries and invariants:** Sama locale-neutral domain payload, ei lokalisoituja näyttötekstejä eikä sensitiivistä dataa; ei CSV:tä ilman erillistä schema-päätöstä.
- **Acceptance criteria:** Export voidaan lukea takaisin yhteensopivaksi domain-raportiksi; deterministic key/value semantics dokumentoidaan.
- **Tests/verification:** Round-trip, old-version fixture, unknown field, privacy field absence ja MIME/share-testit.
- **Documentation:** JSON schema/example ja `PROJECT.md`.
- **Risks:** Formaatista tulee de facto julkinen sopimus, joten versionointi on pakollinen.
- **Decision required:** Kyllä, machine export kohdassa 1.

### 30. Implement functional Settings and persistent preferences

- **Objective:** Korvata Settings-placeholder tuotteen hallintanäkymällä.
- **Current behavior:** Ei settings-UI:ta tai preference persistenceä.
- **Required final behavior:** System/Light/Dark, testivaroitukset, permission status/recovery, report count/delete-all, local-only privacy, privacy policy, app/support info, licenses, feedback, disclaimer ja reopen onboarding.
- **Dependencies:** Kohdat 7, 24, 26 ja valitut päätökset.
- **Likely areas:** Settings route/ViewModel/screen, DataStore preferences, theme integration.
- **Boundaries and invariants:** Settingsin avaaminen ei pyydä lupia. Report content pysyy Roomissa; pienet preferences DataStoressa. Delete all vaatii vahvistuksen.
- **Acceptance criteria:** Teema säilyy restartissa; permission states päivittyvät palattaessa Settingsiin; delete-all ja reopen onboarding toimivat.
- **Tests/verification:** DataStore repository -testit ja Compose Settings -testit theme/permission/delete/link/error-poluille.
- **Documentation:** `PROJECT.md`, privacy/about data ja lisenssilähteet.
- **Risks:** Privacy URL, palautekanava ja lopullinen app metadata ovat ulkoisia release-inputteja.
- **Decision required:** Kyllä, kieli/retention/optional toggles/export-all kohdassa 1.

### 31. Complete the navigation shell and responsive design system

- **Objective:** Vakauttaa koko sovelluksen yhteinen navigointi ja layout ennen lopullista feature-polishia.
- **Current behavior:** Pysyvä top bar näyttää aina app-nimen; Home on kiinteä 2-column/190dp-grid; fullscreen-testit jäävät shellin sisään.
- **Required final behavior:** Route-aware titles/actions, Home-entryt Historyyn ja Settingsiin, hallittu fullscreen-policy, canonical tokens, compact/medium/expanded-layoutit ja system bar -kontrastit.
- **Dependencies:** Pääreitit kohdat 17–30.
- **Likely areas:** MainActivity/NavHost, Home, theme/shared layout components.
- **Boundaries and invariants:** Ei uutta visuaalista kieltä jokaiselle ruudulle; yksi token- ja category catalog -lähde.
- **Acceptance criteria:** Kaikki reitit ovat saavutettavia ja back-stack johdonmukainen; phone/tablet/landscape eivät leikkaa sisältöä.
- **Tests/verification:** Navigation- ja screenshot/layout-testit eri kokoluokille, teemoille ja font scaleille.
- **Documentation:** `PROJECT.md` navigation/design system.
- **Risks:** Laaja samanaikainen redesign voisi vaikeuttaa behavior-regressioiden paikantamista; työ tehdään vain shell-tasolla.
- **Decision required:** Ei.

### 32. Complete shared loading, empty, unavailable, denied, and error presentation

- **Objective:** Varmistaa, ettei mikään feature jää hiljaiseen tai epäselvään tilaan.
- **Current behavior:** Useat ViewModelit keräävät synkronisesti; kaikilla screen-stateilla ei ole error-kenttää tai yhtenäistä palautusta.
- **Required final behavior:** Jaetut mutta kevyet state patterns sekä kategoria- ja raporttikohtaiset loading/empty/unavailable/not-tested/permission-denied/error/retry-näkymät.
- **Dependencies:** Kohdat 8–31.
- **Likely areas:** Shared components, screen state classes, per-feature UI.
- **Boundaries and invariants:** `NotAvailable`, `NotTested`, `Warning` ja `Fail` säilyvät semanttisesti eri tiloina ja väreinä; error ei ole automaattisesti hardware Fail.
- **Acceptance criteria:** Jokaisella async- tai permission-riippuvaisella näkymällä on renderöity ja testattu state matrix.
- **Tests/verification:** Per-screen reducer/unit-testit ja Compose UI state tests.
- **Documentation:** `PROJECT.md` state convention, tarvittaessa `CODE_REVIEW.md`-kohdan sulkeminen.
- **Risks:** Yksi liian geneerinen komponentti voi hävittää featurekohtaisen palautustoiminnon.
- **Decision required:** Ei.

### 33. Complete English/Finnish localization and locale-safe formatting

- **Objective:** Poistaa domainiin ja UI:hin jääneet kovakoodatut tekstit ja arvopäättelyt.
- **Current behavior:** Resurssit kattavat paljon, mutta category-stateissa on englanninkielisiä arvoja ja raporttimalli tallentaa lokalisoituja tekstejä.
- **Required final behavior:** Kaikki UI-, status-, reason-, permission-, onboarding-, PDF- ja disclaimer-tekstit EN/FI-resursseissa; numerot, yksiköt, päivämäärät ja plurals locale-safe.
- **Dependencies:** Kohdat 2 ja 8–32.
- **Likely areas:** `values`/`values-fi`, evidence renderer, PDF formatter.
- **Boundaries and invariants:** Stable IDs/codes eivät lokalisoidu. Käännös ei muuta score- tai statuslogiikkaa.
- **Acceptance criteria:** Molemmat localet kattavat kaikki ruudut ja exportit ilman näkyviä puuttuvia avaimia tai katkenneita pitkiä tekstejä.
- **Tests/verification:** Resource parity, formatter-unit-testit, EN/FI Compose/PDF snapshots ja pseudo-long-text-manuaalitarkistus.
- **Documentation:** `PROJECT.md` localization coverage.
- **Risks:** Tekniset termit, statusreasonit ja PDF-sivutus voivat vaatia eri pituiset muodot.
- **Decision required:** Kyllä, system/per-app locale -linja kohdassa 1.

### 34. Complete accessibility and reduced-motion behavior

- **Objective:** Tehdä koko tuote käytettäväksi TalkBackilla, näppäin-/kytkinohjauksella ja suurilla fonteilla.
- **Current behavior:** Eksplisiittisiä semantics-, heading-, stateDescription- tai liveRegion-määrityksiä on vähän; touch-grid ja fullscreen-exit ovat puutteellisia.
- **Required final behavior:** Semanttiset otsikot/ryhmät/statuskuvaukset, 48dp hit targets, kontrastit, traversal order, live updates, accessible manual test actions, system reduced-motion -kunnioitus.
- **Dependencies:** Kohdat 11, 25–33.
- **Likely areas:** Shared components, interactive diagnostic screens, navigation/onboarding.
- **Boundaries and invariants:** Väri ei ole ainoa statusindikaattori. Dynaaminen mittaus ei tulvi screen readeria.
- **Acceptance criteria:** Kaikki päävirrat voidaan suorittaa TalkBackilla; font scale 200 % ei piilota välttämättömiä toimintoja; fullscreenistä pääsee pois.
- **Tests/verification:** Compose semantics -testit ja fyysinen TalkBack/Switch Access/large-font/light-dark-kontrastikierros.
- **Documentation:** `PROJECT.md` accessibility contract ja QA-matriisi.
- **Risks:** Nopeat sensor/audio/thermal-päivitykset tarvitsevat harkitun announcement-throttlingin.
- **Decision required:** Ei.

### 35. Implement first-run onboarding after the primary flows stabilize

- **Objective:** Selittää tuote, yksityisyys ja rajoitteet ilman permission wallia.
- **Current behavior:** Onboarding puuttuu.
- **Required final behavior:** Welcome, testing model, privacy, permission education, immutable reports ja start; skip, persisted completion ja reopen Settingsistä.
- **Dependencies:** Kohdat 22–34.
- **Likely areas:** Onboarding routes/screen, DataStore, launch routing.
- **Boundaries and invariants:** Ei vaarallisten lupien passiivista pyytämistä; ei vanhenevia screenshot-lupauksia; skip ei estä sovelluksen käyttöä.
- **Acceptance criteria:** Ensimmäinen käynnistys, skip, complete, restart ja reopen toimivat; EN/FI, TalkBack, landscape ja teemat kattavat koko flow’n.
- **Tests/verification:** Navigation/DataStore/Compose UI -testit kaikille entry/exit-poluille.
- **Documentation:** `PROJECT.md`, onboarding copy/limitations.
- **Risks:** Copy voi luvata enemmän kuin lopulliset diagnostiset rajat; tekstit johdetaan hyväksytystä target-statesta.
- **Decision required:** Ei kohdan 1 jälkeen.

### 36. Complete cross-feature automated and instrumented verification

- **Objective:** Todistaa integraatiot, joita yksittäisten kohtien testit eivät kata.
- **Current behavior:** Testilähteitä ei ole.
- **Required final behavior:** End-to-end testit catalog/navigation-, report persistence-, Room-, permissions-, Full Check-, History-, Comparison-, Export-, Settings- ja Onboarding-virroille.
- **Dependencies:** Kohdat 2–35.
- **Likely areas:** `src/test`, `src/androidTest`, test fixtures.
- **Boundaries and invariants:** Ei “add all tests” -massaa; testit kohdistuvat julkaisuriskeihin ja hyväksyntäkriteereihin. Jokainen Room-skeemamuutos saa migration-testin.
- **Acceptance criteria:** Unit, DAO/Room, integration, Compose UI, permission ja lifecycle-suitet kattavat määritellyt riskimatriisit ilman flaky time delay -riippuvuutta.
- **Tests/verification:** Käyttäjä ajaa sovitut Gradle-testit; Codex tarkistaa raportit eikä merkitse kohtaa valmiiksi ilman tuloksia.
- **Documentation:** `PROJECT.md` test inventory ja suunnitelman verification log.
- **Risks:** Hardware-API:a ei voi todistaa pelkillä fakeilla; fyysinen matriisi on erillinen kohta.
- **Decision required:** Ei.

### 37. Execute hardware, OEM, API-level, and lifecycle QA

- **Objective:** Varmistaa julkisiin Android API:eihin perustuvan käytöksen oikeellisuus todellisilla laitteilla.
- **Current behavior:** Ei dokumentoitua laitematriisia tai todennettua tulosta.
- **Required final behavior:** API 26-, 29-, 31-, 33-, 34- ja 36-rajojen emulaattori/laitetestit sekä fyysiset OEM-laitteet, joissa vaihtelevat telephony-, camera-, sensor-, NFC-, biometric- ja storage-ominaisuudet.
- **Dependencies:** Kohta 36.
- **Likely areas:** QA checklist ja plan verification log; tuotantokoodi vain erikseen valituissa korjauskohdissa.
- **Boundaries and invariants:** Yhden OEM:n tulosta ei yleistetä. Löydetty korjaus käsitellään omana valittuna plan iteminä eikä opportunistisena laajana muutoksena.
- **Acceptance criteria:** Jokainen 14 kategoriaa, standalone permissions, Full Check cancel/rotation/background, report restart, export/share ja accessibility on ajettu sovitulla matriisilla.
- **Tests/verification:** Vähintään no-telephony/tablet, single/dual-SIM, multi-camera, no/partial-sensor, BLE, NFC, biometrics, wired/Bluetooth audio, low storage ja unsupported Thermal -tapaukset.
- **Documentation:** Laitemalli/API/build/result/issue kirjataan QA-matriisiin ilman sensitiivisiä tunnisteita.
- **Risks:** Kaikkea laitteistoa ei välttämättä ole saatavilla; puuttuva kattavuus raportoidaan avoimesti.
- **Decision required:** Mahdollisesti laitteiden saatavuudesta, ei tuotteen semantiikasta.

### 38. Prepare release configuration, R8, dependencies, and security

- **Objective:** Todistaa minifioidun release-buildin tekninen eheys.
- **Current behavior:** Minify/shrink on käytössä, mutta custom R8-säännöt ovat käytännössä tyhjät eikä release-buildiä ole tässä työssä todistettu.
- **Required final behavior:** Vain tarvittavat keep rules, resource shrinking, dependency/license-audit, manifest/provider/exported-tarkistus, no-secrets-tarkistus ja release smoke.
- **Dependencies:** Kohdat 28–37.
- **Likely areas:** Release Gradle/R8 config, manifest, security tooling.
- **Boundaries and invariants:** Ei tarpeettomia blanket keep -sääntöjä. Ei secrets-, signing key- tai local config -tiedostoja versionhallintaan. Codex ei aja Gradlea.
- **Acceptance criteria:** Käyttäjän ajama minified release build, asennus ja smoke-testit läpäisevät Room/Hilt/serialization/CameraX/Biometric/PDF/FileProvider-polut.
- **Tests/verification:** Käyttäjä ajaa release-, lint- ja sovitut turvallisuustarkistukset AGENTS-rajojen mukaan; tulokset kirjataan.
- **Documentation:** `PROJECT.md`, release verification log ja relevantit PRE-RELEASE-reviewkohdat.
- **Risks:** Reflektio/serialization/provider-resurssit voivat rikkoutua vain minifioidussa buildissä.
- **Decision required:** Ei, ellei dependency/security-tarkistus vaadi scope-muutosta.

### 39. Complete signing, privacy, Play Store, and legal readiness

- **Objective:** Valmistaa julkaistava AAB ja kaikki ulkoiset julkaisuvelvoitteet.
- **Current behavior:** Ei signing-konfiguraatiota, privacy policy -kohdetta, store metadataa tai lisenssinäkymää.
- **Required final behavior:** Vahvistettu app ID/nimi/versioning, Play App Signing/upload key -prosessi, privacy policy, Data safety, permissions justification, content rating, store listing/assets, lisenssit, support contact ja closed-test-track-valmius.
- **Dependencies:** Kohta 38.
- **Likely areas:** Release metadata, privacy/licenses UI, Play Console -checklist.
- **Boundaries and invariants:** Signing-materiaali ja tunnukset eivät mene repositoryyn tai keskustelutulosteeseen. Play-väitteet vastaavat tarkasti toteutusta.
- **Acceptance criteria:** Kaikki pakolliset Play-lomakkeet ja linkit ovat valmiit; AAB on allekirjoitettu omistajan hallitsemalla avaimella; privacy disclosures vastaavat manifestia, persistenceä ja exportia.
- **Tests/verification:** Play pre-launch report, install/update/backup/data-delete/share-permission-smoke ja store-text fact check.
- **Documentation:** `PROJECT.md`, privacy policy, release checklist ja lisenssiluettelo.
- **Risks:** Ulkoiset URL:t, yhteystiedot, signing ja Play Console -oikeudet vaativat käyttäjän hallitseman inputin.
- **Decision required:** Kyllä, release inputs kohdassa 1.

### 40. Run final release-candidate QA and record the go/no-go decision

- **Objective:** Vahvistaa koko final-product-scope ennen ensimmäistä julkista julkaisua.
- **Current behavior:** Ei release candidatea tai hyväksyttyä kokonaisraporttia.
- **Required final behavior:** Yksi versionumerolla ja commitilla yksilöity RC, jonka kaikki feature-, permission-, persistence-, accessibility-, localization-, security-, hardware- ja store-portit on tarkistettu.
- **Dependencies:** Kohdat 1–39.
- **Likely areas:** QA/release checklist ja suunnitelman status; vain erikseen valitut korjauskohdat muuttavat koodia.
- **Boundaries and invariants:** Tunnettua release-blockeria ei hyväksytä hiljaisesti. Ei PR:n luontia. Gradle ja raskaat checkit käyttäjän terminaalissa.
- **Acceptance criteria:** Kaikki master plan -kohdat ovat hyväksyttyjä; ei avointa blocker/high-severity-löydöstä; clean-install ja upgrade-policy, Full Check, immutable reports, History, Comparison, exports, Settings ja Onboarding on todistettu.
- **Tests/verification:** Täysi mutta ennalta määritelty RC-matriisi; tulokset, laitteet ja merkittävät rajoitteet kirjataan.
- **Documentation:** Lopullinen `PROJECT.md`, implementation plan completion log, release notes ja go/no-go-päätös.
- **Risks:** Viime hetken korjaukset voivat mitätöidä aiemman RC-testauksen; uusi build vaatii kohdistetun regressiokierroksen.
- **Decision required:** Kyllä, lopullinen julkaisu on käyttäjän päätös.

## 5. Recommended first implementation item

**Recommended first implementation item: Item 2 — Define the canonical diagnostic, evidence, score, and coverage contract.**

Se on ensimmäinen varsinainen kooditoteutus sen jälkeen, kun Item 1:n tuotepäätökset on kirjattu. Room-skeema, immutable reports, Full Check, History, Comparison ja export riippuvat kaikki tästä sopimuksesta; niiden aloittaminen ennen sitä aiheuttaisi todennäköisesti uudelleentyötä ja pitkäikäisten raporttien yhteensopivuusriskejä.
