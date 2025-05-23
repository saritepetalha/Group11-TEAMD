package ui_p;

import objects.Tower;
import strategies.TargetingStrategyFactory.StrategyType;
import scenes.Playing;
import constants.GameDimensions;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages the UI for selected towers, including upgrade and targeting strategy buttons.
 * Handles enhanced range indicators with strategy-specific visual effects.
 */
public class TowerSelectionUI {
    
    private Playing playing;
    private Tower selectedTower;
    private TheButton upgradeButton;
    private TargetingButton targetingButton;
    
    // UI positioning
    private static final int BUTTON_WIDTH = 80;
    private static final int BUTTON_HEIGHT = 24;
    private static final int BUTTON_SPACING = 8;
    
    // Range indicator effects
    private long animationStartTime = System.currentTimeMillis();
    
    public TowerSelectionUI(Playing playing) {
        this.playing = playing;
    }
    
    /**
     * Sets the currently selected tower and creates appropriate buttons
     */
    public void setSelectedTower(Tower tower) {
        this.selectedTower = tower;
        
        if (tower != null) {
            createButtons();
        } else {
            clearButtons();
        }
    }
    
    /**
     * Creates the upgrade and targeting buttons for the selected tower
     */
    private void createButtons() {
        if (selectedTower == null) return;
        
        // Position buttons near the tower
        int buttonX = selectedTower.getX() + 70; // Offset to the right of tower
        int buttonY = selectedTower.getY() - 10; // Slightly above tower
        
        // Adjust position if buttons would go off-screen
        if (buttonX + BUTTON_WIDTH > GameDimensions.GAME_WIDTH - 20) {
            buttonX = selectedTower.getX() - BUTTON_WIDTH - 10; // Position to the left
        }
        if (buttonY < 20) {
            buttonY = selectedTower.getY() + 70; // Position below tower
        }
        
        // Create upgrade button
        upgradeButton = new TheButton("Upgrade", buttonX, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT);
        
        // Create targeting button below upgrade button
        targetingButton = new TargetingButton("Targeting", 
            buttonX, 
            buttonY + BUTTON_HEIGHT + BUTTON_SPACING, 
            BUTTON_WIDTH, 
            BUTTON_HEIGHT, 
            selectedTower);
    }
    
    /**
     * Clears all buttons
     */
    private void clearButtons() {
        upgradeButton = null;
        targetingButton = null;
        selectedTower = null;
    }
    
    /**
     * Draws the tower selection UI including buttons and enhanced range indicators
     */
    public void draw(Graphics g) {
        if (selectedTower == null) return;
        
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw enhanced range indicator
        drawEnhancedRangeIndicator(g2d);
        
        // Draw buttons
        if (upgradeButton != null) {
            drawUpgradeButton(g2d);
        }
        
        if (targetingButton != null) {
            targetingButton.draw(g);
        }
    }
    
    /**
     * Draws an enhanced range indicator with strategy-specific effects
     */
    private void drawEnhancedRangeIndicator(Graphics2D g2d) {
        int centerX = selectedTower.getX() + 32; // Tower center
        int centerY = selectedTower.getY() + 32;
        int range = (int) selectedTower.getRange();
        
        // Get current strategy for visual effects
        StrategyType strategy = targetingButton != null ? 
            targetingButton.getCurrentStrategyType() : StrategyType.FIRST;
        
        Color strategyColor = getStrategyColor(strategy);
        
        // Animation time for effects
        long currentTime = System.currentTimeMillis();
        float animationProgress = (currentTime % 3000) / 3000.0f; // 3-second cycle
        
        // Draw semi-transparent range area first (underneath the border)
        drawRangeArea(g2d, centerX, centerY, range, strategyColor);
        
        // Draw strategy-specific range indicator
        switch (strategy) {
            case FIRST:
                drawDashedRangeCircle(g2d, centerX, centerY, range, strategyColor);
                break;
            case LAST:
                drawPulsingRangeCircle(g2d, centerX, centerY, range, strategyColor, animationProgress);
                break;
            case STRONGEST:
                drawThickRangeCircle(g2d, centerX, centerY, range, strategyColor);
                break;
            case WEAKEST:
                drawThinRangeCircle(g2d, centerX, centerY, range, strategyColor, animationProgress);
                break;
        }
        
        // Draw strategy indicator near tower
        drawStrategyIndicator(g2d, centerX, centerY, strategy, strategyColor);
    }
    
    /**
     * Draws a semi-transparent filled area showing the tower's attack range
     */
    private void drawRangeArea(Graphics2D g2d, int centerX, int centerY, int range, Color strategyColor) {
        // Create a semi-transparent version of the strategy color
        Color fillColor = new Color(
            strategyColor.getRed(), 
            strategyColor.getGreen(), 
            strategyColor.getBlue(), 
            40 // Low alpha for subtle visibility
        );
        
        g2d.setColor(fillColor);
        g2d.fillOval(centerX - range, centerY - range, range * 2, range * 2);
    }
    
