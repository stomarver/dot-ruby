package SwordsGame.client.graphics;

import org.joml.Vector3f;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Minimal glTF 2.0 / GLB loader without extra dependencies.
 *
 * Supported now:
 * - .glb and .gltf
 * - POSITION (required), NORMAL (optional), TEXCOORD_0 (optional), indices (optional)
 * - Primitive mode TRIANGLES (4)
 *
 * Not yet supported:
 * - skins/animations/morph targets/material extensions/sparse accessors
 */
public final class GltfModel {
    public static final int GLTF_COMPONENT_UNSIGNED_BYTE = 5121;
    public static final int GLTF_COMPONENT_UNSIGNED_SHORT = 5123;
    public static final int GLTF_COMPONENT_UNSIGNED_INT = 5125;
    public static final int GLTF_COMPONENT_FLOAT = 5126;
    public static final int GLTF_MODE_TRIANGLES = 4;

    public final List<Primitive> primitives;
    public final Vector3f minBounds;
    public final Vector3f maxBounds;

    private GltfModel(List<Primitive> primitives, Vector3f minBounds, Vector3f maxBounds) {
        this.primitives = primitives;
        this.minBounds = minBounds;
        this.maxBounds = maxBounds;
    }

    public static GltfModel load(Path path) throws IOException {
        String file = path.getFileName().toString().toLowerCase(Locale.ROOT);
        if (file.endsWith(".glb")) {
            return loadGlb(path);
        }
        if (file.endsWith(".gltf")) {
            return loadGltf(path);
        }
        throw new IOException("Unsupported model extension: " + path);
    }

    private static GltfModel loadGlb(Path path) throws IOException {
        byte[] bytes = Files.readAllBytes(path);
        ByteBuffer bb = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);

        int magic = bb.getInt();
        int version = bb.getInt();
        int length = bb.getInt();
        if (magic != 0x46546C67 || version != 2 || length != bytes.length) {
            throw new IOException("Invalid GLB header: " + path);
        }

        String json = null;
        ByteBuffer binChunk = null;

        while (bb.remaining() >= 8) {
            int chunkLen = bb.getInt();
            int chunkType = bb.getInt();
            if (chunkLen < 0 || chunkLen > bb.remaining()) throw new IOException("Invalid GLB chunk");

            byte[] chunkData = new byte[chunkLen];
            bb.get(chunkData);

            if (chunkType == 0x4E4F534A) { // JSON
                json = new String(chunkData, StandardCharsets.UTF_8).trim();
            } else if (chunkType == 0x004E4942) { // BIN
                binChunk = ByteBuffer.wrap(chunkData).order(ByteOrder.LITTLE_ENDIAN);
            }
        }

