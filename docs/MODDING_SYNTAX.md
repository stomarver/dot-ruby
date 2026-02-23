# `.ruby` Modding Syntax Reference

Документ описывает **весь текущий модульный/декларативный синтаксис** проекта: что можно добавлять без переписывания ядра и через какие API это делается.

---

## 1) Базовый принцип

Проект использует декларативный стиль через fluent-строители:
- `Syn.*` для клиентских ассетов/блоков.
- `GameplaySyn.*` для серверного gameplay-контента.
- `Dialog.*` для модульного UI-контента (слоты текста/кнопок).

Идея: сначала описываете сущность, потом `register()` / `build()`.

---

## 2) Что можно модульно добавлять прямо сейчас

### Клиентская часть
- Блоки мира (визуал и физ-свойства).
- Текстуры/UI-спрайты.
- glTF/glb модели.
- HUD/Overlay-диалоги (текст и кнопки, якори, блокировка выделения).

### Gameplay часть
- Юниты.
- Здания.
- Ресурсные ноды.
- Мировые объекты.

---

## 3) Синтаксис блоков (`Syn.blk` + `BlockRegistry`)

```java
BlockRegistry.reg(Type.COBBLE,
        Syn.blk(Type.COBBLE)
                .tex(Paths.BLOCK_COBBLE)
                .build());
```

```java
BlockRegistry.reg(Type.GRASS,
        Syn.blk(Type.GRASS)
                .tex(Paths.BLOCK_GRASS, Paths.BLOCK_GRASS, Paths.BLOCK_GRASS)
                .props(p -> p.randomRotation()
                             .randomColor()
                             .smoothing()
                             .hardness(0.7f))
                .build());
```

Поддерживаемые `BlockProps`:
- `randomRotation()`
- `randomColor()`
- `emission()`
- `transparent()`
- `nonSolid()`
- `smoothing()`
- `hardness(float)`

---

## 4) Синтаксис изображений (`Syn.img` + `ImgReg`)

```java
TexLoad.Texture cursor = ImgReg.reg(Syn.img(Paths.UI_CURSOR).alphaKey());
```

```java
TexLoad.Texture raw = ImgReg.reg(Syn.img("ui/icon.png"));
```

---

## 5) Синтаксис моделей (`Syn.mdl` + `MdlReg`)

```java
MdlReg.reg(Syn.mdl("house", "assets/models/house.glb").scale(1.0f));
MdlReg.Ent house = MdlReg.get("house");
```

Поддержка `GltfModel`:
- `.glb` / `.gltf`
- `POSITION` (обязательно)
- `NORMAL`, `TEXCOORD_0` (опционально)
- индексы `UNSIGNED_BYTE/SHORT/INT`
- primitives: `TRIANGLES`

---

## 6) Синтаксис gameplay (`GameplaySyn`)

### 6.1 Юнит
```java
GameplaySyn.unit("myth_hoplite", "Hoplite", FactionType.HUMANS)
        .age(Age.LEGENDS)
        .cost(ResourceBundle.of(ResourceType.FOOD, 60, ResourceType.MINERALS, 30))
        .roles(EnumSet.of(UnitRole.INFANTRY))
        .combat(CombatType.MELEE)
        .stats(new UnitStats(...))
        .model("assets/models/units/hoplite.gltf")
        .tag("unit")
        .register();
```

### 6.2 Здание
```java
GameplaySyn.building("myth_temple", "Temple", FactionType.HUMANS)
        .age(Age.HISTORIES)
        .cost(ResourceBundle.of(ResourceType.WOOD, 150, ResourceType.STONE, 100))
        .roles(EnumSet.of(BuildingRole.WORKSHOP, BuildingRole.TRAINING))
        .model("assets/models/buildings/temple.gltf")
        .register();
```

### 6.3 Ресурс
```java
GameplaySyn.resource("res_gold_mine", "Gold Mine", ResourceType.MINERALS)
        .amount(2200)
        .difficulty(120)
        .model("assets/models/resources/gold_mine.gltf")
        .register();
```

