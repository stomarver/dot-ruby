# DotRuby Groovy/UI/Session Modding Guide

Этот гайд описывает, как добавлять **диалоги, спрайты, текст, кнопки, состояния (сессии/меню)** и как прокидывать переменные в Groovy.

---

## 1) Где что находится

- UI-скрипт (декларативный):
  - `src/main/resources/shared/ui/hud/hud-ui.groovy`
- Исполнитель Groovy UI-скрипта:
  - `src/main/java/SwordsGame/client/ui/HudScriptRunner.java`
- Универсальный HUD-слой и рендер:
  - `src/main/java/SwordsGame/client/ui/Hud.java`
- Состояния приложения:
  - `src/main/java/SwordsGame/client/core/Session.java`
  - `src/main/java/SwordsGame/client/core/SessionStateManager.java`
  - `src/main/java/SwordsGame/client/core/MainMenuState.java`
  - `src/main/java/SwordsGame/client/core/SessionScenarioState.java`
- Блоки (пока Java-регистрация):
  - `src/main/java/SwordsGame/client/blocks/BlockRegistry.java`
  - `src/main/java/SwordsGame/client/assets/Syn.java`

---

## 2) Формат `shared/ui/hud/hud-ui.groovy`

Корень скрипта — это `Map`:

```groovy
[
  base: { ctx -> ... },
  dialogs: [
    'main.menu': { ctx -> ... },
    'session.pause': { ctx -> ... }
  ]
]
```

### Контекст `ctx` (из Java)

Доступные поля:

- `ctx.primaryButtonText` — подпись primary-кнопки.
- `ctx.state` — произвольный map состояния (`hud.putUiState("k", v)`).

---

## 3) Как добавить спрайт / текст / кнопку

### Спрайты

В `base.sprites` добавьте map:

```groovy
[texture: 'char-frame', pivot: 'screen.left.top', alignX: 'LEFT', alignY: 'TOP', x: 0, y: 18, scale: 2.0]
```

Поля:
- `texture`: alias текстуры (должен быть загружен в `Hud`)
- `pivot`: id опорной точки
- `alignX`: `LEFT|CENTER|RIGHT`
- `alignY`: `TOP|CENTER|BOTTOM`
- `x`,`y`: offset
- `scale`: масштаб

### Текст

В `base.texts` или `dialogs.<id>.texts`:

```groovy
[text: 'Hello', alignX: 'CENTER', alignY: 'TOP', x: 0, y: 20, scale: 1.0]
```

### Кнопка

В `dialogs.<id>.buttons` (или в `base.buttons`):

```groovy
[id:'start-session', label:'start', alignX:'CENTER', alignY:'TOP', x:0, y:100, width:200, height:28, scale:1.0, active:true]
```

`id` должен обрабатываться в Java (switch-case в state-классе).

---

## 4) Как добавить новый диалог

1. В `hud-ui.groovy` добавить новый key в `dialogs`, например `"settings.menu"`.
2. В Java вызвать:

```java
hud.applyDialogLayout("settings.menu");
hud.toggleDialogAtPivot("", "screen.center", Anchor.CENTER, Anchor.CENTER_Y, 0, 0, 360, 220, Dialog.SelectionBlockMode.NONE);
```

3. В обработчике кнопок (`pollDialogButtonClick`) добавить `case` под ваши `id`.

---

## 5) Как передавать переменные в Groovy

В Java:

```java
hud.putUiState("debugMode", true);
hud.putUiState("showRendering", showRenderingBlock);
```

В Groovy:

```groovy
def state = (ctx?.state ?: [:]) as Map
active: (state.debugMode == true)
```

---

## 6) Как добавить новое состояние (Session / Menu / Overlay)

1. Создать класс, реализующий `SessionState`:
- `onEnter(SessionContext)`
- `update()`
- `render()`
- `onExit()`

2. Для переходов использовать `SessionCommands` из `SessionContext`:

```java
context.getCommands().openMainMenu();
context.getCommands().startScenario(false);
context.getCommands().exitApplication();
```

3. В `Session.java` начальное состояние устанавливается через:

```java
stateManager.changeState(new MainMenuState());
```

---

## 7) Как добавить новый пункт меню

### В Groovy
Добавьте кнопку в `dialogs['main.menu'].buttons`:

```groovy
btn('open-settings', 'settings', 'CENTER', 'TOP', 0, 176, 200, 28)
```

### В Java (`MainMenuState.update`)

```java
case "open-settings" -> {
    hud.applyDialogLayout("settings.menu");
    hud.toggleDialogAtPivot("", "menu.dialog", Anchor.CENTER, Anchor.CENTER_Y, 0, 0, 360, 220, Dialog.SelectionBlockMode.NONE);
}
```

---

## 8) Как добавить новый HUD alias-спрайт

В `Hud` в конструкторе (рядом с `char-frame`/`separator`):

```java
loadAliased("my-icon", "ui/my-icon.png");
```

После этого `texture: 'my-icon'` доступен из Groovy.

---

## 9) Как добавлять блоки (текущий pipeline)

Сейчас блоки регистрируются Java-кодом в `BlockRegistry.init()`.

Пример:

```java
reg(Type.STONE,
    Syn.blk(Type.STONE)
       .tex(Paths.BLOCK_STONE)
       .props(p -> p.surfaceOnly().hardness(1.5f))
       .build());
```

### Что нужно для нового блока
1. Добавить id в shared block id enum/константы.
2. Добавить `Type` в `client.blocks.Type`.
3. Добавить ресурс текстуры в `src/main/resources/blocks`.
4. Зарегистрировать блок в `BlockRegistry.init()`.

> Важно: для блоков пока нет runtime Groovy-лоадера. Это осознанно, чтобы не ломать бинарную совместимость сетевого формата block id.

---

## 10) Простой стиль Groovy (рекомендованный)

Используйте локальные helper-closure, как в текущем `hud-ui.groovy`:

- `S(ctx)` — безопасный доступ к state
- `btn(...)` — единый конструктор кнопок

Это делает скрипт компактным и модульным, без дублирования map-структур.

---

## 11) Чеклист перед коммитом

- [ ] Нет scale ниже `1.0` в UI-тексте.
- [ ] Все новые button `id` обработаны в Java `switch`.
- [ ] `./gradlew compileJava` проходит.
- [ ] Если меняли упаковку: `./gradlew shadowJar` проходит.

