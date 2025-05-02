package objects;

import constants.Constants;

import java.awt.*;

public class Tower {

    private int x, y, type, ID, countDownClock, damage;
    private float range, cooldown;
    private static int num = 0;

    public Tower(int x, int y, int type) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.ID = num;
        num++;
        setDefaultDamage();
        setDefaultRange();
        setDefaultCooldown();
    }

    private void setDefaultCooldown() {
        cooldown = Constants.Towers.getCooldown(type);
    }

    private void setDefaultRange() {
        range = Constants.Towers.getRange(type);
    }

    private void setDefaultDamage() {
        damage = Constants.Towers.getStartDamage(type);
    }

    public int getDamage() {
        return damage;
    }

    public float getRange() {
        return range;
    }

    public float getCooldown() {
        return cooldown;
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
    public int getType() {
        return type;
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
        countDownClock ++;
    }
}
