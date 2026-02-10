package SwordsGame.client.blocks;

import SwordsGame.client.assets.Paths;
import SwordsGame.client.graphics.Block;
import SwordsGame.client.graphics.BlockProperties;
import SwordsGame.client.graphics.BlockRenderer;
import groovy.lang.Closure;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Registry {
    private static final Map<Type, Block> registry = new HashMap<>();
    private static boolean destroyed = false;

    public static void init() {
        destroyed = false;
        registry.clear();
        try {
            evalGroovyDsl(readResource("/dsl/blocks.dsl"));
        } catch (Exception e) {
            System.err.println("[Registry] Failed to load Groovy block DSL, fallback to defaults: " + e.getMessage());
            registerDefaults();
        }
    }

    public static void evalGroovyDsl(String script) {
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("groovy");
        if (engine == null) {
            throw new IllegalStateException("Groovy ScriptEngine not found");
        }
        engine.put("registry", new GroovyRegistryApi());
        engine.put("Type", Type.class);
        engine.put("Paths", Paths.class);
        runPrelude(engine);
        eval(engine, script);
    }

    private static void runPrelude(ScriptEngine engine) {
        String prelude = "def blocks(Closure c){ registry.blocks(c) }";
        eval(engine, prelude);
    }

    private static void eval(ScriptEngine engine, String script) {
        try {
            engine.eval(script);
        } catch (Exception e) {
            throw new RuntimeException("Groovy DSL eval failed", e);
        }
    }

    private static String readResource(String path) {
        try (InputStream is = Registry.class.getResourceAsStream(path)) {
            if (is == null) {
                throw new IllegalStateException("Resource not found: " + path);
            }
            java.io.ByteArrayOutputStream os = new java.io.ByteArrayOutputStream();
            byte[] chunk = new byte[4096];
            int n;
            while ((n = is.read(chunk)) != -1) {
                os.write(chunk, 0, n);
            }
            return new String(os.toByteArray(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to read resource: " + path, e);
        }
    }

    private static void registerDefaults() {
        register(Type.AIR, null);
        register(Type.GRASS, new Block(Type.GRASS, Paths.BLOCK_GRASS,
                new BlockProperties().randomRotation().randomColor().smoothing().hardness(0.6f)));
        register(Type.COBBLE, new Block(Type.COBBLE, Paths.BLOCK_COBBLE,
                new BlockProperties().hardness(2.0f)));
        register(Type.STONE, new Block(Type.STONE, Paths.BLOCK_STONE,
                new BlockProperties().smoothing().hardness(3.0f)));
    }

    public static void register(Type type, Block block) {
        registry.put(type, block);
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

    public static void draw(byte id, int seed, boolean[] faces) {
        Block block = get(id);
        if (block != null) {
            block.draw(seed, faces);
        }
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

    public static class GroovyRegistryApi {
        public void blocks(Closure<?> closure) {
            BlocksDsl dsl = new BlocksDsl();
            configure(closure, dsl);
        }
    }

    public static class BlocksDsl {
        public void air(Closure<?> closure) { define(closure); }
        public void grass(Closure<?> closure) { define(closure); }
        public void cobble(Closure<?> closure) { define(closure); }
        public void stone(Closure<?> closure) { define(closure); }

        public void define(Closure<?> closure) {
            BlockDsl dsl = new BlockDsl();
            configure(closure, dsl);
            dsl.register();
        }
    }

    public static class BlockDsl {
        private Type type;
        private String texture;
        private String top;
        private String bottom;
        private String side;
        private final PropsDsl props = new PropsDsl();

        public void type(Type value) { this.type = value; }
        public void texture(String value) { this.texture = value; }
        public void top(String value) { this.top = value; }
        public void bottom(String value) { this.bottom = value; }
        public void side(String value) { this.side = value; }

        public void props(Closure<?> closure) {
            configure(closure, props);
        }

        void register() {
            if (type == null) {
                throw new IllegalStateException("Block type is required");
            }
            Block block;
            if (texture == null && top == null && bottom == null && side == null) {
                block = null;
            } else if (texture != null) {
                block = new Block(type, texture, props.build());
            } else {
                block = new Block(type, top, bottom, side, props.build());
            }
            Registry.register(type, block);
        }
    }

    public static class PropsDsl {
        private boolean randomRotation;
        private boolean randomColor;
        private boolean emission;
        private boolean transparent;
        private boolean nonSolid;
        private boolean smoothing;
        private float hardness = 1.0f;

        public boolean getRandomRotation() { randomRotation = true; return true; }
        public void randomRotation(boolean enabled) { randomRotation = enabled; }
        public boolean getRandomColor() { randomColor = true; return true; }
        public void randomColor(boolean enabled) { randomColor = enabled; }
        public boolean getEmission() { emission = true; return true; }
        public void emission(boolean enabled) { emission = enabled; }
        public boolean getTransparent() { transparent = true; return true; }
        public void transparent(boolean enabled) { transparent = enabled; }
        public boolean getSmoothing() { smoothing = true; return true; }
        public void smoothing(boolean enabled) { smoothing = enabled; }
        public boolean getNonSolid() { nonSolid = true; return true; }
        public void nonSolid(boolean enabled) { nonSolid = enabled; }
        public void solid(boolean enabled) { nonSolid = !enabled; }
        public void hardness(float value) { hardness = value; }

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

    private static void configure(Closure<?> closure, Object delegate) {
        closure.setResolveStrategy(Closure.DELEGATE_FIRST);
        closure.setDelegate(delegate);
        closure.call();
    }
}
