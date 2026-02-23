package SwordsGame.client.core;

/**
 * Legacy compatibility wrapper.
 * Use {@link Session} as a single entry point.
 */
@Deprecated
public class Game {
    public static void main(String[] args) {
        Session.main(new String[0]);
    }

    public void start() {
        Session.main(new String[0]);
    }
}
