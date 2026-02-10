# Refact Summary

## Architecture
- Added fixed-tick pipeline (`TickSystem`, 40 TPS cap).
- Connected `Base` and `Debug` to tick updates.
- Added `DayNightCycle` server-side object with client interpolation support.

## Rendering
- Added depth fog in 3D pass (EXP2 distance fog with radial-like attenuation).
- Added vertex ambient occlusion pipeline:
  - AO computed per visible face and per vertex.
  - AO transferred to mesh color buffer.
  - Top smoothing now darkens lowered vertices for slope-aware shading.

## World / Terrain
- Terrain pillars now use `cobble` at top and body, with `stone` underlayer.
- Visibility logic keeps buried stone faces hidden until they contact air.

## DSL and Declarative syntax
- Rebuilt block registration as declarative DSL in `Registry`:
  - `blocks { ... }` style through Java-8 lambda API.
  - Declarative props profile (`PropsDsl`).
- Added declarative text draw DSL in `Text.draw(d -> { ... })`.
- Rewrote `syntax.md` to document the new syntax and conventions.

## Asset Loading
- Added texture load options with `toggleBlack` support.
- Default `toggleBlack` profile is enabled only for `font.png`.
