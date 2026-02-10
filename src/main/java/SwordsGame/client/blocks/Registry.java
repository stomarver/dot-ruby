package SwordsGame.client.blocks;

import SwordsGame.client.assets.Paths;
import SwordsGame.client.graphics.Block;
import SwordsGame.client.graphics.BlockProperties;
import SwordsGame.client.graphics.BlockRenderer;
import groovy.lang.Closure;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public final class Registry {
    private static final Map<Type, Block> REGISTRY = new LinkedHashMap<>();
    private static final String DEFAULT_BLOCKS_DSL = String.join("\n",
            "blocks {",
            "    air {",
            "        type Type.AIR",
            "        props { nonSolid }",
            "    }",
            "",
            "    grass {",
            "        type Type.GRASS",
            "        texture Paths.BLOCK_GRASS",
            "        props {",
            "            randomRotation",
            "            randomColor",
            "            smoothing",
            "            hardness 0.6f",
            "        }",
            "    }",
            "",
            "    cobble {",
            "        type Type.COBBLE",
            "        texture Paths.BLOCK_COBBLE",
            "        props { hardness 2.0f }",
            "    }",
            "",
            "    stone {",
            "        type Type.STONE",
            "        texture Paths.BLOCK_STONE",
            "        props {",
            "            smoothing",
            "            hardness 3.0f",
            "        }",
            "    }",
            "}");

    private static boolean destroyed;

    private Registry() {
    }

    public static void init() {
        destroyed = false;
        resetToDefaults();
    }



    public static void resetToDefaults() {
        REGISTRY.clear();
        registerScript(DEFAULT_BLOCKS_DSL);
    }

    public static void registerScripts(Collection<String> scripts) {
        if (scripts == null) {
            return;
        }
        for (String script : scripts) {
            registerScript(script);
        }
    }

    public static void registerScript(String script) {
        if (script == null || script.trim().isEmpty()) {
            throw new IllegalArgumentException("Block DSL script must not be empty");
        }
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("groovy");
        if (engine == null) {
            throw new IllegalStateException("Groovy ScriptEngine not found");
        }
        engine.put("registry", new GroovyRegistryApi());
        engine.put("Type", Type.class);
        engine.put("Paths", Paths.class);
        eval(engine, "def blocks(Closure c){ registry.blocks(c) }");
        eval(engine, script);
    }

    private static void eval(ScriptEngine engine, String script) {
        try {
            engine.eval(script);
        } catch (Exception e) {
            throw new RuntimeException("Groovy DSL eval failed", e);
        }
    }

    public static void register(Type type, Block block) {
        REGISTRY.put(type, block);
    }

    public static Block get(Type type) {
        return REGISTRY.get(type);
    }

    public static Block get(byte id) {
        return REGISTRY.get(Type.fromId(id));
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
        for (Block block : REGISTRY.values()) {
            if (block != null) {
                block.destroy();
            }
        }
        BlockRenderer.clear();
        destroyed = true;
    }

    public static final class GroovyRegistryApi {
        public void blocks(Closure<?> closure) {
            configure(closure, new BlocksDsl());
        }
    }

    public static final class BlocksDsl {
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

    public static final class BlockDsl {
        private Type type;
        private String texture;
        private String top;
        private String bottom;
        private String side;
        private final PropsDsl props = new PropsDsl();

        public void type(Type value) { type = value; }
        public void texture(String value) { texture = value; }
        public void top(String value) { top = value; }
        public void bottom(String value) { bottom = value; }
        public void side(String value) { side = value; }

        public void props(Closure<?> closure) {
            configure(closure, props);
        }

        void register() {
            if (type == null) {
                throw new IllegalStateException("Block type is required");
            }
            if (texture == null && top == null && bottom == null && side == null) {
                Registry.register(type, null);
                return;
            }
            Block block = texture != null
                    ? new Block(type, texture, props.build())
                    : new Block(type, top, bottom, side, props.build());
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
            BlockProperties p = new BlockProperties().hardness(hardness);
            if (randomRotation) p.randomRotation();
            if (randomColor) p.randomColor();
            if (emission) p.emission();
            if (transparent) p.transparent();
            if (smoothing) p.smoothing();
            if (nonSolid) p.nonSolid();
            return p;
        }
    }

    private static void configure(Closure<?> closure, Object delegate) {
        closure.setResolveStrategy(Closure.DELEGATE_FIRST);
        closure.setDelegate(delegate);
        closure.call();
    }
}
