package SwordsGame.client.blocks;

import SwordsGame.client.assets.Syn;
import SwordsGame.client.graphics.Block;

import groovy.lang.GroovyShell;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class BlockScriptLoader {
    private static final String BLOCK_SCRIPT = "shared/blocks/blocks.groovy";

    private BlockScriptLoader() {
    }

    public static List<RegisteredBlock> loadBlocks() {
        Object raw = new GroovyShell().evaluate(readResource(BLOCK_SCRIPT));
        if (!(raw instanceof List<?> list)) {
            return List.of();
        }
        List<RegisteredBlock> loaded = new ArrayList<>();
        for (Object row : list) {
            if (!(row instanceof Map<?, ?> map)) {
                continue;
            }
            String typeName = str(map.get("type"));
            Type type;
            try {
                type = Type.valueOf(typeName.toUpperCase());
            } catch (Exception e) {
                continue;
            }
            if (type == Type.AIR) {
                continue;
            }

            Map<String, Object> tex = toMap(map.get("tex"));
            Map<String, Object> props = toMap(map.get("props"));

            Syn.BlkDef def = Syn.blk(type);
            String one = str(tex.get("one"));
            if (!one.isBlank()) {
                def.tex(one);
            } else {
                def.tex(str(tex.get("top")), str(tex.get("bottom")), str(tex.get("side")));
            }

            def.props(p -> {
                if (bool(props.get("randomRotation"))) p.randomRotation();
                if (bool(props.get("randomColor"))) {
                    float shift = num(props.get("randomColorShift"), -1f);
                    if (shift > 0f) p.randomColor(shift);
                    else p.randomColor();
                }
                if (bool(props.get("emission"))) p.emission();
                if (bool(props.get("transparent"))) p.transparent();
                if (bool(props.get("nonSolid"))) p.nonSolid();
                if (bool(props.get("smoothing"))) p.smoothing();
                if (bool(props.get("destructible"))) p.destructible();
                if (bool(props.get("surfaceOnly"))) p.surfaceOnly();
                float hardness = num(props.get("hardness"), Float.NaN);
                if (!Float.isNaN(hardness)) p.hardness(hardness);
            });

            Block block = def.build();
            loaded.add(new RegisteredBlock(type, block));
        }
        return loaded;
    }

    private static String readResource(String path) {
        try (InputStream in = BlockScriptLoader.class.getClassLoader().getResourceAsStream(path)) {
            if (in == null) {
                return "[]";
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "[]";
        }
    }

    private static Map<String, Object> toMap(Object raw) {
        if (raw instanceof Map<?, ?> map) {
            Map<String, Object> out = new LinkedHashMap<>();
            for (Map.Entry<?, ?> e : map.entrySet()) {
                out.put(String.valueOf(e.getKey()), e.getValue());
            }
            return out;
        }
        return Map.of();
    }

    private static String str(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private static boolean bool(Object value) {
        if (value instanceof Boolean b) {
            return b;
        }
        return value != null && Boolean.parseBoolean(String.valueOf(value));
    }

    private static float num(Object value, float fallback) {
        if (value instanceof Number n) {
            return n.floatValue();
        }
        if (value == null) {
            return fallback;
        }
        try {
            return Float.parseFloat(String.valueOf(value));
        } catch (Exception e) {
            return fallback;
        }
    }

    public record RegisteredBlock(Type type, Block block) {
    }
}
