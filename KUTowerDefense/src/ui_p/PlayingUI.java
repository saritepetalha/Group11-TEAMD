package ui_p;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static constants.Constants.Player.MAX_HEALTH;
import static constants.Constants.Player.MAX_SHIELD;
import constants.GameDimensions;
import helpMethods.FontLoader;
import managers.AudioManager;
import scenes.Playing;

public class PlayingUI {
    private Playing playing;
    private BufferedImage statusBarImg;
    private BufferedImage buttonBgImg;

    // status bar variables
    private int goldAmount;
    private int healthAmount;
    private int shieldAmount;
    private int startingHealthAmount;
    private int startingShieldAmount;

    // Mouse position
    private int mouseX;
    private int mouseY;

    // playing ui buttons
    private TheButton fastForwardButton;
    private TheButton pauseButton;
    private TheButton optionsButton;

    // options menu back button
    private TheButton backOptionsButton;
    private TheButton mainMenuButton;
    private TheButton saveButton;

    // ulti buttons
    private TheButton earthquakeButton;
    private TheButton lightningButton;
    private TheButton goldFactoryButton;
    private TheButton freezeButton;
    
    // Tooltip system
    private CostTooltip tooltip;

    // option values (WILL BE CHANGED TO BE READ FROM FILE)
    private int soundVolume = 80;
    private int musicVolume = 80;
    private boolean sliderDragging = false;
    private String currentSlider = "";
    private String currentDifficulty = "Normal";

    // Music selection dropdown
    private boolean musicDropdownOpen = false;
    private String currentMusic = "Select Music";
    private Rectangle musicDropdownRect;
    private Map<String, Rectangle> musicOptionRects = new HashMap<>();
    private String[] musicOptions;


    private int buttonSize = GameDimensions.ButtonSize.SMALL.getSize();
    private int ultiButtonSize = GameDimensions.ButtonSize.MEDIUM.getSize();

    // Add scroll variables for music dropdown
    private int musicDropdownScrollOffset = 0;
    private int musicDropdownVisibleItems = 8; // Number of visible items in dropdown

    public PlayingUI(Playing playing) {
        this.playing = playing;
        this.startingHealthAmount = MAX_HEALTH;
        this.startingShieldAmount = MAX_SHIELD;
        this.tooltip = new CostTooltip();
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
                AssetsLoader.getInstance().buttonImages.get(8));

        pauseButton = new TheButton("Pause",
                startX + buttonSize + buttonSpacing,
                startY,
                buttonSize,
                buttonSize,
                AssetsLoader.getInstance().buttonImages.get(12));

        optionsButton = new TheButton("Options",
                startX + (buttonSize + buttonSpacing) * 2,
                startY,
                buttonSize,
                buttonSize,
                AssetsLoader.getInstance().buttonImages.get(1));

        goldFactoryButton = new TheButton(
                "Gold Factory",
                startX - 3 * (ultiButtonSize + buttonSpacing),
                startY,
                ultiButtonSize,
                ultiButtonSize,
                AssetsLoader.getInstance().goldFactoryButtonNormal);

        earthquakeButton = new TheButton(
                "Earthquake",
                startX - 2 * (ultiButtonSize + buttonSpacing),
                startY,
                ultiButtonSize,
                ultiButtonSize,
                AssetsLoader.getInstance().earthquakeButtonImg);

        lightningButton = new TheButton("Lightning",
                startX - 4 * (ultiButtonSize + buttonSpacing), startY,
                ultiButtonSize, ultiButtonSize,
                AssetsLoader.getInstance().lightningButtonNormal
        );

        // Freeze button (use earthquake icon for now)
        freezeButton = new TheButton("Freeze",
                startX - 5 * (ultiButtonSize + buttonSpacing), startY,
                ultiButtonSize, ultiButtonSize,
                AssetsLoader.getInstance().earthquakeButtonImg // placeholder icon
        );

        backOptionsButton = new TheButton("Back",
                GameDimensions.GAME_WIDTH / 2 + AssetsLoader.getInstance().optionsMenuImg.getWidth() / 4,
                GameDimensions.GAME_HEIGHT / 2 - AssetsLoader.getInstance().optionsMenuImg.getHeight() / 3,
                buttonSize,
                buttonSize,
                AssetsLoader.getInstance().backOptionsImg);

        // Initialize save button
        saveButton = new TheButton("Save Game",
                GameDimensions.GAME_WIDTH / 2 - AssetsLoader.getInstance().optionsMenuImg.getWidth() / 4,
                GameDimensions.GAME_HEIGHT / 2 - AssetsLoader.getInstance().optionsMenuImg.getHeight() / 3,
                buttonSize,
                buttonSize,
                AssetsLoader.getInstance().saveButtonImg);

        // Initialize main menu button
        mainMenuButton = new TheButton("Main Menu",
                0, 0, 200, 40, null);

        // Initialize volumes from AudioManager
        soundVolume = (int)(AudioManager.getInstance().getSoundVolume() * 100);
        musicVolume = (int)(AudioManager.getInstance().getMusicVolume() * 100);

