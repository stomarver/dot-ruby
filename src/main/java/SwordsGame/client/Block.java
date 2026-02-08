package SwordsGame.client;

import SwordsGame.client.blocks.Type;

public class Block {
    protected static final float MIN = 0.01f;
    protected static final float MAX = 0.99f;
    private static final float[][] UV = {
            {MIN, MIN, MAX, MIN, MAX, MAX, MIN, MAX},
            {MIN, MAX, MIN, MIN, MAX, MIN, MAX, MAX},
            {MAX, MAX, MIN, MAX, MIN, MIN, MAX, MIN},
            {MAX, MIN, MAX, MAX, MIN, MAX, MIN, MIN}
    };

    private final TextureLoader.Texture[] textures;
    private final BlockProperties properties;
    private final Type type;

    public Block(Type type, String texturePath) {
        this(type, texturePath, new BlockProperties());
    }

    public Block(Type type, String texturePath, BlockProperties props) {
        this.type = type;
        this.properties = props;
        this.textures = new TextureLoader.Texture[1];
        this.textures[0] = TextureLoader.loadTexture(texturePath, false);
    }

    public Block(Type type, String topPath, String bottomPath, String sidePath, BlockProperties props) {
        this.type = type;
        this.properties = props;
        this.textures = new TextureLoader.Texture[3];
        this.textures[0] = TextureLoader.loadTexture(topPath, false);
        this.textures[1] = TextureLoader.loadTexture(bottomPath, false);
        this.textures[2] = TextureLoader.loadTexture(sidePath, false);
    }

    public Type getType() { return type; }
    public BlockProperties getProperties() { return properties; }

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
        for (TextureLoader.Texture texture : textures) {
            if (texture != null) {
                TextureLoader.deleteTexture(texture.id);
            }
        }
    }

    public boolean hasTexture() {
        return textures[0] != null;
    }
}
