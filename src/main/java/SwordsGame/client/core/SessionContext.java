package SwordsGame.client.core;

public class SessionContext {
    private final Window window;
    private final boolean debugProfile;
    private final SessionCommands commands;
    private final HotkeyManager hotkeys;

    public SessionContext(Window window, boolean debugProfile, SessionCommands commands, HotkeyManager hotkeys) {
        this.window = window;
        this.debugProfile = debugProfile;
        this.commands = commands;
        this.hotkeys = hotkeys;
    }

    public Window getWindow() {
        return window;
    }

    public boolean isDebugProfile() {
        return debugProfile;
    }

    public SessionCommands getCommands() {
        return commands;
    }

    public HotkeyManager getHotkeys() {
        return hotkeys;
    }
}
