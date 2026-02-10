blocks {
    air {
        props {
            nonSolid
            aoAffected false
        }
    }

    grass {
        texture "grass.png"
        props {
            randomRotation
            randomColor
            hardness 0.6f
            aoAffected true
        }
    }

    cobble {
        texture "cobble.png"
        props {
            hardness 2.0f
            aoAffected true
        }
    }

    stone {
        texture "stone.png"
        props {
            hardness 3.0f
            aoAffected true
        }
    }

    glass {
        texture "glass.png"
        props {
            transparent
            hardness 0.3f
            aoAffected false
        }
    }
}
