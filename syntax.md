# Синтаксис текста, спрайтов и регистрации блоков

## 1) Текст (client.ui.Text)

Текст рисуется через `SwordsGame.client.ui.Text`.
Основные сигнатуры:

- `draw(String text, Anchor.TypeX ax, Anchor.TypeY ay, float x, float y, float scale)`
- `draw(String text, Anchor.TypeX ax, Anchor.TypeY ay, float x, float y, float scale, Text.Wave wave)`
- `draw(String text, Anchor anchor, float x, float y, float scale)`

Где:
- `Anchor` задаёт привязку по осям X/Y.
- `x`, `y` — смещение от опорной точки.
- `scale` — базовый масштаб текста.

### Перенос строк
Используйте `\n`. Шаг строки можно брать из `Text.getLineStep(scale)`.

### Цветовые коды
Встроенные коды: `^<цифра>`.

- `^1` — красный
- `^2` — зелёный
- `^3` — синий
- `^4` — жёлтый
- `^5` — фиолетовый
- любое другое значение (например `^0`) — белый/сброс

Пример: `"^2Зелёный ^1красный ^0обычный"`

### Анимации текста
Анимация задаётся параметрами API, а не внутри строки:
- `Text.Shake`
- `Text.Wave`
- `Text.Crit`

---

## 2) Спрайты (client.graphics.Sprite)

2D-спрайты рисуются через `SwordsGame.client.graphics.Sprite`:

- `draw(TextureLoader.Texture tex, Anchor.TypeX ax, Anchor.TypeY ay, float x, float y, float scale)`
- `draw(TextureLoader.Texture tex, Anchor anchor, float x, float y, float scale)`

Рекомендуемый стиль:
- использовать `Anchor`/`Anchor.TypeX/TypeY` для всех HUD-элементов;
- не хардкодить пути к файлам в местах рендера (см. раздел Paths).

---

## 3) Пути ассетов и загрузка текстур

### Единая точка путей
Пути к ресурсам держите в `SwordsGame.client.assets.Paths`.

Примеры:
- `Paths.BLOCK_GRASS`
- `Paths.FONT_MAIN`
- `Paths.UI_CURSOR`

### Единая точка загрузки
Используйте `TextureLoader.loadTexture(path, removeBlack)`.

Рекомендуемая договорённость:
- `removeBlack = true` для UI/шрифтов, где чёрный фон должен стать прозрачным;
- `removeBlack = false` для блоков мира и обычных текстур.

---

## 4) Регистрация блоков

### Шаг 1. Добавить тип
В `SwordsGame.client.blocks.Type`:

```java
NEW_BLOCK(BlockId.NEW_BLOCK, "New Block")
```

### Шаг 2. Создать класс блока
В `SwordsGame.client.blocks`:

```java
public class NewBlock extends Block {
    public NewBlock() {
        super(Type.NEW_BLOCK, Paths.BLOCK_NEW,
                new BlockProperties()
                        .randomRotation()
                        .randomColor());
    }
}
```

Свойства `BlockProperties`:
- `randomRotation()`
- `randomColor()`
- `emission()`
- `transparent()`
- `nonSolid()`
- `smoothing()`
- `hardness(float)`

### Шаг 3. Добавить путь
В `SwordsGame.client.assets.Paths`:

```java
public static final String BLOCK_NEW = "blocks/new.png";
```

Файл текстуры: `src/main/resources/blocks/new.png`.

### Шаг 4. Зарегистрировать блок
В `SwordsGame.client.blocks.Registry.init()`:

```java
register(Type.NEW_BLOCK, new NewBlock());
```

### Шаг 5. Использовать в генерации/логике
Если блок участвует в мире/сервере — применяйте `BlockId.NEW_BLOCK` в генерации и протоколах.
