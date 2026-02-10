package SwordsGame.ui;

import SwordsGame.ui.data.text.TextRegistry;

public class ExampleTextOverlay {
    private final Text text;

    public ExampleTextOverlay(Text text) {
        this.text = text;
    }

    public void renderDemo() {
        text.draw(d -> d
                .text(TextRegistry.get("hud.example", "Demo text"))
                .centerBottom()
                .at(0, -80)
                .size(1.6f)
                .wave("medium")
                .shake("slow")
                .crit("medium"));
    }
}
