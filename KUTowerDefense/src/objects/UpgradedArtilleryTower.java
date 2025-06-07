package objects;

import helpMethods.LoadSave;
import java.awt.image.BufferedImage;
import constants.GameDimensions;

public class UpgradedArtilleryTower extends TowerDecorator {
    private BufferedImage sprite;

    public UpgradedArtilleryTower(Tower decoratedTower) {
        super(decoratedTower);
        decoratedTower.setLevel(2);
        this.sprite = LoadSave.getImageFromPath("/TowerAssets/artillery_up.png");
    }

    @Override
    public float getRange() {
        float baseRange = decoratedTower.getRange() * 1.2f;
        if (skills.SkillTree.getInstance().isSkillSelected(skills.SkillType.EAGLE_EYE)) {
            float bonus = GameDimensions.TILE_DISPLAY_SIZE;
            System.out.println("[EAGLE_EYE] Upgraded Artillery tower applies bonus range: " + baseRange + " -> " + (baseRange + bonus));
            baseRange += bonus;
        }
        return baseRange;
    }

    @Override
    public int getDamage() {
        // 20% more damage.
        // The original ArtilleryTower.upgrade() did: `range = (float)(getRange() * 1.2); damage = (int)(getDamage() * 1.2);`
        // So the damage increase applies to the base tower's damage.
        return (int) (decoratedTower.getDamage() * 1.2f);
    }

    // Cooldown remains the same as the base Artillery Tower.

    @Override
    public BufferedImage getSprite() {
        return sprite;
    }

    // Artillery upgrade doesn't specify special on-hit effects beyond AOE, which is inherent to its projectile, not a status effect applied here.
    // So default applyOnHitEffect is fine.
} 