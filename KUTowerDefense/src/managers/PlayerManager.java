package managers;
import config.GameOptions;
import static constants.Constants.Player.MAX_HEALTH;
import static constants.Constants.Player.MAX_SHIELD;
import helpMethods.OptionsIO;
import stats.GameAction;

public class PlayerManager {
    private int gold;
    private int health;
    private int shield;
    private GameOptions gameOptions;
    private int totalGoldEarned;
    private scenes.Playing playing;

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
        gold += amount;
        totalGoldEarned += amount;
        String details = String.format("Gold earned: %d", amount);
        ReplayManager.getInstance().addAction(new GameAction(
            GameAction.ActionType.GOLD_EARNED,
            details,
            (int)playing.getTimePlayedInSeconds()
        ));
    }

    public void spendGold(int amount) {
        gold -= amount;
        String details = String.format("Gold spent: %d", amount);
        ReplayManager.getInstance().addAction(new GameAction(
            GameAction.ActionType.GOLD_SPENT,
            details,
            (int)playing.getTimePlayedInSeconds()
        ));
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
        int maxHealth = getMaxHealthFromOptions();
        health = Math.min(maxHealth, health + amount);
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
        return getMaxHealthFromOptions();
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
        int maxHealth = getMaxHealthFromOptions();
        this.health = Math.min(maxHealth, Math.max(0, health));
    }

    public void setShield(int shield) {
        this.shield = Math.min(MAX_SHIELD, Math.max(0, shield));
    }

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

    /**
     * Update GameOptions reference (for when difficulty changes)
     * Updates the internal GameOptions reference without resetting player stats
     */
    public void updateGameOptions(GameOptions newOptions) {
        if (newOptions != null) {
            this.gameOptions = newOptions;
            System.out.println("PlayerManager: Updated GameOptions reference");
        } else {
            System.out.println("Warning: Received null GameOptions in updateGameOptions, keeping current options");
        }
    }

    /**
     * Get max health from GameOptions, fallback to constant if options unavailable
     */
    private int getMaxHealthFromOptions() {
        if (gameOptions != null) {
            return gameOptions.getStartingPlayerHP();
        }
        return MAX_HEALTH; // Fallback to constant
    }

    public void setPlaying(scenes.Playing playing) {
        this.playing = playing;
    }

    public scenes.Playing getPlaying() {
        return playing;
    }

    public void reset() {
        gold = 100;
        health = 100;
        shield = 0;
        totalGoldEarned = 0;
    }
}
