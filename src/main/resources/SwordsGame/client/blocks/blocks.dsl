blocks {
    air {
        props {
            nonSolid
        }
    }

    grass {
        texture Paths.BLOCK_GRASS
        props {
            randomRotation
            randomColor
            smoothing
            hardness 0.6f
        }
    }

    cobble {
        texture Paths.BLOCK_COBBLE
        props {
            hardness 2.0f
        }
    }

    stone {
        texture Paths.BLOCK_STONE
        props {
            smoothing
            hardness 3.0f
        }
    }
}
