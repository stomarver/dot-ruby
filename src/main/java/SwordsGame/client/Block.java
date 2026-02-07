package SwordsGame.client;

import SwordsGame.client.blocks.BlockType;

import static org.lwjgl.opengl.GL11.*;

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
    private final BlockType type;

    public Block(BlockType type, String texturePath) {
        this(type, texturePath, new BlockProperties());
    }

    public Block(BlockType type, String texturePath, BlockProperties props) {
        this.type = type;
        this.properties = props;
        this.textures = new TextureLoader.Texture[1];
        this.textures[0] = TextureLoader.loadTexture(texturePath, false);
    }

    public Block(BlockType type, String topPath, String bottomPath, String sidePath, BlockProperties props) {
        this.type = type;
        this.properties = props;
        this.textures = new TextureLoader.Texture[3];
        this.textures[0] = TextureLoader.loadTexture(topPath, false);
        this.textures[1] = TextureLoader.loadTexture(bottomPath, false);
        this.textures[2] = TextureLoader.loadTexture(sidePath, false);
    }

    public BlockType getType() { return type; }
    public BlockProperties getProperties() { return properties; }

    public void draw(int seed, boolean[] faces) {
        if (textures[0] == null) return;

        if (properties.hasEmission()) glDisable(GL_LIGHTING);

        if (properties.hasRandomColor()) {
            float colorMod = 0.9f + (Math.abs(seed % 10) / 100f);
            glColor3f(colorMod, colorMod, colorMod);
        } else {
            glColor3f(1.0f, 1.0f, 1.0f);
        }

        int rot = properties.hasRandomRotation() ? (Math.abs(seed) % 4) : 0;
        if (textures.length == 1) {
            glBindTexture(GL_TEXTURE_2D, textures[0].id);
            glBegin(GL_QUADS);
            if (faces[0]) { glNormal3f(0,0,1);  drawFace(-1,-1, 1,  1,-1, 1,  1, 1, 1, -1, 1, 1, rot); }
            if (faces[1]) { glNormal3f(0,0,-1); drawFace(-1,-1,-1, -1, 1,-1,  1, 1,-1,  1,-1,-1, rot); }
            if (faces[2]) { glNormal3f(0,1,0);  drawFace(-1, 1,-1, -1, 1, 1,  1, 1, 1,  1, 1,-1, rot); }
            if (faces[3]) { glNormal3f(0,-1,0); drawFace(-1,-1,-1,  1,-1,-1,  1,-1, 1, -1,-1, 1, rot); }
            if (faces[4]) { glNormal3f(1,0,0);  drawFace( 1,-1,-1,  1, 1,-1,  1, 1, 1,  1,-1, 1, rot); }
            if (faces[5]) { glNormal3f(-1,0,0); drawFace(-1,-1,-1, -1,-1, 1, -1, 1, 1, -1, 1,-1, rot); }
            glEnd();
        } else {
            if (faces[2]) {
                glBindTexture(GL_TEXTURE_2D, textures[0].id);
                glBegin(GL_QUADS);
                glNormal3f(0,1,0);
                drawFace(-1, 1,-1, -1, 1, 1,  1, 1, 1,  1, 1,-1, rot);
                glEnd();
            }
            if (faces[3]) {
                glBindTexture(GL_TEXTURE_2D, textures[1].id);
                glBegin(GL_QUADS);
                glNormal3f(0,-1,0);
                drawFace(-1,-1,-1,  1,-1,-1,  1,-1, 1, -1,-1, 1, rot);
                glEnd();
            }
            if (faces[0] || faces[1] || faces[4] || faces[5]) {
                glBindTexture(GL_TEXTURE_2D, textures[2].id);
                glBegin(GL_QUADS);
                if (faces[0]) { glNormal3f(0,0,1);  drawFace(-1,-1, 1,  1,-1, 1,  1, 1, 1, -1, 1, 1, rot); }
                if (faces[1]) { glNormal3f(0,0,-1); drawFace(-1,-1,-1, -1, 1,-1,  1, 1,-1,  1,-1,-1, rot); }
                if (faces[4]) { glNormal3f(1,0,0);  drawFace( 1,-1,-1,  1, 1,-1,  1, 1, 1,  1,-1, 1, rot); }
                if (faces[5]) { glNormal3f(-1,0,0); drawFace(-1,-1,-1, -1,-1, 1, -1, 1, 1, -1, 1,-1, rot); }
                glEnd();
            }
        }

        if (properties.hasEmission()) glEnable(GL_LIGHTING);
    }

    private void drawFace(float x1, float y1, float z1, float x2, float y2, float z2,
                          float x3, float y3, float z3, float x4, float y4, float z4, int rot) {
        glTexCoord2f(UV[rot][0], UV[rot][1]); glVertex3f(x1, y1, z1);
        glTexCoord2f(UV[rot][2], UV[rot][3]); glVertex3f(x2, y2, z2);
        glTexCoord2f(UV[rot][4], UV[rot][5]); glVertex3f(x3, y3, z3);
        glTexCoord2f(UV[rot][6], UV[rot][7]); glVertex3f(x4, y4, z4);
    }
}
