# Syntax (Integrated Groovy DSL)

DSL встроен в код игры и исполняется через `groovy-jsr223`.
Внешний `blocks.dsl` не нужен.

## Text DSL (упрощённый)

### Java
```java
text.draw(d -> d.text("Boss defeated!\n+1000 gold ^4CRITICAL^0 hit!")
        .centerBottom()
        .at(0, -80)
        .size(1.8f)
        .wave("medium")
        .shake("fast")
        .crit("medium"));
```

Короткие якоря:
- `leftTop()` / `leftBottom()`
- `rightTop()` / `rightBottom()`
- `centerTop()` / `centerBottom()` / `center()`

Алиасы:
- `text(...)` = `content(...)`
- `at(x,y)` = `pos(x,y)`
- `size(v)` = `scale(v)`

### Groovy
```groovy
text.draw {
    content 'Boss defeated!\n+1000 gold ^4CRITICAL^0 hit!'
    center
    pos   0, -80
    scale 1.8f
    wave  'medium'
    shake 'fast'
    crit  'medium'
}
```

## Blocks DSL

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

## Modding API

- `Registry.resetToDefaults()` — сброс к встроенному базовому набору блоков.
- `Registry.registerScript(String script)` — добавить/переопределить блоки Groovy-скриптом.
- `Registry.registerScripts(Collection<String> scripts)` — пакетная регистрация нескольких мод-скриптов.
