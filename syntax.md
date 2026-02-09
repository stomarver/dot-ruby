# Синтаксис текста и регистрация блоков

## Система текста

### Базовый вывод
Текст рисуется через класс `SwordsGame.ui.Text`. Основные методы:

- `draw(String text, Anchor.TypeX ax, Anchor.TypeY ay, float x, float y, float scale)`
- `draw(String text, Anchor.TypeX ax, Anchor.TypeY ay, float x, float y, float scale, Wave wave)`
- `draw(String text, Anchor anchor, float x, float y, float scale)`

Параметры задают:
- **Anchor** — точку привязки (лево/центр/право по X и верх/центр/низ по Y).
- **x, y** — смещения от привязки в пикселях.
- **scale** — масштаб символов.

### Разделение строк
Используйте `\n` для переноса строк. Шаг между строками рассчитывается через `Text.getLineStep(scale)`.

### Цветовые коды
Внутри строки поддерживаются цветовые коды формата `^<цифра>`:

- `^1` — красный
- `^2` — зелёный
- `^3` — синий
- `^4` — жёлтый
- `^5` — фиолетовый
- любые другие цифры (например `^0`) сбрасывают цвет в белый

Пример:
```
"^2Зелёный ^1красный ^0обычный"
```

### Анимации текста
Анимации задаются параметрами метода, а не внутри строки:

- `Text.Shake` — дрожание (`SLOW`, `MEDIUM`, `FAST`)
- `Text.Wave` — волна (`SLOW`, `MEDIUM`, `FAST`)
- `Text.Crit` — критический «толчок» (`SLOW`, `MEDIUM`, `FAST`)

Например, `draw(text, ax, ay, x, y, scale, Text.Wave.MEDIUM)` включает волну.

## Регистрация блоков

### 1) Добавьте тип блока
В `SwordsGame.client.blocks.Type` добавьте новый элемент перечисления с уникальным `id` и именем:
```java
NEW_BLOCK(4, "New Block")
```
`id` используется как сетевой идентификатор и хранится в чанках.

### 2) Создайте класс блока
В `SwordsGame.client.blocks` создайте класс, который наследуется от `Block`:
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
Свойства задаются через `BlockProperties`:
- `randomRotation()` — случайный поворот текстуры
- `randomColor()` — случайный оттенок
- `emission()` — свечение (отключает освещение)
- `transparent()` — прозрачность
- `nonSolid()` — делает блок не-твёрдым
- `smoothing()` — включает сглаживание верхней грани
- `hardness(float)` — прочность

### 3) Пропишите путь к текстуре
В `SwordsGame.client.assets.Paths` добавьте путь к текстуре:
```java
public static final String BLOCK_NEW = "blocks/new.png";
```
Файл должен находиться в `src/main/resources/blocks/`.

### 4) Зарегистрируйте блок
В `SwordsGame.client.blocks.Registry.init()` зарегистрируйте новый блок:
```java
register(Type.NEW_BLOCK, new NewBlock());
```

### 5) Серверная генерация (при необходимости)
Если блок участвует в генерации мира или логике сервера, используйте `Type.NEW_BLOCK.id` в нужных местах (например, в `Terrain.generate`).
