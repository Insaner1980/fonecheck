# PhoneCheck — Phone Diagnostics App

## Package
`com.insaner.phonecheck`

## SDK Targets
- minSdk: 26
- targetSdk: 36
- compileSdk: 36

## Build System
- Kotlin DSL (`build.gradle.kts`)
- Version catalog (`gradle/libs.versions.toml`)
- Kotlin 2.1.0, AGP 8.7.3

## Architecture
- Jetpack Compose + Material 3
- Hilt dependency injection
- Room database (schema-ready)
- Compose Navigation (type-safe, `@Serializable` routes)
- MVVM with ViewModels

## Folder Structure
```
app/src/main/java/com/insaner/phonecheck/
├── PhoneCheckApp.kt              # @HiltAndroidApp Application
├── data/
│   ├── local/                    # Room database, DAOs, entities
│   └── repository/               # Repository implementations
├── di/                           # Hilt modules
├── domain/
│   ├── model/                    # Domain models
│   └── usecase/                  # Use cases
├── navigation/                   # Routes + NavHost
└── ui/
    ├── MainActivity.kt           # @AndroidEntryPoint activity
    ├── screens/                   # Feature screens (composables + viewmodels)
    └── theme/                    # Color, Type, Theme
```

## Theme — Cool Neutral Dark Palette

### Colors
| Token       | Hex       | Usage                      |
|-------------|-----------|----------------------------|
| Neutral950  | #0A0C10   | Background                 |
| Neutral900  | #111318   | Surface                    |
| Neutral850  | #181B22   | Elevated surface           |
| Neutral800  | #1E2128   | Surface variant            |
| Neutral700  | #2A2E37   | Outline variant, container |
| Neutral600  | #3A3F4B   | Outline                    |
| Neutral500  | #555B6A   | Disabled content           |
| Neutral400  | #777E8E   | Secondary text             |
| Neutral300  | #9CA2B0   | On-surface-variant         |
| Neutral200  | #C0C5D0   | On-secondary-container     |
| Neutral100  | #DFE2E8   | On-surface, on-background  |
| Neutral50   | #F0F1F4   | On-primary-container       |
| Blue400     | #6B9FFF   | Primary accent             |
| Blue500     | #4A85F2   | Primary pressed            |
| Blue600     | #3570DB   | Primary container          |
| Green400    | #5FD88E   | Success / pass             |
| Yellow400   | #E8C94A   | Warning / caution          |
| Red400      | #EF6B6B   | Error / fail               |

### Typography
- **Body text**: DM Sans (Regular, Medium, Bold)
- **Numbers/mono**: JetBrains Mono (Regular, Medium, Bold)

## Localization
- Default: English (`values/strings.xml`)
- Finnish: (`values-fi/strings.xml`)

## Build Commands
```bash
./gradlew assembleDebug       # Debug build
./gradlew assembleRelease     # Release build
./gradlew test                # Unit tests
```
