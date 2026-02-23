package SwordsGame.client;

public class FallingBlock {
    public float x, y, z;
    public float vy;
    public byte type;
    public double creationTime;
    public int seed;

    public FallingBlock(float x, float y, float z, byte type, int seed, double creationTime) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.type = type;
        this.seed = seed;
        this.creationTime = creationTime;

        this.vy = 0;
    }

    public void update(float deltaTime) {
        vy -= 0.98f * deltaTime;

        y += vy * deltaTime * 60;
    }
}
