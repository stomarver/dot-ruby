# RTS readiness review (AoM-like direction) + JOML migration scope

## Что уже есть
- Воксельный мир/чанки, LOD, базовый рендер и камера.
- Базовые RTS-данные фракций/юнитов/технологий на серверной стороне.

## Чего не хватает до "полноценной RTS"
1. **Симуляция и детерминизм**
   - Единый fixed-tick серверный simulation loop.
   - Командная модель (order queue), deterministic lockstep/replay.
2. **Юниты и pathfinding**
   - Grid/nav-field pathfinding (A*/flow field), групповое движение, avoidance.
3. **Экономика/строительство**
   - Рабочие, сбор ресурсов, доставка, очереди производства, размещение строений.
4. **Бой и способности**
   - Attack-move, агро-логика, cooldowns, projectiles/AoE, armor/damage types.
5. **Туман войны и разведка**
   - Visibility per faction, memory/shroud, разведданные.
6. **Сетевая модель RTS**
   - Авторитет сервера либо lockstep + анти-десинхронизация.
7. **UX RTS-уровня**
   - Горячие клавиши, control groups, minimap, production panel, command preview.

## JOML migration: что именно переписано в этом шаге
- Подключён JOML (`org.joml:joml`).
- `Camera`:
  - позиция и направление движения переведены на `Vector2f`.
  - движение/edge-scroll/strafe теперь векторные операции (`fma`) вместо ручной тригонометрии в каждой ветке.
- `Renderer`:
  - направление солнца переведено на `Vector3f` + `normalize()`.
- `World`:
  - расчёт фокуса чанка и LOD distance переведены на `Vector2f`.

## Следующие шаги полной миграции математики
1. Вынести общий `MathContext` (векторы/матрицы/пулы временных объектов).
2. Перевести трансформации камеры/мира на `Matrix4f` (убрать fixed-function стиль в перспективе).
3. Перевести меш-генерацию на типизированные векторы (`Vector3f` нормали/вершины).
4. Для gameplay: ввести `Vector2f/Vector3f` в movement/combat/pathfinding данные.
