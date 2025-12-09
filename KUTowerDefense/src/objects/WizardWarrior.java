package objects;

import strategies.TargetingStrategy;
import constants.Constants;

public class WizardWarrior extends Warrior {

    public WizardWarrior(int x, int y) {
        super(x, y);
        setDefaultProperties();
    }
    
    public WizardWarrior(int spawnX, int spawnY, int targetX, int targetY) {
        super(spawnX, spawnY, targetX, targetY);
        setDefaultProperties();
    }


    @Override
    protected void initializeAnimationParameters() {
        this.runFrameCount = 8; // Run animation has 8 frames
        this.attackFrameCount = 8; // Attack animation has 8 frames
        this.animationSpeed = 10; // Slightly slower animation for wizard
    }

    private void setDefaultProperties() {
        this.damage = Constants.Towers.getStartDamage(Constants.Towers.MAGE);
        this.range = Constants.Towers.getRange(Constants.Towers.MAGE);
        this.cooldown = Constants.Towers.getCooldown(Constants.Towers.MAGE);
    }

    @Override
    public int getType() {
        return Constants.Towers.MAGE;
    }

    @Override
    public float getCooldown() {
        return cooldown;
    }

    @Override
    public float getRange() {
        return range;
    }

    @Override
    public int getDamage() {
        return damage;
    }

    @Override
    public int getCost() {
        return 75; // Example cost, adjust as needed
    }
} 