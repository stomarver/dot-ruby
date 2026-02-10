package SwordsGame.ui.data.text;

import groovy.lang.Closure;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public final class TextRegistry {
    private static final String DSL_RESOURCE = "/data/ui/text/text.dsl";
    private static final Map<String, String> TEXTS = new LinkedHashMap<>();

    private TextRegistry() {
    }

    public static void init() {
        TEXTS.clear();
        registerScript(readResource(DSL_RESOURCE));
    }

    public static String get(String key, String fallback) {
        String value = TEXTS.get(key);
        return value == null ? fallback : value;
    }

    private static void registerScript(String script) {
        if (script == null || script.trim().isEmpty()) {
            return;
        }
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("groovy");
        if (engine == null) {
            throw new IllegalStateException("Groovy ScriptEngine not found");
        }
        engine.put("registry", new TextRegistryApi());
        String fullScript = "def text(Closure c){ registry.text(c) }\n" + script;
        try {
            engine.eval(fullScript);
        } catch (Exception e) {
            throw new RuntimeException("Groovy UI text DSL eval failed", e);
        }
    }

    private static String readResource(String path) {
        try (InputStream is = TextRegistry.class.getResourceAsStream(path)) {
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

    public static final class TextRegistryApi {
        public void text(Closure<?> closure) {
            TextDsl dsl = new TextDsl();
            closure.setResolveStrategy(Closure.DELEGATE_FIRST);
            closure.setDelegate(dsl);
            closure.call();
        }
    }

    public static final class TextDsl {
        public void entry(String key, String value) {
            if (key == null || key.trim().isEmpty()) {
                return;
            }
            TEXTS.put(key.trim(), value == null ? "" : value);
        }

        public void key(String key, String value) {
            entry(key, value);
        }
    }
}
