package SwordsGame.client.core;

public class SessionStateManager {
    private SessionContext context;
    private SessionState current;

    public void init(SessionContext context) {
        this.context = context;
    }

    public void changeState(SessionState next) {
        if (current != null) {
            current.onExit();
        }
        current = next;
        if (current != null) {
            current.onEnter(context);
        }
    }

    public void update() {
        if (current != null) {
            current.update();
        }
    }

    public void render() {
        if (current != null) {
            current.render();
        }
    }

    public void shutdown() {
        if (current != null) {
            current.requestClose();
            current.onExit();
            current = null;
        }
    }
}
