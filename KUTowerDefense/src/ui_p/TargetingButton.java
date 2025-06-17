package ui_p;

import strategies.TargetingStrategy;
import strategies.TargetingStrategyFactory;
import strategies.TargetingStrategyFactory.StrategyType;
import objects.Tower;

import java.awt.*;

/**
 * Specialized button for changing tower targeting strategies at runtime.
 * Features pixel art styling and visual feedback for different strategies.
 */
public class TargetingButton extends TheButton {
    
    private Tower associatedTower;
    private StrategyType currentStrategyType;
    private long lastClickTime = 0;
    private static final long TOOLTIP_DISPLAY_DURATION = 2000; // 2 seconds
    private boolean showTooltip = false;
    
    // Strategy colors for visual feedback
    private static final Color FIRST_STRATEGY_COLOR = new Color(255, 255, 255); // White
    private static final Color LAST_STRATEGY_COLOR = new Color(255, 100, 100); // Red
    private static final Color STRONGEST_STRATEGY_COLOR = new Color(100, 255, 100); // Green
    private static final Color WEAKEST_STRATEGY_COLOR = new Color(100, 100, 255); // Blue
    
    public TargetingButton(String text, int x, int y, int width, int height, Tower tower) {
        super(text, x, y, width, height);
        this.associatedTower = tower;
        this.currentStrategyType = getCurrentStrategyType(tower.getTargetingStrategy());
    }
    
    /**
     * Determines the strategy type from the current targeting strategy
     */
    private StrategyType getCurrentStrategyType(TargetingStrategy strategy) {
        String strategyName = strategy.getStrategyName();
        switch (strategyName) {
            case "First": return StrategyType.FIRST;
            case "Last": return StrategyType.LAST;
            case "Strongest": return StrategyType.STRONGEST;
            case "Weakest": return StrategyType.WEAKEST;
            default: return StrategyType.FIRST;
        }
    }
    
    /**
     * Cycles to the next targeting strategy
     */
    public void cycleStrategy() {
        StrategyType[] strategies = StrategyType.values();
        int currentIndex = currentStrategyType.ordinal();
        int nextIndex = (currentIndex + 1) % strategies.length;
        
        currentStrategyType = strategies[nextIndex];
        TargetingStrategy newStrategy = TargetingStrategyFactory.createStrategy(currentStrategyType);
        associatedTower.setTargetingStrategy(newStrategy);
        
        // Show tooltip for a brief moment
        lastClickTime = System.currentTimeMillis();
        showTooltip = true;
        
        System.out.println("Tower targeting strategy changed to: " + newStrategy.getStrategyName());
    }
    
    /**
     * Gets the color associated with the current strategy
     */
    public Color getCurrentStrategyColor() {
        switch (currentStrategyType) {
            case FIRST: return FIRST_STRATEGY_COLOR;
            case LAST: return LAST_STRATEGY_COLOR;
            case STRONGEST: return STRONGEST_STRATEGY_COLOR;
            case WEAKEST: return WEAKEST_STRATEGY_COLOR;
            default: return FIRST_STRATEGY_COLOR;
        }
    }
    
    /**
     * Gets the icon character for the current strategy
     */
    public String getStrategyIcon() {
        switch (currentStrategyType) {
            case FIRST: return "1st";
            case LAST: return "End";
            case STRONGEST: return "Max";
            case WEAKEST: return "Min";
            default: return "1st";
        }
    }
    
