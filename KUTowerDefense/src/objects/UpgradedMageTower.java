package objects;

import enemies.Enemy;
import helpMethods.LoadSave;
import scenes.Playing;
import constants.GameDimensions;

import java.awt.image.BufferedImage;

public class UpgradedMageTower extends TowerDecorator {
    private BufferedImage sprite;
    private static final float SLOW_FACTOR = 0.8f; // 20% slow
    private static final int SLOW_DURATION_SECONDS = 4;

    public UpgradedMageTower(Tower decoratedTower) {
        super(decoratedTower);
        decoratedTower.setLevel(2);
        this.sprite = LoadSave.getImageFromPath("/TowerAssets/mage_up.png");
    }

    // Range, Damage, Cooldown are the same as the base Mage Tower for Level 2 upgrade.
    // The original MageTower.upgrade() only set level and loaded sprite.
    // It did not change other stats directly.

    @Override
    public BufferedImage getSprite() {
        return sprite;
    }

    @Override
    public void applyOnHitEffect(Enemy enemy, Playing playingScene) {
        int damage = getDamage();
        config.EnemyType type = enemy.getEnemyTypeEnum();
        if (skills.SkillTree.getInstance().isSkillSelected(skills.SkillType.MAGIC_PIERCING) &&
            (type == config.EnemyType.KNIGHT || type == config.EnemyType.BARREL)) {
            int bonusDamage = Math.round(damage * 1.2f);
            System.out.println("[MAGIC_PIERCING] Upgraded Mage tower applies bonus damage to armored enemy: " + damage + " -> " + bonusDamage);
            damage = bonusDamage;
        }
        enemy.hurt(damage);
        // Apply 20% slow for 4 seconds
        enemy.applySlow(SLOW_FACTOR, SLOW_DURATION_SECONDS * 60); // Assuming 60 updates per second
        // System.out.println("Mage Tower Applied Slow to Enemy: " + enemy.getId());
    }

    @Override
    public float getRange() {
        float baseRange = decoratedTower.getRange();
        if (skills.SkillTree.getInstance().isSkillSelected(skills.SkillType.EAGLE_EYE)) {
            float bonus = GameDimensions.TILE_DISPLAY_SIZE;
            System.out.println("[EAGLE_EYE] Upgraded Mage tower applies bonus range: " + baseRange + " -> " + (baseRange + bonus));
            baseRange += bonus;
        }
        return baseRange;
    }

} 