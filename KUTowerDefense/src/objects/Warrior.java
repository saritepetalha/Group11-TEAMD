package objects;

import enemies.Enemy;
import strategies.TargetingStrategy;
import strategies.FirstEnemyStrategy;
import helpMethods.LoadSave;

import java.awt.*;
import java.awt.image.BufferedImage;

public abstract class Warrior {

    private int x, y, ID, countDownClock;
    protected int damage;
    protected float range, cooldown;
    private static int num = 0;
    protected int level = 1;
    protected float attackSpeedMultiplier = 1.0f;

    // Strategy Pattern: Warrior targeting behavior
    protected TargetingStrategy targetingStrategy;

    // Animation fields
    protected int animationIndex = 0;
    protected int animationTick = 0;
    protected int animationSpeed = 15; // Adjust as needed
    protected int maxFrameCount;

    public Warrior(int x, int y) {
        this.x = x;
        this.y = y;
        this.ID = num;
        num++;

        // Default targeting strategy is FirstEnemy (current behavior)
        this.targetingStrategy = new FirstEnemyStrategy();
        initializeAnimationParameters();
    }

    // Constructor with custom targeting strategy
    public Warrior(int x, int y, TargetingStrategy targetingStrategy) {
        this.x = x;
        this.y = y;
        this.ID = num;
        num++;
        this.targetingStrategy = targetingStrategy != null ? targetingStrategy : new FirstEnemyStrategy();
        initializeAnimationParameters();
    }

    protected abstract void initializeAnimationParameters();

    public abstract int getType();
    public abstract float getCooldown();
    public abstract float getRange();
    public abstract int getDamage();
    public abstract int getCost();

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
    }

    public void update(float gameSpeedMultiplier) {
        countDownClock += gameSpeedMultiplier * attackSpeedMultiplier;
    }

    public int getLevel() { return level; }
    public boolean isUpgradeable() { return level == 1; }
    public void setLevel(int lvl) { this.level = lvl; }

    public void setAttackSpeedMultiplier(float multiplier) {
        this.attackSpeedMultiplier = multiplier;
    }

    public float getAttackSpeedMultiplier() {
        return attackSpeedMultiplier;
    }

    public float getEffectiveRange() {
        return getRange();
    }

    public int getWidth() {
        return 64;
    }

    public int getHeight() {
        return 64;
    }

    // Default implementation for on-hit effects. Can be overridden by specific warriors.
    public void applyOnHitEffect(Enemy enemy, scenes.Playing playingScene) {
        // Base warriors typically don't have special on-hit effects beyond damage.
        // This can be left empty or log a message if needed.
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public BufferedImage[] getAnimationFrames() {
        return LoadSave.getWarriorAnimation(this);
    }

    public void updateAnimationTick() {
        animationTick++;
        if (animationTick >= animationSpeed) {
            animationTick = 0;
            animationIndex++;
            if (animationIndex >= maxFrameCount) {
                animationIndex = 0;
            }
        }
    }

    public int getAnimationIndex() {
        return animationIndex;
    }
} 