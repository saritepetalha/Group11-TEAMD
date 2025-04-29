package objects;

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
