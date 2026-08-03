# fonecheck Cinematic Splash -toteutussuunnitelma

**Tavoite:** Tee logosta ja DM Sans Medium -sanatunnuksesta 1400 ms Cinematic Assembly -animaatio, joka ehtii näkyä kokonaan.

**Toteutus:** AndroidX Core SplashScreen 1.2.0 käyttää API 31+:ssa nimettyihin VectorDrawable-ryhmiin kytkettyä AnimatedVectorDrawablea. Sama lopputilan vektori toimii staattisena fallbackina API 26–30:ssä. `MainActivity` pitää animoidun splashin näkyvissä vähintään 1500 ms ja poistaa sen 180 ms animaatiolla; staattista fallbackia ei viivytetä.

## Valmis työ

- [x] Core SplashScreen -riippuvuus, starting theme ja manifestikytkentä
- [x] Logon erilliset ylä-, ala- ja turkoosit animaatiot ilmaraot säilyttäen
- [x] DM Sans 400/500/700 -fonttiresurssien korvaaminen oikeilla staattisilla fonteilla
- [x] DM Sans Medium `fonecheck` -sanatunnuksen vektorointi ja porrastettu muodostuminen
- [x] Turkoosi valovälähdys ja koko tunnuksen loppupulssi
- [x] 1500 ms vähimmäisnäyttöaika ja 180 ms poistumisanimaatio
- [x] Sama valmis logo ja sanatunnus staattisena API 26–30 -fallbackina

## Varmistus

- XML-rakenteet, AVD-targetit, resurssiviittaukset ja animaatioiden loppuarvot tarkistetaan ilman Gradlea.
- Android-resurssit käännetään suoraan SDK:n `aapt2`-työkalulla.
- Käyttäjä tekee projektin Gradle-buildin ja tarkistaa cold startin API 31+ -laitteella sekä staattisen fallbackin API 26–30 -laitteella tai emulaattorilla.
