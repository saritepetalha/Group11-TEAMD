package managers;
import static constants.Constants.Player.*;

public class PlayerManager {
    private int gold;
    private int health;
    private int shield;

    public PlayerManager() {
        this.gold = 100;
        this.health = MAX_HEALTH;
        this.shield = MAX_SHIELD;
    }

    public void addGold(int amount) {
        this.gold += amount;
    }

    public boolean spendGold(int amount) {
        if (gold >= amount) {
            gold -= amount;
            return true;
        }
        return false;
    }

    public void takeDamage(int damage) {
        // Shield absorbs damage first
        if (shield > 0) {
            if (shield >= damage) {
                shield -= damage;
                damage = 0;
            } else {
                damage -= shield;
                shield = 0;
            }
        }

        // Remaining damage affects health
        if (damage > 0) {
            health = Math.max(0, health - damage);
        }
    }

    public void heal(int amount) {
        health = Math.min(MAX_HEALTH, health + amount);
    }

    public void rechargeShield(int amount) {
        shield = Math.min(MAX_SHIELD, shield + amount);
    }

    // Getters and setters
    public int getGold() {
        return gold;
    }

    public int getHealth() {
        return health;
    }

    public int getShield() {
        return shield;
    }

    public int getMaxHealth() {
        return MAX_HEALTH;
    }

    public int getMaxShield() {
        return MAX_SHIELD;
    }

    public boolean isAlive() {
        return health > 0;
    }
}
