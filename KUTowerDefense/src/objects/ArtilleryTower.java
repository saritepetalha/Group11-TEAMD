package objects;

import constants.Constants;
import java.awt.image.BufferedImage;

public class ArtilleryTower extends Tower {

    public BufferedImage upgradedSprite;

    public ArtilleryTower(int x, int y) {
        super(x, y);
        setDefaultDamage();
        setDefaultRange();
        setDefaultCooldown();
    }

    @Override
    public float getCooldown() {
        return Constants.Towers.getCooldown(Constants.Towers.ARTILLERY);
    }

    public float getRange() {
        return Constants.Towers.getRange(Constants.Towers.ARTILLERY);
    }

    @Override
    public int getDamage() {
        return Constants.Towers.getStartDamage(Constants.Towers.ARTILLERY);
    }

    @Override
    public int getType() {
        return Constants.Towers.ARTILLERY;
    }

    @Override
    public void upgrade() {
        if (level == 1) {
            level = 2;
            range = (float)(getRange() * 1.2);
            damage = (int)(getDamage() * 1.2);
            // Load upgraded sprite
            upgradedSprite = helpMethods.LoadSave.getImageFromPath("/TowerAssets/artillery_up.png");
        }
    }
}
