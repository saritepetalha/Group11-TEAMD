package views;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import constants.GameDimensions;
import helpMethods.BorderImageRotationGenerator;
import managers.SnowTransitionManager;
import models.PlayingModel;
import objects.Tower;
import objects.Warrior;
import ui_p.AssetsLoader;
import ui_p.DeadTree;
import ui_p.LiveTree;
import ui_p.PlayingUI;
import ui_p.TowerSelectionUI;
import javax.swing.JPanel;

/**
 * PlayingView - Handles all rendering logic for the Playing scene
 * Part of the MVC architecture for the Playing scene
 *
 * Responsibilities:
 * - Render the game world (map, entities, effects)
 * - Handle UI rendering (status bars, buttons, overlays)
 * - Provide visual feedback for user interactions
 * - Observe model changes and update visuals accordingly
 */
@SuppressWarnings("deprecation")
public class PlayingView implements Observer {

    private PlayingModel model;
    private PlayingUI playingUI;
    private TowerSelectionUI towerSelectionUI;
    private JPanel gamePane;

    // Mouse position for rendering
    private int mouseX, mouseY;

    // Spawn point indicator for warrior placement
    private BufferedImage spawnPointIndicator;

    // Tooltip for warrior information
    private ui_p.CostTooltip warriorTooltip;

