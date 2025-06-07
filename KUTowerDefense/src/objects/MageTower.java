package objects;

import constants.Constants;
import constants.GameDimensions;
import strategies.TargetingStrategy;
import enemies.Enemy;
import scenes.Playing;
import skills.SkillTree;
import skills.SkillType;
import config.EnemyType;
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
        float baseRange = Constants.Towers.getRange(Constants.Towers.MAGE);
        if (skills.SkillTree.getInstance().isSkillSelected(skills.SkillType.EAGLE_EYE)) {
            float bonus = GameDimensions.TILE_DISPLAY_SIZE;
            //System.out.println("[EAGLE_EYE] Mage tower applies bonus range: " + baseRange + " -> " + (baseRange + bonus));
            baseRange += bonus;
        }
        return baseRange;
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

    @Override
    public void applyOnHitEffect(Enemy enemy, Playing playingScene) {
        System.out.println("[DEBUG] MageTower.applyOnHitEffect called for enemy type: " + enemy.getEnemyTypeEnum());
        int damage = getDamage();
        EnemyType type = enemy.getEnemyTypeEnum();
        if (SkillTree.getInstance().isSkillSelected(SkillType.MAGIC_PIERCING) &&
            (type == EnemyType.KNIGHT || type == EnemyType.BARREL)) {
            int bonusDamage = Math.round(damage * 1.2f);
            System.out.println("[MAGIC_PIERCING] Mage tower applies bonus damage to armored enemy: " + damage + " -> " + bonusDamage);
            damage = bonusDamage;
        }
        enemy.hurt(damage);
    }

    // public boolean isLevel2() { return isLevel2; } // No longer needed
}
