package objects;

import strategies.TargetingStrategy;
import constants.Constants;

public class ArcherWarrior extends Warrior {

    public ArcherWarrior(int x, int y) {
        super(x, y);
        setDefaultProperties();
    }

    public ArcherWarrior(int x, int y, TargetingStrategy targetingStrategy) {
        super(x, y, targetingStrategy);
        setDefaultProperties();
    }

    @Override
    protected void initializeAnimationParameters() {
        this.maxFrameCount = 14; // Archer has 14 frames
        this.animationSpeed = 10; // Adjust as needed
    }

    private void setDefaultProperties() {
        this.damage = Constants.Towers.getStartDamage(Constants.Towers.ARCHER);
        this.range = Constants.Towers.getRange(Constants.Towers.ARCHER);
        this.cooldown = Constants.Towers.getCooldown(Constants.Towers.ARCHER);
    }

    @Override
    public int getType() {
        return Constants.Towers.ARCHER;
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
        return 50; // Example cost, adjust as needed
    }
} 