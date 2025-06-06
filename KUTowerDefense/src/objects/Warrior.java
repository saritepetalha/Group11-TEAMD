package objects;

import enemies.Enemy;
import strategies.TargetingStrategy;
import strategies.FirstEnemyStrategy;
import helpMethods.LoadSave;

import java.awt.*;
import java.awt.image.BufferedImage;

public abstract class Warrior {

    // Position and identification
    private int x, y, ID, countDownClock;
    protected int damage;
    protected float range, cooldown;
    private static int num = 0;
    protected int level = 1;
    protected float attackSpeedMultiplier = 1.0f;
    
    // Tower reference for tracking
    private Tower spawnedFromTower;
    
    // Lifetime management (similar to GoldFactory)
    private static final long LIFETIME_MILLIS = 30000; // 30 seconds lifetime
    private long creationTime;
    private boolean isReturning = false;

    // Strategy Pattern: Warrior targeting behavior
    protected TargetingStrategy targetingStrategy;

    // Animation fields - updated for new sprite system
    protected int animationIndex = 0;
    protected int animationTick = 0;
    protected int animationSpeed = 8; // Faster animation for 8-frame sprites
    protected int runFrameCount = 8; // Run animations have 8 frames
    protected int attackFrameCount = 8; // Default attack frame count (wizard)

    // ========== NEW: Movement and State System ==========

    public enum WarriorState {
        RUNNING,    // Moving from spawn to destination
        IDLE,       // Stationary but no enemies in range
        ATTACKING,  // Stationary and attacking enemies
        RETURNING   // Moving back to tower (lifetime expired)
    }

    private WarriorState currentState = WarriorState.RUNNING;

    // Movement fields
    private int spawnX, spawnY;     // Where the warrior spawned from (tower position)
    private int targetX, targetY;   // Where the warrior is moving to
    private float moveSpeed = 2.0f; // Movement speed in pixels per update
    private boolean hasReachedDestination = false;

    // ========== NEW: Facing Direction System ==========

    private boolean facingLeft = false; // Default facing right (false = facing right, true = facing left)
    private Enemy currentTarget = null; // Current enemy being targeted

    // Cached animation frames
    private BufferedImage[] runFrames = null;
    private BufferedImage[] attackFrames = null;

    public Warrior(int spawnX, int spawnY, int targetX, int targetY) {
        // Start at spawn position
        this.x = spawnX;
        this.y = spawnY;
        this.spawnX = spawnX;
        this.spawnY = spawnY;
        this.targetX = targetX;
        this.targetY = targetY;

        this.ID = num;
        num++;
        this.creationTime = System.currentTimeMillis();

        // Default targeting strategy is FirstEnemy (current behavior)
        this.targetingStrategy = new FirstEnemyStrategy();
        initializeAnimationParameters();
        loadAnimationFrames();

        // Set initial facing direction based on spawn vs target position
        determineFacingDirectionForMovement();
    }

    // Constructor with custom targeting strategy
    public Warrior(int spawnX, int spawnY, int targetX, int targetY, TargetingStrategy targetingStrategy) {
        this.x = spawnX;
        this.y = spawnY;
        this.spawnX = spawnX;
        this.spawnY = spawnY;
        this.targetX = targetX;
        this.targetY = targetY;

        this.ID = num;
        num++;
        this.creationTime = System.currentTimeMillis();
        this.targetingStrategy = targetingStrategy != null ? targetingStrategy : new FirstEnemyStrategy();
        initializeAnimationParameters();
        loadAnimationFrames();

        // Set initial facing direction based on spawn vs target position
        determineFacingDirectionForMovement();
    }

    // Legacy constructor for backward compatibility
    public Warrior(int x, int y) {
        this(x, y, x, y); // No movement, start in attacking state
        this.hasReachedDestination = true;
        this.currentState = WarriorState.ATTACKING;
        this.creationTime = System.currentTimeMillis();
    }

    protected abstract void initializeAnimationParameters();

    public abstract int getType();
    public abstract float getCooldown();
    public abstract float getRange();
    public abstract int getDamage();
    public abstract int getCost();

    /**
     * Load animation frames for this warrior type
     */
    private void loadAnimationFrames() {
        runFrames = LoadSave.getWarriorRunAnimation(this);
        attackFrames = LoadSave.getWarriorAttackAnimation(this);
    }

    /**
     * Update warrior movement and state
     */
    public void update(float gameSpeedMultiplier) {
        // Update cooldown
        countDownClock += gameSpeedMultiplier * attackSpeedMultiplier;

        // Check if lifetime has expired and warrior should return to tower
        long currentTime = System.currentTimeMillis();
        if (currentTime - creationTime > LIFETIME_MILLIS && !isReturning) {
            startReturningToTower();
        }

        // Update based on current state
        if (currentState == WarriorState.RUNNING && !hasReachedDestination) {
            updateMovement(gameSpeedMultiplier);
        } else if (currentState == WarriorState.RETURNING) {
            updateReturnMovement(gameSpeedMultiplier);
        }

        // Update animation
        updateAnimationTick();
    }