    /**
     * Draws a dashed range circle for First strategy
     */
    private void drawDashedRangeCircle(Graphics2D g2d, int centerX, int centerY, int range, Color color) {
        g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 150));
        g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{8, 6}, 0));
        g2d.drawOval(centerX - range, centerY - range, range * 2, range * 2);
    }
    
    /**
     * Draws a pulsing range circle for Last strategy
     */
    private void drawPulsingRangeCircle(Graphics2D g2d, int centerX, int centerY, int range, Color color, float progress) {
        float pulseIntensity = (float) (0.7f + 0.3f * Math.sin(progress * Math.PI * 2));
        int alpha = (int) (200 * pulseIntensity);
        float strokeWidth = 2 + 2 * pulseIntensity;
        
        g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha));
        g2d.setStroke(new BasicStroke(strokeWidth));
        g2d.drawOval(centerX - range, centerY - range, range * 2, range * 2);
    }
    
    /**
     * Draws a thick range circle for Strongest strategy
     */
    private void drawThickRangeCircle(Graphics2D g2d, int centerX, int centerY, int range, Color color) {
        g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 180));
        g2d.setStroke(new BasicStroke(4));
        g2d.drawOval(centerX - range, centerY - range, range * 2, range * 2);
        
        // Inner circle for emphasis
        g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 80));
        g2d.setStroke(new BasicStroke(1));
        g2d.drawOval(centerX - range + 8, centerY - range + 8, (range - 8) * 2, (range - 8) * 2);
    }
    
    /**
     * Draws a thin, animated range circle for Weakest strategy
     */
    private void drawThinRangeCircle(Graphics2D g2d, int centerX, int centerY, int range, Color color, float progress) {
        // Multiple thin circles with slight offsets
        for (int i = 0; i < 3; i++) {
            float offset = (float) (2 * Math.sin(progress * Math.PI * 2 + i * Math.PI / 3));
            int alpha = 120 - i * 30;
            
            g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha));
            g2d.setStroke(new BasicStroke(1));
            g2d.drawOval((int)(centerX - range + offset), (int)(centerY - range + offset), 
                        (int)(range * 2 - offset * 2), (int)(range * 2 - offset * 2));
        }
    }
    
    /**
     * Draws a small strategy indicator near the tower
     */
    private void drawStrategyIndicator(Graphics2D g2d, int centerX, int centerY, StrategyType strategy, Color color) {
        int indicatorSize = 8;
        int indicatorX = centerX + 20;
        int indicatorY = centerY - 20;
        
        // Background circle
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillOval(indicatorX - indicatorSize/2, indicatorY - indicatorSize/2, indicatorSize, indicatorSize);
        
        // Strategy color fill
        g2d.setColor(color);
        g2d.fillOval(indicatorX - indicatorSize/2 + 1, indicatorY - indicatorSize/2 + 1, indicatorSize - 2, indicatorSize - 2);
        
        // Strategy symbol
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Monospaced", Font.BOLD, 8));
        String symbol = getStrategySymbol(strategy);
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(symbol, indicatorX - fm.stringWidth(symbol)/2, indicatorY + fm.getAscent()/2 - 1);
    }
    
    /**
     * Gets the color for a strategy type
     */
    private Color getStrategyColor(StrategyType strategy) {
        switch (strategy) {
            case FIRST: return new Color(255, 255, 255); // White
            case LAST: return new Color(255, 100, 100); // Red
            case STRONGEST: return new Color(100, 255, 100); // Green
            case WEAKEST: return new Color(100, 100, 255); // Blue
            default: return new Color(255, 255, 255);
        }
    }
    
    /**
     * Gets a single character symbol for a strategy
     */
    private String getStrategySymbol(StrategyType strategy) {
        switch (strategy) {
            case FIRST: return "1";
            case LAST: return "L";
            case STRONGEST: return "S";
            case WEAKEST: return "W";
            default: return "1";
        }
    }
    
    /**
     * Draws the upgrade button with pixel art styling
     */
    private void drawUpgradeButton(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        
        // Check if tower can be upgraded and if player can afford it
        boolean canUpgrade = selectedTower.isUpgradeable();
        boolean canAfford = canUpgrade && playing.getPlayerManager().getGold() >= getUpgradeCost(selectedTower);
        
        Color bgColor = canAfford ? new Color(100, 150, 100) : new Color(100, 100, 100);
        Color borderColor = new Color(80, 80, 80);
        Color textColor = canAfford ? Color.WHITE : new Color(180, 180, 180);
        
        // Button background
        g2d.setColor(bgColor);
        g2d.fillRect(upgradeButton.getX(), upgradeButton.getY(), upgradeButton.getWidth(), upgradeButton.getHeight());
        
        // Hover effect
        if (upgradeButton.isMouseOver() && canAfford) {
            g2d.setColor(new Color(255, 255, 255, 50));
            g2d.fillRect(upgradeButton.getX() + 1, upgradeButton.getY() + 1, 
                        upgradeButton.getWidth() - 2, upgradeButton.getHeight() - 2);
        }
        
        // Press effect
        if (upgradeButton.isMousePressed() && canAfford) {
            g2d.setColor(new Color(0, 0, 0, 100));
            g2d.fillRect(upgradeButton.getX() + 1, upgradeButton.getY() + 1, 
                        upgradeButton.getWidth() - 2, upgradeButton.getHeight() - 2);
        }
        
        // Border
        g2d.setColor(borderColor);
        g2d.setStroke(new BasicStroke(1));
        g2d.drawRect(upgradeButton.getX(), upgradeButton.getY(), upgradeButton.getWidth(), upgradeButton.getHeight());
        
        // Text
        g2d.setColor(textColor);
        g2d.setFont(new Font("Monospaced", Font.BOLD, 10));
        String text = canUpgrade ? ("Upgrade $" + getUpgradeCost(selectedTower)) : "Max Level";
        FontMetrics fm = g2d.getFontMetrics();
        int textX = upgradeButton.getX() + (upgradeButton.getWidth() - fm.stringWidth(text)) / 2;
        int textY = upgradeButton.getY() + (upgradeButton.getHeight() + fm.getAscent()) / 2;
        g2d.drawString(text, textX, textY);
        
        // If not affordable, draw X overlay
        if (canUpgrade && !canAfford) {
            g2d.setColor(new Color(200, 0, 0, 180));
            g2d.setStroke(new BasicStroke(2));
            g2d.drawLine(upgradeButton.getX() + 4, upgradeButton.getY() + 4, 
                        upgradeButton.getX() + upgradeButton.getWidth() - 4, 
                        upgradeButton.getY() + upgradeButton.getHeight() - 4);
            g2d.drawLine(upgradeButton.getX() + upgradeButton.getWidth() - 4, upgradeButton.getY() + 4, 
                        upgradeButton.getX() + 4, upgradeButton.getY() + upgradeButton.getHeight() - 4);
        }
    }
    
    /**
     * Handles mouse movement for button hover detection
     */
    public void mouseMoved(int mouseX, int mouseY) {
        if (upgradeButton != null) {
            upgradeButton.setMouseOver(upgradeButton.getBounds().contains(mouseX, mouseY));
        }
        if (targetingButton != null) {
            targetingButton.setMouseOver(targetingButton.getBounds().contains(mouseX, mouseY));
        }
    }
    
    /**
     * Handles mouse clicks on buttons
     * @return true if a button was clicked and handled, false otherwise
     */
    public boolean mouseClicked(int mouseX, int mouseY) {
        boolean handled = false;
        
        if (upgradeButton != null && upgradeButton.getBounds().contains(mouseX, mouseY)) {
            if (selectedTower.isUpgradeable()) {
                int upgradeCost = getUpgradeCost(selectedTower);
                if (playing.getPlayerManager().getGold() >= upgradeCost) {
                    // Spend the gold
                    playing.getPlayerManager().spendGold(upgradeCost);
                    
                    // Handle upgrade logic
                    Tower upgradedTower = selectedTower.upgrade();
                    playing.getTowerManager().replaceTower(selectedTower, upgradedTower);
                    
                    // Update the selection to the new tower
                    setSelectedTower(upgradedTower);
                    
                    // Trigger upgrade effect
                    playing.getTowerManager().triggerUpgradeEffect(upgradedTower);
                    
                    // Update UI resources
                    playing.updateUIResources();
                    
                    System.out.println("Tower upgraded!");
                }
            }
            handled = true;
        }
        
        if (targetingButton != null && targetingButton.getBounds().contains(mouseX, mouseY)) {
            targetingButton.cycleStrategy();
            handled = true;
        }
        
        return handled;
    }
    
    /**
     * Helper to get upgrade cost
     */
    private int getUpgradeCost(Tower tower) {
        switch (tower.getType()) {
            case 0: return 75; // Archer
            case 1: return 120; // Artillery
            case 2: return 100; // Mage
            default: return 100;
        }
    }
    
    /**
     * Handles mouse press for button visual feedback
     */
    public void mousePressed(int mouseX, int mouseY) {
        if (upgradeButton != null && upgradeButton.getBounds().contains(mouseX, mouseY)) {
            upgradeButton.setMousePressed(true);
        }
        if (targetingButton != null && targetingButton.getBounds().contains(mouseX, mouseY)) {
            targetingButton.setMousePressed(true);
        }
    }
    
    /**
     * Handles mouse release
     */
    public void mouseReleased() {
        if (upgradeButton != null) {
            upgradeButton.setMousePressed(false);
        }
        if (targetingButton != null) {
            targetingButton.setMousePressed(false);
        }
    }
    
    /**
     * Gets the currently selected tower
     */
    public Tower getSelectedTower() {
        return selectedTower;
    }
    
    /**
     * Checks if a tower is currently selected
     */
    public boolean hasTowerSelected() {
        return selectedTower != null;
    }
} 