package enemies;

import config.EnemyStats;
import config.EnemyType;
import config.GameOptions;
import constants.Constants;
import managers.AudioManager;
import helpMethods.LoadSave;

import java.awt.*;
import java.awt.image.BufferedImage;
import static constants.Constants.Enemies.*;

public abstract class Enemy {
    protected float x,y;          // using floats to have much more control when dealing with speed of the enemies
    protected int id;
    protected int health;
    protected int maxHealth;
    protected int enemyType;
    protected int currentPathIndex = 0;
    protected float speed;
    protected boolean reachedEnd = false;
    protected Rectangle boundary;    // for hit box
    protected boolean alive = true;
    protected float dirX = 0;        // direction X component
    protected float dirY = 0;        // direction Y component

    // for animation of enemies' walking
    private int animationIndex = 0;
    private int animationTick = 0;
    private int animationSpeed = 10; // lower is faster
    private int maxFrameCount = 6;   // default frame count
    protected int goldReward;

    // GRASP Information Expert: Damage calculation and resistance properties
    // The Enemy knows best about its own resistances and vulnerabilities
    protected float physicalResistance = 0.0f;    // 0.0 = no resistance, 0.5 = 50% resistance
    protected float magicalResistance = 0.0f;     // 0.0 = no resistance, 0.5 = 50% resistance
    protected float explosiveResistance = 0.0f;   // 0.0 = no resistance, 0.5 = 50% resistance
    protected float ultimateResistance = 0.0f;    // 0.0 = no resistance, 0.5 = 50% resistance

    // Damage vulnerabilities (values > 1.0 mean extra damage)
    protected float physicalVulnerability = 1.0f;  // 1.0 = normal, 1.5 = 50% extra damage
    protected float magicalVulnerability = 1.0f;   // 1.0 = normal, 1.5 = 50% extra damage
    protected float explosiveVulnerability = 1.0f; // 1.0 = normal, 1.5 = 50% extra damage

    // Damage type enumeration for Information Expert pattern
    public enum DamageType {
        PHYSICAL,    // From archer towers, warriors
        MAGICAL,     // From mage towers
        EXPLOSIVE,   // From artillery towers, TNT
        ULTIMATE     // From ultimate abilities (earthquake, lightning, etc.)
    }

    // Slow effect fields
    private boolean isSlowed = false;
    private long slowTimer = 0; // Will store remaining duration in ticks
    private float currentSlowFactor = 1.0f; // 1.0f means no slow
    public static BufferedImage snowflakeIcon = null;

    // Teleport effect fields
    private boolean isTeleporting = false;
    private long teleportEffectTimer = 0;
    public static final long TELEPORT_EFFECT_DURATION = 500_000_000L; // 0.5 seconds
    private boolean invisible = false;

    // Combat synergy fields
    private boolean hasCombatSynergy = false;
    private float originalSpeed;
    private float synergyGoblinSpeed = 0f;
    public static BufferedImage thunderIcon = null;

    // Constants for sprite anchor points in original sprite sheets (used by EnemyManager for precise alignment)
    // These represent the pixel coordinates that should align with the road center
    public static final int GOBLIN_ANCHOR_X = 96;
    public static final int GOBLIN_ANCHOR_Y = 128;
    public static final int KNIGHT_ANCHOR_X = 96;
    public static final int KNIGHT_ANCHOR_Y = 128;
    public static final int TNT_ANCHOR_X = 96;
    public static final int TNT_ANCHOR_Y = 128;
    public static final int BARREL_ANCHOR_X = 64;
    public static final int BARREL_ANCHOR_Y = 96;
    public static final int TROLL_ANCHOR_X = 120;
    public static final int TROLL_ANCHOR_Y = 240;

    private boolean isFrozen = false;
    private long freezeTimer = 0; // Will store remaining duration in ticks

    // Poison effect fields
    private boolean isPoisoned = false;
    private int poisonDamage = 0;
    private long poisonTimer = 0; // Will store remaining duration in ticks
    private int poisonTickInterval = 60; // Apply poison damage every 60 ticks (1 second at 60 FPS)
    private int poisonTickCounter = 0;
    public static BufferedImage poisonIcon = null;

