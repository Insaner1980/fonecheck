# Home design QA

## Evidence

- Visual source: `C:\Users\EmmaH\Downloads\ChatGPT Image Aug 22, 2026, 08_40_50 PM.png` (724 × 2172)
- Audited implementation captures: `C:\Users\EmmaH\Downloads\fonecheck-screenshots2\Screenshot_20260823-122319.png` through `Screenshot_20260823-122341.png` (1080 × 2424)
- State audited: Home with a saved Full Check, in the captured light and dark themes
- Implementation surface: `app/src/main/java/com/insaner/fonecheck/ui/screens/home/HomeScreen.kt`

## Comparison findings

### P1

- The captured build omitted the source's dark inset readout panel and continuous evidence-coverage bar.
- The captured build used a neutral button instead of the source's orange, black-bordered primary control.
- The captured build used a two-column text ledger instead of the source's three-column, five-row status switchboard with filled status surfaces.
- The captured build showed only exception readings instead of the source's complete fourteen-category reading list.

### P2

- The captured header used a large standalone mark that is absent from the visual source.
- The captured status cells repeated verdict words and did not use the source's compact corner-symbol treatment.
- The captured light background was too white and lacked the source's warm instrument-paper tone.

## Iteration 1 corrections

- Rebuilt the Home hierarchy around the source's instrument header, inset readout, orange primary control, status switchboard, legend, and complete reading list.
- Restored the normal-phone status layout to three columns and five rows. The fifteenth position is an unlabelled spare rather than an invented category.
- Kept the canonical fourteen destinations, saved report statuses, and stable measured headline values.
- Added responsive two-column fallback for constrained width or enlarged text, 48 dp or larger touch targets, and contrast checks for the new fixed instrument surfaces.

## Final result

blocked

A fresh post-change device screenshot is required for a visual pass. Project rules prohibit running Gradle from this session, so the updated Android UI cannot be rendered and compared here. The pre-change captures above must not be used as proof of the corrected result.

## Earlier comparison record (superseded source)

- Source visual truth: `C:\Dev\fonecheck\image2.png`
- Pre-change implementation screenshot: `C:\Dev\fonecheck\image1.png`
- Side-by-side comparison: `C:\Users\EmmaH\.codex\visualizations\2026\08\12\019ff4a6-7c64-7643-89d9-d2c69ee7c4de\home-reference-vs-current.png`
- Source pixels: 1122 × 1402; implementation pixels: 1080 × 2424
- State: dark theme; implementation values came from the real report rather than the source examples

That comparison found a physical-device-like score enclosure, three separate metric mini-cards, a heavy layered border, and proportions that were denser than its then-current source. The source was changed to a simple progress arc, an integrated metric row, a thinner surface edge, and a larger status treatment. DM Sans, JetBrains Mono, real report data, Material icons, responsive stacking, localized semantics, and 48 dp header targets were retained.

Its post-change visual verification also remained blocked because no rendered post-change screenshot was available. This record is superseded by the attached 724 × 2172 instrument-panel source used for the current correction.

final result: blocked
