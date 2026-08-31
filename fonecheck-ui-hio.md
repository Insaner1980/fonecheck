# fonecheckin UI-erottimien ja tietohierarkian toteutussuunnitelma

> **Toteuttajalle:** noudata suunnitelmaa tehtävä kerrallaan. Tarkista jokaisen kosketettavan tiedoston nykyinen diff ennen muokkaamista, koska työpuussa on käyttäjän keskeneräisiä muutoksia.

**Tavoite:** Poistaa kaikki koristeelliset piste-erottimet fonecheckin tuotantolähteistä, selkeyttää erityisesti Storage- ja Camera-näkymät, poistaa Device-näkymän baseband-toisto ja yhtenäistää raporttiaikojen esitys.

**Arkkitehtuuri:** Käytä nykyisiä `SectionHeader`-, `DataRow`-, `LongValueRow`, `HeadlineReadout`-, `PrimaryButton`- ja `SecondaryButton`-komponentteja. Erota tiedot rakenteella eikä korvaavalla välimerkillä. Diagnostiikkadata, ViewModel-tilat, navigointi, tallennus ja testien todellinen toiminta säilyvät ennallaan.

**Tekniikka:** Kotlin 2.4.10, Jetpack Compose, Material 3, Android string resources, JUnit ja Compose UI -testit.

**Lähdeaineisto:** 51 kuvakaappausta kansiosta `C:\Users\EmmaH\Downloads\fonecheck-screenshots3`, nykyinen dirty worktree ja edeltävä käyttöliittymäarvio.

## Globaalit rajoitteet

- Älä lisää kortteja, varjoja, liukuvärejä, uusia värejä, riippuvuuksia tai yhtä näkymää varten tehtyjä jaettuja komponentteja.
- Älä lisää Camera-näkymään suurta headline-lukua.
- Älä muuta mittausten, raporttien, ViewModelien, tietokannan tai domain-mallien merkitystä.
- Säilytä kaikki käyttäjän nykyiset muutokset; älä palauta tai korvaa kokonaisia tiedostoja.
- Kaikki uudet ja muutetut merkkijonot tehdään samassa muutoksessa englanniksi ja suomeksi.
- Käyttäjälle näkyvässä fonecheck-sisällössä eivät saa esiintyä merkit `U+00B7`, `U+2022`, `U+2219`, `U+22C5` tai `U+2027`.
- Tavalliset lauseiden pisteet, desimaalierottimet ja teknisten tunnisteiden pisteet säilyvät.
- Androidin tilapalkin ilmoituspiste ei kuulu sovelluksen hallintaan eikä tämän työn piiriin.
- Codex ei aja Gradlea. Toteutuksessa tehdään staattiset tarkistukset ja käyttäjälle annetaan lopuksi todelliset manuaaliset Gradle-komennot.
- Ei commitia, pushia tai PR:ää ilman erillistä pyyntöä.

---

## Lukittu käyttöliittymäsopimus

### Piste-erottimien korvaussääntö

| Nykyinen käyttötapa | Lopullinen ratkaisu |
|---|---|
| Live data ja päivitysaika | `LongValueRow`: label `Live data`, value `Updated <timestamp>`, ei divideria |
| Storage used/free/total | Kolme erillistä `DataRow`-riviä |
| Kameran toiminto ja metadata | Toimintopainikkeessa vain toiminto; ID ja kameratyyppi capability-osioon |
| Mittaus ja sitä selittävä yksikkö | Sulkeet, esimerkiksi `4080 × 3072 (12.5 MP)` |
| Otsikko ja konteksti | `SectionHeader(label, trailing)` |
| Raportin rinnakkaiset luvut | Erilliset `DataRow`-rivit |
| Touch-testin visuaalinen tila | Kolme `DataRow`-riviä; saavutettavuustekstissä täydet lauseet |
| PDF:n score ja state | Kaksi erillistä `PdfTextBlock`-lohkoa |
| GPS-mittauksen tarkenne | Sulkeissa oleva tarkenne, ei keskipistettä |

### Storage-näkymän lopullinen rakenne

```text
INTERNAL STORAGE

24 % used

Used                         58.93 GB
Available                      187 GB
Total                          246 GB

STORAGE ACCESS

Private app storage        Accessible
                          High confidence
Primary shared storage       Mounted
Removable storage                 No

STORAGE SPEED CHECK

[Run benchmark]
[Skip benchmark]

LIMITATIONS
...
```

Käyttäytymissäännöt:

