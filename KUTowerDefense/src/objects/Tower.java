package objects;

import java.awt.*;

public class Tower {

    private int x, y, type, ID;
    private static int num = 0;

    public Tower(int x, int y, int type) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.ID = num;
        num++;
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

}
