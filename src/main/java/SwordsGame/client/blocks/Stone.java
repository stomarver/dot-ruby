package SwordsGame.client.blocks;

import SwordsGame.client.graphics.Block;
import SwordsGame.client.graphics.BlockProps;
import SwordsGame.client.assets.Paths;

public class Stone extends Block {
    public Stone() {
        super(Type.STONE, Paths.BLOCK_STONE,
                new BlockProps()
                        .randomColor());
    }
}
