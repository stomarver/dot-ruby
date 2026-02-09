package SwordsGame.client.graphics;

public class BlockProperties {
    private boolean randomRotation = false;
    private boolean randomColor = false;
    private boolean emission = false;
    private boolean transparent = false;
    private boolean solid = true;
    private boolean smoothing = false;
    private float hardness = 1.0f;

    public BlockProperties() {}

    public BlockProperties randomRotation() { this.randomRotation = true; return this; }
    public BlockProperties randomColor() { this.randomColor = true; return this; }
    public BlockProperties emission() { this.emission = true; return this; }
    public BlockProperties transparent() { this.transparent = true; return this; }
    public BlockProperties nonSolid() { this.solid = false; return this; }
    public BlockProperties smoothing() { this.smoothing = true; return this; }
    public BlockProperties hardness(float value) { this.hardness = value; return this; }

    public boolean hasRandomRotation() { return randomRotation; }
    public boolean hasRandomColor() { return randomColor; }
    public boolean hasEmission() { return emission; }
    public boolean isTransparent() { return transparent; }
    public boolean isSolid() { return solid; }
    public boolean hasSmoothing() { return smoothing; }
    public float getHardness() { return hardness; }
}
