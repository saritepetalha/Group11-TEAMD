package managers;

import helpMethods.LoadSave;
import objects.Tower;
import scenes.Playing;

import java.awt.*;
import java.awt.image.BufferedImage;
import static dimensions.TowerConstants.Towers.*;

public class TowerManager {
    private Playing playing;
    private BufferedImage[] towerImages;
    private Tower tower;

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

    public void draw(Graphics g) {

        g.drawImage(towerImages[ARCHER], tower.getX(), tower.getY(), null);
    }
}
