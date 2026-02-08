package SwordsGame.client.blocks;

import SwordsGame.client.Block;
import SwordsGame.client.BlockProperties;
import SwordsGame.client.assets.Paths;

public class Stone extends Block {
    public Stone() {
        super(Type.STONE, Paths.BLOCK_STONE,
                new BlockProperties()
                        .randomColor());
    }
}
