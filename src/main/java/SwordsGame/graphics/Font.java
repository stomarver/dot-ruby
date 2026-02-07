package SwordsGame.graphics;

import java.util.HashMap;
import java.util.Map;

public class Font {
    // 1. Меняем тип поля с int на Texture
    private final TextureLoader.Texture texture;

    // Эти значения теперь можно брать прямо из текстуры,
    // но оставляем поля, если они используются в расчетах
    private final int charWidth = 6, charHeight = 8, spacing = 1;

    private final CharData[] fastMap = new CharData[1200];
    private final Map<Character, DiacriticData> diacriticMap = new HashMap<>();

    public static class CharData {
        public final int x, y, advance;
        CharData(int x, int y, int advance) { this.x = x; this.y = y; this.advance = advance; }
    }

    public static class DiacriticData {
        public final char baseChar;
        public final int diacriticX, diacriticY, diacriticWidth, diacriticHeight, offsetY;

        DiacriticData(char b, int dx, int dy, int dw, int dh, int oy) {
            this.baseChar = b; this.diacriticX = dx; this.diacriticY = dy;
            this.diacriticWidth = dw; this.diacriticHeight = dh; this.offsetY = oy;
        }
    }

    public Font(String path) {
        // 2. Исправляем загрузку: сохраняем весь объект целиком
        this.texture = TextureLoader.loadTexture(path, true);
        initCharMap();
        initDiacritics();
    }

    // ... (методы initCharMap и initDiacritics без изменений) ...

