package objects;

import constants.Constants;
import java.awt.image.BufferedImage;

public class ArcherTower extends Tower {
    public BufferedImage upgradedSprite;

    public ArcherTower(int x, int y) {
        super(x, y);
        setDefaultDamage();
        setDefaultRange();
        setDefaultCooldown();
    }

    @Override
    public float getCooldown() {
        return Constants.Towers.getCooldown(Constants.Towers.ARCHER);
    }

    @Override
    public float getRange() {
        return Constants.Towers.getRange(Constants.Towers.ARCHER);
    }

    @Override
    public int getDamage() {
        return Constants.Towers.getStartDamage(Constants.Towers.ARCHER);
    }

    @Override
    public int getType() {
        return Constants.Towers.ARCHER;
    }

    @Override
    public void upgrade() {
        if (level == 1) {
            level = 2;
            setDefaultDamage();
            range = (float)(getRange() * 1.5);
            cooldown = getCooldown() / 2f;
            // Load upgraded sprite
            upgradedSprite = helpMethods.LoadSave.getImageFromPath("/TowerAssets/archer_up.png");
        }
    }
}
