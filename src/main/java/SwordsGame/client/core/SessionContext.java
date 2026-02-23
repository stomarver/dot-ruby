package SwordsGame.client.core;

public class SessionContext {
    private final Window window;
    private final boolean debugProfile;
    private final SessionCommands commands;

    public SessionContext(Window window, boolean debugProfile, SessionCommands commands) {
        this.window = window;
        this.debugProfile = debugProfile;
        this.commands = commands;
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
}
