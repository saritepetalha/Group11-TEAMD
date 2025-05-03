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

    // options menu back button
    private TheButton backOptionsButton;
    private TheButton mainMenuButton;

    // option values (WILL BE CHANGED TO BE READ FROM FILE)
    private int soundVolume = 50;
    private int musicVolume = 50;
    private boolean sliderDragging = false;
    private String currentSlider = "";
    private String currentDifficulty = "Normal";

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

        backOptionsButton = new TheButton("Back",
                GameDimensions.GAME_WIDTH / 2 + ButtonAssets.optionsMenuImg.getWidth() / 4,
                GameDimensions.GAME_HEIGHT / 2 - ButtonAssets.optionsMenuImg.getHeight() / 3,
                buttonSize,
                buttonSize,
                ButtonAssets.backOptionsImg);

        // Initialize main menu button
        mainMenuButton = new TheButton("Main Menu",
                0, 0, 200, 40, null);
    }

    public void draw(Graphics g) {
        // draw left side UI (gold, health, shield)
        drawStatusBars(g);

        // draw right side control buttons
        drawControlButtons(g);

        // draw wave indicator on the right
        drawWaveIndicator(g);

        // Draw pause overlay if game is paused
        if (playing.isGamePaused()) {
            drawPauseOverlay(g);
        }

        // Draw options menu if it's open
        if (playing.isOptionsMenuOpen()) {
            drawOptionsMenu(g);
        }
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

    /**
     * Draws the pause overlay when the game is paused
     */
    public void drawPauseOverlay(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRect(0, 0, GameDimensions.GAME_WIDTH, GameDimensions.GAME_HEIGHT);

        Font pauseFont = helpMethods.FontLoader.loadMedodicaFont(64f);
        g2d.setFont(pauseFont);
        String pauseText = "PAUSED";

        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(pauseText);
        int textX = (GameDimensions.GAME_WIDTH - textWidth) / 2;
        int textY = GameDimensions.GAME_HEIGHT / 2;

        g2d.setColor(new Color(0, 0, 0, 200));
        g2d.drawString(pauseText, textX + 3, textY + 3);

        g2d.setColor(Color.WHITE);
        g2d.drawString(pauseText, textX, textY);

        Font hintFont = helpMethods.FontLoader.loadMedodicaFont(32f);
        g2d.setFont(hintFont);
        String hintText = "Click pause button again to resume";

        fm = g2d.getFontMetrics();
        textWidth = fm.stringWidth(hintText);
        textX = (GameDimensions.GAME_WIDTH - textWidth) / 2;
        textY = GameDimensions.GAME_HEIGHT / 2 + 50;

        g2d.setColor(new Color(200, 200, 200));
        g2d.drawString(hintText, textX, textY);
    }

    /**
     * Draws the options menu with the new UI assets
     */
    public void drawOptionsMenu(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        BufferedImage optionsImg = ButtonAssets.optionsMenuImg;
        int menuWidth = optionsImg.getWidth() / 2;
        int menuHeight = optionsImg.getHeight() / 2;
        int menuX = (GameDimensions.GAME_WIDTH - menuWidth) / 2;
        int menuY = (GameDimensions.GAME_HEIGHT - menuHeight) / 2;

        // Draw semi-transparent background overlay
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRect(0, 0, GameDimensions.GAME_WIDTH, GameDimensions.GAME_HEIGHT);

        g2d.drawImage(optionsImg, menuX, menuY, menuWidth, menuHeight, null);

        // Draw back button
        if (ButtonAssets.backOptionsImg != null) {
            int backButtonX = menuX + menuWidth - buttonSize;
            int backButtonY = menuY + 10;

            // Update button position
            backOptionsButton.setX(backButtonX);
            backOptionsButton.setY(backButtonY);

            g2d.drawImage(ButtonAssets.backOptionsImg, backButtonX, backButtonY, buttonSize, buttonSize, null);

            if (backOptionsButton.isMouseOver()) {
                g2d.setColor(new Color(255, 255, 255, 80));
                g2d.fillOval(backButtonX, backButtonY, buttonSize, buttonSize);
            }
        }

        int startY = menuY + menuHeight / 4 + 10;
        int spacing = menuHeight / 6;
        int controlX = menuX + menuWidth / 2 + 10;

        // 1. Sound slider
        drawSlider(g2d, controlX, startY, 70, 20, soundVolume, "sound");

        // 2. Music slider
        drawSlider(g2d, controlX, startY + spacing, 70, 20, musicVolume, "music");

        // 3. Difficulty display
        int difficultyWidth = 90;
        int difficultyHeight = 15;
        int difficultyY = startY + spacing * 2 + 5    ;

        // Display the correct difficulty image
        if (currentDifficulty.equals("Normal") && ButtonAssets.difficultyNormalImg != null) {
            g2d.drawImage(ButtonAssets.difficultyNormalImg, controlX, difficultyY, difficultyWidth, difficultyHeight, null);
        } else if (currentDifficulty.equals("Easy") && ButtonAssets.difficultyEasyImg != null) {
            g2d.drawImage(ButtonAssets.difficultyEasyImg, controlX, difficultyY, difficultyWidth, difficultyHeight, null);
        } else if (currentDifficulty.equals("Hard") && ButtonAssets.difficultyHardImg != null) {
            g2d.drawImage(ButtonAssets.difficultyHardImg, controlX, difficultyY, difficultyWidth, difficultyHeight, null);
        } else {
            // Fallback if images are not available
            g2d.setColor(new Color(80, 80, 200));
            g2d.fillRoundRect(controlX, difficultyY, difficultyWidth, difficultyHeight, 10, 10);
            g2d.setColor(Color.WHITE);
            g2d.drawString(currentDifficulty, controlX + 30, startY + spacing * 2);
        }

        // 4. Return to Main Menu - no visible button, just hitbox
        int btnWidth = 242;
        int btnHeight = 38;
        int btnX = menuX + (menuWidth - btnWidth) / 2;
        int btnY = startY + spacing * 3;

        // Update main menu button position
        mainMenuButton.setX(btnX);
        mainMenuButton.setY(btnY);
        mainMenuButton.setWidth(btnWidth);
        mainMenuButton.setHeight(btnHeight);

        // Draw invisible hitbox for hover/pressed feedback only if mouse is over
        if (mainMenuButton.isMouseOver()) {
            g2d.setColor(new Color(255, 255, 255, 40));
            g2d.fillRoundRect(btnX, btnY, btnWidth, btnHeight, 15, 15);
        }
    }

    /**
     * Draws a slider for options menu
     */
    private void drawSlider(Graphics2D g2d, int x, int y, int width, int height, int value, String id) {
        g2d.setColor(new Color(60, 60, 60));
        g2d.fillRoundRect(x, y + height / 2 - 1, width, 2, 2, 2);

        int thumbX = x + (width * value / 100);
        int thumbWidth = 8;
        int thumbHeight = height;

        g2d.setColor(new Color(80, 180, 255));
        g2d.fillRoundRect(x, y + height / 2 - 1, thumbX - x, 2, 2, 2);

        int thumbRectX = thumbX - thumbWidth / 2;
        int thumbRectY = y;

        g2d.setColor(new Color(220, 220, 220));
        g2d.fillRect(thumbRectX, thumbRectY, thumbWidth, thumbHeight);

        g2d.setColor(new Color(100, 100, 100));
        g2d.drawRect(thumbRectX, thumbRectY, thumbWidth, thumbHeight);
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
        backOptionsButton.setMouseOver(false);
        mainMenuButton.setMouseOver(false);

        // check which button is hovered
        if (pauseButton.getBounds().contains(mouseX, mouseY)) {
            pauseButton.setMouseOver(true);
        } else if (fastForwardButton.getBounds().contains(mouseX, mouseY)) {
            fastForwardButton.setMouseOver(true);
        } else if (optionsButton.getBounds().contains(mouseX, mouseY)) {
            optionsButton.setMouseOver(true);
        } else if (playing.isOptionsMenuOpen()) {
            if (isMouseOverButton(backOptionsButton, mouseX, mouseY)) {
                backOptionsButton.setMouseOver(true);
            } else if (isMouseOverButton(mainMenuButton, mouseX, mouseY)) {
                mainMenuButton.setMouseOver(true);
            }
        }
    }

    private boolean isMouseOverButton(TheButton button, int mouseX, int mouseY) {
        return (mouseX >= button.getX() && mouseX <= button.getX() + button.getWidth() &&
                mouseY >= button.getY() && mouseY <= button.getY() + button.getHeight());
    }

    public void mousePressed(int mouseX, int mouseY) {
        // check which button is pressed
        if (pauseButton.getBounds().contains(mouseX, mouseY)) {
            toggleButtonState(pauseButton);
        } else if (fastForwardButton.getBounds().contains(mouseX, mouseY)) {
            toggleButtonState(fastForwardButton);
        } else if (optionsButton.getBounds().contains(mouseX, mouseY)) {
            toggleButtonState(optionsButton);
        } else if (playing.isOptionsMenuOpen()) {
            if (isMouseOverButton(backOptionsButton, mouseX, mouseY)) {
                toggleButtonState(backOptionsButton);
            } else if (isMouseOverButton(mainMenuButton, mouseX, mouseY)) {
                toggleButtonState(mainMenuButton);
                playing.returnToMainMenu();
            }

            // Check slider interaction
            currentSlider = getSliderAtPosition(mouseX, mouseY);
            if (!currentSlider.isEmpty()) {
                sliderDragging = true;
                updateSliderValue(mouseX);
            }
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
            if (button != backOptionsButton) backOptionsButton.setMousePressed(false);
            if (button != mainMenuButton) mainMenuButton.setMousePressed(false);
        }
    }

    public void mouseReleased() {
        // Stop slider dragging
        sliderDragging = false;
        currentSlider = "";
    }

    public void mouseDragged(int mouseX, int mouseY) {
        if (sliderDragging && !currentSlider.isEmpty()) {
            updateSliderValue(mouseX);
        }
    }

    /**
     * Check if mouse is over a slider and return the slider's ID if true
     */
    private String getSliderAtPosition(int mouseX, int mouseY) {
        if (!playing.isOptionsMenuOpen()) return "";

        BufferedImage optionsImg = ButtonAssets.optionsMenuImg;
        int menuWidth = optionsImg.getWidth() / 2;
        int menuHeight = optionsImg.getHeight() / 2;
        int menuX = (GameDimensions.GAME_WIDTH - menuWidth) / 2;
        int menuY = (GameDimensions.GAME_HEIGHT - menuHeight) / 2;

        int startY = menuY + menuHeight / 4 + 10;
        int spacing = menuHeight / 6;
        int controlX = menuX + menuWidth / 2 + 10;
        int sliderWidth = 70;
        int sliderHeight = 20;

        // make the hitbox taller to be easier to click
        int hitboxHeight = sliderHeight * 2;

        // check sound slider with increased hitbox
        Rectangle soundSlider = new Rectangle(controlX, startY - hitboxHeight/2, sliderWidth, hitboxHeight);
        if (soundSlider.contains(mouseX, mouseY)) return "sound";

        // check music slider with increased hitbox
        Rectangle musicSlider = new Rectangle(controlX, startY + spacing - hitboxHeight/2, sliderWidth, hitboxHeight);
        if (musicSlider.contains(mouseX, mouseY)) return "music";

        return "";
    }

    /**
     * Update slider values when dragging
     */
    public void updateSliderValue(int mouseX) {
        if (!sliderDragging || currentSlider.isEmpty()) return;

        BufferedImage optionsImg = ButtonAssets.optionsMenuImg;
        int menuWidth = optionsImg.getWidth() / 2;
        int menuX = (GameDimensions.GAME_WIDTH - menuWidth) / 2;
        int controlX = menuX + menuWidth / 2 + 10;
        int sliderWidth = 70;

        int value = (int) (((float)(mouseX - controlX) / sliderWidth) * 100);
        value = Math.max(0, Math.min(100, value));

        //System.out.println("value: " + value);

        if (currentSlider.equals("sound")) {
            soundVolume = value;
        } else if (currentSlider.equals("music")) {
            musicVolume = value;
        }
    }

    public TheButton getPauseButton() {
        return pauseButton;
    }

    public TheButton getFastForwardButton() {
        return fastForwardButton;
    }

    public TheButton getOptionsButton() {
        return optionsButton;
    }

    public TheButton getBackOptionsButton() {
        return backOptionsButton;
    }

    public TheButton getMainMenuButton() {
        return mainMenuButton;
    }
}