package managers;
import config.GameOptions;
import static constants.Constants.Player.MAX_HEALTH;
import static constants.Constants.Player.MAX_SHIELD;
import helpMethods.OptionsIO;

public class PlayerManager {
    private int gold;
    private int health;
    private int shield;
    private GameOptions gameOptions;
    private int totalGoldEarned;

    // Round checkpoint variables - store values at the beginning of each round/wave
    private int roundStartGold;
    private int roundStartHealth;
    private int roundStartShield;
    private int roundStartTotalGoldEarned;
    private boolean checkpointExists = false;

    public PlayerManager(GameOptions options) {
        this.gameOptions = options;
        try {
            if (gameOptions != null) {
                this.gold = gameOptions.getStartingGold();
                this.totalGoldEarned = this.gold;
                this.health = gameOptions.getStartingPlayerHP();
                this.shield = gameOptions.getStartingShield();
                System.out.println("PlayerManager initialized with options: Gold=" + gold + ", Health=" + health + ", Shield=" + shield);
            } else {
                System.out.println("Warning: Received null GameOptions, using default values for player");
                this.gold = 100; // Default value
                this.health = MAX_HEALTH;
                this.shield = MAX_SHIELD;
            }
        } catch (Exception e) {
            System.out.println("Error initializing player with options: " + e.getMessage());
            e.printStackTrace();
            this.gold = 100; // Default value
            this.health = MAX_HEALTH;
            this.shield = MAX_SHIELD;
        }
    }

    public void addGold(int amount) {
        this.gold += amount;
        this.totalGoldEarned += amount;
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

    public int getStartingHealthAmount() {
        if (gameOptions != null) {
            return gameOptions.getStartingPlayerHP();
        } else {
            // Fallback to default max health if options are null
            return MAX_HEALTH;
        }
    }

    public int getTotalGoldEarned() {
        return totalGoldEarned;
    }

    public void setGold(int gold) {
        this.gold = gold;
    }

    public void setHealth(int health) {
        this.health = Math.min(MAX_HEALTH, Math.max(0, health));
    }

    public void setShield(int shield) {
        this.shield = Math.min(MAX_SHIELD, Math.max(0, shield));
    }

    public void setTotalGoldEarned(int totalGoldEarned) {
        this.totalGoldEarned = totalGoldEarned;
    }

    /**
     * Creates a checkpoint of the current player state.
     * This should be called at the beginning of each wave/round.
     */
    public void createCheckpoint() {
        this.roundStartGold = this.gold;
        this.roundStartHealth = this.health;
        this.roundStartShield = this.shield;
        this.roundStartTotalGoldEarned = this.totalGoldEarned;
        this.checkpointExists = true;
        System.out.println("PlayerManager checkpoint created: Gold=" + roundStartGold +
                ", Health=" + roundStartHealth + ", Shield=" + roundStartShield +
                ", TotalGoldEarned=" + roundStartTotalGoldEarned);
    }

    /**
     * Restores the player state to the last checkpoint.
     * This should be called when loading a save game to ensure
     * round-level variables are properly restored.
     */
    public void restoreFromCheckpoint() {
        if (checkpointExists) {
            this.gold = this.roundStartGold;
            this.health = this.roundStartHealth;
            this.shield = this.roundStartShield;
            this.totalGoldEarned = this.roundStartTotalGoldEarned;
            System.out.println("PlayerManager restored from checkpoint: Gold=" + gold +
                    ", Health=" + health + ", Shield=" + shield +
                    ", TotalGoldEarned=" + totalGoldEarned);
        } else {
            System.out.println("Warning: No checkpoint exists to restore from");
        }
    }

    /**
     * Sets the checkpoint values directly (used when loading from save)
     */
    public void setCheckpoint(int checkpointGold, int checkpointHealth, int checkpointShield, int checkpointTotalGoldEarned) {
        this.roundStartGold = checkpointGold;
        this.roundStartHealth = checkpointHealth;
        this.roundStartShield = checkpointShield;
        this.roundStartTotalGoldEarned = checkpointTotalGoldEarned;
        this.checkpointExists = true;
    }

    /**
     * Gets the checkpoint values for saving
     */
    public int getRoundStartGold() { return roundStartGold; }
    public int getRoundStartHealth() { return roundStartHealth; }
    public int getRoundStartShield() { return roundStartShield; }
    public int getRoundStartTotalGoldEarned() { return roundStartTotalGoldEarned; }
    public boolean hasCheckpoint() { return checkpointExists; }

    /**
     * Reloads player stats from options
     */
    public void reloadFromOptions() {
        try {
            GameOptions freshOptions = OptionsIO.load();

            if (freshOptions != null) {
                this.gameOptions = freshOptions;
                this.gold = gameOptions.getStartingGold();
                this.health = gameOptions.getStartingPlayerHP();
                this.shield = gameOptions.getStartingShield();
                System.out.println("Player stats reloaded: Gold=" + gold + ", Health=" + health + ", Shield=" + shield);
            } else {
                System.out.println("Warning: Failed to load game options for player reload, keeping current values");
            }
        } catch (Exception e) {
            System.out.println("Error reloading player options: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
