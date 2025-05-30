package objects;

import strategies.TargetingStrategy;
import constants.Constants;

public class WizardWarrior extends Warrior {

    public WizardWarrior(int x, int y) {
        super(x, y);
        setDefaultProperties();
    }

    public WizardWarrior(int x, int y, TargetingStrategy targetingStrategy) {
        super(x, y, targetingStrategy);
        setDefaultProperties();
    }

    @Override
    protected void initializeAnimationParameters() {
        this.maxFrameCount = 7; // Wizard has 7 frames
        this.animationSpeed = 12; // Adjust as needed
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