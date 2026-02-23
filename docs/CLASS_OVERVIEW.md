# DotRuby Class Overview (Full Map)

Документ описывает **все ключевые классы** по слоям: за что отвечают, где находятся и как связаны.

> Примечание по неймингу: в коде уже есть частичный переход к более однородному стилю (особенно в UI Dialog/HUD API), но полная унификация имён по **всему** проекту — это большой отдельный этап, который требует пакетного переименования файлов/классов + миграции импортов.

---

## 1. Client Entrypoints / Runtime

### `SwordsGame.client.core.Game`
Release runtime-клиент.
- Инициализация окна, рендера, мира, камеры, HUD.
- Главный цикл кадра: input → update → world render → UI render.
- Поддержка диалога, selection box и курсора.

### `SwordsGame.client.core.Debug`
Debug runtime-клиент.
- Наследует общий цикл от архитектуры `Game`.
- Добавляет debug-панели, debug-dialog toggles, отладочные метрики (FPS/fog/memory/time).

### `SwordsGame.client.core.Window`
Низкоуровневый слой GLFW/OpenGL окна.
- Создание/уничтожение контекста.
- FBO/виртуальное разрешение.
- Screen/UI координаты мыши, clamp.
- Fullscreen, frame update, swap.

---

## 2. Client World / Camera

### `SwordsGame.client.Camera`
RTS-камера.
- Pan/edge-scroll/zoom/rotation.
- Ограничение по world bounds.
- Преобразования для 3D сцены.

### `SwordsGame.client.World`
Клиентский рендер мира.
- Подготовка/кэш чанковых мешей.
- Culling, LOD-подобный отбор чанков.
- Рендер границ чанков (debug).

### `SwordsGame.client.FallingBlock`
Вспомогательный клиентский класс, исторически связанный с block-логикой/экспериментами.

### `SwordsGame.client.TopFaceSmoothing`
Технический/временный класс-заглушка.

---

## 3. Server World / Simulation

### `SwordsGame.server.Chunk`
Данные одного чанка (блоки, доступ/обновление).

### `SwordsGame.server.ChunkManager`
Менеджер сетки чанков.
- Создание/поиск чанков.
- Размер мира, границы, генерация через `Terrain`.

### `SwordsGame.server.Terrain`
Генерация террейна для чанков.

### `SwordsGame.server.DayNightCycle`
Симуляция времени суток.
- Day/clock/phase.
- Параметры для fog/tint UI.

### `SwordsGame.server.ui.ServerUiComposer`
Формирование server-side UI frame (панели/текст) в shared protocol.

---

## 4. Graphics Pipeline

### `SwordsGame.client.graphics.Renderer`
Оркестратор рендера.
- setup 3D/2D.
- Свет/солнце/tint.
- Fog интеграция через `FogFx`.

### `SwordsGame.client.graphics.FogFx`
Screen-space fog pass (shader + uniforms).

### `SwordsGame.client.graphics.MeshBuilder`
CPU-сборка геометрии чанков.
- Формирует буферы вершин/uv/normal.

### `SwordsGame.client.graphics.FloatCollector`
Динамический массив float для сборки меша.

### `SwordsGame.client.graphics.MeshBuffer`
GPU-буфер (VBO/VAO lifecycle).

### `SwordsGame.client.graphics.ChunkMesh`
Контейнер чанковых буферов (opaque/transparent/etc).

### `SwordsGame.client.graphics.BlockRenderer`
Рендер helper для одиночных блоков.

### `SwordsGame.client.graphics.BlockColorPipeline`
Логика варьирования/tint цвета блоков.

### `SwordsGame.client.graphics.Block`
Дефиниция блока (текстуры/props/draw helpers).

### `SwordsGame.client.graphics.BlockProps`
Параметры поведения/визуала блока (fluent style).

### `SwordsGame.client.graphics.Sprite`
2D sprite draw helper.

### `SwordsGame.client.graphics.Font`
Bitmap-font атлас + glyph metadata.

### `SwordsGame.client.graphics.TexLoad`
Загрузка/кэш текстур (STB/OpenGL).
- `TexLoad.Texture` — metadata texture handle.

### `SwordsGame.client.graphics.ImgReg`
Registry изображений.

