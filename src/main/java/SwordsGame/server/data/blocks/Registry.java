package SwordsGame.server.data.blocks;

import SwordsGame.client.assets.Paths;
import groovy.lang.Closure;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public final class Registry {
    private static final Map<Type, BlockData> DATA = new LinkedHashMap<>();
    private static final StringBuilder ACTIVE_SCRIPTS = new StringBuilder();

    private Registry() {
    }

    public static void init() {
        resetToDefaults();
    }

    public static void resetToDefaults() {
        DATA.clear();
        ACTIVE_SCRIPTS.setLength(0);
        registerScript(defaultBlocksDsl());
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

        engine.put("registry", new DataRegistryApi());
        engine.put("Type", Type.class);
        engine.put("Paths", Paths.class);

        eval(engine,
                "import SwordsGame.server.data.blocks.Type\n" +
                "import SwordsGame.client.assets.Paths\n" +
                "def blocks(Closure c){ registry.blocks(c) }");
        eval(engine, script);

        if (ACTIVE_SCRIPTS.length() > 0) {
            ACTIVE_SCRIPTS.append("\n\n");
        }
        ACTIVE_SCRIPTS.append(script.trim());
    }

    public static String getActiveDsl() {
        return ACTIVE_SCRIPTS.toString();
    }

    public static BlockData get(Type type) {
        return DATA.get(type);
    }

    public static BlockData get(byte id) {
        return DATA.get(Type.fromId(id));
    }

    public static boolean isSolid(byte id) {
        BlockData data = get(id);
        return data != null && data.isSolid();
    }

    private static void register(Type type, BlockData data) {
        DATA.put(type, data);
    }

    private static void eval(ScriptEngine engine, String script) {
        try {
            engine.eval(script);
        } catch (Exception e) {
            throw new RuntimeException("Groovy block data DSL eval failed", e);
        }
    }

    private static String defaultBlocksDsl() {
        return "blocks {\n" +
                "    air {\n" +
                "        type Type.AIR\n" +
                "        props { nonSolid }\n" +
                "    }\n\n" +
                "    grass {\n" +
                "        type Type.GRASS\n" +
                "        texture Paths.BLOCK_GRASS\n" +
                "        props {\n" +
                "            randomRotation\n" +
                "            randomColor\n" +
                "            smoothing\n" +
                "            hardness 0.6f\n" +
                "        }\n" +
                "    }\n\n" +
                "    cobble {\n" +
                "        type Type.COBBLE\n" +
                "        texture Paths.BLOCK_COBBLE\n" +
                "        props { hardness 2.0f }\n" +
                "    }\n\n" +
                "    stone {\n" +
                "        type Type.STONE\n" +
                "        texture Paths.BLOCK_STONE\n" +
                "        props {\n" +
                "            smoothing\n" +
                "            hardness 3.0f\n" +
                "        }\n" +
                "    }\n" +
                "}";
    }

    public static final class DataRegistryApi {
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
        private Type blockType;
        private final PropsDsl props = new PropsDsl();

        public void type(Type value) { this.blockType = value; }
        public void texture(String ignored) { }
        public void top(String ignored) { }
        public void bottom(String ignored) { }
        public void side(String ignored) { }

        public void props(Closure<?> closure) {
            configure(closure, props);
        }

        void register() {
            if (blockType == null) {
                throw new IllegalStateException("Block type is required");
            }
            Registry.register(blockType, new BlockData(blockType, props.solid, props.hardness));
        }
    }

    public static final class PropsDsl {
        private boolean solid = true;
        private float hardness = 1.0f;

        public boolean getNonSolid() { solid = false; return true; }
        public void nonSolid(boolean enabled) { solid = !enabled; }
        public void solid(boolean enabled) { solid = enabled; }
        public void hardness(float value) { hardness = value; }

        public boolean getRandomRotation() { return true; }
        public void randomRotation(boolean ignored) { }
        public boolean getRandomColor() { return true; }
        public void randomColor(boolean ignored) { }
        public boolean getEmission() { return true; }
        public void emission(boolean ignored) { }
        public boolean getTransparent() { return true; }
        public void transparent(boolean ignored) { }
        public boolean getSmoothing() { return true; }
        public void smoothing(boolean ignored) { }
    }

    private static void configure(Closure<?> closure, Object delegate) {
        closure.setResolveStrategy(Closure.OWNER_FIRST);
        closure.setDelegate(delegate);
        closure.call();
    }
}
