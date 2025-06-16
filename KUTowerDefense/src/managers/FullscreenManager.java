package managers;

import main.Game;
import main.GameStates;
import constants.GameDimensions;

import java.awt.*;
import java.awt.geom.AffineTransform;

/**
 * FullscreenManager handles fullscreen mode transitions and coordinate scaling
 * for resolution-independent rendering and input handling.
 */
public class FullscreenManager {

    private Game game;
    private GraphicsDevice graphicsDevice;
    private DisplayMode originalDisplayMode;
    private boolean isFullscreen = false;
    private boolean isFullscreenSupported = false;

    // Base game dimensions for scaling calculations
    private static final int BASE_GAME_WIDTH = GameDimensions.GAME_WIDTH;
    private static final int BASE_GAME_HEIGHT = GameDimensions.GAME_HEIGHT;
    private static final int BASE_MENU_WIDTH = GameDimensions.MAIN_MENU_SCREEN_WIDTH;
    private static final int BASE_MENU_HEIGHT = GameDimensions.MAIN_MENU_SCREEN_HEIGHT;

    // Scaling factors for coordinate transformation
    private double scaleX = 1.0;
    private double scaleY = 1.0;
    private int offsetX = 0;
    private int offsetY = 0;

    // Current screen dimensions
    private int screenWidth;
    private int screenHeight;

    public FullscreenManager(Game game) {
        this.game = game;
        initializeGraphicsDevice();
    }

    private void initializeGraphicsDevice() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        this.graphicsDevice = ge.getDefaultScreenDevice();
        this.originalDisplayMode = graphicsDevice.getDisplayMode();
        this.isFullscreenSupported = graphicsDevice.isFullScreenSupported();

        // Get current screen dimensions
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.screenWidth = (int) screenSize.getWidth();
        this.screenHeight = (int) screenSize.getHeight();

