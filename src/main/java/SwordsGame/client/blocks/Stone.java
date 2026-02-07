package SwordsGame.client.blocks;

import SwordsGame.client.Block;
import SwordsGame.client.BlockProperties;
import SwordsGame.client.assets.TexturePaths;

public class Stone extends Block {
    public Stone() {
        super(Type.STONE, TexturePaths.BLOCK_STONE,
                new BlockProperties()
                        .randomColor());
    }
}
