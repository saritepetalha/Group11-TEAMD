package objects;

import java.awt.geom.Point2D;

public class Projectile {
    private Point2D.Float pos;
    private int id, projectileType, damage;
    private boolean active = true;
    private float xSpeed, ySpeed;

    public Projectile(float x, float y, float xSpeed, float ySpeed, int id, int damage, int projectileType) {
        pos = new Point2D.Float(x,y);
        this.id = id;
        this.damage = damage;
        this.xSpeed = xSpeed;
        this.ySpeed = ySpeed;
        this.projectileType = projectileType;   //0 arrow, 1 cannonball, 2 magicbolt
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Point2D.Float getPos() {
        return pos;
    }

    public void setPos(Point2D.Float pos) {
        this.pos = pos;
    }

    public int getId() {
        return id;
    }

    public int getProjectileType() {
        return projectileType;
    }

    public void move() {
        pos.x += xSpeed;
        pos.y += ySpeed;
    }

    public int getDamage() {
        return damage;
    }

}
