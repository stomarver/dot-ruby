# Unified content syntax

This project now uses one fluent style for content declarations.

## Blocks

```java
BlockRegistry.reg(Type.COBBLE,
        Syn.blk(Type.COBBLE)
                .tex(Paths.BLOCK_COBBLE)
                .build());

BlockRegistry.reg(Type.GRASS,
        Syn.blk(Type.GRASS)
                .tex(Paths.BLOCK_GRASS, Paths.BLOCK_GRASS, Paths.BLOCK_GRASS)
                .props(p -> p.randomRotation().randomColor().smoothing().hardness(0.7f))
                .build());
```

## Images

```java
TexLoad.Texture cursor = ImgReg.reg(Syn.img(Paths.UI_CURSOR).alphaKey());
```

## Models (glTF/GLB)

```java
MdlReg.reg(Syn.mdl("house", "assets/models/house.glb").scale(1.0f));
MdlReg.Ent house = MdlReg.get("house");
```

This keeps block/image/model registration consistent and compact.


## Dialog UI modules

```java
List<Dialog.ButtonSlot> buttons = List.of(
        Dialog.button("toggle-rendering", "rendering", Anchor.LEFT, Anchor.TOP, 18, 34, 210, 30, true),
        Dialog.button("toggle-client", "client", Anchor.LEFT, Anchor.TOP, 18, 70, 210, 30, false),
        Dialog.button("close", "close", Anchor.RIGHT, Anchor.BOTTOM, -18, -16, 140, 30)
);

hud.setDialogContent(List.of(), buttons);
hud.toggleDialog("", Anchor.CENTER, Anchor.CENTER_Y, 0, 0, 620, 330, Dialog.SelectionBlockMode.DIALOG_AREA);
```

This keeps dialog content declarative and mod-friendly (id + active flag + anchor offsets).


## Text effects

```java
text.draw("^3info", Anchor.LEFT, Anchor.TOP, 16, 16, 1.0f, Text.Wave.SLOW);
```

Available enums: `Text.Shake`, `Text.Wave`, `Text.Crit` with `NONE/SLOW/MEDIUM/FAST`.


## Naming style (UI subsystem)

Refactored naming in dialog/HUD path follows short verb-based style:
- `setLayout(...)` for slot payloads
- `getHoveredButtonId(...)` for hit-testing
- `resolveLocalAnchor(...)` for internal anchor resolution

Compatibility wrappers are preserved (`setContent`, `findHoveredButtonId`, `localAnchor`) to avoid breaking existing call-sites/mod code.
