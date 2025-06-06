package ui_p;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

/**
 * A beautiful, reusable tooltip component for displaying costs and descriptions
 */
public class CostTooltip {
    
    private String title;
    private String cost;
    private String description;
    private boolean isAffordable;
    private boolean visible;
    private int x, y;
    private int width, height;
    private long fadeStartTime;
    private boolean fading;
    
    // Visual constants
    private static final int PADDING = 12;
    private static final int CORNER_RADIUS = 8;
    private static final int FADE_DURATION_MS = 200;
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font COST_FONT = new Font("Segoe UI", Font.BOLD, 12);
    private static final Font DESC_FONT = new Font("Segoe UI", Font.PLAIN, 11);
    
    public CostTooltip() {
        this.visible = false;
        this.fading = false;
    }
    
    /**
     * Shows tooltip with cost information
     */
    public void show(String title, int cost, String description, boolean isAffordable, int mouseX, int mouseY) {
        this.title = title;
        this.cost = (cost > 0) ? "$" + cost : "Free";
        this.description = description;
        this.isAffordable = isAffordable;
        this.visible = true;
        this.fading = false;
        
        // Calculate tooltip size
        calculateSize();
        
        // Position tooltip (with screen edge detection)
        positionTooltip(mouseX, mouseY);
    }
    
    /**
     * Shows tooltip for ultimate abilities with cooldown
     */
    public void showUltimate(String title, int cost, String description, boolean isAffordable, 
                            boolean onCooldown, long remainingCooldown, int mouseX, int mouseY) {
        this.title = title;
        
        if (onCooldown) {
            this.cost = "Cooldown: " + (remainingCooldown / 1000) + "s";
            this.isAffordable = false;
        } else {
            this.cost = (cost > 0) ? "$" + cost : "Free";
            this.isAffordable = isAffordable;
        }
        
        this.description = description;
        this.visible = true;
        this.fading = false;
        
        calculateSize();
        positionTooltip(mouseX, mouseY);
    }
    
    /**
     * Shows simple tooltip without cost
     */
    public void showSimple(String title, String description, int mouseX, int mouseY) {
        this.title = title;
        this.cost = null;
        this.description = description;
        this.isAffordable = true;
        this.visible = true;
        this.fading = false;
        
        calculateSize();
        positionTooltip(mouseX, mouseY);
    }
    
    /**
     * Hides tooltip with fade animation
     */
    public void hide() {
        if (visible && !fading) {
            fading = true;
            fadeStartTime = System.currentTimeMillis();
        }
    }
    
    /**
     * Updates tooltip state (handles fade animation)
     */
    public void update() {
        if (fading) {
            long elapsed = System.currentTimeMillis() - fadeStartTime;
            if (elapsed >= FADE_DURATION_MS) {
                visible = false;
                fading = false;
            }
        }
    }
    
