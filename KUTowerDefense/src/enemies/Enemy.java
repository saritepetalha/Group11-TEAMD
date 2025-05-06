package enemies;

import constants.Constants;
import managers.AudioManager;

import java.awt.*;
import static constants.Constants.*;

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

    // for animation of enemies' walking
    private int animationIndex = 0;
    private int animationTick = 0;
    private int animationSpeed = 10; // lower is faster
    private int maxFrameCount = 6;   // default frame count

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

    private void setBoundaryForSize(Size size) {
        switch (size) {
            case SMALL:
                // smaller hitbox for small enemies
                boundary = new Rectangle((int)x + 8, (int)y + 8, 24, 24);
                break;
            case MEDIUM:
                // medium hitbox for medium enemies
                boundary = new Rectangle((int)x + 4, (int)y + 4, 32, 32);
                break;
            case LARGE:
                // larger hitbox for large enemies
                boundary = new Rectangle((int)x, (int)y, 48, 48);
                break;
            default:
                boundary = new Rectangle((int)x + 4, (int)y + 4, 32, 32);
                break;
        }
    }

    private void initializeHealth() {
        health = Constants.Enemies.getStartHealth(enemyType);
    }

    public void move(float x, float y){
        this.x += x;
        this.y += y;

        updateBounds();
    }

    private void updateBounds(){
        // Keep the same offset for each size to ensure consistency
        switch (size) {
            case SMALL:
                boundary.x = (int)x + 8;
                boundary.y = (int)y + 8;
                break;
            case MEDIUM:
                boundary.x = (int)x + 4;
                boundary.y = (int)y + 4;
                break;
            case LARGE:
                boundary.x = (int)x;
                boundary.y = (int)y;
                break;
            default:
                boundary.x = (int)x + 4;
                boundary.y = (int)y + 4;
                break;
        }
    }

    public void setPos(float x, float y){
        this.x = x;
        this.y = y;
        updateBounds();
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
}
