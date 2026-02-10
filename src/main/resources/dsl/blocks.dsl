blocks {
    air {
        type Type.AIR
        props {
            nonSolid
        }
    }

    grass {
        type    Type.GRASS
        texture Paths.BLOCK_GRASS
        props {
            randomRotation
            randomColor
            smoothing
            hardness 0.6f
        }
    }

    cobble {
        type    Type.COBBLE
        texture Paths.BLOCK_COBBLE
        props {
            hardness 2.0f
        }
    }

    stone {
        type    Type.STONE
        texture Paths.BLOCK_STONE
        props {
            smoothing
            hardness 3.0f
        }
    }
}
