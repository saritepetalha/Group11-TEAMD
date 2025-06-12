package managers;
import constants.GameDimensions;
import objects.Tile;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import helpMethods.LoadSave;
import ui_p.AssetsLoader;


public class TileManager {
    public Tile
            CurvedRoadRightUp, CurvedRoadNorth, CurvedRoadLeftDown, FlatRoadUp,
            CurvedRoadWest, Grass, CurvedRoadEast, FlatRoadVertical,
            CurvedRoadRightDown, CurvedRoadSouth, CurvedRoadLeftUp, FlatRoadDown,
            FlatRoadLeft, FlatRoadHorizontal, FlatRoadRight, DeadTree,
            Tree1, Tree2, Tree3, Rock1,
            ArtilleryTower, MageTower, House, Rock2,
            CastleTopLeft, CastleTopRight, ArcherTower, Pit,
            CastleBottomLeft, CastleBottomRight, SmallCastle, Wood, RoadFourWay, PoisonTower;

    public BufferedImage atlas;
    public ArrayList<Tile> tiles = new ArrayList<>();

    // New snow transition manager
    private SnowTransitionManager snowTransitionManager;

    public TileManager() {
        loadAtlas();
        createTiles();

        snowTransitionManager = new SnowTransitionManager();
        System.out.println("TileManager initialized with snow transition support");
    }

    public Tile getTile(int id){
        return tiles.get(id);
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
        tiles.add(CurvedRoadLeftDown = new Tile(getSprite(0, 0), id++, "CurvedRoadLeftDown"));
        tiles.add(CurvedRoadNorth = new Tile(getSprite(1, 0), id++, "CurvedRoadNorth"));
        tiles.add(CurvedRoadRightDown = new Tile(getSprite(2, 0), id++, "CurvedRoadRightDown"));
        tiles.add(FlatRoadUp = new Tile(getSprite(3, 0), id++, "FlatRoadUp"));

        tiles.add(CurvedRoadWest = new Tile(getSprite(0, 1), id++, "CurvedRoadWest"));
        tiles.add(Grass = new Tile(getSprite(1, 1), id++, "Grass"));
        tiles.add(CurvedRoadEast = new Tile(getSprite(2, 1), id++, "CurvedRoadEast"));
        tiles.add(FlatRoadVertical = new Tile(getSprite(3, 1), id++, "FlatRoadVertical"));

        tiles.add(CurvedRoadLeftUp = new Tile(getSprite(0, 2), id++, "CurvedRoadLeftUp"));
        tiles.add(CurvedRoadSouth = new Tile(getSprite(1, 2), id++, "CurvedRoadSouth"));
        tiles.add(CurvedRoadRightUp = new Tile(getSprite(2, 2), id++, "CurvedRoadRightUp"));
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

        tiles.add(CastleTopLeft = new Tile(getSprite(0, 6), id++, "Castle")); // top-left
        tiles.add(CastleTopRight = new Tile(getSprite(1, 6), id++, "Castle")); // top-right
        tiles.add(ArcherTower = new Tile(getSprite(2, 6), id++, "ArcherTower"));
        tiles.add(Pit = new Tile(getSprite(3, 6), id++, "Pit"));

        tiles.add(CastleBottomLeft = new Tile(getSprite(0, 7), id++, "Castle")); // bottom-left
        tiles.add(CastleBottomRight = new Tile(getSprite(1, 7), id++, "Castle")); // bottom-right
        tiles.add(SmallCastle = new Tile(getSprite(2, 7), id++, "SmallCastle"));
        tiles.add(Wood = new Tile(getSprite(3, 7), id++, "Wood"));

        tiles.add(RoadFourWay = new Tile(AssetsLoader.getInstance().fourWayRoadImg,id++,"RoadFourWay"));

        tiles.add(PoisonTower = new Tile(resizeImage(AssetsLoader.getInstance().poisonTowerImg,GameDimensions.TILE_DISPLAY_SIZE, GameDimensions.TILE_DISPLAY_SIZE), id++, "PoisonTower"));
    }

    // This method is used to get the sprite of a specific tile by index
    public BufferedImage getSprite(int index) {
        // Get the original sprite first
        BufferedImage originalSprite = getOriginalSprite(index);

        // Apply snow effect if active
        if (snowTransitionManager != null && snowTransitionManager.isSnowEffectActive()) {
            return snowTransitionManager.getSnowSprite(index, originalSprite);
        }

        return originalSprite;
    }

