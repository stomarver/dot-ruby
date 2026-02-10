# Syntax DSL (Java 8)

## Text DSL

```java
text.draw(d -> {
    d.content("Boss defeated!\n+1000 gold ^4CRITICAL^0 hit!");
    d.center(); // or left(), right(), top(), bottom()
    d.pos(0, -80);
    d.scale(1.8f);
    d.wave("medium");
    d.shake("fast");
    d.crit("medium");
});
```

### Supported text options
- `content(String)` — text body, supports `\n` and color tags `^0..^9`.
- Anchors: `center() | left() | right() | top() | bottom()`.
- `pos(float x, float y)` — offset relative to selected anchor.
- `scale(float)` — glyph scaling.
- Effects (optional):
  - `wave("slow|medium|fast")`
  - `shake("slow|medium|fast")`
  - `crit("slow|medium|fast")`

If an effect is omitted, it is disabled.

---

## Block registration DSL

```java
Registry.blocks(def -> {
    def.air(b -> {
        b.type(Type.AIR);
        b.props(p -> p.nonSolid());
    });

    def.grass(b -> {
        b.type(Type.GRASS);
        b.texture(Paths.BLOCK_GRASS);
        b.props(p -> p.randomRotation().randomColor().smoothing().hardness(0.6f));
    });

    def.cobble(b -> {
        b.type(Type.COBBLE);
        b.texture(Paths.BLOCK_COBBLE);
        b.props(p -> p.hardness(2.0f));
    });

    def.stone(b -> {
        b.type(Type.STONE);
        b.texture(Paths.BLOCK_STONE);
        b.props(p -> p.smoothing().hardness(3.0f));
    });

    def.define(b -> {
        b.type(Type.LOG_OAK);
        b.top(Paths.LOG_OAK_TOP);
        b.bottom(Paths.LOG_OAK_BOTTOM);
        b.side(Paths.LOG_OAK_SIDE);
        b.props(p -> p.hardness(2.0f).randomRotation(false));
    });
});
```

### Block DSL
- `type(Type)` — required.
- Textures:
  - `texture(path)` for single-texture blocks.
  - `top(path)`, `bottom(path)`, `side(path)` for 3-way block texturing.
- `props(Consumer<PropsDsl>)` — block properties.

### Props DSL
Flags are off by default:
- `randomRotation()` / `randomRotation(boolean)`
- `randomColor()` / `randomColor(boolean)`
- `emission()` / `emission(boolean)`
- `transparent()` / `transparent(boolean)`
- `smoothing()` / `smoothing(boolean)`
- `nonSolid()` / `nonSolid(boolean)`
- `solid(boolean)`
- `hardness(float)`

---

## Texture loading option: toggleBlack

Texture loader now supports options:

```java
TextureLoader.loadTexture("fonts/font.png");
TextureLoader.loadTexture("my/path.png", TextureLoader.LoadOptions.of(true));
```

Rules:
- `toggleBlack` means pure black pixels become transparent.
- Default profile applies `toggleBlack=true` **only** to `font.png`.

---

## Tick / DayNight conventions

- Simulation tick cap: **40 TPS**.
- Day/night update: **+1 yaw every 4 ticks** (10 updates/sec at 40 TPS).
- Rendering uses interpolation between previous/current sun yaw for smoother animation.
