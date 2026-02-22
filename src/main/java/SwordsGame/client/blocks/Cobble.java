package SwordsGame.client.blocks;

import SwordsGame.client.graphics.Block;
import SwordsGame.client.graphics.BlockProps;
import SwordsGame.client.assets.Paths;

public class Cobble extends Block {
    public Cobble() {
        super(Type.COBBLE, Paths.BLOCK_COBBLE,
                new BlockProps()
                        .randomColor(0.4f));
    }
}
