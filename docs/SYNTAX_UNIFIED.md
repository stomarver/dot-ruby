# Unified content syntax

This project now uses one fluent style for content declarations.

## Blocks

```java
Reg.reg(Type.COBBLE,
        Syn.blk(Type.COBBLE)
                .tex(Paths.BLOCK_COBBLE)
                .build());

Reg.reg(Type.GRASS,
        Syn.blk(Type.GRASS)
                .tex(Paths.BLOCK_GRASS, Paths.BLOCK_GRASS, Paths.BLOCK_GRASS)
                .props(p -> p.randomRotation().randomColor().smoothing().hardness(0.7f))
                .build());
```

## Images

```java
TexLd.Texture cursor = ImgReg.reg(Syn.img(Paths.UI_CURSOR).alphaKey());
```

## Models (glTF/GLB)

```java
MdlReg.reg(Syn.mdl("house", "assets/models/house.glb").scale(1.0f));
MdlReg.Ent house = MdlReg.get("house");
```

This keeps block/image/model registration consistent and compact.
