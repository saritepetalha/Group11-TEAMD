package enemies;

import java.awt.*;

public class Enemy {

    private float x,y;          // using floats to have much more control when dealing with speed of the enemies
    private int id;
    private int health;
    private int enemyType;
    private Rectangle boundary;    // for hit box

    public Enemy(float x, float y, int id, int enemyType){
        this.x = x;
        this.y = y;
        this.id = id;
        this.enemyType = enemyType;
        boundary = new Rectangle((int)x, (int)y, 64, 64);
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

    public int getEnemyType() {
        return enemyType;
    }

    public void setEnemyType(int enemyType) {
        this.enemyType = enemyType;
    }

    public Rectangle getBoundary() {
        return boundary;
    }

    public void setBoundary(Rectangle boundary) {
        this.boundary = boundary;
    }
}
