package SwordsGame.client.blocks;

import SwordsGame.client.graphics.Blk;
import SwordsGame.client.graphics.BlkProps;
import SwordsGame.client.assets.Paths;

public class Stn extends Blk {
    public Stn() {
        super(Type.STONE, Paths.BLOCK_STONE,
                new BlkProps()
                        .randomColor());
    }
}
