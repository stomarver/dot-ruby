package SwordsGame.graphics;

import SwordsGame.ui.Anchor;

import static org.lwjgl.opengl.GL11.*;

public class Sprite {
    private final float screenW, screenH;

    public Sprite(int w, int h) {
        this.screenW = (float)w;
        this.screenH = (float)h;
    }

    // --- ПЕРЕХОДНИКИ (Синтаксис как у Text) ---

    /**
     * Отрисовка с указанием выравнивания через Enum (например, Anchor.LEFT, Anchor.TOP).
     * @param tex Текстура (объект с ID и размерами)
     * @param ax Выравнивание по горизонтали
     * @param ay Выравнивание по вертикали
     * @param x Смещение по X
     * @param y Смещение по Y
     * @param s Масштаб (1.0 = исходный размер)
     */
    public void draw(TextureLoader.Texture tex, Anchor.TypeX ax, Anchor.TypeY ay, float x, float y, float s) {
        float bx = (ax == Anchor.TypeX.LEFT) ? 0 : (ax == Anchor.TypeX.CENTER ? screenW / 2f : screenW);
        float by = (ay == Anchor.TypeY.TOP) ? 0 : (ay == Anchor.TypeY.CENTER ? screenH / 2f : screenH);
        drawInternal(tex, new Anchor(ax, ay, bx, by), x, y, s);
    }

    /**
     * Перегрузка для случая, когда случайно передали два TypeX (например, CENTER, CENTER)
     */
    public void draw(TextureLoader.Texture tex, Anchor.TypeX ax, Anchor.TypeX ay, float x, float y, float s) {
        Anchor.TypeY vy = (ay == Anchor.TypeX.CENTER) ? Anchor.TypeY.CENTER : Anchor.TypeY.TOP;
        draw(tex, ax, vy, x, y, s);
    }

    /**
     * Отрисовка с использованием готового объекта Anchor (для оптимизации)
     */
    public void draw(TextureLoader.Texture tex, Anchor a, float x, float y, float s) {
        drawInternal(tex, a, x, y, s);
    }

    // --- ОСНОВНОЕ ЯДРО ---

    private void drawInternal(TextureLoader.Texture tex, Anchor a, float ox, float oy, float scale) {
        if (tex == null) return;

        glEnable(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, tex.id);
        glColor3f(1, 1, 1); // Сбрасываем цвет на белый, чтобы картинка не красилась

        // Вычисляем размеры с учетом масштаба
        float w = tex.width * scale;
        float h = tex.height * scale;

        // Базовая позиция якоря + смещение
        float rx = a.x + ox;
        float ry = a.y + oy;

        // Корректировка в зависимости от типа якоря
        // Если CENTER - сдвигаем назад на половину ширины
        // Если RIGHT/BOTTOM - сдвигаем на всю ширину/высоту
        if (a.tx == Anchor.TypeX.CENTER) rx -= w / 2f;
        else if (a.tx == Anchor.TypeX.RIGHT) rx -= w;

        if (a.ty == Anchor.TypeY.CENTER) ry -= h / 2f;
        else if (a.ty == Anchor.TypeY.BOTTOM) ry -= h;

        glBegin(GL_QUADS);
        glTexCoord2f(0, 0); glVertex2f(rx, ry);
        glTexCoord2f(1, 0); glVertex2f(rx + w, ry);
        glTexCoord2f(1, 1); glVertex2f(rx + w, ry + h);
        glTexCoord2f(0, 1); glVertex2f(rx, ry + h);
        glEnd();

        glDisable(GL_TEXTURE_2D);
    }
}