    /**
     * Draws the tooltip
     */
    public void draw(Graphics2D g2d) {
        if (!visible) return;
        
        // Calculate fade alpha
        float alpha = 1.0f;
        if (fading) {
            long elapsed = System.currentTimeMillis() - fadeStartTime;
            alpha = Math.max(0.0f, 1.0f - (float)elapsed / FADE_DURATION_MS);
        }
        
        // Save original composite
        Composite originalComposite = g2d.getComposite();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        
        // Enable anti-aliasing for smooth curves
        Object originalAntiAlias = g2d.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw background with gradient
        drawBackground(g2d);
        
        // Draw content
        drawContent(g2d);
        
        // Draw border
        drawBorder(g2d);
        
        // Restore rendering hints and composite
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, originalAntiAlias);
        g2d.setComposite(originalComposite);
    }
    
    private void drawBackground(Graphics2D g2d) {
        // Create gradient background
        Color bgTop = isAffordable ? new Color(40, 45, 52, 240) : new Color(60, 40, 40, 240);
        Color bgBottom = isAffordable ? new Color(25, 28, 35, 240) : new Color(45, 25, 25, 240);
        
        GradientPaint gradient = new GradientPaint(
            x, y, bgTop,
            x, y + height, bgBottom
        );
        
        g2d.setPaint(gradient);
        g2d.fill(new RoundRectangle2D.Float(x, y, width, height, CORNER_RADIUS, CORNER_RADIUS));
    }
    
    private void drawContent(Graphics2D g2d) {
        int contentY = y + PADDING;
        
        // Draw title
        g2d.setFont(TITLE_FONT);
        g2d.setColor(Color.WHITE);
        FontMetrics titleFm = g2d.getFontMetrics();
        g2d.drawString(title, x + PADDING, contentY + titleFm.getAscent());
        contentY += titleFm.getHeight() + 4;
        
        // Draw cost (if present)
        if (cost != null) {
            g2d.setFont(COST_FONT);
            Color costColor = isAffordable ? new Color(255, 215, 0) : new Color(255, 100, 100); // Gold or red
            g2d.setColor(costColor);
            FontMetrics costFm = g2d.getFontMetrics();
            g2d.drawString(cost, x + PADDING, contentY + costFm.getAscent());
            contentY += costFm.getHeight() + 6;
        }
        
        // Draw description
        if (description != null && !description.isEmpty()) {
            g2d.setFont(DESC_FONT);
            g2d.setColor(new Color(200, 200, 200));
            drawWrappedText(g2d, description, x + PADDING, contentY, width - 2 * PADDING);
        }
    }
    
    private void drawBorder(Graphics2D g2d) {
        // Draw border with glow effect
        Color borderColor = isAffordable ? new Color(100, 150, 255, 180) : new Color(255, 100, 100, 180);
        g2d.setColor(borderColor);
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.draw(new RoundRectangle2D.Float(x, y, width, height, CORNER_RADIUS, CORNER_RADIUS));
        
        // Add subtle inner glow
        Color glowColor = isAffordable ? new Color(100, 150, 255, 40) : new Color(255, 100, 100, 40);
        g2d.setColor(glowColor);
        g2d.setStroke(new BasicStroke(1.0f));
        g2d.draw(new RoundRectangle2D.Float(x + 1, y + 1, width - 2, height - 2, CORNER_RADIUS - 1, CORNER_RADIUS - 1));
    }
    
    private void drawWrappedText(Graphics2D g2d, String text, int x, int y, int maxWidth) {
        FontMetrics fm = g2d.getFontMetrics();
        
        // First split by explicit line breaks (\n)
        String[] lines = text.split("\n");
        int lineY = y + fm.getAscent();
        
        for (String lineText : lines) {
            // Then handle word wrapping for each line
            String[] words = lineText.split(" ");
            StringBuilder line = new StringBuilder();
            
            for (String word : words) {
                String testLine = line.length() > 0 ? line + " " + word : word;
                if (fm.stringWidth(testLine) > maxWidth && line.length() > 0) {
                    g2d.drawString(line.toString(), x, lineY);
                    line = new StringBuilder(word);
                    lineY += fm.getHeight();
                } else {
                    line = new StringBuilder(testLine);
                }
            }
            
            if (line.length() > 0) {
                g2d.drawString(line.toString(), x, lineY);
            }
            
            // Move to next line (for explicit line breaks)
            lineY += fm.getHeight();
        }
    }
    
    private void calculateSize() {
        // Create temporary graphics for measuring
        BufferedImage tempImg = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D tempG2d = tempImg.createGraphics();
        
        int maxWidth = 0;
        int totalHeight = PADDING * 2;
        
        // Measure title
        tempG2d.setFont(TITLE_FONT);
        FontMetrics titleFm = tempG2d.getFontMetrics();
        maxWidth = Math.max(maxWidth, titleFm.stringWidth(title));
        totalHeight += titleFm.getHeight() + 4;
        
        // Measure cost
        if (cost != null) {
            tempG2d.setFont(COST_FONT);
            FontMetrics costFm = tempG2d.getFontMetrics();
            maxWidth = Math.max(maxWidth, costFm.stringWidth(cost));
            totalHeight += costFm.getHeight() + 6;
        }
        
        // Measure description
        if (description != null && !description.isEmpty()) {
            tempG2d.setFont(DESC_FONT);
            FontMetrics descFm = tempG2d.getFontMetrics();
            
            // Calculate wrapped text dimensions with line break support
            String[] lineBreaks = description.split("\n");
            int totalLines = 0;
            int maxLineWidth = 200; // Max description width
            
            for (String lineText : lineBreaks) {
                String[] words = lineText.split(" ");
                StringBuilder line = new StringBuilder();
                int linesInThisBreak = 0;
                
                for (String word : words) {
                    String testLine = line.length() > 0 ? line + " " + word : word;
                    if (descFm.stringWidth(testLine) > maxLineWidth && line.length() > 0) {
                        maxWidth = Math.max(maxWidth, descFm.stringWidth(line.toString()));
                        line = new StringBuilder(word);
                        linesInThisBreak++;
                    } else {
                        line = new StringBuilder(testLine);
                    }
                }
                
                if (line.length() > 0) {
                    maxWidth = Math.max(maxWidth, descFm.stringWidth(line.toString()));
                    linesInThisBreak++;
                }
                
                totalLines += linesInThisBreak;
            }
            
            totalHeight += totalLines * descFm.getHeight();
        }
        
        width = maxWidth + 2 * PADDING;
        height = totalHeight;
        
        tempG2d.dispose();
    }
    
    private void positionTooltip(int mouseX, int mouseY) {
        // Default position (above and to the right of cursor)
        x = mouseX + 15;
        y = mouseY - height - 10;
        
        // Screen bounds (assuming 1280x720 game window)
        int screenWidth = 1280;
        int screenHeight = 720;
        
        // Adjust if tooltip would go off right edge
        if (x + width > screenWidth - 10) {
            x = mouseX - width - 15;
        }
        
        // Adjust if tooltip would go off top edge
        if (y < 10) {
            y = mouseY + 20;
        }
        
        // Adjust if tooltip would go off bottom edge
        if (y + height > screenHeight - 10) {
            y = screenHeight - height - 10;
        }
        
        // Ensure tooltip stays on left edge
        if (x < 10) {
            x = 10;
        }
    }
    
    public boolean isVisible() {
        return visible;
    }
} 