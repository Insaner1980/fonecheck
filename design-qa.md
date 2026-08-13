# Home visual design QA

- Source visual truth: `C:\Dev\fonecheck\image2.png`
- Pre-change implementation screenshot: `C:\Dev\fonecheck\image1.png`
- Side-by-side comparison: `C:\Users\EmmaH\.codex\visualizations\2026\08\12\019ff4a6-7c64-7643-89d9-d2c69ee7c4de\home-reference-vs-current.png`
- Post-change implementation screenshot: unavailable until the revised Compose source is rendered
- Source pixels: 1122 x 1402
- Pre-change implementation pixels: 1080 x 2424
- Comparison normalization: the implementation Home top was cropped to 1080 x 1300 from y=140, resized to 1122 px wide, and extended to 1122 x 1402 before horizontal comparison
- State: dark theme; report values differ intentionally because the reference values are examples and the implementation uses real report data

## Full-view comparison evidence

The normalized side-by-side comparison showed four blocking visual differences in the previous implementation: the score graphic resembled a physical device rather than an abstract gauge, the card used a thick multi-layer enclosure, the metrics sat in three separate mini-cards, and the status/card proportions were substantially denser than the reference.

The Compose source was revised to use one calm rounded report surface, a 270-degree abstract score arc, a larger status hierarchy, and three integrated metric columns separated only by dividers. The History and Settings controls retain real navigation and use the existing fonecheck visual language with softer lavender-edged surfaces.

## Focused region comparison evidence

The score-and-summary region and the bottom metrics region were inspected in the combined comparison. The implementation changes directly address the visible silhouette, hierarchy, spacing, icon meaning, border weight, and mini-card drift found there. The existing fonecheck mark remains the logo asset, while metric and summary symbols use the project's Material icon family.

## Fidelity surfaces

- Fonts and typography: DM Sans and JetBrains Mono remain the implementation fonts. The status is now a 34sp display treatment and the score remains a large monospace value; post-render wrapping and optical balance are not yet visually verified.
- Spacing and layout rhythm: the revised phone card has 20dp horizontal and 26dp vertical padding, a 188dp minimum hero row, and an integrated 92dp metric row. Post-render proportions are not yet visually verified.
- Colors and tokens: the implementation keeps the graphite, aqua, lavender, green, coral, and red project tokens. The revised surface uses a thin lavender edge instead of the previous heavy layered frame.
- Image quality and assets: the existing `fonecheck_mark` raster asset is retained. No reference bitmap is embedded in the UI and no new raster asset was introduced.
- Copy and content: all visible values, report status, completion time, labels, and localized strings remain driven by the real report and existing resources.
- Icons: Categories uses Material `List`, Coverage uses `CheckCircle`, and Needs attention uses `Warning` or `CheckCircle` when clear. Post-render size and optical alignment are not yet visually verified.
- Responsiveness and accessibility: the existing phone/tablet grid, large-font stacked report layout, semantics, localized state descriptions, and 48dp header touch targets remain in place.

## Comparison history

- [P1] Physical-device score enclosure. Fixed in source by replacing it with a simple progress arc and centered score.
- [P1] Separate metric mini-cards. Fixed in source by integrating the metrics into the main report surface with dividers.
- [P2] Heavy layered border and cramped report proportions. Fixed in source with a single thin-edged surface, larger padding, and a taller hero row.
- [P2] Status hierarchy too small relative to the gauge. Fixed in source with a larger status treatment and clearer label/body hierarchy.
- Post-fix visual evidence: blocked because no device or emulator is connected and repository guidance prohibits Codex from running Gradle to render a new build.

## Remaining blocker

- [P2] Post-change visual fidelity is unverified.
  - Location: Home header and latest Full Check card.
  - Evidence: the source and pre-change screenshot can be compared, but there is no rendered screenshot of the revised Compose source.
  - Impact: exact proportions, wrapping, icon alignment, and tonal balance cannot be confirmed from source alone.
  - Fix: render the revised Home screen in dark theme with a saved Full Check report, capture the same top region, and repeat the normalized side-by-side comparison.

## Implementation checklist

- Render the revised Home top on a device or Compose Preview.
- Capture the dark-theme latest-report state at the same viewport.
- Compare it with `image2.png` and correct any remaining P1/P2 visual differences.

final result: blocked
