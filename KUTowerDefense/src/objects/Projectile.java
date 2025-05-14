package objects;

import java.awt.*;
import java.awt.geom.Point2D;

public class Projectile {
    private float x, y, xSpeed, ySpeed;
    private int id, damage, projectileType;
    private boolean active = true;
    private boolean exploding = false;

    private int animationFrame = 0;
    private int explosionFrame = 0;

    private long lastFrameTime = System.nanoTime();
    private long animationDelay = 100_000_000;

    public Projectile(float x, float y, float xSpeed, float ySpeed, int id, int damage, int projectileType) {
        this.x = x;
        this.y = y;
        this.xSpeed = xSpeed;
        this.ySpeed = ySpeed;
        this.id = id;
        this.damage = damage;
        this.projectileType = projectileType;
    }

    public void move() {
        x += xSpeed;
        y += ySpeed;
    }

    public void incrementAnimationFrame() {
        long currentTime = System.nanoTime();
        if (currentTime - lastFrameTime >= animationDelay) {
            animationFrame = (animationFrame + 1) % 8;
            lastFrameTime = currentTime;
        }
    }

    public void incrementExplosionFrame() {
        long currentTime = System.nanoTime();
        if (currentTime - lastFrameTime >= animationDelay) {
            explosionFrame++;
            lastFrameTime = currentTime;
            if (explosionFrame >= 8) {
                active = false;
            }
        }
    }

    public Point getPos() {
        return new Point((int) x, (int) y);
    }

    public int getProjectileType() {
        return projectileType;
    }

    public int getDamage() {
        return damage;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isExploding() {
        return exploding;
    }

    public int getAnimationFrame() {
        return animationFrame;
    }

    public int getExplosionFrame() {
        return explosionFrame;
    }

    public void setExploding(boolean exploding) {
        this.exploding = exploding;
        this.explosionFrame = 0;
        this.lastFrameTime = System.nanoTime();
    }

    public void setActive(boolean active) {
        this.active = active;
    }


}
