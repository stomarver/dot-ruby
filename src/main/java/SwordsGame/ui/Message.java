package SwordsGame.ui;

import java.util.ArrayList;
import java.util.List;
import static org.lwjgl.opengl.GL11.*;

public class Message {
    private static class Entry {
        String text;
        long timestamp;
        Entry(String t) { this.text = t; this.timestamp = System.currentTimeMillis(); }
    }

    private final List<Entry> queue = new ArrayList<>();
    private final int MAX_LIFE = 8000;
    private final int FADE_TIME = 2000;

    public void add(String txt) {
        synchronized (queue) {
            queue.add(new Entry(txt));
            if (queue.size() > 10) queue.remove(0);
        }
    }

    public void draw(Text renderer) {
        long now = System.currentTimeMillis();

        synchronized (queue) {
            // 1. Сначала удаляем все просроченные сообщения
            queue.removeIf(e -> (now - e.timestamp) > MAX_LIFE);

            int offset = 0;
            // 2. Идем по списку СЗАДИ (от новых к старым)
            for (int i = queue.size() - 1; i >= 0; i--) {
                Entry e = queue.get(i);
                long age = now - e.timestamp;

                float alpha = 1.0f;
                if (age > (MAX_LIFE - FADE_TIME)) {
                    alpha = 1.0f - (float) (age - (MAX_LIFE - FADE_TIME)) / FADE_TIME;
                }

                glColor4f(1, 1, 1, alpha);

                // Теперь самый новый (первый в этом цикле) будет на -10,
                // а каждый следующий (более старый) будет выше на величину offset
                renderer.draw(e.text, Anchor.LEFT, Anchor.BOTTOM, 130, -10 - offset, 1);

                offset += 20;
            }
        }
        glColor4f(1, 1, 1, 1);
    }
}