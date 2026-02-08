package SwordsGame.client.blocks;

import SwordsGame.client.Block;

import java.util.HashMap;
import java.util.Map;

public class Registry {
    private static final Map<Type, Block> registry = new HashMap<>();
    private static boolean destroyed = false;

    public static void init() {
        destroyed = false;
        registry.put(Type.AIR, null);
        registry.put(Type.COBBLE, new Cobble());
        registry.put(Type.GRASS, new Grass());
        registry.put(Type.STONE, new Stone());
    }

    public static void draw(byte id, int seed, boolean[] faces) {
        Type type = Type.fromId(id);
        Block block = registry.get(type);
        if (block != null) {
            block.draw(seed, faces);
        }
    }

    public static Block get(Type type) {
        return registry.get(type);
    }

    public static Block get(byte id) {
        return registry.get(Type.fromId(id));
    }

    public static boolean isTransparent(byte id) {
        Block block = get(id);
        return block == null || block.getProperties().isTransparent();
    }

    public static boolean isSolid(byte id) {
        Block block = get(id);
        return block != null && block.getProperties().isSolid();
    }

    public static void destroy() {
        if (destroyed) {
            return;
        }
        for (Block block : registry.values()) {
            if (block != null) {
                block.destroy();
            }
        }
        destroyed = true;
    }
}
