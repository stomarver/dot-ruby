# Naming Refactor Plan (Project-wide)

Цель: привести **все** классы/методы/поля к единому короткому и понятному стилю.

## Style baseline
- Классы: `PascalCase`, короткие, доменные (`World`, `Camera`, `Dialog`).
- Методы: `verbNoun` (`setLayout`, `drawAbsolute`, `updateCycle`).
- Булевы поля/методы: `is/has/can` (`isVisible`, `hasFocus`, `canSelect`).
- Сокращения: только общепринятые (`UI`, `FPS`, `ID`).

## Migration phases
1. **UI/Core** — уже частично сделано (`setLayout`, `resolveLocalAnchor`, `getHoveredButtonId`).
2. **Graphics** — выровнять helper/registry naming (`TexLoad`, `ImgReg`, `MdlReg` API).
3. **Gameplay** — единые глаголы для DSL и registry методов.
4. **Legacy/temporary classes** — удалить/переименовать (`TopFaceSmoothing`, `FallingBlock` done; remaining legacy classes next).
5. **Shared protocol DTO naming** — единая терминология frame/panel/state.

## Compatibility policy
- На каждом этапе сохранять старые имена через thin-wrapper методы.
- Помечать wrapper-методы как deprecated на следующем этапе.
- Удалять wrapper только после обновления мод-документов и примеров.

## Completion criteria
- Нет бессистемных сокращений/разнобоя в public API.
- Документация `CLASS_OVERVIEW.md` и `MODDING_SYNTAX.md` совпадает с текущим кодом.
- `./gradlew compileJava` проходит после каждого этапа.
