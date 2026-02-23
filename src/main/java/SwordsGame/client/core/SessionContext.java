package SwordsGame.client.core;

public class SessionContext {
    private final Window window;
    private final boolean debugProfile;

    public SessionContext(Window window, boolean debugProfile) {
        this.window = window;
        this.debugProfile = debugProfile;
    }

    public Window getWindow() {
        return window;
    }

    public boolean isDebugProfile() {
        return debugProfile;
    }
}
