# Snout app icon

The button nose on Ember orange (`#BC3D0A`). The SVGs here are the masters; re-export from them rather than editing PNGs.

The platform icons live in the apps:
- Android: `androidApp/src/main/res/` (adaptive icon with vector foreground, colour background and monochrome layer, plus PNG fallbacks for API < 26)
- iOS: `iosApp/iosApp/Assets.xcassets/AppIcon.appiconset/` (light, dark and tinted 1024 px)

| File | Use |
|---|---|
| `snout-icon-light.svg` | Default icon. |
| `snout-icon-dark.svg` | Dark appearance (transparent background). |
| `snout-icon-tinted.svg` | Tinted appearance. |
| `snout-icon-monochrome.svg` | Monochrome layer for Android themed icons, scaled into the 66 dp safe zone. |
| `snout-icon-play-store-512.png` | Play Console hi-res icon (512 × 512, opaque; Play applies the mask). |
