package models;

import static constants.Constants.PathPoints.*;
import helpMethods.LoadSave;
import managers.TileManager;
import objects.Tile;
import observers.MapChangeObserver;
import observers.MapChangeType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * MapModel - Handles all map data and business logic
 * Responsibilities:
 * - Map data storage (level, overlay)
 * - Tile placement validation
 * - Castle placement logic
 * - Road logic and pathfinding
 * - Save/Load operations
 * - Observer pattern for notifications
 */
public class MapModel {
    
    // Core map data
    private int[][] level;
    private int[][] overlayData;
    private String currentLevelName;
    
    // Managers and utilities
    private TileManager tileManager;
    
    // Observer pattern for notifications
    private List<MapChangeObserver> observers = new ArrayList<>();
    
    // Constants
    private static final int GRASS_TILE_ID = 5;
    private static final int WALL_ID = -3;
    private static final int GATE_ID = -4;
    
    public MapModel(int width, int height) {
        this.level = new int[height][width];
        this.overlayData = new int[height][width];
        this.tileManager = new TileManager();
        initializeMap();
    }
    
    public MapModel(int[][] existingLevel) {
        this.level = existingLevel;
        this.overlayData = new int[level.length][level[0].length];
        this.tileManager = new TileManager();
    }
    
    private void initializeMap() {
        // Initialize with grass tiles
        for (int i = 0; i < level.length; i++) {
            for (int j = 0; j < level[i].length; j++) {
                level[i][j] = GRASS_TILE_ID;
                overlayData[i][j] = NO_OVERLAY;
            }
        }
    }
    
    // Observer pattern methods
    public void addObserver(MapChangeObserver observer) {
        observers.add(observer);
    }
    
    public void removeObserver(MapChangeObserver observer) {
        observers.remove(observer);
    }
    
    private void notifyObservers(MapChangeType changeType, int x, int y) {
        for (MapChangeObserver observer : observers) {
            observer.onMapChanged(changeType, x, y, this);
        }
    }
    
    // Tile placement validation
    public boolean isValidTilePlacement(int x, int y, Tile tile) {
        if (!isWithinBounds(x, y)) return false;
        
        if (tile.getId() == -1 || tile.getId() == -2) { // Start/End points
            return isRoadTile(level[y][x]);
        }
        
        if (tile.getName().equals("Castle")) {
            return isValidCastlePlacement(y, x);
        }
        
        return true;
    }
    
    public PlacementResult placeTile(int x, int y, Tile tile) {
        if (!isValidTilePlacement(x, y, tile)) {
            return PlacementResult.invalid(getPlacementErrorMessage(x, y, tile));
        }
        
        if (tile.getId() == -1) { // Start point
            return placeStartPoint(x, y);
        } else if (tile.getId() == -2) { // End point
            return placeEndPoint(x, y);
        } else if (tile.getName().equals("Castle")) {
            return placeCastle(x, y);
        } else {
            return placeRegularTile(x, y, tile);
        }
    }
    
    private PlacementResult placeStartPoint(int x, int y) {
        // Clear existing start points
        clearOverlayType(START_POINT);
        overlayData[y][x] = START_POINT;
        notifyObservers(MapChangeType.OVERLAY_CHANGED, x, y);
        return PlacementResult.success();
    }
    
    private PlacementResult placeEndPoint(int x, int y) {
        // Store previous end point location for wall cleanup
        int[] prevEndPoint = findOverlayPosition(END_POINT);
        
        // Clear existing end points
        clearOverlayType(END_POINT);
        
        // Remove previous walls if they existed
        if (prevEndPoint != null) {
            clearWallsAndGate(prevEndPoint[0], prevEndPoint[1]);
        }
        
        // Place new end point
        overlayData[y][x] = END_POINT;
        addWallsAndGateAroundEndPoint(x, y);
        
        notifyObservers(MapChangeType.OVERLAY_CHANGED, x, y);
        return PlacementResult.success();
    }
    
    private PlacementResult placeCastle(int x, int y) {
        clearOverlappingCastles(y, x);
        
        // Place 2x2 castle
        level[y][x] = tileManager.CastleTopLeft.getId();
        level[y][x + 1] = tileManager.CastleTopRight.getId();
        level[y + 1][x] = tileManager.CastleBottomLeft.getId();
        level[y + 1][x + 1] = tileManager.CastleBottomRight.getId();
        
        // Clear overlays
        overlayData[y][x] = NO_OVERLAY;
        overlayData[y][x + 1] = NO_OVERLAY;
        overlayData[y + 1][x] = NO_OVERLAY;
        overlayData[y + 1][x + 1] = NO_OVERLAY;
        
        notifyObservers(MapChangeType.TERRAIN_CHANGED, x, y);
        return PlacementResult.success();
    }
    
