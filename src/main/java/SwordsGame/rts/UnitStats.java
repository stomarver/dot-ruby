package SwordsGame.rts;

public class UnitStats {
    private final int health;
    private final int damage;
    private final int movementSpeed;
    private final int attackSpeed;
    private final int buildSpeed;
    private final ResourceBundle gatherRates;

    public UnitStats(int health,
                     int damage,
                     int movementSpeed,
                     int attackSpeed,
                     int buildSpeed,
                     ResourceBundle gatherRates) {
        this.health = health;
        this.damage = damage;
        this.movementSpeed = movementSpeed;
        this.attackSpeed = attackSpeed;
        this.buildSpeed = buildSpeed;
        this.gatherRates = gatherRates == null ? new ResourceBundle() : gatherRates.copy();
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

    public ResourceBundle getGatherRates() {
        return gatherRates.copy();
    }
}
