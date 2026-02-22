package SwordsGame.client.graphics;

import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;

public class MshBuf {
    private static final int STRIDE_FLOATS = 11;
    private static final int STRIDE_BYTES = STRIDE_FLOATS * Float.BYTES;

    private final int vboId;
    private final int vertexCount;

    private MshBuf(int vboId, int vertexCount) {
        this.vboId = vboId;
        this.vertexCount = vertexCount;
    }

    public static MshBuf build(FCol collector) {
        if (collector == null || collector.size() == 0) return null;
        FloatBuffer buffer = BufferUtils.createFloatBuffer(collector.size());
        buffer.put(collector.array(), 0, collector.size()).flip();

        int vboId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        return new MshBuf(vboId, collector.size() / STRIDE_FLOATS);
    }

    public void render(boolean useColorArray) {
        glBindBuffer(GL_ARRAY_BUFFER, vboId);

        glEnableClientState(GL_VERTEX_ARRAY);
        glEnableClientState(GL_NORMAL_ARRAY);
        glEnableClientState(GL_TEXTURE_COORD_ARRAY);
        if (useColorArray) {
            glEnableClientState(GL_COLOR_ARRAY);
        } else {
            glDisableClientState(GL_COLOR_ARRAY);
        }

        glVertexPointer(3, GL_FLOAT, STRIDE_BYTES, 0L);
        glNormalPointer(GL_FLOAT, STRIDE_BYTES, (long) (3 * Float.BYTES));
        glTexCoordPointer(2, GL_FLOAT, STRIDE_BYTES, (long) (6 * Float.BYTES));
        if (useColorArray) {
            glColorPointer(3, GL_FLOAT, STRIDE_BYTES, (long) (8 * Float.BYTES));
        }

        glDrawArrays(GL_TRIANGLES, 0, vertexCount);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    public void destroy() {
        glDeleteBuffers(vboId);
    }
}
