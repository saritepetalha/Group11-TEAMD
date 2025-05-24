package managers;

import constants.Constants;
import enemies.Enemy;
import helpMethods.LoadSave;
import objects.Projectile;
import objects.Tower;
import scenes.Playing;
import helpMethods.RotSprite;
import helpMethods.RotatedProjectileFrameGenerator;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import static constants.Constants.Towers.*;
import static constants.Constants.Projectiles.*;

public class ProjectileManager {
    private Playing playing;
    private ArrayList<Projectile> projectiles = new ArrayList<>();
    private BufferedImage[] proj_imgs; // Index 0: ARROW, 1: CANNONBALL (L1), 2: MAGICBOLT (L1)
    private BufferedImage[] fireball_imgs; // For L2 CANNONBALL animation
    private BufferedImage[][] rotatedFireballFrames; // For rotated L2 CANNONBALL animation [animFrame][rotFrame]
    private BufferedImage[] explosion_imgs;
    private BufferedImage[] arrowFrames; // Array for rotated arrow sprites
    private int projID = 0;

    public ProjectileManager(Playing playing) {
        this.playing = playing;
        importImages();
        loadArrowFrames();
    }

    private void importImages() {
        proj_imgs = new BufferedImage[3];
        for (int i = 0; i < 3; i++) {
            proj_imgs[i] = LoadSave.getTowerMaterial(i, 24, 24);
        }

        fireball_imgs = LoadSave.getFireballAnimation();
        explosion_imgs = LoadSave.getExplosionAnimation();

        // Load rotated fireball frames
        loadRotatedFireballFrames();
    }

    private void loadArrowFrames() {
        final int frameCount = 72; // Same as original: 72 frames with 5.0 degree steps

        // Try to load pre-generated frames
        arrowFrames = LoadSave.loadArrowFrames(frameCount);

        if (arrowFrames == null) {
            System.out.println("Pre-generated arrow frames not found. Generating them automatically...");
            RotatedProjectileFrameGenerator.generateAndSaveArrowFrames();

            // Try loading again after generation
            arrowFrames = LoadSave.loadArrowFrames(frameCount);

            if (arrowFrames != null) {
                System.out.println("Arrow frames generated and loaded successfully.");
            } else {
                System.err.println("Failed to generate or load arrow frames!");
            }
        } else {
            System.out.println("Loaded " + frameCount + " pre-generated arrow frames.");
        }
    }

    private void loadRotatedFireballFrames() {
        // Try to load pre-generated rotated fireball frames
        rotatedFireballFrames = LoadSave.loadFireballFrames();

        if (rotatedFireballFrames == null) {
            System.out.println("Pre-generated rotated fireball frames not found. Generating them automatically...");
            RotatedProjectileFrameGenerator.generateAndSaveFireballFrames();

            // Try loading again after generation
            rotatedFireballFrames = LoadSave.loadFireballFrames();

            if (rotatedFireballFrames != null) {
                System.out.println("Rotated fireball frames generated and loaded successfully.");
            } else {
                System.err.println("Failed to generate or load rotated fireball frames!");
            }
        } else {
            System.out.println("Loaded pre-generated rotated fireball frames (5 animation frames × 36 rotation frames).");
        }
    }

    public void newProjectile(Tower tower, Enemy enemy) {
        int projType = getProjectileType(tower);

        // get the exact center of the enemy's sprite using the dedicated methods
        float enemyCenterX = enemy.getSpriteCenterX();
        float enemyCenterY = enemy.getSpriteCenterY();

        // tower center (32 is half of a tile's size - 64 pixels)
        int towerCenterX = tower.getX() + 32;
        int towerCenterY = tower.getY() + 32;

        // calculate distance and direction components
        float xDiff = enemyCenterX - towerCenterX;
        float yDiff = enemyCenterY - towerCenterY;
        float distance = (float) Math.sqrt(xDiff * xDiff + yDiff * yDiff);

        // Calculate angle for sprite rotation (in degrees)
        float angle = (float) Math.toDegrees(Math.atan2(yDiff, xDiff));
        // Normalize angle to 0-360 range
        if (angle < 0) angle += 360;

        // Calculate time to reach target based on projectile speed
        float projectileSpeed = Constants.Projectiles.getSpeed(projType);
        float timeToTarget = distance / projectileSpeed;

        // Get enemy's movement direction and speed
        float enemyDirX = enemy.getDirX();
        float enemyDirY = enemy.getDirY();
        float enemySpeed = enemy.getSpeed();

        // Get current game speed multiplier
        float gameSpeedMultiplier = playing.getGameSpeedMultiplier();

        // Predict enemy position after timeToTarget, considering both speed and direction
        // Adjust prediction based on game speed
        float predictedX = enemyCenterX + (enemySpeed * timeToTarget * enemyDirX * gameSpeedMultiplier);
        float predictedY = enemyCenterY + (enemySpeed * timeToTarget * enemyDirY * gameSpeedMultiplier);

        // Add a small random offset to make shots more natural
        float randomOffset = (float) (Math.random() * 0.1 - 0.05); // ±5% random offset
        float adjustedSpeed = projectileSpeed * (1 + randomOffset);

        // Recalculate direction to predicted position
        xDiff = predictedX - towerCenterX;
        yDiff = predictedY - towerCenterY;
        distance = (float) Math.sqrt(xDiff * xDiff + yDiff * yDiff);

        // Recalculate angle based on predicted position
        angle = (float) Math.toDegrees(Math.atan2(yDiff, xDiff));
        if (angle < 0) angle += 360;

        // Normalize direction and apply speed
        float xSpeed = (xDiff / distance) * adjustedSpeed;
        float ySpeed = (yDiff / distance) * adjustedSpeed;

        // Pass tower level and rotation angle to projectile
        projectiles.add(new Projectile(towerCenterX, towerCenterY, xSpeed, ySpeed, projID++, tower.getDamage(), projType, tower.getLevel(), angle));
    }

