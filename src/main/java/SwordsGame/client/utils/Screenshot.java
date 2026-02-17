package SwordsGame.client.utils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.imageio.ImageIO;
import org.lwjgl.BufferUtils;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;

public class Screenshot {

    private static File getUniqueFile() {
        String home = System.getProperty("user.home");
        String sep = File.separator;
        String timestamp = new SimpleDateFormat("MMdd-HHmm").format(new Date());
        File dir = new File(home + sep + "Pictures" + sep + "SwordsGame");
        if (!dir.exists()) dir.mkdirs();

        String baseName = timestamp;
        File file = new File(dir, baseName + ".png");
        int count = 1;
        while (file.exists()) {
            file = new File(dir, baseName + "_" + count + ".png");
            count++;
        }
        return file;
    }

    public static void takeScreenshot(int fboId, int width, int height) {
        glBindFramebuffer(GL_FRAMEBUFFER, fboId);

        ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4);
        glReadPixels(0, 0, width, height, GL_RGBA, GL_UNSIGNED_BYTE, buffer);

        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int i = (x + (width * y)) * 4;
                int r = buffer.get(i) & 0xFF;
                int g = buffer.get(i + 1) & 0xFF;
                int b = buffer.get(i + 2) & 0xFF;
                img.setRGB(x, height - (y + 1), (r << 16) | (g << 8) | b);
            }
        }

        try {
            File targetFile = getUniqueFile();
            ImageIO.write(img, "png", targetFile);
            System.out.println("[Scr]" + targetFile.getAbsolutePath());
        } catch (Exception e) {
            System.err.println("[Scr]" + e.getMessage());
        }
    }

    public static void takeScreenshotFromBackBuffer(int width, int height) {
        glBindFramebuffer(GL_READ_FRAMEBUFFER, 0);

        ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4);
        glReadPixels(0, 0, width, height, GL_RGBA, GL_UNSIGNED_BYTE, buffer);

        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int i = (x + (width * y)) * 4;
                int r = buffer.get(i) & 0xFF;
                int g = buffer.get(i + 1) & 0xFF;
                int b = buffer.get(i + 2) & 0xFF;
                img.setRGB(x, height - (y + 1), (r << 16) | (g << 8) | b);
            }
        }

        try {
            File targetFile = getUniqueFile();
            ImageIO.write(img, "png", targetFile);
            System.out.println("[Scr]" + targetFile.getAbsolutePath());
        } catch (Exception e) {
            System.err.println("[Scr]" + e.getMessage());
        }
    }
}
