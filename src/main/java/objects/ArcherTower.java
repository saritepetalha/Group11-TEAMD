package objects;

import constants.Constants;
// import java.awt.image.BufferedImage; // No longer needed here

public class ArcherTower extends Tower {
    // public BufferedImage upgradedSprite; // No longer needed here

    public ArcherTower(int x, int y) {
        super(x, y);
        setDefaultDamage();
        setDefaultRange();
        setDefaultCooldown();
    }

    @Override
    public float getCooldown() {
        // For level 1, return base cooldown. Decorator will modify for level 2.
        return Constants.Towers.getCooldown(Constants.Towers.ARCHER);
    }

    @Override
    public float getRange() {
        // For level 1, return base range. Decorator will modify for level 2.
        return Constants.Towers.getRange(Constants.Towers.ARCHER);
    }

    @Override
    public int getDamage() {
        // For level 1, return base damage. Decorator will modify for level 2.
        return Constants.Towers.getStartDamage(Constants.Towers.ARCHER);
    }

    @Override
    public int getType() {
        return Constants.Towers.ARCHER;
    }

    @Override
    public Tower upgrade() {
        if (level == 1) {
            this.level = 2;
            // Stats are now handled by the decorator.
            // Base tower stats (damage, range, cooldown) remain as level 1 defaults.
            // The decorator will override getDamage, getRange, getCooldown.
            // No need to load upgraded sprite here; decorator handles its own sprite.
            return new UpgradedArcherTower(this);
        }
        return this; // Already upgraded or cannot be upgraded further
    }
}
