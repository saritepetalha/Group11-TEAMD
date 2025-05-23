package objects;

import constants.Constants;
// import java.awt.image.BufferedImage; // No longer needed here

public class ArtilleryTower extends Tower {

    // public BufferedImage upgradedSprite; // No longer needed here

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

    @Override
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
    public Tower upgrade() {
        if (level == 1) {
            this.level = 2;
            // Stats (range, damage) are handled by the decorator.
            // Base tower stats remain as level 1 defaults.
            // Decorator handles its own sprite.
            return new UpgradedArtilleryTower(this);
        }
        return this; // Already upgraded
    }
}
