package SwordsGame.utils;

import club.minnced.discord.rpc.DiscordEventHandlers;
import club.minnced.discord.rpc.DiscordRPC;
import club.minnced.discord.rpc.DiscordRichPresence;

import java.io.InputStream;
import java.util.Properties;

public class Discord {

    private static final String CLIENT_ID = "1469020484942430270";
    private static final DiscordRPC lib = DiscordRPC.INSTANCE;

    private static long startTime;
    private static String gameVersion = ".ruby-unknown";

    public static void init() {
        try {
            loadVersion();

            startTime = System.currentTimeMillis() / 1000;

            DiscordEventHandlers handlers = new DiscordEventHandlers();
            handlers.ready = (user) -> System.out.println("[RPC] Ready: " + user.username);

            lib.Discord_Initialize(CLIENT_ID, handlers, true, "");
            updateStatus();

            Thread t = new Thread(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    lib.Discord_RunCallbacks();
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }, "RPC-Callback-Handler");

            t.setDaemon(true);
            t.start();

            System.out.println("[RPC] Initialized with version: " + gameVersion);

        } catch (Exception e) {
            System.err.println("[RPC] Failure: " + e.getMessage());
        }
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
        try {
            DiscordRichPresence presence = new DiscordRichPresence();
            presence.details = gameVersion;
            presence.startTimestamp = startTime;
            presence.largeImageKey = "logo";
            presence.largeImageText = "DotRuby";

            lib.Discord_UpdatePresence(presence);

        } catch (Exception e) {
            System.err.println("[RPC] Failed to update: " + e.getMessage());
        }
    }

    public static void shutdown() {
        lib.Discord_Shutdown();
    }
}
