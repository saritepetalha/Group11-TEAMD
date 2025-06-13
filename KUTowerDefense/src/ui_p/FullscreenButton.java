package ui_p;

import main.Game;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * FullscreenButton provides a UI button for toggling fullscreen mode
 */
public class FullscreenButton {

    private int x, y, width, height;
    private Rectangle bounds;
    private Game game;
    private boolean isHovered = false;
    private boolean isPressed = false;

    // Button graphics
    private BufferedImage[] buttonImages;
    private String text;

    public FullscreenButton(int x, int y, int width, int height, Game game) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.game = game;
        this.bounds = new Rectangle(x, y, width, height);
        this.text = "Fullscreen";

        loadButtonImages();
    }

    private void loadButtonImages() {
        // Try to load button images, fallback to creating them if not available
        try {
            buttonImages = new BufferedImage[3]; // normal, hover, pressed

            // For now, create simple colored rectangles as button states
            // In a full implementation, these would be loaded from assets
            buttonImages[0] = createButtonImage(new Color(100, 100, 100)); // normal
            buttonImages[1] = createButtonImage(new Color(120, 120, 120)); // hover
            buttonImages[2] = createButtonImage(new Color(80, 80, 80));    // pressed

        } catch (Exception e) {
            System.err.println("Error loading fullscreen button images: " + e.getMessage());
            // Create fallback images
            buttonImages = new BufferedImage[3];
            buttonImages[0] = createButtonImage(new Color(100, 100, 100));
            buttonImages[1] = createButtonImage(new Color(120, 120, 120));
            buttonImages[2] = createButtonImage(new Color(80, 80, 80));
        }
    }

    private BufferedImage createButtonImage(Color backgroundColor) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw button background
        g.setColor(backgroundColor);
        g.fillRoundRect(0, 0, width, height, 8, 8);

        // Draw border
        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(2));
        g.drawRoundRect(1, 1, width - 2, height - 2, 8, 8);

        g.dispose();
        return img;
    }

    public void update() {
        // Update button text based on current fullscreen state
        if (game.getFullscreenManager() != null) {
            if (game.getFullscreenManager().isFullscreen()) {
                text = "Exit Fullscreen";
            } else {
                text = "Enter Fullscreen";
            }
        }
    }

    public void render(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Select appropriate button image based on state
        BufferedImage buttonImg = buttonImages[0]; // normal
        if (isPressed) {
            buttonImg = buttonImages[2]; // pressed
        } else if (isHovered) {
            buttonImg = buttonImages[1]; // hover
        }

        // Draw button image
        g2d.drawImage(buttonImg, x, y, null);

        // Draw button text
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 12));

        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int textHeight = fm.getHeight();
        int textX = x + (width - textWidth) / 2;
        int textY = y + (height + textHeight) / 2 - fm.getDescent();

        g2d.drawString(text, textX, textY);

        // Draw keyboard shortcut hint
        g2d.setFont(new Font("Arial", Font.PLAIN, 10));
        g2d.setColor(Color.LIGHT_GRAY);
        String shortcut = "(F11 or ESC)";
        FontMetrics fmSmall = g2d.getFontMetrics();
        int shortcutWidth = fmSmall.stringWidth(shortcut);
        int shortcutX = x + (width - shortcutWidth) / 2;
        int shortcutY = y + height - 5;
        g2d.drawString(shortcut, shortcutX, shortcutY);
    }

    public void mouseMoved(int mouseX, int mouseY) {
        isHovered = bounds.contains(mouseX, mouseY);
    }

    public void mousePressed(int mouseX, int mouseY) {
        if (bounds.contains(mouseX, mouseY)) {
            isPressed = true;
        }
    }

    public void mouseReleased(int mouseX, int mouseY) {
        if (isPressed && bounds.contains(mouseX, mouseY)) {
            // Button was clicked
            onClick();
        }
        isPressed = false;
    }

    public void mouseClicked(int mouseX, int mouseY) {
        if (bounds.contains(mouseX, mouseY)) {
            onClick();
        }
    }

    private void onClick() {
        if (game.getFullscreenManager() != null) {
            System.out.println("Fullscreen button clicked - toggling fullscreen mode");
            game.getFullscreenManager().toggleFullscreen();
        }
    }

    // Getters and setters
    public Rectangle getBounds() { return bounds; }
    public boolean isHovered() { return isHovered; }
    public boolean isPressed() { return isPressed; }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
        this.bounds.x = x;
        this.bounds.y = y;
    }

    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
        this.bounds.width = width;
        this.bounds.height = height;
        // Reload button images with new size
        loadButtonImages();
    }
}