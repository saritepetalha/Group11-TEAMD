package objects;

import constants.Constants;
import strategies.TargetingStrategy;
// import java.awt.image.BufferedImage; // No longer needed here

public class MageTower extends Tower {
    // public BufferedImage upgradedSprite; // No longer needed
    // private boolean isLevel2 = false; // No longer needed, level is in Tower and decorator handles upgraded state

    public MageTower(int x, int y) {
        super(x, y);
        setDefaultDamage();
        setDefaultRange();
        setDefaultCooldown();
    }

    // Constructor with custom targeting strategy
    public MageTower(int x, int y, TargetingStrategy targetingStrategy) {
        super(x, y, targetingStrategy);
        setDefaultDamage();
        setDefaultRange();
        setDefaultCooldown();
    }

    @Override
    public float getCooldown() {
        return Constants.Towers.getCooldown(Constants.Towers.MAGE);
    }

    @Override
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
    public Tower upgrade() {
        if (level == 1) {
            this.level = 2;
            // Stats are handled by the decorator (slow effect).
            // Base tower stats remain as level 1 defaults.
            // Decorator handles its own sprite.
            return new UpgradedMageTower(this);
        }
        return this; // Already upgraded
    }

    // public boolean isLevel2() { return isLevel2; } // No longer needed
}
