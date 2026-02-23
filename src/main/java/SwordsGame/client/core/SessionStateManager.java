package SwordsGame.client.core;

public class SessionStateManager {
    private SessionContext context;
    private SessionState current;
    private SessionState pending;

    public void init(SessionContext context) {
        this.context = context;
    }

    public void changeState(SessionState next) {
        pending = next;
    }

    private void applyPendingTransition() {
        if (pending == null) {
            return;
        }
        if (current != null) {
            current.onExit(pending);
        }
        current = pending;
        pending = null;
        if (current != null) {
            current.onEnter(context);
        }
    }

    public void update() {
        applyPendingTransition();
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
        pending = null;
        if (current != null) {
            current.requestClose();
            current.onExit(null);
            current = null;
        }
    }
}