        System.out.println("FullscreenManager initialized:");
        System.out.println("- Fullscreen supported: " + isFullscreenSupported);
        System.out.println("- Screen resolution: " + screenWidth + "x" + screenHeight);
    }

    public boolean toggleFullscreen() {
        // Only allow fullscreen in PLAYING mode
        if (GameStates.gameState != GameStates.PLAYING) {
            System.out.println("Fullscreen is only available in playing mode");
            return false;
        }

        if (!isFullscreenSupported) {
            System.out.println("Fullscreen mode is not supported on this system");
            return false;
        }

        if (isFullscreen) {
            exitFullscreen();
        } else {
            enterFullscreen();
        }

        // Notify UI components of fullscreen state change first
        notifyFullscreenStateChange();

        // Then refresh the game screen for the new fullscreen state
        if (game.getGameScreen() != null) {
            game.getGameScreen().refreshForFullscreenChange();
            game.getGameScreen().setPanelSize();
        }

        return isFullscreen;
    }

    public void enterFullscreen() {
        // Only allow fullscreen in PLAYING mode
        if (GameStates.gameState != GameStates.PLAYING) {
            System.out.println("Cannot enter fullscreen - not in playing mode");
            return;
        }

        if (!isFullscreenSupported || isFullscreen) {
            return;
        }

        try {
            // Store current state and original display mode
            GameStates currentState = GameStates.gameState;
            originalDisplayMode = graphicsDevice.getDisplayMode();

            // Remove window decorations
            game.dispose();
            game.setUndecorated(true);
            game.setResizable(false);

            // Enter fullscreen
            graphicsDevice.setFullScreenWindow(game);

            // Update screen dimensions
            this.screenWidth = graphicsDevice.getDisplayMode().getWidth();
            this.screenHeight = graphicsDevice.getDisplayMode().getHeight();

            // Calculate scaling factors
            calculateScalingFactors();

            isFullscreen = true;
            System.out.println("Entered fullscreen mode: " + screenWidth + "x" + screenHeight);

            // Recreate and show the window
            game.setVisible(true);
            game.toFront();

            // Force complete GameScreen reset for fullscreen mode
            if (game.getGameScreen() != null) {
                // Remove all components first
                game.getGameScreen().removeAll();

                // Force update content for current state in fullscreen mode
                game.getGameScreen().updateContentForState(currentState, currentState);

                // Set proper panel size for fullscreen mode
                game.getGameScreen().setPanelSize();
                game.getGameScreen().revalidate();
                game.getGameScreen().repaint();
            }
            game.revalidate();
            game.repaint();

        } catch (Exception e) {
            System.err.println("Error entering fullscreen mode: " + e.getMessage());
            e.printStackTrace();
            // Fallback to windowed mode
            exitFullscreen();
        }
    }

    public void exitFullscreen() {
        if (!isFullscreen) {
            return;
        }

        try {
            // Store current state before exiting fullscreen
            GameStates currentState = GameStates.gameState;

            // Exit fullscreen
            graphicsDevice.setFullScreenWindow(null);

            // Restore window decorations
            game.dispose();
            game.setUndecorated(false);
            game.setResizable(false);

            // Reset scaling factors
            resetScalingFactors();

            isFullscreen = false;
            System.out.println("Exited fullscreen mode");

            // Recreate and show the window with proper sizing
            game.setVisible(true);

            // Force complete GameScreen reset for proper windowed mode
            if (game.getGameScreen() != null) {
                // Remove all components first
                game.getGameScreen().removeAll();

                // Force update content for current state in windowed mode
                game.getGameScreen().updateContentForState(currentState, currentState);

                // Set proper panel size for windowed mode
                game.getGameScreen().setPanelSize();
            }

            // Pack and center the window
            game.pack();
            game.setLocationRelativeTo(null);

            // Force complete layout refresh
            if (game.getGameScreen() != null) {
                game.getGameScreen().revalidate();
                game.getGameScreen().repaint();
            }
            game.revalidate();
            game.repaint();

        } catch (Exception e) {
            System.err.println("Error exiting fullscreen mode: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void calculateScalingFactors() {
        // Get base dimensions based on current game state
        int baseWidth = getBaseWidth();
        int baseHeight = getBaseHeight();

        // Use GameScreen dimensions instead of screen dimensions for more accurate scaling
        if (game.getGameScreen() != null) {
            Dimension gameScreenSize = game.getGameScreen().getSize();
            if (gameScreenSize.width > 0 && gameScreenSize.height > 0) {
                baseWidth = gameScreenSize.width;
                baseHeight = gameScreenSize.height;
            }
        }

        // Calculate scaling to maintain aspect ratio
        double screenAspectRatio = (double) screenWidth / screenHeight;
        double gameAspectRatio = (double) baseWidth / baseHeight;

        if (screenAspectRatio > gameAspectRatio) {
            // Screen is wider than game - scale by height
            scaleY = (double) screenHeight / baseHeight;
            scaleX = scaleY;
            offsetX = (screenWidth - (int)(baseWidth * scaleX)) / 2;
            offsetY = 0;
        } else {
            // Screen is taller than game - scale by width
            scaleX = (double) screenWidth / baseWidth;
            scaleY = scaleX;
            offsetX = 0;
            offsetY = (screenHeight - (int)(baseHeight * scaleY)) / 2;
        }

        System.out.println("Scaling factors calculated:");
        System.out.println("- Scale: " + scaleX + ", " + scaleY);
        System.out.println("- Offset: " + offsetX + ", " + offsetY);
        System.out.println("- Base dimensions: " + baseWidth + "x" + baseHeight);
    }

    private void resetScalingFactors() {
        scaleX = 1.0;
        scaleY = 1.0;
        offsetX = 0;
        offsetY = 0;
    }

    private int getBaseWidth() {
        GameStates currentState = GameStates.gameState;
        switch (currentState) {
            case PLAYING:
                // For playing state, get actual level width if available
                if (game.getPlaying() != null && game.getPlaying().getLevel() != null) {
                    int[][] level = game.getPlaying().getLevel();
                    if (level.length > 0) {
                        return level[0].length * GameDimensions.TILE_DISPLAY_SIZE;
                    }
                }
                return BASE_GAME_WIDTH;
            case MENU:
            case OPTIONS:
            case EDIT:
            case NEW_GAME_LEVEL_SELECT:
            case LOAD_GAME:
            default:
                return BASE_MENU_WIDTH;
        }
    }

    private int getBaseHeight() {
        GameStates currentState = GameStates.gameState;
        switch (currentState) {
            case PLAYING:
                // For playing state, get actual level height if available
                if (game.getPlaying() != null && game.getPlaying().getLevel() != null) {
                    int[][] level = game.getPlaying().getLevel();
                    return level.length * GameDimensions.TILE_DISPLAY_SIZE;
                }
                return BASE_GAME_HEIGHT;
            case MENU:
            case OPTIONS:
            case EDIT:
            case NEW_GAME_LEVEL_SELECT:
            case LOAD_GAME:
            default:
                return BASE_MENU_HEIGHT;
        }
    }

    public Point scaleMouseCoordinates(int screenX, int screenY) {
        if (!isFullscreen) {
            return new Point(screenX, screenY);
        }

        // Transform screen coordinates back to game coordinates
        int gameX = (int)((screenX - offsetX) / scaleX);
        int gameY = (int)((screenY - offsetY) / scaleY);

        return new Point(gameX, gameY);
    }

    public void applyRenderingTransform(Graphics2D g2d) {
        if (!isFullscreen) {
            return;
        }

        // Store original transform
        AffineTransform originalTransform = g2d.getTransform();

        // Apply scaling and offset
        g2d.translate(offsetX, offsetY);
        g2d.scale(scaleX, scaleY);

        // Enable high-quality rendering for scaled graphics
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    }

    public void resetRenderingTransform(Graphics2D g2d) {
        if (!isFullscreen) {
            return;
        }

        // Reset to identity transform
        g2d.setTransform(new AffineTransform());
    }

    public void onGameStateChanged() {
        // If we're switching away from PLAYING mode while in fullscreen, exit fullscreen
        if (isFullscreen && GameStates.gameState != GameStates.PLAYING) {
            exitFullscreen();
        }

        // Recalculate scaling factors for the new state
        if (isFullscreen) {
            calculateScalingFactors();
        }
    }

    private void notifyFullscreenStateChange() {
        // Notify Playing UI if it exists and we're in playing mode
        if (GameStates.gameState == GameStates.PLAYING && game.getPlaying() != null) {
            game.getPlaying().getPlayingUI().updateForFullscreen();
        }
    }

    // Getters
    public boolean isFullscreen() { return isFullscreen; }
    public boolean isFullscreenSupported() { return isFullscreenSupported; }
    public double getScaleX() { return scaleX; }
    public double getScaleY() { return scaleY; }
    public int getOffsetX() { return offsetX; }
    public int getOffsetY() { return offsetY; }
    public int getScreenWidth() { return screenWidth; }
    public int getScreenHeight() { return screenHeight; }
}