package SwordsGame.client.graphics;

import SwordsGame.client.assets.Syn;

import java.util.HashMap;
import java.util.Map;

public final class ImgReg {
    private static final Map<String, TexLoad.Texture> IMG = new HashMap<>();

    private ImgReg() {}

    public static TexLoad.Texture reg(Syn.Img def) {
        TexLoad.Texture tex = TexLoad.loadTexture(def.path, def.alphaKey);
        IMG.put(def.path, tex);
        return tex;
    }

    public static TexLoad.Texture get(String path) {
        return IMG.get(path);
    }

    public static void clear() {
        IMG.clear();
    }
}
