package SwordsGame.client.ui;

import SwordsGame.client.assets.Paths;

import groovy.lang.Closure;
import groovy.lang.GroovyShell;
import groovy.lang.Script;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HudScriptRunner {
    private final Script script;

    public HudScriptRunner() {
        this.script = compile(readResource(Paths.UI_HUD_SCRIPT));
    }

    public BaseFrame evaluateBase(Map<String, Object> context) {
        Map<String, Object> root = asMap(script.run());
        Object base = root.get("base");
        Map<String, Object> resolved = resolveMap(base, context);
        return new BaseFrame(
                parseSprites(resolved.get("sprites")),
                parseTexts(resolved.get("texts")),
                parseButtons(resolved.get("buttons"))
        );
    }

    public DialogFrame evaluateDialog(String dialogId, Map<String, Object> context) {
        if (dialogId == null || dialogId.isBlank()) {
            return DialogFrame.empty();
        }
        Map<String, Object> root = asMap(script.run());
        Map<String, Object> dialogs = asMap(root.get("dialogs"));
        Map<String, Object> dialog = resolveMap(dialogs.get(dialogId), context);
        if (dialog.isEmpty()) {
            return DialogFrame.empty();
        }
        return new DialogFrame(
                parseTexts(dialog.get("texts")),
                parseButtons(dialog.get("buttons"))
        );
    }

    private Script compile(String source) {
        return new GroovyShell().parse(source == null ? "[:]" : source);
    }

    private static String readResource(String path) {
        if (path == null || path.isBlank()) {
            return "[:]";
        }
        try (InputStream in = HudScriptRunner.class.getClassLoader().getResourceAsStream(path)) {
            if (in == null) {
                return "[:]";
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "[:]";
        }
    }

    private static Map<String, Object> resolveMap(Object source, Map<String, Object> context) {
        Object resolved = resolveValue(source, context);
        return asMap(resolved);
    }

    private static Object resolveValue(Object source, Map<String, Object> context) {
        if (source instanceof Closure<?> closure) {
            return closure.call(context);
        }
        return source;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> asMap(Object value) {
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> out = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                out.put(String.valueOf(entry.getKey()), entry.getValue());
            }
            return out;
        }
        return Collections.emptyMap();
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> asListOfMaps(Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object item : list) {
            if (item instanceof Map<?, ?> map) {
                Map<String, Object> out = new LinkedHashMap<>();
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    out.put(String.valueOf(entry.getKey()), entry.getValue());
                }
                result.add(out);
            }
        }
        return result;
    }

    private static List<SpriteDef> parseSprites(Object raw) {
        List<SpriteDef> result = new ArrayList<>();
        for (Map<String, Object> map : asListOfMaps(raw)) {
            result.add(new SpriteDef(
                    str(map.get("texture")),
                    str(map.get("pivot")),
                    parseX(map.get("alignX"), Anchor.LEFT),
                    parseY(map.get("alignY"), Anchor.TOP),
                    num(map.get("x"), 0f),
                    num(map.get("y"), 0f),
                    num(map.get("scale"), 1f)
            ));
        }
        return result;
    }

    private static List<TextDef> parseTexts(Object raw) {
        List<TextDef> result = new ArrayList<>();
        for (Map<String, Object> map : asListOfMaps(raw)) {
            result.add(new TextDef(
                    str(map.get("text")),
                    str(map.get("pivot")),
                    parseX(map.get("alignX"), Anchor.LEFT),
                    parseY(map.get("alignY"), Anchor.TOP),
                    num(map.get("x"), 0f),
                    num(map.get("y"), 0f),
                    num(map.get("scale"), 1f)
            ));
        }
        return result;
    }

    private static List<ButtonDef> parseButtons(Object raw) {
        List<ButtonDef> result = new ArrayList<>();
        for (Map<String, Object> map : asListOfMaps(raw)) {
            result.add(new ButtonDef(
                    str(map.get("id")),
                    str(map.get("label")),
                    str(map.get("pivot")),
                    parseX(map.get("alignX"), Anchor.LEFT),
                    parseY(map.get("alignY"), Anchor.TOP),
                    num(map.get("x"), 0f),
                    num(map.get("y"), 0f),
                    num(map.get("width"), 100f),
                    num(map.get("height"), 28f),
                    num(map.get("scale"), 1f),
                    bool(map.get("active"), true)
            ));
        }
        return result;
    }

    private static String str(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private static float num(Object value, float fallback) {
        if (value instanceof Number n) {
            return n.floatValue();
        }
        try {
            return value == null ? fallback : Float.parseFloat(String.valueOf(value));
        } catch (Exception e) {
            return fallback;
        }
    }

    private static boolean bool(Object value, boolean fallback) {
        if (value instanceof Boolean b) {
            return b;
        }
        if (value == null) {
            return fallback;
        }
        return Boolean.parseBoolean(String.valueOf(value));
    }

    private static Anchor.TypeX parseX(Object value, Anchor.TypeX fallback) {
        try {
            return Anchor.TypeX.valueOf(str(value).toUpperCase());
        } catch (Exception e) {
            return fallback;
        }
    }

    private static Anchor.TypeY parseY(Object value, Anchor.TypeY fallback) {
        String normalized = str(value).toUpperCase();
        if ("CENTER_Y".equals(normalized)) {
            normalized = "CENTER";
        }
        try {
            return Anchor.TypeY.valueOf(normalized);
        } catch (Exception e) {
            return fallback;
        }
    }

    public record BaseFrame(List<SpriteDef> sprites, List<TextDef> texts, List<ButtonDef> buttons) {
    }

    public record DialogFrame(List<TextDef> texts, List<ButtonDef> buttons) {
        static DialogFrame empty() {
            return new DialogFrame(List.of(), List.of());
        }
    }

    public record SpriteDef(String texture, String pivot, Anchor.TypeX alignX, Anchor.TypeY alignY,
                            float x, float y, float scale) {
    }

    public record TextDef(String text, String pivot, Anchor.TypeX alignX, Anchor.TypeY alignY,
                          float x, float y, float scale) {
    }

    public record ButtonDef(String id, String label, String pivot, Anchor.TypeX alignX, Anchor.TypeY alignY,
                            float x, float y, float width, float height, float scale, boolean active) {
    }
}
