package SwordsGame.client.graphics;

public class BlockProps {
    private boolean randomRotation = false;
    private boolean randomColor = false;
    private float randomColorShift = 0.25f;
    private boolean emission = false;
    private boolean transparent = false;
    private boolean solid = true;
    private boolean smoothing = false;
    private float hardness = 1.0f;

    public BlockProps() {}

    public BlockProps randomRotation() { this.randomRotation = true; return this; }
    public BlockProps randomColor() { this.randomColor = true; return this; }
    public BlockProps randomColor(float shift) {
        this.randomColor = true;
        this.randomColorShift = Math.max(0.01f, Math.min(1.0f, shift));
        return this;
    }
    public BlockProps emission() { this.emission = true; return this; }
    public BlockProps transparent() { this.transparent = true; return this; }
    public BlockProps nonSolid() { this.solid = false; return this; }
    public BlockProps smoothing() { this.smoothing = true; return this; }
    public BlockProps hardness(float value) { this.hardness = value; return this; }

    public boolean hasRandomRotation() { return randomRotation; }
    public boolean hasRandomColor() { return randomColor; }
    public float getRandomColorShift() { return randomColorShift; }
    public boolean hasEmission() { return emission; }
    public boolean isTransparent() { return transparent; }
    public boolean isSolid() { return solid; }
    public boolean hasSmoothing() { return smoothing; }
    public float getHardness() { return hardness; }
}
