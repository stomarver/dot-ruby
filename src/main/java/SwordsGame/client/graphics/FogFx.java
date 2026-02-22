package SwordsGame.client.graphics;

import SwordsGame.client.core.Window;
import org.joml.Math;
import org.joml.Vector2f;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL20.*;

public class FogFx {
    private static final float BASE_START = -735.0f;
    private static final float BASE_END = -315.0f;
    private static final float START_OFFSET = 0.05f;
    private static final float SOFTNESS = 2.0f;

    private float startDist = BASE_START;
    private float endDist = BASE_END;

    private int shaderProgram = 0;
    private int depthUniform = -1;
    private int nearUniform = -1;
    private int farUniform = -1;

    public float startDist() { return startDist; }
    public float endDist() { return endDist; }

    public void setZoom(float cameraZoom) {
        float safeZoom = Math.max(0.001f, cameraZoom);
        startDist = BASE_START * safeZoom;
        endDist = BASE_END * safeZoom;
    }

    public void apply(Window window, int viewportX, int viewportY, int viewportWidth, int viewportHeight) {
        if (window == null) return;

        int depthTextureId = window.getDepthTextureId();
        if (depthTextureId == 0) return;

        ensureShader();
        if (shaderProgram == 0) return;

        Vector2f depthRange = buildDepthRange();

        float texU0 = viewportX / (float) window.getRenderWidth();
        float texV0 = viewportY / (float) window.getRenderHeight();
        float texU1 = (viewportX + viewportWidth) / (float) window.getRenderWidth();
        float texV1 = (viewportY + viewportHeight) / (float) window.getRenderHeight();

        glDisable(GL_DEPTH_TEST);
        glDisable(GL_LIGHTING);
        glDisable(GL_FOG);
        glDisable(GL_CULL_FACE);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        glUseProgram(shaderProgram);
        glUniform1i(depthUniform, 0);
        glUniform1f(nearUniform, depthRange.x);
        glUniform1f(farUniform, depthRange.y);

        glActiveTexture(GL_TEXTURE0);
        glEnable(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, depthTextureId);

        glViewport(viewportX, viewportY, viewportWidth, viewportHeight);

        glMatrixMode(GL_PROJECTION);
        glPushMatrix();
        glLoadIdentity();
        glOrtho(0, 1, 0, 1, -1, 1);

        glMatrixMode(GL_MODELVIEW);
        glPushMatrix();
        glLoadIdentity();

        glBegin(GL_QUADS);
        glTexCoord2f(texU0, texV0); glVertex2f(0f, 0f);
        glTexCoord2f(texU1, texV0); glVertex2f(1f, 0f);
        glTexCoord2f(texU1, texV1); glVertex2f(1f, 1f);
        glTexCoord2f(texU0, texV1); glVertex2f(0f, 1f);
        glEnd();

        glPopMatrix();
        glMatrixMode(GL_PROJECTION);
        glPopMatrix();
        glMatrixMode(GL_MODELVIEW);

        glBindTexture(GL_TEXTURE_2D, 0);
        glUseProgram(0);
        glDisable(GL_BLEND);
    }

    private Vector2f buildDepthRange() {
        float nearDistance = Math.min(-startDist, -endDist);
        float farDistance = Math.max(-startDist, -endDist);
        float nearDepthRaw = 0.5f - (nearDistance / 10000.0f);
        float farDepthRaw = 0.5f - (farDistance / 10000.0f);
        float nearDepthBase = Math.min(nearDepthRaw, farDepthRaw);
        float farDepthBase = Math.max(nearDepthRaw, farDepthRaw);

        float nearDepth = clamp01(nearDepthBase + START_OFFSET);
        float farDepth = clamp01(nearDepth + ((farDepthBase - nearDepthBase) * SOFTNESS));
        return new Vector2f(nearDepth, farDepth);
    }

    private void ensureShader() {
        if (shaderProgram != 0) return;

        String vertexSource = "#version 120\n" +
                "varying vec2 vUv;\n" +
                "void main() {\n" +
                "  gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;\n" +
                "  vUv = gl_MultiTexCoord0.xy;\n" +
                "}";

        String fragmentSource = "#version 120\n" +
                "uniform sampler2D depthTex;\n" +
                "uniform float fogNearDepth;\n" +
                "uniform float fogFarDepth;\n" +
                "varying vec2 vUv;\n" +
                "void main() {\n" +
                "  float depth = texture2D(depthTex, vUv).r;\n" +
                "  float fogFactor = clamp((depth - fogNearDepth) / max(0.0001, fogFarDepth - fogNearDepth), 0.0, 1.0);\n" +
                "  gl_FragColor = vec4(0.0, 0.0, 0.0, fogFactor);\n" +
                "}";

        int vertexShader = compileShader(GL_VERTEX_SHADER, vertexSource);
        int fragmentShader = compileShader(GL_FRAGMENT_SHADER, fragmentSource);
        if (vertexShader == 0 || fragmentShader == 0) return;

        int program = glCreateProgram();
        glAttachShader(program, vertexShader);
        glAttachShader(program, fragmentShader);
        glLinkProgram(program);

        if (glGetProgrami(program, GL_LINK_STATUS) == GL_FALSE) {
            glDeleteProgram(program);
            glDeleteShader(vertexShader);
            glDeleteShader(fragmentShader);
            return;
        }

        shaderProgram = program;
        depthUniform = glGetUniformLocation(shaderProgram, "depthTex");
        nearUniform = glGetUniformLocation(shaderProgram, "fogNearDepth");
        farUniform = glGetUniformLocation(shaderProgram, "fogFarDepth");

        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);
    }

    private int compileShader(int shaderType, String source) {
        int shader = glCreateShader(shaderType);
        glShaderSource(shader, source);
        glCompileShader(shader);
        if (glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE) {
            glDeleteShader(shader);
            return 0;
        }
        return shader;
    }

    private float clamp01(float value) {
        return Math.clamp(0.0f, 1.0f, value);
    }
}
