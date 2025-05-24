package objects;

import constants.Constants;
import java.awt.image.BufferedImage;

public class MageTower extends Tower {
    public BufferedImage upgradedSprite;
    private boolean isLevel2 = false;

    public MageTower(int x, int y) {
        super(x, y);
        setDefaultDamage();
        setDefaultRange();
        setDefaultCooldown();
    }

    @Override
    public float getCooldown() {
        return Constants.Towers.getCooldown(Constants.Towers.MAGE);
    }

    public float getRange() {
        return Constants.Towers.getRange(Constants.Towers.MAGE);
    }

    @Override
    public int getDamage() {
        return Constants.Towers.getStartDamage(Constants.Towers.MAGE);
    }

    @Override
    public int getType() {
        return Constants.Towers.MAGE;
    }

    @Override
    public void upgrade() {
        if (level == 1) {
            level = 2;
            isLevel2 = true;
            // Load upgraded sprite
            upgradedSprite = helpMethods.LoadSave.getImageFromPath("/TowerAssets/mage_up.png");
        }
    }

    public boolean isLevel2() { return isLevel2; }
}
