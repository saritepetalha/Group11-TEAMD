package objects;

import enemies.Enemy;
import helpMethods.LoadSave;
import scenes.Playing;

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
        super.applyOnHitEffect(enemy, playingScene); // Call decorated tower's effect first (if any)
        // Apply 20% slow for 4 seconds
        enemy.applySlow(SLOW_FACTOR, SLOW_DURATION_SECONDS * 60); // Assuming 60 updates per second
        // System.out.println("Mage Tower Applied Slow to Enemy: " + enemy.getId());
    }

} 