# Refact notes

- Удалена зависимость от `Paths`: пути до block texture теперь задаются прямо в DSL (`texture "grass.png"`).
- Добавлена авто-нормализация путей блоков в `RenderRegistry`: все относительные пути идут в `textures/blocks/`.
- Ресурсы реорганизованы:
  - `textures/blocks`
  - `textures/fonts`
  - `textures/ui`
- Добавлен блок `glass` и параметр `aoAffected`.
- Добавлен UI text data registry: `resources/data/ui/text/text.dsl` + `TextRegistry`.
