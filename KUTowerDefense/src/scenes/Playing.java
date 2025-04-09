package scenes;

import java.awt.Color;
import java.awt.Graphics;

import helpMethods.LevelBuilder;
import main.Game;
import managers.TileManager;

public class Playing extends GameScene implements SceneMethods {
    private int[][] level;
    private TileManager tileManager;

    public Playing(Game game) {
        super(game);
        level = LevelBuilder.getLevelData();
        tileManager = new TileManager();

    }

    @Override
    public void render(Graphics g) {

        for (int i = 0; i < level.length; i++) {
            for (int j = 0; j < level[i].length; j++) {
                g.drawImage(tileManager.getSprite(level[i][j]), j * 64, i * 64, null);
            }
        }

        g.setColor(Color.RED);
        g.fillRect(0, 0, 640, 640);
    }
}