        // Get available music tracks
        musicOptions = AudioManager.getInstance().getAvailableMusicTracks();
        Arrays.sort(musicOptions); // Sort alphabetically

    }

    public void draw(Graphics g) {
        drawStatusBars(g);
        drawWaveIndicator(g);

        if (playing.isOptionsMenuOpen()) {
            drawOptionsMenu(g);
        } else {
            drawControlButtons(g);
        }

        if (playing.isGamePaused() && !playing.isOptionsMenuOpen()) {
            drawPauseOverlay(g);
        }

        // Draw Lightning preview if targeting mode is active
        if (playing.getUltiManager().isWaitingForLightningTarget()) {
            drawLightningPreview((Graphics2D) g);
        }

        // Draw Gold Factory preview if selected (but not over menus)
        if (playing.getUltiManager().isGoldFactorySelected() &&
                !playing.isOptionsMenuOpen() && !playing.isGamePaused()) {
            drawGoldFactoryPreview((Graphics2D) g);
        }
        
        // Update and draw tooltip
        tooltip.update();
        tooltip.draw((Graphics2D) g);
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

        statusBarImg = AssetsLoader.getInstance().statusBarImg;

        // draw the statusBarImg at half its original size
        if (statusBarImg != null) {
            int imgWidth = statusBarImg.getWidth();
            int imgHeight = statusBarImg.getHeight();
            g.drawImage(statusBarImg, barX + 10, barY + 13, imgWidth / 10, imgHeight / 10, null);
        }

        // draw Gold bar with 3-slide button
        drawSlideButton(g, ""+ goldAmount, barX + 50, barY + 15, buttonWidth, buttonHeight, goldAmount, 1000000);
        // draw Health bar with 3-slide button
        drawSlideButton(g, healthAmount + "/" + startingHealthAmount, barX + 50, barY + 50, buttonWidth, buttonHeight, healthAmount, startingHealthAmount);
        // draw Shield bar with 3-slide button
        drawSlideButton(g, shieldAmount + "/" + startingShieldAmount, barX + 50, barY + 85, buttonWidth, buttonHeight, shieldAmount, startingShieldAmount);
    }

    private void drawWaveIndicator(Graphics g) {
        int buttonSpacing = 8;
        int barWidth = 150; // Increased width for more text
        int barHeight = 32;
        int iconSize = 32;
        int startX = GameDimensions.GAME_WIDTH - barWidth - iconSize - buttonSpacing - 10;
        int startY = buttonSize + 16;

        // Using the new method from Playing that works with our refactored WaveManager
        String waveStatus = playing.getWaveStatus();

        // Extract current wave number for the progress bar
        int currentWave = playing.getWaveManager().getWaveIndex() + 1;
        // Assuming there are about 5 waves total for the progress bar
        // (this is just for visual purposes)
        int estimatedTotalWaves = 5;

        // draw wave icon
        g.drawImage(AssetsLoader.getInstance().waveImg, startX, startY, iconSize, iconSize, null);

        // draw wave status bar with the full status text
        drawSlideButton(g, waveStatus,
                startX + iconSize + buttonSpacing, startY,
                barWidth, barHeight,
                currentWave, estimatedTotalWaves);

        // Draw weather information below wave indicator
        drawWeatherInfo(g, startX, startY + barHeight + 8);
    }

    /**
     * Draws weather information including current weather type and effects
     */
    private void drawWeatherInfo(Graphics g, int x, int y) {
        if (playing.getWeatherManager() == null) return;

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Check if we need to expand panel for snow transition info
        boolean isSnowy = playing.getWeatherManager().isSnowing();
        boolean hasSnowTransition = playing.getTileManager() != null &&
                playing.getTileManager().getSnowState() !=
                        managers.SnowTransitionManager.SnowState.NORMAL;

        // Weather info panel dimensions
        int panelWidth = 180;
        int panelHeight = isSnowy || hasSnowTransition ? 80 : 60; // Expanded height for snow info

        // Background panel
        g2d.setColor(new Color(0, 0, 0, 120));
        g2d.fillRoundRect(x, y, panelWidth, panelHeight, 8, 8);

        // Border
        g2d.setColor(new Color(255, 255, 255, 180));
        g2d.drawRoundRect(x, y, panelWidth, panelHeight, 8, 8);

        // Weather type and time of day
        String weatherType = playing.getWeatherManager().getCurrentWeatherType().toString();
        String timeOfDay = playing.getWeatherManager().getCurrentTimeOfDay();

        // Format weather name
        weatherType = weatherType.substring(0, 1).toUpperCase() + weatherType.substring(1).toLowerCase();

        // Draw weather icon based on type
        Color weatherColor = getWeatherColor(playing.getWeatherManager().getCurrentWeatherType());
        g2d.setColor(weatherColor);
        g2d.fillOval(x + 8, y + 8, 16, 16);

        // Draw weather text
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.drawString(weatherType + " - " + timeOfDay, x + 30, y + 20);

        // Draw weather effects
        g2d.setFont(new Font("Arial", Font.PLAIN, 10));
        String effectText = getWeatherEffectText(playing.getWeatherManager().getCurrentWeatherType());
        if (!effectText.isEmpty()) {
            g2d.setColor(new Color(255, 255, 100));
            g2d.drawString(effectText, x + 8, y + 38);
        }

        // Draw snow transition information if active
        if (isSnowy || hasSnowTransition) {
            drawSnowTransitionInfo(g2d, x, y, panelWidth);
        }

        // Draw additional effect if applicable
        String secondEffect = getSecondaryWeatherEffect(playing.getWeatherManager().getCurrentWeatherType());
        if (!secondEffect.isEmpty()) {
            int effectY = isSnowy || hasSnowTransition ? y + 70 : y + 50;
            g2d.drawString(secondEffect, x + 8, effectY);
        }
    }

    /**
     * Draws snow transition information
     */
    private void drawSnowTransitionInfo(Graphics2D g2d, int x, int y, int panelWidth) {
        if (playing.getTileManager() == null) return;

        String snowState = playing.getTileManager().getSnowStateDescription();
        float progress = playing.getTileManager().getSnowTransitionProgress();

        // Draw snow state text
        g2d.setColor(new Color(200, 220, 255));
        g2d.setFont(new Font("Arial", Font.PLAIN, 9));
        g2d.drawString("Snow: " + snowState, x + 8, y + 52);

        // Draw progress bar for transition
        if (progress > 0.0f && progress < 1.0f) {
            int barWidth = panelWidth - 16;
            int barHeight = 3;
            int barX = x + 8;
            int barY = y + 56;

            // Background
            g2d.setColor(new Color(100, 100, 100, 150));
            g2d.fillRect(barX, barY, barWidth, barHeight);

            // Progress fill
            g2d.setColor(new Color(255, 255, 255, 200));
            g2d.fillRect(barX, barY, (int)(barWidth * progress), barHeight);

            // Border
            g2d.setColor(new Color(255, 255, 255, 100));
            g2d.drawRect(barX, barY, barWidth, barHeight);
        }
    }

    /**
     * Gets the color associated with a weather type
     */
    private Color getWeatherColor(managers.WeatherManager.WeatherType weatherType) {
        switch (weatherType) {
            case RAINY: return new Color(100, 150, 255);
            case SNOWY: return new Color(255, 255, 255);
            case WINDY: return new Color(139, 69, 19);
            case CLEAR: return new Color(255, 255, 100);
            default: return Color.WHITE;
        }
    }

    /**
     * Gets the primary effect text for a weather type
     */
    private String getWeatherEffectText(managers.WeatherManager.WeatherType weatherType) {
        switch (weatherType) {
            case RAINY: return "Tower Range -20%";
            case SNOWY: return "Enemy Speed -25%";
            case WINDY: return "Archers & Archer Warriors +30% Miss";
            case CLEAR: return "No Effect";
            default: return "";
        }
    }

    /**
     * Gets the secondary effect text for a weather type
     */
    private String getSecondaryWeatherEffect(managers.WeatherManager.WeatherType weatherType) {
        switch (weatherType) {
            default: return "";
        }
    }


    private void drawSlideButton(Graphics g, String text, int x, int y, int width, int height, int value, int maxValue) {
        Graphics2D g2d = (Graphics2D) g;

        buttonBgImg = AssetsLoader.getInstance().modeImage;

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
        String[] lines = text.split("\n");
        String firstLine = lines.length > 0 ? lines[0] : "";
        String secondLine = lines.length > 1 ? lines[1] : "";

        int textX1 = x + (width - fm.stringWidth(firstLine)) / 2;
        int textY1 = y + ((height - fm.getHeight()) / 2) + fm.getAscent();
        g2d.setColor(new Color(0, 0, 0, 120));
        g2d.drawString(firstLine, textX1, textY1);

        if (!secondLine.isEmpty()) {
            int textX2 = x + (width - fm.stringWidth(secondLine)) / 2;
            int textY2 = textY1 + 30;
            g2d.setColor(new Color(0, 0, 0, 100));
            g2d.drawString(secondLine, textX2, textY2);
        }
    }

    private void drawControlButtons(Graphics g) {
        int buttonSpacing = 8;
        int startX = GameDimensions.GAME_WIDTH - (buttonSize * 3 + buttonSpacing * 2) - 10;
        int startY = 8;

        Graphics2D g2d = (Graphics2D) g;

        drawControlButton(g2d, fastForwardButton, startX , startY, buttonSize, buttonSize,
                AssetsLoader.getInstance().buttonImages.get(8),
                AssetsLoader.getInstance().buttonHoverEffectImages.get(13),
                AssetsLoader.getInstance().buttonPressedEffectImages.get(9));


        drawControlButton(g2d, pauseButton, startX + buttonSize + buttonSpacing, startY, buttonSize, buttonSize,
                AssetsLoader.getInstance().buttonImages.get(12),
                AssetsLoader.getInstance().buttonHoverEffectImages.get(3),
                AssetsLoader.getInstance().buttonPressedEffectImages.get(13));

        drawControlButton(g2d, optionsButton, startX + (buttonSize + buttonSpacing) * 2, startY, buttonSize, buttonSize,
                AssetsLoader.getInstance().buttonImages.get(1),
                AssetsLoader.getInstance().buttonHoverEffectImages.get(2),
                AssetsLoader.getInstance().buttonPressedEffectImages.get(11));

        drawControlButton(g2d, goldFactoryButton,
                startX - 3 * (ultiButtonSize + buttonSpacing), startY,
                ultiButtonSize, ultiButtonSize,
                AssetsLoader.getInstance().goldFactoryButtonNormal,
                AssetsLoader.getInstance().goldFactoryButtonHover,
                AssetsLoader.getInstance().goldFactoryButtonPressed);

        drawControlButton(g2d, earthquakeButton,
                startX - 2 * (ultiButtonSize + buttonSpacing), startY,
                ultiButtonSize, ultiButtonSize,
                AssetsLoader.getInstance().earthquakeButtonImg,
                AssetsLoader.getInstance().earthquakeButtonHoverImg,
                AssetsLoader.getInstance().earthquakeButtonPressedImg);

        drawControlButton(g2d, lightningButton,
                startX - 4 * (ultiButtonSize + buttonSpacing), startY,
                ultiButtonSize, ultiButtonSize,
                AssetsLoader.getInstance().lightningButtonNormal,
                AssetsLoader.getInstance().lightningButtonHover,
                AssetsLoader.getInstance().lightningButtonPressed);

        // Draw freeze button (use earthquake hover/pressed for now)
        drawControlButton(g2d, freezeButton,
                startX - 5 * (ultiButtonSize + buttonSpacing), startY,
                ultiButtonSize, ultiButtonSize,
                AssetsLoader.getInstance().earthquakeButtonImg,
                AssetsLoader.getInstance().earthquakeButtonHoverImg,
                AssetsLoader.getInstance().earthquakeButtonPressedImg);
    }


    private void drawControlButton(Graphics2D g2d, TheButton button, int x, int y, int width, int height,
                                   BufferedImage normalImg, BufferedImage hoverImg, BufferedImage pressedImg) {

        boolean isEarthquakeCooldown = (button == earthquakeButton && !playing.getUltiManager().canUseEarthquake());
        boolean isLightningCooldown = (button == lightningButton && !playing.getUltiManager().canUseLightning());
        boolean isGoldFactoryUnavailable = false;
        
        if (button == goldFactoryButton) {
            // Check if gold factory can be used (handles cooldown AND active factory check)
            boolean canUse = playing.getUltiManager().canUseGoldFactory();
            boolean canAfford = playing.getPlayerManager().getGold() >= 100;
            boolean hasActiveFactory = playing.getUltiManager().hasActiveGoldFactory();
            long currentGameTime = playing.getGameTime();
            long timeSinceLastUse = currentGameTime - playing.getUltiManager().getLastGoldFactoryTime();
            boolean cooldownReady = timeSinceLastUse >= 30000;
            

            isGoldFactoryUnavailable = !canUse || !canAfford;
        }

        // Draw the normal image first
        g2d.drawImage(normalImg, x, y, width, height, null);

        // Handle different visual states - show pressed/grayed state for unavailable buttons
        if (isEarthquakeCooldown || isLightningCooldown || isGoldFactoryUnavailable || button.isMousePressed()) {
            g2d.drawImage(pressedImg, x, y, width, height, null);
        } else if (button.isMouseOver()) {
            long currentTime = System.currentTimeMillis();
            float alpha = (float) (0.5f + 0.5f * Math.sin(currentTime * 0.003));
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2d.drawImage(hoverImg, x, y, width, height, null);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

            // Only show glow effect for buttons that are actually usable
            if ((button == earthquakeButton && !isEarthquakeCooldown) || 
                (button == lightningButton && !isLightningCooldown) || 
                (button == goldFactoryButton && !isGoldFactoryUnavailable)) {
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha * 0.6f));
                g2d.setColor(new Color(255, 255, 255, 100));
                g2d.setStroke(new BasicStroke(3f));
                g2d.drawRoundRect(x - 2, y - 2, width + 4, height + 4, 8, 8);
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            }
        }
    }

    /**
     * Draws the pause overlay when the game is paused
     */
    public void drawPauseOverlay(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

        // Draw a gradient overlay that's less dark around the pause button
        int pauseButtonX = pauseButton.getX();
        int pauseButtonY = pauseButton.getY();
        int pauseButtonSize = pauseButton.getWidth();

        // Create a radial gradient that's more transparent around the pause button
        RadialGradientPaint gradient = new RadialGradientPaint(
                pauseButtonX + pauseButtonSize/2, pauseButtonY + pauseButtonSize/2, // center point
                pauseButtonSize * 2, // radius
                new float[]{0.0f, 0.5f, 1.0f}, // fractions
                new Color[]{
                        new Color(0, 0, 0, 0), // transparent at center
                        new Color(0, 0, 0, 100), // semi-transparent in middle
                        new Color(0, 0, 0, 150) // darker at edges
                }
        );

        g2d.setPaint(gradient);
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
        Graphics2D g2d = (Graphics2D) g.create();

        BufferedImage optionsImg = AssetsLoader.getInstance().optionsMenuImg;
        int menuWidth = optionsImg.getWidth() / 2;
        int menuHeight = optionsImg.getHeight() / 2;
        int menuX = (GameDimensions.GAME_WIDTH - menuWidth) / 2;
        int menuY = (GameDimensions.GAME_HEIGHT - menuHeight) / 2;

        // draw semi-transparent background overlay
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRect(0, 0, GameDimensions.GAME_WIDTH, GameDimensions.GAME_HEIGHT);

        g2d.drawImage(optionsImg, menuX, menuY, menuWidth, menuHeight, null);

        // draw back button
        if (AssetsLoader.getInstance().backOptionsImg != null) {
            int backButtonX = menuX + menuWidth - buttonSize;
            int backButtonY = menuY + 10;

            backOptionsButton.setX(backButtonX);
            backOptionsButton.setY(backButtonY);

            g2d.drawImage(AssetsLoader.getInstance().backOptionsImg, backButtonX, backButtonY, buttonSize, buttonSize, null);

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

        // 2. Difficulty display - positioned after the music slider
        int difficultyWidth = 90;
        int difficultyHeight = 15;
        int difficultyY = startY + spacing * 2 + 5;

        // display the correct difficulty image
        if (currentDifficulty.equals("Normal") && AssetsLoader.getInstance().difficultyNormalImg != null) {
            g2d.drawImage(AssetsLoader.getInstance().difficultyNormalImg, controlX, difficultyY, difficultyWidth, difficultyHeight, null);
        } else if (currentDifficulty.equals("Easy") && AssetsLoader.getInstance().difficultyEasyImg != null) {
            g2d.drawImage(AssetsLoader.getInstance().difficultyEasyImg, controlX, difficultyY, difficultyWidth, difficultyHeight, null);
        } else if (currentDifficulty.equals("Hard") && AssetsLoader.getInstance().difficultyHardImg != null) {
            g2d.drawImage(AssetsLoader.getInstance().difficultyHardImg, controlX, difficultyY, difficultyWidth, difficultyHeight, null);
        } else if (currentDifficulty.equals("Custom") && AssetsLoader.getInstance().difficultyCustomImg != null) {
            g2d.drawImage(AssetsLoader.getInstance().difficultyCustomImg, controlX, difficultyY, difficultyWidth, difficultyHeight, null);
        } else {
            // fallback if images are not available
            g2d.setColor(new Color(80, 80, 200));
            g2d.fillRoundRect(controlX, difficultyY, difficultyWidth, difficultyHeight, 10, 10);
            g2d.setColor(Color.WHITE);
            g2d.drawString(currentDifficulty, controlX + 30, difficultyY + 12);
        }

        // 3. music selection dropdown
        int dropdownY = startY + spacing / 2; // Positioned between sound slider and music slider
        int dropdownWidth = 120;
        int dropdownHeight = 23;

        g2d.setColor(new Color(60, 60, 60));
        g2d.fillRoundRect(controlX - 10, dropdownY, dropdownWidth, dropdownHeight, 5, 5);

        g2d.setColor(Color.WHITE);
        Font dropdownFont = FontLoader.loadMedodicaFont(14f);
        g2d.setFont(dropdownFont);

        // truncate music name if it's too long
        String displayName = currentMusic;
        if (g2d.getFontMetrics().stringWidth(displayName) > dropdownWidth - 20) {
            int charWidth = g2d.getFontMetrics().charWidth('W');
            int maxChars = (dropdownWidth - 25) / charWidth;
            if (displayName.length() > maxChars) {
                displayName = displayName.substring(0, maxChars - 3) + "...";
            }
        }

        g2d.drawString(displayName, controlX, dropdownY + 17); // Adjusted text position

        g2d.setColor(Color.WHITE);
        int arrowX = controlX + dropdownWidth - 20;
        int arrowY = dropdownY + 12;
        g2d.fillPolygon(
                new int[]{arrowX, arrowX + 8, arrowX + 4},
                new int[]{arrowY, arrowY, arrowY + 5},
                3);

        musicDropdownRect = new Rectangle(controlX - 10, dropdownY, dropdownWidth, dropdownHeight);

        if (musicDropdownOpen && musicOptions != null && musicOptions.length > 0) {
            musicOptionRects.clear();
            int optionHeight = 25; // Made taller

            // calculate dropdown dimensions
            int totalMusicOptions = musicOptions.length;
            int visibleItems = Math.min(musicDropdownVisibleItems, totalMusicOptions);
            int totalDropdownHeight = optionHeight * visibleItems;

            // ensure scroll offset is within bounds
            int maxScrollOffset = Math.max(0, totalMusicOptions - musicDropdownVisibleItems);
            musicDropdownScrollOffset = Math.max(0, Math.min(musicDropdownScrollOffset, maxScrollOffset));

            // ensure the dropdown appears above all other UI elements
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.95f));

            // create scrolling dropdown container
            g2d.setColor(new Color(50, 50, 60, 245));
            g2d.fillRoundRect(controlX - 10, dropdownY + dropdownHeight, dropdownWidth, totalDropdownHeight, 5, 5);

            // draw scrollbar if needed
            if (totalMusicOptions > musicDropdownVisibleItems) {
                int scrollbarWidth = 8;
                int scrollbarHeight = totalDropdownHeight - 4;
                int scrollbarX = controlX + dropdownWidth - scrollbarWidth - 5;
                int scrollbarY = dropdownY + dropdownHeight + 2;

                // scrollbar background
                g2d.setColor(new Color(30, 30, 40));
                g2d.fillRoundRect(scrollbarX, scrollbarY, scrollbarWidth, scrollbarHeight, 4, 4);

                // scrollbar handle
                float handleRatio = (float)musicDropdownVisibleItems / totalMusicOptions;
                int handleHeight = Math.max(20, (int)(scrollbarHeight * handleRatio));
                int handleY = scrollbarY + (int)((scrollbarHeight - handleHeight) * ((float)musicDropdownScrollOffset / maxScrollOffset));

                g2d.setColor(new Color(150, 150, 200));
                g2d.fillRoundRect(scrollbarX, handleY, scrollbarWidth, handleHeight, 4, 4);
            }

            // draw up/down scroll indicators if needed
            if (musicDropdownScrollOffset > 0) {
                // up arrow
                g2d.setColor(new Color(200, 200, 255));
                int upArrowX = controlX + dropdownWidth / 2;
                int upArrowY = dropdownY + dropdownHeight + 10;
                g2d.fillPolygon(
                        new int[]{upArrowX - 8, upArrowX + 8, upArrowX},
                        new int[]{upArrowY + 6, upArrowY + 6, upArrowY},
                        3
                );
            }

            if (musicDropdownScrollOffset < maxScrollOffset) {
                // down arrow
                g2d.setColor(new Color(200, 200, 255));
                int downArrowX = controlX + dropdownWidth / 2;
                int downArrowY = dropdownY + dropdownHeight + totalDropdownHeight - 10;
                g2d.fillPolygon(
                        new int[]{downArrowX - 8, downArrowX + 8, downArrowX},
                        new int[]{downArrowY - 6, downArrowY - 6, downArrowY},
                        3
                );
            }

            g2d.setColor(Color.WHITE);

            // draw visible options based on scroll offset
            for (int i = 0; i < visibleItems; i++) {
                int optionIndex = i + musicDropdownScrollOffset;
                if (optionIndex >= totalMusicOptions) break;

                String option = musicOptions[optionIndex];
                int optionY = dropdownY + dropdownHeight + (i * optionHeight);

                Rectangle optionRect = new Rectangle(controlX - 10, optionY, dropdownWidth, optionHeight);
                musicOptionRects.put(option, optionRect);

                // truncate option name if it's too long
                String displayOption = option;
                if (g2d.getFontMetrics().stringWidth(displayOption) > dropdownWidth - 25) {
                    int charWidth = g2d.getFontMetrics().charWidth('W');
                    int maxChars = (dropdownWidth - 30) / charWidth;
                    if (displayOption.length() > maxChars) {
                        displayOption = displayOption.substring(0, maxChars - 3) + "...";
                    }
                }

                // highlight the option if mouse is over it
                if (optionRect.contains(mouseX, mouseY)) {
                    g2d.setColor(new Color(100, 100, 255));
                    g2d.fillRect(optionRect.x, optionRect.y, optionRect.width, optionRect.height);
                    g2d.setColor(Color.WHITE);
                }

                g2d.drawString(displayOption, controlX + 5, optionY + 17);
            }
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }

        // 4. Music slider
        drawSlider(g2d, controlX, startY + spacing, 70, 20, musicVolume, "music");

        // 6. Return to Main Menu - no visible button, just hitbox
        int btnWidth = 242;
        int btnHeight = 38;
        int btnX = menuX + (menuWidth - btnWidth) / 2;
        int btnY = startY + spacing * 3;

        mainMenuButton.setX(btnX);
        mainMenuButton.setY(btnY);
        mainMenuButton.setWidth(btnWidth);
        mainMenuButton.setHeight(btnHeight);

        // draw invisible hitbox for hover/pressed feedback only if mouse is over
        if (mainMenuButton.isMouseOver()) {
            g2d.setColor(new Color(255, 255, 255, 40));
            g2d.fillRoundRect(btnX, btnY, btnWidth, btnHeight, 15, 15);
        }

        // 7. Save button - positioned below main menu button
        int saveLoadWidth = 120;
        int saveLoadHeight = 30;
        int saveLoadY = btnY + btnHeight + 8; // Position below main menu button

        // Save button
        saveButton.setX(btnX + (btnWidth - saveLoadWidth) / 2); // Center horizontally with main menu button
        saveButton.setY(saveLoadY);
        saveButton.setWidth(saveLoadWidth);
        saveButton.setHeight(saveLoadHeight);

        // Draw the save button image
        BufferedImage saveButtonImage = AssetsLoader.getInstance().saveButtonImg;
        if (saveButtonImage != null) {
            // Draw the save button image
            g2d.drawImage(saveButtonImage, saveButton.getX(), saveLoadY, saveLoadWidth, saveLoadHeight, null);

            // Add hover effect if mouse is over the button
            if (saveButton.isMouseOver()) {
                g2d.setColor(new Color(255, 255, 255, 50)); // Light white overlay for hover effect
                g2d.fillRoundRect(saveButton.getX(), saveLoadY, saveLoadWidth, saveLoadHeight, 5, 5);
            }
        } else {
            // Fallback if image is not available
            g2d.setColor(new Color(60, 60, 60));
            g2d.fillRoundRect(saveButton.getX(), saveLoadY, saveLoadWidth, saveLoadHeight, 5, 5);
            g2d.setColor(Color.WHITE);
            g2d.drawString("Save Game", saveButton.getX() + 5, saveLoadY + 20);
            if (saveButton.isMouseOver()) {
                g2d.setColor(new Color(100, 100, 255));
                g2d.fillRoundRect(saveButton.getX(), saveLoadY, saveLoadWidth, saveLoadHeight, 5, 5);
                g2d.setColor(Color.WHITE);
                g2d.drawString("Save Game", saveButton.getX() + 5, saveLoadY + 20);
            }
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

    private void drawLightningPreview(Graphics2D g2d) {
        int radius = playing.getUltiManager().getLightningRadius();
        int diameter = radius * 2;
        int circleX = mouseX - radius;
        int circleY = mouseY - radius;

        g2d.setColor(new Color(0, 200, 255, 80));
        g2d.fillOval(circleX, circleY, diameter, diameter);

        g2d.setColor(new Color(0, 150, 255, 180));
        g2d.drawOval(circleX, circleY, diameter, diameter);
    }

    private void drawGoldFactoryPreview(Graphics2D g2d) {
        // Snap to tile grid
        int tileX = (mouseX / 64) * 64;
        int tileY = (mouseY / 64) * 64;

        // Check if tile is valid for placement
        boolean isValidTile = false;
        int[][] level = playing.getLevel();
        int levelTileX = mouseX / 64;
        int levelTileY = mouseY / 64;

        if (levelTileY >= 0 && levelTileY < level.length &&
                levelTileX >= 0 && levelTileX < level[0].length) {
            isValidTile = (level[levelTileY][levelTileX] == 5); // Grass tile
        }

        // Enable anti-aliasing for smoother graphics
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw cute rounded background with soft colors
        if (isValidTile) {
            // Soft green with gradient effect
            g2d.setColor(new Color(144, 238, 144, 80)); // Light green with transparency
        } else {
            // Soft red with gradient effect
            g2d.setColor(new Color(255, 182, 193, 80)); // Light pink/red with transparency
        }

        // Draw rounded rectangle instead of harsh rectangle
        g2d.fillRoundRect(tileX + 2, tileY + 2, 60, 60, 12, 12);

        // Draw the factory sprite with transparency
        try {
            BufferedImage factorySprite = AssetsLoader.getInstance().goldFactorySprite;
            if (factorySprite != null) {
                // Draw the sprite with some transparency to show it's a preview
                Composite originalComposite = g2d.getComposite();
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8f));
                g2d.drawImage(factorySprite, tileX + 2, tileY + 2, 60, 60, null);
                g2d.setComposite(originalComposite);
            } else {
                // Fallback: Draw a cute "G" with better styling
                g2d.setColor(new Color(218, 165, 32, 180)); // Gold color
                g2d.setFont(new Font("Arial", Font.BOLD, 20));
                FontMetrics fm = g2d.getFontMetrics();
                String text = "G";
                int textX = tileX + 32 - fm.stringWidth(text) / 2;
                int textY = tileY + 32 + fm.getAscent() / 2;
                g2d.drawString(text, textX, textY);
            }
        } catch (Exception e) {
            // Fallback with better styling
            g2d.setColor(new Color(218, 165, 32, 180));
            g2d.setFont(new Font("Arial", Font.BOLD, 20));
            FontMetrics fm = g2d.getFontMetrics();
            String text = "G";
            int textX = tileX + 32 - fm.stringWidth(text) / 2;
            int textY = tileY + 32 + fm.getAscent() / 2;
            g2d.drawString(text, textX, textY);
        }

        // Draw cute border with rounded corners
        if (isValidTile) {
            g2d.setColor(new Color(34, 139, 34, 120)); // Forest green border
        } else {
            g2d.setColor(new Color(220, 20, 60, 120)); // Crimson border
        }
        g2d.setStroke(new BasicStroke(2f)); // Thicker, softer border
        g2d.drawRoundRect(tileX + 2, tileY + 2, 60, 60, 12, 12);

        // Add a subtle sparkle effect for valid placement
        if (isValidTile) {
            long time = System.currentTimeMillis();
            float sparkleAlpha = (float)(0.3f + 0.2f * Math.sin(time * 0.005f));
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, sparkleAlpha));
            g2d.setColor(new Color(255, 255, 255, 100));
            // Draw small sparkles at corners
            g2d.fillOval(tileX + 8, tileY + 8, 4, 4);
            g2d.fillOval(tileX + 52, tileY + 8, 4, 4);
            g2d.fillOval(tileX + 8, tileY + 52, 4, 4);
            g2d.fillOval(tileX + 52, tileY + 52, 4, 4);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }

        // Reset anti-aliasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
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

    public void setStartingHealthAmount(int startingHealthAmount) { this.startingHealthAmount = startingHealthAmount; }

    public void setStartingShieldAmount(int startingShieldAmount) { this.startingShieldAmount = startingShieldAmount;}

    // add methods to handle mouse hover and press for control buttons
    public void mouseMoved(int mouseX, int mouseY) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;

        // Reset all button hover states
        fastForwardButton.setMouseOver(false);
        pauseButton.setMouseOver(false);
        optionsButton.setMouseOver(false);
        backOptionsButton.setMouseOver(false);
        mainMenuButton.setMouseOver(false);
        saveButton.setMouseOver(false);

        // Reset ultimate button hover states
        earthquakeButton.setMouseOver(false);
        lightningButton.setMouseOver(false);
        goldFactoryButton.setMouseOver(false);
        freezeButton.setMouseOver(false);

        // check which button is hovered
        if (isMouseOverButton(fastForwardButton, mouseX, mouseY)) {
            fastForwardButton.setMouseOver(true);
        } else if (isMouseOverButton(pauseButton, mouseX, mouseY)) {
            pauseButton.setMouseOver(true);
        } else if (isMouseOverButton(optionsButton, mouseX, mouseY)) {
            optionsButton.setMouseOver(true);
        } else if (isMouseOverButton(backOptionsButton, mouseX, mouseY)) {
            backOptionsButton.setMouseOver(true);
        } else if (isMouseOverButton(saveButton, mouseX, mouseY)) {
            saveButton.setMouseOver(true);
        } else if (!musicDropdownOpen && isMouseOverButton(mainMenuButton, mouseX, mouseY)) {
            mainMenuButton.setMouseOver(true);
        }

        // Check ultimate buttons for hover and show tooltips
        if (isMouseOverButton(earthquakeButton, mouseX, mouseY)) {
            earthquakeButton.setMouseOver(true);
            boolean canUse = playing.getUltiManager().canUseEarthquake();
            boolean canAfford = playing.getPlayerManager().getGold() >= 50;
            long remainingCooldown = canUse ? 0 : 15000 - (playing.getGameTime() - playing.getUltiManager().getLastEarthquakeTime());
            tooltip.showUltimate("Earthquake", 50, 
                "Deals 20 damage to all enemies. 50% chance to destroy level 1 towers.",
                canAfford, !canUse, remainingCooldown, mouseX, mouseY);
        } else if (isMouseOverButton(lightningButton, mouseX, mouseY)) {
            lightningButton.setMouseOver(true);
            boolean canUse = playing.getUltiManager().canUseLightning();
            boolean canAfford = playing.getPlayerManager().getGold() >= 75;
            long remainingCooldown = canUse ? 0 : 20000 - (playing.getGameTime() - playing.getUltiManager().getLastLightningTime());
            tooltip.showUltimate("Lightning Strike", 75,
                "Deals 80 damage to enemies in a large radius. Click to target.",
                canAfford, !canUse, remainingCooldown, mouseX, mouseY);
        } else if (isMouseOverButton(goldFactoryButton, mouseX, mouseY)) {
            goldFactoryButton.setMouseOver(true);
            boolean canUse = playing.getUltiManager().canUseGoldFactory();
            boolean canAfford = playing.getPlayerManager().getGold() >= 100;
            boolean hasActiveFactory = playing.getUltiManager().hasActiveGoldFactory();
            
            String description;
            if (hasActiveFactory) {
                description = "Only one Gold Factory allowed at a time. Destroy existing factory first.";
            } else {
                description = "Produces gold bags over time. Place on grass tiles only.";
            }
            
            long remainingCooldown = 0;
            if (!canUse && !hasActiveFactory) {
                remainingCooldown = 30000 - (playing.getGameTime() - playing.getUltiManager().getLastGoldFactoryTime());
            }
            
            tooltip.showUltimate("Gold Factory", 100, description,
                canAfford && !hasActiveFactory, !canUse, remainingCooldown, mouseX, mouseY);
        } else if (isMouseOverButton(freezeButton, mouseX, mouseY)) {
            freezeButton.setMouseOver(true);
            tooltip.showUltimate("Freeze", 0, "Freezes enemies temporarily.", true, false, 0, mouseX, mouseY);
        } else {
            // Hide tooltip if not hovering over any button
            tooltip.hide();
        }
    }

    private boolean isMouseOverButton(TheButton button, int mouseX, int mouseY) {
        return (mouseX >= button.getX() && mouseX <= button.getX() + button.getWidth() &&
                mouseY >= button.getY() && mouseY <= button.getY() + button.getHeight());
    }

    public void mousePressed(int mouseX, int mouseY) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;

        if (playing.getUltiManager().isWaitingForLightningTarget() &&
                !lightningButton.getBounds().contains(mouseX, mouseY)) {

            playing.getUltiManager().triggerLightningAt(mouseX, mouseY);
            return;
        }

        if (playing.getUltiManager().isGoldFactorySelected() &&
                !goldFactoryButton.getBounds().contains(mouseX, mouseY) &&
                !playing.isOptionsMenuOpen() && !playing.isGamePaused()) {

            // Don't handle placement here - let the Playing scene handle it
            return;
        }

        if (pauseButton.getBounds().contains(mouseX, mouseY)) {
            AudioManager.getInstance().playButtonClickSound();
            toggleButtonState(pauseButton);
        } else if (fastForwardButton.getBounds().contains(mouseX, mouseY)) {
            AudioManager.getInstance().playButtonClickSound();

            toggleButtonState(fastForwardButton);
        } else if (optionsButton.getBounds().contains(mouseX, mouseY)) {
            AudioManager.getInstance().playButtonClickSound();
            toggleButtonState(optionsButton);
        } else if (playing.isOptionsMenuOpen()) {
            if (isMouseOverButton(backOptionsButton, mouseX, mouseY)) {
                AudioManager.getInstance().playButtonClickSound();
                toggleButtonState(backOptionsButton);
                musicDropdownOpen = false;
                return;
            }

            // check if clicking on music dropdown
            if (musicDropdownRect != null && musicDropdownRect.contains(mouseX, mouseY)) {
                musicDropdownOpen = !musicDropdownOpen;
                // reset scroll position when opening dropdown
                if (musicDropdownOpen) {
                    musicDropdownScrollOffset = 0;
                }
            }
            // check if clicking on a music option
            else if (musicDropdownOpen) {
                // check if clicking on scroll up button
                int upArrowX = musicDropdownRect.x + musicDropdownRect.width / 2;
                int upArrowY = musicDropdownRect.y + musicDropdownRect.height + 10;
                Rectangle upArrowBounds = new Rectangle(upArrowX - 10, upArrowY - 5, 20, 10);

                // check if clicking on scroll down button
                int visibleItems = Math.min(musicDropdownVisibleItems, musicOptions.length);
                int optionHeight = 25;
                int totalDropdownHeight = optionHeight * visibleItems;
                int downArrowX = upArrowX;
                int downArrowY = musicDropdownRect.y + musicDropdownRect.height + totalDropdownHeight - 10;
                Rectangle downArrowBounds = new Rectangle(downArrowX - 10, downArrowY - 5, 20, 10);

                if (musicDropdownScrollOffset > 0 && upArrowBounds.contains(mouseX, mouseY)) {
                    // Scroll up
                    musicDropdownScrollOffset = Math.max(0, musicDropdownScrollOffset - 1);
                } else if (musicDropdownScrollOffset < Math.max(0, musicOptions.length - musicDropdownVisibleItems)
                        && downArrowBounds.contains(mouseX, mouseY)) {
                    // Scroll down
                    musicDropdownScrollOffset = Math.min(
                            musicOptions.length - musicDropdownVisibleItems,
                            musicDropdownScrollOffset + 1
                    );
                } else {
                    // check if clicking on a music option
                    for (Map.Entry<String, Rectangle> entry : musicOptionRects.entrySet()) {
                        if (entry.getValue().contains(mouseX, mouseY)) {
                            currentMusic = entry.getKey();
                            AudioManager.getInstance().playMusic(currentMusic);
                            musicDropdownOpen = false;
                            break;
                        }
                    }

                    // if not clicking on any option, check if clicking on scrollbar
                    int scrollbarWidth = 8;
                    int scrollbarX = musicDropdownRect.x + musicDropdownRect.width - scrollbarWidth - 5;
                    int scrollbarY = musicDropdownRect.y + musicDropdownRect.height + 2;
                    int scrollbarHeight = totalDropdownHeight - 4;
                    Rectangle scrollbarBounds = new Rectangle(scrollbarX, scrollbarY, scrollbarWidth, scrollbarHeight);

                    if (musicOptions.length > musicDropdownVisibleItems && scrollbarBounds.contains(mouseX, mouseY)) {
                        // calculate new scroll position based on click position
                        float clickPositionRatio = (float)(mouseY - scrollbarY) / scrollbarHeight;
                        musicDropdownScrollOffset = (int)(clickPositionRatio *
                                (musicOptions.length - musicDropdownVisibleItems));
                        musicDropdownScrollOffset = Math.max(0, Math.min(
                                musicOptions.length - musicDropdownVisibleItems,
                                musicDropdownScrollOffset
                        ));
                    }
                }
            }
            // if clicking outside dropdown while it's open, close it
            else if (musicDropdownOpen && !musicDropdownRect.contains(mouseX, mouseY)) {
                musicDropdownOpen = false;
            }
            // pnly process other buttons if dropdown is closed
            else if (!musicDropdownOpen) {
                if (isMouseOverButton(backOptionsButton, mouseX, mouseY)) {
                    AudioManager.getInstance().playButtonClickSound();
                    toggleButtonState(backOptionsButton);
                    musicDropdownOpen = false;
                    return;
                }
                if (isMouseOverButton(saveButton, mouseX, mouseY)) {
                    AudioManager.getInstance().playButtonClickSound();
                    toggleButtonState(saveButton);
                    playing.saveGameState();
                    return;
                }
                if (isMouseOverButton(mainMenuButton, mouseX, mouseY)) {
                    AudioManager.getInstance().playButtonClickSound();
                    toggleButtonState(mainMenuButton);
                    return;
                }

                // check slider interaction
                currentSlider = getSliderAtPosition(mouseX, mouseY);
                if (!currentSlider.isEmpty()) {
                    sliderDragging = true;
                    updateSliderValue(mouseX);
                }
            }
        }
        if (goldFactoryButton.getBounds().contains(mouseX, mouseY)) {
            // Handle gold factory button click
            AudioManager.getInstance().playButtonClickSound();
            toggleButtonState(goldFactoryButton);

            // If already selected, deselect it
            if (playing.getUltiManager().isGoldFactorySelected()) {
                playing.getUltiManager().deselectGoldFactory();
                return;
            }

            // Check if there's already an active factory
            if (playing.getUltiManager().hasActiveGoldFactory()) {
                System.out.println("Only one Gold Factory allowed at a time!");
                return;
            }

            if (playing.getUltiManager().canUseGoldFactory()) {
                if (playing.getPlayerManager().getGold() >= 100) { // goldFactoryCost
                    playing.getUltiManager().selectGoldFactory();
                } else {
                    System.out.println("Not enough gold for Gold Factory!");
                }
            } else {
                System.out.println("Gold Factory is on cooldown!");
            }
            return;
        }

        if (earthquakeButton.getBounds().contains(mouseX, mouseY)) {
            if (playing.getUltiManager().canUseEarthquake()) {
                if (playing.getPlayerManager().getGold() >= 50) { // earthquakeCost
                    AudioManager.getInstance().playButtonClickSound();
                    toggleButtonState(earthquakeButton);
                    playing.getUltiManager().triggerEarthquake();
                } else {
                    System.out.println("Not enough gold for Earthquake!");
                }
            } else {
                System.out.println("Earthquake is on cooldown!");
            }
            return;
        }

        if (lightningButton.getBounds().contains(mouseX, mouseY)) {
            if (playing.getUltiManager().isWaitingForLightningTarget()) {
                playing.getUltiManager().setWaitingForLightningTarget(false);
            } else if (playing.getUltiManager().canUseLightning()) {
                if (playing.getPlayerManager().getGold() >= 75) {
                    playing.getUltiManager().setWaitingForLightningTarget(true);
                } else {
                    System.out.println("Not enough gold for Lightning Strike!");
                }
            } else {
                System.out.println("Lightning Strike is on cooldown!");
            }
            return;
        }

        if (freezeButton != null && freezeButton.getBounds().contains(mouseX, mouseY)) {
            System.out.println("Freeze button clicked");
            playing.getUltiManager().triggerFreeze();
            return;
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
            if (button != saveButton) saveButton.setMousePressed(false);
        }
    }

    public void mouseReleased() {
        // stop slider dragging

        sliderDragging = false;
        currentSlider = "";

        earthquakeButton.setMousePressed(false);
        lightningButton.setMousePressed(false);
        goldFactoryButton.setMousePressed(false);
        freezeButton.setMousePressed(false);
    }

    public void mouseDragged(int mouseX, int mouseY) {
        int prevMouseY = this.mouseY;
        this.mouseX = mouseX;
        this.mouseY = mouseY;

        // handle slider dragging
        if (sliderDragging && !currentSlider.isEmpty()) {
            updateSliderValue(mouseX);
        }

        // handle music dropdown scrolling via drag
        if (musicDropdownOpen && musicOptions.length > musicDropdownVisibleItems) {
            int optionHeight = 25;
            int visibleItems = Math.min(musicDropdownVisibleItems, musicOptions.length);
            int totalDropdownHeight = optionHeight * visibleItems;

            // create dropdown area rectangle
            Rectangle dropdownArea = new Rectangle(
                    musicDropdownRect.x,
                    musicDropdownRect.y + musicDropdownRect.height,
                    musicDropdownRect.width,
                    totalDropdownHeight
            );

            // check if dragging inside dropdown area or scrollbar
            if (dropdownArea.contains(mouseX, mouseY) ||
                    (mouseX >= dropdownArea.x + dropdownArea.width - 15 &&
                            mouseY >= dropdownArea.y && mouseY <= dropdownArea.y + dropdownArea.height)) {

                // calculate drag distance and direction
                int dragDistance = mouseY - prevMouseY;

                if (Math.abs(dragDistance) > 5) {  // threshold to avoid micro-scrolls
                    // calculate scroll movement (negative = scroll up, positive = scroll down)
                    int scrollMove = dragDistance > 0 ? 1 : -1;

                    // update scroll offset
                    musicDropdownScrollOffset += scrollMove;

                    // ensure scroll offset is within bounds
                    int maxScrollOffset = Math.max(0, musicOptions.length - musicDropdownVisibleItems);
                    musicDropdownScrollOffset = Math.max(0, Math.min(musicDropdownScrollOffset, maxScrollOffset));
                }
            }
        }
    }

    /**
     * Check if mouse is over a slider and return the slider's ID if true
     */
    private String getSliderAtPosition(int mouseX, int mouseY) {
        if (!playing.isOptionsMenuOpen()) return "";

        BufferedImage optionsImg = AssetsLoader.getInstance().optionsMenuImg;
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

        BufferedImage optionsImg = AssetsLoader.getInstance().optionsMenuImg;
        int menuWidth = optionsImg.getWidth() / 2;
        int menuX = (GameDimensions.GAME_WIDTH - menuWidth) / 2;
        int controlX = menuX + menuWidth / 2 + 10;
        int sliderWidth = 70;

        int value = (int) (((float)(mouseX - controlX) / sliderWidth) * 100);
        value = Math.max(0, Math.min(100, value));

        //System.out.println("value: " + value);

        if (currentSlider.equals("sound")) {
            soundVolume = value;
            AudioManager.getInstance().setSoundVolume(value / 100.0f);
        } else if (currentSlider.equals("music")) {
            musicVolume = value;
            AudioManager.getInstance().setMusicVolume(value / 100.0f);
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

    /**
     * Handle mouse wheel scrolling over the music dropdown
     * @param e The mouse wheel event
     */
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (playing.isOptionsMenuOpen() && musicDropdownOpen && musicOptions.length > musicDropdownVisibleItems) {
            int mouseX = e.getX();
            int mouseY = e.getY();

            // calculate dropdown dimensions
            int optionHeight = 25;
            int visibleItems = Math.min(musicDropdownVisibleItems, musicOptions.length);
            int totalDropdownHeight = optionHeight * visibleItems;

            // create dropdown area rectangle
            Rectangle dropdownArea = new Rectangle(
                    musicDropdownRect.x,
                    musicDropdownRect.y + musicDropdownRect.height,
                    musicDropdownRect.width,
                    totalDropdownHeight
            );

            // check if mouse is over the dropdown area
            if (dropdownArea.contains(mouseX, mouseY)) {
                // get scroll direction (positive for scroll down, negative for scroll up)
                int scrollDirection = e.getWheelRotation();

                // update scroll offset based on wheel rotation
                // multiply by 2 for faster scrolling
                musicDropdownScrollOffset += scrollDirection * 2;

                // ensure scroll offset is within bounds
                int maxScrollOffset = Math.max(0, musicOptions.length - musicDropdownVisibleItems);
                musicDropdownScrollOffset = Math.max(0, Math.min(musicDropdownScrollOffset, maxScrollOffset));
            }
        }
    }

    /**
     * Sets the current difficulty for display in the options menu
     * @param difficulty The difficulty level ("Easy", "Normal", "Hard", or "Custom")
     */
    public void setCurrentDifficulty(String difficulty) {
        this.currentDifficulty = difficulty;
    }

}