    // Enemy size category
    public enum Size {
        SMALL(32, 32),
        MEDIUM(48, 48),
        LARGE(64, 64);

        private final int width;
        private final int height;

        Size(int width, int height) {
            this.width = width;
            this.height = height;
        }

        public int getWidth() { return width; }
        public int getHeight() { return height; }
    }

    private Size size;

    // Cached values for performance optimization
    private final int halfWidth;
    private final int halfHeight;

    // Original animation speed before any multipliers are applied
    private int baseAnimationSpeed = 10;

    public Enemy(float x, float y, int id, int enemyType, float speed, Size size, int maxFrameCount){
        this.x = x;
        this.y = y;
        this.id = id;
        this.enemyType = enemyType;
        this.speed = speed;
        this.size = size;
        this.maxFrameCount = maxFrameCount;

        // Cache half dimensions for performance
        this.halfWidth = this.size.getWidth() / 2;
        this.halfHeight = this.size.getHeight() / 2;

        // Initialize boundary directly using cached values
        this.boundary = new Rectangle(
                (int)this.x - this.halfWidth,
                (int)this.y - this.halfHeight,
                this.size.getWidth(),
                this.size.getHeight()
        );

        initializeHealth();
        maxHealth = health;

        // GRASP Information Expert: Initialize resistances based on enemy type
        initializeResistances();
    }

    /**
     * Reads the gold‐reward value for this enemy's type from GameOptions and applies it.
     */
    public void updateGoldRewardFromOptions(GameOptions options) {
        if (options == null) {
            System.out.println("Warning: GameOptions is null; using default goldReward=" + goldReward);
            return;
        }

        EnemyType type = getEnemyTypeEnum();
        if (type == null) {
            System.out.println("Warning: Unknown EnemyType for id=" + id + "; using default goldReward=" + goldReward);
            return;
        }

        EnemyStats stats = options.getEnemyStats().get(type);
        if (stats == null) {
            System.out.println("Warning: No EnemyStats for type=" + type + "; using default goldReward=" + goldReward);
            return;
        }
        this.goldReward = stats.getGoldReward();
        System.out.println("Updated goldReward for " + type + " (ID: " + id + ") = " + goldReward);
    }

