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
    private BufferedImage[] wizardFrames;
    private int projID = 0;

    public ProjectileManager(Playing playing) {
        this.playing = playing;
        importImages();
        loadArrowFrames();
        loadWizardFrames();
    }

    private void importImages() {
        proj_imgs = new BufferedImage[4];
        for (int i = 0; i < 4; i++) {
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

    private void loadWizardFrames() {
        final int frameCount = 72;
        wizardFrames = LoadSave.loadWizardFrames(frameCount);
        if (wizardFrames == null) {
            RotatedProjectileFrameGenerator.generateAndSaveWizardFrames();
            wizardFrames = LoadSave.loadWizardFrames(frameCount);
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
            damage = tower.getConditionBasedDamage();
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
                angle,
                enemy  // Pass the target enemy for tracking
        );

        // Handle windy weather miss chance for arrows
        if (playing.getWeatherManager().isWindy() && projType == Constants.Projectiles.ARROW) {
            if (Math.random() < 0.3) { // 30% miss chance in windy weather
                projectile.setWillMiss(true);
                // Disable tracking for missing projectiles so they fly off-target
                projectile.disableTracking();

                // Add some randomness to trajectory for missing arrows
                float missOffset = 60f + (float)(Math.random() * 40f); // 60-100 pixel offset
                float missAngle = (float)(Math.random() * 2 * Math.PI); // Random direction
                float originalSpeed = projectile.getProjectileSpeed();

                // Apply miss offset to speed
                projectile.setSpeed(
                        projectile.getXSpeed() + (float)Math.cos(missAngle) * missOffset * 0.1f,
                        projectile.getYSpeed() + (float)Math.sin(missAngle) * missOffset * 0.1f
                );
            }
        }

        projectiles.add(projectile);

        // Play appropriate sound effect based on projectile type
        switch (projType) {
            case Constants.Projectiles.ARROW:
                managers.AudioManager.getInstance().playArrowShotSound();
                break;
            case Constants.Projectiles.CANNONBALL:
                managers.AudioManager.getInstance().playBombShotSound();
                break;
            case Constants.Projectiles.MAGICBOLT:
                managers.AudioManager.getInstance().playSpellShotSound();
                break;
            case Constants.Projectiles.WIZARD_BOLT:
                managers.AudioManager.getInstance().playSpellShotSound(); // Wizard warriors use same sound as mage towers
                break;
        }
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

                // For tracking projectiles that are very close to dead targets, mark as hit
                if (projectile.isTracking() && projectile.getTargetEnemy() != null) {
                    Enemy target = projectile.getTargetEnemy();
                    if (!target.isAlive()) {
                        // Target died, stop tracking and mark as hit to clean up
                        projectile.disableTracking();
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
        // For tracking projectiles, check primarily against their target
        if (projectile.isTracking() && projectile.getTargetEnemy() != null) {
            Enemy targetEnemy = projectile.getTargetEnemy();
            if (targetEnemy.isAlive() && checkProjectileHit(projectile, targetEnemy)) {
                // Hit the target enemy - use GRASP Information Expert pattern
                applyProjectileDamage(projectile, targetEnemy);
                playing.addTotalDamage(projectile.getDamage());

                // Handle enemy death
                if (!targetEnemy.isAlive()) {
                    if (playing.getController() != null && playing.getController().getModel() != null) {
                        // Use new method to track death location for confetti
                        playing.getController().getModel().enemyDiedAt((int)targetEnemy.getX(), (int)targetEnemy.getY());
                    } else {
                        playing.incrementEnemyDefeated();
                    }
                }

                // Apply special effects
                applySpecialEffects(projectile, targetEnemy);
                return true;
            }
            // If target is dead but projectile is still tracking, disable tracking
            else if (!targetEnemy.isAlive()) {
                projectile.disableTracking();
            }
        }

        // For non-tracking projectiles or as fallback, check all enemies
        for (Enemy enemy : playing.getEnemyManager().getEnemies()) {
            if (!enemy.isAlive()) continue;

            if (checkProjectileHit(projectile, enemy)) {
                // Apply damage using GRASP Information Expert pattern
                applyProjectileDamage(projectile, enemy);
                playing.addTotalDamage(projectile.getDamage());

                // Handle enemy death
                if (!enemy.isAlive()) {
                    if (playing.getController() != null && playing.getController().getModel() != null) {
                        // Use new method to track death location for confetti
                        playing.getController().getModel().enemyDiedAt((int)enemy.getX(), (int)enemy.getY());
                    } else {
                        playing.incrementEnemyDefeated();
                    }
                }

                // Apply special effects
                applySpecialEffects(projectile, enemy);

                return true;
            }
        }
        return false;
    }

    private boolean checkProjectileHit(Projectile projectile, Enemy enemy) {
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

        // For tracking projectiles, use a more generous hit detection
        if (projectile.isTracking()) {
            // Calculate distance between projectile and enemy center
            float dx = projectile.getX() - centerX;
            float dy = projectile.getY() - centerY;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);

            // Hit if within a reasonable distance (slightly larger than hitbox)
            return distance <= (hitSize / 2.0f + Constants.Projectiles.TRACKING_HIT_DISTANCE);
        } else {
            // Original hit detection for non-tracking projectiles
            return hitArea.contains(projectile.getPos());
        }
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
        // Mage slow effect - 20% slow for 4 seconds (Level 2 mage towers only)
        if (projectile.getProjectileType() == MAGICBOLT && projectile.getLevel() == 2) {
            enemy.applySlow(0.8f, 240); // 20% slow (80% speed) for 4 seconds (240 ticks at 60 FPS)
        }

        // Mage teleport effect
        if (projectile.getProjectileType() == MAGICBOLT && Math.random() < 0.03) {
            enemy.applyTeleportEffect();
            playing.getEnemyManager().teleportEnemyToStart(enemy);
        }

        // Wizard warrior effects - slightly different from mage tower
        if (projectile.getProjectileType() == Constants.Projectiles.WIZARD_BOLT) {
            // Wizard warriors have a smaller chance for teleport effect but it's always available
            if (Math.random() < 0.02) {
                enemy.applyTeleportEffect();
                playing.getEnemyManager().teleportEnemyToStart(enemy);
            }
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
        } else if (projectile.getProjectileType() == MAGICBOLT && projectile.getLevel() == 2) {
            drawUpgradedMagicBolt(projectile, g);
        } else if (projectile.getProjectileType() == ARROW && arrowFrames != null) {
            drawArrow(projectile, g);
        } else if (projectile.getProjectileType() == WIZARD_BOLT && wizardFrames != null) {
            drawWizard(projectile, g);
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

    private void drawWizard(Projectile projectile, Graphics g) {
        int frameIndex = projectile.getRotationFrameIndex();
        if (frameIndex >= 0 && frameIndex < wizardFrames.length) {
            g.drawImage(wizardFrames[frameIndex],
                    (int)projectile.getPos().x - 12,
                    (int)projectile.getPos().y - 12,
                    null);
        }
    }

    private void drawUpgradedMagicBolt(Projectile projectile, Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int centerX = (int)projectile.getPos().x;
        int centerY = (int)projectile.getPos().y;

        // Create a completely new purple magic bolt instead of layering on the blue one

        // Outer purple glow
        g2d.setColor(new Color(138, 43, 226, 80)); // Purple with transparency
        g2d.fillOval(centerX - 10, centerY - 10, 20, 20);

        // Middle purple core
        g2d.setColor(new Color(148, 0, 211, 160)); // Darker purple, more opaque
        g2d.fillOval(centerX - 6, centerY - 6, 12, 12);

        // Inner bright purple core
        g2d.setColor(new Color(186, 85, 211, 220)); // Medium purple
        g2d.fillOval(centerX - 4, centerY - 4, 8, 8);

        // Central bright core
        g2d.setColor(new Color(255, 255, 255, 200)); // Bright white center
        g2d.fillOval(centerX - 2, centerY - 2, 4, 4);

        // Add trailing sparkles
        g2d.setColor(new Color(200, 162, 200, 120));
        g2d.fillOval(centerX - 1, centerY - 1, 2, 2);

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
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
            return WIZARD_BOLT;
        } else if (warrior instanceof ArcherWarrior) {
            return ARROW;
        }
        return ARROW;
    }

    public void clearProjectiles() {
        projectiles.clear();
    }

    private boolean isProjectileOffScreen(Projectile projectile) {
        Point pos = projectile.getPos();
        return pos.x < -50 || pos.x > 1024 + 50 || pos.y < -50 || pos.y > 576 + 50;
    }

    // Method to access projectiles list (used by model abstraction)
    public ArrayList<Projectile> getProjectiles() {
        return projectiles;
    }

    /**
     * GRASP Information Expert: Apply projectile damage using the Enemy's damage calculation
     * Determines the appropriate damage type based on projectile type
     */
    private void applyProjectileDamage(Projectile projectile, Enemy enemy) {
        int damage = projectile.getDamage();
        enemies.Enemy.DamageType damageType = getDamageTypeFromProjectile(projectile);

        // Let the enemy (Information Expert) calculate and apply the damage
        enemy.takeDamage(damage, damageType);
    }

    /**
     * GRASP Information Expert: Determine damage type based on projectile characteristics
     */
    private enemies.Enemy.DamageType getDamageTypeFromProjectile(Projectile projectile) {
        switch (projectile.getProjectileType()) {
            case constants.Constants.Projectiles.ARROW:
                return enemies.Enemy.DamageType.PHYSICAL;
            case constants.Constants.Projectiles.MAGICBOLT:
            case constants.Constants.Projectiles.WIZARD_BOLT:
                return enemies.Enemy.DamageType.MAGICAL;
            case constants.Constants.Projectiles.CANNONBALL:
                return enemies.Enemy.DamageType.EXPLOSIVE;
            default:
                return enemies.Enemy.DamageType.PHYSICAL; // Default fallback
        }
    }
}
