package SwordsGame.client.blocks;

import SwordsGame.client.assets.Paths;
import SwordsGame.client.graphics.Block;
import SwordsGame.client.graphics.BlockProperties;
import SwordsGame.client.graphics.BlockRenderer;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class Registry {
    private static final Map<Type, Block> registry = new HashMap<>();
    private static boolean destroyed = false;

    public static void init() {
        destroyed = false;
        registry.clear();

        blocks(def -> {
            def.air(b -> {
                b.type(Type.AIR);
                b.props(p -> p.nonSolid());
            });
            def.grass(b -> {
                b.type(Type.GRASS);
                b.texture(Paths.BLOCK_GRASS);
                b.props(p -> p.randomRotation().randomColor().smoothing().hardness(0.6f));
            });
            def.cobble(b -> {
                b.type(Type.COBBLE);
                b.texture(Paths.BLOCK_COBBLE);
                b.props(p -> p.hardness(2.0f));
            });
            def.stone(b -> {
                b.type(Type.STONE);
                b.texture(Paths.BLOCK_STONE);
                b.props(p -> p.smoothing().hardness(3.0f));
            });
        });
    }

    public static void blocks(Consumer<BlocksDsl> script) {
        BlocksDsl dsl = new BlocksDsl();
        script.accept(dsl);
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

    public static final class BlocksDsl {
        public void air(Consumer<BlockDsl> c) { define(c); }
        public void grass(Consumer<BlockDsl> c) { define(c); }
        public void cobble(Consumer<BlockDsl> c) { define(c); }
        public void stone(Consumer<BlockDsl> c) { define(c); }

        public void define(Consumer<BlockDsl> c) {
            BlockDsl dsl = new BlockDsl();
            c.accept(dsl);
            dsl.register();
        }
    }

    public static final class BlockDsl {
        private Type type;
        private String texture;
        private String top;
        private String bottom;
        private String side;
        private final PropsDsl propsDsl = new PropsDsl();

        public void type(Type value) { this.type = value; }
        public void texture(String value) { this.texture = value; }
        public void top(String value) { this.top = value; }
        public void bottom(String value) { this.bottom = value; }
        public void side(String value) { this.side = value; }
        public void props(Consumer<PropsDsl> c) { c.accept(propsDsl); }

        void register() {
            if (type == null) {
                throw new IllegalStateException("Block DSL requires type");
            }
            BlockProperties properties = propsDsl.build();
            Block block;
            if (texture == null && top == null && bottom == null && side == null) {
                block = null;
            } else if (texture != null) {
                block = new Block(type, texture, properties);
            } else {
                block = new Block(type, top, bottom, side, properties);
            }
            Registry.register(type, block);
        }
    }

    public static final class PropsDsl {
        private boolean randomRotation;
        private boolean randomColor;
        private boolean emission;
        private boolean transparent;
        private boolean nonSolid;
        private boolean smoothing;
        private float hardness = 1.0f;

        public PropsDsl randomRotation() { this.randomRotation = true; return this; }
        public PropsDsl randomRotation(boolean enabled) { this.randomRotation = enabled; return this; }
        public PropsDsl randomColor() { this.randomColor = true; return this; }
        public PropsDsl randomColor(boolean enabled) { this.randomColor = enabled; return this; }
        public PropsDsl emission() { this.emission = true; return this; }
        public PropsDsl emission(boolean enabled) { this.emission = enabled; return this; }
        public PropsDsl transparent() { this.transparent = true; return this; }
        public PropsDsl transparent(boolean enabled) { this.transparent = enabled; return this; }
        public PropsDsl smoothing() { this.smoothing = true; return this; }
        public PropsDsl smoothing(boolean enabled) { this.smoothing = enabled; return this; }
        public PropsDsl nonSolid() { this.nonSolid = true; return this; }
        public PropsDsl nonSolid(boolean enabled) { this.nonSolid = enabled; return this; }
        public PropsDsl solid(boolean enabled) { this.nonSolid = !enabled; return this; }
        public PropsDsl hardness(float value) { this.hardness = value; return this; }

        BlockProperties build() {
            BlockProperties properties = new BlockProperties().hardness(hardness);
            if (randomRotation) properties.randomRotation();
            if (randomColor) properties.randomColor();
            if (emission) properties.emission();
            if (transparent) properties.transparent();
            if (smoothing) properties.smoothing();
            if (nonSolid) properties.nonSolid();
            return properties;
        }
    }
}
