package scenes;

import constants.GameDimensions;
import helpMethods.LoadSave;
import helpMethods.LevelBuilder;
import main.Game;
import managers.TileManager;
import objects.Tile;
import ui_p.ButtonAssets;
import ui_p.EditTiles;

import java.awt.*;
import java.awt.image.BufferedImage;

// a class to edit map. map editor part on the main screen.

public class MapEditing extends GameScene implements SceneMethods{

    private int[][] level;           // main level data with terrain/road IDs
    private int[][] overlayData;     // new array to store start/end point data
    private TileManager tileManager;
    private Tile selectedTile;
    private boolean drawSelected = false;
    private EditTiles editTiles;
    private int lastTileX, lastTileY, lastTileId, prevDraggedTileX, prevDraggedTileY;

    private int mouseX, mouseY;

    // constants for overlay data
    private static final int NO_OVERLAY = 0;
    private static final int START_POINT = 1;
    private static final int END_POINT = 2;

    public MapEditing(Game game) {
        super(game);
        level = LevelBuilder.getLevelData();
        overlayData = new int[level.length][level[0].length]; // initialize the overlay with the same dimensions as the level
        tileManager = new TileManager();
        editTiles = new EditTiles(GameDimensions.GAME_WIDTH,0,4*GameDimensions.ButtonSize.MEDIUM.getSize(), GameDimensions.GAME_HEIGHT,this, game);
        createDefaultLevel();
        loadDefaultLevel();
    }

