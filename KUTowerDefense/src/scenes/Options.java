package scenes;

import java.awt.Color;
import java.awt.Graphics;

import dimensions.GameDimensions;
import main.Game;
import ui_p.TheButton;
import static main.GameStates.*;

public class Options extends GameScene implements SceneMethods {

    private TheButton backMenu;

    public Options(Game game) {
        super(game);
        initButtons();
    }

    private void initButtons() {
        backMenu = new TheButton("Back", 2, 2, 100, 30);
    }

    @Override
    public void render(Graphics g) {
        g.setColor(Color.BLUE);
        g.fillRect(0, 0, GameDimensions.GAME_WIDTH, GameDimensions.GAME_HEIGHT);
        drawButtons(g);
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