- `HeadlineReadout.value` on lokalisoitu käyttöprosentti.
- `HeadlineReadout.unit` on `% used` tai `% käytetty`.
- `supportingLines` on tyhjä; kapasiteetit esitetään DataRow-riveinä.
- Jos `usagePercent == null`, headline jätetään pois ja nykyinen unavailable-käyttäytyminen säilytetään.
- App-private-käyttöoikeus siirretään pois kapasiteettiyhteenvedosta `Storage access` -osioon.
- Ensisijaisen, ei-irrotettavan shared storagen Total- ja Available-arvoja ei toisteta.
- Jos laitteella on lisätaltio tai irrotettava ensisijainen taltio, sille näytetään oma osio, jossa säilyvät State-, Removable-, Total- ja Available-tiedot.
- Benchmarkin laskenta, väliaikaistiedosto, tulokset, virheet, cancel ja skip eivät muutu.

Lukitut tekstit:

| English | Finnish |
|---|---|
| `% used` | `% käytetty` |
| `Storage access` | `Tallennustilan käyttö` |
| `Private app storage` | `Sovelluksen yksityinen tallennustila` |
| `Primary shared storage` | `Ensisijainen jaettu tallennustila` |
| `Removable storage` | `Irrotettava tallennustila` |
| `Storage speed check` | `Tallennustilan nopeustesti` |
| `Limitations` | `Rajoitukset` |

Älä nimeä `storage_volumes_title`-resurssia uudelleen `Storage access` -tekstiksi, koska sitä käytetään myös `storage.volume_count`-evidencen nimenä. Lisää näytölle oma `storage_access_title`.

### Camera-näkymän lopullinen rakenne

```text
PERMISSION
Permission granted

CAMERA TEST

[Open rear camera]
[Open front camera]

Valinnan jälkeen:
[Preview]
[Capture] [Stop]

Captured     4080 × 3072 (12.5 MP)

Did the preview and photo work?
[Problem] [Confirmed]

TORCH TEST

Torch                   Not measured
[Turn on torch]

REAR CAMERA                  Camera 0
Max resolution        4080 × 3072
Zoom range             0.5× – 8.0×
OIS                            Yes
Flash                          Yes
Focal lengths              6.90 mm
[Show technical details]

FRONT CAMERA                 Camera 1
...
```

Käyttäytymissäännöt:

- Inaktiiviset valinnat ovat `Open rear camera`, `Open front camera` tai `Open external camera`.
- Valitun kameran näkyvä teksti on `Rear camera`, `Front camera` tai `External camera`; nykyinen `selected`-semantiikka säilyy.
- Jos samalla facing-arvolla on useita julkisia kameroita, lisää vain silloin painikkeen loppuun `(camera <id>)`, jotta valinnat ovat yksikäsitteisiä.
- `logical multi-camera`, standard/physical/external-luokka ja fyysiset kamera-ID:t siirretään `Show technical details` -sisältöön.
- Capability-osion otsikko on facing-nimi ja `SectionHeader.trailing` näyttää `Camera <id>`.
- `Camera 0 capabilities`- ja `Camera 1 capabilities` -otsikot poistuvat.
- `Flash / torch` korvataan otsikolla `Torch test`; workflow käyttää sanaa `Torch`, capability-rivi säilyttää sanan `Flash`.
- Kamera- ja torch-toiminnallisuus, käyttöoikeuspyyntö, preview, capture, vahvistukset ja resurssien vapautus eivät muutu.
- `NEXT TOUCH` -kohdista tarkistetaan samalla vain suoraan tähän tiedostoon liittyvät olemassa olevat loading-, preview-state-, initialization-error- ja resource-release-käyttäytymiset. Niitä ei refaktoroida ilman todettua ongelmaa.

### Device ja aikamuodot

- `splitConcatenatedDeviceIdentifiers` säilyy pienenä esityskerroksen funktiona.
- Kahden pilkulla tai puolipisteellä erotetun arvon osat trimmataan ja deduplikoidaan.
- Kaksi erilaista arvoa näytetään edelleen eri riveillä.
- Kaksi identtistä arvoa näytetään ja kopioidaan kerran.
- Kolmen tai useamman osan tunniste säilyy muuttumattomana nykyisen sopimuksen mukaisesti.
- Raaka `DeviceInfo.basebandVersion` säilytetään muuttumattomana domain-mallissa.
- Sama normalisoitu arvo välitetään riville, long-press-copyyn ja `DeviceSnapshotText`-vientiin.
- Live-mittausaika säilyy teknisessä ISO-muodossa `uuuu-MM-dd HH:mm`.
- Tallennetun raportin completion-aika käyttää Homessa ja Report historyssa samaa `FormatStyle.MEDIUM`-muotoa, käyttöliittymän kieltä ja järjestelmän aikavyöhykettä.
- Report comparison, RunAll Results ja PDF käyttävät jo samaa lokalisoitua medium-periaatetta; niiden käyttäytymistä ei muuteta tässä työssä.

