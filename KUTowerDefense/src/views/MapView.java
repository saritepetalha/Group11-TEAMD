package views;

import constants.GameDimensions;
import static constants.Constants.PathPoints.*;
import helpMethods.BorderImageRotationGenerator;
import models.MapModel;
import objects.Tile;
import observers.MapChangeObserver;
import observers.MapChangeType;
import ui_p.AssetsLoader;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * MapView - Handles all rendering and visual presentation
 * Responsibilities:
 * - Map rendering (terrain, overlays, grid)
 * - Selected tile preview
 * - Popup messages
 * - Visual effects and animations
 * - Observer of model changes for UI updates
 */
public class MapView implements MapChangeObserver {
    
    private MapModel mapModel;
    
    // Visual state
    private Tile selectedTile;
    private boolean drawSelected = false;
    private int mouseX, mouseY;
    
    // Popup message system
    private String popupMessage = "";
    private int popupX = 0;
    private int popupY = 0;
    private long popupStartTime = 0;
    private static final long POPUP_DURATION = 2000; // 2 seconds
    
    // Performance optimizations
    private boolean needsFullRedraw = true;
    private BufferedImage cachedMapImage;
    
    public MapView(MapModel mapModel) {
        this.mapModel = mapModel;
        this.mapModel.addObserver(this);
        initializeCachedImage();
    }
    
    private void initializeCachedImage() {
        cachedMapImage = new BufferedImage(
            GameDimensions.GAME_WIDTH, 
            GameDimensions.GAME_HEIGHT, 
            BufferedImage.TYPE_INT_ARGB
        );
        needsFullRedraw = true;
    }
    
    @Override
    public void onMapChanged(MapChangeType changeType, int x, int y, MapModel model) {
        switch (changeType) {
            case FULL_MAP_CHANGED:
                needsFullRedraw = true;
                break;
            case TERRAIN_CHANGED:
            case OVERLAY_CHANGED:
                // Could implement partial updates here for performance
                needsFullRedraw = true;
                break;
        }
    }
    
    /**
     * Main render method - called every frame
     */
    public void render(Graphics g) {
        if (needsFullRedraw) {
            redrawCachedMap();
            needsFullRedraw = false;
        }
        
        // Draw cached map
        g.drawImage(cachedMapImage, 0, 0, null);
        
        // Draw dynamic elements
        drawSelectedTile(g);
        drawPopupMessage(g);
    }
    
    /**
     * Redraws the entire map to the cached image
     */
    private void redrawCachedMap() {
        Graphics2D g2d = cachedMapImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Clear background
        g2d.setColor(new Color(134, 177, 63, 255)); // Game background color
        g2d.fillRect(0, 0, GameDimensions.GAME_WIDTH, GameDimensions.GAME_HEIGHT);
        
        drawMapTiles(g2d);
        drawMapOverlays(g2d);
        drawMapGrid(g2d);
        
        g2d.dispose();
    }
    
    /**
     * Draws the map tiles (terrain)
     */
    private void drawMapTiles(Graphics2D g2d) {
        int[][] level = mapModel.getLevel();
        int gateEdge = BorderImageRotationGenerator.getInstance().detectGateEdge(level);
        
        for (int i = 0; i < level.length; i++) {
            for (int j = 0; j < level[i].length; j++) {
                int tileId = level[i][j];
                int x = j * GameDimensions.TILE_DISPLAY_SIZE;
                int y = i * GameDimensions.TILE_DISPLAY_SIZE;
                int ts = GameDimensions.TILE_DISPLAY_SIZE;
                
                BufferedImage tileImage = getTileImage(tileId, gateEdge);
                if (tileImage != null) {
                    g2d.drawImage(tileImage, x, y, ts, ts, null);
                }
            }
        }
    }
    
    /**
     * Gets the appropriate image for a tile ID
     */
    private BufferedImage getTileImage(int tileId, int gateEdge) {
        if (tileId == -3) { // Wall
            return BorderImageRotationGenerator.getInstance().getRotatedBorderImage(true, gateEdge);
        } else if (tileId == -4) { // Gate
            return BorderImageRotationGenerator.getInstance().getRotatedBorderImage(false, gateEdge);
        } else {
            return mapModel.getTileManager().getSprite(tileId);
        }
    }
    
