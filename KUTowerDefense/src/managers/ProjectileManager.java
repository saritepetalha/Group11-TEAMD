package managers;

import constants.Constants;
import enemies.Enemy;
import helpMethods.LoadSave;
import objects.Projectile;
import objects.Tower;
import scenes.Playing;
import helpMethods.RotatedProjectileFrameGenerator;
import objects.Warrior;
import objects.WizardWarrior;
import objects.ArcherWarrior;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import static constants.Constants.Towers.*;
import static constants.Constants.Projectiles.*;

public class ProjectileManager {
    private Playing playing;
    private ArrayList<Projectile> projectiles = new ArrayList<>();
    private BufferedImage[] proj_imgs;
    private BufferedImage[] fireball_imgs;
    private BufferedImage[][] rotatedFireballFrames;
    private BufferedImage[] explosion_imgs;
    private BufferedImage[] arrowFrames;
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
        loadRotatedFireballFrames();
    }

    private void loadArrowFrames() {
        final int frameCount = 72;
        arrowFrames = LoadSave.loadArrowFrames(frameCount);
        if (arrowFrames == null) {
            RotatedProjectileFrameGenerator.generateAndSaveArrowFrames();
            arrowFrames = LoadSave.loadArrowFrames(frameCount);
        }
    }

    private void loadRotatedFireballFrames() {
        rotatedFireballFrames = LoadSave.loadFireballFrames();
        if (rotatedFireballFrames == null) {
            RotatedProjectileFrameGenerator.generateAndSaveFireballFrames();
            rotatedFireballFrames = LoadSave.loadFireballFrames();
        }
    }

    public void newProjectile(Object shooter, Enemy enemy) {
        int projType;
        float projectileSpeed;
        int shooterCenterX;
        int shooterCenterY;
        int damage;
        int level;

        if (shooter instanceof Tower) {
            Tower tower = (Tower) shooter;
            projType = getProjectileType(tower);
            projectileSpeed = Constants.Projectiles.getSpeed(projType);
            shooterCenterX = tower.getX() + tower.getWidth() / 2;
            shooterCenterY = tower.getY() + tower.getHeight() / 2;
            damage = tower.getDamage();
            level = tower.getLevel();
        } else if (shooter instanceof Warrior) {
            Warrior warrior = (Warrior) shooter;
            projType = getProjectileType(warrior);
            projectileSpeed = Constants.Projectiles.getSpeed(projType);
            shooterCenterX = warrior.getX() + warrior.getWidth() / 2;
            shooterCenterY = warrior.getY() + warrior.getHeight() / 2;
            damage = warrior.getDamage();
            level = warrior.getLevel();
        } else {
            return; // Invalid shooter type
        }

        float enemyCenterX = enemy.getSpriteCenterX();
        float enemyCenterY = enemy.getSpriteCenterY();

        float dx = enemyCenterX - shooterCenterX;
        float dy = enemyCenterY - shooterCenterY;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        float angle = (float) Math.toDegrees(Math.atan2(dy, dx));
        if (angle < 0) angle += 360;

        float xSpeed = (dx / distance) * projectileSpeed;
        float ySpeed = (dy / distance) * projectileSpeed;

        Projectile projectile = new Projectile(
            shooterCenterX,
            shooterCenterY,
            xSpeed,
            ySpeed,
            projID++,
            damage,
            projType,
            level,
            angle
        );

        if (playing.getWeatherManager().isWindy() && 
            projType == Constants.Projectiles.ARROW && 
            Math.random() < 0.3) {
            projectile.setWillMiss(true);
        }

        projectiles.add(projectile);
    }

    public void update() {
        update(1.0f);
    }

    public void update(float gameSpeedMultiplier) {
        for (Projectile projectile : projectiles) {
            if (!projectile.isActive()) continue;

            if (projectile.isExploding()) {
                projectile.incrementExplosionFrame();
            } else {
                // Move projectile
                projectile.move(gameSpeedMultiplier);

                // Update animation for cannonballs
                if (projectile.getProjectileType() == CANNONBALL) {
                    projectile.incrementAnimationFrame();
                }

                // Check for hits
                if (!projectile.isHit() && !projectile.willMiss() && isEnemyShot(projectile)) {
                    if (projectile.getProjectileType() == CANNONBALL) {
                        projectile.setExploding(true);
                    } else {
                        projectile.setHit();
                    }
                }

                // Remove projectiles that are off screen
                if (isProjectileOffScreen(projectile)) {
                    projectile.setActive(false);
                }
            }
            projectile.update();
        }
    }

    private boolean isEnemyShot(Projectile projectile) {
        for (Enemy enemy : playing.getEnemyManager().getEnemies()) {
            if (!enemy.isAlive()) continue;

            // Get enemy center and hit area
            float centerX = enemy.getSpriteCenterX();
            float centerY = enemy.getSpriteCenterY();
            int hitSize = getHitSize(enemy);

            // Create hit area
            Rectangle hitArea = new Rectangle(
                (int)centerX - hitSize/2,
                (int)centerY - hitSize/2,
                hitSize,
                hitSize
            );

            // Check for hit
            if (hitArea.contains(projectile.getPos())) {
                // Apply damage
                enemy.hurt(projectile.getDamage());
                playing.addTotalDamage(projectile.getDamage());

                // Handle enemy death
                if (!enemy.isAlive()) {
                    playing.incrementEnemyDefeated();
                }

                // Apply special effects
                applySpecialEffects(projectile, enemy);

                return true;
            }
        }
        return false;
    }

    private int getHitSize(Enemy enemy) {
        switch (enemy.getSize()) {
            case SMALL: return 16;
            case MEDIUM: return 24;
            case LARGE: return 32;
            default: return 20;
        }
    }

    private void applySpecialEffects(Projectile projectile, Enemy enemy) {
        // Mage slow effect
        if (projectile.getProjectileType() == MAGICBOLT && projectile.getLevel() == 2) {
            enemy.applySlow(0.5f, 120);
        }

        // Mage teleport effect
        if (projectile.getProjectileType() == MAGICBOLT && Math.random() < 0.03) {
            enemy.applyTeleportEffect();
            playing.getEnemyManager().teleportEnemyToStart(enemy);
        }

        // Artillery AOE damage
        if (projectile.getProjectileType() == CANNONBALL) {
            handleAOEDamage(projectile, enemy);
        }
    }

    private void handleAOEDamage(Projectile projectile, Enemy targetEnemy) {
        float aoeRadius = Constants.Towers.getAOERadius(ARTILLERY);
        float centerX = targetEnemy.getSpriteCenterX();
        float centerY = targetEnemy.getSpriteCenterY();

        for (Enemy enemy : playing.getEnemyManager().getEnemies()) {
            if (enemy == targetEnemy || !enemy.isAlive()) continue;

            float dx = enemy.getSpriteCenterX() - centerX;
            float dy = enemy.getSpriteCenterY() - centerY;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);

            if (distance <= aoeRadius) {
                enemy.hurt(projectile.getDamage() / 2);
            }
        }
    }

    public void draw(Graphics g) {
        for (Projectile projectile : new ArrayList<>(projectiles)) {
            drawProjectile(projectile, g);
        }
    }

    private void drawProjectile(Projectile projectile, Graphics g) {
        if (!projectile.isActive()) return;

        if (projectile.isExploding()) {
            drawExplosion(projectile, g);
        } else if (projectile.getProjectileType() == CANNONBALL && projectile.getLevel() == 2) {
            drawFireball(projectile, g);
        } else if (projectile.getProjectileType() == ARROW && arrowFrames != null) {
            drawArrow(projectile, g);
        } else {
            drawDefaultProjectile(projectile, g);
        }
    }

    private void drawExplosion(Projectile projectile, Graphics g) {
        int frame = projectile.getExplosionFrame();
        if (frame >= 0 && frame < explosion_imgs.length) {
            g.drawImage(explosion_imgs[frame],
                (int)projectile.getPos().x - explosion_imgs[frame].getWidth() / 2,
                (int)projectile.getPos().y - explosion_imgs[frame].getHeight() / 2,
                null);
        }
    }

    private void drawFireball(Projectile projectile, Graphics g) {
        int animFrame = projectile.getAnimationFrame();
        if (rotatedFireballFrames != null && animFrame >= 0 && animFrame < rotatedFireballFrames.length) {
            float angle = projectile.getRotationAngle();
            int rotationFrame = Math.round(angle / 10.0f) % 36;
            BufferedImage fireballImg = rotatedFireballFrames[animFrame][rotationFrame];
            if (fireballImg != null) {
                g.drawImage(fireballImg,
                    (int)projectile.getPos().x - fireballImg.getWidth() / 2,
                    (int)projectile.getPos().y - fireballImg.getHeight() / 2,
                    null);
            }
        }
    }

    private void drawArrow(Projectile projectile, Graphics g) {
        int frameIndex = projectile.getRotationFrameIndex();
        if (frameIndex >= 0 && frameIndex < arrowFrames.length) {
            g.drawImage(arrowFrames[frameIndex],
                (int)projectile.getPos().x - 12,
                (int)projectile.getPos().y - 12,
                null);
        }
    }

    private void drawDefaultProjectile(Projectile projectile, Graphics g) {
        BufferedImage img = proj_imgs[projectile.getProjectileType()];
        if (img != null) {
            g.drawImage(img,
                (int)projectile.getPos().x - img.getWidth() / 2,
                (int)projectile.getPos().y - img.getHeight() / 2,
                null);
        }
    }

    private int getProjectileType(Tower tower) {
        switch (tower.getType()) {
            case ARCHER: return ARROW;
            case ARTILLERY: return CANNONBALL;
            case MAGE: return MAGICBOLT;
            default: return ARROW;
        }
    }

    private int getProjectileType(Warrior warrior) {
        if (warrior instanceof WizardWarrior) {
            return MAGICBOLT;
        } else if (warrior instanceof ArcherWarrior) {
            return ARROW;
        }
        return ARROW; // Default to ARROW if type is unknown
    }

    public void clearProjectiles() {
        projectiles.clear();
    }

    private boolean isProjectileOffScreen(Projectile projectile) {
        Point pos = projectile.getPos();
        return pos.x < -50 || pos.x > 1024 + 50 || pos.y < -50 || pos.y > 576 + 50;
    }
}
