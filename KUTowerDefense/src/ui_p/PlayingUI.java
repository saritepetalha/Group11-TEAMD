package ui_p;

import constants.GameDimensions;
import static constants.Constants.Player.*;
import scenes.Playing;

import java.awt.*;
import java.awt.image.BufferedImage;

public class PlayingUI {
    private Playing playing;
    private BufferedImage statusBarImg;
    private BufferedImage buttonBgImg;

    // status bar variables
    private int goldAmount;
    private int healthAmount;
    private int shieldAmount;

    // playing ui buttons
    private TheButton fastForwardButton;
    private TheButton pauseButton;
    private TheButton optionsButton;

    private int buttonSize = GameDimensions.ButtonSize.SMALL.getSize();

    public PlayingUI(Playing playing) {
        this.playing = playing;
        initButtons();
    }

    private void initButtons() {
        int buttonSpacing = 8;
        int startX = GameDimensions.GAME_WIDTH - (buttonSize * 3 + buttonSpacing * 2) - 10;
        int startY = 8;

        // initialize control buttons as TheButton objects
        fastForwardButton = new TheButton("Fast Forward",
                startX,
                startY,
                buttonSize,
                buttonSize,
                ButtonAssets.buttonImages.get(8));

        pauseButton = new TheButton("Pause",
                startX + buttonSize + buttonSpacing,
                startY,
                buttonSize,
                buttonSize,
                ButtonAssets.buttonImages.get(12));

        optionsButton = new TheButton("Options",
                startX + (buttonSize + buttonSpacing) * 2,
                startY,
                buttonSize,
                buttonSize,
                ButtonAssets.buttonImages.get(1));
    }

    public void draw(Graphics g) {
        // draw left side UI (gold, health, shield)
        drawStatusBars(g);

        // draw right side control buttons
        drawControlButtons(g);

        // draw wave indicator on the right
        drawWaveIndicator(g);
    }

    private void drawStatusBars(Graphics g) {
        // Status bar positions
        int barX = 10;
        int barY = 10;

        Font mvBoliFont = new Font("MV Boli", Font.BOLD, 14);
        g.setFont(mvBoliFont);

        // button dimensions
        int buttonWidth = 120;
        int buttonHeight = 32;

        statusBarImg = ButtonAssets.statusBarImg;

        // draw the statusBarImg at half its original size
        if (statusBarImg != null) {
            int imgWidth = statusBarImg.getWidth();
            int imgHeight = statusBarImg.getHeight();
            g.drawImage(statusBarImg, barX + 10, barY + 13, imgWidth / 10, imgHeight / 10, null);
        }

        // draw Gold bar with 3-slide button
        drawSlideButton(g, ""+ goldAmount, barX + 50, barY + 15, buttonWidth, buttonHeight, goldAmount, 1000000);
        // draw Health bar with 3-slide button
        drawSlideButton(g, healthAmount + "/10", barX + 50, barY + 50, buttonWidth, buttonHeight, healthAmount, MAX_HEALTH);
        // draw Shield bar with 3-slide button
        drawSlideButton(g, shieldAmount + "/25", barX + 50, barY + 85, buttonWidth, buttonHeight, shieldAmount, MAX_SHIELD);
    }

    private void drawWaveIndicator(Graphics g) {
        int buttonSpacing = 8;
        int barWidth = 120;
        int barHeight = 32;
        int iconSize = 32;
        int startX = GameDimensions.GAME_WIDTH - barWidth - iconSize - buttonSpacing - 10;
        int startY = buttonSize + 16;
        int currentWave = playing.getWaveManager().getWaveIndex() + 1;
        int totalWaves = playing.getWaveManager().getWaves().size();

        // draw wave icon
        g.drawImage(ButtonAssets.waveImg, startX, startY, iconSize, iconSize, null);

        // Draw wave progress bar
        drawSlideButton(g,currentWave + "/" + totalWaves,
                startX + iconSize + buttonSpacing, startY,
                barWidth, barHeight,
                currentWave, totalWaves);
    }


