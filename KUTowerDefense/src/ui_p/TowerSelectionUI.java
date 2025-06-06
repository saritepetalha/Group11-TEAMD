package ui_p;

import objects.Tower;
import strategies.TargetingStrategyFactory.StrategyType;
import scenes.Playing;
import constants.GameDimensions;
import objects.WizardWarrior;
import objects.ArcherWarrior;
import objects.Warrior;

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
    private TheButton reviveButton;
    private TheButton spawnButton;
    private TheButton lightUpgradeButton;
    private CostTooltip tooltip;

    // UI positioning
    private static final int BUTTON_WIDTH = 80;
    private static final int BUTTON_HEIGHT = 24;
    private static final int BUTTON_SPACING = 8;

    // Range indicator effects
    private long animationStartTime = System.currentTimeMillis();

    public TowerSelectionUI(Playing playing) {
        this.playing = playing;
        this.tooltip = new CostTooltip();
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

        if (selectedTower.isDestroyed()) {
            reviveButton = new TheButton("Revive", buttonX, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT);
            upgradeButton = null;
            targetingButton = null;
            spawnButton = null;
            lightUpgradeButton = null;
        } else {
            int currentButtonY = buttonY;
            // Create upgrade button
            upgradeButton = new TheButton("Upgrade", buttonX, currentButtonY, BUTTON_WIDTH, BUTTON_HEIGHT);
            currentButtonY += BUTTON_HEIGHT + BUTTON_SPACING;

            // Create targeting button below upgrade button
            targetingButton = new TargetingButton("Targeting",
                    buttonX,
                    currentButtonY,
                    BUTTON_WIDTH,
                    BUTTON_HEIGHT,
                    selectedTower);
            currentButtonY += BUTTON_HEIGHT + BUTTON_SPACING;

            // Create spawn button below targeting button, only for Archer and Mage Towers
            // Need to check through all decorator layers to find the base tower type
            boolean canSpawn = canTowerSpawn(selectedTower);
            
            if (canSpawn) {
                spawnButton = new TheButton("Spawn",
                        buttonX,
                        currentButtonY,
                        BUTTON_WIDTH,
                        BUTTON_HEIGHT);
                currentButtonY += BUTTON_HEIGHT + BUTTON_SPACING;        
            } else {
                spawnButton = null;
            }
 
            // Create light upgrade button below the last button
            lightUpgradeButton = new TheButton("Light",
                    buttonX,
                    currentButtonY, // Positioned below the previous button
                    BUTTON_WIDTH,
                    BUTTON_HEIGHT);
 
            reviveButton = null;
        }
    }

    /**
     * Clears all buttons
     */
    private void clearButtons() {
        upgradeButton = null;
        targetingButton = null;
        reviveButton = null;
        spawnButton = null;
        lightUpgradeButton = null;
        selectedTower = null;
    }

    /**
     * Draws the tower selection UI including buttons and enhanced range indicators
     */
    public void draw(Graphics g) {
        if (selectedTower == null) return;

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw range preview if hovering upgrade button
        if (upgradeButton != null && upgradeButton.isMouseOver()) {
            int centerX = selectedTower.getX() + selectedTower.getWidth() / 2;
            int centerY = selectedTower.getY() + selectedTower.getHeight() / 2;
            float baseRange = selectedTower.getRange();
            float previewRange = baseRange;

            // Calculate upgraded range based on tower type
            if (selectedTower.getLevel() == 1) {
                switch (selectedTower.getType()) {
                    case 0: // Archer
                        previewRange *= 1.5f;
                        break;
                    case 1: // Artillery
                        previewRange *= 1.2f;
                        break;
                    case 2: // Mage
                        // Mage range does not change on upgrade
                        break;
                }
            }

            // Apply weather effects to range preview
            if (playing.getWeatherManager().isRaining()) {
                previewRange *= playing.getWeatherManager().getTowerRangeMultiplier();
            }

            // Add a small buffer to account for enemy size
            float enemySize = 32f; // Average enemy size
            float adjustedRange = previewRange + enemySize / 2;

            // Draw semi-transparent range area
            g2d.setColor(new Color(100, 200, 255, 40));
            g2d.fillOval(centerX - (int) adjustedRange, centerY - (int) adjustedRange,
                    (int) adjustedRange * 2, (int) adjustedRange * 2);

            // Draw range border
            g2d.setColor(new Color(100, 200, 255, 180));
            g2d.setStroke(new BasicStroke(2));
            g2d.drawOval(centerX - (int) adjustedRange, centerY - (int) adjustedRange,
                    (int) adjustedRange * 2, (int) adjustedRange * 2);
        }

        // Draw enhanced range indicator
        drawEnhancedRangeIndicator(g2d);

        // Draw buttons
        if (selectedTower.isDestroyed() && reviveButton != null) {
            drawReviveButton(g2d);
        } else {
            if (upgradeButton != null) {
                drawUpgradeButton(g2d);
            }
            if (targetingButton != null) {
                targetingButton.draw(g);
            }
            if (spawnButton != null) {
                drawSpawnButton(g2d);
            }
            if (lightUpgradeButton != null) {
                drawLightUpgradeButton(g2d);
            }
        }
        
        // Update and draw tooltip
        tooltip.update();
        tooltip.draw(g2d);
    }

    /**
     * Draws an enhanced range indicator with strategy-specific effects
     */
    private void drawEnhancedRangeIndicator(Graphics2D g2d) {
        int centerX = selectedTower.getX() + selectedTower.getWidth() / 2; // Tower center
        int centerY = selectedTower.getY() + selectedTower.getHeight() / 2;
        float baseRange = selectedTower.getRange();
        float effectiveRange = baseRange;

        // Apply weather effects to range display
        if (playing.getWeatherManager().isRaining()) {
            effectiveRange *= playing.getWeatherManager().getTowerRangeMultiplier();
        }

        // Add a small buffer to account for enemy size
        float enemySize = 32f; // Average enemy size
        float adjustedRange = effectiveRange + enemySize/2;

        int range = (int) adjustedRange;

        // Get current strategy for visual effects
        StrategyType strategy = targetingButton != null ?
                targetingButton.getCurrentStrategyType() : StrategyType.FIRST;

        Color strategyColor = getStrategyColor(strategy);

        // Modify color for rainy mode to make it more visible
        if (playing.getWeatherManager().isRaining()) {
            strategyColor = new Color(100, 150, 255); // Blue tint for rain
        }

        // Animation time for effects
        long currentTime = System.currentTimeMillis();
        float animationProgress = (currentTime % 3000) / 3000.0f; // 3-second cycle

        // Draw semi-transparent range area first (underneath the border)
        drawRangeArea(g2d, centerX, centerY, range, strategyColor);

        // Draw strategy-specific range indicator
        if (playing.getWeatherManager().isRaining()) {
            // Special rainy mode indicator - thick blue circle with rain effect
            drawRainyRangeCircle(g2d, centerX, centerY, range, strategyColor, animationProgress);
        } else {
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
     * Draws a special range circle for rainy weather with reduced range
     */
    private void drawRainyRangeCircle(Graphics2D g2d, int centerX, int centerY, int range, Color color, float progress) {
        // Draw a thick blue circle with animated rain drops effect
        float pulseIntensity = (float) (0.8f + 0.2f * Math.sin(progress * Math.PI * 4));
        int alpha = (int) (220 * pulseIntensity);
        float strokeWidth = 3 + 1 * pulseIntensity;

        // Main range circle
        g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha));
        g2d.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{6, 4}, 0));
        g2d.drawOval(centerX - range, centerY - range, range * 2, range * 2);

        // Inner warning circle to show reduced range
        g2d.setColor(new Color(255, 255, 100, 100)); // Yellow warning
        g2d.setStroke(new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{3, 3}, 0));
        g2d.drawOval(centerX - range + 5, centerY - range + 5, (range - 5) * 2, (range - 5) * 2);

        // Add rain drop indicators around the circle
        for (int i = 0; i < 8; i++) {
            double angle = (i * Math.PI * 2 / 8) + (progress * Math.PI * 2);
            int dropX = (int) (centerX + (range + 10) * Math.cos(angle));
            int dropY = (int) (centerY + (range + 10) * Math.sin(angle));

            g2d.setColor(new Color(100, 150, 255, 150));
            g2d.setStroke(new BasicStroke(2));
            g2d.drawLine(dropX, dropY, dropX, dropY + 6);
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
        String text = canUpgrade ? "Upgrade" : "Max Level";
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

    private void drawReviveButton(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        int reviveCost = getUpgradeCost(selectedTower);
        boolean canAfford = playing.getPlayerManager().getGold() >= reviveCost;
        Color bgColor = canAfford ? new Color(100, 150, 100) : new Color(100, 100, 100);
        Color borderColor = new Color(80, 80, 80);
        Color textColor = canAfford ? Color.WHITE : new Color(180, 180, 180);
        g2d.setColor(bgColor);
        g2d.fillRect(reviveButton.getX(), reviveButton.getY(), reviveButton.getWidth(), reviveButton.getHeight());
        if (reviveButton.isMouseOver() && canAfford) {
            g2d.setColor(new Color(255, 255, 255, 50));
            g2d.fillRect(reviveButton.getX() + 1, reviveButton.getY() + 1,
                    reviveButton.getWidth() - 2, reviveButton.getHeight() - 2);
        }
        if (reviveButton.isMousePressed() && canAfford) {
            g2d.setColor(new Color(0, 0, 0, 100));
            g2d.fillRect(reviveButton.getX() + 1, reviveButton.getY() + 1,
                    reviveButton.getWidth() - 2, reviveButton.getHeight() - 2);
        }
        g2d.setColor(borderColor);
        g2d.setStroke(new BasicStroke(1));
        g2d.drawRect(reviveButton.getX(), reviveButton.getY(), reviveButton.getWidth(), reviveButton.getHeight());
        g2d.setColor(textColor);
        g2d.setFont(new Font("Monospaced", Font.BOLD, 10));
        String text = "Revive";
        FontMetrics fm = g2d.getFontMetrics();
        int textX = reviveButton.getX() + (reviveButton.getWidth() - fm.stringWidth(text)) / 2;
        int textY = reviveButton.getY() + (reviveButton.getHeight() + fm.getAscent()) / 2 - 2;
        g2d.drawString(text, textX, textY);
    }

    private void drawSpawnButton(Graphics2D g2d) {
        // Determine warrior type and cost for affordability check
        Warrior tempWarrior = null;
        Color bgColor = new Color(100, 100, 100); // Default greyed out
        Color textColor = new Color(180, 180, 180); // Default greyed out text
        Color borderColor = new Color(80, 80, 80);
        boolean canAfford = false;
        int spawnCost = 0;
        String text = "Spawn"; // Default text

        // Unwrap all decorators to get the base tower type
        Tower baseTower = selectedTower;
        while (baseTower instanceof objects.TowerDecorator) {
            baseTower = ((objects.TowerDecorator) baseTower).decoratedTower;
        }

        if (baseTower instanceof objects.MageTower) {
            tempWarrior = new WizardWarrior(selectedTower.getX(), selectedTower.getY());
        } else if (baseTower instanceof objects.ArcherTower) {
            tempWarrior = new ArcherWarrior(selectedTower.getX(), selectedTower.getY());
        }

        if (tempWarrior != null) {
            spawnCost = tempWarrior.getCost();
            canAfford = playing.getPlayerManager().getGold() >= spawnCost && selectedTower.canSpawnWarrior();
            
            // Simple button text without count (count will be shown in tooltip)
            text = "Spawn";
            
            if (canAfford) {
                bgColor = spawnButton.isMouseOver() ? new Color(120, 170, 120) : new Color(100, 150, 100);
                textColor = Color.WHITE;
            } else {
                // bgColor and textColor already set to greyed out defaults
            }
        } else {
            // This case (spawn button for non-spawnable tower) should ideally not happen
            // if createButtons() logic is correct. Default colors are already set.
             canAfford = false; 
        }

        g2d.setColor(bgColor);
        g2d.fillRect(spawnButton.getX(), spawnButton.getY(), spawnButton.getWidth(), spawnButton.getHeight());
        
        // Hover and Press effects (only if affordable)
        if (canAfford) {
            if (spawnButton.isMouseOver()) {
                g2d.setColor(new Color(255, 255, 255, 50)); // Brighter for hover
                g2d.fillRect(spawnButton.getX() + 1, spawnButton.getY() + 1,
                        spawnButton.getWidth() - 2, spawnButton.getHeight() - 2);
            }
            if (spawnButton.isMousePressed()) {
                g2d.setColor(new Color(0, 0, 0, 100)); // Darker for press
                g2d.fillRect(spawnButton.getX() + 1, spawnButton.getY() + 1,
                        spawnButton.getWidth() - 2, spawnButton.getHeight() - 2);
            }
        }

        g2d.setColor(borderColor);
        g2d.setStroke(new BasicStroke(1));
        g2d.drawRect(spawnButton.getX(), spawnButton.getY(), spawnButton.getWidth(), spawnButton.getHeight());
        
        g2d.setColor(textColor);
        g2d.setFont(new Font("Monospaced", Font.BOLD, 10));
        FontMetrics fm = g2d.getFontMetrics();
        int textX = spawnButton.getX() + (spawnButton.getWidth() - fm.stringWidth(text)) / 2;
        int textY = spawnButton.getY() + (spawnButton.getHeight() + fm.getAscent()) / 2 - 2;
        g2d.drawString(text, textX, textY);

        // If not affordable, draw X overlay
        if (!canAfford && tempWarrior != null) { 
            g2d.setColor(new Color(200, 0, 0, 180));
            g2d.setStroke(new BasicStroke(2));
            g2d.drawLine(spawnButton.getX() + 4, spawnButton.getY() + 4,
                    spawnButton.getX() + spawnButton.getWidth() - 4,
                    spawnButton.getY() + spawnButton.getHeight() - 4);
            g2d.drawLine(spawnButton.getX() + spawnButton.getWidth() - 4, spawnButton.getY() + 4,
                    spawnButton.getX() + 4, spawnButton.getY() + spawnButton.getHeight() - 4);
        }
    }

    /**
     * Draws the light upgrade button with pixel art styling
     */
    private void drawLightUpgradeButton(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

        // Check if tower can be upgraded with light and if player can afford it
        boolean canUpgrade = playing.getTowerManager().canUpgradeWithLight(selectedTower);
        int lightCost = playing.getTowerManager().getLightUpgradeCost();
        boolean canAfford = canUpgrade && playing.getPlayerManager().getGold() >= lightCost;

        Color bgColor = canAfford ? new Color(255, 215, 0) : new Color(100, 100, 100); // Gold color for light
        Color borderColor = new Color(80, 80, 80);
        Color textColor = canAfford ? Color.BLACK : new Color(180, 180, 180);

        // Button background
        g2d.setColor(bgColor);
        g2d.fillRect(lightUpgradeButton.getX(), lightUpgradeButton.getY(),
                lightUpgradeButton.getWidth(), lightUpgradeButton.getHeight());

        // Hover effect
        if (lightUpgradeButton.isMouseOver() && canAfford) {
            g2d.setColor(new Color(255, 255, 255, 80));
            g2d.fillRect(lightUpgradeButton.getX() + 1, lightUpgradeButton.getY() + 1,
                    lightUpgradeButton.getWidth() - 2, lightUpgradeButton.getHeight() - 2);
        }

        // Press effect
        if (lightUpgradeButton.isMousePressed() && canAfford) {
            g2d.setColor(new Color(0, 0, 0, 100));
            g2d.fillRect(lightUpgradeButton.getX() + 1, lightUpgradeButton.getY() + 1,
                    lightUpgradeButton.getWidth() - 2, lightUpgradeButton.getHeight() - 2);
        }

        // Border
        g2d.setColor(borderColor);
        g2d.setStroke(new BasicStroke(1));
        g2d.drawRect(lightUpgradeButton.getX(), lightUpgradeButton.getY(),
                lightUpgradeButton.getWidth(), lightUpgradeButton.getHeight());

        // Text
        g2d.setColor(textColor);
        g2d.setFont(new Font("Monospaced", Font.BOLD, 10));
        String text = canUpgrade ? "Light" : "Has Light";
        FontMetrics fm = g2d.getFontMetrics();
        int textX = lightUpgradeButton.getX() + (lightUpgradeButton.getWidth() - fm.stringWidth(text)) / 2;
        int textY = lightUpgradeButton.getY() + (lightUpgradeButton.getHeight() + fm.getAscent()) / 2;
        g2d.drawString(text, textX, textY);

        // If not affordable, draw X overlay
        if (canUpgrade && !canAfford) {
            g2d.setColor(new Color(200, 0, 0, 180));
            g2d.setStroke(new BasicStroke(2));
            g2d.drawLine(lightUpgradeButton.getX() + 4, lightUpgradeButton.getY() + 4,
                    lightUpgradeButton.getX() + lightUpgradeButton.getWidth() - 4,
                    lightUpgradeButton.getY() + lightUpgradeButton.getHeight() - 4);
            g2d.drawLine(lightUpgradeButton.getX() + lightUpgradeButton.getWidth() - 4,
                    lightUpgradeButton.getY() + 4,
                    lightUpgradeButton.getX() + 4,
                    lightUpgradeButton.getY() + lightUpgradeButton.getHeight() - 4);
        }

        // Light effect when hovering (preview the light range)
        if (lightUpgradeButton.isMouseOver() && canUpgrade) {
            float lightRadius = selectedTower.getRange() * 0.7f;
            int centerX = selectedTower.getX() + selectedTower.getWidth() / 2;
            int centerY = selectedTower.getY() + selectedTower.getHeight() / 2;

            // Draw preview light circle
            g2d.setColor(new Color(255, 255, 150, 60));
            g2d.fillOval(centerX - (int)lightRadius, centerY - (int)lightRadius,
                    (int)lightRadius * 2, (int)lightRadius * 2);

            g2d.setColor(new Color(255, 255, 100, 120));
            g2d.setStroke(new BasicStroke(2.0f));
            g2d.drawOval(centerX - (int)lightRadius, centerY - (int)lightRadius,
                    (int)lightRadius * 2, (int)lightRadius * 2);
        }
    }

    /**
     * Handles mouse movement for button hover detection and tooltips
     */
    public void mouseMoved(int mouseX, int mouseY) {
        boolean tooltipShown = false;
        
        if (upgradeButton != null) {
            boolean isHover = upgradeButton.getBounds().contains(mouseX, mouseY);
            upgradeButton.setMouseOver(isHover);
            
            if (isHover) {
                boolean canUpgrade = selectedTower.isUpgradeable();
                int upgradeCost = getUpgradeCost(selectedTower);
                boolean canAfford = canUpgrade && playing.getPlayerManager().getGold() >= upgradeCost;
                
                String description = canUpgrade ? 
                    "Upgrade this tower to increase its damage and capabilities." :
                    "This tower is already at maximum level.";
                
                tooltip.show("Upgrade Tower", canUpgrade ? upgradeCost : 0, description, canAfford, mouseX, mouseY);
                tooltipShown = true;
            }
        }
        
        if (!tooltipShown && spawnButton != null) {
            boolean isHover = spawnButton.getBounds().contains(mouseX, mouseY);
            spawnButton.setMouseOver(isHover);
            
            if (isHover) {
                // Determine warrior type and cost
                Tower baseTower = selectedTower;
                while (baseTower instanceof objects.TowerDecorator) {
                    baseTower = ((objects.TowerDecorator) baseTower).decoratedTower;
                }
                
                Warrior tempWarrior = null;
                String description = "";
                
                if (baseTower instanceof objects.MageTower) {
                    tempWarrior = new WizardWarrior(0, 0);
                    description = "Spawn a Wizard Warrior that can cast spells and fight enemies.";
                } else if (baseTower instanceof objects.ArcherTower) {
                    tempWarrior = new ArcherWarrior(0, 0);
                    description = "Spawn an Archer Warrior with ranged attacks and high mobility.";
                }
                
                if (tempWarrior != null) {
                    int spawnCost = tempWarrior.getCost();
                    boolean canAfford = playing.getPlayerManager().getGold() >= spawnCost && selectedTower.canSpawnWarrior();
                    
                    // Create a well-formatted description for the spawn button
                    String spawnDescription = 
                        "Deploy a warrior to fight enemies. Warriors have a 30-second lifetime. " +
                        "Can only spawn within 3 tiles of this tower.";
                    
                    // Enhanced title with warrior count
                    String spawnTitle = String.format("Spawn Warrior (%d/%d)", 
                        selectedTower.getCurrentWarriors(), 
                        selectedTower.getMaxWarriors());
                    
                    tooltip.show(spawnTitle, spawnCost, spawnDescription, canAfford, mouseX, mouseY);
                    tooltipShown = true;
                }
            }
        }
        
        if (!tooltipShown && lightUpgradeButton != null) {
            boolean isHover = lightUpgradeButton.getBounds().contains(mouseX, mouseY);
            lightUpgradeButton.setMouseOver(isHover);
            
            if (isHover) {
                boolean canUpgrade = playing.getTowerManager().canUpgradeWithLight(selectedTower);
                int lightCost = playing.getTowerManager().getLightUpgradeCost();
                boolean canAfford = canUpgrade && playing.getPlayerManager().getGold() >= lightCost;
                
                String description = canUpgrade ? 
                    "Add light enhancement to make this tower damage nearby enemies with light." :
                    "This tower already has light enhancement.";
                
                tooltip.show("Light Enhancement", canUpgrade ? lightCost : 0, description, canAfford, mouseX, mouseY);
                tooltipShown = true;
            }
        }
        
        if (!tooltipShown && reviveButton != null) {
            boolean isHover = reviveButton.getBounds().contains(mouseX, mouseY);
            reviveButton.setMouseOver(isHover);
            
            if (isHover) {
                int reviveCost = getUpgradeCost(selectedTower);
                boolean canAfford = playing.getPlayerManager().getGold() >= reviveCost;
                
                tooltip.show("Revive Tower", reviveCost, 
                    "Bring this destroyed tower back to life with full functionality.", 
                    canAfford, mouseX, mouseY);
                tooltipShown = true;
            }
        }
        
        if (targetingButton != null) {
            targetingButton.setMouseOver(targetingButton.getBounds().contains(mouseX, mouseY));
        }
        
        if (!tooltipShown) {
            tooltip.hide();
        }
    }

    /**
     * Handles mouse clicks on buttons
     * @return true if a button was clicked and handled, false otherwise
     */
    public boolean mouseClicked(int mouseX, int mouseY) {
        boolean handled = false;

        // Revive button interaction
        if (reviveButton != null && reviveButton.getBounds().contains(mouseX, mouseY)) {
            int reviveCost = getUpgradeCost(selectedTower);
            if (playing.getPlayerManager().getGold() >= reviveCost) {
                playing.getPlayerManager().spendGold(reviveCost);
                selectedTower.revive();
                setSelectedTower(selectedTower); // Refresh buttons
                playing.updateUIResources();
                System.out.println("Tower revived!");
            }
            handled = true;
        }

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

        if (lightUpgradeButton != null && lightUpgradeButton.getBounds().contains(mouseX, mouseY)) {
            if (playing.getTowerManager().canUpgradeWithLight(selectedTower)) {
                int lightCost = playing.getTowerManager().getLightUpgradeCost();
                if (playing.getPlayerManager().getGold() >= lightCost) {
                    // Spend the gold
                    playing.getPlayerManager().spendGold(lightCost);

                    // Handle light upgrade logic
                    objects.LightDecorator lightTower = playing.getTowerManager().upgradeTowerWithLight(selectedTower);

                    if (lightTower != null) {
                        // Update the selection to the new light tower
                        setSelectedTower(lightTower);

                        // Update UI resources
                        playing.updateUIResources();

                        System.out.println("Tower upgraded with light!");
                    }
                }
            }
            handled = true;
        }

        if (targetingButton != null && targetingButton.getBounds().contains(mouseX, mouseY)) {
            targetingButton.cycleStrategy();
            handled = true;
        }

        if (spawnButton != null && spawnButton.getBounds().contains(mouseX, mouseY)) {
            Warrior warriorToSpawn = null;

            // Determine the base tower type by unwrapping all decorators
            Tower baseTower = selectedTower;
            while (baseTower instanceof objects.TowerDecorator) {
                baseTower = ((objects.TowerDecorator) baseTower).decoratedTower;
            }

            // Get the tower's spawn position (center of the tower)
            int spawnX = selectedTower.getX() + selectedTower.getWidth() / 2 - 32; // Center and offset for warrior size
            int spawnY = selectedTower.getY() + selectedTower.getHeight() / 2 - 32;

            // Check the type of the base tower and create warrior for spawning
            if (baseTower instanceof objects.MageTower) {
                // Create wizard warrior at spawn position, target will be set during placement
                warriorToSpawn = new WizardWarrior(spawnX, spawnY, spawnX, spawnY); // Same position initially
                // Store spawn position for later use
                warriorToSpawn.setX(spawnX);
                warriorToSpawn.setY(spawnY);
            } else if (baseTower instanceof objects.ArcherTower) {
                // Create archer warrior at spawn position, target will be set during placement
                warriorToSpawn = new ArcherWarrior(spawnX, spawnY, spawnX, spawnY); // Same position initially
                // Store spawn position for later use  
                warriorToSpawn.setX(spawnX);
                warriorToSpawn.setY(spawnY);
            }

            if (warriorToSpawn != null) {
                if (playing.getPlayerManager().getGold() >= warriorToSpawn.getCost() && selectedTower.canSpawnWarrior()) {
                    // Set the tower reference in the warrior
                    warriorToSpawn.setSpawnedFromTower(selectedTower);
                    
                    // Gold and warrior limit check successful, proceed to placement mode
                    playing.startWarriorPlacement(warriorToSpawn);
                } else {
                    if (playing.getPlayerManager().getGold() < warriorToSpawn.getCost()) {
                        System.out.println("Not enough gold to spawn warrior!");
                    } else {
                        System.out.println("Tower already has maximum warriors (" + selectedTower.getMaxWarriors() + ")!");
                    }
                }
            }
            handled = true;
        }

        return handled;
    }

    /**
     * Helper method to check if a tower can spawn warriors
     * Recursively unwraps decorators to find the base tower type
     */
    private boolean canTowerSpawn(Tower tower) {
        Tower currentTower = tower;
        
        // Unwrap all decorator layers to find the base tower
        while (currentTower instanceof objects.TowerDecorator) {
            currentTower = ((objects.TowerDecorator) currentTower).decoratedTower;
        }
        
        // Check if the base tower is an Archer or Mage tower
        return currentTower instanceof objects.ArcherTower || currentTower instanceof objects.MageTower;
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
        if (spawnButton != null && spawnButton.getBounds().contains(mouseX, mouseY)) {
            spawnButton.setMousePressed(true);
        }
        if (lightUpgradeButton != null && lightUpgradeButton.getBounds().contains(mouseX, mouseY)) {
            lightUpgradeButton.setMousePressed(true);
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
        if (spawnButton != null) {
            spawnButton.setMousePressed(false);
        }
        if (lightUpgradeButton != null) {
            lightUpgradeButton.setMousePressed(false);
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