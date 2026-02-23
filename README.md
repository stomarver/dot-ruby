# .ruby

`Genre: RTS` • `Status: pre-alpha (core foundation)`

Рубилэнд больше не держится на мире и подношениях Великому Рубину. Осколки силы разлетелись по «Рудникам», и расы вступили в борьбу за выживание, власть и будущее эпох.

`.ruby` — это инди-RTS на Java/LWJGL с намеренно «сырой, живой» эстетикой ранних прототипов: пиксельная графика, минималистичный UI, атмосфера незавершённого, но настоящего мира.

---

## Быстрый старт

### Требования
- Java 17

### Сборка
```bash
./gradlew shadowJar
```

Готовый JAR появляется в `build/libs/dot-ruby-*/`.

### Запуск
```bash
java -jar build/libs/dot-ruby-*/dot-ruby-*.jar
```

Debug-профиль:
```bash
java -jar build/libs/dot-ruby-*/dot-ruby-*.jar --debug
```

---

## Что уже есть в игре

- Сессионная архитектура сцен: главное меню + игровая сцена
- 3D-мир на чанках (LWJGL/OpenGL)
- Камера: стрелки, edge-scroll, вращение, zoom, reset
- Day/Night цикл + туман и цветовой tint
- HUD/Dialogs на Groovy-скриптах
- Debug-панель с переключаемыми блоками
- Discord RPC
- Скриншоты в `~/Pictures/SwordsGame/`
- Ввод текста из терминала в игровой HUD

---

## Управление

### Базовые
- `F4` или `Alt+Enter` — fullscreen
- `F12` или `Ctrl+P` — screenshot
- `Esc` — закрыть приложение
- `Arrow Keys` или edge-scroll — движение камеры
- `Q / E` или колесо мыши — вращение
- `=` / `-` (или wheel zoom) — zoom
- `R` — сброс камеры

### Системные / Debug
- `F7` — Virtual/Real render mode
- `F8` — toggle debug info
- `B` — toggle chunk bounds
- `Numpad + / Numpad -` — debug fog distance
- `Y / U` — управление временем day/night (ускорение / обратное смещение)

---

## Контент и моддинг (текущее состояние)

- HUD, диалоги, кнопки, тексты, спрайты: `src/main/resources/shared/ui/hud/hud-ui.groovy`
- Блоки: `src/main/resources/shared/blocks/blocks.groovy`
- Полный гайд по актуальному синтаксису:
  - [`docs/ModdingGuide.md`](docs/ModdingGuide.md)

---

## Фракции (концепт проекта)

В дизайне заложены 3 фракции и 3 эпохи развития:
- **Люди** — универсальный баланс + сильная пехота/элита
- **Эльфы** — мобильность, дальний бой, гибкая экономика
- **Дворфы** — осада, прочность, техно-апгрейды

Экономика строится на 4 ресурсах:
- древесина
- минералы
- сырьё
- камень

Сейчас это активный дизайн-вектор, а не полностью реализованный gameplay-слой.

---

## Дорожная карта (кратко)

1. Базовые RTS-механики: selection, orders, pathfinding
2. Рабочие циклы: добыча → доставка → строительство
3. Юниты/здания и боевой loop
4. Переходы эпох и апгрейды
5. AI-оппонент и дальнейший сетевой слой

---

## Ссылки

- Discord: https://discord.gg/yXJgtqAfWe
- GitHub: https://github.com/stomarver/dot-ruby
- Screenshots: https://imgur.com/a/Bweq7sZ

---

Если ты Java/LWJGL разработчик и тебе близок «old-school indie RTS» вайб — проекту очень нужен соавтор.
