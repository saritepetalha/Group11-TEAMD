package managers;
import constants.GameDimensions;
import objects.Tile;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
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
            CastleBottomLeft, CastleBottomRight, SmallCastle, Wood, RoadFourWay;

    public BufferedImage atlas;
    public ArrayList<Tile> tiles = new ArrayList<>();

    // Kar çimi karoları için değişkenler
    private BufferedImage[] snowyGrassSprites;
    private Map<String, Integer> grassSnowStages = new HashMap<>();
    private long lastSnowUpdateTime = 0;
    private static final long SNOW_UPDATE_INTERVAL = 30000; // 30 saniye (milisaniye cinsinden)
    private static final int MAX_SNOW_STAGES = 4; // 4 aşamalı kar birikimi

    public TileManager() {
        loadAtlas();
        loadSnowyGrassSprites();
        createTiles();
    }

    public Tile getTile(int id){
        return tiles.get(id);
    }

    // Kar çimi sprite'larını yükle
    private void loadSnowyGrassSprites() {
        try {
            System.out.println("Kar çimi sprite'ları yükleniyor...");
            BufferedImage snowyGrassAtlas = LoadSave.getImageFromPath("/Tiles/snowy_grass.png");
            if (snowyGrassAtlas != null) {
                System.out.println("Kar çimi atlas yüklendi. Boyut: " + snowyGrassAtlas.getWidth() + "x" + snowyGrassAtlas.getHeight());
                snowyGrassSprites = new BufferedImage[MAX_SNOW_STAGES];

                // Asset 4 farklı kar aşamasını içeriyor (1024x1536 toplam boyut)
                // 2x2 grid halinde düzenlenmiş: 1024/2 = 512 genişlik, 1536/2 = 768 yükseklik
                int tileWidth = 512;  // Her tile'ın genişliği
                int tileHeight = 768; // Her tile'ın yüksekliği

                System.out.println("Her sprite boyutu: " + tileWidth + "x" + tileHeight);

                // 4 farklı kar aşamasını yükle (2x2 grid)
                // Aşama 0: Az kar (sol-üst), Aşama 1: Orta kar (sağ-üst)
                // Aşama 2: Çok kar (sol-alt), Aşama 3: Tam kar (sağ-alt)
                int[] xPositions = {0, 512, 0, 512};
                int[] yPositions = {0, 0, 768, 768};

                for (int i = 0; i < MAX_SNOW_STAGES; i++) {
                    snowyGrassSprites[i] = snowyGrassAtlas.getSubimage(
                            xPositions[i], yPositions[i], tileWidth, tileHeight
                    );
                    // Sprite'ları oyun boyutuna yeniden boyutlandır
                    snowyGrassSprites[i] = resizeImage(snowyGrassSprites[i],
                            GameDimensions.TILE_DISPLAY_SIZE, GameDimensions.TILE_DISPLAY_SIZE);
                    System.out.println("Kar çimi sprite " + i + " yüklendi (x:" + xPositions[i] + ", y:" + yPositions[i] + ")");
                }
                System.out.println("Kar çimi sprite'ları başarıyla yüklendi: " + MAX_SNOW_STAGES + " aşama");
            } else {
                System.err.println("Kar çimi sprite'ları yüklenemedi! snowy_grass.png dosyası bulunamadı.");
            }
        } catch (Exception e) {
            System.err.println("Kar çimi sprite'ları yüklenirken hata: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Kar modunda çim karolarının kar aşamasını güncelle
    public void updateSnowOnGrass(boolean isSnowing) {
        long currentTime = System.currentTimeMillis();

        if (isSnowing && currentTime - lastSnowUpdateTime >= SNOW_UPDATE_INTERVAL) {
            // Kar yağıyorsa kar aşamasını artır
            int updatedCount = 0;
            for (Map.Entry<String, Integer> entry : grassSnowStages.entrySet()) {
                int currentStage = entry.getValue();
                if (currentStage < MAX_SNOW_STAGES - 1) {
                    grassSnowStages.put(entry.getKey(), currentStage + 1);
                    updatedCount++;
                }
            }
            if (updatedCount > 0) {
                System.out.println("Kar yağıyor! " + updatedCount + " çim karosu kar aşaması artırıldı");
            }
            lastSnowUpdateTime = currentTime;
        } else if (!isSnowing && currentTime - lastSnowUpdateTime >= SNOW_UPDATE_INTERVAL * 2) {
            // Kar yağmıyorsa kar aşamasını azalt (daha yavaş)
            int updatedCount = 0;
            for (Map.Entry<String, Integer> entry : grassSnowStages.entrySet()) {
                int currentStage = entry.getValue();
                if (currentStage > 0) {
                    grassSnowStages.put(entry.getKey(), currentStage - 1);
                    updatedCount++;
                }
            }
            if (updatedCount > 0) {
                System.out.println("Kar yağmıyor! " + updatedCount + " çim karosu kar aşaması azaltıldı");
            }
            lastSnowUpdateTime = currentTime;
        }
    }

    // Belirli bir pozisyondaki çim karosunun kar aşamasını al
    public int getGrassSnowStage(int x, int y) {
        String key = x + "," + y;
        return grassSnowStages.getOrDefault(key, 0);
    }

    // Belirli bir pozisyondaki çim karosunun kar aşamasını ayarla
    public void setGrassSnowStage(int x, int y, int stage) {
        String key = x + "," + y;
        grassSnowStages.put(key, Math.max(0, Math.min(stage, MAX_SNOW_STAGES - 1)));
    }

    // Kar çimi sprite'ını al
    public BufferedImage getSnowyGrassSprite(int stage) {
        if (snowyGrassSprites != null && stage >= 0 && stage < snowyGrassSprites.length) {
            return snowyGrassSprites[stage];
        }
        return null;
    }

    // Haritadaki tüm çim karolarını başlat
    public void initializeGrassSnowStages(int[][] level) {
        if (level == null) return;

        for (int i = 0; i < level.length; i++) {
            for (int j = 0; j < level[i].length; j++) {
                if (level[i][j] == 5) { // Çim karosu ID'si
                    setGrassSnowStage(j, i, 0); // Başlangıçta kar yok
                }
            }
        }
        System.out.println("Çim karolarının kar aşamaları başlatıldı");
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
    }

    // This method is used to get the sprite of a specific tile by index
    public BufferedImage getSprite(int index) {
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