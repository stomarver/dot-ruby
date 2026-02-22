package SwordsGame.client.blocks;

import SwordsGame.client.graphics.Blk;
import SwordsGame.client.graphics.BlkRdr;

import java.util.HashMap;
import java.util.Map;

public class Reg {
    private static final Map<Type, Blk> registry = new HashMap<>();
    private static boolean destroyed = false;

    public static void init() {
        destroyed = false;
        registry.clear();
        register(Type.AIR, null);
        register(Type.COBBLE, new Cob());
        register(Type.GRASS, new Grs());
        register(Type.STONE, new Stn());
    }

    public static void register(Type type, Blk block) {
        registry.put(type, block);
    }

    public static void draw(byte id, int seed, boolean[] faces) {
        Type type = Type.fromId(id);
        Blk block = registry.get(type);
        if (block != null) {
            block.draw(seed, faces);
        }
    }

    public static Blk get(Type type) {
        return registry.get(type);
    }

    public static Blk get(byte id) {
        return registry.get(Type.fromId(id));
    }

    public static boolean isTransparent(byte id) {
        Blk block = get(id);
        return block == null || block.getProperties().isTransparent();
    }

    public static boolean isSolid(byte id) {
        Blk block = get(id);
        return block != null && block.getProperties().isSolid();
    }

    public static void destroy() {
        if (destroyed) {
            return;
        }
        for (Blk block : registry.values()) {
            if (block != null) {
                block.destroy();
            }
        }
        BlkRdr.clear();
        destroyed = true;
    }
}