    private void createDefaultLevel() {
        int[][] bruh = new int[20][20];
        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 20; j++) {
                bruh[i][j] = 0;
            }
        }
        //LoadSave.createLevel("defaultleveltest1", bruh);
    }

    private void loadDefaultLevel() {
        int[][] lvl = LoadSave.getLevelData("defaultleveltest1");
        //THIS LINE IS JUST TO SEE WHETHER THE BACKEND OF THE getLevelData function works or not
        //IT WORKS!!!
        System.out.println(java.util.Arrays.deepToString(lvl));
    }

    private void drawMap(Graphics g) {

        g.setColor(new Color(134,177,63,255));
        g.fillRect(0, 0, GameDimensions.GAME_WIDTH, GameDimensions.GAME_HEIGHT);

        for (int i = 0; i < level.length; i++) {
            for (int j = 0; j < level[i].length; j++) {
                g.drawImage(tileManager.getSprite(level[i][j]), j * GameDimensions.TILE_DISPLAY_SIZE, i * GameDimensions.TILE_DISPLAY_SIZE, null);

                // then draw any overlay (start/end points) if they exist
                if (overlayData[i][j] == START_POINT) {
                    drawOverlayImage(g, ButtonAssets.startPointImg, j, i);
                } else if (overlayData[i][j] == END_POINT) {
                    drawOverlayImage(g, ButtonAssets.endPointImg, j, i);
                }
            }
        }
    }

    // helper method to draw overlay images with transparency
    private void drawOverlayImage(Graphics g, BufferedImage image, int tileX, int tileY) {
        Graphics2D g2d = (Graphics2D) g;
        // save original composite
        Composite originalComposite = g2d.getComposite();

        // set semi-transparent composite
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));

        // draw the overlay image
        g2d.drawImage(image,
                tileX * GameDimensions.TILE_DISPLAY_SIZE,
                tileY * GameDimensions.TILE_DISPLAY_SIZE,
                GameDimensions.TILE_DISPLAY_SIZE,
                GameDimensions.TILE_DISPLAY_SIZE, null);

        // restore original composite
        g2d.setComposite(originalComposite);
    }

    @Override
    public void render(Graphics g) {

        drawMap(g);
        editTiles.draw(g);
        drawSelectedTile(g);
    }

    private void drawSelectedTile(Graphics g) {
        if (selectedTile != null && drawSelected) {
            int tileSize = GameDimensions.TILE_DISPLAY_SIZE;

            BufferedImage spriteToDraw;

            if (selectedTile.getName().equals("Castle")) {
                spriteToDraw = tileManager.getFullCastleSprite(); // using the complete 2x2 castle sprite
                g.drawImage(spriteToDraw, mouseX, mouseY, tileSize * 2, tileSize * 2, null);
            } else {
                spriteToDraw = selectedTile.getSprite();

                // if it's a start/end point, draw it semi-transparent
                if (selectedTile.getId() == -1 || selectedTile.getId() == -2) {
                    Graphics2D g2d = (Graphics2D) g;
                    Composite originalComposite = g2d.getComposite();
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
                    g2d.drawImage(spriteToDraw, mouseX, mouseY, tileSize, tileSize, null);
                    g2d.setComposite(originalComposite);
                } else {
                    g.drawImage(spriteToDraw, mouseX, mouseY, tileSize, tileSize, null);
                }
            }

        }
    }

    // check if a tile is a road tile
    private boolean isRoadTile(int tileId) {
        // check if the tile ID corresponds to any road type
        return tileId >= 0 && tileId <= 14 && tileId != 5; //
    }


    public void modifyTile(int x, int y) {
        x /= 64;
        y /= 64;

        if (selectedTile == null) {
            return;
        }

        // for start/end points, check if we're on a road tile
        if (selectedTile.getId() == -1) { // Start point
            if (!isRoadTile(level[y][x])) {
                System.out.println("Start point must be placed on a road!");
                return;
            }

            // cear any existing start points
            for (int i = 0; i < overlayData.length; i++) {
                for (int j = 0; j < overlayData[0].length; j++) {
                    if (overlayData[i][j] == START_POINT) {
                        overlayData[i][j] = NO_OVERLAY;
                    }
                }
            }

            // set new start point in overlay
            overlayData[y][x] = START_POINT;
            System.out.println("Start point placed at: " + x + "," + y);

        } else if (selectedTile.getId() == -2) { // End point
            if (!isRoadTile(level[y][x])) {
                System.out.println("End point must be placed on a road!");
                return;
            }

            // clear any existing end points
            for (int i = 0; i < overlayData.length; i++) {
                for (int j = 0; j < overlayData[0].length; j++) {
                    if (overlayData[i][j] == END_POINT) {
                        overlayData[i][j] = NO_OVERLAY;  // rest overlay to no_overlay
                    }
                }
            }

            // set new end point in overlay
            overlayData[y][x] = END_POINT;
            System.out.println("End point placed at: " + x + "," + y);


        } else if (selectedTile.getName().equals("Castle")) {
            // place Castle in 2x2 area
            if (y + 1 < level.length && x + 1 < level[0].length) {
                level[y][x] = tileManager.CastleTopLeft.getId();                   // top-left: ID 24
                level[y][x + 1] = tileManager.CastleTopRight.getId();              // top-right: ID 25
                level[y + 1][x] = tileManager.CastleBottomLeft.getId();            // bottom-left: ID 28
                level[y + 1][x + 1] = tileManager.CastleBottomRight.getId();       // bottom-right: ID 29

                // clear any overlays where castle is placed
                overlayData[y][x] = NO_OVERLAY;
                overlayData[y][x + 1] = NO_OVERLAY;
                overlayData[y + 1][x] = NO_OVERLAY;
                overlayData[y + 1][x + 1] = NO_OVERLAY;
            }
        } else {
            level[y][x] = selectedTile.getId();
            overlayData[y][x] = NO_OVERLAY;
        }
    }

    public void eraseTile(int x, int y) {
        x /= 64;
        y /= 64;
        level[y][x] = 5;
        overlayData[y][x] = NO_OVERLAY;
    }

    public void fillAllTiles() {
        if (selectedTile == null) {
            return;
        }

        // clear all overlays when filling the map
        for (int i = 0; i < overlayData.length; i++) {
            for (int j = 0; j < overlayData[i].length; j++) {
                overlayData[i][j] = NO_OVERLAY;
            }
        }

        for (int i = 0; i < level.length; i++) {
            for (int j = 0; j < level[i].length; j++) {
                if (selectedTile.getName().equals("Castle")) {
                    if (i + 1 < level.length && j + 1 < level[i].length) {
                        level[i][j] = tileManager.CastleTopLeft.getId();
                        level[i][j + 1] = tileManager.CastleTopRight.getId();
                        level[i + 1][j] = tileManager.CastleBottomLeft.getId();
                        level[i + 1][j + 1] = tileManager.CastleBottomRight.getId();
                    }
                } else {
                    if(selectedTile.getId() != -2 && selectedTile.getId() != -1) {
                        level[i][j] = selectedTile.getId();
                    }
                }
            }
        }
    }

    public void resetAllTiles() {
        for (int i = 0; i < level.length; i++) {
            for (int j = 0; j < level[i].length; j++) {
                level[i][j] = 5;
                overlayData[i][j] = NO_OVERLAY;
            }
        }
    }

    public void saveLevel() {
        LoadSave.saveLevel("defaultleveltest1",level);
    }

    public void setDrawSelected(boolean drawSelected) {
        this.drawSelected = drawSelected;
    }

    public TileManager getTileManager() {
        return tileManager;
    }

    public void setSelectedTile(Tile selectedTile) {
        this.selectedTile = selectedTile;
        drawSelected = true;
    }

    @Override
    public void mouseClicked(int x, int y) {
        if( x >= GameDimensions.GAME_WIDTH){
            editTiles.mouseClicked(x,y);
        }
        else {
            if (editTiles.getCurrentMode().equals("Erase")) {
                eraseTile(x, y);
            } else {
                modifyTile(x, y);
            }
        }
    }

    @Override
    public void mouseMoved(int x, int y) {
        if( x >= GameDimensions.GAME_WIDTH){
            editTiles.mouseMoved(x,y);
            drawSelected = false;
        }
        else{
            mouseX = x / 64;
            mouseY = y / 64;
            mouseX *= 64;
            mouseY *= 64;
            drawSelected = true;
        }
    }

    @Override
    public void mousePressed(int x, int y) {
        if( x >= GameDimensions.GAME_WIDTH){
            editTiles.mousePressed(x,y);
        }
    }

    @Override
    public void mouseReleased(int x, int y) {
        editTiles.mouseReleased(x,y);
    }

    @Override
    public void mouseDragged(int x, int y) {
        if (x < GameDimensions.GAME_WIDTH && y < GameDimensions.GAME_HEIGHT) {
            changeTile(x, y);
        }
    }

    private void changeTile(int x, int y) {
        if (selectedTile != null) {
            int tileX = x / GameDimensions.TILE_DISPLAY_SIZE;
            int tileY = y / GameDimensions.TILE_DISPLAY_SIZE;

            if (lastTileX == tileX && lastTileY == tileY && lastTileId == selectedTile.getId()) {
                return;
            }

            if (!selectedTile.getName().contains("Road")) {
                return;
            }

            int dx = tileX - prevDraggedTileX;
            int dy = tileY - prevDraggedTileY;

            if (dx != 0 || dy != 0) {
                Tile tileToPlace = getAutoConvertedTile(selectedTile, dx, dy);

                level[tileY][tileX] = tileToPlace.getId();
                lastTileId = tileToPlace.getId();
            }

            lastTileX = tileX;
            lastTileY = tileY;
            prevDraggedTileX = tileX;
            prevDraggedTileY = tileY;
        }
    }

    private Tile getAutoConvertedTile(Tile originalTile, int dx, int dy) {
        String name = originalTile.getName();

        if (!name.startsWith("CurvedRoad")) {
            return originalTile;
        }

        if (dx == 1 && dy == 0) {
            return tileManager.FlatRoadHorizontal;
        } else if (dx == -1 && dy == 0) {
            return tileManager.FlatRoadHorizontal;
        } else if (dx == 0 && dy == 1) {
            return tileManager.FlatRoadVertical;
        } else if (dx == 0 && dy == -1) {
            return tileManager.FlatRoadVertical;
        }

        return originalTile;
    }

}
