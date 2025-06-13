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
        if (!isFullscreenSupported) {
            System.out.println("Fullscreen mode is not supported on this system");
            return false;
        }

        if (isFullscreen) {
            exitFullscreen();
        } else {
            enterFullscreen();
        }

        // Refresh the game screen for the new fullscreen state
        if (game.getGameScreen() != null) {
            game.getGameScreen().refreshForFullscreenChange();
            game.getGameScreen().setPanelSize();
        }

        return isFullscreen;
    }

    public void enterFullscreen() {
        if (!isFullscreenSupported || isFullscreen) {
            return;
        }

        try {
            // Store original display mode
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

            // Recreate and show the window
            game.setVisible(true);
            game.pack();
            game.setLocationRelativeTo(null);

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
        int actualWidth = screenWidth;
        int actualHeight = screenHeight;

        if (game.getGameScreen() != null) {
            actualWidth = game.getGameScreen().getWidth();
            actualHeight = game.getGameScreen().getHeight();
        }

        System.out.println("🔍 SCALING DEBUG - Current game state: " + GameStates.gameState);
        System.out.println("🔍 Screen dimensions: " + screenWidth + "x" + screenHeight);
        System.out.println("🔍 GameScreen dimensions: " + actualWidth + "x" + actualHeight);
        System.out.println("🔍 Base dimensions: " + baseWidth + "x" + baseHeight);

        // Calculate scale factors to fit the content on screen while maintaining aspect ratio
        double scaleX = (double) actualWidth / baseWidth;
        double scaleY = (double) actualHeight / baseHeight;

        // Use uniform scaling to maintain aspect ratio
        double scale = Math.min(scaleX, scaleY);

        this.scaleX = scale;
        this.scaleY = scale;

        // Calculate offsets to center the content
        int scaledWidth = (int) (baseWidth * scale);
        int scaledHeight = (int) (baseHeight * scale);

        this.offsetX = (actualWidth - scaledWidth) / 2;
        this.offsetY = (actualHeight - scaledHeight) / 2;

        System.out.println("🔍 Calculated scale factors:");
        System.out.println("🔍 - scaleX ratio: " + scaleX + ", scaleY ratio: " + scaleY);
        System.out.println("🔍 - Final scale: " + scale);
        System.out.println("🔍 - Scaled size: " + scaledWidth + "x" + scaledHeight);
        System.out.println("🔍 - Offsets: " + offsetX + "x" + offsetY);
    }

    private void resetScalingFactors() {
        this.scaleX = 1.0;
        this.scaleY = 1.0;
        this.offsetX = 0;
        this.offsetY = 0;
    }

    private int getBaseWidth() {
        switch (GameStates.gameState) {
            case PLAYING:
            case GAME_OVER:
                return BASE_GAME_WIDTH;
            case EDIT:
                return GameDimensions.TOTAL_GAME_WIDTH;
            default:
                return BASE_MENU_WIDTH;
        }
    }

    private int getBaseHeight() {
        switch (GameStates.gameState) {
            case PLAYING:
            case GAME_OVER:
            case EDIT:
                return BASE_GAME_HEIGHT;
            default:
                return BASE_MENU_HEIGHT;
        }
    }

    /**
     * Scales mouse coordinates from screen space to game space
     */
    public Point scaleMouseCoordinates(int screenX, int screenY) {
        if (!isFullscreen) {
            return new Point(screenX, screenY);
        }

        // Convert from screen coordinates to game coordinates
        int gameX = (int) ((screenX - offsetX) / scaleX);
        int gameY = (int) ((screenY - offsetY) / scaleY);

        return new Point(gameX, gameY);
    }

    /**
     * Applies scaling transformation to Graphics2D context for rendering
     */
    public void applyRenderingTransform(Graphics2D g2d) {
        if (!isFullscreen) {
            return;
        }

        System.out.println("🎨 APPLYING TRANSFORM - State: " + GameStates.gameState);
        System.out.println("🎨 - Scale: " + scaleX + "x" + scaleY);
        System.out.println("🎨 - Offset: " + offsetX + "," + offsetY);

        // Fill background with black to handle letterboxing using actual GameScreen dimensions
        int actualWidth = screenWidth;
        int actualHeight = screenHeight;

        if (game.getGameScreen() != null) {
            actualWidth = game.getGameScreen().getWidth();
            actualHeight = game.getGameScreen().getHeight();
        }

        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, actualWidth, actualHeight);

        // Apply transformation
        AffineTransform transform = new AffineTransform();
        transform.translate(offsetX, offsetY);
        transform.scale(scaleX, scaleY);
        g2d.setTransform(transform);

        // Enable high-quality rendering
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    }

    /**
     * Resets the Graphics2D transform after rendering
     */
    public void resetRenderingTransform(Graphics2D g2d) {
        if (!isFullscreen) {
            return;
        }

        g2d.setTransform(new AffineTransform());
    }

    /**
     * Updates scaling factors when game state changes
     */
    public void onGameStateChanged() {
        if (isFullscreen) {
            calculateScalingFactors();
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