---

## Toteutustehtävät

### 1. Suojaa nykyinen dirty worktree

**Toimet**

- [ ] Tallenna `git status --short` ja rajattu `git diff` kaikista kosketettavista tiedostoista ennen ensimmäistä muokkausta.
- [ ] Merkitse erityisesti käyttäjän nykyiset muutokset `CaptureTimestamp`, Home-, History-, Storage-, Camera-, Device-, Display-, Connectivity-, RunAllResults- ja PDF-tiedostoissa.
- [ ] Tee muutokset pieninä kontekstipatcheina; älä korvaa tiedostoja kokonaan.
- [ ] Tarkista jokaisen osatehtävän jälkeen, ettei diffiin ole tullut tehtävän ulkopuolisia muutoksia.

**Valmis, kun**

- Lähtötilan käyttäjämuutokset voidaan erottaa tämän työn muutoksista.
- Mitään unrelated-tiedostoa ei ole muokattu.

### 2. Yhteinen timestamp- ja raporttiaikamuoto

**Keskeiset tiedostot**

- `ui/components/CaptureTimestamp.kt`
- uusi `ui/format/UiDateTimeFormat.kt`
- Home- ja History-näkymät sekä niiden nykyiset testit
- englannin ja suomen `strings.xml`

**Rajapinnat**

```kotlin
fun formatUiDateTime(
    value: Instant,
    locale: Locale,
    zoneId: ZoneId = ZoneId.systemDefault(),
): String
```

`CaptureTimestamp(capturedAt, modifier)` ja `LiveStateTimestamp(...)` säilyttävät nykyiset julkiset allekirjoituksensa.

**Toimet**

- [ ] Lisää puhdas `formatUiDateTime`, joka käyttää `uiLanguageLocale(locale)`-, `FormatStyle.MEDIUM`- ja annettua aikavyöhykettä.
- [ ] Korvaa Homen kiinteä `homeTimestampFormatter` yhteisellä formatterilla.
- [ ] Korvaa Historyn paikallinen formatter yhteisellä formatterilla.
- [ ] Muuta `CaptureTimestamp` käyttämään `LongValueRow`-rakennetta:
  - label `Live data` / `Reaaliaikaiset tiedot`
  - value `Updated %1$s` / `Päivitetty %1$s`
  - `showDivider = false`
  - nykyinen ylä- ja alaväli säilytetään.
- [ ] Säilytä live-timestampissa nykyinen ISO-formaatti.
- [ ] Säilytä plain-text Device exportissa `Captured %1$s` / `Kerätty %1$s`; se ei käytä live-row-tekstiä.
- [ ] Päivitä dirty `DeviceInfoContentTest` vastaamaan tätä rajaa.

**Testit**

- [ ] Lisää JVM-testit englannin ja suomen raporttiaikamuodolle sekä annetulle aikavyöhykkeelle.
- [ ] Päivitä `HomeFormattingTest`: odota lokalisoitua medium-muotoa kiinteän ISO-muodon sijaan.
- [ ] Päivitä `HomeContentTest` ja `HistoryScreenTest` käyttämään samaa helperiä.
- [ ] Lisää Compose-testi, joka löytää `Live data`- ja `Updated <timestamp>` -tekstit erillisinä nodeina.
- [ ] Varmista, ettei CaptureTimestamp-testissä ole yhdistettyä keskipistetekstiä.

### 3. Storage-hierarkia

**Keskeiset tiedostot**

- `ui/screens/storage/StorageTestScreen.kt`
- englannin ja suomen resurssit
- uusi instrumented `StoragePresentationTest`

**Sisäiset saumat**

```kotlin
@Composable
internal fun StorageOverviewSection(info: StorageInfo)

@Composable
internal fun StorageAccessSection(info: StorageInfo)
```

Lisätaltioiden näyttämisen ehto:

```kotlin
volume.isRemovable || !volume.isPrimary
```

**Toimet**