    private void drawSlideButton(Graphics g, String text, int x, int y, int width, int height, int value, int maxValue) {
        Graphics2D g2d = (Graphics2D) g;

        buttonBgImg = ButtonAssets.modeImage;

        if (buttonBgImg != null) {
            // calculate how much of the button width is filled based on value/maxValue ratio
            float ratio = Math.max(0, Math.min(1, (float) value / maxValue));
            int fillWidth = Math.round(width * ratio);

            // check if this is a gold bar (maxValue of 10000) - constant coloring
            boolean isGoldBar = (maxValue == 1000000);

            if (isGoldBar) {
                // for gold bar, draw the full button (no partial fill)
                g2d.drawImage(buttonBgImg, x, y, width, height, null);
            } else {
                // for health and shield bars, create a continuous fill effect

                // first draw background for entire bar with reduced opacity
                Composite originalComposite = g2d.getComposite();
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
                g2d.drawImage(buttonBgImg, x, y, width, height, null);

                g2d.setComposite(originalComposite);

                // now draw the filled portion at full opacity using a clipping region
                if (fillWidth > 0) {
                    // create a clip to only show the filled part
                    Shape oldClip = g2d.getClip();
                    g2d.setClip(x, y, fillWidth, height);

                    // draw the entire button image within the clip region
                    g2d.drawImage(buttonBgImg, x, y, width, height, null);

                    g2d.setClip(oldClip);
                }
            }
        } else {
            // fallback if image is not available
            g2d.setColor(Color.BLUE);
            g2d.fillRect(x, y, width, height);

            // calculate variables for fallback rendering
            boolean isGoldBar = (maxValue == 1000000);
            float ratio = Math.max(0, Math.min(1, (float) value / maxValue));
            int fillWidth = Math.round(width * ratio);

            if (!isGoldBar) {
                // draw the fill percentage for health/shield
                g2d.setColor(new Color(30, 144, 255)); // Lighter blue for the filled part
                g2d.fillRect(x, y, fillWidth, height);
            }
        }

        Font mvBoliFont = new Font("MV Boli", Font.BOLD, 14);
        g2d.setFont(mvBoliFont);
        g2d.setColor(Color.WHITE);

        FontMetrics fm = g2d.getFontMetrics();
        int textX = x + (width - fm.stringWidth(text)) / 2;
        int textY = y + ((height - fm.getHeight()) / 4) + fm.getAscent();

        g2d.setColor(new Color(0, 0, 0, 120));
        g2d.drawString(text, textX, textY);
    }

    private void drawControlButtons(Graphics g) {
        int buttonSpacing = 8;
        int startX = GameDimensions.GAME_WIDTH - (buttonSize * 3 + buttonSpacing * 2) - 10;
        int startY = 8;

        Graphics2D g2d = (Graphics2D) g;

        drawControlButton(g2d, fastForwardButton, startX , startY, buttonSize, buttonSize,
                ButtonAssets.buttonImages.get(8),
                ButtonAssets.buttonHoverEffectImages.get(13),
                ButtonAssets.buttonPressedEffectImages.get(9));


        drawControlButton(g2d, pauseButton, startX + buttonSize + buttonSpacing, startY, buttonSize, buttonSize,
                ButtonAssets.buttonImages.get(12),
                ButtonAssets.buttonHoverEffectImages.get(3),
                ButtonAssets.buttonPressedEffectImages.get(13));

        drawControlButton(g2d, optionsButton, startX + (buttonSize + buttonSpacing) * 2, startY, buttonSize, buttonSize,
                ButtonAssets.buttonImages.get(1),
                ButtonAssets.buttonHoverEffectImages.get(2),
                ButtonAssets.buttonPressedEffectImages.get(11));
    }


    private void drawControlButton(Graphics2D g2d, TheButton button, int x, int y, int width, int height,
                                   BufferedImage normalImg, BufferedImage hoverImg, BufferedImage pressedImg) {

        g2d.setColor(new Color(157, 209, 153, 255));
        g2d.fillRect(x, y, width, height);

        g2d.drawImage(normalImg, x, y, width, height, null); // draw base image

        if (button.isMousePressed()) {               // if button is pressed, draw pressed effect
            g2d.drawImage(pressedImg, x, y, width, height, null);
        } else if (button.isMouseOver()) {           // if mouse is hovering, draw hover effect with animation
            // create a shining animation effect
            long currentTime = System.currentTimeMillis();
            float alpha = (float) (0.5f + 0.5f * Math.sin(currentTime * 0.003)); // oscillate between 0.5 and 1.0

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

    // add methods to handle mouse hover and press for control buttons
    public void mouseMoved(int mouseX, int mouseY) {
        // reset all hover states
        pauseButton.setMouseOver(false);
        fastForwardButton.setMouseOver(false);
        optionsButton.setMouseOver(false);

        // check which button is hovered
        if (pauseButton.getBounds().contains(mouseX, mouseY)) {
            pauseButton.setMouseOver(true);
        } else if (fastForwardButton.getBounds().contains(mouseX, mouseY)) {
            fastForwardButton.setMouseOver(true);
        } else if (optionsButton.getBounds().contains(mouseX, mouseY)) {
            optionsButton.setMouseOver(true);
        }
    }

    public void mousePressed(int mouseX, int mouseY) {
        // check which button is pressed
        if (pauseButton.getBounds().contains(mouseX, mouseY)) {
            toggleButtonState(pauseButton);
        } else if (fastForwardButton.getBounds().contains(mouseX, mouseY)){
            toggleButtonState(fastForwardButton);
        } else if (optionsButton.getBounds().contains(mouseX, mouseY)) {
            toggleButtonState(optionsButton);
        }
    }

    // helper method to toggle button state
    private void toggleButtonState(TheButton button) {
        // toggle the button's pressed state
        button.setMousePressed(!button.isMousePressed());

        // if this button is now pressed, release other buttons
        if (button.isMousePressed()) {
            if (button != pauseButton) pauseButton.setMousePressed(false);
            if (button != fastForwardButton) fastForwardButton.setMousePressed(false);
            if (button != optionsButton) optionsButton.setMousePressed(false);
        }
    }

    public void mouseReleased() {
    }

}