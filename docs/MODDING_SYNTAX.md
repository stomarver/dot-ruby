# Modding Syntax Guide

Практический справочник по синтаксису контента и UI для моддеров.

---

## 1. Базовая идея

Во всём проекте используется единый стиль деклараций через `Syn`:

- блоки: `Syn.blk(...)`
- изображения: `Syn.img(...)`
- модели: `Syn.mdl(...)`

Старайтесь придерживаться формата: **описали -> зарегистрировали -> используем через id/path**.

---

## 2. Регистрация блоков

Файл: `SwordsGame.client.blocks.Reg`.

### 2.1 Один texture-слой

```java
Reg.reg(Type.COBBLE,
        Syn.blk(Type.COBBLE)
                .tex(Paths.BLOCK_COBBLE)
                .build());
```

### 2.2 Top/Bottom/Side текстуры + свойства

```java
Reg.reg(Type.GRASS,
        Syn.blk(Type.GRASS)
                .tex(Paths.BLOCK_GRASS, Paths.BLOCK_GRASS, Paths.BLOCK_GRASS)
                .props(p -> p.randomRotation()
                             .randomColor()
                             .smoothing()
                             .hardness(0.7f))
                .build());
```

### 2.3 Поддерживаемые свойства `BlkProps`

- `randomRotation()`
- `randomColor()`
- `emission()`
- `transparent()`
- `nonSolid()`
- `smoothing()`
- `hardness(float)`

---

## 3. Регистрация изображений

Используйте `ImgReg` + `Syn.img`.

```java
TexLd.Texture t = ImgReg.reg(Syn.img(Paths.UI_CURSOR).alphaKey());
```

Где:
- `alphaKey()` включает удаление чёрного фона в альфу.
- Путь обычно берётся из `Paths`, либо строкой ресурса внутри `src/main/resources`.

Пример без alpha key:

```java
TexLd.Texture raw = ImgReg.reg(Syn.img("ui/icon.png"));
```

---

## 4. Регистрация glTF/GLB моделей

Используйте `MdlReg` + `Syn.mdl`.

```java
MdlReg.reg(Syn.mdl("house", "assets/models/house.glb").scale(1.0f));
MdlReg.Ent house = MdlReg.get("house");
```

`Ent` содержит:
- `mdl` — `GltfModel`
- `scale` — масштаб модели

### 4.1 Что поддерживает `GltfModel`

- `.glb` и `.gltf`
- `POSITION` (обязательно)
- `NORMAL`, `TEXCOORD_0` (опционально)
- индексы `UNSIGNED_BYTE/SHORT/INT`
- только TRIANGLES primitives

Если нужны скины/анимации/расширения — это отдельный этап.

---

## 5. Пути ресурсов

`Paths` содержит базовые константы:

- `BLOCK_COBBLE`, `BLOCK_GRASS`, `BLOCK_STONE`
- `FONT_MAIN`
- `UI_CURSOR`, `UI_CHAR_FRAME`, `UI_SEPARATOR`

Рекомендация: для стабильности добавляйте новые пути в `Paths` вместо raw-строк по проекту.

---

## 6. Синтаксис текста (HUD / UI)

Текст рисуется через `Txt.draw(...)`.

Пример:

```java
text.draw("unit.name", Anc.LEFT, Anc.TOP, 10, 2, 1);
```

### 6.1 Цвет-коды в строке

Поддержка `^N`:

- `^1` красный
- `^2` зелёный
- `^3` синий
- `^4` жёлтый
- `^5` магента
- остальное -> белый

Пример:

```java
"^2Gold^0: 300\n^1Alert^0: under attack"
```

### 6.2 Выравнивание/anchor

Используйте `Anc` (`LEFT/CENTER/RIGHT`, `TOP/CENTER/BOTTOM`) и офсеты.

---

## 7. Кнопки и UI элементы

`Btn.draw(...)` принимает anchor, размеры и текущий курсор:

```java
primaryButton.draw(label, Anc.LEFT, Anc.TOP, 10, 170, 100, 28, 1.0f, cursorX, cursorY);
```

Клик обрабатывается через hit-test (`contains`) + состояние кнопки в HUD.

---

## 8. Как добавить новый мод-контент (чеклист)

1. Добавить изображения/модели в ресурсы/asset-папку.
2. Добавить путь в `Paths` (желательно).
3. Зарегистрировать:
   - блок -> `Reg.reg(... Syn.blk ...)`
   - текстуру -> `ImgReg.reg(Syn.img(...))`
   - модель -> `MdlReg.reg(Syn.mdl(...))`
4. Использовать id/type/path в рендер- или gameplay-логике.
5. Проверить сборку `./gradlew compileJava`.

---

## 9. Стиль моддерского кода

Чтобы код был однородным:

- Делайте декларативно (через `Syn`), а не ad-hoc-конструкторы по месту.
- Старайтесь выносить числа/пути в константы.
- Для ресурсоёмких штук держите отдельный registry/class.
- Не миксуйте разные стили регистрации в одном файле.