    /**
     * Gets the original sprite without any snow effects
     */
    private BufferedImage getOriginalSprite(int index) {
        // special handling for start and end points
        if (index == -1) {
            return resizeImage(AssetsLoader.getInstance().startPointImg, GameDimensions.PATHPOINT_DISPLAY_SIZE, GameDimensions.PATHPOINT_DISPLAY_SIZE);
        } else if (index == -2) {
            return resizeImage(AssetsLoader.getInstance().endPointImg, GameDimensions.PATHPOINT_DISPLAY_SIZE, GameDimensions.PATHPOINT_DISPLAY_SIZE);
        } else if (index == -3) { // Wall
            BufferedImage wallImg = LoadSave.getImageFromPath("/Borders/wall.png");
            if (wallImg != null) {
                // Determine wall orientation based on position
                int[][] levelData = LoadSave.getLevelData("defaultleveltest1");
                if (levelData != null) {
                    // Find the wall's position in the level data
                    for (int i = 0; i < levelData.length; i++) {
                        for (int j = 0; j < levelData[i].length; j++) {
                            if (levelData[i][j] == -3) {
                                // Check which edge the wall is on
                                if (i == 0) { // Top edge
                                    return resizeImage(wallImg, GameDimensions.TILE_DISPLAY_SIZE, GameDimensions.TILE_DISPLAY_SIZE);
                                } else if (i == levelData.length - 1) { // Bottom edge
                                    return rotateImage(wallImg, 180);
                                } else if (j == 0) { // Left edge
                                    return rotateImage(wallImg, -90);
                                } else if (j == levelData[i].length - 1) { // Right edge
                                    return rotateImage(wallImg, 90);
                                }
                            }
                        }
                    }
                }
                // Default case: return original image
                return resizeImage(wallImg, GameDimensions.TILE_DISPLAY_SIZE, GameDimensions.TILE_DISPLAY_SIZE);
            }
        } else if (index == -4) { // Gate
            BufferedImage gateImg = LoadSave.getImageFromPath("/Borders/gate.png");
            if (gateImg != null) {
                return resizeImage(gateImg, GameDimensions.TILE_DISPLAY_SIZE, GameDimensions.TILE_DISPLAY_SIZE);
            }
        }

        // regular tile handling
        if (index < 0 || index >= tiles.size()) {
            throw new IndexOutOfBoundsException("Invalid tile index: " + index);
        }
        return tiles.get(index).getSprite();
    }

    /**
     * Updates snow transition manager with current weather state
     */
    public void updateSnowTransition(float deltaTime, boolean isSnowing) {
        if (snowTransitionManager != null) {
            snowTransitionManager.update(deltaTime, isSnowing);
        }
    }

    /**
     * Gets the current snow transition state
     */
    public SnowTransitionManager.SnowState getSnowState() {
        return snowTransitionManager != null ? snowTransitionManager.getCurrentState() : SnowTransitionManager.SnowState.NORMAL;
    }

    /**
     * Gets the snow transition progress (0.0 to 1.0)
     */
    public float getSnowTransitionProgress() {
        return snowTransitionManager != null ? snowTransitionManager.getTransitionProgress() : 0.0f;
    }

    /**
     * Gets a description of the current snow state
     */
    public String getSnowStateDescription() {
        return snowTransitionManager != null ? snowTransitionManager.getStateDescription() : "No Snow";
    }

    private BufferedImage rotateImage(BufferedImage original, double degrees) {
        double rads = Math.toRadians(degrees);
        double sin = Math.abs(Math.sin(rads));
        double cos = Math.abs(Math.cos(rads));

        int w = original.getWidth();
        int h = original.getHeight();

        int newWidth = (int) Math.floor(w * cos + h * sin);
        int newHeight = (int) Math.floor(h * cos + w * sin);

        BufferedImage rotated = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = rotated.createGraphics();

        g2d.translate((newWidth - w) / 2, (newHeight - h) / 2);
        g2d.rotate(rads, w / 2, h / 2);
        g2d.drawImage(original, 0, 0, null);
        g2d.dispose();

        return resizeImage(rotated, GameDimensions.TILE_DISPLAY_SIZE, GameDimensions.TILE_DISPLAY_SIZE);
    }

    private BufferedImage resizeImage(BufferedImage original, int width, int height) {
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = resized.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(original, 0, 0, width, height, null);
        g.dispose();
        return resized;
    }

    // dedicated method to get the full castle sprite, as its dimension doubles other tiles' dimensions
    public BufferedImage getFullCastleSprite() {
        return atlas.getSubimage(
                0 * GameDimensions.TILE_DISPLAY_SIZE,    // X coordinate (first tile, left)
                6 * GameDimensions.TILE_DISPLAY_SIZE,       // Y coordinate (row 6)
                GameDimensions.TILE_DISPLAY_SIZE * 2,       // 2 tiles wide, as it is a 2x2 tile
                GameDimensions.TILE_DISPLAY_SIZE * 2        // 2 tiles high, as it is a 2x2 tile
        );
    }

    // This method is used to get a specific tile sprite from the atlas (except castle tiles)
    private BufferedImage getSprite(int x, int y) {
        BufferedImage tile = atlas.getSubimage(x * GameDimensions.TILE_DISPLAY_SIZE,
                y * GameDimensions.TILE_DISPLAY_SIZE,
                GameDimensions.TILE_DISPLAY_SIZE,
                GameDimensions.TILE_DISPLAY_SIZE);
        return tile;
    }
}