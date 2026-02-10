# Syntax (Java 8 + Groovy JSR223)

## Архитектура регистрации блоков

- **Server authoritative data:** `SwordsGame.server.data.blocks.Registry`
- **Client render mapping:** `SwordsGame.client.blocks.RenderRegistry`

Server хранит data/логические свойства блоков и DSL-скрипты.
Client строит render-блоки (текстуры/прозрачность/сглаживание) из того же DSL.

---

## Blocks DSL (единый формат для data/render)

```groovy
blocks {
    air {
        type Type.AIR
        props { nonSolid }
    }

    grass {
        type Type.GRASS
        texture Paths.BLOCK_GRASS
        props {
            randomRotation
            randomColor
            smoothing
            hardness 0.6f
        }
    }

    cobble {
        type Type.COBBLE
        texture Paths.BLOCK_COBBLE
        props { hardness 2.0f }
    }

    stone {
        type Type.STONE
        texture Paths.BLOCK_STONE
        props {
            smoothing
            hardness 3.0f
        }
    }
}
```

### Modding API

Server:
- `Registry.resetToDefaults()`
- `Registry.registerScript(String script)`
- `Registry.registerScripts(Collection<String> scripts)`
- `Registry.getActiveDsl()`

Client:
- `RenderRegistry.initFromServerDsl()`
- `RenderRegistry.registerScript(String script)`

---

## Text DSL (compact)

```java
text.draw(d -> d.text("Boss defeated!\n+1000 gold ^4CRITICAL^0 hit!")
        .centerBottom()
        .at(0, -80)
        .size(1.8f)
        .wave("medium")
        .shake("fast")
        .crit("medium"));
```

Aliases:
- `text(...)` = `content(...)`
- `at(...)` = `pos(...)`
- `size(...)` = `scale(...)`

Anchor presets:
- `leftTop`, `leftBottom`, `rightTop`, `rightBottom`, `centerTop`, `centerBottom`, `center`.