    public void update() {
        for (Projectile currentProjectile : projectiles) {
            if (currentProjectile.isActive()) {
                if (currentProjectile.isExploding()) {
                    currentProjectile.incrementExplosionFrame();
                } else {
                    currentProjectile.move();
                    if (currentProjectile.getProjectileType() == Constants.Projectiles.CANNONBALL) {
                        currentProjectile.incrementAnimationFrame();
                    }
                    if (!currentProjectile.isHit() && isEnemyShot(currentProjectile)) {
                        if (currentProjectile.getProjectileType() == Constants.Projectiles.CANNONBALL) {
                            currentProjectile.setExploding(true);
                        } else {
                            currentProjectile.setHit();
                        }
                    }
                }
                currentProjectile.update();
            }
        }
    }

    public void update(float gameSpeedMultiplier) {
        for (Projectile currentProjectile : projectiles) {
            if (currentProjectile.isActive()) {
                if (currentProjectile.isExploding()) {
                    currentProjectile.incrementExplosionFrame();
                } else {
                    currentProjectile.move(gameSpeedMultiplier);
                    if (currentProjectile.getProjectileType() == Constants.Projectiles.CANNONBALL) {
                        currentProjectile.incrementAnimationFrame();
                    }
                    if (!currentProjectile.isHit() && isEnemyShot(currentProjectile)) {
                        if (currentProjectile.getProjectileType() == Constants.Projectiles.CANNONBALL) {
                            currentProjectile.setExploding(true);
                        } else {
                            currentProjectile.setHit();
                        }
                    }
                }
                currentProjectile.update();
            }
        }
    }

