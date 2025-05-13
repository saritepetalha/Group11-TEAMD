package managers;

import constants.Constants;
import enemies.Enemy;
import helpMethods.LoadSave;
import objects.Projectile;
import objects.Tower;
import scenes.Playing;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import static constants.Constants.Towers.*;
import static constants.Constants.Projectiles.*;

public class ProjectileManager {
    private Playing playing;
    private ArrayList<Projectile> projectiles = new ArrayList<>();
    private BufferedImage[] proj_imgs;
    private int projID = 0;

    public ProjectileManager(Playing playing) {
        this.playing = playing;
        importImages();

    }

    private void importImages() {
        proj_imgs = new BufferedImage[3];
        for (int i = 0; i < 3; i++) {
            proj_imgs[i] = LoadSave.getTowerMaterial(i, 24, 24);
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
        int xDist = (int) Math.abs(towerCenterX - enemyCenterX);
        int yDist = (int) Math.abs(towerCenterY - enemyCenterY);
        int totalDist = xDist + yDist;

        // if distances are too small, avoid division by zero
        if (totalDist < 1) totalDist = 1;

        // calculate speed ratios based on distance components
        float xRatio = (float) xDist / totalDist;
        float yRatio = (float) yDist / totalDist;

        // calculate actual speed components
        float xSpeed = xRatio * Constants.Projectiles.getSpeed(projType);
        float ySpeed = yRatio * Constants.Projectiles.getSpeed(projType);

        // adjust direction based on relative positions
        if (towerCenterX > enemyCenterX) {
            xSpeed *= -1;
        }

        if (towerCenterY > enemyCenterY) {
            ySpeed *= -1;
        }

        projectiles.add(new Projectile(towerCenterX, towerCenterY, xSpeed, ySpeed, projID++, tower.getDamage(), projType));
    }

    public void update() {
        for (Projectile currentProjectile : projectiles) {
            if (currentProjectile.isActive()) {
                currentProjectile.move();
                if (isEnemyShot(currentProjectile)) {
                    currentProjectile.setActive(false);
                } else {
                    // nothing
                }
            }
        }
    }

    private boolean isEnemyShot(Projectile projectile) {
        for (Enemy enemy : playing.getEnemyManager().getEnemies()) {
            if (enemy.isAlive()) {
                // check if the projectile hits the enemy's boundary
                if (enemy.getBounds().contains(projectile.getPos())) {
                    enemy.hurt(projectile.getDamage());

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
        for (Projectile currentProjectile : projectiles) {
            if (currentProjectile.isActive()) {
                g.drawImage(proj_imgs[currentProjectile.getProjectileType()],
                        (int) currentProjectile.getPos().x,
                        (int) currentProjectile.getPos().y, null);
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