- [ ] Kirjoita ensin Compose-testit uudelle rakenteelle.
- [ ] Muuta overview näyttämään `HeadlineReadout` ilman supporting lineja.
- [ ] Lisää headlinea seuraavat Used-, Available- ja Total-rivit.
- [ ] Siirrä private-storage access ja confidence `StorageAccessSection`-osioon.
- [ ] Näytä ensisijaisesta shared storagesta vain mount state ja removable state.
- [ ] Näytä lisä- ja removable-taltioiden kapasiteetit omissa osioissaan.
- [ ] Poista käyttämättömiksi jäävät `storage_usage_used_free`- ja `storage_usage_total`-resurssit molemmista localeista.
- [ ] Lisää `storage_usage_unit`, `storage_access_title`, UI-kohtainen private-storage-label ja removable-storage-label.
- [ ] Lyhennä benchmark- ja limitation-otsikot lukitun tekstitaulukon mukaisesti.
- [ ] Säilytä kaikki benchmark-statejen nykyiset painikkeet, virheet, tulosrivit ja limitation-teksti.

**Testit**

- [ ] Normaali 246 GB / 187 GB / 24 % -tila näyttää jokaisen kapasiteetin vain kerran.
- [ ] `% used` on headline-unit eikä irrallinen `%`.
- [ ] App-private access ei ole headline-alueessa.
- [ ] `usagePercent == null` ei piirrä headlinea.
- [ ] Tyhjä volumes-lista näyttää nykyisen unavailable-noten.
- [ ] Ensisijainen ei-irrotettava taltio ei toista kapasiteettia.
- [ ] Lisä- tai removable-taltio säilyttää State-, Removable-, Total- ja Available-arvot.
- [ ] English- ja Finnish-resourceavaimet pysyvät pariteetissa.
- [ ] `HeadlineReadoutTest` käyttää erillisiä supporting lineja eikä piste-erotinta.

### 4. Camera-information architecture

**Keskeiset tiedostot**

- `ui/screens/camera/CameraTestScreen.kt`
- englannin ja suomen resurssit
- uusi instrumented `CameraPresentationTest`

**Sisäiset saumat**

Erota nykyisestä preview-osiosta testattava valintalista, joka kuluttaa kamerat, aktiivisen ID:n ja `onSelect(cameraId)`-callbackin. ViewModel- tai CameraX-rajapintoja ei muuteta.

`CameraSection` saa valinnaisen trailing-arvon:

```kotlin
@Composable
private fun CameraSection(
    title: String,
    trailing: String? = null,
    content: @Composable () -> Unit,
)
```

**Toimet**

- [ ] Lisää staattisilla fake-capabilities-arvoilla epäonnistuvat Compose-testit ennen layout-muutosta.
- [ ] Korvaa `camera_selector_label` facing-kohtaisilla toimintoresursseilla.
- [ ] Säilytä valitun painikkeen primary-tyyli ja `selected`-semantiikka.
- [ ] Lisää saman facing-tyypin duplikaateille ID vain sulkeissa.
- [ ] Muuta capability-otsikoiksi Rear camera, Front camera tai External camera.
- [ ] Näytä `Camera <id>` otsikon trailing-arvona.
- [ ] Lisää laajennettuihin teknisiin tietoihin `Camera type` sekä sorted physical camera IDs, jos niitä on.
- [ ] Muuta captured-result resurssimuotoon `%1$s (%2$s)`.
- [ ] Nimeä Flash/Torch-workflow lukitun tekstin mukaisesti.
- [ ] Säilytä capability-rivin nimi `Flash`, koska se kuvaa laiteominaisuutta.
- [ ] Älä muuta preview-, capture-, confirmation-, flash-control- tai permission-logiikkaa.

**Testit**

- [ ] Kahden kameran perustila näyttää `Open rear camera` ja `Open front camera`.
- [ ] Painikkeissa ei näy kamera-ID:tä tai `logical multi-camera` -tekstiä, kun facing on yksikäsitteinen.
- [ ] Kahden takakameran valinnat saavat toisistaan erottuvat ID-tarkenteet.
- [ ] Painikkeen klikkaus välittää oikean camera ID:n.
- [ ] Valittu kamera säilyttää selected-semanticsin.
- [ ] Capability-otsikko on `Rear camera`, trailing on `Camera 0`.
- [ ] `Camera type` näkyy vasta teknisten tietojen avaamisen jälkeen.
- [ ] Capture-tulos näkyy muodossa `4080 × 3072 (12.5 MP)`.
- [ ] Torch- ja Flash-termit esiintyvät vain sovituissa rooleissa.
- [ ] Loading-, no-camera-, permission-denied- ja retry-tilat säilyvät.

