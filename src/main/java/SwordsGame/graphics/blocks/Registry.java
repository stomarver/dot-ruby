package SwordsGame.graphics.blocks;

import SwordsGame.graphics.Block;
import SwordsGame.graphics.BlockProperties;

import java.util.HashMap;
import java.util.Map;

public class Registry {
    private static final Map<Type, Block> registry = new HashMap<>();

    public static void init() {
        // Воздух (не рендерится)
        registry.put(Type.AIR, null);

        // Булыжник - базовый блок
        registry.put(Type.COBBLE,
                new Block(Type.COBBLE, "blocks/cobble.png"));

        // Трава - со случайным поворотом, цветом и разными текстурами граней
        // (можно добавить разные текстуры для верха/низа/бока если есть)
        registry.put(Type.GRASS,
                new Block(Type.GRASS, "blocks/grass.png",
                        new BlockProperties()
                                .randomRotation()
                                .randomColor()));

        // Камень - только случайный цвет
        registry.put(Type.STONE,
                new Block(Type.STONE, "blocks/stone.png",
                        new BlockProperties()
                                .randomColor()));
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
}