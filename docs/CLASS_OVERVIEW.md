# DotRuby Class Overview

Этот документ описывает ключевые классы проекта и их ответственность.

## 1) Core loop / запуск

- `SwordsGame.client.core.Game`
  - Основной runtime-клиент (release entrypoint).
  - Создаёт окно, рендер, мир, камеру, HUD, курсор.
  - Держит главный цикл: input -> update -> world render -> UI render.

- `SwordsGame.client.core.Dbg`
  - Debug-entrypoint.
  - Поверх обычного цикла добавляет debug overlays/переключатели.

- `SwordsGame.client.core.Win`
  - GLFW-окно, FBO, input, виртуальное разрешение, мышь, clamp.
  - Отдаёт mouse coords/scroll, управляет fullscreen и кадром.

## 2) World / camera

- `SwordsGame.client.Cam`
  - Камера RTS: edge-scroll, zoom, rotation, clamp внутри world bounds.

- `SwordsGame.client.Wld`
  - Клиентский рендер мира по чанкам, LOD/culling, bounds/debug draw.

- `SwordsGame.server.ChMgr`
  - Серверный/симуляционный менеджер чанков мира.

- `SwordsGame.server.Chk`
  - Данные конкретного чанка (блоки и операции доступа).

- `SwordsGame.server.Ter`
  - Генерация террейна (наполнение чанков).

## 3) Graphics / rendering pipeline

- `SwordsGame.client.graphics.Rdr`
  - Общий рендер-контекст: viewport, setup 3D/2D, lighting, fog delegation.

- `SwordsGame.client.graphics.FogFx`
  - Screen-space fog шейдер и математика fog depth range.
  - Реагирует на zoom через `setZoom(...)`.

- `SwordsGame.client.graphics.MshBld`
  - Строит геометрию чанка в CPU-буферы (`FCol`) с учётом face visibility.

- `SwordsGame.client.graphics.MshBuf`
  - VBO-обёртка: upload и draw массива вершин.

- `SwordsGame.client.graphics.ChkMsh`
  - Набор меш-буферов чанка (opaque/transparent/emissive).

- `SwordsGame.client.graphics.BlkRdr`
  - Непосредственный рендер одиночного блока (immediate/render helper).

- `SwordsGame.client.graphics.Spr`
  - 2D sprite helper для UI.

- `SwordsGame.client.graphics.Font`
  - Шрифт-атлас, метрики символов/диакритики.

- `SwordsGame.client.graphics.FCol`
  - Динамический float-коллектор вершинных данных.

## 4) Blocks / block data

- `SwordsGame.client.blocks.Type`
  - Enum типов блоков (id + display/meta).

- `SwordsGame.client.blocks.Reg`
  - Реестр блоков и их регистрация в `init()`.
  - Доступ к блоку по id/type, destroy lifecycle.

- `SwordsGame.client.graphics.Blk`
  - Базовый класс block definition: текстуры, uv, свойства, draw/destroy.

- `SwordsGame.client.graphics.BlkProps`
  - Fluent-параметры блока: transparent, random rotation/color, hardness, smoothing.

- `SwordsGame.client.blocks.Cob`, `Grs`, `Stn`
  - Конкретные типы/пресеты блоков.

## 5) UI layer

- `SwordsGame.client.ui.Hud`
  - UI-контейнер кадра: рамки, панели, кнопки, server/camera info.

- `SwordsGame.client.ui.Txt`
  - Рисование текста (цветовые коды, wave/shake/crit эффекты, выравнивание).

- `SwordsGame.client.ui.Btn`
  - Примитивные UI-кнопки, hover/click hit test.

- `SwordsGame.client.ui.Cur`
  - Спрайтовый курсор.

- `SwordsGame.client.ui.SelBox`
  - Прямоугольник выделения (drag selection).

- `SwordsGame.client.ui.SelArea`
  - Геометрия допустимой зоны выделения/кламп мыши.

- `SwordsGame.client.ui.Anc`
  - Anchoring enum/value для позиционирования UI.

- `SwordsGame.client.ui.Inf`, `Msg`
  - Блоки информации и сообщений HUD.

## 6) Assets / content DSL / resources

- `SwordsGame.client.assets.Paths`
  - Константы путей к встроенным ресурсам (блоки, UI, шрифт).

- `SwordsGame.client.assets.Syn`
  - Единый DSL для деклараций контента:
    - `Syn.blk(...)`
    - `Syn.img(...)`
    - `Syn.mdl(...)`

- `SwordsGame.client.graphics.TexLd`
  - Загрузка текстур через STB, cache, mipmaps/anisotropy, cleanup.

- `SwordsGame.client.graphics.ImgReg`
  - Регистрация/доступ изображений из `Syn.img(...)`.

- `SwordsGame.client.graphics.GltfModel`
  - Минимальный glTF/GLB loader (без доп. зависимостей).

- `SwordsGame.client.graphics.MdlReg`
  - Реестр моделей: регистрация glTF/GLB из `Syn.mdl(...)`.

## 7) Server gameplay templates

- `SwordsGame.server.gameplay.RtsTemplates`
  - Наборы шаблонов юнитов/строений/технологий по фракциям.

- `SwordsGame.server.gameplay.FTech`
  - Tech-tree фракции (units/buildings/tech).

- `SwordsGame.server.gameplay.UTemp`, `BTemp`, `TTemp`
  - Шаблоны юнитов, зданий и технологий.

- `SwordsGame.server.gameplay.UStat`, `ResPack`
  - Статы юнитов, ресурсные bundles/стоимости.

- Enums: `Fac`, `Age`, `URole`, `BRole`, `Res`, `Cbt`
  - Базовые типы RTS-домена.

## 8) Shared UI protocol

- `SwordsGame.shared.protocol.ui.UiFrm`
  - Состояние UI-кадра (набор панелей).

- `SwordsGame.shared.protocol.ui.UiPan`
  - Состояние одной панели (id/text и т.п.).

## 9) Utils

- `SwordsGame.client.utils.Disc`
  - Discord Rich Presence bootstrap/update.

- `SwordsGame.client.utils.Shot`
  - Скриншоты в локальную папку пользователя.
