# Modding Guide (UI / Dialogs / Blocks / Session States)

Короткий и практичный гайд по **актуальному** синтаксису DotRuby.

---

## 1) Где что редактировать

### UI / HUD
- Скрипт интерфейса: `src/main/resources/shared/ui/hud/hud-ui.groovy`
- Исполнитель скрипта: `src/main/java/SwordsGame/client/ui/HudScriptRunner.java`
- Рендер/интеракция HUD: `src/main/java/SwordsGame/client/ui/Hud.java`
- Диалоговый контейнер: `src/main/java/SwordsGame/client/ui/Dialog.java`
- Реестр стандартных pivot-точек: `src/main/java/SwordsGame/client/ui/HudLayoutRegistry.java`

### Сцены / состояния
- Точка входа и запуск с `--debug`: `src/main/java/SwordsGame/client/core/Session.java`
- Контракт состояния: `src/main/java/SwordsGame/client/core/SessionState.java`
- Менеджер переходов: `src/main/java/SwordsGame/client/core/SessionStateManager.java`
- Главное меню: `src/main/java/SwordsGame/client/core/MainMenuState.java`
- Игровая сессия: `src/main/java/SwordsGame/client/core/SessionScenarioState.java`

### Блоки
- Скрипт блоков: `src/main/resources/shared/blocks/blocks.groovy`
- Загрузка скрипта блоков: `src/main/java/SwordsGame/client/blocks/BlockScriptLoader.java`
- Регистрация блоков: `src/main/java/SwordsGame/client/blocks/BlockRegistry.java`
- Сборка Block DSL: `src/main/java/SwordsGame/client/assets/Syn.java`

---

## 2) Запуск debug-профиля

Отдельный debug fat-jar больше не нужен.

Debug-профиль включается аргументом:
- `--debug`
- или `debug`

Пример:
```bash
java -jar dot-ruby-xxxx.jar --debug
```

---

## 3) Система сцен (Session / State)

### Как устроены переходы
1. `Session` создает `SessionStateManager`.
2. Начальное состояние: `new MainMenuState()`.
3. Переходы делаются через `SessionCommands`:
   - `openMainMenu()`
   - `startScenario(boolean debugProfile)`
   - `exitApplication()`
4. `SessionStateManager` вызывает:
   - у старой сцены: `onExit(nextState)`
   - у новой сцены: `onEnter(context)`

### Где регистрировать свою сцену
- Добавьте новый класс, реализующий `SessionState`.
- Добавьте путь перехода в реализацию `SessionCommands` в `Session.java`.
- Вызывайте переход из текущей сцены (обычно в `update()`).

Шаблон:
```java
public class MyState implements SessionState {
    @Override public void onEnter(SessionContext context) {}
    @Override public void onExit(SessionState nextState) {}
    @Override public void update() {}
    @Override public void render() {}
}
```

---

## 4) HUD Groovy: полный формат

Корень `hud-ui.groovy` — это `Map`:

```groovy
[
  base: { ctx -> ... },
  dialogs: [
    'dialog.id': { ctx -> ... }
  ]
]
```

### Контекст `ctx`
Из Java в скрипт приходят:
- `ctx.primaryButtonText`
- `ctx.state` (данные из `hud.putUiState(key, value)`)

Удобный helper:
```groovy
def S = { ctx -> (ctx?.state ?: [:]) as Map }
```

---

## 5) Синтаксис элементов UI

> Парсинг делает `HudScriptRunner`, поэтому ниже — реальные ключи и дефолты.

### 5.1 `base.sprites`

Пример:
```groovy
[texture: 'char-frame', pivot: 'screen.left.top', alignX: 'LEFT', alignY: 'TOP', x: 0, y: 18, scale: 2.0]
```

Параметры:
- `texture` (String) — alias текстуры, загруженный в `Hud`.
- `pivot` (String) — id pivot-точки.
- `alignX` (String) — `LEFT | CENTER | RIGHT` (дефолт: `LEFT`).
- `alignY` (String) — `TOP | CENTER | BOTTOM | CENTER_Y` (`CENTER_Y` нормализуется в `CENTER`; дефолт: `TOP`).
- `x` (Number) — offset X (дефолт: `0`).
- `y` (Number) — offset Y (дефолт: `0`).
- `scale` (Number) — масштаб (дефолт: `1.0`).

### 5.2 `base.texts` и `dialogs.<id>.texts`

Пример:
```groovy
[text: '^2Hello', pivot: 'screen.left.top', alignX: 'LEFT', alignY: 'TOP', x: 10, y: 2, scale: 1.0]
```

Параметры:
- `text` (String) — строка (поддерживает цветовые коды формата проекта).
- `pivot`, `alignX`, `alignY`, `x`, `y`, `scale` — как у спрайтов.

### 5.3 `base.buttons` и `dialogs.<id>.buttons`

Пример:
```groovy
[id:'start-session', label:'start', alignX:'CENTER', alignY:'TOP', x:0, y:108, width:200, height:28, scale:1.0, active:true]
```

