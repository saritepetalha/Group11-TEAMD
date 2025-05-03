package managers;

import constants.GameDimensions;
import enemies.Enemy;
import helpMethods.LoadSave;
import objects.ArcherTower;
import objects.ArtilleryTower;
import objects.MageTower;
import objects.Tower;
import scenes.Playing;
import ui_p.DeadTree;
import helpMethods.Utils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

//import static constants.TowerConstants.Towers.*;

public class TowerManager {
    private Playing playing;
    private BufferedImage[] towerImages;
    private Tower tower;
    private ArrayList<Tower> towers = new ArrayList<>();

    public TowerManager(Playing playing) {

        this.playing = playing;
        loadTowerImages();
    }


    private void loadTowerImages() {

        BufferedImage tilesetImage = LoadSave.getSpriteAtlas();
        towerImages = new BufferedImage[3];
        towerImages[0] = tilesetImage.getSubimage(5, 0 * 64, 64, 64);
        towerImages[1] = tilesetImage.getSubimage(5, 1 * 64, 64, 64);
        towerImages[2] = tilesetImage.getSubimage(5, 2 * 64, 64, 64);
    }

    public void update() {
        attackEnemyIfInRange();
    }


    public void update(float speedMultiplier) {
        attackEnemyIfInRange(speedMultiplier);
    }

    private void attackEnemyIfInRange() {
        attackEnemyIfInRange(1.0f);
    }

    private void attackEnemyIfInRange(float speedMultiplier) {

        for (Tower tower : towers) {
            tower.update();
            attackEnemyIfInRange(tower);
        }
    }

    private void attackEnemyIfInRange(Tower tower) {
        for (Enemy enemy : playing.getEnemyManager().getEnemies()) {
            if (enemy.isAlive()) {
                if (isEnemyInRange(tower, enemy)) {
                    if (tower.isCooldownOver()){
                        playing.shootEnemy(tower, enemy);
                        tower.resetCooldown();
                    }
                } else {
                    // PASS
                }
            }
        }
    }

    private boolean isEnemyInRange(Tower tower, Enemy enemy) {
        int range = Utils.GetHypo(tower.getX(), tower.getY(), enemy.getX(), enemy.getY());
        return range < tower.getRange();
    }

    public BufferedImage[] getTowerImages() {
        return towerImages;
    }

    public void draw(Graphics g) {

    }

    public List<DeadTree> findDeadTrees(int[][] level){

        List<DeadTree> trees = new ArrayList<>();
        for (int row = 0; row < level.length; row++) {
            for (int col = 0; col < level[row].length; col++) {
                if (level[row][col] == 15) {
                    int x = col * GameDimensions.TILE_DISPLAY_SIZE;
                    int y = row * GameDimensions.TILE_DISPLAY_SIZE;
                    trees.add(new DeadTree(x, y));
                }
            }
        }
        return trees;
    }

    public void buildArcherTower(int x, int y) {
        towers.add(new ArcherTower(x, y));
    }

    public void buildMageTower(int x, int y) {
        towers.add(new MageTower(x, y));
    }

    public void buildArtilerryTower(int x, int y) {
        towers.add(new ArtilleryTower(x, y));
    }

    public ArrayList<Tower> getTowers() {return towers;}


}