    private boolean isEnemyShot(Projectile projectile) {
        for (Enemy enemy : playing.getEnemyManager().getEnemies()) {
            if (enemy.isAlive()) {
                // Get enemy's actual sprite bounds for visual hit detection
                Rectangle enemyBounds = enemy.getBounds();

                // Calculate the actual sprite center and size based on enemy type
                float centerX = enemy.getSpriteCenterX();
                float centerY = enemy.getSpriteCenterY();

                // Create a hit area that scales with enemy size
                int hitSize;
                switch (enemy.getSize()) {
                    case SMALL:
                        hitSize = 16;  // Smaller hit area for small enemies
                        break;
                    case MEDIUM:
                        hitSize = 24;  // Medium hit area for medium enemies
                        break;
                    case LARGE:
                        hitSize = 32;  // Larger hit area for large enemies
                        break;
                    default:
                        hitSize = 20;
                }

                Rectangle hitArea = new Rectangle(
                        (int)centerX - hitSize/2,
                        (int)centerY - hitSize/2,
                        hitSize,
                        hitSize
                );

                // Check if the projectile hits the enemy's sprite center
                if (hitArea.contains(projectile.getPos())) {
                    enemy.hurt(projectile.getDamage());
                    playing.addTotalDamage(projectile.getDamage());

                    if (!enemy.isAlive()) {
                        playing.incrementEnemyDefeated();
                    }

                    // Mage slow effect
                    if (projectile.getProjectileType() == Constants.Projectiles.MAGICBOLT && projectile.getLevel() == 2) {
                        // Apply a default slow from projectiles, e.g., 50% slow for 2 seconds (120 ticks)
                        enemy.applySlow(0.5f, 120);
                    }

                    // Mage teleport effect - "Back to step 1" mechanic
                    if (projectile.getProjectileType() == Constants.Projectiles.MAGICBOLT) {
                        if (Math.random() < 0.03) {
                            // Play a teleport sound or effect here if available
                            System.out.println("TELEPORT TRIGGERED for enemy " + enemy.getId());

                            // Teleport the enemy back to the start of the path
                            playing.getEnemyManager().teleportEnemyToStart(enemy);
                        }
                    }

                    // handle AOE damage for CANNONBALL projectile type
                    if (projectile.getProjectileType() == Constants.Projectiles.CANNONBALL) {
                        handleAOEDamage(projectile, enemy);
                    }

                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Handles AOE (Area of Effect) damage for artillery towers
     * @param projectile The projectile that hit
     * @param targetEnemy The enemy that was directly hit
     */
    private void handleAOEDamage(Projectile projectile, Enemy targetEnemy) {
        // get AOE radius from the constants - artillery has AOE damage
        float aoeDamageRadius = Constants.Towers.getAOERadius(Constants.Towers.ARTILLERY);

        // calculate center position of the hit enemy using sprite center methods
        float centerX = targetEnemy.getSpriteCenterX();
        float centerY = targetEnemy.getSpriteCenterY();

        // check all enemies for AOE damage
        for (Enemy enemy : playing.getEnemyManager().getEnemies()) {
            // skip the already damaged target enemy and dead enemies
            if (enemy == targetEnemy || !enemy.isAlive()) {
                continue;
            }

            // get this enemy's sprite center
            float enemyX = enemy.getSpriteCenterX();
            float enemyY = enemy.getSpriteCenterY();

            // calculate distance between this enemy and the hit enemy
            float distX = enemyX - centerX;
            float distY = enemyY - centerY;
            float distance = (float) Math.sqrt(distX * distX + distY * distY);

            // if within AOE radius, apply half damage
            if (distance <= aoeDamageRadius) {
                enemy.hurt(projectile.getDamage() / 2);
            }
        }
    }

    public void draw(Graphics g) {
        // Create a copy of the projectiles list to avoid concurrent modification
        ArrayList<Projectile> projectilesCopy = new ArrayList<>(projectiles);

        for (Projectile currentProjectile : projectilesCopy) {
            drawProjectile(currentProjectile, g);
        }
    }

    private void drawProjectile(Projectile currentProjectile, Graphics g) {
        if (currentProjectile.isActive()) {
            if (currentProjectile.isExploding()) {
                int frame = currentProjectile.getExplosionFrame();
                if (frame >= 0 && frame < explosion_imgs.length) {
                    g.drawImage(explosion_imgs[frame],
                            (int) currentProjectile.getPos().x - explosion_imgs[frame].getWidth() / 2,
                            (int) currentProjectile.getPos().y - explosion_imgs[frame].getHeight() / 2,
                            null);
                }
            } else if (currentProjectile.getProjectileType() == Constants.Projectiles.CANNONBALL && currentProjectile.getLevel() == 2) {
                // Draw Lvl 2 Cannonball animation (rotated fireball)
                int animFrame = currentProjectile.getAnimationFrame();

                if (rotatedFireballFrames != null && animFrame >= 0 && animFrame < rotatedFireballFrames.length) {
                    // Calculate rotation frame based on projectile angle (10-degree intervals)
                    float angle = currentProjectile.getRotationAngle();
                    int rotationFrame = Math.round(angle / 10.0f) % 36; // 36 frames for 360°

                    BufferedImage fireballImg = rotatedFireballFrames[animFrame][rotationFrame];
                    if (fireballImg != null) {
                        g.drawImage(fireballImg,
                                (int) currentProjectile.getPos().x - fireballImg.getWidth() / 2,
                                (int) currentProjectile.getPos().y - fireballImg.getHeight() / 2,
                                null);
                    }
                } else if (fireball_imgs != null && animFrame >= 0 && animFrame < fireball_imgs.length) {
                    // Fallback to non-rotated fireball if rotated frames are not available
                    g.drawImage(fireball_imgs[animFrame],
                            (int) currentProjectile.getPos().x - fireball_imgs[animFrame].getWidth() / 2,
                            (int) currentProjectile.getPos().y - fireball_imgs[animFrame].getHeight() / 2,
                            null);
                }
            } else if (currentProjectile.getProjectileType() == Constants.Projectiles.ARROW && arrowFrames != null) {
                // Use rotated arrow sprites based on direction (already centered)
                int frameIndex = currentProjectile.getRotationFrameIndex();
                if (frameIndex >= 0 && frameIndex < arrowFrames.length) {
                    BufferedImage arrowImg = arrowFrames[frameIndex];
                    g.drawImage(arrowImg,
                            (int) currentProjectile.getPos().x - 12,
                            (int) currentProjectile.getPos().y - 12,
                            null);
                }
            } else {
                // Default drawing for L1 projectiles (including L1 Cannonball, L1 Magicbolt, L1 Arrow)
                // Uses proj_imgs and centers the image
                BufferedImage imgToDraw = proj_imgs[currentProjectile.getProjectileType()];
                if (imgToDraw != null) {
                    g.drawImage(imgToDraw,
                            (int) currentProjectile.getPos().x - imgToDraw.getWidth() / 2,
                            (int) currentProjectile.getPos().y - imgToDraw.getHeight() / 2,
                            null);
                }
            }
        }
    }

    private int getProjectileType(Tower tower) {
        switch (tower.getType()) {
            case ARCHER:
                return ARROW;
            case ARTILLERY:
                return CANNONBALL;
            case MAGE:
                return MAGICBOLT;
        }
        return 0;
    }

    public void clearProjectiles() {
        projectiles.clear();
    }
}
