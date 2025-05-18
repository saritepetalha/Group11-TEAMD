package enemies;

import config.EnemyStats;
import config.EnemyType;
import config.GameOptions;
import constants.Constants;
import managers.AudioManager;

import java.awt.*;
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

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    // Enemy size category
    public enum Size {
        SMALL,  // goblin, tnt
        MEDIUM, // warrior, barrel
        LARGE   // troll
    }

    private Size size;

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

        // set boundary based on size
        setBoundaryForSize(size);

        // initialize health based on enemy type
        initializeHealth();
        maxHealth = health;
    }

    /**
     * Updates enemy stats based on the provided GameOptions
     * @param options The GameOptions containing enemy stats
     */
    public void updateStatsFromOptions(GameOptions options) {
        try {
            if (options == null) {
                System.out.println("Warning: GameOptions is null, using default stats for enemy ID " + id);
                return;
            }

            EnemyType type = getEnemyTypeEnum();
            if (type == null) {
                System.out.println("Warning: Could not determine EnemyType for enemy ID " + id + " (type " + enemyType + "), using default stats");
                return;
            }

            if (!options.getEnemyStats().containsKey(type)) {
                System.out.println("Warning: No stats found for enemy type " + type + ", using default stats");
                return;
            }

            EnemyStats stats = options.getEnemyStats().get(type);
            if (stats == null) {
                System.out.println("Warning: Stats are null for enemy type " + type + ", using default stats");
                return;
            }

            // Store current health percentage before updating maxHealth
            float currentHealthPercentage = getHealthBarPercentage();

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
    protected EnemyType getEnemyTypeEnum() {
        try {
            switch (enemyType) {
                case GOBLIN:
                    return EnemyType.GOBLIN;
                case WARRIOR:
                    return EnemyType.WARRIOR;
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

    private void setBoundaryForSize(Size size) {
        switch (size) {
            case SMALL:
                boundary = new Rectangle((int)x - 16, (int)y - 16, 32, 32);
                break;
            case MEDIUM:
                boundary = new Rectangle((int)x - 24, (int)y - 24, 48, 48);
                break;
            case LARGE:
                boundary = new Rectangle((int)x - 32, (int)y - 32, 64, 64);
                break;
            default:
                boundary = new Rectangle((int)x - 16, (int)y - 16, 32, 32);
                break;
        }
    }

    protected void initializeHealth() {
        health = Constants.Enemies.getStartHealth(enemyType);
    }

    public void move(float xSpeed, float ySpeed) {
        this.x += xSpeed;
        this.y += ySpeed;

        // Update direction based on movement
        float totalSpeed = (float) Math.sqrt(xSpeed * xSpeed + ySpeed * ySpeed);
        if (totalSpeed > 0) {
            this.dirX = xSpeed / totalSpeed;
            this.dirY = ySpeed / totalSpeed;
        }

        // Update the boundary position
        updateBoundary();
    }

    private void updateBoundary() {
        switch (size) {
            case SMALL:
                boundary.x = (int)x - 16;
                boundary.y = (int)y - 16;
                break;
            case MEDIUM:
                boundary.x = (int)x - 24;
                boundary.y = (int)y - 24;
                break;
            case LARGE:
                boundary.x = (int)x - 32;
                boundary.y = (int)y - 32;
                break;
        }
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

    public int getMaxFrameCount() {
        return maxFrameCount;
    }

    public Size getSize() {
        return size;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public Rectangle getBounds() {return boundary;}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public void hurt(int damage){
        this.health -= damage;
        if(health <= 0) {
            playDeathSound();
            alive = false;
        }
    }

    private void playDeathSound() {
        if (enemyType == Constants.Enemies.TROLL) {
            AudioManager.getInstance().playTrollDeathSound();
        } else if (enemyType == Constants.Enemies.GOBLIN ||
                enemyType == Constants.Enemies.TNT ||
                enemyType == Constants.Enemies.BARREL) {
            AudioManager.getInstance().playRandomGoblinDeathSound();
        } else if (enemyType == Constants.Enemies.WARRIOR) {
            AudioManager.getInstance().playWarriorDeathSound();
        }
    }

    public boolean isAlive() {return alive;}

    public int getEnemyType() {return enemyType;}

    public float getSpeed() {return speed;}

    public boolean hasReachedEnd() {return reachedEnd;}

    public void setReachedEnd(boolean reachedEnd) {this.reachedEnd = reachedEnd;}

    public int getCurrentPathIndex() {return currentPathIndex;}

    public void setCurrentPathIndex(int currentPathIndex) {this.currentPathIndex = currentPathIndex;}

    public float getHealthBarPercentage() {return health / (float) maxHealth;}


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
     * @return The exact center X coordinate of the sprite
     */
    public float getSpriteCenterX() {
        // the adjustment factors are based on how enemies are drawn in EnemyManager.drawEnemy
        // can be modified
        switch (size) {
            case SMALL:
                return x - 10;
            case MEDIUM:
                if (enemyType == Constants.Enemies.BARREL) {
                    return x - 15;
                } else {
                    return x - 25;
                }
            case LARGE:
                if (enemyType == Constants.Enemies.TROLL) {
                    return x - 40;
                } else {
                    return x - 50;
                }
            default:
                return x;
        }
    }

    /**
     * Gets the center Y coordinate of the enemy's sprite as drawn on screen
     * @return The exact center Y coordinate of the sprite
     */
    public float getSpriteCenterY() {
        // the adjustment factors are based on how enemies are drawn in EnemyManager.drawEnemy
        // can be modified
        switch (size) {
            case SMALL:
                return y - 10;
            case MEDIUM:
                if (enemyType == Constants.Enemies.BARREL) {
                    return y - 15;
                } else {
                    return y - 25;
                }
            case LARGE:
                if (enemyType == Constants.Enemies.TROLL) {
                    return y - 40;
                } else {
                    return y - 50;
                }
            default:
                return y;
        }
    }
    public int getGoldReward() {
        return goldReward;
    }

    public float getDirX() {
        return dirX;
    }

    public float getDirY() {
        return dirY;
    }
}