    /**
     * Draws overlay elements (start/end points)
     */
    private void drawMapOverlays(Graphics2D g2d) {
        int[][] overlayData = mapModel.getOverlayData();
        
        for (int i = 0; i < overlayData.length; i++) {
            for (int j = 0; j < overlayData[i].length; j++) {
                if (overlayData[i][j] == START_POINT) {
                    drawOverlayImage(g2d, AssetsLoader.getInstance().startPointImg, j, i);
                } else if (overlayData[i][j] == END_POINT) {
                    drawOverlayImage(g2d, AssetsLoader.getInstance().endPointImg, j, i);
                }
            }
        }
    }
    
    /**
     * Draws a single overlay image with transparency
     */
    private void drawOverlayImage(Graphics2D g2d, BufferedImage image, int tileX, int tileY) {
        Composite originalComposite = g2d.getComposite();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
        
        g2d.drawImage(image,
                tileX * GameDimensions.TILE_DISPLAY_SIZE,
                tileY * GameDimensions.TILE_DISPLAY_SIZE,
                GameDimensions.TILE_DISPLAY_SIZE,
                GameDimensions.TILE_DISPLAY_SIZE, null);
        
        g2d.setComposite(originalComposite);
    }
    
    /**
     * Draws the map grid
     */
    private void drawMapGrid(Graphics2D g2d) {
        g2d.setColor(new Color(40, 40, 40, 30));
        
        float[] dashPattern = {5, 5};
        BasicStroke dashedStroke = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dashPattern, 0.0f);
        Stroke originalStroke = g2d.getStroke();
        g2d.setStroke(dashedStroke);
        
        // Vertical lines
        for (int x = 0; x <= GameDimensions.GAME_WIDTH; x += GameDimensions.TILE_DISPLAY_SIZE) {
            g2d.drawLine(x, 0, x, GameDimensions.GAME_HEIGHT);
        }
        
        // Horizontal lines
        for (int y = 0; y <= GameDimensions.GAME_HEIGHT; y += GameDimensions.TILE_DISPLAY_SIZE) {
            g2d.drawLine(0, y, GameDimensions.GAME_WIDTH, y);
        }
        