### `SwordsGame.client.graphics.GltfModel`
Минимальный glTF/glb loader.
- `Primitive` и внутренний `Json` parser.

### `SwordsGame.client.graphics.MdlReg`
Registry моделей.
- `MdlReg.Ent` — зарегистрированная сущность модели + scale.

---

## 5. Blocks Layer

### `SwordsGame.client.blocks.Type`
Enum идентификаторов блока.

### `SwordsGame.client.blocks.BlockRegistry`
Registry блоков клиента.
- `init()/destroy()` lifecycle.
- lookup по id/type.

---

## 6. Assets / Declarative Syntax

### `SwordsGame.client.assets.Paths`
Константы путей ресурсов.

### `SwordsGame.client.assets.Syn`
Unified DSL для декларации:
- `Syn.img(...)`
- `Syn.mdl(...)`
- `Syn.blk(...)`

Вложенные классы:
- `Syn.Img`
- `Syn.Mdl`
- `Syn.BlkDef`

---

## 7. UI Layer

### `SwordsGame.client.ui.Hud`
Главный UI-композитор кадра.
- Базовый HUD.
- Overlay dialog pass.
- Кнопки, info-панели, сообщения.

### `SwordsGame.client.ui.Dialog`
Система диалогов/оверлеев.
- Anchor-based layout.
- Selection block modes.
- Text/Button slots.
- Active/dimmed кнопки.
- fill/border opacity.

### `SwordsGame.client.ui.Button`
UI button primitive.
- Hit-test.
- Absolute draw.
- Dimmed state draw.

### `SwordsGame.client.ui.Text`
Текстовый рендер.
- Цвет-коды `^N`.
- Анимации: `Shake`, `Wave`, `Crit`.
- Выравнивание/anchor.

### `SwordsGame.client.ui.Info`
Debug info панели (card-style).

### `SwordsGame.client.ui.Message`
Временные сообщения поверх HUD.
- `Message.Entry` внутренний тип сообщения.

### `SwordsGame.client.ui.Cursor`
Спрайтовый курсор.

### `SwordsGame.client.ui.SelectionBox`
Прямоугольник выделения drag-select.

### `SwordsGame.client.ui.SelectionArea`
Геометрия разрешённой зоны выделения.

### `SwordsGame.client.ui.Anchor`
Anchor enums + anchor point data.

---

## 8. Utilities

### `SwordsGame.client.utils.Discord`
Discord Rich Presence integration.

### `SwordsGame.client.utils.Screenshot`
Скриншоты из фреймбуфера.

---

## 9. Gameplay Domain (Server)

### Core containers
- `TemplateBase`
- `WorldObjectTemplate`

### Definitions
- `UnitTemplate`
- `BuildingTemplate`
- `TechnologyTemplate`
- `ResourceNodeTemplate`

### Stats / costs
- `UnitStats`
- `ResourceBundle`

### Trees / packs / registry
- `FactionTechTree`
- `RtsTemplates`
- `MythicCorePack`
- `MythicFactionPack`
- `GameplayRegistry`
- `GameplaySyn` (DSL)

### Enums
- `Age`
- `FactionType`
- `CombatType`
- `ResourceType`
- `UnitRole`
- `BuildingRole`
- `ObjectRole`

### GameplaySyn nested defs
- `DefBase<T>`
- `UnitDef`
- `BuildingDef`
- `TechDef`
- `ResourceDef`
- `ObjectDef`

---

## 10. Shared Protocol

### `SwordsGame.shared.protocol.ui.UiFrameState`
Shared frame DTO для UI-панелей.

### `SwordsGame.shared.protocol.ui.UiPanelState`
Одна UI-панель (text payload).

### `SwordsGame.shared.world.BlockId`
Shared block id constants.

---

## 11. Что ещё нужно для «полной» унификации имен во всём проекте

Для действительно полного rename-прохода (всех классов/методов/полей) нужен отдельный migration этап:
1. Словарь целевых имён (old → new) по всем пакетам.
2. Массовое переименование файлов/классов/методов (IDE-safe refactor).
3. Compatibility layer (deprecated wrappers) на переходный период.
4. Обновление документации/примеров/мод-синтаксиса.
5. Полная проверка сборки + smoke runtime.

Этот этап должен идти отдельной серией PR, чтобы не получить массивный unreviewable diff.
