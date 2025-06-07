package objects;

import helpMethods.LoadSave;
import java.awt.image.BufferedImage;

public class UpgradedArcherTower extends TowerDecorator {
    private BufferedImage sprite;

    public UpgradedArcherTower(Tower decoratedTower) {
        super(decoratedTower);
        // Ensure the decorated tower's level is set to 2 when it's wrapped.
        // This might have been done in ArcherTower's upgrade() method before returning this decorator.
        // If ArcherTower.upgrade() already sets its own level to 2, this line is redundant.
        // However, to be safe and ensure the wrapped tower reflects its upgraded state:
        decoratedTower.setLevel(2);
        this.sprite = LoadSave.getImageFromPath("/TowerAssets/archer_up.png");
    }

    @Override
    public float getCooldown() {
        // 2x fire rate means cooldown is halved.
        return decoratedTower.getCooldown() / 2f;
    }

    @Override
    public float getRange() {
        float baseRange = decoratedTower.getRange() * 1.5f;
        if (skills.SkillTree.getInstance().isSkillSelected(skills.SkillType.EAGLE_EYE)) {
            float bonus = constants.GameDimensions.TILE_DISPLAY_SIZE;
            System.out.println("[EAGLE_EYE] Upgraded Archer tower applies bonus range: " + baseRange + " -> " + (baseRange + bonus));
            baseRange += bonus;
        }
        return baseRange;
    }

    @Override
    public int getDamage() {
        // Damage remains the same as the base Archer Tower unless specified otherwise.
        // The original ArcherTower.upgrade() called setDefaultDamage(), implying damage might reset or change.
        // For decorator, we should be clear: if damage changes, it should be here.
        // If it simply uses the base tower's (level 1) default damage, this is fine.
        // The original ArcherTower.upgrade() did: `setDefaultDamage(); range = (float)(getRange() * 1.5); cooldown = getCooldown() / 2f;`
        // `setDefaultDamage()` would use Constants.Towers.getStartDamage(ARCHER).
        // So, the upgraded archer tower should also use this base damage.
        return decoratedTower.getDamage(); //This will fetch damage from the underlying ArcherTower, which should have its damage set appropriately.
    }

    // getType() is inherited from TowerDecorator, which delegates to decoratedTower.

    @Override
    public BufferedImage getSprite() {
        return sprite;
    }

    // applyOnHitEffect is inherited and defaults to decoratedTower's implementation (which is none for base Tower).
    // Archer upgrade doesn't specify special on-hit effects, so default is fine.
} 