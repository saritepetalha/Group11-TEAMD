package objects;

import enemies.Enemy;
import java.awt.*;

public class Projectile {
    private float x, y, xSpeed, ySpeed;
    private int id, damage, projectileType;
    private boolean active = true;
    private boolean exploding = false;
    private boolean hit = false;
    private boolean willMiss = false; // Flag for arrows that will miss in windy weather
    private long hitTime = 0;
    private static final long HIT_DISPLAY_TIME = 50_000_000; // 50ms in nanoseconds
    private static final long TRACKING_HIT_DISPLAY_TIME = 16_000_000; // 16ms for tracking projectiles (1 frame at 60fps)
    private float rotationAngle; // Stores the rotation angle in degrees
    private float projectileSpeed; // Store the original speed for tracking calculations

    // Target tracking fields
    private Enemy targetEnemy; // Reference to the target enemy
    private boolean isTracking = true; // Whether this projectile should track its target

    private int animationFrame = 0;
    private int explosionFrame = 0;
    private int level = 1;

    private long lastFrameTime = System.nanoTime();
    private long animationDelay = 100_000_000;

    public Projectile(float x, float y, float xSpeed, float ySpeed, int id, int damage, int projectileType, int level) {
        this.x = x;
        this.y = y;
        this.xSpeed = xSpeed;
        this.ySpeed = ySpeed;
        this.id = id;
        this.damage = damage;
        this.projectileType = projectileType;
        this.level = level;
        this.rotationAngle = 0; // Default angle
        this.projectileSpeed = (float) Math.sqrt(xSpeed * xSpeed + ySpeed * ySpeed); // Calculate speed magnitude
        this.targetEnemy = null; // Default no target
        this.isTracking = false; // Default no tracking
    }

    public Projectile(float x, float y, float xSpeed, float ySpeed, int id, int damage, int projectileType, int level, float rotationAngle) {
        this(x, y, xSpeed, ySpeed, id, damage, projectileType, level);
        this.rotationAngle = rotationAngle;
    }

    // New constructor with target tracking
    public Projectile(float x, float y, float xSpeed, float ySpeed, int id, int damage, int projectileType, int level, float rotationAngle, Enemy targetEnemy) {
        this(x, y, xSpeed, ySpeed, id, damage, projectileType, level, rotationAngle);
        this.targetEnemy = targetEnemy;
        this.isTracking = (targetEnemy != null); // Enable tracking if target is provided
    }

    public void move() {
        move(1.0f);
    }

    public void move(float gameSpeedMultiplier) {
        if (isTracking && targetEnemy != null && targetEnemy.isAlive()) {
            // Tracking movement: continuously adjust direction toward target
            moveTowardsTarget(gameSpeedMultiplier);
        } else {
            // Original straight-line movement
            x += xSpeed * gameSpeedMultiplier;
            y += ySpeed * gameSpeedMultiplier;
        }
    }

    private void moveTowardsTarget(float gameSpeedMultiplier) {
        // Get target position
        float targetX = targetEnemy.getSpriteCenterX();
        float targetY = targetEnemy.getSpriteCenterY();
        
        // Calculate direction to target
        float dx = targetX - x;
        float dy = targetY - y;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        
        // If we're very close to the target, we've essentially hit it
        if (distance < constants.Constants.Projectiles.DIRECT_HIT_DISTANCE) {
            // Move directly to target and mark as hit immediately
            x = targetX;
            y = targetY;
            setHit(); // Mark as hit to trigger cleanup
            return;
        }
        
        // Normalize direction and apply speed
        float dirX = dx / distance;
        float dirY = dy / distance;
        
        // Update velocity to point toward target
        this.xSpeed = dirX * projectileSpeed;
        this.ySpeed = dirY * projectileSpeed;
        
        // Update rotation angle for visual tracking
        this.rotationAngle = (float) Math.toDegrees(Math.atan2(dy, dx));
        if (this.rotationAngle < 0) this.rotationAngle += 360;
        
        // Move projectile
        x += xSpeed * gameSpeedMultiplier;
        y += ySpeed * gameSpeedMultiplier;
    }

    public void setHit() {
        if (!hit) {
            hit = true;
            hitTime = System.nanoTime();
        }
    }

    public void update() {
        if (hit) {
            // Use shorter display time for tracking projectiles to reduce "sticking" effect
            long displayTime = isTracking ? TRACKING_HIT_DISPLAY_TIME : HIT_DISPLAY_TIME;
            if (System.nanoTime() - hitTime > displayTime) {
                active = false;
            }
        }
        
        // If target enemy is dead, disable tracking and continue in straight line
        if (isTracking && (targetEnemy == null || !targetEnemy.isAlive())) {
            isTracking = false;
            targetEnemy = null;
        }
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

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
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

    public boolean isHit() {
        return hit;
    }

    public int getLevel() {
        return level;
    }

    public float getRotationAngle() {
        return rotationAngle;
    }

    public int getRotationFrameIndex() {
        int frameIndex = Math.round(rotationAngle / 5.0f) % 72;
        return frameIndex;
    }

    public void setWillMiss(boolean willMiss) {
        this.willMiss = willMiss;
    }

    public boolean willMiss() {
        return willMiss;
    }

    // Getter for target enemy
    public Enemy getTargetEnemy() {
        return targetEnemy;
    }

    // Check if projectile is tracking
    public boolean isTracking() {
        return isTracking;
    }

    // Disable tracking (for special cases)
    public void disableTracking() {
        this.isTracking = false;
        this.targetEnemy = null;
    }
    
    // Get projectile speed magnitude
    public float getProjectileSpeed() {
        return projectileSpeed;
    }
    
    // Get current X speed
    public float getXSpeed() {
        return xSpeed;
    }
    
    // Get current Y speed  
    public float getYSpeed() {
        return ySpeed;
    }
    
    // Set new speed values (for miss behavior)
    public void setSpeed(float newXSpeed, float newYSpeed) {
        this.xSpeed = newXSpeed;
        this.ySpeed = newYSpeed;
        // Update speed magnitude
        this.projectileSpeed = (float) Math.sqrt(newXSpeed * newXSpeed + newYSpeed * newYSpeed);
    }
}
