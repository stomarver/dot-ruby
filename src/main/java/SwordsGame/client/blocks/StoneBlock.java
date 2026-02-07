package SwordsGame.client.blocks;

import SwordsGame.client.Block;
import SwordsGame.client.BlockProperties;
import SwordsGame.client.assets.TexturePaths;

public class StoneBlock extends Block {
    public StoneBlock() {
        super(BlockType.STONE, TexturePaths.BLOCK_STONE,
                new BlockProperties()
                        .randomColor());
    }
}
