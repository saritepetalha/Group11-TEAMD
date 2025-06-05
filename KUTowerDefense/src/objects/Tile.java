package objects;

import java.awt.image.BufferedImage;

public class Tile {
    private BufferedImage sprite;
    private int id;
    private String name;
    private int x;
    private int y;

    public Tile(int x, int y, int id) {
        this.x = x;
        this.y = y;
        this.id = id;
    }

    public Tile(BufferedImage sprite, int id, String name) {
        this.sprite = sprite;
        this.id = id;
        this.name = name;
    }

    public int getId() {return id;}

    public String getName() {return name;}

    public BufferedImage getSprite() {
        return sprite;
    }

    public int getX() {return x;}

    public int getY() {return y;}
}
