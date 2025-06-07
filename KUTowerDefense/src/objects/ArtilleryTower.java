package objects;

import constants.Constants;
import constants.GameDimensions;
import strategies.TargetingStrategy;
// import java.awt.image.BufferedImage; // No longer needed here

public class ArtilleryTower extends Tower {

    // public BufferedImage upgradedSprite; // No longer needed here

    public ArtilleryTower(int x, int y) {
        super(x, y);
        setDefaultDamage();
        setDefaultRange();
        setDefaultCooldown();
    }

    // Constructor with custom targeting strategy
    public ArtilleryTower(int x, int y, TargetingStrategy targetingStrategy) {
        super(x, y, targetingStrategy);
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
        float baseRange = Constants.Towers.getRange(Constants.Towers.ARTILLERY);
        if (skills.SkillTree.getInstance().isSkillSelected(skills.SkillType.EAGLE_EYE)) {
            float bonus = GameDimensions.TILE_DISPLAY_SIZE;
            //System.out.println("[EAGLE_EYE] Artillery tower applies bonus range: " + baseRange + " -> " + (baseRange + bonus));
            baseRange += bonus;
        }
        return baseRange;
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
