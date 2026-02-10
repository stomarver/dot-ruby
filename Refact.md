# Refact

## Ключевые изменения

- Исправлена ошибка Groovy DSL (`No such property: Type`) через стабильный ScriptEngine binding и корректный closure delegate flow.
- Server block-логика закреплена в `SwordsGame.server.data.blocks`.
- Client render block-логика закреплена в `SwordsGame.client.blocks.RenderRegistry`.
- Введены два отдельных DSL-файла:
  - `SwordsGame/server/data/blocks/blocks.dsl`
  - `SwordsGame/client/blocks/blocks.dsl`
- DSL упрощён: тип блока выводится по имени секции (`stone { ... }`), без обязательного `type Type.STONE`.

## Почему структура лучше

- Данные и авторитетная логика блоков находятся на сервере.
- Рендер и визуальные свойства блоков находятся на клиенте.
- DSL стал чище и ближе к нативному Groovy-стилю.
- Подготовка к дальнейшему расширению `server.data` (юниты/постройки/статистика) стала логичнее.