### 5. Device-basebandin normalisointi

**Keskeiset tiedostot**

- `ui/screens/deviceinfo/DeviceInfoScreen.kt`
- `DeviceSnapshotText.kt`
- nykyiset Device unit- ja Compose-testit

**Toimet**

- [ ] Lisää testit identtiselle comma-parille ja identtiselle semicolon-parille.
- [ ] Muuta kahden osan käsittely muotoon trim, `distinct`, newline join.
- [ ] Laske kernel-, baseband- ja bootloader-riveille kerran normalisoitu näyttöarvo.
- [ ] Käytä samaa arvoa `LongValueRow`-sisällössä ja long-press-copyssa.
- [ ] Säilytä raw-arvo `DeviceInfo`-mallissa.
- [ ] Anna `DeviceSnapshotText`-viennin jatkaa saman helperin käyttöä.

**Testit**

- [ ] `"radio-one, radio-one"` tuottaa `"radio-one"`.
- [ ] `"radio-one; radio-one"` tuottaa `"radio-one"`.
- [ ] `"radio-one, radio-two"` tuottaa kaksi riviä.
- [ ] `"one,two,three"` säilyy muuttumattomana.
- [ ] Null ja unavailable säilyvät nykyisinä.
- [ ] Compose-puu sisältää identtisen basebandin kerran.
- [ ] Long-press copy palauttaa normalisoidun arvon.
- [ ] Plain-text snapshot sisältää identtisen basebandin kerran.

### 6. Loput piste-erottimet

#### Connectivity GPS

- [ ] Muuta label muotoon `Satellite <id> (<constellation>)`.
- [ ] Muuta value muotoon `<signal> dB-Hz (used in fix)` tai `(not used in fix)`.
- [ ] Lisää vastaavat suomalaiset tilatekstit.
- [ ] Säilytä yksi `LongValueRow` per satelliitti ja nykyinen näkyvien satelliittien raja.
- [ ] Lisää testit käytössä ja ei käytössä oleville satelliiteille.

#### RunAll Results

- [ ] Korvaa yhdistetty coverage-note kahdella neutraalilla DataRow-rivillä:
  - `Coverage` / `<percentage>%`
  - `Checks` / `<completed>/<applicable>`
- [ ] Säilytä score, SegmentedBar ja kaikki statuslaskurit ennallaan.
- [ ] Päivitä `RunAllResultsScreenTest` vahvistamaan molemmat rivit.

#### Report history

- [ ] Muuta category-only-header muotoon `SectionHeader(label = Category retest, trailing = Storage)`.
- [ ] Full Check- ja unavailable-headerit säilyvät ilman trailing-kategoriaa.
- [ ] Korvaa warnings/issues-note kahdella neutraalilla DataRow-rivillä.
- [ ] Säilytä Open-, Compare-, Export- ja Delete-toiminnot.
- [ ] Päivitä `HistoryScreenTest` tarkistamaan headerin label ja trailing erillisinä sekä Warnings/Issues-rivit.

#### Display touch overlay

- [ ] Korvaa visuaalinen yhdistelmäteksti kolmella DataRow-rivillä:
  - Grid coverage
  - Active touches
  - Maximum touches
- [ ] Säilytä Canvasin `stateDescription`, mutta muodosta se täydellisiksi lokalisoiduiksi lauseiksi ilman koristeellista erotinta.
- [ ] Päivitä `DisplayInteractionTest` tarkistamaan sekä kolme näkyvää arvoa että state description.

#### PDF

- [ ] Jaa Score ja Score state kahdeksi `PdfTextBlock`-lohkoksi.
- [ ] Score säilyy `HEADING`-tyylisenä; score state on `BODY`.
- [ ] Säilytä lohkojen järjestys ennen Coverage-lohkoa.
- [ ] Päivitä `ReportPdfContentTest` tarkistamaan molemmat erilliset rivit ja niiden järjestys.

### 7. Globaali regressionesto

**Uusi testi**

`app/src/test/java/com/insaner/fonecheck/localization/UiSeparatorPolicyTest.kt`

**Tarkka vastuu**

- Etsi module root samalla tavalla kuin `ResourceParityTest`.
- Skannaa `src/main`-puun `.kt`- ja `.xml`-tiedostot.
- Vertaa Unicode-koodipisteisiin, älä sisällytä kiellettyjä merkkejä testin omaan lähteeseen literaaleina.
- Epäonnistu viestillä, joka listaa tiedoston, rivin ja koodipisteen.
- Älä kiellä tavallista pistettä, pilkkua, sulkeita, en dashia tai em dashia.