    /**
     * Custom draw method with pixel art styling and strategy feedback
     */
    @Override
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF); // Pixel art style
        
        // Update tooltip visibility
        if (showTooltip && System.currentTimeMillis() - lastClickTime > TOOLTIP_DISPLAY_DURATION) {
            showTooltip = false;
        }
        
        // Draw button background with strategy color accent
        Color strategyColor = getCurrentStrategyColor();
        Color baseColor = new Color(60, 60, 60); // Dark base
        Color borderColor = new Color(40, 40, 40); // Darker border
        
        // Button background
        g2d.setColor(baseColor);
        g2d.fillRect(x, y, width, height);
        
        // Strategy color accent bar on left side
        g2d.setColor(strategyColor);
        g2d.fillRect(x, y, 3, height);
        
        // Border
        g2d.setColor(borderColor);
        g2d.setStroke(new BasicStroke(1));
        g2d.drawRect(x, y, width, height);
        
        // Hover effect
        if (isMouseOver()) {
            long currentTime = System.currentTimeMillis();
            float alpha = (float) (0.3f + 0.2f * Math.sin(currentTime * 0.008)); // Subtle pulse
            g2d.setColor(new Color(strategyColor.getRed(), strategyColor.getGreen(), strategyColor.getBlue(), (int)(alpha * 255)));
            g2d.fillRect(x + 1, y + 1, width - 2, height - 2);
        }
        
        // Press effect
        if (isMousePressed()) {
            g2d.setColor(new Color(0, 0, 0, 100));
            g2d.fillRect(x + 1, y + 1, width - 2, height - 2);
        }
        
        // Draw crosshair icon
        drawCrosshairIcon(g2d);
        
        // Draw strategy text
        drawStrategyText(g2d);
        
        // Draw tooltip if visible
        if (showTooltip) {
            drawTooltip(g2d);
        }
    }
    
    /**
     * Draws a crosshair icon for targeting
     */
    private void drawCrosshairIcon(Graphics2D g2d) {
        int centerX = x + width / 4;
        int centerY = y + height / 2;
        int size = Math.min(width, height) / 6;
        
        g2d.setColor(getCurrentStrategyColor());
        g2d.setStroke(new BasicStroke(2));
        
        // Horizontal line
        g2d.drawLine(centerX - size, centerY, centerX + size, centerY);
        // Vertical line
        g2d.drawLine(centerX, centerY - size, centerX, centerY + size);
        
        // Center dot
        g2d.fillOval(centerX - 1, centerY - 1, 3, 3);
    }
    
    /**
     * Draws the strategy text/icon
     */
    private void drawStrategyText(Graphics2D g2d) {
        Font pixelFont = new Font("Monospaced", Font.BOLD, 10);
        g2d.setFont(pixelFont);
        g2d.setColor(Color.WHITE);
        
        String strategyIcon = getStrategyIcon();
        FontMetrics fm = g2d.getFontMetrics();
        int textX = x + width * 3/4 - fm.stringWidth(strategyIcon) / 2;
        int textY = y + height / 2 + fm.getAscent() / 2;
        
        // Text shadow for better readability
        g2d.setColor(Color.BLACK);
        g2d.drawString(strategyIcon, textX + 1, textY + 1);
        
        g2d.setColor(getCurrentStrategyColor());
        g2d.drawString(strategyIcon, textX, textY);
    }
    
    /**
     * Draws tooltip showing current strategy
     */
    private void drawTooltip(Graphics2D g2d) {
        String tooltipText = "Target: " + associatedTower.getTargetingStrategy().getStrategyName() + " Enemy";
        Font tooltipFont = new Font("Monospaced", Font.PLAIN, 11);
        g2d.setFont(tooltipFont);
        
        FontMetrics fm = g2d.getFontMetrics();
        int tooltipWidth = fm.stringWidth(tooltipText) + 12;
        int tooltipHeight = fm.getHeight() + 8;
        
        int tooltipX = x - tooltipWidth / 2 + width / 2;
        int tooltipY = y - tooltipHeight - 5;
        
        // Ensure tooltip stays on screen
        if (tooltipX < 5) tooltipX = 5;
        if (tooltipY < 5) tooltipY = y + height + 5;
        
        // Tooltip background
        g2d.setColor(new Color(40, 40, 40, 220));
        g2d.fillRoundRect(tooltipX, tooltipY, tooltipWidth, tooltipHeight, 4, 4);
        
        // Tooltip border
        g2d.setColor(getCurrentStrategyColor());
        g2d.setStroke(new BasicStroke(1));
        g2d.drawRoundRect(tooltipX, tooltipY, tooltipWidth, tooltipHeight, 4, 4);
        
        // Tooltip text
        g2d.setColor(Color.WHITE);
        g2d.drawString(tooltipText, tooltipX + 6, tooltipY + fm.getAscent() + 4);
    }

    /**
     * Gets the current strategy type
     */
    public StrategyType getCurrentStrategyType() {
        return currentStrategyType;
    }

} 