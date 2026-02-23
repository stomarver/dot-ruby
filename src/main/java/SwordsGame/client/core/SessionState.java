package SwordsGame.client.core;

public interface SessionState {
    void onEnter(SessionContext context);

    void onExit();

    void update();

    void render();

    default void requestClose() {
    }
}
