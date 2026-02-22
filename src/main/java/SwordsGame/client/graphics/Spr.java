package SwordsGame.client.graphics;

import SwordsGame.client.ui.Anc;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;

public class Spr {
    private final float screenW, screenH;

    public Spr(int w, int h) {
        this.screenW = (float)w;
        this.screenH = (float)h;
    }

    /**
     * Отрисовка с указанием выравнивания через Enum (например, Anc.LEFT, Anc.TOP).
     * @param tex Текстура (объект с ID и размерами)
     * @param ax Выравнивание по горизонтали
     * @param ay Выравнивание по вертикали
     * @param x Смещение по X
     * @param y Смещение по Y
     * @param s Масштаб (1.0 = исходный размер)
     */
    public void draw(TexLd.Texture tex, Anc.TypeX ax, Anc.TypeY ay, float x, float y, float s) {
        drawInternal(tex, buildAnchor(ax, ay), x, y, s);
    }

    /**
     * Перегрузка для случая, когда случайно передали два TypeX (например, CENTER, CENTER)
     */
    public void draw(TexLd.Texture tex, Anc.TypeX ax, Anc.TypeX ay, float x, float y, float s) {
        draw(tex, ax, toTypeY(ay), x, y, s);
    }

    /**
     * Отрисовка с использованием готового объекта Anc (для оптимизации)
     */
    public void draw(TexLd.Texture tex, Anc a, float x, float y, float s) {
        drawInternal(tex, a, x, y, s);
    }

    private void drawInternal(TexLd.Texture tex, Anc a, float ox, float oy, float scale) {
        if (tex == null) return;

        float w = tex.width * scale;
        float h = tex.height * scale;

        float rx = a.x + ox;
        float ry = a.y + oy;

        if (a.tx == Anc.TypeX.CENTER) rx -= w / 2f;
        else if (a.tx == Anc.TypeX.RIGHT) rx -= w;

        if (a.ty == Anc.TypeY.CENTER) ry -= h / 2f;
        else if (a.ty == Anc.TypeY.BOTTOM) ry -= h;

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

    private Anc buildAnchor(Anc.TypeX ax, Anc.TypeY ay) {
        float bx = (ax == Anc.TypeX.LEFT) ? 0 : (ax == Anc.TypeX.CENTER ? screenW / 2f : screenW);
        float by = (ay == Anc.TypeY.TOP) ? 0 : (ay == Anc.TypeY.CENTER ? screenH / 2f : screenH);
        return new Anc(ax, ay, bx, by);
    }

    private Anc.TypeY toTypeY(Anc.TypeX type) {
        return type == Anc.TypeX.CENTER ? Anc.TypeY.CENTER : Anc.TypeY.TOP;
    }
}
