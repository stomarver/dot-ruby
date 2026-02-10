# Refact

## Что исправлено относительно прошлого рефакторинга

- Удалён Fog из рендера.
- Тиковая система переписана и перенесена в серверный слой (`SwordsGame.server.tick`).
- Обновление DayNightCycle замедлено (обновление yaw теперь реже).
- Регистрация блоков переведена на Groovy DSL (`groovy-jsr223`) с синтаксисом без лишних скобок и `;`.
- Удалены классы `Stone.java`, `Grass.java`, `Cobble.java` — блоки теперь задаются только через DSL/Registry.
- `syntax.md` переписан под Groovy DSL формат.

## Архитектурная заметка

- `Registry` загружает `/dsl/blocks.dsl` и исполняет его через Groovy ScriptEngine.
- `Text` поддерживает `drawGroovy(script)` с `text.draw { ... }` синтаксисом.
