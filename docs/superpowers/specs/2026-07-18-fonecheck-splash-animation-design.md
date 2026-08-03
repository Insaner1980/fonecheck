# fonecheck Splash -animaation design

## Tavoite

fonecheck näyttää käynnistyessä logon ja `fonecheck`-sanatunnuksen näyttävänä Cinematic Assembly -animaationa. Tunnus käyttää sovelluksen DM Sans Medium -fonttia, ja alkuperäiset ilmaraot turkoosin nauhan sekä violetin kehyksen välillä säilyvät.

## Liike ja ajoitus

- Tausta on heti näkyvä `#02021A`.
- 80–650 ms: violetin kehyksen puolikkaat saapuvat kaukaa ylhäältä ja alhaalta, kiertyvät vastakkaisista kulmista ja asettuvat kevyen ylityksen kautta.
- 260–920 ms: turkoosi nauha pyyhkäisee vasemmalta alhaalta oman diagonaalinsa suuntaisesti ja asettuu kehikon ilmaraot säilyttäen.
- 720–1250 ms: `fonecheck` muodostuu logon alle ryhmissä `fo`, `nec` ja `heck`. Kaikki glyyfit on ladottu yhtenäisen sanan mitoituksella, joten valmis sanatunnus on saumaton.
- 1080–1400 ms: turkoosin nauhan päällä kulkee lyhyt vaalea välähdys.
- 1150–1400 ms: valmis logo ja sanatunnus tekevät kolmen prosentin loppupulssin.
- API 31+: splash pysyy näkyvissä vähintään 1500 ms ja poistuu 180 ms häivytyksellä sekä pienennyksellä 92 prosenttiin.

## Fontti

- Sovelluksen DM Sans -resurssit ovat Google Fontsin virallisesta muuttuvasta fontista tuotetut staattiset leikkaukset 400, 500 ja 700.
- Splashin `fonecheck` on muunnettu DM Sans Medium 500 -leikkauksesta vektoripoluiksi, koska VectorDrawable ei voi piirtää fonttitekstiä suoraan.
- Sana on kokonaan pienillä kirjaimilla ilman ylimääräistä kirjainvälistystä.

## Alustakäyttäytyminen

- API 31+: järjestelmän splash käyttää 1400 ms AnimatedVectorDrawable-animaatiota.
- API 26–30: SplashScreen Compat näyttää saman logon ja sanatunnuksen valmiina staattisena vektorina ilman API 31+:n animaatiota tai keinotekoista 1500 ms pitoa.
- Launcher-kuvake säilyy ennallaan; sanatunnus kuuluu vain splashiin.

## Rajaukset

- Ei erillistä splash-Activityä.
- Ei looppaavaa animaatiota.
- Ei muutoksia navigaatioon, Home-näyttöön tai launcher-kuvakkeen ulkoasuun.
