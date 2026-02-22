package SwordsGame.client.blocks;

import SwordsGame.client.graphics.Block;
import SwordsGame.client.graphics.BlockProps;
import SwordsGame.client.assets.Paths;

public class Grass extends Block {
    public Grass() {
        super(Type.GRASS, Paths.BLOCK_GRASS,
                new BlockProps()
                        .randomRotation()
                        .randomColor()
                        .smoothing());
    }
}
