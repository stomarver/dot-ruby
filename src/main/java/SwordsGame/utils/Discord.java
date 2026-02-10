package SwordsGame.utils;

import java.io.InputStream;
import java.util.Properties;

public class Discord {

    private static String gameVersion = ".ruby-unknown";

    public static void init() {
        loadVersion();
        System.out.println("[RPC] Disabled (no bundled binary). Version: " + gameVersion);
    }

    private static void loadVersion() {
        try (InputStream input = Discord.class.getClassLoader().getResourceAsStream("version.properties")) {
            if (input != null) {
                Properties prop = new Properties();
                prop.load(input);
                gameVersion = prop.getProperty("version", ".ruby-dev");
            } else {
                gameVersion = ".ruby-dev";
            }
        } catch (Exception e) {
            gameVersion = ".ruby-dev";
        }
    }

    public static void updateStatus() {
        // no-op in compatibility mode
    }

    public static void shutdown() {
        // no-op in compatibility mode
    }
}
