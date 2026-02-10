package SwordsGame.client.blocks;

import SwordsGame.client.assets.Paths;
import SwordsGame.client.graphics.Block;
import SwordsGame.client.graphics.BlockProperties;
import SwordsGame.client.graphics.BlockRenderer;
import SwordsGame.server.data.blocks.Type;
import groovy.lang.Closure;
import groovy.lang.MissingMethodException;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public final class RenderRegistry {
    private static final Map<Type, Block> REGISTRY = new LinkedHashMap<>();
    private static final String DSL_RESOURCE = "/SwordsGame/client/blocks/blocks.dsl";
    private static boolean destroyed;

    private RenderRegistry() {
    }

    public static void initFromServerDsl() {
        destroyed = false;
        REGISTRY.clear();
        registerScript(readResource(DSL_RESOURCE));
    }

    public static void registerScript(String script) {
        if (script == null || script.trim().isEmpty()) {
            return;
        }
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("groovy");
        if (engine == null) {
            throw new IllegalStateException("Groovy ScriptEngine not found");
        }

        engine.put("registry", new RenderRegistryApi());
        engine.put("Paths", Paths.class);

        eval(engine,
                "import SwordsGame.server.data.blocks.Type\n" +
                "import SwordsGame.client.assets.Paths\n" +
                "def blocks(Closure c){ registry.blocks(c) }");
        eval(engine, script);
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

    private static void register(Type type, Block block) {
        REGISTRY.put(type, block);
    }

    private static void eval(ScriptEngine engine, String script) {
        try {
            engine.eval(script);
        } catch (Exception e) {
            throw new RuntimeException("Groovy render block DSL eval failed", e);
        }
    }

    private static String readResource(String path) {
        try (InputStream is = RenderRegistry.class.getResourceAsStream(path)) {
            if (is == null) {
                throw new IllegalStateException("Resource not found: " + path);
            }
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int n;
            while ((n = is.read(buf)) != -1) {
                os.write(buf, 0, n);
            }
            return new String(os.toByteArray(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to read resource: " + path, e);
        }
    }

    public static final class RenderRegistryApi {
        public void blocks(Closure<?> closure) {
            configure(closure, new BlocksDsl());
        }
    }

    public static final class BlocksDsl {
        public void define(Type type, Closure<?> closure) {
            BlockDsl dsl = new BlockDsl(type);
            configure(closure, dsl);
            dsl.register();
        }

        public Object invokeMethod(String name, Object args) {
            Type type = resolveType(name);
            if (type == null) {
                throw new MissingMethodException(name, getClass(), toArgs(args));
            }
            Object[] arr = toArgs(args);
            if (arr.length != 1 || !(arr[0] instanceof Closure)) {
                throw new IllegalArgumentException("Block declaration expects a closure: " + name + " { ... }");
            }
            define(type, (Closure<?>) arr[0]);
            return null;
        }

        private Type resolveType(String name) {
            String normalized = name.toUpperCase(Locale.ROOT);
            for (Type type : Type.values()) {
                if (type.name().equals(normalized)) return type;
            }
            return null;
        }
    }

    public static final class BlockDsl {
        private final Type blockType;
        private String texture;
        private String top;
        private String bottom;
        private String side;
        private final PropsDsl props = new PropsDsl();

        private BlockDsl(Type blockType) {
            this.blockType = blockType;
        }

        public void type(Type ignored) { }
        public void texture(String value) { this.texture = value; }
        public void top(String value) { this.top = value; }
        public void bottom(String value) { this.bottom = value; }
        public void side(String value) { this.side = value; }

        public void props(Closure<?> closure) {
            configure(closure, props);
        }

        void register() {
            if (texture == null && top == null && bottom == null && side == null) {
                RenderRegistry.register(blockType, null);
                return;
            }
            Block block = texture != null
                    ? new Block(blockType, texture, props.build())
                    : new Block(blockType, top, bottom, side, props.build());
            RenderRegistry.register(blockType, block);
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

    private static Object[] toArgs(Object args) {
        if (args == null) return new Object[0];
        if (args instanceof Object[]) return (Object[]) args;
        return new Object[]{args};
    }

    private static void configure(Closure<?> closure, Object delegate) {
        closure.setResolveStrategy(Closure.DELEGATE_FIRST);
        closure.setDelegate(delegate);
        closure.call();
    }
}
