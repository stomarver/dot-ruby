# Modding Guide (UI / Dialogs / Blocks / Session States)

This guide describes the **current** `.ruby` syntax and integration flow.

---

## 1) Where to edit what

### UI / HUD
- HUD script: `src/main/resources/shared/ui/hud/hud-ui.groovy`
- Script evaluator: `src/main/java/SwordsGame/client/ui/HudScriptRunner.java`
- HUD runtime/render/input: `src/main/java/SwordsGame/client/ui/Hud.java`
- Dialog container/runtime: `src/main/java/SwordsGame/client/ui/Dialog.java`
- Default pivots registry: `src/main/java/SwordsGame/client/ui/HudLayoutRegistry.java`

### Session / scenes
- Entrypoint and `--debug` parsing: `src/main/java/SwordsGame/client/core/Session.java`
- Scene contract: `src/main/java/SwordsGame/client/core/SessionState.java`
- Transition manager: `src/main/java/SwordsGame/client/core/SessionStateManager.java`
- Main menu scene: `src/main/java/SwordsGame/client/core/MainMenuState.java`
- Scenario scene: `src/main/java/SwordsGame/client/core/SessionScenarioState.java`

### Blocks
- Block script definitions: `src/main/resources/shared/blocks/blocks.groovy`
- Block script loader: `src/main/java/SwordsGame/client/blocks/BlockScriptLoader.java`
- Block registry/fallback: `src/main/java/SwordsGame/client/blocks/BlockRegistry.java`
- Block DSL builder: `src/main/java/SwordsGame/client/assets/Syn.java`

---

## 2) Debug profile launch

A separate debug fat-jar is no longer required.

Use either argument:
- `--debug`
- `debug`

Example:
```bash
java -jar dot-ruby-xxxx.jar --debug
```

---

## 3) Session state model

### Transition lifecycle
1. `Session` creates `SessionStateManager`.
2. Initial scene is `MainMenuState`.
3. Scene changes are requested through `SessionCommands`:
   - `openMainMenu()`
   - `startScenario(boolean debugProfile)`
   - `exitApplication()`
4. Manager applies transition in this order:
   - current scene: `onExit(nextState)`
   - next scene: `onEnter(context)`

### Adding a new scene
- Create a class implementing `SessionState`.
- Add a transition route in `Session` (inside `SessionCommands` implementation).
- Trigger it from your active scene (`update()` in most cases).

Template:
```java
public class MyState implements SessionState {
    @Override public void onEnter(SessionContext context) {}
    @Override public void onExit(SessionState nextState) {}
    @Override public void update() {}
    @Override public void render() {}
}
```

---

## 4) HUD Groovy root schema

`hud-ui.groovy` must return a root `Map`:

```groovy
[
  base: { ctx -> ... },
  dialogs: [
    'dialog.id': { ctx -> ... }
  ]
]
```

### Runtime context (`ctx`)
Fields currently exposed from Java:
- `ctx.primaryButtonText`
- `ctx.state` (data written by `hud.putUiState(key, value)`)

Common helper:
```groovy
def S = { ctx -> (ctx?.state ?: [:]) as Map }
```

---

## 5) UI element syntax

> Parsed by `HudScriptRunner`, so keys/defaults below match the runtime parser.

### 5.1 `base.sprites`

Example:
```groovy
[texture: 'char-frame', pivot: 'screen.left.top', alignX: 'LEFT', alignY: 'TOP', x: 0, y: 18, scale: 2.0]
```

Parameters:
- `texture` (String): texture alias loaded in `Hud`.
- `pivot` (String): pivot id.
- `alignX` (String): `LEFT | CENTER | RIGHT` (default `LEFT`).
- `alignY` (String): `TOP | CENTER | BOTTOM | CENTER_Y` (`CENTER_Y` is normalized to `CENTER`; default `TOP`).
- `x` (Number): X offset (default `0`).
- `y` (Number): Y offset (default `0`).
- `scale` (Number): draw scale (default `1.0`).

### 5.2 `base.texts` and `dialogs.<id>.texts`

Example:
```groovy
[text: '^2Hello', pivot: 'screen.left.top', alignX: 'LEFT', alignY: 'TOP', x: 10, y: 2, scale: 1.0]
```

Parameters:
- `text` (String): display string (project color-codes supported).
- `pivot`, `alignX`, `alignY`, `x`, `y`, `scale`: same behavior as sprites.

### 5.3 `base.buttons` and `dialogs.<id>.buttons`

Example:
```groovy
[id:'start-session', label:'start', alignX:'CENTER', alignY:'TOP', x:0, y:108, width:200, height:28, scale:1.0, active:true]
```

