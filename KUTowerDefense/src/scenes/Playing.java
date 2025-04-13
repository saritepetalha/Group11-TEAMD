package scenes;

import java.awt.Color;
import java.awt.Graphics;

import helpMethods.LevelBuilder;
import main.Game;
import managers.TileManager;
import ui_p.EditTiles;
import ui_p.TheButton;

import static main.GameStates.*;

public class Playing extends GameScene implements SceneMethods {
    private int[][] level;
    private TileManager tileManager;
    private TheButton backMenu;
    private EditTiles editTiles;

    public Playing(Game game) {
        super(game);
        level = LevelBuilder.getLevelData();
        tileManager = new TileManager();
        editTiles = new EditTiles(1536,0,256, 864);
        initButtons();
    }

    private void initButtons() {
        backMenu = new TheButton("Back", 2, 2, 100, 30);
    }

    @Override
    public void render(Graphics g) {
        g.setColor(Color.RED);
        g.fillRect(0, 0, 1536, 864);

        for (int i = 0; i < level.length; i++) {
            for (int j = 0; j < level[i].length; j++) {
                g.drawImage(tileManager.getSprite(level[i][j]), j * 96, i * 96, null);
            }
        }
        drawButtons(g);
        editTiles.draw(g);

    }

    private void drawButtons(Graphics g) {
        backMenu.draw(g);
    }

    @Override
    public void mouseClicked(int x, int y) {
        if (backMenu.getBounds().contains(x, y)) {
            setGameState(MENU);
        }
    }

    @Override
    public void mouseMoved(int x, int y) {
        backMenu.setMouseOver(false);
        if (backMenu.getBounds().contains(x, y)) {
            backMenu.setMouseOver(true);
        }
    }

    @Override
    public void mousePressed(int x, int y) {
        if (backMenu.getBounds().contains(x, y)) {
            backMenu.setMousePressed(true);
        }
    }

    @Override
    public void mouseReleased(int x, int y) {
        backMenu.resetBooleans();
    }
}
