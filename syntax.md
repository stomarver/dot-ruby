# Syntax (Java 8 + Groovy JSR223)

## Пакетная структура
- Server data blocks: `SwordsGame.server.data.blocks`
- Client render blocks: `SwordsGame.client.data.blocks`
- UI text data: `SwordsGame.ui.data.text`

## Пути ресурсов
- Блоки: `src/main/resources/textures/blocks/*`
- Шрифты: `src/main/resources/textures/fonts/*`
- UI: `src/main/resources/textures/ui/*`

## Block DSL
Server DSL: `src/main/resources/data/server/blocks/blocks.dsl`  
Client DSL: `src/main/resources/data/client/blocks/blocks.dsl`

Тип блока задаётся по секции:

```groovy
blocks {
    grass {
        texture "grass.png"      // автоматически станет textures/blocks/grass.png
        props {
            hardness 0.6f
            aoAffected true
        }
    }

    glass {
        texture "glass.png"
        props {
            transparent
            nonSolid
            hardness 0.3f
            aoAffected false
        }
    }
}
```

### Что делает `hardness`
`hardness` — логическая «твёрдость» блока (серверные данные + клиентские метаданные). Сейчас это значение хранится в `BlockData/BlockProperties` и предназначено для геймплейных механик (скорость ломания, требования инструмента, баланс урона по блоку и т.п.).

## UI Text DSL
Ресурс: `src/main/resources/data/ui/text/text.dsl`

```groovy
text {
    key "hud.title", "Грунт"
    key "hud.example", "Boss defeated!\\n+1000 gold ^4CRITICAL^0 hit!"
}
```

Пример нового текстового класса: `SwordsGame.ui.ExampleTextOverlay`.