Parameters:
- `id` (String): button event id (must be handled in Java scene logic).
- `label` (String): button label.
- `pivot` (String): pivot id (optional; uses dialog/anchor context when absent).
- `alignX` / `alignY`: local anchor alignment.
- `x`, `y`: offsets.
- `width`, `height`: size (defaults `100x28`).
- `scale`: text scale (default `1.0`).
- `active` (Boolean): enabled state (default `true`).

---

## 6) Pivots and anchoring

Default pivots are registered through:
```java
HudLayoutRegistry.registerDefaultPivots(hud);
```

Built-in pivot ids:
- `screen.left.top`
- `screen.center`
- `screen.bottom.center`
- `screen.right.center`
- `debug.info.dialog`
- `menu.dialog`

Register custom pivot:
```java
hud.setPivot("my.pivot", Anchor.CENTER, Anchor.BOTTOM, 0, -12);
```

---

## 7) Dialogs: open/close and parameters

### Open a dialog from layout id
1. Apply layout:
```java
hud.applyDialogLayout("my.dialog.id");
```
2. Toggle dialog visibility:
```java
hud.toggleDialogAtPivot(
    "", "screen.center",
    Anchor.CENTER, Anchor.CENTER_Y,
    0, 0, 360, 220,
    Dialog.SelectionBlockMode.NONE
);
```

### `toggleDialogAtPivot(...)` arguments
- `body`: fallback plain text.
- `pivotId`: pivot reference.
- `alignX`, `alignY`: dialog rectangle anchor mode.
- `x`, `y`: offsets.
- `width`, `height`: rectangle size.
- `blockMode`:
  - `NONE`: dialog does not block selection.
  - `FULL_SCREEN`: selection is blocked while dialog is visible.

### Dialog opacity
```java
hud.setDialogOpacity(fillAlpha, borderAlpha); // range: 0..1
hud.resetDialogOpacity();
```

---

## 8) Button handling in scenes

### Base HUD buttons
```java
if (hud.consumeBaseButtonClick("primary-button", mouseDown)) {
    // action
}
```

### Dialog buttons
```java
String action = hud.pollDialogButtonClick(mouseDown);
if (action != null) {
    switch (action) {
        case "start-session" -> ...
        case "exit-app" -> ...
    }
}
```

Every `id` declared in Groovy should be handled in the owning scene `update()` logic.

---

## 9) Global loading text

Control API:
```java
hud.setGlobalLoadingText("loading");
hud.setGlobalLoadingVisible(true);
```

Current placement: `screen.bottom.center` with a 10px bottom offset.

---

## 10) Adding a sprite alias

In `Hud`, register texture alias:
```java
loadAliased("my-icon", "ui/my-icon.png");
```

Then use it in Groovy:
```groovy
[texture: 'my-icon', ...]
```

---

## 11) Blocks: current script-first format

`shared/blocks/blocks.groovy` returns a list:

```groovy
[
  [
    type : 'COBBLE',
    tex  : [one: 'blocks/cobble.png'],
    props: [randomColorShift: 0.4, destructible: true, hardness: 1.0]
  ]
]
```

### Block keys
- `type` (String, required)
  - Must match `Type` enum (`COBBLE`, `GRASS`, `STONE`, ...)
  - `AIR` is ignored by loader

- `tex` (Map)
  - Option A: `one: 'path'`
  - Option B: `top`, `bottom`, `side`

- `props` (Map, optional)
  - `randomRotation: true`
  - `randomColor: true`
  - `randomColorShift: <Number>` (used with `randomColor`)
  - `emission: true`
  - `transparent: true`
  - `nonSolid: true`
  - `smoothing: true`
  - `destructible: true`
  - `surfaceOnly: true`
  - `hardness: <Number>`

### Failure behavior
- Invalid `type` rows are skipped.
- If the script is missing/invalid/empty, `BlockRegistry` falls back to built-in defaults (COBBLE/GRASS/STONE).

---

## 12) Where to register new things

- **HUD elements / dialogs / buttons / text / sprites**
  - Define in `hud-ui.groovy`
  - Handle button ids in scene `update()` (`MainMenuState`, `SessionScenarioState`, or your custom scene)

- **New pivots and dialog opening presets**
  - Register in scene `onEnter(...)`
  - Or centralize global pivots in `HudLayoutRegistry`

- **New blocks**
  - Define in `shared/blocks/blocks.groovy`
  - Ensure matching `Type` enum/protocol id exists

- **New scene/scenario state**
  - Implement `SessionState`
  - Wire transition through `SessionCommands` and `SessionStateManager`
