package ui_p;

import constants.GameDimensions;
import scenes.Playing;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class PlayingUI {
    private Playing playing;
    private BufferedImage statusBarImg;
    private BufferedImage buttonBgImg;

    // Status bar variables
    private int goldAmount = 0;
    private int healthAmount = 10;
    private int shieldAmount = 25;

    private ArrayList<Point> buttonPositions;
    private int buttonSize = GameDimensions.ButtonSize.SMALL.getSize();

    // Add properties to track button states
    private boolean pauseButtonHover = false;
    private boolean pauseButtonPressed = false;
    private boolean fastForwardButtonHover = false;
    private boolean fastForwardButtonPressed = false;
    private boolean optionsButtonHover = false;
    private boolean optionsButtonPressed = false;

    public PlayingUI(Playing playing) {
        this.playing = playing;
    }


    public void draw(Graphics g) {
        // Draw left side UI (gold, health, shield)
        drawStatusBars(g);

        // Draw right side control buttons
        drawControlButtons(g);
    }

    private void drawStatusBars(Graphics g) {
        // Status bar positions
        int barX = 10;
        int barY = 10;

        // Create custom MV Boli font
        Font mvBoliFont = new Font("MV Boli", Font.BOLD, 14);
        g.setFont(mvBoliFont);

        // Button dimensions
        int buttonWidth = 120;
        int buttonHeight = 32;

        statusBarImg = ButtonAssets.statusBarImg;

        // Draw the statusBarImg at half its original size
        if (statusBarImg != null) {
            int imgWidth = statusBarImg.getWidth();
            int imgHeight = statusBarImg.getHeight();
            g.drawImage(statusBarImg, barX + 10, barY + 10, imgWidth / 2, imgHeight / 2, null);
        }

        // Draw Gold bar with 3-slide button
        drawSlideButton(g, ""+ goldAmount, barX + 50, barY + 15, buttonWidth, buttonHeight, goldAmount, 10000);

        // Draw Health bar with 3-slide button
        drawSlideButton(g, healthAmount + "/10", barX + 50, barY + 50, buttonWidth, buttonHeight, healthAmount, 10);

        // Draw Shield bar with 3-slide button
        drawSlideButton(g, shieldAmount + "/25", barX + 50, barY + 85, buttonWidth, buttonHeight, shieldAmount, 25);
    }

    private void drawSlideButton(Graphics g, String text, int x, int y, int width, int height, int value, int maxValue) {
        Graphics2D g2d = (Graphics2D) g;

        buttonBgImg = ButtonAssets.modeImage;

        if (buttonBgImg != null) {
            // Assuming Button_Blue_3Slides.png has 3 sections: left edge, middle (repeatable), right edge
            int edgeWidth = buttonBgImg.getHeight(); // Assuming square edges, height = edge width

            // Calculate how much of the button width is filled based on value/maxValue ratio
            float ratio = Math.max(0, Math.min(1, (float) value / maxValue));
            int fillWidth = Math.round(width * ratio);

            // Draw left edge
            g2d.drawImage(buttonBgImg.getSubimage(0, 0, edgeWidth, buttonBgImg.getHeight()),
                    x, y, edgeWidth, height, null);

            // Draw middle section (repeatable) - Only draw up to the fill width
            int middleWidth = Math.max(0, fillWidth - edgeWidth * 2);
            if (middleWidth > 0) {
                // Draw middle section by stretching
                g2d.drawImage(buttonBgImg.getSubimage(edgeWidth, 0, edgeWidth, buttonBgImg.getHeight()),
                        x + edgeWidth, y, middleWidth, height, null);
            }

            // Draw right edge - Only if we've filled to the end
            if (fillWidth >= width - edgeWidth) {
                g2d.drawImage(buttonBgImg.getSubimage(buttonBgImg.getWidth() - edgeWidth, 0, edgeWidth, buttonBgImg.getHeight()),
                        x + width - edgeWidth, y, edgeWidth, height, null);
            }

            // Draw unfilled part with a darker version
            if (fillWidth < width) {
                // Set a translucent composite to darken the unfilled part
                Composite originalComposite = g2d.getComposite();
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));

                // Draw unfilled left edge if needed
                if (fillWidth < edgeWidth) {
                    g2d.drawImage(buttonBgImg.getSubimage(0, 0, edgeWidth, buttonBgImg.getHeight()),
                            x, y, edgeWidth, height, null);
                }

                // Draw unfilled middle
                int unfillMiddleWidth = Math.max(0, width - Math.max(fillWidth, edgeWidth) - edgeWidth);
                if (unfillMiddleWidth > 0) {
                    g2d.drawImage(buttonBgImg.getSubimage(edgeWidth, 0, edgeWidth, buttonBgImg.getHeight()),
                            x + Math.max(fillWidth, edgeWidth), y, unfillMiddleWidth, height, null);
                }

                // Draw unfilled right edge
                g2d.drawImage(buttonBgImg.getSubimage(buttonBgImg.getWidth() - edgeWidth, 0, edgeWidth, buttonBgImg.getHeight()),
                        x + width - edgeWidth, y, edgeWidth, height, null);

                g2d.setComposite(originalComposite);
            }
        } else {
            // Fallback if image is not available
            g2d.setColor(Color.BLUE);
            g2d.fillRect(x, y, width, height);
        }

        // Draw text with MV Boli font
        Font mvBoliFont = new Font("MV Boli", Font.BOLD, 14);
        g2d.setFont(mvBoliFont);
        g2d.setColor(Color.WHITE);

        // Center text
        FontMetrics fm = g2d.getFontMetrics();
        int textX = x + (width - fm.stringWidth(text)) / 2;
        int textY = y + ((height - fm.getHeight()) / 4) + fm.getAscent();

        g2d.setColor(new Color(0, 0, 0, 120));
        g2d.drawString(text, textX + 1, textY + 1);

        g2d.setColor(Color.WHITE);
        g2d.drawString(text, textX, textY);
    }

    private void drawControlButtons(Graphics g) {
        int buttonSize = GameDimensions.ButtonSize.SMALL.getSize();
        int buttonSpacing = 8;
        int startX = GameDimensions.GAME_WIDTH - (buttonSize * 3 + buttonSpacing * 2) - 10;
        int startY = 8;

        Graphics2D g2d = (Graphics2D) g;

        drawControlButton(g2d, startX, startY, buttonSize, buttonSize,
                ButtonAssets.buttonImages.get(12),
                ButtonAssets.buttonHoverEffectImages.get(3),
                ButtonAssets.buttonPressedEffectImages.get(13),
                pauseButtonHover, pauseButtonPressed);

        drawControlButton(g2d, startX + buttonSize + buttonSpacing, startY, buttonSize, buttonSize,
                ButtonAssets.buttonImages.get(8),
                ButtonAssets.buttonHoverEffectImages.get(13),
                ButtonAssets.buttonPressedEffectImages.get(9),
                fastForwardButtonHover, fastForwardButtonPressed);


        drawControlButton(g2d, startX + (buttonSize + buttonSpacing) * 2, startY, buttonSize, buttonSize,
                ButtonAssets.buttonImages.get(1),
                ButtonAssets.buttonHoverEffectImages.get(2),
                ButtonAssets.buttonPressedEffectImages.get(11),
                optionsButtonHover, optionsButtonPressed);
    }

    private void drawControlButton(Graphics2D g2d, int x, int y, int width, int height,
                                   BufferedImage normalImg, BufferedImage hoverImg, BufferedImage pressedImg,
                                   boolean isHover, boolean isPressed) {

        g2d.setColor(new Color(157, 209, 153, 255));
        g2d.fillRect(x, y, width, height);

        g2d.drawImage(normalImg, x, y, width, height, null);    // draw base image

        if (isPressed) {
            g2d.drawImage(pressedImg, x, y, width, height, null);   // if button is pressed, draw pressed effect
        } else if (isHover) {
            // if mouse is hovering, draw hover effect with animation
            long currentTime = System.currentTimeMillis();     // create a shining animation effect
            float alpha = (float) (0.5f + 0.5f * Math.sin(currentTime * 0.003)); // oscillate between 0.5 and 1.0

            // set the alpha composite for the hover glow effect
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2d.drawImage(hoverImg, x, y, width, height, null);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }
    }

    public void setGoldAmount(int goldAmount) {
        this.goldAmount = goldAmount;
    }

    public void setHealthAmount(int healthAmount) {
        this.healthAmount = healthAmount;
    }

    public void setShieldAmount(int shieldAmount) {
        this.shieldAmount = shieldAmount;
    }

    // Add methods to handle mouse hover and press for control buttons
    public void mouseMoved(int mouseX, int mouseY) {
        int buttonSize = 40;
        int buttonSpacing = 10;
        int startX = GameDimensions.GAME_WIDTH - (buttonSize * 3 + buttonSpacing * 2) - 10;
        int startY = 10;

        // Reset all hover states
        pauseButtonHover = false;
        fastForwardButtonHover = false;
        optionsButtonHover = false;

        // Check pause button
        if (new Rectangle(startX, startY, buttonSize, buttonSize).contains(mouseX, mouseY)) {
            pauseButtonHover = true;
        }

        // Check fast forward button
        if (new Rectangle(startX + buttonSize + buttonSpacing, startY, buttonSize, buttonSize)
                .contains(mouseX, mouseY)) {
            fastForwardButtonHover = true;
        }

        // Check options button
        if (new Rectangle(startX + (buttonSize + buttonSpacing) * 2, startY, buttonSize, buttonSize)
                .contains(mouseX, mouseY)) {
            optionsButtonHover = true;
        }
    }

    public void mousePressed(int mouseX, int mouseY) {
        int buttonSize = 40;
        int buttonSpacing = 10;
        int startX = GameDimensions.GAME_WIDTH - (buttonSize * 3 + buttonSpacing * 2) - 10;
        int startY = 10;

        // Check pause button
        if (new Rectangle(startX, startY, buttonSize, buttonSize).contains(mouseX, mouseY)) {
            // Toggle behavior - invert current state
            pauseButtonPressed = !pauseButtonPressed;

            // If this button is pressed, release other buttons
            if (pauseButtonPressed) {
                fastForwardButtonPressed = false;
                optionsButtonPressed = false;
            }
        }

        // Check fast forward button
        else if (new Rectangle(startX + buttonSize + buttonSpacing, startY, buttonSize, buttonSize)
                .contains(mouseX, mouseY)) {
            // Toggle behavior - invert current state
            fastForwardButtonPressed = !fastForwardButtonPressed;

            // If this button is pressed, release other buttons
            if (fastForwardButtonPressed) {
                pauseButtonPressed = false;
                optionsButtonPressed = false;
            }
        }

        // Check options button
        else if (new Rectangle(startX + (buttonSize + buttonSpacing) * 2, startY, buttonSize, buttonSize)
                .contains(mouseX, mouseY)) {
            // Toggle behavior - invert current state
            optionsButtonPressed = !optionsButtonPressed;

            // If this button is pressed, release other buttons
            if (optionsButtonPressed) {
                pauseButtonPressed = false;
                fastForwardButtonPressed = false;
            }
        }
    }

    public void mouseReleased() {
        // Don't reset button states - they should stay pressed until toggled off
    }

    // Add method to manually release all buttons
    public void releaseAllButtons() {
        pauseButtonPressed = false;
        fastForwardButtonPressed = false;
        optionsButtonPressed = false;
    }
}