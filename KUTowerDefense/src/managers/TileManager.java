package managers;

import objects.Tile;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import helpMethods.LoadSave;

public class TileManager {
    public Tile Grass, Rock, Road;
    public BufferedImage atlas;
    public ArrayList<Tile> tiles = new ArrayList<>();

    public TileManager() {
        loadAtlas();
        createTiles();
    }

    // This method is used to load the tile atlas from the resources by using LoadSave class
    private void loadAtlas() {
        atlas = LoadSave.getSpriteAtlas();
        if (atlas == null) {
            throw new RuntimeException("Failed to load tile atlas");
        }
    }

    // This method is used to create the tiles from the atlas
    private void createTiles() {
        tiles.add(Grass = new Tile(getTileSprite(3, 3))); // Grass
        tiles.add(Rock = new Tile(getTileSprite(4, 3))); // Rock
        tiles.add(Road = new Tile(getTileSprite(3, 1))); // Road
    }

    // This method is used to get the sprite of a specific tile by index
    public BufferedImage getSprite(int index) {
        if (index < 0 || index >= tiles.size()) {
            throw new IndexOutOfBoundsException("Invalid tile index: " + index);
        }
        return tiles.get(index).getSprite();
    }

    // This method is used to get a specific tile sprite from the atlas
    private BufferedImage getTileSprite(int x, int y) {
        return atlas.getSubimage(x * 64, y * 64, 64, 64);
    }
}