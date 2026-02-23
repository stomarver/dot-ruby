package SwordsGame.client.ui;

public final class HudLayoutRegistry {
    public static final String DIALOG_MAIN_MENU = "main.menu";
    public static final String DIALOG_SESSION_PAUSE = "session.pause";
    public static final String DIALOG_DEBUG_INFO = "debug.info";
    public static final String DIALOG_SHOWCASE_MENU = "showcase.menu";

    private HudLayoutRegistry() {
    }

    public static void registerDefaultPivots(Hud hud) {
        hud.setPivot("screen.left.top", Anchor.LEFT, Anchor.TOP, 0, 0);
        hud.setPivot("screen.center", Anchor.CENTER, Anchor.CENTER_Y, 0, 0);
        hud.setPivot("screen.bottom.center", Anchor.CENTER, Anchor.BOTTOM, 0, 0);
        hud.setPivot("screen.right.center", Anchor.RIGHT, Anchor.CENTER_Y, 0, 0);
        hud.setPivot("debug.info.dialog", Anchor.RIGHT, Anchor.CENTER_Y, -20, 0);
        hud.setPivot("menu.dialog", Anchor.CENTER, Anchor.CENTER_Y, 0, 0);
        hud.setPivot("showcase.dialog.left", Anchor.LEFT, Anchor.CENTER_Y, 150, 0);
        hud.setPivot("showcase.dialog.right", Anchor.RIGHT, Anchor.CENTER_Y, -150, 0);
    }
}
