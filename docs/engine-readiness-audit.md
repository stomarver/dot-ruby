# Engine Readiness Audit (branch `work`)

## Scope of review
- Structure and possible redundant files/classes.
- Consistency of texture loading, sprite placement, and text rendering APIs.
- Readiness for scaling into an RTS with factions/epochs/resources.

## Quick findings

### Potentially redundant or risky areas
1. `Base` and `Debug` duplicate most of the bootstrap/game-loop/cleanup code.
   - This is not "wrong", but it increases divergence risk when adding features.
   - Recommended: extract a shared `ClientApplication`/`GameLoop` core and keep `Base`/`Debug` as thin mode wrappers.

2. `HUD.startTerminalThread()` has an empty `catch` block.
   - Silent failure complicates diagnostics.
   - Recommended: at least log one-line reason, or route through a lightweight logger.

3. `syntax.md` contained outdated package references (`SwordsGame.ui.Text`) and did not fully reflect current class layout.
   - This document should stay tightly synchronized with `client.ui` and `client.graphics` packages to avoid integration mistakes.

4. Local environment artifact `.gradle/` appears in working tree (untracked).
   - Recommended: keep it ignored to avoid accidental commits.

### Texture/text/sprite API consistency
Current state is mostly coherent:
- Texture paths are centralized in `client.assets.Paths`.
- Loading is funneled through `TextureLoader.loadTexture(...)` with cache semantics.
- UI sprites are rendered via `Sprite.draw(...)` with shared `Anchor` placement model.
- Text rendering is centralized in `ui.Text`, including multi-line and color code behavior.

Remaining normalization opportunities:
1. Define one explicit policy for `removeBlack` flag usage (UI/font vs world textures) and document it.
2. Replace repeated raw numeric offsets/scales in HUD with named constants (`HUDLayout`).
3. Add a small `UiTheme`/`UiAtlas` layer to avoid passing raw texture handles in more places as UI grows.

## RTS-scale readiness (current verdict)
The project is **partially ready for extension**:
- Good: clear package split (`client/server/shared`), template-driven faction data (`RtsTemplates`, `FactionTechTree`), and protocol DTOs for UI.
- Missing for long-term scalability:
  1. Shared game-loop abstraction (reduce `Base` vs `Debug` copy-paste).
  2. Data-driven content externalization (JSON/TOML/YAML) for units/buildings/tech instead of only Java constants.
  3. Lightweight service boundaries on server side (`EconomyService`, `CombatService`, `ConstructionService`, `PathingService`) before logic volume increases.
  4. Minimal CI checks (compile + style + smoke tests) with pinned JDK.

## Suggested next refactor order
1. Extract shared app lifecycle from `Base`/`Debug`.
2. Introduce `HUDLayout` constants and a simple logger.
3. Move RTS templates to external data files + validator.
4. Add deterministic simulation step + headless tests for economy/combat balance rules.
