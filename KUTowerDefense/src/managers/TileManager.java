package managers;

import dimensions.GameDimensions;
import objects.Tile;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import helpMethods.LoadSave;

public class TileManager {
    public Tile
            CurvedRoadNorthWest,
            CurvedRoadNorth,
            CurvedRoadNorthEast,
            FlatRoadUp,

            CurvedRoadWest,
            Grass,
            CurvedRoadEast,
            FlatRoadVertical,

            CurvedRoadSouthWest,
            CurvedRoadSouth,
            CurvedRoadSouthEast,
            FlatRoadDown,

            FlatRoadLeft,
            FlatRoadHorizontal,
            FlatRoadRight,
            DeadTree,

            Tree1,
            Tree2,
            Tree3,
            Rock1,

            ArtilleryTower,
            MageTower,
            House,
            Rock2,

            Castle,
            ArcherTower,
            Pit,
            SmallCastle,
            Wood;

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
        int id = 0;
        tiles.add(CurvedRoadNorthWest = new Tile(getSprite(0, 0), id++, "CurvedRoadNorthWest"));
        tiles.add(CurvedRoadNorth = new Tile(getSprite(1, 0), id++, "CurvedRoadNorth"));
        tiles.add(CurvedRoadNorthEast = new Tile(getSprite(2, 0), id++, "CurvedRoadNorthEast"));
        tiles.add(FlatRoadUp = new Tile(getSprite(3, 0), id++, "FlatRoadUp"));

        tiles.add(CurvedRoadWest = new Tile(getSprite(0, 1), id++, "CurvedRoadWest"));
        tiles.add(Grass = new Tile(getSprite(1, 1), id++, "Grass"));
        tiles.add(CurvedRoadEast = new Tile(getSprite(2, 1), id++, "CurvedRoadEast"));
        tiles.add(FlatRoadVertical = new Tile(getSprite(3, 1), id++, "FlatRoadVertical"));

        tiles.add(CurvedRoadSouthWest = new Tile(getSprite(0, 2), id++, "CurvedRoadSouthWest"));
        tiles.add(CurvedRoadSouth = new Tile(getSprite(1, 2), id++, "CurvedRoadSouth"));
        tiles.add(CurvedRoadSouthEast = new Tile(getSprite(2, 2), id++, "CurvedRoadSouthEast"));
        tiles.add(FlatRoadDown = new Tile(getSprite(3, 2), id++, "FlatRoadDown"));

        tiles.add(FlatRoadLeft = new Tile(getSprite(0, 3), id++, "FlatRoadLeft"));
        tiles.add(FlatRoadHorizontal = new Tile(getSprite(1, 3), id++, "FlatRoadHorizontal"));
        tiles.add(FlatRoadRight = new Tile(getSprite(2, 3), id++, "FlatRoadRight"));
        tiles.add(DeadTree = new Tile(getSprite(3, 3), id++, "DeadTree"));

        tiles.add(Tree1 = new Tile(getSprite(0, 4), id++, "Tree1"));
        tiles.add(Tree2 = new Tile(getSprite(1, 4), id++, "Tree2"));
        tiles.add(Tree3 = new Tile(getSprite(2, 4), id++, "Tree3"));
        tiles.add(Rock1 = new Tile(getSprite(3, 4), id++, "Rock1"));

        tiles.add(ArtilleryTower = new Tile(getSprite(0, 5), id++, "ArtilleryTower"));
        tiles.add(MageTower = new Tile(getSprite(1, 5), id++, "MageTower"));
        tiles.add(House = new Tile(getSprite(2, 5), id++, "House"));
        tiles.add(Rock2 = new Tile(getSprite(3, 5), id++, "Rock2"));

        tiles.add(Castle = new Tile(getSprite(0, 6), id++, "Castle")); // top-left
        tiles.add(new Tile(getSprite(1, 6), id++, "Castle")); // top-right
        tiles.add(ArcherTower = new Tile(getSprite(2, 6), id++, "ArcherTower"));
        tiles.add(Pit = new Tile(getSprite(3, 6), id++, "Pit"));

        tiles.add(new Tile(getSprite(0, 7), id++, "Castle")); // bottom-left
        tiles.add(new Tile(getSprite(1, 7), id++, "Castle")); // bottom-right
        tiles.add(SmallCastle = new Tile(getSprite(2, 7), id++, "SmallCastle"));
        tiles.add(Wood = new Tile(getSprite(3, 7), id++, "Wood"));



    }

    // This method is used to get the sprite of a specific tile by index
    public BufferedImage getSprite(int index) {
        if (index < 0 || index >= tiles.size()) {
            throw new IndexOutOfBoundsException("Invalid tile index: " + index);
        }
        return tiles.get(index).getSprite();
    }

    // This method is used to get a specific tile sprite from the atlas
    private BufferedImage getSprite(int x, int y) {
        // for castle tile, because its dimensions are doubled other tiles
        if(x == 0 && y == 6){
            BufferedImage tile = atlas.getSubimage(x * GameDimensions.TILE_DISPLAY_SIZE, y * GameDimensions.TILE_DISPLAY_SIZE,
                    GameDimensions.TILE_DISPLAY_SIZE*2, GameDimensions.TILE_DISPLAY_SIZE*2);
            System.out.println("Tile 1 loaded: " + (tile != null));
            return tile;
        }

        // other tiles
        else{
            BufferedImage tile = atlas.getSubimage(x * GameDimensions.TILE_DISPLAY_SIZE, y * GameDimensions.TILE_DISPLAY_SIZE,
                    GameDimensions.TILE_DISPLAY_SIZE, GameDimensions.TILE_DISPLAY_SIZE);
            System.out.println("Tile 1 loaded: " + (tile != null));
            return tile;
        }


    }
}