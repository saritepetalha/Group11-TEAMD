package objects;

import constants.Constants;

public class ArcherTower extends Tower {
    public ArcherTower(int x, int y) {
        super(x, y);
        setDefaultDamage();
        setDefaultRange();
        setDefaultCooldown();
    }

    @Override
    public float getCooldown() {
        return Constants.Towers.getCooldown(Constants.Towers.ARCHER);
    }

    @Override
    public float getRange() {
        return Constants.Towers.getRange(Constants.Towers.ARCHER);
    }

    @Override
    public int getDamage() {
        return Constants.Towers.getStartDamage(Constants.Towers.ARCHER);
    }

    @Override
    public int getType() {
        return Constants.Towers.ARCHER;
    }
}
