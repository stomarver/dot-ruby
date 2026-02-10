# Refact

## Что исправлено в этой итерации

- Исправлена ошибка в `Info.java` (конфликт имени `content` в цикле рендера строк).
- Сильнее упрощён синтаксис text DSL:
  - добавлены короткие якоря `leftTop/leftBottom/rightTop/rightBottom/centerTop/centerBottom`;
  - добавлены алиасы `text`, `at`, `size` для более чистого вызова.
- HUD/Info/Message переписаны на компактный декларативный стиль `text.draw(...)`.
- Улучшена база для моддинга блоков:
  - встроенный default block DSL;
  - удобные API: `resetToDefaults`, `registerScript`, `registerScripts`.
- Восстановлен grass над cobble в генерации столбов.

## Результат

Кодовая база стала чище, синтаксис короче, а вход для моддинга — проще и удобнее для расширения.