### 6.4 Мировой объект
```java
GameplaySyn.object("obj_relic_sun", "Relic of the Sun")
        .roles(EnumSet.of(ObjectRole.RELIC, ObjectRole.INTERACTIVE))
        .hp(250)
        .interactable(true)
        .model("assets/models/objects/relic_sun.gltf")
        .register();
```

Все записи попадают в `GameplayRegistry` с защитой от дубликатов id.

---

## 7) Синтаксис UI-диалогов (модульно)

`Dialog` поддерживает:
- позиционирование через `Anchor`.
- режимы выделения (`NONE` — обычное выделение, `FULL_SCREEN` — полное отключение выделения).
- слоты текста и кнопок.
- кнопки с `id` и флагом `active`.

### 7.1 Открытие
```java
hud.toggleDialog("", Anchor.CENTER, Anchor.CENTER_Y, 0, 0, 620, 330,
        Dialog.SelectionBlockMode.NONE);
```

### 7.2 Контент (рекомендуемый краткий синтаксис)
```java
List<Dialog.TextSlot> textSlots = List.of(
        Dialog.text("^3title", Anchor.LEFT, Anchor.TOP, 18, 40)
);

List<Dialog.ButtonSlot> buttons = List.of(
        Dialog.button("toggle-rendering", "rendering", Anchor.LEFT, Anchor.TOP, 18, 34, 210, 30, true),
        Dialog.button("close", "close", Anchor.RIGHT, Anchor.BOTTOM, -18, -16, 140, 30)
);

hud.setDialogContent(textSlots, buttons);
```

### 7.3 Визуальный state кнопки
`active=false` затемняет кнопку (off-состояние).

---

## 8) HUD / Text синтаксис

Текст:
```java
text.draw("unit.name", Anchor.LEFT, Anchor.TOP, 10, 2, 1.0f);
```

Цвет-коды:
- `^1` red
- `^2` green
- `^3` blue
- `^4` yellow
- `^5` magenta
- иное -> white

Секции в Info-панелях можно задавать через `# Header` в первой строке блока.

---

## 9) Мини-чеклист добавления нового модуля

1. Добавить ресурс (текстура/модель).
2. Добавить путь в `Paths` (рекомендуется).
3. Зарегистрировать через `Syn`/`GameplaySyn`.
4. Привязать к UI/HUD при необходимости.
5. Проверить `./gradlew compileJava`.


---

## 10) Модификаторы текста (Shake / Wave / Crit)

В `Text` есть встроенные модификаторы анимации текста:

- `Text.Shake`: `NONE`, `SLOW`, `MEDIUM`, `FAST`
- `Text.Wave`: `NONE`, `SLOW`, `MEDIUM`, `FAST`
- `Text.Crit`: `NONE`, `SLOW`, `MEDIUM`, `FAST`

Пример `Wave`:
```java
text.draw("^4warning", Anchor.LEFT, Anchor.TOP, 20, 20, 1.0f, Text.Wave.MEDIUM);
```

Цвет-коды (`^1..^5`) и модификаторы можно комбинировать в одной строке.

---

## 11) Чего не хватает текущей системе диалогов

Чтобы на текущей базе строить полноценно подменю/уведомления/wiki/древо навыков, не хватает:

1. **Контейнеров layout** (vertical/horizontal stack, grid, auto-size).
2. **Скролла** (включая инерцию/колёсико/scrollbar).
3. **Постоянных виджетов** (checkbox, tab, input, slider, icon button).
4. **Навигации по страницам** (history back/forward, breadcrumbs).
5. **Событийной модели** (onClick/onHover/onFocus без ручного polling).
6. **Tooltip/Popup API** (для уведомлений и справки по tech tree).
7. **Горячих клавиш и фокуса** (keyboard-first UX).
8. **Сериализуемых UI-схем** (JSON/Groovy DSL для модов без Java-кода).
9. **Состояний переходов/анимаций** (open/close/fade/slide).
10. **Виртуализации длинных списков** (wiki-страницы и большие ветки дерева).

---

## 12) Краткий анализ по релизам/истории

В локальной копии репозитория git-теги релизов отсутствуют, поэтому ориентир был взят по последним коммитам ветки (HUD/dialog/debug evolution). При появлении тегов имеет смысл вынести changelog по версиям в отдельный `docs/RELEASE_NOTES_SYNTAX.md`.
