package SwordsGame.client.graphics;

import SwordsGame.client.assets.Syn;

import java.util.HashMap;
import java.util.Map;

public final class ImgReg {
    private static final Map<String, TexLd.Texture> IMG = new HashMap<>();

    private ImgReg() {}

    public static TexLd.Texture reg(Syn.Img def) {
        TexLd.Texture tex = TexLd.loadTexture(def.path, def.alphaKey);
        IMG.put(def.path, tex);
        return tex;
    }

    public static TexLd.Texture get(String path) {
        return IMG.get(path);
    }

    public static void clear() {
        IMG.clear();
    }
}
