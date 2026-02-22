package SwordsGame.client.assets;

import SwordsGame.client.blocks.Type;
import SwordsGame.client.graphics.Block;
import SwordsGame.client.graphics.BlockProps;

import java.util.function.Consumer;

public final class Syn {
    private Syn() {}

    public static Img img(String path) {
        return new Img(path);
    }

    public static Mdl mdl(String id, String path) {
        return new Mdl(id, path);
    }

    public static BlkDef blk(Type type) {
        return new BlkDef(type);
    }

    public static final class Img {
        public final String path;
        public boolean alphaKey;

        private Img(String path) { this.path = path; }

        public Img alphaKey() { this.alphaKey = true; return this; }
        public Img alphaKey(boolean value) { this.alphaKey = value; return this; }
    }

    public static final class Mdl {
        public final String id;
        public final String path;
        public float scale = 1f;

        private Mdl(String id, String path) {
            this.id = id;
            this.path = path;
        }

        public Mdl scale(float value) { this.scale = value; return this; }
    }

    public static final class BlkDef {
        private final Type type;
        private final BlockProps props = new BlockProps();
        private String one;
        private String top;
        private String bottom;
        private String side;

        private BlkDef(Type type) {
            this.type = type;
        }

        public BlkDef tex(String oneTexture) {
            this.one = oneTexture;
            this.top = null;
            this.bottom = null;
            this.side = null;
            return this;
        }

        public BlkDef tex(String top, String bottom, String side) {
            this.one = null;
            this.top = top;
            this.bottom = bottom;
            this.side = side;
            return this;
        }

        public BlkDef props(Consumer<BlockProps> cfg) {
            if (cfg != null) cfg.accept(props);
            return this;
        }

        public Block build() {
            if (one != null) {
                return new Block(type, one, props);
            }
            return new Block(type, top, bottom, side, props);
        }
    }
}