Параметры:
- `id` (String) — event id кнопки (обязательно обработать в Java).
- `label` (String) — текст кнопки.
- `pivot` (String) — pivot-id (если не задан, используется диалог/anchor-контекст).
- `alignX` / `alignY` — выравнивание точки привязки.
- `x`, `y` — смещение.
- `width`, `height` — размер (дефолт `100x28`).
- `scale` — масштаб текста (дефолт `1.0`).
- `active` (Boolean) — активность кнопки (дефолт `true`).

---

## 6) Pivot-точки и привязка

Стандартные pivots регистрируются через:
```java
HudLayoutRegistry.registerDefaultPivots(hud);
```

По умолчанию есть:
- `screen.left.top`
- `screen.center`
- `screen.bottom.center`
- `screen.right.center`
- `debug.info.dialog`
- `menu.dialog`

Добавить свой pivot:
```java
hud.setPivot("my.pivot", Anchor.CENTER, Anchor.BOTTOM, 0, -12);
```

---

## 7) Диалоги: где открывать и как настраивать

### Открыть/закрыть через layout-id
1. Подготовить контент:
```java
hud.applyDialogLayout("my.dialog.id");
```
2. Показать/скрыть:
```java
hud.toggleDialogAtPivot(
    "", "screen.center",
    Anchor.CENTER, Anchor.CENTER_Y,
    0, 0, 360, 220,
    Dialog.SelectionBlockMode.NONE
);
```

### Параметры `toggleDialogAtPivot(...)`
- `body` — fallback-текст в диалоге.
- `pivotId` — точка привязки.
- `alignX`, `alignY` — как позиционировать rect диалога относительно pivot.
- `x`, `y` — смещение.
- `width`, `height` — размер.
- `blockMode`:
  - `NONE` — диалог не блокирует selection.
  - `FULL_SCREEN` — selection блокируется, пока диалог видим.

### Прозрачность диалога
```java
hud.setDialogOpacity(fillAlpha, borderAlpha); // 0..1
hud.resetDialogOpacity();
```

---

## 8) Кнопки: обработка событий в сценах

### Base-кнопки
Для базового HUD-слоя:
```java
if (hud.consumeBaseButtonClick("primary-button", mouseDown)) {
    // action
}
```

### Кнопки диалога
```java
String action = hud.pollDialogButtonClick(mouseDown);
if (action != null) {
    switch (action) {
        case "start-session" -> ...
        case "exit-app" -> ...
    }
}
```

> Любой `id` из Groovy должен быть обработан в `update()` соответствующей сцены.

---

## 9) Глобальная надпись загрузки

Управление:
```java
hud.setGlobalLoadingText("loading");
hud.setGlobalLoadingVisible(true);
```

Сейчас рисуется у `screen.bottom.center` с нижним отступом `10`.

---

## 10) Как добавить новый спрайт-алиас

В `Hud` добавьте загрузку:
```java
loadAliased("my-icon", "ui/my-icon.png");
```

После этого в Groovy можно писать `texture: 'my-icon'`.

---

## 11) Блоки: актуальный script-first синтаксис

Файл: `shared/blocks/blocks.groovy` — это список блоков:

```groovy
[
  [
    type : 'COBBLE',
    tex  : [one: 'blocks/cobble.png'],
    props: [randomColorShift: 0.4, destructible: true, hardness: 1.0]
  ]
]
```

### Параметры блока
- `type` (String, обязателен)
  - должен совпадать с `Type` enum (`COBBLE`, `GRASS`, `STONE`, ...)
  - `AIR` игнорируется загрузчиком

- `tex` (Map)
  - вариант 1: `one: 'path'`
  - вариант 2: `top`, `bottom`, `side`

- `props` (Map, опционально)
  - `randomRotation: true`
  - `randomColor: true`
  - `randomColorShift: <Number>` (используется вместе с `randomColor`)
  - `emission: true`
  - `transparent: true`
  - `nonSolid: true`
  - `smoothing: true`
  - `destructible: true`
  - `surfaceOnly: true`
  - `hardness: <Number>`

### Что происходит при ошибках
- Невалидный `type` пропускается.
- Если скрипт не загрузился/пустой, `BlockRegistry` применит встроенный fallback (COBBLE/GRASS/STONE).

---

## 12) Где регистрировать что именно

- **Новые HUD элементы/диалоги/кнопки/надписи/спрайты**:
  - описывать в `hud-ui.groovy`
  - обработку `id` кнопок — в `update()` конкретной сцены (`MainMenuState` / `SessionScenarioState` / ваша сцена)

- **Новые pivots и стиль открытия диалогов**:
  - в коде сцены при `onEnter(...)` (или в `HudLayoutRegistry` для глобальных точек)

- **Новые блоки**:
  - в `shared/blocks/blocks.groovy`
  - и обязательно соответствующий `Type` в клиентском enum/протоколе

- **Новая сцена/сценарий**:
  - новый класс `SessionState`
  - новый переход через `SessionCommands`/`SessionStateManager`

---

## 13) Мини-чеклист перед релизом

- Все новые `button.id` обрабатываются в state `update()`.
- Все `pivot` id, используемые в Groovy, реально зарегистрированы.
- Для новых блоков есть текстуры в `src/main/resources/blocks/`.
- `./gradlew compileJava` проходит.
- `./gradlew shadowJar` проходит.
