package managers;

import constants.GameDimensions;
import helpMethods.LoadSave;
import objects.Tile;
import ui_p.TheButton;

import javax.imageio.ImageIO;
import javax.swing.Timer;
import java.awt.event.ActionListener;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import scenes.Playing;
import models.PlayingModel;
import views.PlayingView;

public class StoneMiningManager {
    private static StoneMiningManager instance;
    private TheButton mineButton;
    private boolean isMiningInProgress;
    private Tile currentMiningTile;
    private Timer miningTimer;
    private Playing playing;
    private PlayingModel gameModel;
    private PlayingView gameView;
    private static final int MINING_DURATION = 20000; // 20 seconds in milliseconds
    private static final int ANIMATION_INTERVAL = 100; // 100ms for smooth animation
    private static final int GOLD_REWARD = 50;
    private long miningStartTime;
    private float miningProgress;
    private Timer miningAnimationTimer;
    private boolean showButton = false;
    private static BufferedImage buttonSheetImg;
    private BufferedImage pickaxeButtonImage;
    private static BufferedImage buttonImage;

    public StoneMiningManager() {
        loadButtonImageFile();

    }

    public static StoneMiningManager getInstance() {
        if (instance == null) {
            instance = new StoneMiningManager();
        }
        return instance;
    }

    // initialize metodu - model ve view parametrelerini alacak şekilde güncellendi
    public void initialize(PlayingModel model, PlayingView view) {
        this.gameModel = model;
        this.gameView = view;
    }

    public void handleStoneClick(Tile tile) {
        if (isMiningInProgress) return;

        // Show or hide button depending on tile ID
        if (tile.getId() == 19 || tile.getId() == 23) {
            currentMiningTile = tile;
            showMiningButton(tile);
            showButton = true;
        } else {
            clearMiningButton();
        }
    }

    public boolean isButtonClicked(int mouseX, int mouseY) {
        return mineButton != null && mineButton.getBounds().contains(mouseX, mouseY);
    }

    public void showMiningButton(Tile tile) {
        int x = tile.getX() * GameDimensions.TILE_DISPLAY_SIZE;
        int y = tile.getY() * GameDimensions.TILE_DISPLAY_SIZE;
        mineButton = new TheButton("Mine", x + 16, y - 32, 32, 32, buttonImage);
    }

    public void clearMiningButton() {
        showButton = false;
        mineButton = null;
    }

    public void mousePressed(int x, int y) {
        if (mineButton != null && showButton) {
            if (mineButton.isMousePressed(x, y)) {
                startMining();
                mineButton.setMousePressed(true);
                showButton = false;
            } else {
                // Clicked outside the button → hide it
                clearMiningButton();
            }
        }
    }


    public void mouseMoved(int x, int y) {
        if (mineButton != null) {
            mineButton.setMouseOver(mineButton.getBounds().contains(x, y));
        }
    }

    public void mouseReleased(int x, int y) {
        if (mineButton != null) {
            mineButton.setMousePressed(false);
        }
    }

    public void startMining() {
        if (mineButton == null) {
            return;
        }
        showButton = false;
        miningStartTime = System.currentTimeMillis();
        isMiningInProgress = true;
        currentMiningTile = new Tile(mineButton.getX() / GameDimensions.TILE_DISPLAY_SIZE, 
                                   mineButton.getY() / GameDimensions.TILE_DISPLAY_SIZE, 
                                   19); // Assuming 19 is the stone tile ID

        // Start mining timer
        miningTimer = new Timer(MINING_DURATION, e -> {
            completeMining();
        });
        miningTimer.setRepeats(false);
        miningTimer.start();

        miningAnimationTimer = new Timer(ANIMATION_INTERVAL, e -> {
            miningProgress = (float)(System.currentTimeMillis() - miningStartTime) / MINING_DURATION;
            if (miningProgress >= 1.0f) {
                miningAnimationTimer.stop();
                completeMining();
            }
        });
        miningAnimationTimer.setRepeats(true);
        miningAnimationTimer.start();
    }

    private void completeMining() {
        if (currentMiningTile != null) {
            // Add gold to player's balance
            if (gameModel != null && gameModel.getPlayerManager() != null) {
                gameModel.getPlayerManager().addGold(GOLD_REWARD);
                if (playing != null) {
                    playing.updateUIResources();
                }
            }
            
            isMiningInProgress = false;
            currentMiningTile = null;
            if (miningTimer != null) {
                miningTimer.stop();
            }
            if (miningAnimationTimer != null) {
                miningAnimationTimer.stop();
            }
        }
    }

    public TheButton getMineButton() {
        return mineButton;
    }

    public boolean isMiningInProgress() {
        return isMiningInProgress;
    }

    public void draw(Graphics2D g) {
        // 1. Draw the mine button if it's currently visible
        if (mineButton != null && showButton) {
            mineButton.draw(g);
        }

        // 2. Draw the mining progress indicator if mining is in progress
        if (currentMiningTile != null && isMiningInProgress) {
            // Enable anti-aliasing for smoother graphics
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Convert tile coordinates to pixel positions
            int tileX = currentMiningTile.getX();
            int tileY = currentMiningTile.getY();
            int tileSize = GameDimensions.TILE_DISPLAY_SIZE;

            int pixelX = tileX * tileSize;
            int pixelY = tileY * tileSize;

            // Define progress bar dimensions and position
            int barWidth = 44;
            int barHeight = 8;
            int barX = pixelX + 10;
            int barY = pixelY + 70;

            // Draw background of the progress bar (semi-transparent dark color)
            g.setColor(new Color(0, 0, 0, 100));
            g.fillRoundRect(barX, barY, barWidth, barHeight, 4, 4);

            // Calculate progress bar fill width based on current progress value (0.0 to 1.0)
            int progressWidth = (int)(40 * miningProgress);
            int progressX = barX + 2;
            int progressY = barY + 2;

            // Draw the filled portion of the progress bar (gold color)
            g.setColor(new Color(255, 215, 0, 200));
            g.fillRoundRect(progressX, progressY, progressWidth, 4, 2, 2);

            // Disable anti-aliasing to restore original rendering settings
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        }
    }

    public void loadButtonImage(BufferedImage sheet) {
        this.buttonSheetImg = sheet;
    }

    public static void loadButtonImageFile() {
        InputStream is = LoadSave.class.getResourceAsStream("/UI/kutowerbuttons4.png");
        try {
            buttonSheetImg = ImageIO.read(is);
            buttonImage = buttonSheetImg.getSubimage(
                    GameDimensions.TILE_DISPLAY_SIZE * 2,
                    GameDimensions.TILE_DISPLAY_SIZE * 2,
                    GameDimensions.TILE_DISPLAY_SIZE,
                    GameDimensions.TILE_DISPLAY_SIZE
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

} 