package SwordsGame.client.core;

/**
 * Legacy compatibility wrapper.
 * Use {@link Session} as a single entry point and pass "--debug".
 */
@Deprecated
public class Debug {
    public void start() {
        Session.main(new String[] {"--debug"});
    }
}