    /**
     * Updates enemy stats based on the provided GameOptions
     * @param options The GameOptions containing enemy stats
     */
    public void updateStatsFromOptions(GameOptions options) {
        try {
            if (options == null) {
                //System.out.println("Warning: GameOptions is null, using default stats for enemy ID " + id);
                return;
            }

            EnemyType type = getEnemyTypeEnum();
            if (type == null) {
                System.out.println("Warning: Could not determine EnemyType for enemy ID " + id + " (type " + enemyType + "), using default stats");
                return;
            }

            EnemyStats stats = options.getEnemyStats().get(type);
            if (stats == null) {
                System.out.println("Warning: No stats found for enemy type " + type + ", using default stats");
                return;
            }

            // Store current health percentage before updating maxHealth
            float currentHealthPercentage = getHealthBarPercentage();
            updateGoldRewardFromOptions(options);

            // Apply the new stats
            this.maxHealth = stats.getHitPoints();
            // Apply the previous health percentage to the new maxHealth, clamp between 0 and maxHealth
            this.health = Math.max(0, Math.min(this.maxHealth, (int)(this.maxHealth * currentHealthPercentage)));
            this.speed = (float)stats.getMoveSpeed();

            System.out.println("Applied stats for " + type + " (ID: "+ id +"): MaxHP=" + maxHealth + ", CurrentHP=" + health + ", Speed=" + speed);
        } catch (Exception e) {
            System.out.println("Error updating enemy stats for ID " + id + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Converts the enemy's integer type to the corresponding EnemyType enum
     * @return The EnemyType enum value, or null if not found
     */
    public EnemyType getEnemyTypeEnum() {
        try {
            switch (enemyType) {
                case GOBLIN:
                    return EnemyType.GOBLIN;
                case KNIGHT:
                    return EnemyType.KNIGHT;
                case BARREL:
                    return EnemyType.BARREL;
                case TNT:
                    return EnemyType.TNT;
                case TROLL:
                    return EnemyType.TROLL;
                default:
                    System.out.println("Unknown enemy type: " + enemyType);
                    return null;
            }
        } catch (Exception e) {
            System.out.println("Error getting enemy type enum: " + e.getMessage());
            return null;
        }
    }

    protected void initializeHealth() {
        health = Constants.Enemies.getStartHealth(enemyType);
    }

    public void updateAnimationTick() {
        if (isFrozen) {
            return;
        }
        animationTick++;
        if (animationTick >= animationSpeed) {
            animationTick = 0;
            animationIndex++;
            if (animationIndex >= maxFrameCount) {
                animationIndex = 0;
            }
        }
    }

    // Getters and Setters
    public int getAnimationIndex() { return animationIndex; }
    public Size getSize() { return size; }
    public float getX() { return x; }
    public void setX(float x) { this.x = x; }
    public float getY() { return y; }
    public void setY(float y) { this.y = y; }
    public Rectangle getBounds() { return boundary; }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getHealth() { return health; }
    public void setHealth(int health) { this.health = health; }
    public boolean isAlive() { return alive; }
    public int getEnemyType() { return enemyType; }
    public float getSpeed() { return speed; }
    public boolean hasReachedEnd() { return reachedEnd; }
    public void setReachedEnd(boolean reachedEnd) { this.reachedEnd = reachedEnd; }
    public int getCurrentPathIndex() { return currentPathIndex; }
    public void setCurrentPathIndex(int currentPathIndex) { this.currentPathIndex = currentPathIndex; }
    public float getHealthBarPercentage() { return health / (float) maxHealth; }
    public int getGoldReward() { return goldReward; }
    public float getDirX() { return dirX; }
    public float getDirY() { return dirY; }
    public void setDirection(float dirX, float dirY) { this.dirX = dirX; this.dirY = dirY; }
    public int getWidth() { return size.getWidth(); }
    public int getHeight() { return size.getHeight(); }
    public boolean isSlowed() { return isSlowed; }
    public boolean isTeleporting() { return isTeleporting; }
    public long getTeleportEffectTimer() { return teleportEffectTimer; }
    public boolean hasCombatSynergy() { return hasCombatSynergy; }
    public void setInvisible(boolean invisible) { this.invisible = invisible; }

    /**
     * GRASP Information Expert: Calculate actual damage after applying resistances
     * The Enemy is the expert on its own resistance values and damage calculation
     * @param rawDamage The base damage before resistances
     * @param damageType The type of damage being dealt
     * @return The actual damage after resistances are applied
     */
    public int calculateActualDamage(int rawDamage, DamageType damageType) {
        if (rawDamage <= 0) return 0;

        float resistance = getResistanceForDamageType(damageType);
        float vulnerability = getVulnerabilityForDamageType(damageType);

        // Apply resistance first (reduces damage)
        float damageAfterResistance = rawDamage * (1.0f - resistance);

        // Then apply vulnerability (can increase damage)
        float finalDamage = damageAfterResistance * vulnerability;

        // Ensure minimum damage of 1 if raw damage was > 0
        int actualDamage = Math.max(1, Math.round(finalDamage));

        // Log damage calculation for debugging
        if (resistance > 0 || vulnerability != 1.0f) {
            System.out.println("Enemy " + getEnemyTypeEnum() + " took " + actualDamage +
                    " " + damageType + " damage (raw: " + rawDamage +
                    ", resistance: " + (resistance * 100) + "%" +
                    ", vulnerability: " + (vulnerability * 100) + "%)");
        }

        return actualDamage;
    }

    /**
     * GRASP Information Expert: Get resistance value for specific damage type
     */
    private float getResistanceForDamageType(DamageType damageType) {
        switch (damageType) {
            case PHYSICAL: return physicalResistance;
            case MAGICAL: return magicalResistance;
            case EXPLOSIVE: return explosiveResistance;
            case ULTIMATE: return ultimateResistance;
            default: return 0.0f;
        }
    }

    /**
     * GRASP Information Expert: Get vulnerability value for specific damage type
     */
    private float getVulnerabilityForDamageType(DamageType damageType) {
        switch (damageType) {
            case PHYSICAL: return physicalVulnerability;
            case MAGICAL: return magicalVulnerability;
            case EXPLOSIVE: return explosiveVulnerability;
            case ULTIMATE: return 1.0f; // Ultimate abilities ignore vulnerabilities
            default: return 1.0f;
        }
    }

    /**
     * GRASP Information Expert: Apply damage with type-specific resistances
     * This replaces the old hurt() method with intelligent damage calculation
     */
    public void takeDamage(int rawDamage, DamageType damageType) {
        takeDamage(rawDamage, damageType, false);
    }

    /**
     * GRASP Information Expert: Apply damage with optional invisibility bypass
     */
    public void takeDamage(int rawDamage, DamageType damageType, boolean ignoreInvisibility) {
        // Check invisibility for Goblin enemies
        if (!ignoreInvisibility && enemyType == Constants.Enemies.GOBLIN && invisible) {
            System.out.println("Attack missed invisible Goblin!");
            return;
        }

        // Calculate actual damage using Information Expert pattern
        int actualDamage = calculateActualDamage(rawDamage, damageType);

        // Apply the calculated damage
        this.health -= actualDamage;

        // Handle death
        if (health <= 0) {
            playDeathSound();
            alive = false;
        }
    }

    /**
     * GRASP Information Expert: Initialize resistance values based on enemy type
     * Each enemy type has different resistance characteristics
     */
    protected void initializeResistances() {
        switch (enemyType) {
            case KNIGHT:
                // Knights have strong physical resistance but are vulnerable to magic
                physicalResistance = 0.3f;     // 30% physical resistance
                magicalVulnerability = 1.2f;   // 20% extra magical damage
                explosiveResistance = 0.1f;    // 10% explosive resistance
                break;

            case TROLL:
                // Trolls are very tough with general resistance but slow
                physicalResistance = 0.2f;     // 20% physical resistance
                magicalResistance = 0.2f;      // 20% magical resistance
                explosiveResistance = 0.4f;    // 40% explosive resistance (thick skin)
                ultimateResistance = 0.1f;     // 10% ultimate resistance
                break;

            case GOBLIN:
                // Goblins are nimble but fragile, vulnerable to explosives
                explosiveVulnerability = 1.3f; // 30% extra explosive damage
                break;

            case BARREL:
                // Barrels are explosive containers, very vulnerable to fire/explosive
                explosiveVulnerability = 1.5f; // 50% extra explosive damage
                magicalVulnerability = 1.2f;   // 20% extra magical damage (fire)
                break;

            case TNT:
                // TNT is extremely volatile
                explosiveVulnerability = 2.0f; // 100% extra explosive damage
                physicalResistance = 0.1f;     // 10% physical resistance (hard shell)
                break;

            default:
                // Default values (already set in field declarations)
                break;
        }
    }



    // Legacy hurt methods - now delegate to the new takeDamage methods
    @Deprecated
    public void hurt(int damage){
        takeDamage(damage, DamageType.PHYSICAL);
    }

    @Deprecated
    public void hurt(int damage, boolean ignoreInvisibility) {
        takeDamage(damage, DamageType.PHYSICAL, ignoreInvisibility);
    }

    private void playDeathSound() {
        EnemyType type = getEnemyTypeEnum();
        if (type == null) return;

        switch (type) {
            case TROLL:
                AudioManager.getInstance().playTrollDeathSound();
                break;
            case GOBLIN:
            case TNT:
            case BARREL:
                AudioManager.getInstance().playRandomGoblinDeathSound();
                break;
            case KNIGHT:
                AudioManager.getInstance().playKnightDeathSound();
                break;
            default:
                // Optional: Log a warning or play a default sound for unhandled types
                System.out.println("Warning: No specific death sound for enemy type: " + type);
                break;
        }
    }

    public void setAnimationSpeed(int speed) {
        this.baseAnimationSpeed = speed;
        this.animationSpeed = speed;
    }

    /**
     * Adjust animation speed based on game speed multiplier
     * This ensures animations remain properly synchronized when game speed changes
     * @param speedMultiplier The current game speed multiplier
     */
    public void adjustAnimationForGameSpeed(float speedMultiplier) {
        // animation speed is inversely proportional to game speed
        // when game is faster, animation needs to update more quickly (lower animation speed value)
        if (speedMultiplier > 1.0f) {
            // ensure animation speed doesn't go below 1 (too fast)
            this.animationSpeed = Math.max(1, (int)(baseAnimationSpeed / speedMultiplier));
        } else {
            this.animationSpeed = baseAnimationSpeed;
        }
    }

    /**
     * Gets the center X coordinate of the enemy's sprite as drawn on screen
     * With the new anchor-based positioning system, the sprite center is the same as the enemy's position
     * @return The exact center X coordinate of the sprite
     */
    public float getSpriteCenterX() {
        // With anchor-based positioning, the enemy's x position represents the sprite center
        return x;
    }

    /**
     * Gets the center Y coordinate of the enemy's sprite as drawn on screen
     * With the new anchor-based positioning system, the sprite center is the same as the enemy's position
     * @return The exact center Y coordinate of the sprite
     */
    public float getSpriteCenterY() {
        // With anchor-based positioning, the enemy's y position represents the sprite center
        return y;
    }

    public void applySlow(float slowFactor, int durationTicks) {
        if (!isSlowed || slowFactor < this.currentSlowFactor) { // Apply new slow if not slowed, or if new slow is stronger
            this.isSlowed = true;
            this.currentSlowFactor = slowFactor;
            this.slowTimer = durationTicks;
            if (snowflakeIcon == null) {
                snowflakeIcon = LoadSave.getImageFromPath("/TowerAssets/snow flake icon.png");
            }
        }
    }

    private void updateSlow(float speedMultiplier) {
        if (isSlowed) {
            slowTimer -= speedMultiplier;
            if (slowTimer <= 0) {
                isSlowed = false;
                currentSlowFactor = 1.0f; // Reset slow factor
            }
        }
    }

    /**
     * Applies a teleport visual effect to the enemy
     */
    public void applyTeleportEffect() {
        isTeleporting = true;
        teleportEffectTimer = System.nanoTime();
    }

    private void updateTeleportEffect() {
        if (isTeleporting && System.nanoTime() - teleportEffectTimer > TELEPORT_EFFECT_DURATION) {
            isTeleporting = false;
        }
    }

    public void freeze(int durationTicks) {
        this.isFrozen = true;
        this.freezeTimer = durationTicks;
        System.out.println("Enemy ID: " + id + " is now frozen.");
    }

    private void updateFreeze(float speedMultiplier) {
        if (isFrozen) {
            freezeTimer -= speedMultiplier;
            if (freezeTimer <= 0) {
                isFrozen = false;
                System.out.println("Freeze effect ended for enemy ID: " + id);
            }
        }
    }

    public boolean isFrozen() {
        return isFrozen;
    }

    /**
     * Apply poison effect to this enemy
     * @param damage Damage per poison tick
     * @param durationTicks How long the poison lasts (in game ticks)
     */
    public void applyPoison(int damage, int durationTicks) {
        if (!isPoisoned || damage > this.poisonDamage) { // Apply new poison if not poisoned, or if new poison is stronger
            this.isPoisoned = true;
            this.poisonDamage = damage;
            this.poisonTimer = durationTicks;
            this.poisonTickCounter = 0; // Reset tick counter
            if (poisonIcon == null) {
                poisonIcon = LoadSave.getImageFromPath("/TowerAssets/poison_icon.png");
            }
            System.out.println("Enemy ID: " + id + " is now poisoned for " + damage + " damage over " + durationTicks + " ticks.");
        }
    }

    private void updatePoison() {
        if (isPoisoned) {
            poisonTickCounter++;

            // Apply poison damage every interval
            if (poisonTickCounter >= poisonTickInterval) {
                // Use MAGICAL damage type for poison (since it's like a magical effect)
                takeDamage(poisonDamage, DamageType.MAGICAL, true); // Ignore invisibility for poison
                poisonTickCounter = 0; // Reset counter
                System.out.println("Enemy ID: " + id + " took " + poisonDamage + " poison damage. Health: " + health);
            }

            // Decrease poison timer
            poisonTimer--;
            if (poisonTimer <= 0) {
                isPoisoned = false;
                poisonDamage = 0;
                poisonTickCounter = 0;
                System.out.println("Poison effect ended for enemy ID: " + id);
            }
        }
    }

    public boolean isPoisoned() {
        return isPoisoned;
    }

    public void update(float speedMultiplier) {
        updateFreeze(speedMultiplier);
        updatePoison();
        if (!isFrozen) {
            updateAnimationTick();
        }
        updateStatsFromOptions(null);
        updateSlow(speedMultiplier);
        updateTeleportEffect();
    }

    public void update() {
        update(1.0f); // Default speed multiplier for backward compatibility
    }

    public float getEffectiveSpeed() {
        if (isFrozen) {
            return 0; // Ensure no movement when frozen
        }
        float effectiveSpeed = speed;
        if (hasCombatSynergy) {

            effectiveSpeed = (originalSpeed + synergyGoblinSpeed) * 0.5f; // Use multiplication instead of division

        }
        if (isSlowed) {
            effectiveSpeed *= 0.5f;
        }
        return effectiveSpeed;
    }

    public void applyCombatSynergy(float goblinSpeed) {
        if (!hasCombatSynergy) {
            originalSpeed = speed;
            hasCombatSynergy = true;
        }
        synergyGoblinSpeed = goblinSpeed;
        if (thunderIcon == null) {
            thunderIcon = LoadSave.getImageFromPath("/TowerAssets/thunder_icon.png");
        }
    }

    public void removeCombatSynergy() {
        if (hasCombatSynergy) {
            hasCombatSynergy = false;
            synergyGoblinSpeed = 0f;
        }
    }


    public void draw(Graphics2D g2d) {
        // Draw the enemy sprite using the current animation frame
        BufferedImage sprite = getSpriteFrame(animationIndex);
        if (sprite != null) {
            g2d.drawImage(sprite, (int)x - getWidth() / 2, (int)y - getHeight() / 2, getWidth(), getHeight(), null);
        }
        // If frozen, draw a semi-transparent ice-blue overlay
        if (isFrozen) {
            System.out.println("Enemy ID: " + id + " is frozen.");
            Composite oldComposite = g2d.getComposite();
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
            g2d.setColor(new Color(100, 200, 255)); // ice-blue
            g2d.fillOval((int)x - getWidth() / 2, (int)y - getHeight() / 2, getWidth(), getHeight());
            g2d.setComposite(oldComposite);
        }
        // Health bar drawing
        int barWidth = getWidth();
        int barHeight = 6;
        int barX = (int)x - barWidth / 2;
        int barY = (int)y - getHeight() / 2 - 10; // Above the enemy

        float healthPercent = Math.max(0, (float)health / maxHealth);
        g2d.setColor(Color.DARK_GRAY);
        g2d.fillRect(barX, barY, barWidth, barHeight);
        g2d.setColor(Color.RED);
        g2d.fillRect(barX, barY, (int)(barWidth * healthPercent), barHeight);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(barX, barY, barWidth, barHeight);
    }

    // Default implementation that returns null. Subclasses can override if needed.
    protected BufferedImage getSpriteFrame(int animationIndex) {
        return null;
    }
}
