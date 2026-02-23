package SwordsGame.client.core;

import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;

public class Session {
    public static void main(String[] args) {
        boolean debugProfile = hasArg(args, "--debug") || hasArg(args, "debug");

        Window window = new Window("SwordsGame");
        window.create();

        SessionStateManager stateManager = new SessionStateManager();

        SessionCommands commands = new SessionCommands() {
            @Override
            public void openMainMenu() {
                stateManager.changeState(new MainMenuState());
            }

            @Override
            public void startScenario(boolean debugMode) {
                stateManager.changeState(new SessionScenarioState(debugMode));
            }

            @Override
            public void exitApplication() {
                glfwSetWindowShouldClose(window.getHandle(), true);
            }
        };

        stateManager.init(new SessionContext(window, debugProfile, commands));

        if (debugProfile) {
            stateManager.changeState(new SessionScenarioState(true));
        } else {
            stateManager.changeState(new MainMenuState());
        }

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
