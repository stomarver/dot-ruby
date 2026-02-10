# Syntax (Groovy DSL)

## Требования
- Используется Groovy DSL через `groovy-jsr223`.
- Синтаксис без Java-скобок/`;`.
- Флаги-параметры можно писать декларативно одной строкой.

## DSL текста

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

Доступные якоря:
- `center`
- `left`
- `right`
- `top`
- `bottom`

Эффекты принимают `slow | medium | fast`.
Если эффект не указан — он выключен.

## DSL блоков

```groovy
blocks {
    air {
        type Type.AIR
        props {
            nonSolid
        }
    }

    grass {
        type    Type.GRASS
        texture Paths.BLOCK_GRASS
        props {
            randomRotation
            randomColor
            smoothing
            hardness 0.6f
        }
    }

    cobble {
        type    Type.COBBLE
        texture Paths.BLOCK_COBBLE
        props {
            hardness 2.0f
        }
    }

    stone {
        type    Type.STONE
        texture Paths.BLOCK_STONE
        props {
            smoothing
            hardness 3.0f
        }
    }
}
```

Поддержка свойств (`props`):
- `randomRotation`
- `randomColor`
- `emission`
- `transparent`
- `smoothing`
- `nonSolid`
- `hardness <float>`

Явное выключение флагов:
- `randomRotation false`
- `randomColor false`
- `emission false`
- `transparent false`
- `smoothing false`
- `nonSolid false`

## TextureLoader

`toggleBlack` настраивается через `TextureLoader.LoadOptions`.
По умолчанию `toggleBlack=true` только для `font.png`.