        g2d.setStroke(originalStroke);
    }
    
    /**
     * Draws the selected tile preview
     */
    private void drawSelectedTile(Graphics g) {
        if (selectedTile == null || !drawSelected) return;
        
        int tileSize = GameDimensions.TILE_DISPLAY_SIZE;
        BufferedImage spriteToDraw;
        
        if (selectedTile.getName().equals("Castle")) {
            spriteToDraw = mapModel.getTileManager().getFullCastleSprite();
            g.drawImage(spriteToDraw, mouseX, mouseY, tileSize * 2, tileSize * 2, null);
        } else {
            spriteToDraw = selectedTile.getSprite();
            
            // Semi-transparent for start/end points
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
    
    /**
     * Enhanced popup message rendering
     */
    private void drawPopupMessage(Graphics g) {
        if (popupMessage.isEmpty()) return;
        
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - popupStartTime;
        
        if (elapsedTime > POPUP_DURATION) {
            popupMessage = "";
            return;
        }
        
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        Font originalFont = g2d.getFont();
        Font popupFont = new Font(originalFont.getName(), Font.BOLD, 12);
        g2d.setFont(popupFont);
        
        FontMetrics fm = g2d.getFontMetrics();
        
        int maxTextWidth = 160;
        int padding = 12;
        int iconSize = 14;
        int iconPadding = 6;
        
        List<String> lines = wrapText(popupMessage, fm, maxTextWidth);
        
        // Calculate dimensions
        int textHeight = fm.getHeight();
        int totalTextHeight = lines.size() * textHeight;
        int maxLineWidth = getMaxLineWidth(lines, fm);
        
        int textAreaWidth = maxLineWidth + iconSize + iconPadding;
        int boxWidth = textAreaWidth + 2 * padding;
        int boxHeight = Math.max(totalTextHeight, iconSize) + 2 * padding;
        
        // Position within screen bounds
        int x = Math.min(popupX, GameDimensions.GAME_WIDTH - boxWidth);
        int y = Math.max(popupY - boxHeight, 0);
        
        // Fade effect
        float fadeAlpha = calculateFadeAlpha(elapsedTime);
        
        // Draw popup
        drawPopupBackground(g2d, x, y, boxWidth, boxHeight, fadeAlpha);
        drawPopupIcon(g2d, x + padding, y + padding + (boxHeight - 2 * padding - iconSize) / 2, iconSize, fadeAlpha);
        drawPopupText(g2d, lines, x + padding + iconSize + iconPadding, y + padding + fm.getAscent(), textHeight, fadeAlpha);
        
        g2d.setFont(originalFont);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT);
    }
    
    private List<String> wrapText(String message, FontMetrics fm, int maxWidth) {
        List<String> lines = new ArrayList<>();
        
        // Hardcoded line breaks for specific messages
        if (message.equals("End point must be placed on a road!")) {
            lines.add("End point must be");
            lines.add("placed on a road!");
        } else if (message.equals("Start point must be placed on a road!")) {
            lines.add("Start point must be");
            lines.add("placed on a road!");
        } else {
            // Generic word wrapping
            String[] words = message.split(" ");
            String currentLine = "";
            
            for (String word : words) {
                String testLine = currentLine.isEmpty() ? word : currentLine + " " + word;
                if (fm.stringWidth(testLine) <= maxWidth) {
                    currentLine = testLine;
                } else {
                    if (!currentLine.isEmpty()) {
                        lines.add(currentLine);
                    }
                    currentLine = word;
                }
            }
            if (!currentLine.isEmpty()) {
                lines.add(currentLine);
            }
        }
        
        return lines;
    }
    
    private int getMaxLineWidth(List<String> lines, FontMetrics fm) {
        int maxWidth = 0;
        for (String line : lines) {
            maxWidth = Math.max(maxWidth, fm.stringWidth(line));
        }
        return maxWidth;
    }
    
    private float calculateFadeAlpha(long elapsedTime) {
        if (elapsedTime > POPUP_DURATION - 500) {
            return (POPUP_DURATION - elapsedTime) / 500.0f;
        }
        return 1.0f;
    }
    
    private void drawPopupBackground(Graphics2D g2d, int x, int y, int width, int height, float alpha) {
        // Shadow
        g2d.setColor(new Color(0, 0, 0, (int)(30 * alpha)));
        g2d.fillRoundRect(x + 2, y + 2, width, height, 8, 8);
        
        // Background
        g2d.setColor(new Color(245, 240, 220, (int)(180 * alpha)));
        g2d.fillRoundRect(x, y, width, height, 8, 8);
        
        // Highlight
        g2d.setColor(new Color(255, 250, 235, (int)(60 * alpha)));
        g2d.fillRoundRect(x + 1, y + 1, width - 2, height/4, 6, 6);
        
        // Border
        g2d.setColor(new Color(139, 125, 82, (int)(160 * alpha)));
        g2d.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.drawRoundRect(x, y, width, height, 8, 8);
    }
    
    private void drawPopupIcon(Graphics2D g2d, int x, int y, int size, float alpha) {
        // Warning icon background (tomato red)
        g2d.setColor(new Color(255, 99, 71, (int)(160 * alpha)));
        g2d.fillOval(x, y, size, size);
        
        // Exclamation mark
        g2d.setColor(new Color(255, 255, 255, (int)(255 * alpha)));
        Font iconFont = new Font("SansSerif", Font.BOLD, 10);
        g2d.setFont(iconFont);
        FontMetrics iconFm = g2d.getFontMetrics();
        String exclamation = "!";
        int exclamationX = x + (size - iconFm.stringWidth(exclamation)) / 2;
        int exclamationY = y + (size + iconFm.getAscent()) / 2 - 1;
        g2d.drawString(exclamation, exclamationX, exclamationY);
    }
    
    private void drawPopupText(Graphics2D g2d, List<String> lines, int startX, int startY, int lineHeight, float alpha) {
        g2d.setColor(new Color(76, 63, 47, (int)(255 * alpha)));
        for (int i = 0; i < lines.size(); i++) {
            g2d.drawString(lines.get(i), startX, startY + (i * lineHeight));
        }
    }
    
    // Public methods for controller interaction
    public void setSelectedTile(Tile tile) {
        this.selectedTile = tile;
        this.drawSelected = (tile != null);
    }
    
    public void setDrawSelected(boolean drawSelected) {
        this.drawSelected = drawSelected;
    }
    
    public void updateMousePosition(int x, int y) {
        // Snap to grid for tile preview
        this.mouseX = (x / GameDimensions.TILE_DISPLAY_SIZE) * GameDimensions.TILE_DISPLAY_SIZE;
        this.mouseY = (y / GameDimensions.TILE_DISPLAY_SIZE) * GameDimensions.TILE_DISPLAY_SIZE;
    }
    
    public void showPopupMessage(String message, int x, int y) {
        this.popupMessage = message;
        this.popupX = x;
        this.popupY = y;
        this.popupStartTime = System.currentTimeMillis();
    }
    
    public void hidePopupMessage() {
        this.popupMessage = "";
    }
    
    public boolean isDrawSelected() {
        return drawSelected;
    }
    
    // Performance method
    public void invalidateCache() {
        needsFullRedraw = true;
    }
} 