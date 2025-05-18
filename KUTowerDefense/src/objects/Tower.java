package objects;

import constants.Constants;

import java.awt.*;

public abstract class Tower {

    private int x, y, ID, countDownClock;
    protected int damage;
    protected float range, cooldown;
    private static int num = 0;
    protected int level = 1;
    public abstract int getType();

    public Tower(int x, int y) {
        this.x = x;
        this.y = y;
        this.ID = num;
        num++;
    }

    public abstract float getCooldown();
    public abstract float getRange();
    public abstract int getDamage();

    protected void setDefaultCooldown() {
        cooldown = Constants.Towers.getCooldown(getType());
    }

    protected void setDefaultRange() {
        range = Constants.Towers.getRange(getType());
    }

    protected void setDefaultDamage() {
        damage = Constants.Towers.getStartDamage(getType());
    }


    public boolean isClicked(int mouseX, int mouseY) {
        Rectangle bounds = new Rectangle(x, y, 64, 64);
        return bounds.contains(mouseX, mouseY);
    }

    public int getX() {
        return x;
    }
    public int getY() {
        return y;
    }
    public int getID() {
        return ID;
    }

    public boolean isCooldownOver() {
        return countDownClock >= cooldown;
    }

    public void resetCooldown() {
        countDownClock = 0;
    }

    public void update() {
        countDownClock++;
    }

    public void update(float gameSpeedMultiplier) {
        countDownClock += gameSpeedMultiplier;
    }

    public int getLevel() { return level; }
    public boolean isUpgradeable() { return level == 1; }
    public abstract void upgrade();
    public void setLevel(int lvl) { this.level = lvl; }
}
