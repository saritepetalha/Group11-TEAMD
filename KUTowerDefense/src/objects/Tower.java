package objects;

import constants.Constants;
import enemies.Enemy;
import strategies.TargetingStrategy;
import strategies.FirstEnemyStrategy;

import java.awt.*;

public abstract class Tower {

    private int x, y, ID, countDownClock;
    protected int damage;
    protected float range, cooldown;
    private static int num = 0;
    protected int level = 1;
    protected float attackSpeedMultiplier = 1.0f;

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
    public abstract float getRange();
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
    }

    public void update(float gameSpeedMultiplier) {
        countDownClock += gameSpeedMultiplier * attackSpeedMultiplier;
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

    public boolean isDestroyed() { return destroyed; }
    public void setDestroyed(boolean destroyed) { this.destroyed = destroyed; }
    public void setDestroyedSprite(java.awt.image.BufferedImage sprite) { this.destroyedSprite = sprite; }
    public java.awt.image.BufferedImage getDestroyedSprite() { return destroyedSprite; }
}
