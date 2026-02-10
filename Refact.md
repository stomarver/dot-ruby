# Refact

## Main structural refactor for multiplayer-ready RTS base

- Перенесена регистрация блоков на **server layer**:
  - `SwordsGame.server.data.blocks.Registry`
  - `SwordsGame.server.data.blocks.Type`
  - `SwordsGame.server.data.blocks.BlockData`
- Исправлена причина ScriptException (`Type.AIR`):
  - теперь в Groovy prelude используются `import SwordsGame.server.data.blocks.Type` и `import Paths`.

## Client/Server split

- `server.data.blocks.Registry` — authoritative block DSL + logical block data.
- `client.blocks.RenderRegistry` — только render-аспекты (текстуры/visual props), инициализируется из `Registry.getActiveDsl()`.
- Инициализация в `Base/Debug`:
  1) `server Registry.init()`
  2) `RenderRegistry.initFromServerDsl()`

## UI DSL cleanup

- HUD/Info/Message используют упрощенный `text.draw(...)` синтаксис.
- Исправлена ошибка в `Info` с конфликтом переменных.

## Goal

Структура стала логичнее для multiplayer RTS:
- authoritative data/state на сервере,
- rendering concerns на клиенте,
- единый DSL формат для modding и расширения.
