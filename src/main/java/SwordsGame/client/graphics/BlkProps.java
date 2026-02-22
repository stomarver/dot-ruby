package SwordsGame.client.graphics;

public class BlkProps {
    private boolean randomRotation = false;
    private boolean randomColor = false;
    private boolean emission = false;
    private boolean transparent = false;
    private boolean solid = true;
    private boolean smoothing = false;
    private float hardness = 1.0f;

    public BlkProps() {}

    public BlkProps randomRotation() { this.randomRotation = true; return this; }
    public BlkProps randomColor() { this.randomColor = true; return this; }
    public BlkProps emission() { this.emission = true; return this; }
    public BlkProps transparent() { this.transparent = true; return this; }
    public BlkProps nonSolid() { this.solid = false; return this; }
    public BlkProps smoothing() { this.smoothing = true; return this; }
    public BlkProps hardness(float value) { this.hardness = value; return this; }

    public boolean hasRandomRotation() { return randomRotation; }
    public boolean hasRandomColor() { return randomColor; }
    public boolean hasEmission() { return emission; }
    public boolean isTransparent() { return transparent; }
    public boolean isSolid() { return solid; }
    public boolean hasSmoothing() { return smoothing; }
    public float getHardness() { return hardness; }
}
