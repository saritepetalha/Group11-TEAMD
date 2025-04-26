package ui_p;

import constants.GameDimensions;
import main.Game;
import scenes.Playing;

import java.awt.*;

import static main.GameStates.MENU;

public class PlayingBar extends Bar {
    private Game game;
    private Playing playing;
    private TheButton backMenu;

    public PlayingBar(int x, int y, int width, int height, Playing playing) {
        super(x, y, width, height);
        this.playing = playing;

        initButtons();
    }

    private void initButtons() {
        backMenu = new TheButton("Back",
                GameDimensions.GAME_WIDTH + 4 * GameDimensions.ButtonSize.MEDIUM.getSize() - GameDimensions.ButtonSize.SMALL.getSize(),
                GameDimensions.BUTTON_PADDING,
                GameDimensions.ButtonSize.SMALL.getSize(),
                GameDimensions.ButtonSize.SMALL.getSize(),
                ButtonAssets.buttonImages.get(3)
        );

    }

    public void draw(Graphics g) {
        g.setColor(new Color(157,209,153,255));     // color given in the project's example image
        g.fillRect(x,y,width,height);                           // fill rectangular

        drawButtons(g);
    }

    private void drawButtons(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        backMenu.draw(g);

    }

    public void mouseClicked(int x, int y) {
        if (backMenu.getBounds().contains(x, y))
            game.changeGameState(MENU);

    }

    public void mouseMoved(int x, int y) {
        backMenu.setMouseOver(false);
        if (backMenu.getBounds().contains(x, y))
            backMenu.setMouseOver(true);
    }

    public void mousePressed(int x, int y) {
        if (backMenu.getBounds().contains(x, y))
            backMenu.setMousePressed(true);

    }

    public void mouseReleased(int x, int y) {
        backMenu.resetBooleans();

    }

}
