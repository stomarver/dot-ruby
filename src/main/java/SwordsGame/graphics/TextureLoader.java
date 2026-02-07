package SwordsGame.graphics;

import org.lwjgl.system.MemoryStack;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*; // Для GL_CLAMP_TO_EDGE
import static org.lwjgl.opengl.GL30.glGenerateMipmap;
import static org.lwjgl.stb.STBImage.*;
import static org.lwjgl.opengl.EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT;
import static org.lwjgl.opengl.EXTTextureFilterAnisotropic.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT;

public class TextureLoader {

    private static int loadCount = 0;
    private static boolean isReleasing = false;

    public static class Texture {
        public final int id;
        public final int width, height;

        public Texture(int id, int width, int height) {
            this.id = id;
            this.width = width;
            this.height = height;
        }
    }

    public static Texture loadTexture(String path, boolean removeBlack) {
        if (loadCount == 0) {
            System.out.println("[Sys] Textures Loading with MipMaps & Anisotropy:");
        }
        loadCount++;

        ByteBuffer image;
        int width, height;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer w = stack.mallocInt(1), h = stack.mallocInt(1), comp = stack.mallocInt(1);

            byte[] data = readResource(path);
            ByteBuffer buffer = stack.malloc(data.length).put(data);
            buffer.flip();

            image = stbi_load_from_memory(buffer, w, h, comp, 4);
            if (image == null) throw new RuntimeException("STB failed: " + stbi_failure_reason());

            width = w.get(0);
            height = h.get(0);

            if (removeBlack) processTransparency(image, width * height);

            int id = glGenTextures();
            glBindTexture(GL_TEXTURE_2D, id);

            // 1. Устанавливаем данные текстуры
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, image);

            // 2. Генерируем MipMap'ы
            glGenerateMipmap(GL_TEXTURE_2D);

            // 3. Настройка фильтрации (MipMap для уменьшения, Linear для увеличения)
            // Используем GL_NEAREST_MIPMAP_LINEAR для сохранения пиксельности, но плавного перехода
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST_MIPMAP_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

            // Убираем швы на краях текстур
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

            // 4. Включаем анизотропную фильтрацию (если поддерживается)
            if (glGetFloat(GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT) > 0) {
                float maxAnisotropy = glGetFloat(GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT);
                // Ставим 4.0 или 8.0 для баланса качества и скорости
                glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAX_ANISOTROPY_EXT, Math.min(8.0f, maxAnisotropy));
            }

            stbi_image_free(image);
            System.out.printf("[ID: %d] | %-15s | [%dx%d] (MipMapped)%n", id, path, width, height);

            return new Texture(id, width, height);
        }
    }

    public static void finishLoading() {
        if (loadCount > 0) {
            System.out.println("[Sys] Textures Loaded");
            loadCount = 0;
        }
    }

    public static void deleteTexture(int id) {
        if (id > 0) {
            if (!isReleasing) {
                System.out.println("[Sys] Textures Releasing:");
                isReleasing = true;
            }
            glDeleteTextures(id);
            System.out.printf("[ID: %d]%n", id);
        }
    }

    public static void finishCleanup() {
        if (isReleasing) {
            System.out.println("[Sys] Textures Released");
            isReleasing = false;
        }
    }

    private static void processTransparency(ByteBuffer buf, int pixels) {
        for (int i = 0; i < pixels; i++) {
            int r = buf.get(i * 4) & 0xFF;
            int g = buf.get(i * 4 + 1) & 0xFF;
            int b = buf.get(i * 4 + 2) & 0xFF;
            if (r == 0 && g == 0 && b == 0) {
                buf.put(i * 4 + 3, (byte) 0);
            }
        }
    }

    private static byte[] readResource(String path) {
        String resPath = path.startsWith("/") ? path : "/" + path;
        try (InputStream is = TextureLoader.class.getResourceAsStream(resPath)) {
            if (is == null) throw new RuntimeException("Resource not found: " + resPath);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int n;
            while ((n = is.read(buffer)) != -1) {
                os.write(buffer, 0, n);
            }
            return os.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to read resource: " + path, e);
        }
    }
}