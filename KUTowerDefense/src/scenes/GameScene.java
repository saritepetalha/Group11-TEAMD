package scenes;

import main.Game;
import java.awt.Cursor;

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
            Cursor customCursor = game.getCursor();
            game.setCursor(customCursor);
        }
    }
}
