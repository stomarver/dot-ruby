package SwordsGame.client.blocks;

import SwordsGame.client.Block;
import SwordsGame.client.BlockProperties;
import SwordsGame.client.assets.TexturePaths;

public class GrassBlock extends Block {
    public GrassBlock() {
        super(BlockType.GRASS, TexturePaths.BLOCK_GRASS,
                new BlockProperties()
                        .randomRotation()
                        .randomColor());
    }
}
