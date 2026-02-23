package SwordsGame.client.core;

/**
 * Legacy compatibility wrapper.
 * Use {@link Session} as a single entry point.
 */
@Deprecated
public class Game {
    public void start() {
        Session.main(new String[0]);
    }
}
