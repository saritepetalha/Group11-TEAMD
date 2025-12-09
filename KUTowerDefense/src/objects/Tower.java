package objects;

import constants.Constants;
import enemies.Enemy;
import strategies.TargetingStrategy;
import strategies.FirstEnemyStrategy;
import skills.SkillTree;
import skills.SkillType;
import constants.GameDimensions;

import java.awt.*;

public abstract class Tower {

    private int x, y, ID, countDownClock;
    protected int damage;
    protected float range, cooldown;
    private static int num = 0;
    protected int level = 1;
    protected float attackSpeedMultiplier = 1.0f;

    // Tower condition system
    protected int usageCount = 0;
    protected float condition = 100.0f; // 100% is perfect condition
    protected static final int MAX_USAGE_BEFORE_DEGRADATION = 50; // Number of attacks before condition starts degrading
    protected static final float MIN_CONDITION = 20.0f; // Minimum condition before repair is required
    protected static final float CONDITION_DAMAGE_MULTIPLIER = 0.5f; // How much condition affects damage
    protected static final float CONDITION_RANGE_MULTIPLIER = 0.3f; // How much condition affects range
    protected static final float CONDITION_SPEED_MULTIPLIER = 0.4f; // How much condition affects attack speed

    // Tower type specific degradation rates
    protected static final float ARCHER_DEGRADATION_RATE = 0.3f; // Archer towers degrade slower
    protected static final float ARTILLERY_DEGRADATION_RATE = 0.8f; // Artillery towers degrade faster
    protected static final float MAGE_DEGRADATION_RATE = 0.5f; // Mage towers degrade at medium rate

    // Warrior spawning limitations
    protected int maxWarriors = 2; // Maximum warriors per tower
    protected int currentWarriors = 0; // Current number of active warriors from this tower
    protected static final int MAX_SPAWN_DISTANCE_TILES = 3; // Maximum distance in tiles

    // Destroyed state
    protected boolean destroyed = false;
    protected java.awt.image.BufferedImage destroyedSprite = null;

    // Strategy Pattern: Tower targeting behavior
    protected TargetingStrategy targetingStrategy;

    // Debris effect for earthquake destruction
    public static class Debris {
        public float x, y, vx, vy;
        public float alpha;
        public int color;
        public int size;
        public int lifetime;
        public int age;
        public Debris(float x, float y, float vx, float vy, int color, int size, int lifetime) {
            this.x = x; this.y = y; this.vx = vx; this.vy = vy;
            this.color = color; this.size = size; this.lifetime = lifetime; this.age = 0; this.alpha = 1f;
        }
    }
    public java.util.List<Debris> debrisList = null;
    public long debrisStartTime = 0;
    public static final int DEBRIS_DURATION_MS = 500;

    private boolean boostActive = false;
    private long boostEndTime = 0;

    public abstract int getType();

    public Tower(int x, int y) {
        this.x = x;
        this.y = y;
        this.ID = num;
        num++;

        // Default targeting strategy is FirstEnemy (current behavior)
        this.targetingStrategy = new FirstEnemyStrategy();
    }

    // Constructor with custom targeting strategy
    public Tower(int x, int y, TargetingStrategy targetingStrategy) {
        this.x = x;
        this.y = y;
        this.ID = num;
        num++;
        this.targetingStrategy = targetingStrategy != null ? targetingStrategy : new FirstEnemyStrategy();
    }

    public abstract float getCooldown();
    public abstract int getDamage();

    protected void setDefaultCooldown() {
        cooldown = Constants.Towers.getCooldown(getType());
    }

    protected void setDefaultRange() {
        range = Constants.Towers.getRange(getType());
    }

    protected void setDefaultDamage() {
        damage = Constants.Towers.getStartDamage(getType());
    }

    public boolean isClicked(int mouseX, int mouseY) {
        Rectangle bounds = new Rectangle(x, y, 64, 64);
        return bounds.contains(mouseX, mouseY);
    }

    // Targeting Strategy methods
    public TargetingStrategy getTargetingStrategy() {
        return targetingStrategy;
    }

    public void setTargetingStrategy(TargetingStrategy targetingStrategy) {
        this.targetingStrategy = targetingStrategy != null ? targetingStrategy : new FirstEnemyStrategy();
    }

    public int getX() {
        return x;
    }
    public int getY() {
        return y;
    }
    public int getID() {
        return ID;
    }

    public boolean isCooldownOver() {
        return countDownClock >= cooldown;
    }

    public void resetCooldown() {
        countDownClock = 0;
    }

