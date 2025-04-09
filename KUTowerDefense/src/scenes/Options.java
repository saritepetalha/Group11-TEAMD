package scenes;

import java.awt.Color;
import java.awt.Graphics;

import main.Game;

public class Options extends GameScene implements SceneMethods {

    public Options(Game game) {
        super(game);
    }

    @Override
    public void render(Graphics g) {
        g.setColor(Color.BLUE);
        g.fillRect(0, 0, 640, 640);
    }
}
