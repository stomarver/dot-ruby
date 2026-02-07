package SwordsGame.graphics;

public class FallingBlock {
    public float x, y, z;
    public float vy; // Только вертикальная скорость
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

        // Начальная скорость = 0, блок просто начинает падать
        this.vy = 0;
    }

    public void update(float deltaTime) {
        // Гравитация
        vy -= 0.98f * deltaTime;

        // Обновляем только позицию по Y
        y += vy * deltaTime * 60;
    }
}