package SwordsGame.graphics;

import SwordsGame.core.Window;
import static org.lwjgl.opengl.GL11.*;

public class Renderer {

    public void setup3D(Window win) {
        glClearColor(0.5f, 0.8f, 1.0f, 1.0f); // Светло-голубое небо
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glViewport(120, 0, 720, 540);
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(-360, 360, -270, 270, -5000, 5000);
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();

        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE); // Включаем! Теперь задние грани не рисуются
        glCullFace(GL_BACK);    // Уточняем, что не рисуем именно задние
        glEnable(GL_TEXTURE_2D);
        glDisable(GL_BLEND);

        // --- Настройка Глобального Освещения ---
        glEnable(GL_LIGHTING);   // Включаем систему освещения
        glEnable(GL_LIGHT0);     // Включаем первый источник света

        // Позволяет использовать glColor3f вместе со светом
        glEnable(GL_COLOR_MATERIAL);
        glColorMaterial(GL_FRONT_AND_BACK, GL_AMBIENT_AND_DIFFUSE);

        // Смещение по X даст разницу между лево/право
// Смещение по Z даст разницу между перед/зад
// Высота 10.0 оставит верх самой яркой частью
        float[] lightPosition = { 15.0f, 25.0f, 10.0f, 0.0f };
        glLightfv(GL_LIGHT0, GL_POSITION, lightPosition);

        // Ambient 0.3f дает хорошую глубину, Diffuse 1.0f — яркие верхушки
        float[] ambientLight = { 0.3f, 0.3f, 0.3f, 1.0f };
        glLightfv(GL_LIGHT0, GL_AMBIENT, ambientLight);

        float[] whiteLight = { 0.9f, 0.9f, 0.9f, 1.0f };
        glLightfv(GL_LIGHT0, GL_DIFFUSE, whiteLight);
    }

    public void setup2D(Window win) {
        glViewport(0, 0, 960, 540);
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(0, 960, 540, 0, -1, 1);
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();

        glDisable(GL_DEPTH_TEST);
        glDisable(GL_LIGHTING); // ВАЖНО: выключаем свет для интерфейса
        glDisable(GL_CULL_FACE);
        glEnable(GL_TEXTURE_2D);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }
}