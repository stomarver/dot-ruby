# .ruby

`Genre: RTS` • `Status: pre-alpha (core foundation)`

Rubiland no longer lives in peace around the Great Ruby. Its shards are now scattered across the Mines, and every faction is forced into conflict for territory, survival, and progression.

`.ruby` is an indie RTS built with Java and LWJGL, focused on a raw prototype-era visual language: pixel art, minimal UI, and a world-first atmosphere.

---

## Quick Start

### Requirements
- Java 17

### Build
```bash
./gradlew shadowJar
```

The packaged JAR is generated in `build/libs/dot-ruby-*/`.

### Run
```bash
java -jar build/libs/dot-ruby-*/dot-ruby-*.jar
```

Run with debug profile:
```bash
java -jar build/libs/dot-ruby-*/dot-ruby-*.jar --debug
```

---

## Current Runtime Foundation

- Session-based runtime flow (Main Menu + Scenario state)
- Chunk-based 3D world rendering (LWJGL/OpenGL)
- Camera movement, edge-scroll, rotation, zoom, reset
- Day/Night cycle with fog and tint blending
- Script-driven HUD and dialog layouts (Groovy)
- Toggleable debug information blocks
- Discord RPC integration
- Screenshot capture to `~/Pictures/SwordsGame/`
- Terminal text messages displayed in HUD

---

## Controls

### Core
- `F4` or `Alt+Enter` — fullscreen
- `F12` or `Ctrl+P` — screenshot
- `Esc` — close application
- `Arrow Keys` or edge-scroll — camera movement
- `Q / E` or mouse wheel — camera rotation
- `=` / `-` (or zoom wheel behavior) — zoom
- `R` — reset camera

### System / Debug
- `F7` — toggle Virtual/Real render mode
- `F8` — toggle debug info visibility
- `B` — toggle chunk bounds
- `Numpad + / Numpad -` — adjust debug fog distance
- `Y / U` — day/night time control

---

## Content & Modding Entry Points

- HUD / dialog / button / text / sprite script:
  - `src/main/resources/shared/ui/hud/hud-ui.groovy`
- Block script definitions:
  - `src/main/resources/shared/blocks/blocks.groovy`
- Full syntax and integration guide:
  - [`docs/ModdingGuide.md`](docs/ModdingGuide.md)

---

## Factions (Design Direction)

The design target includes 3 factions across 3 ages:
- **Humans** — balanced core army with strong infantry/elite direction
- **Elves** — mobility, ranged pressure, flexible economy
- **Dwarves** — siege strength, durability, technology upgrades

Economy is built around 4 resources:
- wood
- minerals
- food/raw supplies
- stone

This faction layer is a design target and not fully implemented gameplay yet.

---

## Roadmap (Short)

1. Core RTS loop: selection, orders, pathfinding
2. Worker loops: gather → deliver → build
3. Unit/building production and combat loop
4. Age progression and upgrades
5. AI opponent and later networking layer

---

## Links

- Discord: https://discord.gg/yXJgtqAfWe
- GitHub: https://github.com/stomarver/dot-ruby
- Screenshots: https://imgur.com/a/Bweq7sZ

---

If you work with Java/LWJGL and want to help build an RTS from first principles, contributions are welcome.
