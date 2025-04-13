package dimensions;

public class GameDimensions {

    // Button size variants
    public enum ButtonSize {
        SMALL(32),
        MEDIUM(40);

        private final int size;

        ButtonSize(int size) {
            this.size = size;
        }

        public int getSize() {
            return size;
        }
    }

    // Screen size or other dimensions
    public static final int GAME_WIDTH = 1024;
    public static final int GAME_HEIGHT = 576;

    // Tile and UI sizes
    public static final int TILE_DISPLAY_SIZE = 64;

    // Padding or margin constants
    public static final int BUTTON_PADDING = 4;


}
