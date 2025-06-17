package objects;

import constants.Constants;

public class ArcherWarrior extends Warrior {

    public ArcherWarrior(int x, int y) {
        super(x, y);
        setDefaultProperties();
    }
    
    public ArcherWarrior(int spawnX, int spawnY, int targetX, int targetY) {
        super(spawnX, spawnY, targetX, targetY);
        setDefaultProperties();
    }


    @Override
    protected void initializeAnimationParameters() {
        this.runFrameCount = 8; // Run animation has 8 frames
        this.attackFrameCount = 6; // Attack animation has 6 frames
        this.animationSpeed = 8; // Animation speed
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