    private void initCharMap() {
        // Латиница (Ряд 0-2)
        addChar('A', 0, 0, 5); addChar('a', 1, 0, 5); addChar('B', 2, 0, 5); addChar('b', 3, 0, 5);
        addChar('C', 4, 0, 5); addChar('c', 5, 0, 5); addChar('D', 6, 0, 5); addChar('d', 7, 0, 5);
        addChar('E', 8, 0, 5); addChar('e', 9, 0, 5); addChar('F', 10, 0, 5); addChar('f', 11, 0, 4);
        addChar('G', 12, 0, 5); addChar('g', 13, 0, 5); addChar('H', 14, 0, 5); addChar('h', 15, 0, 5);
        addChar('I', 16, 0, 4); addChar('i', 17, 0, 2);

        addChar('J', 0, 1, 5); addChar('j', 1, 1, 5); addChar('K', 2, 1, 5); addChar('k', 3, 1, 5);
        addChar('L', 4, 1, 5); addChar('l', 5, 1, 3); addChar('M', 6, 1, 6); addChar('m', 7, 1, 6);
        addChar('N', 8, 1, 5); addChar('n', 9, 1, 5); addChar('O', 10, 1, 5); addChar('o', 11, 1, 5);
        addChar('P', 12, 1, 5); addChar('p', 13, 1, 5); addChar('Q', 14, 1, 5); addChar('q', 15, 1, 5);
        addChar('R', 16, 1, 5); addChar('r', 17, 1, 5);

        addChar('S', 0, 2, 5); addChar('s', 1, 2, 5); addChar('T', 2, 2, 6); addChar('t', 3, 2, 4);
        addChar('U', 4, 2, 5); addChar('u', 5, 2, 5); addChar('V', 6, 2, 6); addChar('v', 7, 2, 5);
        addChar('W', 8, 2, 6); addChar('w', 9, 2, 6); addChar('X', 10, 2, 5); addChar('x', 11, 2, 5);
        addChar('Y', 12, 2, 5); addChar('y', 13, 2, 5); addChar('Z', 14, 2, 5); addChar('z', 15, 2, 5);

        // Кириллица (Ряд 3-6)
        addChar('А', 0, 3, 5); addChar('а', 1, 3, 5); addChar('Б', 2, 3, 5); addChar('б', 3, 3, 5);
        addChar('В', 4, 3, 5); addChar('в', 5, 3, 5); addChar('Г', 6, 3, 5); addChar('г', 7, 3, 4);
        addChar('Д', 8, 3, 6); addChar('д', 9, 3, 5); addChar('Е', 10, 3, 5); addChar('е', 11, 3, 5);
        addChar('Ж', 12, 3, 6); addChar('ж', 13, 3, 6); addChar('З', 14, 3, 5); addChar('з', 15, 3, 5);
        addChar('И', 16, 3, 5); addChar('и', 17, 3, 5);

        addChar('К', 0, 4, 5); addChar('к', 1, 4, 5); addChar('Л', 2, 4, 5); addChar('л', 3, 4, 5);
        addChar('М', 4, 4, 6); addChar('м', 5, 4, 6); addChar('Н', 6, 4, 5); addChar('н', 7, 4, 5);
        addChar('О', 8, 4, 5); addChar('о', 9, 4, 5); addChar('П', 10, 4, 5); addChar('п', 11, 4, 5);
        addChar('Р', 12, 4, 5); addChar('р', 13, 4, 5); addChar('С', 14, 4, 5); addChar('с', 15, 4, 5);
        addChar('Т', 16, 4, 6); addChar('т', 17, 4, 6);

        addChar('У', 0, 5, 5); addChar('у', 1, 5, 5); addChar('Ф', 2, 5, 6); addChar('ф', 3, 5, 6);
        addChar('Х', 4, 5, 6); addChar('х', 5, 5, 5); addChar('Ц', 6, 5, 6); addChar('ц', 7, 5, 6);
        addChar('Ч', 8, 5, 5); addChar('ч', 9, 5, 5); addChar('Ш', 10, 5, 6); addChar('ш', 11, 5, 6);
        addChar('Щ', 12, 5, 7); addChar('щ', 13, 5, 7); addChar('ъ', 14, 5, 6); addChar('ы', 15, 5, 7);
        addChar('ь', 16, 5, 5); addChar('Э', 17, 5, 5);

        addChar('э', 0, 6, 5); addChar('Ю', 1, 6, 7); addChar('ю', 2, 6, 7); addChar('Я', 3, 6, 5); addChar('я', 4, 6, 5);

        // Цифры и знаки (Ряд 7)
        addChar('0', 0, 7, 5); addChar('1', 1, 7, 4); addChar('2', 2, 7, 5); addChar('3', 3, 7, 5);
        addChar('4', 4, 7, 5); addChar('5', 5, 7, 5); addChar('6', 6, 7, 5); addChar('7', 7, 7, 5);
        addChar('8', 8, 7, 5); addChar('9', 9, 7, 5); addChar('.', 10, 7, 2); addChar(',', 11, 7, 2);
        addChar('_', 12, 7, 4); addChar('!', 13, 7, 2); addChar('?', 14, 7, 6);

        // Спецсимволы
        addChar(' ', 0, 0, 3);
    }

    private void initDiacritics() {
        diacriticMap.put('Й', new DiacriticData('И', 105, 66, 4, 1, -2));
        diacriticMap.put('й', new DiacriticData('и', 105, 66, 4, 1, 0));
        diacriticMap.put('Ё', new DiacriticData('Е', 105, 68, 4, 1, -2));
        diacriticMap.put('ё', new DiacriticData('е', 105, 68, 4, 1, 0));
    }

    private void addChar(char c, int ix, int iy, int adv) {
        if (c < fastMap.length) {
            int x = ix * (charWidth + spacing);
            int y = iy * (charHeight + spacing);
            fastMap[c] = new CharData(x, y, adv);
        }
    }

    public CharData getCharData(char c) {
        return (c < fastMap.length) ? fastMap[c] : null;
    }

    public DiacriticData getDiacriticData(char c) {
        return diacriticMap.get(c);
    }

    // 3. Обновляем геттеры: берем данные из объекта texture
    public int getTextureID() { return texture.id; }
    public int getTextureWidth() { return texture.width; }
    public int getTextureHeight() { return texture.height; }

    public int getCharWidth() { return charWidth; }
    public int getCharHeight() { return charHeight; }

    public void destroy() {
        if (texture != null) TextureLoader.deleteTexture(texture.id);
    }
}