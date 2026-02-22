package SwordsGame.client.graphics;

import SwordsGame.client.blocks.Type;
import java.util.Objects;

public class Block {
    protected static final float MIN = 0.01f;
    protected static final float MAX = 0.99f;
    private static final float[][] UV = {
            {MIN, MIN, MAX, MIN, MAX, MAX, MIN, MAX},
            {MIN, MAX, MIN, MIN, MAX, MIN, MAX, MAX},
            {MAX, MAX, MIN, MAX, MIN, MIN, MAX, MIN},
            {MAX, MIN, MAX, MAX, MIN, MAX, MIN, MIN}
    };

    private final TexLoad.Texture[] textures;
    private final BlockProps properties;
    private final Type type;

    public Block(Type type, String texturePath) {
        this(type, new BlockProps(), texturePath);
    }

    public Block(Type type, String texturePath, BlockProps props) {
        this(type, props, texturePath);
    }

    public Block(Type type, String topPath, String bottomPath, String sidePath, BlockProps props) {
        this(type, props, topPath, bottomPath, sidePath);
    }

    public Type getType() { return type; }
    public BlockProps getProperties() { return properties; }

    public int getTextureId(int face) {
        if (textures[0] == null) return 0;
        if (textures.length == 1) return textures[0].id;
        if (face == 2) return textures[0].id;
        if (face == 3) return textures[1].id;
        return textures[2].id;
    }

    public float[] getUv(int rotation) {
        return UV[rotation % 4];
    }

    public void draw(int seed, boolean[] faces) {
        BlockRenderer.renderBlock(this, seed, faces);
    }

    public void destroy() {
        for (TexLoad.Texture texture : textures) {
            if (texture != null) {
                TexLoad.deleteTexture(texture.id);
            }
        }
    }

    public boolean hasTexture() {
        return textures[0] != null;
    }

    private Block(Type type, BlockProps props, String... texturePaths) {
        this.type = Objects.requireNonNull(type, "type");
        this.properties = Objects.requireNonNull(props, "props");
        this.textures = loadTextures(texturePaths);
    }

    private TexLoad.Texture[] loadTextures(String[] texturePaths) {
        int count = texturePaths.length;
        if (count != 1 && count != 3) {
            throw new IllegalArgumentException("Block textures must have 1 or 3 paths");
        }
        TexLoad.Texture[] loaded = new TexLoad.Texture[count];
        for (int i = 0; i < count; i++) {
            loaded[i] = TexLoad.loadTexture(texturePaths[i], false);
        }
        return loaded;
    }
}
