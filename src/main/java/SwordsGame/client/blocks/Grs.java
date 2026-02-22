package SwordsGame.client.blocks;

import SwordsGame.client.graphics.Blk;
import SwordsGame.client.graphics.BlkProps;
import SwordsGame.client.assets.Paths;

public class Grs extends Blk {
    public Grs() {
        super(Type.GRASS, Paths.BLOCK_GRASS,
                new BlkProps()
                        .randomRotation()
                        .randomColor()
                        .smoothing());
    }
}
