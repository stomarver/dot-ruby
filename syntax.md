# Syntax (Integrated Groovy DSL)

DSL встроен в код игры и исполняется через `groovy-jsr223` (без внешнего `blocks.dsl` в resources).

## Text DSL

```groovy
text.draw {
    content 'Boss defeated!\n+1000 gold ^4CRITICAL^0 hit!'
    center
    pos     0, -80
    scale   1.8f
    wave    'medium'
    shake   'fast'
    crit    'medium'
}
```

Якоря:
- `center`
- `left`
- `right`
- `top`
- `bottom`

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

Поддержка props:
- `randomRotation`
- `randomColor`
- `emission`
- `transparent`
- `smoothing`
- `nonSolid`
- `hardness <float>`

Отключение флагов: `randomRotation false`, `randomColor false`, и т.д.

## Modding API

- `Registry.registerScript(String script)` — регистрация/расширение блоков пользовательским Groovy DSL-скриптом во время запуска.
- Базовый встроенный DSL загружается в `Registry.init()` автоматически.
