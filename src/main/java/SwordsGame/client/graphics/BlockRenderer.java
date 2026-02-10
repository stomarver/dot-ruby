package SwordsGame.client.graphics;

import SwordsGame.client.World;
import SwordsGame.client.blocks.RenderRegistry;

import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;

public class BlockRenderer {
    private static final Map<Block, ChunkMesh[]> cache = new HashMap<>();
    private static final boolean[] ALL_FACES = {true, true, true, true, true, true};

    public static void renderBlock(Block block, int seed, boolean[] faces) {
        renderBlock(block, seed, faces, 1.0f);
    }

    public static void renderBlock(Block block, int seed, boolean[] faces, float alpha) {
        if (block == null || !block.hasTexture()) return;

        BlockProperties props = block.getProperties();
        int rot = props.hasRandomRotation() ? (Math.abs(seed) % 4) : 0;

        boolean fullFaces = isAllFaces(faces);
        ChunkMesh mesh = fullFaces ? getMesh(block, rot) : buildDynamicMesh(block, rot, faces);
        if (mesh == null) return;

        if (props.hasEmission()) glDisable(GL_LIGHTING);

        if (props.hasRandomColor()) {
            float colorMod = 0.9f + (Math.abs(seed % 10) / 100f);
            glColor4f(colorMod, colorMod, colorMod, alpha);
        } else {
            glColor4f(1.0f, 1.0f, 1.0f, alpha);
        }

        mesh.render(false);

        if (!fullFaces) {
            mesh.destroy();
        }

        if (props.hasEmission()) glEnable(GL_LIGHTING);
    }

    public static void renderBlock(byte id, int seed, boolean[] faces) {
        Block block = RenderRegistry.get(id);
        if (block != null) {
            renderBlock(block, seed, faces, 1.0f);
        }
    }

    public static void renderBlock(byte id, int seed, boolean[] faces, float alpha) {
        Block block = RenderRegistry.get(id);
        if (block != null) {
            renderBlock(block, seed, faces, alpha);
        }
    }

    public static void clear() {
        for (ChunkMesh[] meshes : cache.values()) {
            for (ChunkMesh mesh : meshes) {
                if (mesh != null) mesh.destroy();
            }
        }
        cache.clear();
    }

    private static ChunkMesh getMesh(Block block, int rot) {
        ChunkMesh[] meshes = cache.computeIfAbsent(block, key -> new ChunkMesh[4]);
        if (meshes[rot] == null) {
            meshes[rot] = buildMesh(block, rot);
        }
        return meshes[rot];
    }

    private static ChunkMesh buildMesh(Block block, int rot) {
        MeshBuilder builder = new MeshBuilder(false, false);
        builder.addBlock(block.getType().id, rot, ALL_FACES, 0, 0, 0, 0, World.BLOCK_SCALE);
        return builder.build();
    }

    private static ChunkMesh buildDynamicMesh(Block block, int rot, boolean[] faces) {
        MeshBuilder builder = new MeshBuilder(false, false);
        builder.addBlock(block.getType().id, rot, faces, 0, 0, 0, 0, World.BLOCK_SCALE);
        return builder.build();
    }

    private static boolean isAllFaces(boolean[] faces) {
        for (boolean face : faces) {
            if (!face) return false;
        }
        return true;
    }
}
