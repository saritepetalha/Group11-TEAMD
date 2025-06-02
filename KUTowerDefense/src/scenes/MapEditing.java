package scenes;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.Window;
import java.awt.image.BufferedImage;

import static constants.Constants.PathPoints.END_POINT;
import static constants.Constants.PathPoints.NO_OVERLAY;
import static constants.Constants.PathPoints.START_POINT;
import constants.GameDimensions;
import helpMethods.LevelBuilder;
import helpMethods.LoadSave;
import main.Game;
import managers.TileManager;
import objects.Tile;
import objects.Tower;
import ui_p.AssetsLoader;
import ui_p.EditTiles;


// a class to edit map. map editor part on the main screen.

public class MapEditing extends GameScene implements SceneMethods{

    private int[][] level;           // main level data with terrain/road IDs
    private int[][] overlayData;     // new array to store start/end point data
    private TileManager tileManager;
    private Tile selectedTile;
    private boolean drawSelected = false;
    private EditTiles editTiles;
    private int lastTileX, lastTileY, lastTileId, prevDraggedTileX, prevDraggedTileY;
    private String currentLevelName;

    private final Window owner;
    private int mouseX, mouseY;

    private Tower selectedTower;

    private BufferedImage wallImage;
    private BufferedImage gateImage;

    public MapEditing(Game game, Window owner) {
        super(game);
        this.owner = owner;
        level = LevelBuilder.getLevelData();
        overlayData = new int[level.length][level[0].length]; // initialize the overlay with the same dimensions as the level
        tileManager = new TileManager();
        editTiles = new EditTiles(GameDimensions.GAME_WIDTH,0,4*GameDimensions.ButtonSize.MEDIUM.getSize(), GameDimensions.GAME_HEIGHT,this, game, owner);
        createDefaultLevel();
        loadDefaultLevel();
        loadBorderImages();
    }

    private void loadBorderImages() {
        try {
            wallImage = LoadSave.getImageFromPath("/Borders/wall.png");
            gateImage = LoadSave.getImageFromPath("/Borders/gate.png");
            System.out.println("Border images loaded successfully");
        } catch (Exception e) {
            System.err.println("Error loading border images: " + e.getMessage());
            e.printStackTrace();
        }
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
        int[][] lvl = LoadSave.loadLevel("defaultleveltest1");
        //THIS LINE IS JUST TO SEE WHETHER THE BACKEND OF THE loadLevel function works or not
        //IT WORKS!!!
        System.out.println(java.util.Arrays.deepToString(lvl));
    }