    private PlacementResult placeRegularTile(int x, int y, Tile tile) {
        // Check if placing on castle - clear it first
        if (isCastleTile(level[y][x])) {
            clearCastleAtPosition(y, x);
        }
        
        level[y][x] = tile.getId();
        overlayData[y][x] = NO_OVERLAY;
        
        notifyObservers(MapChangeType.TERRAIN_CHANGED, x, y);
        return PlacementResult.success();
    }
    
    public void eraseTile(int x, int y) {
        if (!isWithinBounds(x, y)) return;
        
        if (isCastleTile(level[y][x])) {
            clearCastleAtPosition(y, x);
        } else {
            level[y][x] = GRASS_TILE_ID;
            overlayData[y][x] = NO_OVERLAY;
        }
        
        notifyObservers(MapChangeType.TERRAIN_CHANGED, x, y);
    }
    
    public void fillAllTiles(Tile tile) {
        clearAllOverlays();
        
        if (tile.getName().equals("Castle")) {
            fillWithCastles();
        } else if (tile.getId() != -1 && tile.getId() != -2) {
            fillWithRegularTile(tile);
        }
        
        notifyObservers(MapChangeType.FULL_MAP_CHANGED, -1, -1);
    }
    
    public void resetAllTiles() {
        initializeMap();
        notifyObservers(MapChangeType.FULL_MAP_CHANGED, -1, -1);
    }
    
    // Utility methods
    private boolean isWithinBounds(int x, int y) {
        return x >= 0 && x < level[0].length && y >= 0 && y < level.length;
    }
    
    private boolean isRoadTile(int tileId) {
        return tileId >= 0 && tileId <= 14 && tileId != GRASS_TILE_ID;
    }
    
    private boolean isCastleTile(int tileId) {
        return tileId == tileManager.CastleTopLeft.getId() ||
               tileId == tileManager.CastleTopRight.getId() ||
               tileId == tileManager.CastleBottomLeft.getId() ||
               tileId == tileManager.CastleBottomRight.getId();
    }
    
    private boolean isValidCastlePlacement(int topLeftY, int topLeftX) {
        return topLeftY + 1 < level.length && topLeftX + 1 < level[0].length;
    }
    
    // Helper methods for complex operations
    private void clearOverlayType(int overlayType) {
        for (int i = 0; i < overlayData.length; i++) {
            for (int j = 0; j < overlayData[i].length; j++) {
                if (overlayData[i][j] == overlayType) {
                    overlayData[i][j] = NO_OVERLAY;
                }
            }
        }
    }
    
    private int[] findOverlayPosition(int overlayType) {
        for (int i = 0; i < overlayData.length; i++) {
            for (int j = 0; j < overlayData[i].length; j++) {
                if (overlayData[i][j] == overlayType) {
                    return new int[]{j, i}; // x, y
                }
            }
        }
        return null;
    }
    
    private void clearOverlappingCastles(int topLeftY, int topLeftX) {
        Set<String> clearedPositions = new HashSet<>();
        
        for (int i = 0; i <= 1; i++) {
            for (int j = 0; j <= 1; j++) {
                if (topLeftY + i >= level.length || topLeftX + j >= level[0].length) continue;
                
                if (isCastleTile(level[topLeftY + i][topLeftX + j])) {
                    int[] castleTopLeft = findCastleTopLeft(topLeftY + i, topLeftX + j);
                    String posKey = castleTopLeft[1] + "," + castleTopLeft[0];
                    
                    if (!clearedPositions.contains(posKey)) {
                        clearedPositions.add(posKey);
                        clearCastleAt(castleTopLeft[1], castleTopLeft[0]);
                    }
                }
            }
        }
    }
    
    private int[] findCastleTopLeft(int y, int x) {
        int tileId = level[y][x];
        int topLeftY = y, topLeftX = x;
        
        if (tileId == tileManager.CastleTopRight.getId()) {
            topLeftX--;
        } else if (tileId == tileManager.CastleBottomLeft.getId()) {
            topLeftY--;
        } else if (tileId == tileManager.CastleBottomRight.getId()) {
            topLeftY--;
            topLeftX--;
        }
        
        return new int[]{topLeftX, topLeftY};
    }
    
    private void clearCastleAtPosition(int y, int x) {
        int[] topLeft = findCastleTopLeft(y, x);
        clearCastleAt(topLeft[1], topLeft[0]);
    }
    
    private void clearCastleAt(int topLeftY, int topLeftX) {
        if (topLeftY >= 0 && topLeftY + 1 < level.length &&
            topLeftX >= 0 && topLeftX + 1 < level[0].length) {
            
            level[topLeftY][topLeftX] = GRASS_TILE_ID;
            level[topLeftY][topLeftX + 1] = GRASS_TILE_ID;
            level[topLeftY + 1][topLeftX] = GRASS_TILE_ID;
            level[topLeftY + 1][topLeftX + 1] = GRASS_TILE_ID;
        }
    }
    
