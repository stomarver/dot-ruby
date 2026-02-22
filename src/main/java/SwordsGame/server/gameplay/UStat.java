package SwordsGame.server.gameplay;

public class UStat {
    private final int health;
    private final int damage;
    private final int movementSpeed;
    private final int attackSpeed;
    private final int buildSpeed;
    private final ResPack gatherRates;

    public UStat(int health,
                     int damage,
                     int movementSpeed,
                     int attackSpeed,
                     int buildSpeed,
                     ResPack gatherRates) {
        this.health = health;
        this.damage = damage;
        this.movementSpeed = movementSpeed;
        this.attackSpeed = attackSpeed;
        this.buildSpeed = buildSpeed;
        this.gatherRates = gatherRates == null ? new ResPack() : gatherRates.copy();
    }

    public int getHealth() {
        return health;
    }

    public int getDamage() {
        return damage;
    }

    public int getMovementSpeed() {
        return movementSpeed;
    }

    public int getAttackSpeed() {
        return attackSpeed;
    }

    public int getBuildSpeed() {
        return buildSpeed;
    }

    public ResPack getGatherRates() {
        return gatherRates.copy();
    }
}
