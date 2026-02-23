package SwordsGame.client.core;

public class Session {
    public static void main(String[] args) {
        boolean debugProfile = hasArg(args, "--debug") || hasArg(args, "debug");

        Window window = new Window("SwordsGame");
        window.create();

        SessionStateManager stateManager = new SessionStateManager();
        stateManager.init(new SessionContext(window, debugProfile));

        // Текущая сессия — это игровой сценарий. Позже сюда можно добавить состояние главного меню.
        stateManager.changeState(new SessionScenarioState());

        while (!window.shouldClose()) {
            stateManager.update();
            stateManager.render();
            window.update();
        }

        stateManager.shutdown();
        window.destroy();
        System.exit(0);
    }

    private static boolean hasArg(String[] args, String needle) {
        if (args == null || needle == null) {
            return false;
        }
        for (String arg : args) {
            if (needle.equalsIgnoreCase(arg)) {
                return true;
            }
        }
        return false;
    }
}
