package SwordsGame.server.data.blocks;

import groovy.lang.Closure;
import groovy.lang.MissingMethodException;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public final class Registry {
    private static final Map<Type, BlockData> DATA = new LinkedHashMap<>();
    private static final StringBuilder ACTIVE_SCRIPTS = new StringBuilder();
    private static final String DSL_RESOURCE = "/SwordsGame/server/data/blocks/blocks.dsl";

    private Registry() {
    }

    public static void init() {
        resetToDefaults();
    }

    public static void resetToDefaults() {
        DATA.clear();
        ACTIVE_SCRIPTS.setLength(0);
        registerScript(readResource(DSL_RESOURCE));
    }

    public static void registerScripts(Collection<String> scripts) {
        if (scripts == null) return;
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
        eval(engine,
                "import SwordsGame.server.data.blocks.Type\n" +
                "def blocks(Closure c){ registry.blocks(c) }");
        eval(engine, script);

        if (ACTIVE_SCRIPTS.length() > 0) ACTIVE_SCRIPTS.append("\n\n");
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

    private static String readResource(String path) {
        try (InputStream is = Registry.class.getResourceAsStream(path)) {
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

    public static final class DataRegistryApi {
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
        private final PropsDsl props = new PropsDsl();

        private BlockDsl(Type blockType) {
            this.blockType = blockType;
        }

        public void type(Type ignored) { }
        public void texture(String ignored) { }
        public void top(String ignored) { }
        public void bottom(String ignored) { }
        public void side(String ignored) { }

        public void props(Closure<?> closure) {
            configure(closure, props);
        }

        void register() {
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