        if (json == null) throw new IOException("GLB has no JSON chunk: " + path);
        List<ByteBuffer> buffers = new ArrayList<>();
        if (binChunk != null) buffers.add(binChunk);
        return parseGltfDocument(path.getParent(), json, buffers);
    }

    private static GltfModel loadGltf(Path path) throws IOException {
        String json = Files.readString(path);
        return parseGltfDocument(path.getParent(), json, null);
    }

    private static GltfModel parseGltfDocument(Path baseDir, String json, List<ByteBuffer> preloadedBuffers) throws IOException {
        Object rootObj = new Json(json).parse();
        if (!(rootObj instanceof Map<?, ?> rawRoot)) throw new IOException("Invalid glTF JSON root");

        @SuppressWarnings("unchecked")
        Map<String, Object> root = (Map<String, Object>) rawRoot;

        List<Map<String, Object>> buffersDef = getObjectList(root, "buffers");
        List<Map<String, Object>> bufferViews = getObjectList(root, "bufferViews");
        List<Map<String, Object>> accessors = getObjectList(root, "accessors");
        List<Map<String, Object>> meshes = getObjectList(root, "meshes");

        List<ByteBuffer> buffers = preloadedBuffers != null ? preloadedBuffers : new ArrayList<>();
        if (preloadedBuffers == null) {
            for (Map<String, Object> b : buffersDef) {
                String uri = getString(b, "uri", null);
                if (uri == null) throw new IOException("External buffer uri is missing in .gltf");
                buffers.add(loadBufferUri(baseDir, uri));
            }
        }

        List<Primitive> out = new ArrayList<>();
        Vector3f minB = new Vector3f(Float.POSITIVE_INFINITY);
        Vector3f maxB = new Vector3f(Float.NEGATIVE_INFINITY);

        for (Map<String, Object> mesh : meshes) {
            List<Map<String, Object>> primitives = getObjectList(mesh, "primitives");
            for (Map<String, Object> primitive : primitives) {
                int mode = getInt(primitive, "mode", GLTF_MODE_TRIANGLES);
                if (mode != GLTF_MODE_TRIANGLES) continue;

                Map<String, Object> attrs = getObject(primitive, "attributes");
                int posAccessorId = getInt(attrs, "POSITION", -1);
                if (posAccessorId < 0) continue;

                int normalAccessorId = getInt(attrs, "NORMAL", -1);
                int uvAccessorId = getInt(attrs, "TEXCOORD_0", -1);
                int idxAccessorId = getInt(primitive, "indices", -1);

                float[] pos = readFloatAccessor(accessors.get(posAccessorId), bufferViews, buffers, 3);
                float[] nrm = normalAccessorId >= 0 ? readFloatAccessor(accessors.get(normalAccessorId), bufferViews, buffers, 3) : null;
                float[] uv = uvAccessorId >= 0 ? readFloatAccessor(accessors.get(uvAccessorId), bufferViews, buffers, 2) : null;
                int[] idx = idxAccessorId >= 0 ? readIndexAccessor(accessors.get(idxAccessorId), bufferViews, buffers) : null;

                Primitive p = new Primitive(pos, nrm, uv, idx);
                out.add(p);
                expandBounds(minB, maxB, pos);
            }
        }

        if (out.isEmpty()) throw new IOException("No supported TRIANGLES primitives found");
        return new GltfModel(out, minB, maxB);
    }

    public FloatCollector toFloatCollector() {
        FloatCollector collector = new FloatCollector(4096);
        for (Primitive p : primitives) {
            int triVerts = p.indices != null ? p.indices.length : p.positions.length / 3;
            for (int i = 0; i < triVerts; i++) {
                int v = p.indices != null ? p.indices[i] : i;
                int pi = v * 3;
                float x = p.positions[pi];
                float y = p.positions[pi + 1];
                float z = p.positions[pi + 2];

                float nx = 0f, ny = 1f, nz = 0f;
                if (p.normals != null && (v * 3 + 2) < p.normals.length) {
                    nx = p.normals[pi];
                    ny = p.normals[pi + 1];
                    nz = p.normals[pi + 2];
                }

                float u = 0f, vv = 0f;
                if (p.uvs != null && (v * 2 + 1) < p.uvs.length) {
                    int ti = v * 2;
                    u = p.uvs[ti];
                    vv = p.uvs[ti + 1];
                }

                collector.add(x, y, z, nx, ny, nz, u, vv, 1f, 1f, 1f);
            }
        }
        return collector;
    }

    public MeshBuffer toMeshBuffer() {
        return MeshBuffer.build(toFloatCollector());
    }

    public static final class Primitive {
        public final float[] positions;
        public final float[] normals;
        public final float[] uvs;
        public final int[] indices;

        public Primitive(float[] positions, float[] normals, float[] uvs, int[] indices) {
            this.positions = positions;
            this.normals = normals;
            this.uvs = uvs;
            this.indices = indices;
        }
    }

    private static void expandBounds(Vector3f minB, Vector3f maxB, float[] pos) {
        for (int i = 0; i < pos.length; i += 3) {
            float x = pos[i], y = pos[i + 1], z = pos[i + 2];
            minB.x = Math.min(minB.x, x);
            minB.y = Math.min(minB.y, y);
            minB.z = Math.min(minB.z, z);
            maxB.x = Math.max(maxB.x, x);
            maxB.y = Math.max(maxB.y, y);
            maxB.z = Math.max(maxB.z, z);
        }
    }

    private static ByteBuffer loadBufferUri(Path baseDir, String uri) throws IOException {
        byte[] data;
        if (uri.startsWith("data:")) {
            int comma = uri.indexOf(',');
            if (comma < 0) throw new IOException("Invalid data URI buffer");
            String base64 = uri.substring(comma + 1);
            data = Base64.getDecoder().decode(base64);
        } else {
            Path resolved = (baseDir == null) ? Path.of(uri) : baseDir.resolve(uri);
            data = Files.readAllBytes(resolved);
        }
        return ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
    }

    private static float[] readFloatAccessor(Map<String, Object> accessor, List<Map<String, Object>> views, List<ByteBuffer> buffers, int expectedVecSize) throws IOException {
        int componentType = getInt(accessor, "componentType", -1);
        String type = getString(accessor, "type", "");
        int count = getInt(accessor, "count", 0);
        int vecSize = typeToElements(type);
        if (componentType != GLTF_COMPONENT_FLOAT || vecSize != expectedVecSize) {
            throw new IOException("Unsupported accessor format. Need FLOAT " + expectedVecSize + ", got component=" + componentType + " type=" + type);
        }
        ByteBuffer slice = resolveAccessorBuffer(accessor, views, buffers, Float.BYTES);
        float[] out = new float[count * vecSize];
        FloatBuffer fb = slice.order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer();
        fb.get(out);
        return out;
    }

    private static int[] readIndexAccessor(Map<String, Object> accessor, List<Map<String, Object>> views, List<ByteBuffer> buffers) throws IOException {
        int componentType = getInt(accessor, "componentType", -1);
        int count = getInt(accessor, "count", 0);
        int compBytes = switch (componentType) {
            case GLTF_COMPONENT_UNSIGNED_BYTE -> 1;
            case GLTF_COMPONENT_UNSIGNED_SHORT -> 2;
            case GLTF_COMPONENT_UNSIGNED_INT -> 4;
            default -> throw new IOException("Unsupported index component type: " + componentType);
        };
        ByteBuffer slice = resolveAccessorBuffer(accessor, views, buffers, compBytes);
        int[] out = new int[count];
        switch (componentType) {
            case GLTF_COMPONENT_UNSIGNED_BYTE -> {
                for (int i = 0; i < count; i++) out[i] = slice.get(i) & 0xFF;
            }
            case GLTF_COMPONENT_UNSIGNED_SHORT -> {
                ShortBuffer sb = slice.order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();
                for (int i = 0; i < count; i++) out[i] = sb.get(i) & 0xFFFF;
            }
            case GLTF_COMPONENT_UNSIGNED_INT -> {
                IntBuffer ib = slice.order(ByteOrder.LITTLE_ENDIAN).asIntBuffer();
                for (int i = 0; i < count; i++) out[i] = ib.get(i);
            }
        }
        return out;
    }

    private static ByteBuffer resolveAccessorBuffer(Map<String, Object> accessor, List<Map<String, Object>> views, List<ByteBuffer> buffers, int componentBytes) throws IOException {
        int viewId = getInt(accessor, "bufferView", -1);
        if (viewId < 0 || viewId >= views.size()) throw new IOException("Invalid accessor.bufferView");

        Map<String, Object> view = views.get(viewId);
        int bufId = getInt(view, "buffer", 0);
        ByteBuffer source = buffers.get(bufId).duplicate().order(ByteOrder.LITTLE_ENDIAN);

        int accessorOffset = getInt(accessor, "byteOffset", 0);
        int viewOffset = getInt(view, "byteOffset", 0);
        int count = getInt(accessor, "count", 0);
        String type = getString(accessor, "type", "SCALAR");
        int elemCount = typeToElements(type);

        int packedStride = componentBytes * elemCount;
        int byteStride = getInt(view, "byteStride", 0);
        if (byteStride != 0 && byteStride != packedStride) {
            throw new IOException("Interleaved bufferViews are not supported in this minimal loader");
        }

        int totalBytes = count * packedStride;
        int offset = viewOffset + accessorOffset;
        source.position(offset);
        source.limit(offset + totalBytes);
        return source.slice().order(ByteOrder.LITTLE_ENDIAN);
    }

    private static int typeToElements(String type) throws IOException {
        return switch (type) {
            case "SCALAR" -> 1;
            case "VEC2" -> 2;
            case "VEC3" -> 3;
            case "VEC4" -> 4;
            default -> throw new IOException("Unsupported accessor type: " + type);
        };
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> getObjectList(Map<String, Object> map, String key) {
        Object v = map.get(key);
        if (!(v instanceof List<?> list)) return Collections.emptyList();
        List<Map<String, Object>> out = new ArrayList<>(list.size());
        for (Object o : list) {
            if (o instanceof Map<?, ?> m) out.add((Map<String, Object>) m);
        }
        return out;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> getObject(Map<String, Object> map, String key) {
        Object v = map.get(key);
        if (v instanceof Map<?, ?> m) return (Map<String, Object>) m;
        return Collections.emptyMap();
    }

    private static int getInt(Map<String, Object> map, String key, int def) {
        Object v = map.get(key);
        if (v instanceof Number n) return n.intValue();
        return def;
    }

    private static String getString(Map<String, Object> map, String key, String def) {
        Object v = map.get(key);
        return v instanceof String s ? s : def;
    }

    private static final class Json {
        private final String s;
        private int i = 0;

        Json(String s) { this.s = s; }

        Object parse() throws IOException {
            skipWs();
            Object v = parseValue();
            skipWs();
            return v;
        }

        private Object parseValue() throws IOException {
            skipWs();
            if (i >= s.length()) throw err("Unexpected EOF");
            char c = s.charAt(i);
            if (c == '{') return parseObject();
            if (c == '[') return parseArray();
            if (c == '"') return parseString();
            if (c == 't' || c == 'f') return parseBoolean();
            if (c == 'n') return parseNull();
            return parseNumber();
        }

        private Map<String, Object> parseObject() throws IOException {
            expect('{');
            Map<String, Object> map = new LinkedHashMap<>();
            skipWs();
            if (peek('}')) { i++; return map; }
            while (true) {
                String k = parseString();
                skipWs();
                expect(':');
                Object v = parseValue();
                map.put(k, v);
                skipWs();
                if (peek('}')) { i++; break; }
                expect(',');
            }
            return map;
        }

        private List<Object> parseArray() throws IOException {
            expect('[');
            List<Object> out = new ArrayList<>();
            skipWs();
            if (peek(']')) { i++; return out; }
            while (true) {
                out.add(parseValue());
                skipWs();
                if (peek(']')) { i++; break; }
                expect(',');
            }
            return out;
        }

        private String parseString() throws IOException {
            expect('"');
            StringBuilder b = new StringBuilder();
            while (i < s.length()) {
                char c = s.charAt(i++);
                if (c == '"') break;
                if (c == '\\') {
                    if (i >= s.length()) throw err("Invalid escape");
                    char e = s.charAt(i++);
                    switch (e) {
                        case '"', '\\', '/' -> b.append(e);
                        case 'b' -> b.append('\b');
                        case 'f' -> b.append('\f');
                        case 'n' -> b.append('\n');
                        case 'r' -> b.append('\r');
                        case 't' -> b.append('\t');
                        case 'u' -> {
                            if (i + 4 > s.length()) throw err("Invalid unicode escape");
                            int cp = Integer.parseInt(s.substring(i, i + 4), 16);
                            i += 4;
                            b.append((char) cp);
                        }
                        default -> throw err("Unknown escape: " + e);
                    }
                } else {
                    b.append(c);
                }
            }
            return b.toString();
        }

        private Boolean parseBoolean() throws IOException {
            if (s.startsWith("true", i)) { i += 4; return Boolean.TRUE; }
            if (s.startsWith("false", i)) { i += 5; return Boolean.FALSE; }
            throw err("Invalid boolean");
        }

        private Object parseNull() throws IOException {
            if (s.startsWith("null", i)) { i += 4; return null; }
            throw err("Invalid null");
        }

        private Number parseNumber() throws IOException {
            int start = i;
            if (peek('-')) i++;
            while (i < s.length() && Character.isDigit(s.charAt(i))) i++;
            if (peek('.')) {
                i++;
                while (i < s.length() && Character.isDigit(s.charAt(i))) i++;
            }
            if (peek('e') || peek('E')) {
                i++;
                if (peek('+') || peek('-')) i++;
                while (i < s.length() && Character.isDigit(s.charAt(i))) i++;
            }
            String n = s.substring(start, i);
            try {
                return n.contains(".") || n.contains("e") || n.contains("E") ? Double.parseDouble(n) : Long.parseLong(n);
            } catch (NumberFormatException ex) {
                throw err("Invalid number: " + n);
            }
        }

        private void skipWs() {
            while (i < s.length()) {
                char c = s.charAt(i);
                if (c == ' ' || c == '\n' || c == '\r' || c == '\t') i++;
                else break;
            }
        }

        private boolean peek(char c) { return i < s.length() && s.charAt(i) == c; }

        private void expect(char c) throws IOException {
            skipWs();
            if (!peek(c)) throw err("Expected '" + c + "'");
            i++;
        }

        private IOException err(String msg) {
            return new IOException("JSON parse error at " + i + ": " + msg);
        }
    }
}
