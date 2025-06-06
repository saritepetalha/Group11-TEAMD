package managers;

import constants.GameDimensions;
import helpMethods.LoadSave;
import interfaces.GameContext;
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

import static constants.GameDimensions.TILE_DISPLAY_SIZE;

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
    private static BufferedImage buttonImage;

    private BufferedImage pickaxeSpriteSheet;
    private BufferedImage[] pickaxeFrames;
    private int currentFrame = 0;
    private int animationTick = 0;
    private static final int MAX_TICKS_PER_FRAME = 8; // speed of animation
    private static final int TOTAL_FRAMES = 5; // update this according to your image
    private boolean isAnimating = false;

    private int miningTickCounter = 0;
    private static final int MINING_TICKS = 60 * 20;
    private final GameContext gameContext;

    public StoneMiningManager(GameContext context) {
        loadButtonImageFile();
        loadPickaxeAnimation();
        this.gameContext = context;
    }

    public static StoneMiningManager getInstance(GameContext context) {
        if (instance == null) {
            instance = new StoneMiningManager(context);
        }
        return instance;
    }

    public static StoneMiningManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("StoneMiningManager must be initialized with context first.");
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
        int x = tile.getX() * TILE_DISPLAY_SIZE;
        int y = tile.getY() * TILE_DISPLAY_SIZE;
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
        isMiningInProgress = true;
        miningTickCounter = 0;
        miningProgress = 0f;

        currentFrame = 0;
        animationTick = 0;
    }



    private void completeMining() {
        if (currentMiningTile != null) {

            if (gameModel != null && gameModel.getPlayerManager() != null) {
                gameModel.getPlayerManager().addGold(GOLD_REWARD);
                if (playing != null) {
                    playing.updateUIResources();
                }
            }

            int x = currentMiningTile.getX();
            int y = currentMiningTile.getY();
            if (gameModel != null && gameModel.getLevel() != null) {
                gameModel.getLevel()[y][x] = 5; // grass tile ID
            }

            isMiningInProgress = false;
            currentMiningTile = null;
            miningProgress = 0f;

            if (miningTimer != null) {
                miningTimer.stop();
                miningTimer = null;
            }
            if (miningAnimationTimer != null) {
                miningAnimationTimer.stop();
                miningAnimationTimer = null;
            }

            currentFrame = 0;
            animationTick = 0;

            mineButton = null;
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

        // 2. If mining is in progress, prepare tile position once
        if (currentMiningTile != null && isMiningInProgress) {
            int tileSize = TILE_DISPLAY_SIZE;
            int tileX = currentMiningTile.getX();
            int tileY = currentMiningTile.getY();
            int pixelX = tileX * tileSize;
            int pixelY = tileY * tileSize;

            // 3. Draw pickaxe animation centered on tile
            if (pickaxeFrames != null && pickaxeFrames.length > 0) {
                int frameWidth = pickaxeFrames[currentFrame].getWidth();
                int frameHeight = pickaxeFrames[currentFrame].getHeight();

                // Center the frame on tile
                int drawX = pixelX + (tileSize / 2) - (frameWidth / 2);
                int drawY = pixelY + (tileSize / 2) - (frameHeight / 2);

                System.out.println("Drawing pickaxe frame " + currentFrame + " at (" + drawX + ", " + drawY + ")");
                g.drawImage(pickaxeFrames[currentFrame], drawX, drawY, null);

                animationTick++;
                if (animationTick >= MAX_TICKS_PER_FRAME) {
                    animationTick = 0;
                    currentFrame = (currentFrame + 1) % TOTAL_FRAMES;
                }
            }

            // 4. Draw progress bar
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int barWidth = 44;
            int barHeight = 8;
            int barX = pixelX + 10;
            int barY = pixelY + tileSize - 20; // slightly above tile bottom

            g.setColor(new Color(0, 0, 0, 100));
            g.fillRoundRect(barX, barY, barWidth, barHeight, 4, 4);

            int progressWidth = (int)(40 * miningProgress);
            int progressX = barX + 2;
            int progressY = barY + 2;

            g.setColor(new Color(255, 215, 0, 200));
            g.fillRoundRect(progressX, progressY, progressWidth, 4, 2, 2);

            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        }
    }



    public static void loadButtonImageFile() {
        InputStream is = LoadSave.class.getResourceAsStream("/UI/kutowerbuttons4.png");
        try {
            buttonSheetImg = ImageIO.read(is);
            buttonImage = buttonSheetImg.getSubimage(
                    TILE_DISPLAY_SIZE * 2,
                    TILE_DISPLAY_SIZE * 2,
                    TILE_DISPLAY_SIZE,
                    TILE_DISPLAY_SIZE
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadPickaxeAnimation() {
        try (InputStream is = LoadSave.class.getResourceAsStream("/UI/Pickaxe_Swing_Anim.png")) {
            BufferedImage original = ImageIO.read(is);

            // Resize sprite sheet to 320x64 (5 frames, 64x64 each)
            pickaxeSpriteSheet = LoadSave.resizeImage(original, 64 * TOTAL_FRAMES, 64);

            pickaxeFrames = new BufferedImage[TOTAL_FRAMES];
            for (int i = 0; i < TOTAL_FRAMES; i++) {
                pickaxeFrames[i] = pickaxeSpriteSheet.getSubimage(
                        i * 64, 0, 64, 64
                );
            }

            System.out.println("Pickaxe animation loaded and resized.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void updateMiningAnimation() {
        if (!isMiningInProgress) return;

        animationTick++;
        if (animationTick >= MAX_TICKS_PER_FRAME) {
            animationTick = 0;
            currentFrame++;

            if (currentFrame >= TOTAL_FRAMES) {
                currentFrame = 0;
                isMiningInProgress = false;
            }
        }
    }

    public void update() {
        if (!isMiningInProgress || currentMiningTile == null)
            return;

        if (gameContext.isGamePaused())
            return;

        float speed = gameContext.getGameSpeedMultiplier();

        miningTickCounter += speed;
        miningProgress = (float) miningTickCounter / MINING_TICKS;

        animationTick += speed;
        if (animationTick >= MAX_TICKS_PER_FRAME) {
            animationTick = 0;
            currentFrame = (currentFrame + 1) % TOTAL_FRAMES;
        }

        if (miningTickCounter >= MINING_TICKS) {
            completeMining();
        }
    }

} 