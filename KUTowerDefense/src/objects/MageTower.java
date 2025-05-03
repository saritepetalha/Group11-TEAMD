package objects;

import constants.Constants;

public class MageTower extends Tower {
    public MageTower(int x, int y) {
        super(x, y);
        setDefaultDamage();
        setDefaultRange();
        setDefaultCooldown();
    }

    @Override
    public float getCooldown() {
        return Constants.Towers.getCooldown(Constants.Towers.MAGE);
    }

    public float getRange() {
        return Constants.Towers.getRange(Constants.Towers.MAGE);
    }

    @Override
    public int getDamage() {
        return Constants.Towers.getStartDamage(Constants.Towers.MAGE);
    }

    @Override
    public int getType() {
        return Constants.Towers.MAGE;
    }
}
