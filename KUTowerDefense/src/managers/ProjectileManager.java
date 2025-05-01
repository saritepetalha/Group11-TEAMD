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

        int xDist = (int) Math.abs(tower.getX() - enemy.getX());
        int yDist = (int) Math.abs(tower.getY() - enemy.getY());
        int totalDist = xDist + yDist;

        float xRatio = (float) xDist / totalDist;
        float yRatio = 1 - xRatio;

        float xSpeed = xRatio * Constants.Projectiles.getSpeed(projType);
        float ySpeed = yRatio * Constants.Projectiles.getSpeed(projType);

        if (tower.getX() > enemy.getX()) {
            xSpeed *= -1;
        }

        if (tower.getY() > enemy.getY()) {
            ySpeed *= -1;
        }
        projectiles.add(new Projectile(tower.getX() + 20, tower.getY() + 20, xSpeed, ySpeed, projID++, tower.getDamage(), projType));
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
            if (enemy.getBounds().contains(projectile.getPos())){
                enemy.hurt(projectile.getDamage());
                return true;
            }
        }
        return false;
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
}