    public PlayingView(PlayingModel model) {
        this.model = model;
        this.model.addObserver(this);

        // Initialize UI components that handle rendering
        initializeUIComponents();
        loadSpawnPointIndicator();

        // Initialize warrior tooltip
        this.warriorTooltip = new ui_p.CostTooltip();

        // Initialize game pane
        gamePane = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                render(g);
            }
        };
        gamePane.setLayout(null);
    }

    private void initializeUIComponents() {
        // Create one shared adapter to avoid multiple instances
        PlayingAdapter sharedAdapter = new PlayingAdapter(model);

        // Use the shared adapter for both UI components
        this.playingUI = new PlayingUIAdapter(model, sharedAdapter);
        this.towerSelectionUI = new TowerSelectionUI(sharedAdapter);

        // Initialize the difficulty display
        if (playingUI != null) {
            playingUI.setCurrentDifficulty(model.getCurrentDifficulty());
            System.out.println("Initialized PlayingUI with difficulty: " + model.getCurrentDifficulty());
        }
    }

    private void loadSpawnPointIndicator() {
        // Create a placeholder graphic for warrior placement
        int indicatorSize = 24;
        spawnPointIndicator = new BufferedImage(indicatorSize, indicatorSize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = spawnPointIndicator.createGraphics();

        // Draw a yellow circle with a black border
        g.setColor(Color.YELLOW);
        g.fillOval(2, 2, indicatorSize - 4, indicatorSize - 4);
        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(2));
        g.drawOval(2, 2, indicatorSize - 4, indicatorSize - 4);

        g.dispose();
    }

    /**
     * Main render method called by the controller
     */
    public void render(Graphics g) {
        // Apply ultimate manager shake effect if needed
        if (model.getUltiManager() != null) {
            model.getUltiManager().applyShakeIfNeeded(g);
        }

        // Draw the game world
        drawTiles(g);
        drawTowers(g);
        drawEnemies(g);
        drawProjectiles(g);
        drawEffects(g);

        // Draw mining button if it exists
        if (model.getStoneMiningManager() != null && model.getStoneMiningManager().getMineButton() != null) {
            model.getStoneMiningManager().getMineButton().draw(g);
        }

        // Draw UI elements
        drawUI(g);

        // Draw stone mining effects
        if (model.getStoneMiningManager() != null) {
            model.getStoneMiningManager().draw((Graphics2D) g);
        }

        // Reverse shake effect
        if (model.getUltiManager() != null) {
            model.getUltiManager().reverseShake(g);
        }
    }

    private void drawTiles(Graphics g) {
        int[][] level = model.getLevel();
        if (level == null) return;

        // Fill background color
        g.setColor(new Color(134, 177, 63, 255));
        g.fillRect(0, 0, GameDimensions.GAME_WIDTH, GameDimensions.GAME_HEIGHT);

        int rowCount = level.length;
        int colCount = level[0].length;

        // Check if we're in snow mode
        boolean isSnowActive = model.getTileManager() != null &&
                model.getTileManager().getSnowState() != SnowTransitionManager.SnowState.NORMAL;

        // Detect which edge contains the gate (endpoint) for border rendering
        int gateEdge = BorderImageRotationGenerator.getInstance().detectGateEdge(level);

        if (isSnowActive) {
            drawSnowLayeredMap(g, level, rowCount, colCount, gateEdge);
        } else {
            drawNormalMap(g, level, rowCount, colCount, gateEdge);
        }
    }

    /**
     * Draws the map with proper snow layering
     */
    private void drawSnowLayeredMap(Graphics g, int[][] level, int rowCount, int colCount, int gateEdge) {
        // LAYER 1: Draw snowy grass base across entire map
        BufferedImage snowyGrassSprite = getSnowGrassSprite();
        if (snowyGrassSprite != null) {
            for (int i = 0; i < rowCount; i++) {
                for (int j = 0; j < colCount; j++) {
                    g.drawImage(snowyGrassSprite,
                            j * GameDimensions.TILE_DISPLAY_SIZE,
                            i * GameDimensions.TILE_DISPLAY_SIZE, null);
                }
            }
        }

        // LAYER 2: Draw all non-grass elements on top
        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < colCount; j++) {
                int tileId = level[i][j];

                // Skip certain tower tiles (handled elsewhere)
                if (tileId == 20 || tileId == 21 || tileId == 26) {
                    continue;
                }

                // Handle special border tiles
                if (tileId == -3 || tileId == -4) {
                    drawBorderTile(g, tileId, j, i, gateEdge);
                } else {
                    BufferedImage tileSprite = model.getTileManager().getSprite(tileId);
                    if (tileSprite != null) {
                        g.drawImage(tileSprite,
                                j * GameDimensions.TILE_DISPLAY_SIZE,
                                i * GameDimensions.TILE_DISPLAY_SIZE, null);
                    }
                }
            }
        }
    }

    /**
     * Draws the map normally (without snow effects)
     */
    private void drawNormalMap(Graphics g, int[][] level, int rowCount, int colCount, int gateEdge) {
        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < colCount; j++) {
                int tileId = level[i][j];

                // Skip certain tower tiles (handled elsewhere)
                if (tileId == 20 || tileId == 21 || tileId == 26) {
                    continue;
                }

                // Handle special border tiles
                if (tileId == -3 || tileId == -4) {
                    drawBorderTile(g, tileId, j, i, gateEdge);
                } else {
                    BufferedImage tileSprite = model.getTileManager().getSprite(tileId);
                    if (tileSprite != null) {
                        g.drawImage(tileSprite,
                                j * GameDimensions.TILE_DISPLAY_SIZE,
                                i * GameDimensions.TILE_DISPLAY_SIZE, null);
                    }
                }
            }
        }
    }

    /**
     * Gets the appropriate snowy grass sprite
     */
    private BufferedImage getSnowGrassSprite() {
        if (model.getTileManager() == null) return null;
        return model.getTileManager().getSprite(5); // Grass tile ID
    }

    /**
     * Draws border tiles with proper rotation
     */
    private void drawBorderTile(Graphics g, int tileId, int j, int i, int gateEdge) {
        BufferedImage img = null;
        if (tileId == -3) { // Wall
            img = BorderImageRotationGenerator.getInstance().getRotatedBorderImage(true, gateEdge);
        } else if (tileId == -4) { // Gate
            img = BorderImageRotationGenerator.getInstance().getRotatedBorderImage(false, gateEdge);
        }

        if (img != null) {
            int x = j * GameDimensions.TILE_DISPLAY_SIZE;
            int y = i * GameDimensions.TILE_DISPLAY_SIZE;
            int ts = GameDimensions.TILE_DISPLAY_SIZE;
            g.drawImage(img, x, y, ts, ts, null);
        }
    }

    private void drawEnemies(Graphics g) {
        // Draw ultimates effects
        if (model.getUltiManager() != null) {
            model.getUltiManager().draw(g, model.getGameSpeedMultiplier());
        }

        // Draw enemies
        if (model.getEnemyManager() != null) {
            model.getEnemyManager().draw(g, model.isGamePaused());
        }
    }

    private void drawTowers(Graphics g) {
        // Draw towers
        if (model.getTowerManager() != null) {
            model.getTowerManager().draw(g, model.getGameSpeedMultiplier());
        }

        // Draw tower buttons (dead trees)
        drawTowerButtons(g);

        // Draw live tree buttons
        drawLiveTreeButtons(g);

        // Draw tower selection UI (range indicators, buttons, etc.)
        if (towerSelectionUI != null) {
            towerSelectionUI.draw(g);
        }

        // Draw tower light effects
        if (model.getTowerManager() != null) {
            model.getTowerManager().drawLightEffects(g);
        }

        // Draw gold bags
        if (model.getGoldBagManager() != null) {
            model.getGoldBagManager().draw(g);
        }
    }

    private void drawTowerButtons(Graphics g) {
        List<DeadTree> deadTrees = model.getDeadTrees();
        if (deadTrees != null) {
            for (DeadTree deadTree : deadTrees) {
                deadTree.draw(g);
            }
        }
    }

    private void drawLiveTreeButtons(Graphics g) {
        List<LiveTree> liveTrees = model.getLiveTrees();
        if (liveTrees != null) {
            for (LiveTree liveTree : liveTrees) {
                liveTree.draw(g);
            }
        }
    }

    /**
     * Handles mouse movement for tree button tooltips
     */
    public void handleMouseMovedForTooltips(int mouseX, int mouseY) {
        // Handle dead tree tooltips
        List<DeadTree> deadTrees = model.getDeadTrees();
        if (deadTrees != null) {
            for (DeadTree deadTree : deadTrees) {
                deadTree.handleMouseHover(mouseX, mouseY, model);
            }
        }

        // Handle live tree tooltips  
        List<LiveTree> liveTrees = model.getLiveTrees();
        if (liveTrees != null) {
            for (LiveTree liveTree : liveTrees) {
                liveTree.handleMouseHover(mouseX, mouseY, model);
            }
        }
    }

    /**
     * Handles warrior hover detection and tooltip display
     */
    private void handleWarriorHover(int mouseX, int mouseY) {
        if (model.getTowerManager() == null || warriorTooltip == null) {
            return;
        }

        boolean hoveredWarrior = false;

        // Check each warrior for hover
        for (objects.Warrior warrior : model.getTowerManager().getWarriors()) {
            if (isMouseOverWarrior(warrior, mouseX, mouseY)) {
                // Show warrior tooltip
                String warriorName = getWarriorClassName(warrior);
                String description = getWarriorDescription(warrior);

                warriorTooltip.showSimple(warriorName, description, mouseX, mouseY);
                hoveredWarrior = true;
                break; // Only show one tooltip at a time
            }
        }

        // Hide tooltip if not hovering any warrior
        if (!hoveredWarrior) {
            warriorTooltip.hide();
        }

        // Update tooltip animation
        warriorTooltip.update();
    }

    /**
     * Checks if mouse is over a warrior (using smaller, more precise hitbox)
     */
    private boolean isMouseOverWarrior(objects.Warrior warrior, int mouseX, int mouseY) {
        int warriorCenterX = warrior.getX() + 32; // Center of the 64x64 tile
        int warriorCenterY = warrior.getY() + 32;

        // Use smaller, more precise hitbox (about 60% of sprite size)
        int hitboxWidth, hitboxHeight;
        if (warrior instanceof objects.WizardWarrior) {
            hitboxWidth = 55;  // Reduced from 92
            hitboxHeight = 45; // Reduced from 76
        } else { // ArcherWarrior
            hitboxWidth = 50;  // Reduced from 84
            hitboxHeight = 50; // Reduced from 84
        }

        // Center the hitbox on the warrior
        int hitboxX = warriorCenterX - hitboxWidth / 2;
        int hitboxY = warriorCenterY - hitboxHeight / 2;

        return mouseX >= hitboxX && mouseX <= hitboxX + hitboxWidth &&
                mouseY >= hitboxY && mouseY <= hitboxY + hitboxHeight;
    }

    /**
     * Gets the warrior class name for tooltip
     */
    private String getWarriorClassName(objects.Warrior warrior) {
        if (warrior instanceof objects.ArcherWarrior) {
            return "Archer Warrior";
        } else if (warrior instanceof objects.WizardWarrior) {
            return "Wizard Warrior";
        }
        return "Warrior";
    }

    /**
     * Gets warrior description with time remaining for tooltip
     */
    private String getWarriorDescription(objects.Warrior warrior) {
        long remainingLifetime = warrior.getRemainingLifetime() / 1000; // Convert to seconds
        return String.format("Time remaining: %ds", remainingLifetime);
    }

    private void drawProjectiles(Graphics g) {
        // Draw projectiles
        if (model.getProjectileManager() != null) {
            model.getProjectileManager().draw(g);
        }
    }

    private void drawEffects(Graphics g) {
        // Draw weather effects
        if (model.getWeatherManager() != null) {
            model.getWeatherManager().draw(g);
        }

        // Draw fire animations
        if (model.getFireAnimationManager() != null) {
            model.getFireAnimationManager().draw(g);
        }

        // Draw victory confetti animation
        if (model.getVictoryConfetti() != null) {
            model.getVictoryConfetti().draw(g);
        }
    }

    private void drawUI(Graphics g) {
        // Draw Gold Factory preview if selected (in game world, before UI)
        if (model.getUltiManager() != null && model.getUltiManager().isGoldFactorySelected()) {
            drawGoldFactoryPreview((Graphics2D) g);
        }

        // Draw Warrior placement preview if warrior is pending (in game world, before UI)
        if (model.getPendingWarriorPlacement() != null) {
            drawWarriorPlacementPreview((Graphics2D) g);
        }

        // Draw castle health bar
        if (!model.isOptionsMenuOpen()) {
            drawCastleHealthBar(g);
        }

        // Draw main UI
        if (playingUI != null) {
            playingUI.draw(g);
        }

        // Draw warrior placement message if pending
        if (model.getPendingWarriorPlacement() != null) {
            drawWarriorPlacementMessage(g);
        }

        // Draw gold factory placement message if selected
        if (model.getUltiManager() != null && model.getUltiManager().isGoldFactorySelected()) {
            drawGoldFactoryPlacementMessage(g);
        }

        // Draw lightning targeting message if waiting for target
        if (model.getUltiManager() != null && model.getUltiManager().isWaitingForLightningTarget()) {
            drawLightningTargetingMessage(g);
        }

        // Draw warrior tooltip (rendered on top of everything)
        if (warriorTooltip != null) {
            warriorTooltip.draw((Graphics2D) g);
        }
    }

    private void drawWarriorPlacementPreview(Graphics2D g) {
        Warrior pendingWarrior = model.getPendingWarriorPlacement();
        if (pendingWarrior == null) return;

        // Snap to tile grid
        int tileX = (mouseX / GameDimensions.TILE_DISPLAY_SIZE) * GameDimensions.TILE_DISPLAY_SIZE;
        int tileY = (mouseY / GameDimensions.TILE_DISPLAY_SIZE) * GameDimensions.TILE_DISPLAY_SIZE;

        // Check if tile is valid for placement
        boolean isValidTile = isValidTileForWarriorPlacement(mouseX, mouseY);

        // Enable anti-aliasing
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw background
        if (isValidTile) {
            g.setColor(new Color(144, 238, 144, 80)); // Light green
        } else {
            g.setColor(new Color(255, 182, 193, 80)); // Light red
        }

        g.fillRoundRect(tileX + 2, tileY + 2, 60, 60, 12, 12);

        // Draw the warrior sprite
        try {
            BufferedImage warriorSprite = getWarriorSprite(pendingWarrior);
            if (warriorSprite != null) {
                Composite originalComposite = g.getComposite();
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8f));

                // Determine sprite dimensions and scaling based on warrior type
                int drawWidth, drawHeight;
                int drawX, drawY;

                if (pendingWarrior instanceof objects.WizardWarrior) {
                    // Wizard sprites - match the exact drawing size from TowerManager
                    drawWidth = 92;
                    drawHeight = 76;
                    drawX = tileX + (64 - drawWidth) / 2;
                    drawY = tileY + (64 - drawHeight) + 4;
                } else {
                    drawWidth = 84;
                    drawHeight = 84;
                    drawX = tileX + (64 - drawWidth) / 2;
                    drawY = tileY + (64 - drawHeight) + 12;
                }

                // Handle sprite mirroring for preview based on warrior's facing direction
                if (pendingWarrior.isFacingLeft()) {
                    // Draw the sprite mirrored horizontally
                    g.drawImage(warriorSprite, drawX + drawWidth, drawY, -drawWidth, drawHeight, null);
                } else {
                    // Normal drawing for facing right
                    g.drawImage(warriorSprite, drawX, drawY, drawWidth, drawHeight, null);
                }
                g.setComposite(originalComposite);
            } else {
                // Fallback - draw warrior initial
                g.setColor(new Color(100, 149, 237, 180)); // Cornflower blue
                g.setFont(new Font("Arial", Font.BOLD, 20));
                FontMetrics fm = g.getFontMetrics();
                String text = getWarriorInitial(pendingWarrior);
                int textX = tileX + 32 - fm.stringWidth(text) / 2;
                int textY = tileY + 32 + fm.getAscent() / 2;
                g.drawString(text, textX, textY);
            }
        } catch (Exception e) {
            // Fallback
            g.setColor(new Color(100, 149, 237, 180));
            g.setFont(new Font("Arial", Font.BOLD, 20));
            FontMetrics fm = g.getFontMetrics();
            String text = getWarriorInitial(pendingWarrior);
            int textX = tileX + 32 - fm.stringWidth(text) / 2;
            int textY = tileY + 32 + fm.getAscent() / 2;
            g.drawString(text, textX, textY);
        }

        // Draw border
        if (isValidTile) {
            g.setColor(new Color(34, 139, 34, 120)); // Forest green
        } else {
            g.setColor(new Color(220, 20, 60, 120)); // Crimson
        }
        g.setStroke(new BasicStroke(2f));
        g.drawRoundRect(tileX + 2, tileY + 2, 60, 60, 12, 12);

        // Add sparkle effect for valid placement
        if (isValidTile) {
            long time = System.currentTimeMillis();
            float sparkleAlpha = (float)(0.3f + 0.2f * Math.sin(time * 0.005f));
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, sparkleAlpha));
            g.setColor(new Color(255, 255, 255, 100));
            g.fillOval(tileX + 8, tileY + 8, 4, 4);
            g.fillOval(tileX + 52, tileY + 8, 4, 4);
            g.fillOval(tileX + 8, tileY + 52, 4, 4);
            g.fillOval(tileX + 52, tileY + 52, 4, 4);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    }

    /**
     * Helper method to check if a tile is valid for warrior placement
     */
    private boolean isValidTileForWarriorPlacement(int pixelX, int pixelY) {
        int[][] level = model.getLevel();
        if (level == null) return false;

        int tileC = pixelX / GameDimensions.TILE_DISPLAY_SIZE;
        int tileR = pixelY / GameDimensions.TILE_DISPLAY_SIZE;

        if (tileR >= 0 && tileR < level.length && tileC >= 0 && tileC < level[0].length) {
            // Check if the tile type is grass (ID 5)
            boolean isGrass = level[tileR][tileC] == 5;
            if (!isGrass) return false;

            // Check if the tile is already occupied by a tower
            if (model.getTowerManager() != null) {
                for (Tower tower : model.getTowerManager().getTowers()) {
                    if (tower.isClicked(pixelX, pixelY)) {
                        return false;
                    }
                }
            }

            // Check if the tile is already occupied by another warrior
            if (model.getTowerManager() != null) {
                int tileX = (pixelX / GameDimensions.TILE_DISPLAY_SIZE) * GameDimensions.TILE_DISPLAY_SIZE;
                int tileY = (pixelY / GameDimensions.TILE_DISPLAY_SIZE) * GameDimensions.TILE_DISPLAY_SIZE;

                for (Warrior warrior : model.getTowerManager().getWarriors()) {
                    if (warrior.getX() == tileX && warrior.getY() == tileY - 8) { // Account for warrior offset
                        return false;
                    }
                }
            }

            // Check distance limitation from spawning tower
            Warrior pendingWarrior = model.getPendingWarriorPlacement();
            if (pendingWarrior != null) {
                Tower spawnTower = pendingWarrior.getSpawnedFromTower();
                if (spawnTower != null && !spawnTower.isWithinSpawnDistance(pixelX, pixelY)) {
                    return false; // Too far from tower
                }
            }

            return true;
        }
        return false;
    }

    /**
     * Helper method to get warrior sprite based on warrior type
     */
    private BufferedImage getWarriorSprite(Warrior warrior) {
        // Get the first frame of the attack animation for preview
        BufferedImage[] attackFrames = helpMethods.LoadSave.getWarriorAttackAnimation(warrior);
        if (attackFrames != null && attackFrames.length > 0) {
            return attackFrames[0]; // Return first frame for preview
        }
        return null;
    }

    /**
     * Helper method to get warrior initial for fallback display
     */
    private String getWarriorInitial(Warrior warrior) {
        if (warrior instanceof objects.ArcherWarrior) {
            return "A";
        } else if (warrior instanceof objects.WizardWarrior) {
            return "W";
        }
        return "?";
    }

    private void drawWarriorPlacementMessage(Graphics g) {
        Warrior pendingWarrior = model.getPendingWarriorPlacement();
        if (pendingWarrior == null) return;

        String warriorType = getWarriorTypeName(pendingWarrior);
        String message = "⚔️ Click to place " + warriorType + " Warrior ($" + pendingWarrior.getCost() + ") | Range: 3 tiles | Right-click to cancel ❌";

        g.setColor(new Color(100, 149, 237)); // Cornflower blue
        g.setFont(new Font("Segoe UI Emoji", Font.BOLD, 18));
        int stringWidth = g.getFontMetrics().stringWidth(message);
        int x = (GameDimensions.GAME_WIDTH - stringWidth) / 2;
        int y = 30;

        Graphics2D g2d = (Graphics2D) g;
        // Message background
        g2d.setColor(new Color(0, 0, 0, 100));
        g2d.fillRoundRect(x - 10, y - 20, stringWidth + 20, 30, 10, 10);

        g.setColor(new Color(100, 149, 237));
        g.drawString(message, x, y);

        // Draw spawn range indicator around the spawning tower
        Tower spawnTower = pendingWarrior.getSpawnedFromTower();
        if (spawnTower != null) {
            g2d.setColor(new Color(100, 149, 237, 30)); // Light blue with transparency
            g2d.setStroke(new BasicStroke(2f));

            // Calculate range area (3 tiles = 3 * 64 pixels)
            int rangeInPixels = 3 * GameDimensions.TILE_DISPLAY_SIZE;
            int centerX = spawnTower.getX() + spawnTower.getWidth() / 2;
            int centerY = spawnTower.getY() + spawnTower.getHeight() / 2;

            // Draw range circle
            g2d.drawOval(centerX - rangeInPixels, centerY - rangeInPixels,
                    rangeInPixels * 2, rangeInPixels * 2);
        }

        // Draw blue squares on all valid placement tiles
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int[][] level = model.getLevel();
        if (level != null) {
            for (int r = 0; r < level.length; r++) {
                for (int c = 0; c < level[0].length; c++) {
                    if (level[r][c] == 5) { // Grass tile
                        int tilePixelX = c * GameDimensions.TILE_DISPLAY_SIZE;
                        int tilePixelY = r * GameDimensions.TILE_DISPLAY_SIZE;

                        // Check if this specific tile is valid (no towers/warriors)
                        if (isValidTileForWarriorPlacement(tilePixelX, tilePixelY)) {
                            // Blue highlight for valid tiles
                            g2d.setColor(new Color(100, 149, 237, 40)); // Light blue
                            g2d.fillRoundRect(tilePixelX + 4, tilePixelY + 4,
                                    GameDimensions.TILE_DISPLAY_SIZE - 8,
                                    GameDimensions.TILE_DISPLAY_SIZE - 8, 8, 8);

                            g2d.setColor(new Color(65, 105, 225, 80)); // Royal blue border
                            g2d.setStroke(new BasicStroke(1.5f));
                            g2d.drawRoundRect(tilePixelX + 4, tilePixelY + 4,
                                    GameDimensions.TILE_DISPLAY_SIZE - 8,
                                    GameDimensions.TILE_DISPLAY_SIZE - 8, 8, 8);
                        }
                    }
                }
            }
        }

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    }

    /**
     * Helper method to get warrior type name for display
     */
    private String getWarriorTypeName(Warrior warrior) {
        if (warrior instanceof objects.ArcherWarrior) {
            return "Archer";
        } else if (warrior instanceof objects.WizardWarrior) {
            return "Wizard";
        }
        return "Unknown";
    }

    private void drawCastleHealthBar(Graphics g) {
        int[][] level = model.getLevel();
        if (level == null || model.getTileManager() == null || model.getPlayerManager() == null) return;

        int castleX = -1, castleY = -1;
        outer:
        for (int i = 0; i < level.length - 1; i++) {
            for (int j = 0; j < level[i].length - 1; j++) {
                if (level[i][j] == model.getTileManager().CastleTopLeft.getId() &&
                        level[i][j + 1] == model.getTileManager().CastleTopRight.getId() &&
                        level[i + 1][j] == model.getTileManager().CastleBottomLeft.getId() &&
                        level[i + 1][j + 1] == model.getTileManager().CastleBottomRight.getId()) {
                    castleX = j;
                    castleY = i;
                    break outer;
                }
            }
        }
        if (castleX == -1 || castleY == -1) return;

        int tileSize = GameDimensions.TILE_DISPLAY_SIZE;
        int barWidth = tileSize * 2 - 8;
        int barHeight = 8;
        int barX = castleX * tileSize + 4;
        int barY = castleY * tileSize - 14;

        g.setColor(Color.DARK_GRAY);
        g.fillRoundRect(barX, barY, barWidth, barHeight, 6, 6);

        float healthPercent = Math.max(0,
                (float) model.getPlayerManager().getHealth() / model.getPlayerManager().getStartingHealthAmount());
        Color healthColor = new Color((int) (255 * (1 - healthPercent)), (int) (220 * healthPercent), 40);
        int healthBarWidth = (int) (barWidth * healthPercent);
        g.setColor(healthColor);
        g.fillRoundRect(barX, barY, healthBarWidth, barHeight, 6, 6);

        g.setColor(Color.BLACK);
        g.drawRoundRect(barX, barY, barWidth, barHeight, 6, 6);
    }

    private void drawGoldFactoryPreview(Graphics2D g) {
        // Snap to tile grid
        int tileX = (mouseX / 64) * 64;
        int tileY = (mouseY / 64) * 64;

        // Check if tile is valid for placement
        boolean isValidTile = false;
        int[][] level = model.getLevel();
        int levelTileX = mouseX / 64;
        int levelTileY = mouseY / 64;

        if (level != null && levelTileY >= 0 && levelTileY < level.length &&
                levelTileX >= 0 && levelTileX < level[0].length) {
            isValidTile = (level[levelTileY][levelTileX] == 5); // Grass tile
        }

        // Enable anti-aliasing
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw background
        if (isValidTile) {
            g.setColor(new Color(144, 238, 144, 80));
        } else {
            g.setColor(new Color(255, 182, 193, 80));
        }

        g.fillRoundRect(tileX + 2, tileY + 2, 60, 60, 12, 12);

        // Draw the factory sprite
        try {
            BufferedImage factorySprite = AssetsLoader.getInstance().goldFactorySprite;
            if (factorySprite != null) {
                Composite originalComposite = g.getComposite();
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8f));
                g.drawImage(factorySprite, tileX + 2, tileY + 2, 60, 60, null);
                g.setComposite(originalComposite);
            } else {
                // Fallback
                g.setColor(new Color(218, 165, 32, 180));
                g.setFont(new Font("Arial", Font.BOLD, 20));
                FontMetrics fm = g.getFontMetrics();
                String text = "G";
                int textX = tileX + 32 - fm.stringWidth(text) / 2;
                int textY = tileY + 32 + fm.getAscent() / 2;
                g.drawString(text, textX, textY);
            }
        } catch (Exception e) {
            // Fallback
            g.setColor(new Color(218, 165, 32, 180));
            g.setFont(new Font("Arial", Font.BOLD, 20));
            FontMetrics fm = g.getFontMetrics();
            String text = "G";
            int textX = tileX + 32 - fm.stringWidth(text) / 2;
            int textY = tileY + 32 + fm.getAscent() / 2;
            g.drawString(text, textX, textY);
        }

        // Draw border
        if (isValidTile) {
            g.setColor(new Color(34, 139, 34, 120));
        } else {
            g.setColor(new Color(220, 20, 60, 120));
        }
        g.setStroke(new BasicStroke(2f));
        g.drawRoundRect(tileX + 2, tileY + 2, 60, 60, 12, 12);

        // Add sparkle effect for valid placement
        if (isValidTile) {
            long time = System.currentTimeMillis();
            float sparkleAlpha = (float)(0.3f + 0.2f * Math.sin(time * 0.005f));
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, sparkleAlpha));
            g.setColor(new Color(255, 255, 255, 100));
            g.fillOval(tileX + 8, tileY + 8, 4, 4);
            g.fillOval(tileX + 52, tileY + 8, 4, 4);
            g.fillOval(tileX + 8, tileY + 52, 4, 4);
            g.fillOval(tileX + 52, tileY + 52, 4, 4);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    }

    private void drawGoldFactoryPlacementMessage(Graphics g) {
        String message = "🏭 Click to place Gold Factory ($100) | Right-click to cancel ❌";

        g.setColor(new Color(255, 215, 0));
        g.setFont(new Font("Segoe UI Emoji", Font.BOLD, 18));
        int stringWidth = g.getFontMetrics().stringWidth(message);
        int x = (GameDimensions.GAME_WIDTH - stringWidth) / 2;
        int y = 30;

        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(new Color(0, 0, 0, 100));
        g2d.fillRoundRect(x - 10, y - 20, stringWidth + 20, 30, 10, 10);

        g.setColor(new Color(255, 215, 0));
        g.drawString(message, x, y);

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int[][] level = model.getLevel();
        if (level != null) {
            for (int r = 0; r < level.length; r++) {
                for (int c = 0; c < level[0].length; c++) {
                    if (level[r][c] == 5) { // Grass tile
                        int tilePixelX = c * GameDimensions.TILE_DISPLAY_SIZE;
                        int tilePixelY = r * GameDimensions.TILE_DISPLAY_SIZE;

                        g2d.setColor(new Color(144, 238, 144, 40));
                        g2d.fillRoundRect(tilePixelX + 4, tilePixelY + 4,
                                GameDimensions.TILE_DISPLAY_SIZE - 8,
                                GameDimensions.TILE_DISPLAY_SIZE - 8, 8, 8);

                        g2d.setColor(new Color(34, 139, 34, 80));
                        g2d.setStroke(new BasicStroke(1.5f));
                        g2d.drawRoundRect(tilePixelX + 4, tilePixelY + 4,
                                GameDimensions.TILE_DISPLAY_SIZE - 8,
                                GameDimensions.TILE_DISPLAY_SIZE - 8, 8, 8);
                    }
                }
            }
        }

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    }

    private void drawLightningTargetingMessage(Graphics g) {
        String message = "⚡ Click to target Lightning Strike ($75) | Right-click to cancel ❌";

        g.setColor(new Color(255, 255, 100)); // Bright yellow for lightning
        g.setFont(new Font("Segoe UI Emoji", Font.BOLD, 18));
        int stringWidth = g.getFontMetrics().stringWidth(message);
        int x = (GameDimensions.GAME_WIDTH - stringWidth) / 2;
        int y = 30;

        Graphics2D g2d = (Graphics2D) g;
        // Message background
        g2d.setColor(new Color(0, 0, 0, 100));
        g2d.fillRoundRect(x - 10, y - 20, stringWidth + 20, 30, 10, 10);

        g.setColor(new Color(255, 255, 100));
        g.drawString(message, x, y);
    }

    // Mouse event handling for UI
    public void mouseMoved(int x, int y) {
        this.mouseX = x;
        this.mouseY = y;

        // Check for warrior hover first (before other UI elements)
        handleWarriorHover(x, y);

        if (playingUI != null) {
            playingUI.mouseMoved(x, y);
        }

        if (towerSelectionUI != null) {
            towerSelectionUI.mouseMoved(x, y);
        }
    }

    public boolean mouseClicked(int x, int y) {
        boolean handled = false;

        if (towerSelectionUI != null) {
            handled = towerSelectionUI.mouseClicked(x, y);
        }

        return handled;
    }

    public void mousePressed(int x, int y) {
        if (playingUI != null) {
            playingUI.mousePressed(x, y);
        }
        if (towerSelectionUI != null) {
            towerSelectionUI.mousePressed(x, y);
        }
    }

    public void mouseReleased(int x, int y) {
        if (playingUI != null) {
            playingUI.mouseReleased();
        }
        if (towerSelectionUI != null) {
            towerSelectionUI.mouseReleased();
        }
    }

    public void mouseDragged(int x, int y) {
        if (playingUI != null) {
            playingUI.mouseDragged(x, y);
        }
    }

    public void mouseWheelMoved(MouseWheelEvent e) {
        if (playingUI != null) {
            playingUI.mouseWheelMoved(e);
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        // Handle model updates
        String notification = (String) arg;
        switch (notification) {
            case "resourcesUpdated":
                updateUIResources();
                break;
            case "towerSelected":
                updateTowerSelection();
                break;
            case "gameStateReset":
                handleGameStateReset();
                break;
            case "difficultyChanged":
                updateDifficultyDisplay();
                break;
            // Add more cases as needed
        }
    }

    private void updateUIResources() {
        if (playingUI != null && model.getPlayerManager() != null) {
            playingUI.setGoldAmount(model.getPlayerManager().getGold());
            playingUI.setHealthAmount(model.getPlayerManager().getHealth());
            playingUI.setShieldAmount(model.getPlayerManager().getShield());

            if (model.getGameOptions() != null) {
                playingUI.setStartingHealthAmount(model.getGameOptions().getStartingPlayerHP());
                playingUI.setStartingShieldAmount(model.getGameOptions().getStartingShield());
            }
        }
    }

    private void updateTowerSelection() {
        if (towerSelectionUI != null) {
            towerSelectionUI.setSelectedTower(model.getDisplayedTower());
        }
    }

    private void updateDifficultyDisplay() {
        if (playingUI != null) {
            playingUI.setCurrentDifficulty(model.getCurrentDifficulty());
            System.out.println("Updated PlayingUI difficulty display to: " + model.getCurrentDifficulty());
        }
    }

    private void handleGameStateReset() {
        // Handle any view-specific reset logic
        updateUIResources();
        updateTowerSelection();
        updateDifficultyDisplay();
    }

    // Getters for controller access
    public PlayingUI getPlayingUI() { return playingUI; }
    public TowerSelectionUI getTowerSelectionUI() { return towerSelectionUI; }

    public JPanel getGamePane() {
        return gamePane;
    }

    /**
     * Temporary adapter classes to bridge between new MVC and existing UI components
     */
    private static class PlayingUIAdapter extends PlayingUI {
        private PlayingModel model;
        private PlayingAdapter sharedAdapter;

        public PlayingUIAdapter(PlayingModel model, PlayingAdapter sharedAdapter) {
            super(sharedAdapter);
            this.model = model;
            this.sharedAdapter = sharedAdapter;
        }

        @Override
        public void setCurrentDifficulty(String difficulty) {
            super.setCurrentDifficulty(difficulty);
        }
    }

    private static class PlayingAdapter extends scenes.Playing {
        private PlayingModel model;

        public PlayingAdapter(PlayingModel model) {
            super(null, true); // Use safe constructor with isAdapter=true
            this.model = model;
        }

        // Override key methods that UI components need
        @Override
        public boolean isGamePaused() { return model.isGamePaused(); }

        @Override
        public boolean isOptionsMenuOpen() { return model.isOptionsMenuOpen(); }

        @Override
        public float getGameSpeedMultiplier() { return model.getGameSpeedMultiplier(); }

        @Override
        public String getWaveStatus() { return model.getWaveStatus(); }

        @Override
        public managers.WeatherManager getWeatherManager() { return model.getWeatherManager(); }

        @Override
        public managers.TileManager getTileManager() { return model.getTileManager(); }

        @Override
        public managers.PlayerManager getPlayerManager() { return model.getPlayerManager(); }

        @Override
        public managers.UltiManager getUltiManager() { return model.getUltiManager(); }

        @Override
        public int[][] getLevel() { return model.getLevel(); }

        // Override all manager getters to prevent null access
        @Override
        public managers.WaveManager getWaveManager() { return model.getWaveManager(); }

        @Override
        public managers.EnemyManager getEnemyManager() { return model.getEnemyManager(); }

        @Override
        public managers.TowerManager getTowerManager() { return model.getTowerManager(); }

        @Override
        public managers.GoldBagManager getGoldBagManager() { return model.getGoldBagManager(); }

        @Override
        public managers.FireAnimationManager getFireAnimationManager() { return model.getFireAnimationManager(); }

        @Override
        public int[][] getOverlay() { return model.getOverlay(); }

        @Override
        public long getGameTime() { return model.getGameTime(); }

        @Override
        public String getCurrentMapName() { return model.getCurrentMapName(); }

        @Override
        public String getCurrentDifficulty() { return model.getCurrentDifficulty(); }

        @Override
        public boolean isAllWavesFinished() { return model.isAllWavesFinished(); }

        @Override
        public config.GameOptions getGameOptions() { return model.getGameOptions(); }

        // Override tree-related methods
        @Override
        public java.util.List<ui_p.DeadTree> getDeadTrees() { return model.getDeadTrees(); }

        @Override
        public java.util.List<ui_p.LiveTree> getLiveTrees() { return model.getLiveTrees(); }

        // Override methods that are called by UI components
        @Override
        public void startWarriorPlacement(Warrior warrior) {
            // Handle directly through model
            model.setPendingWarriorPlacement(warrior);
            // Clear tower selection
            model.setDisplayedTower(null);
            System.out.println("Warrior placement mode started for: " + warrior.getClass().getSimpleName());
        }

        @Override
        public void modifyTile(int x, int y, String tile) {
            // Handle tile modification directly through model (same logic as Playing.java)
            x /= 64;
            y /= 64;

            int[][] level = model.getLevel();
            if (level == null) return;

            if (tile.equals("ARCHER")) {
                level[y][x] = 26;
            } else if (tile.equals("MAGE")) {
                level[y][x] = 20;
            } else if (tile.equals("ARTILERRY")) {
                level[y][x] = 21;
            } else if (tile.equals("DEADTREE")) {
                level[y][x] = 15;
            }
            System.out.println("Tile modified at (" + x + ", " + y + ") to: " + tile);
        }
    }
} 