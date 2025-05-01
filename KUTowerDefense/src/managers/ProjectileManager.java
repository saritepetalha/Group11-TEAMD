package managers;

import helpMethods.LoadSave;
import objects.Projectile;
import scenes.Playing;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class ProjectileManager {
    private Playing playing;
    private ArrayList<Projectile> projectiles = new ArrayList<>();
    private BufferedImage[] proj_imgs;

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

    public void update() {

    }

    public void draw(Graphics g) {
        for (BufferedImage i: proj_imgs) {
            g.drawImage(i, 300, 300, null);
        }
    }

}