    private void drawMap(Graphics g) {
        g.setColor(new Color(134,177,63,255));
        g.fillRect(0, 0, GameDimensions.GAME_WIDTH, GameDimensions.GAME_HEIGHT);

        int rowCount = level.length;
        int colCount = level[0].length;

        // Detect which edge contains the gate (endpoint)
        int gateEdge = -1; // 0=top, 1=bottom, 2=left, 3=right
        for (int i = 0; i < rowCount; i++) {
            if (level[i][0] == -4) gateEdge = 2; // left
            if (level[i][colCount - 1] == -4) gateEdge = 3; // right
        }
        for (int j = 0; j < colCount; j++) {
            if (level[0][j] == -4) gateEdge = 0; // top
            if (level[rowCount - 1][j] == -4) gateEdge = 1; // bottom
        }

        for (int i = 0; i < level.length; i++) {
            for (int j = 0; j < level[i].length; j++) {
                int tileId = level[i][j];

                if (tileId == -3 && wallImage != null) { // Wall
                    BufferedImage img = wallImage;
                    Graphics2D g2d = (Graphics2D) g.create();
                    int x = j * GameDimensions.TILE_DISPLAY_SIZE;
                    int y = i * GameDimensions.TILE_DISPLAY_SIZE;
                    int ts = GameDimensions.TILE_DISPLAY_SIZE;
                    if (gateEdge == 0) { // top
                        g2d.drawImage(img, x, y, ts, ts, null);
                    } else if (gateEdge == 1) { // bottom
                        g2d.drawImage(img, x, y + ts, ts, -ts, null); // flip vertically
                    } else if (gateEdge == 2) { // left
                        g2d.rotate(-Math.PI / 2, x + ts / 2, y + ts / 2);
                        g2d.drawImage(img, x, y, ts, ts, null);
                    } else if (gateEdge == 3) { // right
                        g2d.rotate(Math.PI / 2, x + ts / 2, y + ts / 2);
                        g2d.drawImage(img, x, y, ts, ts, null);
                    } else {
                        g2d.drawImage(img, x, y, ts, ts, null);
                    }
                    g2d.dispose();
                } else if (tileId == -4 && gateImage != null) { // Gate
                    BufferedImage img = gateImage;
                    Graphics2D g2d = (Graphics2D) g.create();
                    int x = j * GameDimensions.TILE_DISPLAY_SIZE;
                    int y = i * GameDimensions.TILE_DISPLAY_SIZE;
                    int ts = GameDimensions.TILE_DISPLAY_SIZE;
                    if (gateEdge == 0) { // top
                        g2d.drawImage(img, x, y, ts, ts, null);
                    } else if (gateEdge == 1) { // bottom
                        g2d.drawImage(img, x, y + ts, ts, -ts, null); // flip vertically
                    } else if (gateEdge == 2) { // left
                        g2d.rotate(-Math.PI / 2, x + ts / 2, y + ts / 2);
                        g2d.drawImage(img, x, y, ts, ts, null);
                    } else if (gateEdge == 3) { // right
                        g2d.rotate(Math.PI / 2, x + ts / 2, y + ts / 2);
                        g2d.drawImage(img, x, y, ts, ts, null);
                    } else {
                        g2d.drawImage(img, x, y, ts, ts, null);
                    }
                    g2d.dispose();
                } else {
                    g.drawImage(tileManager.getSprite(tileId), j * GameDimensions.TILE_DISPLAY_SIZE, i * GameDimensions.TILE_DISPLAY_SIZE, null);
                }

                if (overlayData[i][j] == START_POINT) {
                    drawOverlayImage(g, AssetsLoader.getInstance().startPointImg, j, i);
                } else if (overlayData[i][j] == END_POINT) {
                    drawOverlayImage(g, AssetsLoader.getInstance().endPointImg, j, i);
                }
            }
        }

        drawMapGrid(g);
    }

    private BufferedImage getTransformedBorderImage(BufferedImage img, int i, int j, int rowCount, int colCount) {
        if (i == 0 && j == 0) return rotateImage(img, -90);
        if (i == 0 && j == colCount - 1) return rotateImage(img, 90);
        if (i == rowCount - 1 && j == 0) return rotateImage(img, 90);
        if (i == rowCount - 1 && j == colCount - 1) return rotateImage(img, -90);

        if (i == 0) return img; // üst
        if (i == rowCount - 1) return rotateImage(img, 180);
        if (j == 0) return rotateImage(img, -90);
        if (j == colCount - 1) return rotateImage(img, 90);

        return img;
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
        return rotated;
    }

    private void drawMapGrid(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        g2d.setColor(new Color(40, 40, 40, 30));

        float[] dashPattern = {5, 5};
        BasicStroke dashedStroke = new BasicStroke(
                1,
                BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER,
                10.0f,
                dashPattern,
                0.0f
        );


        Stroke originalStroke = g2d.getStroke();
        g2d.setStroke(dashedStroke);

        for (int x = 0; x <= GameDimensions.GAME_WIDTH; x += GameDimensions.TILE_DISPLAY_SIZE) {
            g2d.drawLine(x, 0, x, GameDimensions.GAME_HEIGHT);
        }

        for (int y = 0; y <= GameDimensions.GAME_HEIGHT; y += GameDimensions.TILE_DISPLAY_SIZE) {
            g2d.drawLine(0, y, GameDimensions.GAME_WIDTH, y);
        }

        g2d.setStroke(originalStroke);
    }

