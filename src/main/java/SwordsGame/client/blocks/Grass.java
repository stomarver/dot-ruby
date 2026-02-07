package SwordsGame.client.blocks;

import SwordsGame.client.Block;
import SwordsGame.client.BlockProperties;
import SwordsGame.client.assets.TexturePaths;

public class Grass extends Block {
    public Grass() {
        super(Type.GRASS, TexturePaths.BLOCK_GRASS,
                new BlockProperties()
                        .randomRotation()
                        .randomColor());
    }
}
