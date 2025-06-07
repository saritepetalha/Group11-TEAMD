package objects;

import constants.Constants;
import constants.GameDimensions;
import strategies.TargetingStrategy;
import skills.SkillTree;
import skills.SkillType;
// import java.awt.image.BufferedImage; // No longer needed here

public class ArcherTower extends Tower {
    // public BufferedImage upgradedSprite; // No longer needed here

    public ArcherTower(int x, int y) {
        super(x, y);
        setDefaultDamage();
        setDefaultRange();
        setDefaultCooldown();
    }

    // Constructor with custom targeting strategy
    public ArcherTower(int x, int y, TargetingStrategy targetingStrategy) {
        super(x, y, targetingStrategy);
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
        float baseRange = Constants.Towers.getRange(Constants.Towers.ARCHER);
        if (SkillTree.getInstance().isSkillSelected(SkillType.EAGLE_EYE)) {
            float bonus = GameDimensions.TILE_DISPLAY_SIZE;
            //System.out.println("[EAGLE_EYE] Archer tower applies bonus range: " + baseRange + " -> " + (baseRange + bonus));
            baseRange += bonus;
        }
        return baseRange;
    }

    @Override
    public int getDamage() {
        int baseDamage = Constants.Towers.getStartDamage(Constants.Towers.ARCHER);
        if (SkillTree.getInstance().isSkillSelected(SkillType.SHARP_ARROW_TIPS)) {
            int bonusDamage = Math.round(baseDamage * 1.10f);
            //System.out.println("[SHARP_ARROW_TIPS] Archer tower applies bonus damage: " + baseDamage + " -> " + bonusDamage);
            baseDamage = bonusDamage;
        }
        return baseDamage;
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
