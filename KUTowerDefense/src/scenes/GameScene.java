package scenes;

import main.Game;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import ui_p.AssetsLoader;

public class GameScene {

    protected Game game;

    public GameScene(Game game) {
        this.game = game;
        setCustomCursor();
    }

    public Game getGame() {
        return game;
    }

    protected void setCustomCursor() {
        if (game != null) {
            // Set default cursor to normal cursor
            game.setCursor(AssetsLoader.getInstance().customNormalCursor);
        }
    }

    protected void setHandCursor() {
        if (game != null) {
            game.setCursor(AssetsLoader.getInstance().customHandCursor);
        }
    }

    protected void setNormalCursor() {
        if (game != null) {
            game.setCursor(AssetsLoader.getInstance().customNormalCursor);
        }
    }
}
