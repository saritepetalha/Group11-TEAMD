package enemies;

import java.awt.*;
import static constants.Constants.*;

public abstract class Enemy {
    private float x,y;          // using floats to have much more control when dealing with speed of the enemies
    private int id;
    private int health;
    private int maxHealth;
    private int enemyType;
    private int currentPathIndex = 0;
    private float speed;
    private boolean reachedEnd = false;
    private Rectangle boundary;    // for hit box

    // for animation of enemies' walking
    private int animationIndex = 0;
    private int animationTick = 0;
    private int animationSpeed = 10; // lower is faster

    public Enemy(float x, float y, int id, int enemyType, float speed){
        this.x = x;
        this.y = y;
        this.id = id;
        this.enemyType = enemyType;
        this.speed = speed;
        boundary = new Rectangle((int)x, (int)y, 64, 64);

        // initialize health based on enemy type
        initializeHealth();
        maxHealth = health;
    }

    protected abstract void initializeHealth();

    public void move(float x, float y){
        this.x += x;
        this.y += y;

        updateBounds();
    }

    private void updateBounds(){
        boundary.x = (int) x;
        boundary.y = (int) y;
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
            if (animationIndex >= 6) {
                animationIndex = 0;
            }
        }
    }

    public int getAnimationIndex() {
        return animationIndex;
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

    public boolean isAlive(){return health > 0;}

    public void hurt(int damage){
        this.health -= damage;
        if(health <= 0) health = 0;
    }

    public int getEnemyType() {return enemyType;}

    public float getSpeed() {return speed;}

    public boolean hasReachedEnd() {return reachedEnd;}

    public void setReachedEnd(boolean reachedEnd) {this.reachedEnd = reachedEnd;}

    public int getCurrentPathIndex() {return currentPathIndex;}

    public void setCurrentPathIndex(int currentPathIndex) {this.currentPathIndex = currentPathIndex;}

    public float getHealthBarPercentage() {return (float) health/maxHealth;}

}
