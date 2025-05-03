package objects;

import constants.Constants;

public class ArtilleryTower extends Tower {

    public ArtilleryTower(int x, int y) {
        super(x, y);
        setDefaultDamage();
        setDefaultRange();
        setDefaultCooldown();
    }

    @Override
    public float getCooldown() {
        return Constants.Towers.getCooldown(Constants.Towers.ARTILLERY);
    }

    public float getRange() {
        return Constants.Towers.getRange(Constants.Towers.ARTILLERY);
    }

    @Override
    public int getDamage() {
        return Constants.Towers.getStartDamage(Constants.Towers.ARTILLERY);
    }

    @Override
    public int getType() {
        return Constants.Towers.ARTILLERY;
    }
}
