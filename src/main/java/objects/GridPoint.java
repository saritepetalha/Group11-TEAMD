package objects;

public class GridPoint {
    private int x,y;

    public GridPoint(int x, int y){
        this.x = x;
        this.y = y;
    }

    public int getX(){return x;}

    public int getY(){return y;}

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof GridPoint){
            objects.GridPoint other = (GridPoint) obj;
            return this.x == other.getX() && this.y == other.getY();
        }

        return false;
    }

    @Override
    public int hashCode() {
        return x*31 + y;
    }
}