    // Helper method to draw silhouette of an image
    private void drawSilhouette(Graphics g, BufferedImage image, int x, int y, int width, int height) {
        Graphics2D g2d = (Graphics2D) g.create();

        // Create silhouette effect
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));

        // Draw a semi-transparent blue silhouette
        g2d.setColor(new Color(100, 100, 255, 150));
        g2d.fillRect(x, y, width, height);

        // Draw the image with a blue tint
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
        g2d.drawImage(image, x, y, width, height, null);

        g2d.dispose();
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
        setCustomCursor(); // Her render işleminde imleci kontrol et
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

    /**
     * Utility method to check if the 2x2 castle at the given position is valid
     * (within bounds and not overlapping with other castles)
     * @param topLeftY Y coordinate of the top-left corner of the castle
     * @param topLeftX X coordinate of the top-left corner of the castle
     * @return true if the castle placement is valid, false otherwise
     */
    private boolean isValidCastlePlacement(int topLeftY, int topLeftX) {
        // Check if the castle would be in bounds
        if (topLeftY + 1 >= level.length || topLeftX + 1 >= level[0].length) {
            System.out.println("Castle placement rejected: would be out of bounds");
            return false;
        }

        // Castle placement is valid (we'll handle any overlaps by clearing existing castles)
        return true;
    }

    /**
     * Clears any existing castles that would overlap with a new castle placement
     * @param topLeftY Y coordinate of the top-left corner of the new castle
     * @param topLeftX X coordinate of the top-left corner of the new castle
     */
    private void clearOverlappingCastles(int topLeftY, int topLeftX) {
        // Keep track of castle top-left positions we've already cleared to avoid clearing the same one multiple times
        java.util.HashSet<String> clearedPositions = new java.util.HashSet<>();
        boolean clearedAnyCastle = false;

        // Check all four positions of the new castle
        for (int i = 0; i <= 1; i++) {
            for (int j = 0; j <= 1; j++) {
                // Skip if out of bounds
                if (topLeftY + i >= level.length || topLeftX + j >= level[0].length) continue;

                // If this position contains a castle tile
                if (isCastleTile(level[topLeftY + i][topLeftX + j])) {
                    // Find the top-left position of this castle
                    int castleTopLeftY = topLeftY + i;
                    int castleTopLeftX = topLeftX + j;

                    int tileId = level[topLeftY + i][topLeftX + j];

                    // If this is top-right, move left
                    if (tileId == tileManager.CastleTopRight.getId()) {
                        castleTopLeftX--;
                    }
                    // If this is bottom-left, move up
                    else if (tileId == tileManager.CastleBottomLeft.getId()) {
                        castleTopLeftY--;
                    }
                    // If this is bottom-right, move up and left
                    else if (tileId == tileManager.CastleBottomRight.getId()) {
                        castleTopLeftY--;
                        castleTopLeftX--;
                    }

                    // Generate a unique key for this castle's position
                    String posKey = castleTopLeftY + "," + castleTopLeftX;

                    // If we haven't cleared this castle yet
                    if (!clearedPositions.contains(posKey)) {
                        // Mark this castle as cleared
                        clearedPositions.add(posKey);
                        clearedAnyCastle = true;

                        System.out.println("Clearing overlapping castle at: " + castleTopLeftY + "," + castleTopLeftX);

                        // Clear the castle
                        if (castleTopLeftY >= 0 && castleTopLeftY + 1 < level.length &&
                                castleTopLeftX >= 0 && castleTopLeftX + 1 < level[0].length) {

                            level[castleTopLeftY][castleTopLeftX] = 5; // grass
                            level[castleTopLeftY][castleTopLeftX + 1] = 5;
                            level[castleTopLeftY + 1][castleTopLeftX] = 5;
                            level[castleTopLeftY + 1][castleTopLeftX + 1] = 5;

                            // Clear any overlays
                            overlayData[castleTopLeftY][castleTopLeftX] = NO_OVERLAY;
                            overlayData[castleTopLeftY][castleTopLeftX + 1] = NO_OVERLAY;
                            overlayData[castleTopLeftY + 1][castleTopLeftX] = NO_OVERLAY;
                            overlayData[castleTopLeftY + 1][castleTopLeftX + 1] = NO_OVERLAY;
                        }
                    }
                }
            }
        }

        if (clearedAnyCastle) {
            System.out.println("Castle overlap detected and resolved");
        }
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
            int prevEndX = -1, prevEndY = -1;
            for (int i = 0; i < overlayData.length; i++) {
                for (int j = 0; j < overlayData[0].length; j++) {
                    if (overlayData[i][j] == END_POINT) {
                        overlayData[i][j] = NO_OVERLAY;  // rest overlay to no_overlay
                        prevEndX = j;
                        prevEndY = i;
                    }
                }
            }

            // Remove previous walls/gate if previous endpoint existed
            if (prevEndX != -1 && prevEndY != -1) {
                boolean wasOnLeft = (prevEndX == 0);
                boolean wasOnRight = (prevEndX == level[0].length - 1);
                boolean wasOnTop = (prevEndY == 0);
                boolean wasOnBottom = (prevEndY == level.length - 1);
                if (wasOnLeft) {
                    for (int i = 0; i < level.length; i++) {
                        if (level[i][0] == -3 || level[i][0] == -4) level[i][0] = 5;
                    }
                } else if (wasOnRight) {
                    for (int i = 0; i < level.length; i++) {
                        if (level[i][level[0].length - 1] == -3 || level[i][level[0].length - 1] == -4) level[i][level[0].length - 1] = 5;
                    }
                } else if (wasOnTop) {
                    for (int j = 0; j < level[0].length; j++) {
                        if (level[0][j] == -3 || level[0][j] == -4) level[0][j] = 5;
                    }
                } else if (wasOnBottom) {
                    for (int j = 0; j < level[0].length; j++) {
                        if (level[level.length - 1][j] == -3 || level[level.length - 1][j] == -4) level[level.length - 1][j] = 5;
                    }
                }
            }

            // set new end point in overlay
            overlayData[y][x] = END_POINT;
            System.out.println("End point placed at: " + x + "," + y);

            // Add walls and gate around the end point
            addWallsAndGateAroundEndPoint(x, y);

        } else if (selectedTile.getName().equals("Castle")) {
            // place Castle in 2x2 area
            if (isValidCastlePlacement(y, x)) {
                // Clear any existing castles that would overlap with this new castle
                clearOverlappingCastles(y, x);

                // Now place the new castle
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
            // Check if we're about to place a tile on any part of a castle
            if (isCastleTile(level[y][x])) {
                // We're placing on a castle tile, need to clear the whole castle
                clearCastleAtPosition(y, x);
            }

            level[y][x] = selectedTile.getId();
            overlayData[y][x] = NO_OVERLAY;
        }
    }

    /**
     * Checks if a tile is part of a castle
     */
    private boolean isCastleTile(int tileId) {
        return tileId == tileManager.CastleTopLeft.getId() ||
                tileId == tileManager.CastleTopRight.getId() ||
                tileId == tileManager.CastleBottomLeft.getId() ||
                tileId == tileManager.CastleBottomRight.getId();
    }

    /**
     * Clears all tiles of a castle containing the given position
     */
    private void clearCastleAtPosition(int y, int x) {
        int grassTileId = 5; // ID for grass tile

        // Find the top-left position of the castle by checking positions
        int topLeftY = y;
        int topLeftX = x;

        int tileId = level[y][x];

        // If this is top-right, move left
        if (tileId == tileManager.CastleTopRight.getId()) {
            topLeftX--;
        }
        // If this is bottom-left, move up
        else if (tileId == tileManager.CastleBottomLeft.getId()) {
            topLeftY--;
        }
        // If this is bottom-right, move up and left
        else if (tileId == tileManager.CastleBottomRight.getId()) {
            topLeftY--;
            topLeftX--;
        }

        // Replace the whole 2x2 castle with grass
        if (topLeftY >= 0 && topLeftY + 1 < level.length &&
                topLeftX >= 0 && topLeftX + 1 < level[0].length) {

            level[topLeftY][topLeftX] = grassTileId;
            level[topLeftY][topLeftX + 1] = grassTileId;
            level[topLeftY + 1][topLeftX] = grassTileId;
            level[topLeftY + 1][topLeftX + 1] = grassTileId;
        }
    }

    public void eraseTile(int x, int y) {
        x /= 64;
        y /= 64;

        // Check if we're trying to erase a castle tile
        if (isCastleTile(level[y][x])) {
            // We're erasing a castle tile, need to clear the whole castle
            clearCastleAtPosition(y, x);
        } else {
            level[y][x] = 5;
            overlayData[y][x] = NO_OVERLAY;
        }
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

        if (selectedTile.getName().equals("Castle")) {
            // Special case for Castle (2x2 tiles)
            // Fill the map with complete 2x2 castles without overlap
            for (int i = 0; i < level.length; i += 2) {  // Increment by 2 for castle height
                for (int j = 0; j < level[0].length; j += 2) {  // Increment by 2 for castle width
                    if (i + 1 < level.length && j + 1 < level[0].length) {
                        level[i][j] = tileManager.CastleTopLeft.getId();           // top-left
                        level[i][j + 1] = tileManager.CastleTopRight.getId();      // top-right
                        level[i + 1][j] = tileManager.CastleBottomLeft.getId();    // bottom-left
                        level[i + 1][j + 1] = tileManager.CastleBottomRight.getId(); // bottom-right
                    }
                }
            }
        } else {
            // Handle regular 1x1 tiles
            for (int i = 0; i < level.length; i++) {
                for (int j = 0; j < level[i].length; j++) {
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

    public void saveLevel(String filename) {
        LoadSave.saveLevel(filename,level);
        LoadSave.saveOverlay(currentLevelName, overlayData);
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
            editTiles.mouseReleased(x, y);
            mouseX = x / 64;
            mouseY = y / 64;
            mouseX *= 64;
            mouseY *= 64;
            drawSelected = true;
        }
    }

    /**
     * Reset the road tracking when starting a new drag operation
     */
    @Override
    public void mousePressed(int x, int y) {
        if( x >= GameDimensions.GAME_WIDTH){
            editTiles.mousePressed(x,y);
        }
        isFirstTile = true;
        prevDx = 0;
        prevDy = 0;
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

    // Track the previous direction to detect changes
    private int prevDx = 0;
    private int prevDy = 0;
    private boolean isFirstTile = true;

    /**
     * Modified changeTile method with road crossing detection
     */
    private void changeTile(int x, int y) {
        if (selectedTile != null) {
            int tileX = x / GameDimensions.TILE_DISPLAY_SIZE;
            int tileY = y / GameDimensions.TILE_DISPLAY_SIZE;

            // Skip if it's the same tile we just placed
            if (lastTileX == tileX && lastTileY == tileY) {
                return;
            }

            // Special handling for castle drag placement
            if (selectedTile.getName().equals("Castle")) {
                // Check if we can place a 2x2 castle here
                if (isValidCastlePlacement(tileY, tileX)) {
                    // Check and clear any overlapping castles
                    clearOverlappingCastles(tileY, tileX);

                    // Place the 2x2 castle
                    level[tileY][tileX] = tileManager.CastleTopLeft.getId();            // top-left
                    level[tileY][tileX + 1] = tileManager.CastleTopRight.getId();       // top-right
                    level[tileY + 1][tileX] = tileManager.CastleBottomLeft.getId();     // bottom-left
                    level[tileY + 1][tileX + 1] = tileManager.CastleBottomRight.getId(); // bottom-right

                    // Clear any overlays
                    overlayData[tileY][tileX] = NO_OVERLAY;
                    overlayData[tileY][tileX + 1] = NO_OVERLAY;
                    overlayData[tileY + 1][tileX] = NO_OVERLAY;
                    overlayData[tileY + 1][tileX + 1] = NO_OVERLAY;

                    lastTileX = tileX;
                    lastTileY = tileY;
                    prevDraggedTileX = tileX;
                    prevDraggedTileY = tileY;
                    return;
                }
            }

            // Handle eraser tool
            if (editTiles.getCurrentMode().equals("Erase")) {
                if (isCastleTile(level[tileY][tileX])) {
                    clearCastleAtPosition(tileY, tileX);
                } else {
                    level[tileY][tileX] = 5; // grass tile
                    overlayData[tileY][tileX] = NO_OVERLAY;
                }
                lastTileX = tileX;
                lastTileY = tileY;
                prevDraggedTileX = tileX;
                prevDraggedTileY = tileY;
                return;
            }

            // Handle regular tiles and road tiles
            // Check if we're overlapping a castle part
            if (isCastleTile(level[tileY][tileX])) {
                clearCastleAtPosition(tileY, tileX);
            }

            // Only handle road tiles specifically with the existing logic
            if (selectedTile.getName().contains("Road")) {
                // Calculate direction of movement
                int dx = tileX - prevDraggedTileX;
                int dy = tileY - prevDraggedTileY;

                // Only process if we've actually moved to a different tile
                if (dx != 0 || dy != 0) {
                    // Normalize movement to single unit (prevents diagonal placement)
                    if (Math.abs(dx) > Math.abs(dy)) {
                        // Horizontal movement is dominant
                        dx = dx > 0 ? 1 : -1;
                        dy = 0;
                    } else {
                        // Vertical movement is dominant
                        dx = 0;
                        dy = dy > 0 ? 1 : -1;
                    }

                    // Place appropriate tile based on current and previous direction
                    Tile tileToPlace;

                    // Check if we're crossing an existing road
                    Tile existingTile = tileManager.getTile(level[tileY][tileX]);
                    boolean isRoadTile = existingTile != null && existingTile.getName().contains("Road");

                    if (isRoadTile) {
                        // We're crossing an existing road, determine what type of crossing
                        tileToPlace = handleRoadCrossing(existingTile, dx, dy);
                    } else if (isFirstTile) {
                        // First tile placement - use the selected tile type
                        tileToPlace = getBasicRoadTile(dx, dy);
                        isFirstTile = false;
                    } else if (dx != prevDx || dy != prevDy) {
                        // Direction changed - place a curve
                        tileToPlace = getCurvedRoadTile(prevDx, prevDy, dx, dy);

                        // If there was a valid previous tile, we might need to adjust the previous tile
                        if (prevDraggedTileX >= 0 && prevDraggedTileY >= 0) {
                            // Only update the previous tile if it was a straight road
                            String prevTileName = tileManager.getTile(level[prevDraggedTileY][prevDraggedTileX]).getName();
                            if (prevTileName.contains("FlatRoad")) {
                                // Place the curve at the previous position
                                level[prevDraggedTileY][prevDraggedTileX] = tileToPlace.getId();

                                // And continue with a straight road in the new direction
                                tileToPlace = getBasicRoadTile(dx, dy);
                            }
                        }
                    } else {
                        // Continuing in the same direction - place a straight road
                        tileToPlace = getBasicRoadTile(dx, dy);
                    }

                    // Place the tile
                    level[tileY][tileX] = tileToPlace.getId();
                    lastTileId = tileToPlace.getId();

                    // Store current position and direction for next time
                    prevDx = dx;
                    prevDy = dy;
                }
            } else {
                // Handle regular non-road, non-castle tiles
                level[tileY][tileX] = selectedTile.getId();
                overlayData[tileY][tileX] = NO_OVERLAY;
            }

            lastTileX = tileX;
            lastTileY = tileY;
            prevDraggedTileX = tileX;
            prevDraggedTileY = tileY;
        }
    }

    /**
     * Handles the case when we're crossing an existing road
     * Returns the appropriate tile based on the existing road and the new direction
     */
    private Tile handleRoadCrossing(Tile existingTile, int dx, int dy) {
        String tileName = existingTile.getName();

        // Already a four-way crossing, keep it that way
        if (tileName.contains("FourWay")) {
            return tileManager.RoadFourWay;
        }

        // Check for horizontal crossing vertical
        if ((tileName.contains("FlatRoadHorizontal") && dy != 0) ||
                (tileName.contains("FlatRoadVertical") && dx != 0)) {
            return tileManager.RoadFourWay;
        }

        // For curved roads, it's a bit more complex - we need to check if the new direction
        // would create a three-way or four-way intersection
        if (tileName.contains("CurvedRoad")) {
            // For simplicity, we'll make any crossing of a curved road a four-way intersection
            // You could add more complex logic here to handle T-junctions if needed
            return tileManager.RoadFourWay;
        }

        // If we can't determine a crossing, just use the basic road tile
        return getBasicRoadTile(dx, dy);
    }

    /**
     * Returns a basic straight road tile based on the direction
     */
    private Tile getBasicRoadTile(int dx, int dy) {
        if (dx != 0) {
            return tileManager.FlatRoadHorizontal;
        } else {
            return tileManager.FlatRoadVertical;
        }
    }

    /**
     * Returns the appropriate curved road tile based on the direction change
     * oldDx/oldDy: The previous direction
     * newDx/newDy: The new direction
     */
    private Tile getCurvedRoadTile(int oldDx, int oldDy, int newDx, int newDy) {
        // Right to Down
        if (oldDx == 1 && oldDy == 0 && newDx == 0 && newDy == 1) {
            return tileManager.CurvedRoadRightDown;
            //also UpLeft
        }
        // Right to Up
        else if (oldDx == 1 && oldDy == 0 && newDx == 0 && newDy == -1) {
            return tileManager.CurvedRoadRightUp;
            //also DownLeft
        }
        // Left to Down
        else if (oldDx == -1 && oldDy == 0 && newDx == 0 && newDy == 1) {
            return tileManager.CurvedRoadLeftDown;
            //also UpRight
        }
        // Left to Up
        else if (oldDx == -1 && oldDy == 0 && newDx == 0 && newDy == -1) {
            return tileManager.CurvedRoadLeftUp;
            //also DownRight
        }
        // Down to Right
        else if (oldDx == 0 && oldDy == 1 && newDx == 1 && newDy == 0) {
            return tileManager.CurvedRoadLeftUp;
        }
        // Down to Left
        else if (oldDx == 0 && oldDy == 1 && newDx == -1 && newDy == 0) {
            return tileManager.CurvedRoadRightUp;
        }
        // Up to Right
        else if (oldDx == 0 && oldDy == -1 && newDx == 1 && newDy == 0) {
            return tileManager.CurvedRoadLeftDown;
        }
        // Up to Left
        else if (oldDx == 0 && oldDy == -1 && newDx == -1 && newDy == 0) {
            return tileManager.CurvedRoadRightDown;
        }

        // Fallback to a straight road if something unexpected happens
        return getBasicRoadTile(newDx, newDy);
    }

    public void setLevel(int[][] level) {
        this.level = level;
    }

    public void setOverlayData(int[][] overlayData) {
        this.overlayData = overlayData;
    }

    public void setCurrentLevelName(String name) {
        this.currentLevelName = name;
    }

    public String getCurrentLevelName() {
        return this.currentLevelName;
    }

    private void addWallsAndGateAroundEndPoint(int x, int y) {
        // Determine which edge the end point is on
        boolean isOnLeftEdge = (x == 0);
        boolean isOnRightEdge = (x == level[0].length - 1);
        boolean isOnTopEdge = (y == 0);
        boolean isOnBottomEdge = (y == level.length - 1);

        // If the end point is not on an edge, do nothing
        if (!isOnLeftEdge && !isOnRightEdge && !isOnTopEdge && !isOnBottomEdge) {
            System.out.println("End point is not on an edge, not adding walls and gate");
            return;
        }

        System.out.println("Adding walls and gate around end point at " + x + "," + y);

        // Define wall and gate IDs
        int wallId = -3;
        int gateId = -4;

        // Place walls and gate based on which edge the end point is on
        if (isOnLeftEdge) {
            // Place walls on the left edge (except at the end point)
            for (int i = 0; i < level.length; i++) {
                if (i != y) {
                    // Place wall
                    level[i][0] = wallId;
                } else {
                    // Place gate at end point
                    level[i][0] = gateId;
                }
            }
        } else if (isOnRightEdge) {
            // Place walls on the right edge (except at the end point)
            for (int i = 0; i < level.length; i++) {
                if (i != y) {
                    // Place wall
                    level[i][level[0].length - 1] = wallId;
                } else {
                    // Place gate at end point
                    level[i][level[0].length - 1] = gateId;
                }
            }
        } else if (isOnTopEdge) {
            // Place walls on the top edge (except at the end point)
            for (int j = 0; j < level[0].length; j++) {
                if (j != x) {
                    // Place wall
                    level[0][j] = wallId;
                } else {
                    // Place gate at end point
                    level[0][j] = gateId;
                }
            }
        } else if (isOnBottomEdge) {
            // Place walls on the bottom edge (except at the end point)
            for (int j = 0; j < level[0].length; j++) {
                if (j != x) {
                    // Place wall
                    level[level.length - 1][j] = wallId;
                } else {
                    // Place gate at end point
                    level[level.length - 1][j] = gateId;
                }
            }
        }

        System.out.println("Walls and gate added successfully");
    }

    private boolean isCorner(int i, int j, int rowCount, int colCount) {
        return (i == 0 || i == rowCount - 1) && (j == 0 || j == colCount - 1);
    }

    private int getCornerStraightRotation(int i, int j, int[][] level) {
        boolean right = (j + 1 < level[0].length) && (level[i][j + 1] == -3);
        boolean left = (j - 1 >= 0) && (level[i][j - 1] == -3);
        boolean up = (i - 1 >= 0) && (level[i - 1][j] == -3);
        boolean down = (i + 1 < level.length) && (level[i + 1][j] == -3);
        if (right) return 0;
        if (down) return 90;
        if (left) return 180;
        if (up) return 270;
        return 0;
    }

}