    /**
     * Update warrior movement towards target position
     */
    private void updateMovement(float speedMultiplier) {
        float dx = targetX - x;
        float dy = targetY - y;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        // Check if we've reached the destination
        if (distance <= moveSpeed * speedMultiplier) {
            // Snap to exact target position
            x = targetX;
            y = targetY;
            hasReachedDestination = true;
            currentState = WarriorState.IDLE; // Start in idle state, will be changed to attacking when enemy found
            System.out.println("Warrior reached destination: (" + targetX + ", " + targetY + ")");
        } else {
            // Move towards target
            float moveX = (dx / distance) * moveSpeed * speedMultiplier;
            float moveY = (dy / distance) * moveSpeed * speedMultiplier;

            x += (int) moveX;
            y += (int) moveY;
        }
    }

    /**
     * Start the return-to-tower process when lifetime expires
     */
    private void startReturningToTower() {
        isReturning = true;
        currentState = WarriorState.RETURNING;
        
        // Set target back to spawn position (tower)
        targetX = spawnX;
        targetY = spawnY;
        hasReachedDestination = false;
        
        // Update facing direction for return journey - face the direction towards tower
        int deltaX = targetX - x; // Current position to tower
        if (deltaX < 0) {
            facingLeft = true; // Tower is to the left
        } else if (deltaX > 0) {
            facingLeft = false; // Tower is to the right
        }
        // If deltaX == 0, keep current facing direction
        
        System.out.println("Warrior lifetime expired, returning to tower at (" + spawnX + ", " + spawnY + ")");
    }

    /**
     * Update warrior movement back to tower
     */
    private void updateReturnMovement(float speedMultiplier) {
        float dx = targetX - x;
        float dy = targetY - y;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        // Check if we've reached the tower
        if (distance <= moveSpeed * speedMultiplier) {
            // Warrior has returned to tower - it should be removed by TowerManager
            x = targetX;
            y = targetY;
            hasReachedDestination = true;
            System.out.println("Warrior returned to tower and should be removed");
        } else {
            // Move towards tower
            float moveX = (dx / distance) * moveSpeed * speedMultiplier;
            float moveY = (dy / distance) * moveSpeed * speedMultiplier;

            x += (int) moveX;
            y += (int) moveY;
        }
    }

    /**
     * Determines facing direction based on movement from spawn to target
     */
    private void determineFacingDirectionForMovement() {
        int deltaX = targetX - spawnX;

        // Only consider X axis difference as requested
        if (deltaX < 0) {
            // Moving left (target is to the left of spawn tower)
            facingLeft = true;
        } else if (deltaX > 0) {
            // Moving right (target is to the right of spawn tower)
            facingLeft = false;
        }
        // If deltaX == 0, keep default facing direction (right)
    }

