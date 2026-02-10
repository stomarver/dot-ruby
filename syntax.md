# Syntax (Java 8 + Groovy JSR223)

## Пакетная структура (RTS/multiplayer-ready)

- Server data blocks: `SwordsGame.server.data.blocks`
- Client render blocks: `SwordsGame.client.data.blocks`

## DSL файлы

- Server DSL: `src/main/resources/data/server/blocks/blocks.dsl`
- Client DSL: `src/main/resources/data/client/blocks/blocks.dsl`

Оба файла используют один и тот же Groovy-стиль, но:
- server-файл влияет на логические данные блока,
- client-файл влияет на визуал (текстуры/props).

---

## Block DSL (без `type Type.STONE`)

Теперь тип блока определяется по имени секции:

```groovy
blocks {
    air {
        props { nonSolid }
    }

    grass {
        texture Paths.BLOCK_GRASS
        props {
            randomRotation
            randomColor
            smoothing
            hardness 0.6f
        }
    }

    cobble {
        texture Paths.BLOCK_COBBLE
        props { hardness 2.0f }
    }

    stone {
        texture Paths.BLOCK_STONE
        props {
            smoothing
            hardness 3.0f
        }
    }
}
```

---

## Modding API

Server (`SwordsGame.server.data.blocks.Registry`):
- `resetToDefaults()`
- `registerScript(String script)`
- `registerScripts(Collection<String> scripts)`

Client (`SwordsGame.client.data.blocks.RenderRegistry`):
- `initFromServerDsl()` (загружает client DSL файл)
- `registerScript(String script)`

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
