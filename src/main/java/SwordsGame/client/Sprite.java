package SwordsGame.client;

import SwordsGame.ui.Anchor;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;

public class Sprite {
    private final float screenW, screenH;

    public Sprite(int w, int h) {
        this.screenW = (float)w;
        this.screenH = (float)h;
    }

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
        drawInternal(tex, buildAnchor(ax, ay), x, y, s);
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

    private void drawInternal(TextureLoader.Texture tex, Anchor a, float ox, float oy, float scale) {
        if (tex == null) return;

        float w = tex.width * scale;
        float h = tex.height * scale;

        float rx = a.x + ox;
        float ry = a.y + oy;

        if (a.tx == Anchor.TypeX.CENTER) rx -= w / 2f;
        else if (a.tx == Anchor.TypeX.RIGHT) rx -= w;

        if (a.ty == Anchor.TypeY.CENTER) ry -= h / 2f;
        else if (a.ty == Anchor.TypeY.BOTTOM) ry -= h;

        FloatBuffer verts = BufferUtils.createFloatBuffer(8);
        verts.put(new float[]{
                rx, ry,
                rx + w, ry,
                rx + w, ry + h,
                rx, ry + h
        }).flip();
        FloatBuffer uvs = BufferUtils.createFloatBuffer(8);
        uvs.put(new float[]{
                0f, 0f,
                1f, 0f,
                1f, 1f,
                0f, 1f
        }).flip();

        glEnable(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, tex.id);
        glColor3f(1, 1, 1);

        glEnableClientState(GL_VERTEX_ARRAY);
        glEnableClientState(GL_TEXTURE_COORD_ARRAY);

        glVertexPointer(2, GL_FLOAT, 0, verts);
        glTexCoordPointer(2, GL_FLOAT, 0, uvs);
        glDrawArrays(GL_QUADS, 0, 4);

        glDisableClientState(GL_TEXTURE_COORD_ARRAY);
        glDisableClientState(GL_VERTEX_ARRAY);

        glDisable(GL_TEXTURE_2D);
    }

    private Anchor buildAnchor(Anchor.TypeX ax, Anchor.TypeY ay) {
        float bx = (ax == Anchor.TypeX.LEFT) ? 0 : (ax == Anchor.TypeX.CENTER ? screenW / 2f : screenW);
        float by = (ay == Anchor.TypeY.TOP) ? 0 : (ay == Anchor.TypeY.CENTER ? screenH / 2f : screenH);
        return new Anchor(ax, ay, bx, by);
    }
}