    /**
     * Updates facing direction based on target enemy position
     */
    public void updateFacingDirectionForTarget(Enemy targetEnemy) {
        if (targetEnemy == null) return;

        this.currentTarget = targetEnemy;

        // Get warrior center position
        int warriorCenterX = x + getWidth() / 2;

        // Get enemy center position
        float enemyCenterX = targetEnemy.getSpriteCenterX();

        // Determine facing based on enemy position relative to warrior
        float deltaX = enemyCenterX - warriorCenterX;

        if (deltaX < 0) {
            // Enemy is to the left, face left
            facingLeft = true;
        } else if (deltaX > 0) {
            // Enemy is to the right, face right
            facingLeft = false;
        }
        // If deltaX == 0, keep current facing direction
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

    // Legacy update method for backward compatibility
    public void update() {
        update(1.0f);
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

    /**
     * Get animation frames based on current state
     */
    public BufferedImage[] getAnimationFrames() {
        if ((currentState == WarriorState.RUNNING || currentState == WarriorState.RETURNING) && runFrames != null) {
            return runFrames;
        } else if (currentState == WarriorState.ATTACKING && attackFrames != null) {
            return attackFrames;
        } else if (currentState == WarriorState.IDLE && attackFrames != null) {
            // For idle state, create a single-frame array with the first attack frame (resting pose)
            return new BufferedImage[]{attackFrames[0]};
        }
        // Fallback to old method for compatibility
        return LoadSave.getWarriorAttackAnimation(this);
    }

    public void updateAnimationTick() {
        // Don't animate when idle (stay on first frame)
        if (currentState == WarriorState.IDLE) {
            animationIndex = 0;
            return;
        }

        animationTick++;
        if (animationTick >= animationSpeed) {
            animationTick = 0;
            animationIndex++;

            // Use correct frame count based on current state
            int maxFrames = (currentState == WarriorState.RUNNING || currentState == WarriorState.RETURNING) ? runFrameCount : attackFrameCount;
            if (animationIndex >= maxFrames) {
                animationIndex = 0;
            }
        }
    }

    public int getAnimationIndex() {
        return animationIndex;
    }

    // ========== NEW: State and Movement Getters ==========

    public WarriorState getCurrentState() {
        return currentState;
    }

    public boolean hasReachedDestination() {
        return hasReachedDestination;
    }

    public boolean isRunning() {
        return currentState == WarriorState.RUNNING && !hasReachedDestination;
    }

    public boolean isAttacking() {
        return currentState == WarriorState.ATTACKING;
    }

    public boolean isIdle() {
        return currentState == WarriorState.IDLE;
    }

    public int getSpawnX() {
        return spawnX;
    }

    public int getSpawnY() {
        return spawnY;
    }

    public int getTargetX() {
        return targetX;
    }

    public int getTargetY() {
        return targetY;
    }

    public void setMoveSpeed(float speed) {
        this.moveSpeed = speed;
    }

    public float getMoveSpeed() {
        return moveSpeed;
    }

    // ========== NEW: Facing Direction Getters ==========

    /**
     * Returns whether the warrior is currently facing left
     * @return true if facing left, false if facing right
     */
    public boolean isFacingLeft() {
        return facingLeft;
    }

    /**
     * Sets the facing direction manually
     * @param facingLeft true for facing left, false for facing right
     */
    public void setFacingLeft(boolean facingLeft) {
        this.facingLeft = facingLeft;
    }

    /**
     * Gets the current target enemy (may be null)
     */
    public Enemy getCurrentTarget() {
        return currentTarget;
    }

    /**
     * Clears the current target and resets facing to movement direction
     */
    public void clearCurrentTarget() {
        this.currentTarget = null;
        // Reset to movement-based facing direction
        determineFacingDirectionForMovement();
    }

    /**
     * Set new target destination and start movement
     */
    public void setTargetDestination(int newTargetX, int newTargetY) {
        this.targetX = newTargetX;
        this.targetY = newTargetY;
        this.hasReachedDestination = false;
        this.currentState = WarriorState.RUNNING;

        // Update facing direction for new movement
        determineFacingDirectionForMovement();

        System.out.println("Warrior target set to: (" + newTargetX + ", " + newTargetY + ")");
    }

    /**
     * Set warrior to attacking state when an enemy is found in range
     */
    public void setAttackingState() {
        if (hasReachedDestination) {
            currentState = WarriorState.ATTACKING;
        }
    }

    /**
     * Set warrior to idle state when no enemies are in range
     */
    public void setIdleState() {
        if (hasReachedDestination) {
            currentState = WarriorState.IDLE;
            clearCurrentTarget(); // Clear target when going idle
        }
    }

    /**
     * Check if warrior should be removed (has returned to tower)
     */
    public boolean shouldBeRemoved() {
        return isReturning && hasReachedDestination;
    }

    /**
     * Check if warrior is in returning state
     */
    public boolean isReturning() {
        return isReturning;
    }

    /**
     * Get remaining lifetime in milliseconds
     */
    public long getRemainingLifetime() {
        long currentTime = System.currentTimeMillis();
        long elapsed = currentTime - creationTime;
        return Math.max(0, LIFETIME_MILLIS - elapsed);
    }

    /**
     * Get lifetime percentage (1.0 = full lifetime, 0.0 = expired)
     */
    public float getLifetimePercentage() {
        long remaining = getRemainingLifetime();
        return (float) remaining / LIFETIME_MILLIS;
    }

    /**
     * Draw lifetime bar above the warrior (similar to GoldFactory)
     */
    public void drawLifetimeBar(Graphics g) {
        if (isReturning) return; // Don't show lifetime bar when returning

        float lifePercentage = getLifetimePercentage();

        int barWidth = getWidth() / 2; // Make bar much smaller (32 pixels instead of 64)
        int barHeight = 4; // Make bar very thin
        int barX = x + (getWidth() - barWidth) / 2; // Center the small bar
        int barY = y + getHeight() - 4; // Position it much closer to the warrior (near the bottom)

        // Background
        g.setColor(Color.DARK_GRAY);
        g.fillRect(barX, barY, barWidth, barHeight);

        // Life remaining
        g.setColor(lifePercentage > 0.3f ? Color.GREEN : Color.RED);
        g.fillRect(barX, barY, (int)(barWidth * lifePercentage), barHeight);

        // Border
        g.setColor(Color.BLACK);
        g.drawRect(barX, barY, barWidth, barHeight);
    }

    // Tower reference methods
    public Tower getSpawnedFromTower() {
        return spawnedFromTower;
    }

    public void setSpawnedFromTower(Tower tower) {
        this.spawnedFromTower = tower;
    }
} 