**Testit ja staattinen hyväksyntä**

- [ ] Policy-testin pitää löytää nykyiset osumat ennen tuotantomuutoksia.
- [ ] Lopullisessa lähteessä policy-testin odotus on nolla osumaa.
- [ ] Aja staattisesti:

```powershell
rg -n "[·•∙⋅‧]" app/src/main
git diff --check
```

Odotus:

- `rg` ei tulosta mitään.
- `git diff --check` päättyy onnistuneesti.
- English- ja Finnish-string-avaimet ovat identtiset.
- Uusia hardcoded user-facing stringejä ei ole.
- `git status --short` ei sisällä odottamattomia tiedostoja.

---

## Manuaalinen testaus

Codex ei suorita näitä. Käyttäjä ajaa ne omassa terminaalissaan yksi kerrallaan:

```powershell
.\gradlew :app:testDebugUnitTest
```

```powershell
.\gradlew :app:connectedDebugAndroidTest
```

Tarvittaessa lopullinen debug-paketointi:

```powershell
.\gradlew :app:assembleDebug
```

### Fyysisen laitteen hyväksymisskenaariot

- [ ] Device näyttää basebandin kerran sekä vaaleassa että tummassa teemassa.
- [ ] Basebandin long-press copy ei sisällä identtistä toistoa.
- [ ] Storage-yläosa vastaa lukittua rakennetta eikä toista 246 GB / 187 GB -arvoja.
- [ ] Storage toimii myös ilman usage-percent-arvoa ja ilman shared volumea.
- [ ] Camera-valinnat ovat toimintolähtöisiä ja mahtuvat yhdelle tai hallitulle kahdelle riville.
- [ ] Rear/Front-yhteys capability-osioihin on ymmärrettävä ilman aiemman ID:n muistamista.
- [ ] Preview, capture, stop, confirmation ja torch toimivat kuten ennen.
- [ ] GPS-expanded state ei sisällä piste-erottimia.
- [ ] Display touch overlay näyttää kolme erillistä mittaria.
- [ ] Full Check Results ja Report history näyttävät luvut erillisinä riveinä.
- [ ] Luotu PDF ei sisällä koristeellisia piste-erottimia.
- [ ] Home ja Report history näyttävät saman raportin completion-ajan samalla lokalisoidulla tavalla.
- [ ] Kaikki yllä olevat tarkistetaan englanniksi ja suomeksi.
- [ ] Storage, Camera, History ja CaptureTimestamp tarkistetaan järjestelmän suurella fonttikoolla.
- [ ] Camera-painikkeiden TalkBack-nimet kertovat toiminnon ja selected-tilan.
- [ ] Androidin tilapalkin järjestelmäpistettä ei tulkita fonecheckin regressioksi.

## Julkiset rajapinnat ja yhteensopivuus

- Ei muutoksia domain-malleihin, tietokantaan, raporttischemaan, navigointiin tai ViewModelien julkiseen tilaan.
- `CaptureTimestamp`, `LiveStateTimestamp` ja `HeadlineReadout` säilyttävät allekirjoituksensa.
- Ainoa uusi yleinen tuotantorajapinta on puhdas sisäinen `formatUiDateTime`.
- Camera- ja Storage-testisaumat ovat `internal`, eivät uusia julkisia design-system-komponentteja.
- PDF:n sisältö muuttuu vain kahden yhdistetyn tekstilohkon jakamiseksi.
- Raw device data säilyy muuttumattomana; deduplikointi koskee esitystä, kopiointia ja käyttäjälle näkyvää vientiä.

## Valmis-määritelmä

Työ on valmis vasta, kun:

1. Kaikki 20 nykyistä tuotantolähteen piste-erotinosumaa on poistettu.
2. Storage-, Camera- ja Device-korjaukset vastaavat yllä lukittuja rakenteita.
3. EN/FI-resurssit ovat pariteetissa.
4. Kaikki uudet ja päivitetyt testit ovat kirjoitettu.
5. Staattiset tarkistukset ovat puhtaat.
6. Käyttäjän manuaalisten Gradle-testien tulos on kirjattu erikseen.
7. Laite-, kamera- ja PDF-runtime-tarkistuksia ei väitetä tehdyiksi ennen todellista suoritusta.
8. Käyttäjän aiemmat dirty-worktree-muutokset ovat säilyneet.