    public void update() {
        countDownClock += 1.0f * attackSpeedMultiplier;
        updateCondition();
    }

    public void update(float gameSpeedMultiplier) {
        countDownClock += gameSpeedMultiplier * attackSpeedMultiplier;
        updateCondition();
    }

    private void updateCondition() {
        if (usageCount > MAX_USAGE_BEFORE_DEGRADATION) {
            float degradationRate = getDegradationRate();
            condition = Math.max(MIN_CONDITION, condition - degradationRate);
        }
    }

    protected float getDegradationRate() {
        switch (getType()) {
            case Constants.Towers.ARCHER:
                return ARCHER_DEGRADATION_RATE;
            case Constants.Towers.ARTILLERY:
                return ARTILLERY_DEGRADATION_RATE;
            case Constants.Towers.MAGE:
                return MAGE_DEGRADATION_RATE;
            case Constants.Towers.POISON:
                return 0.6f; // Poison towers degrade moderately due to toxic materials
            default:
                return 0.5f; // Default degradation rate
        }
    }

    public void incrementUsage() {
        usageCount++;
        // Artillery towers degrade faster with each shot
        if (getType() == Constants.Towers.ARTILLERY) {
            condition = Math.max(MIN_CONDITION, condition - 0.1f);
        }
    }


    public float getConditionBasedRange() {
        float baseRange = getRange();
        float conditionMultiplier = 1.0f - (CONDITION_RANGE_MULTIPLIER * (1.0f - condition / 100.0f));
        return baseRange * conditionMultiplier;
    }

    public int getConditionBasedDamage() {
        int baseDamage = getDamage();
        float conditionMultiplier = 1.0f - (CONDITION_DAMAGE_MULTIPLIER * (1.0f - condition / 100.0f));
        return (int)(baseDamage * conditionMultiplier);
    }

    public int getLevel() { return level; }
    public boolean isUpgradeable() { return level == 1; }
    public abstract Tower upgrade();
    public void setLevel(int lvl) { this.level = lvl; }


    // Default implementation for on-hit effects. Can be overridden by specific towers or decorators.
    public void applyOnHitEffect(Enemy enemy, scenes.Playing playingScene) {
        // Base towers typically don't have special on-hit effects beyond damage.
        // This can be left empty or log a message if needed.
    }

    public int getWidth() {
        return 64;
    }

    public int getHeight() {
        return 64;
    }

    public boolean isDestroyed() { return destroyed; }
    public void setDestroyed(boolean destroyed) { this.destroyed = destroyed; }
    public void setDestroyedSprite(java.awt.image.BufferedImage sprite) { this.destroyedSprite = sprite; }
    public java.awt.image.BufferedImage getDestroyedSprite() { return destroyedSprite; }

    public void revive() {
        this.destroyed = false;
        // Reset tower to level 1 upon revival (earthquake only destroys level 1 towers)
        this.level = 1;
        // Reset any other necessary tower stats upon revival
        this.countDownClock = 0; // Reset cooldown as well
    }

    // Warrior management methods
    public boolean canSpawnWarrior() {
        return currentWarriors < maxWarriors;
    }

    public void addWarrior() {
        if (currentWarriors < maxWarriors) {
            currentWarriors++;
        }
    }

    public void removeWarrior() {
        if (currentWarriors > 0) {
            currentWarriors--;
        }
    }

    public int getCurrentWarriors() {
        return currentWarriors;
    }

    public int getMaxWarriors() {
        return maxWarriors;
    }

    public boolean isWithinSpawnDistance(int targetX, int targetY) {
        // Convert positions to tile coordinates
        int towerTileX = x / 64; // Assuming 64x64 tile size
        int towerTileY = y / 64;
        int targetTileX = targetX / 64;
        int targetTileY = targetY / 64;

        // Calculate distance in tiles
        int deltaX = Math.abs(targetTileX - towerTileX);
        int deltaY = Math.abs(targetTileY - towerTileY);
        int maxDistance = Math.max(deltaX, deltaY); // Chebyshev distance (allows diagonal)

        return maxDistance <= MAX_SPAWN_DISTANCE_TILES;
    }

    public float getRange() {
        float baseRange = range;
        if (SkillTree.getInstance().isSkillSelected(SkillType.EAGLE_EYE)) {
            float bonus = GameDimensions.TILE_DISPLAY_SIZE;
            System.out.println("[EAGLE_EYE] Tower applies bonus range: " + baseRange + " -> " + (baseRange + bonus));
            baseRange += bonus;
        }
        return baseRange;
    }
}
