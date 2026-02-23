package SwordsGame.client.core;

public interface SessionState {
    void onEnter(SessionContext context);

    void onExit(SessionState nextState);

    void update();

    void render();

    default void requestClose() {
    }
}
