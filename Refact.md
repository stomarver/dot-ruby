# Refact

## Ключевые изменения

- Исправлена ошибка Groovy DSL (`No such property: Type`) через стабильный ScriptEngine binding и устранение проблем резолвинга DSL-методов в одном script unit.
- Server block-логика закреплена в `SwordsGame.server.data.blocks`.
- Client render block-логика закреплена в `SwordsGame.client.data.blocks.RenderRegistry`.
- Введены два отдельных DSL-файла:
  - `data/server/blocks/blocks.dsl`
  - `data/client/blocks/blocks.dsl`
- DSL упрощён: тип блока выводится по имени секции (`stone { ... }`), legacy `type Type.*` удалён.

## Почему структура лучше

- Данные и авторитетная логика блоков находятся на сервере.
- Рендер и визуальные свойства блоков находятся на клиенте.
- DSL стал чище и ближе к нативному Groovy-стилю.
- Подготовка к дальнейшему расширению `server.data` (юниты/постройки/статистика) стала логичнее.
