package SwordsGame.client.blocks;

import SwordsGame.client.Block;

import java.util.HashMap;
import java.util.Map;

public class BlockRegistry {
    private static final Map<BlockType, Block> registry = new HashMap<>();

    public static void init() {
        registry.put(BlockType.AIR, null);
        registry.put(BlockType.COBBLE, new CobbleBlock());
        registry.put(BlockType.GRASS, new GrassBlock());
        registry.put(BlockType.STONE, new StoneBlock());
    }

    public static void draw(byte id, int seed, boolean[] faces) {
        BlockType type = BlockType.fromId(id);
        Block block = registry.get(type);
        if (block != null) {
            block.draw(seed, faces);
        }
    }

    public static Block get(BlockType type) {
        return registry.get(type);
    }

    public static Block get(byte id) {
        return registry.get(BlockType.fromId(id));
    }

    public static boolean isTransparent(byte id) {
        Block block = get(id);
        return block == null || block.getProperties().isTransparent();
    }

    public static boolean isSolid(byte id) {
        Block block = get(id);
        return block != null && block.getProperties().isSolid();
    }
}
