package SwordsGame.client.graphics;

import SwordsGame.client.assets.Syn;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public final class MdlReg {
    private static final Map<String, Ent> MDL = new HashMap<>();

    private MdlReg() {}

    public static void reg(Syn.Mdl def) {
        try {
            GltfModel mdl = GltfModel.load(Path.of(def.path));
            MDL.put(def.id, new Ent(mdl, def.scale));
        } catch (Exception e) {
            throw new RuntimeException("Model load failed: " + def.path, e);
        }
    }

    public static Ent get(String id) {
        return MDL.get(id);
    }

    public static final class Ent {
        public final GltfModel mdl;
        public final float scale;

        public Ent(GltfModel mdl, float scale) {
            this.mdl = mdl;
            this.scale = scale;
        }
    }
}
