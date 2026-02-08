package SwordsGame.client.blocks;

import SwordsGame.client.graphics.Block;
import SwordsGame.client.graphics.BlockRenderer;

import java.util.HashMap;
import java.util.Map;

public class Registry {
    private static final Map<Type, Block> registry = new HashMap<>();
    private static boolean destroyed = false;

    public static void init() {
        destroyed = false;
        registry.clear();
        register(Type.AIR, null);
        register(Type.COBBLE, new Cobble());
        register(Type.GRASS, new Grass());
        register(Type.STONE, new Stone());
    }

    public static void register(Type type, Block block) {
        registry.put(type, block);
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
        BlockRenderer.clear();
        destroyed = true;
    }
}
