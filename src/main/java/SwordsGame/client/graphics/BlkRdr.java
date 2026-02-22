package SwordsGame.client.graphics;

import SwordsGame.client.Wld;
import SwordsGame.client.blocks.Reg;

import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;

public class BlkRdr {
    private static final Map<Blk, ChkMsh[]> cache = new HashMap<>();
    private static final boolean[] ALL_FACES = {true, true, true, true, true, true};

    public static void renderBlock(Blk block, int seed, boolean[] faces) {
        renderBlock(block, seed, faces, 1.0f);
    }

    public static void renderBlock(Blk block, int seed, boolean[] faces, float alpha) {
        if (block == null || !block.hasTexture()) return;

        BlkProps props = block.getProperties();
        int rot = props.hasRandomRotation() ? (Math.abs(seed) % 4) : 0;

        boolean fullFaces = isAllFaces(faces);
        ChkMsh mesh = fullFaces ? getMesh(block, rot) : buildDynamicMesh(block, rot, faces);
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
        Blk block = Reg.get(id);
        if (block != null) {
            renderBlock(block, seed, faces, 1.0f);
        }
    }

    public static void renderBlock(byte id, int seed, boolean[] faces, float alpha) {
        Blk block = Reg.get(id);
        if (block != null) {
            renderBlock(block, seed, faces, alpha);
        }
    }

    public static void clear() {
        for (ChkMsh[] meshes : cache.values()) {
            for (ChkMsh mesh : meshes) {
                if (mesh != null) mesh.destroy();
            }
        }
        cache.clear();
    }

    private static ChkMsh getMesh(Blk block, int rot) {
        ChkMsh[] meshes = cache.computeIfAbsent(block, key -> new ChkMsh[4]);
        if (meshes[rot] == null) {
            meshes[rot] = buildMesh(block, rot);
        }
        return meshes[rot];
    }

    private static ChkMsh buildMesh(Blk block, int rot) {
        MshBld builder = new MshBld(false, false);
        builder.addBlock(block.getType().id, rot, ALL_FACES, 0, 0, 0, 0, Wld.BLOCK_SCALE);
        return builder.build();
    }

    private static ChkMsh buildDynamicMesh(Blk block, int rot, boolean[] faces) {
        MshBld builder = new MshBld(false, false);
        builder.addBlock(block.getType().id, rot, faces, 0, 0, 0, 0, Wld.BLOCK_SCALE);
        return builder.build();
    }

    private static boolean isAllFaces(boolean[] faces) {
        for (boolean face : faces) {
            if (!face) return false;
        }
        return true;
    }
}
