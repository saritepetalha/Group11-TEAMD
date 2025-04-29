package managers;

import dimensions.GameDimensions;
import helpMethods.LoadSave;
import objects.Tower;
import scenes.Playing;
import ui_p.DeadTree;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import static dimensions.TowerConstants.Towers.*;

public class TowerManager {
    private Playing playing;
    private BufferedImage[] towerImages;
    private Tower tower;
    private ArrayList<Tower> towers = new ArrayList<>();

    public TowerManager(Playing playing) {

        this.playing = playing;
        loadTowerImages();
        initializaTowers();
    }

    private void initializaTowers() {
        tower = new Tower(5, 0, ARCHER);
    }

    private void loadTowerImages() {

        BufferedImage tilesetImage = LoadSave.getSpriteAtlas();
        towerImages = new BufferedImage[3];
        towerImages[0] = tilesetImage.getSubimage(5, 0 * 64, 64, 64);
        towerImages[1] = tilesetImage.getSubimage(5, 1 * 64, 64, 64);
        towerImages[2] = tilesetImage.getSubimage(5, 2 * 64, 64, 64);
    }

    public void update() {

    }

    public BufferedImage[] getTowerImages() {
        return towerImages;
    }

    public void draw(Graphics g) {

        g.drawImage(towerImages[ARCHER], tower.getX(), tower.getY(), null);
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

    public void buildArcherTower() {

    }

    public void buildMageTower() {

    }

    public void buildArtilerryTower() {
    }


}


