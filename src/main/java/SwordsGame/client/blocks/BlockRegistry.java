package SwordsGame.client.blocks;

import SwordsGame.client.assets.Paths;
import SwordsGame.client.assets.Syn;
import SwordsGame.client.graphics.Block;
import SwordsGame.client.graphics.BlockRenderer;

import java.util.HashMap;
import java.util.Map;

public class BlockRegistry {
    private static final Map<Type, Block> registry = new HashMap<>();
    private static boolean destroyed = false;

    public static void init() {
        destroyed = false;
        registry.clear();

        reg(Type.AIR, null);

        reg(Type.COBBLE,
                Syn.blk(Type.COBBLE)
                        .tex(Paths.BLOCK_COBBLE)
                        .props(p -> p.randomColor())
                        .build());

        reg(Type.GRASS,
                Syn.blk(Type.GRASS)
                        .tex(Paths.BLOCK_GRASS, Paths.BLOCK_GRASS, Paths.BLOCK_GRASS)
                        .props(p -> p.randomColor().smoothing().hardness(0.7f))
                        .build());

        reg(Type.STONE,
                Syn.blk(Type.STONE)
                        .tex(Paths.BLOCK_STONE)
                        .props(p -> p.hardness(1.5f))
                        .build());
    }

    public static void reg(Type type, Block block) {
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