    private void addWallsAndGateAroundEndPoint(int x, int y) {
        boolean isOnLeftEdge = (x == 0);
        boolean isOnRightEdge = (x == level[0].length - 1);
        boolean isOnTopEdge = (y == 0);
        boolean isOnBottomEdge = (y == level.length - 1);
        
        if (!isOnLeftEdge && !isOnRightEdge && !isOnTopEdge && !isOnBottomEdge) {
            return;
        }
        
        if (isOnLeftEdge) {
            placeWallsOnEdge(0, 0, 0, level.length - 1, true, y);
        } else if (isOnRightEdge) {
            placeWallsOnEdge(level[0].length - 1, 0, level[0].length - 1, level.length - 1, true, y);
        } else if (isOnTopEdge) {
            placeWallsOnEdge(0, 0, level[0].length - 1, 0, false, x);
        } else if (isOnBottomEdge) {
            placeWallsOnEdge(0, level.length - 1, level[0].length - 1, level.length - 1, false, x);
        }
    }
    
    private void placeWallsOnEdge(int startX, int startY, int endX, int endY, boolean vertical, int gatePos) {
        if (vertical) {
            for (int i = startY; i <= endY; i++) {
                level[i][startX] = (i == gatePos) ? GATE_ID : WALL_ID;
            }
        } else {
            for (int j = startX; j <= endX; j++) {
                level[startY][j] = (j == gatePos) ? GATE_ID : WALL_ID;
            }
        }
    }
    
    private void clearWallsAndGate(int x, int y) {
        // Implementation for clearing walls when endpoint moves
        // Similar logic to addWallsAndGateAroundEndPoint but clearing
    }
    
    private void clearAllOverlays() {
        for (int i = 0; i < overlayData.length; i++) {
            for (int j = 0; j < overlayData[i].length; j++) {
                overlayData[i][j] = NO_OVERLAY;
            }
        }
    }
    
    private void fillWithCastles() {
        for (int i = 0; i < level.length; i += 2) {
            for (int j = 0; j < level[0].length; j += 2) {
                if (i + 1 < level.length && j + 1 < level[0].length) {
                    level[i][j] = tileManager.CastleTopLeft.getId();
                    level[i][j + 1] = tileManager.CastleTopRight.getId();
                    level[i + 1][j] = tileManager.CastleBottomLeft.getId();
                    level[i + 1][j + 1] = tileManager.CastleBottomRight.getId();
                }
            }
        }
    }
    
    private void fillWithRegularTile(Tile tile) {
        for (int i = 0; i < level.length; i++) {
            for (int j = 0; j < level[i].length; j++) {
                level[i][j] = tile.getId();
            }
        }
    }
    
    private String getPlacementErrorMessage(int x, int y, Tile tile) {
        if (tile.getId() == -1) {
            return "Start point must be placed on a road!";
        } else if (tile.getId() == -2) {
            return "End point must be placed on a road!";
        }
        return "Invalid tile placement!";
    }
    
    // Save/Load operations
    public void saveLevel(String filename) {
        LoadSave.saveLevel(filename, level);
        LoadSave.saveOverlay(filename, overlayData);
        this.currentLevelName = filename;
    }
    
    public void loadLevel(String filename) {
        int[][] loadedLevel = LoadSave.getLevelData(filename);
        if (loadedLevel != null) {
            this.level = loadedLevel;
            this.overlayData = new int[level.length][level[0].length];
            // Try to load overlay data
            // LoadSave.getOverlayData(filename) - implement if needed
            this.currentLevelName = filename;
            notifyObservers(MapChangeType.FULL_MAP_CHANGED, -1, -1);
        }
    }
    
    // Getters
    public int[][] getLevel() { return level; }
    public int[][] getOverlayData() { return overlayData; }
    public String getCurrentLevelName() { return currentLevelName; }
    public TileManager getTileManager() { return tileManager; }
    public int getWidth() { return level[0].length; }
    public int getHeight() { return level.length; }
    public int getTileAt(int x, int y) { 
        return isWithinBounds(x, y) ? level[y][x] : -1; 
    }
    public int getOverlayAt(int x, int y) { 
        return isWithinBounds(x, y) ? overlayData[y][x] : NO_OVERLAY; 
    }
    
    // Setters
    public void setCurrentLevelName(String name) { 
        this.currentLevelName = name; 
    }
    
    public void setLevel(int[][] level) {
        this.level = level;
        this.overlayData = new int[level.length][level[0].length];
        notifyObservers(MapChangeType.FULL_MAP_CHANGED, -1, -1);
    }
    
    public void setOverlayData(int[][] overlayData) {
        this.overlayData = overlayData;
        notifyObservers(MapChangeType.OVERLAY_CHANGED, -1, -1);
    }
} 