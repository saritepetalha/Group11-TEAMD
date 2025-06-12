package constants;

import java.awt.Dimension;
import java.awt.Toolkit;

public class GameDimensions {

    // Base dimensions
    public static final int BASE_WIDTH = 1280;
    public static final int BASE_HEIGHT = 720;

    // Game dimensions
    public static final int GAME_WIDTH = 1280;
    public static final int GAME_HEIGHT = 720;
    public static final int TOTAL_GAME_WIDTH = 1280 + 4 * ButtonSize.MEDIUM.getSize();

    // Menu dimensions
    public static final int MAIN_MENU_SCREEN_WIDTH = 1280;
    public static final int MAIN_MENU_SCREEN_HEIGHT = 720;

    // Tile dimensions
    public static final int TILE_DISPLAY_SIZE = 64;

    // Button sizes
    public enum ButtonSize {
        SMALL(32),
        MEDIUM(48),
        LARGE(64);

        private final int size;

        ButtonSize(int size) {
            this.size = size;
        }

        public int getSize() {
            return size;
        }
    }

    // Scaling support
    private static double scaleX = 1.0;
    private static double scaleY = 1.0;

    public static void updateScaling(Dimension screenSize) {
        scaleX = (double) screenSize.width / BASE_WIDTH;
        scaleY = (double) screenSize.height / BASE_HEIGHT;

        // Use the smaller scale to maintain aspect ratio
        scaleX = scaleY = Math.min(scaleX, scaleY);
    }

    public static int scaleX(int x) {
        return (int)(x * scaleX);
    }

    public static int scaleY(int y) {
        return (int)(y * scaleY);
    }

    public static double getScaleX() {
        return scaleX;
    }

    public static double getScaleY() {
        return scaleY;
    }

    // Screen size or other dimensions
    public static final int PATHPOINT_DISPLAY_SIZE = 48;

    // Sidebar dimensions (for edit mode)
    public static final int SIDEBAR_WIDTH = 4 * ButtonSize.MEDIUM.getSize();

    // Padding or margin constants
    public static final int BUTTON_PADDING = 4;

}
