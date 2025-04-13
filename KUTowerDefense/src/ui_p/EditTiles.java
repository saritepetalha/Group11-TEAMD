package ui_p;

import java.awt.*;
import java.util.ArrayList;

import static main.GameStates.MENU;
import static main.GameStates.setGameState;

import dimensions.GameDimensions;
import scenes.Playing;
import objects.Tile;

public class EditTiles {

    private int x,y, width, height; // starting position x,y, and width and height of the edit tiles bar

    private TheButton backMenu;
    private Playing playing;

    private ArrayList<TheButton> tilesButtons = new ArrayList<>();

    public EditTiles(int x, int y, int width, int height, Playing playing) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.playing = playing;

        initButtons();
    }

    private void initButtons() {
        backMenu = new TheButton("Back", GameDimensions.GAME_WIDTH + 4* GameDimensions.ButtonSize.MEDIUM.getSize() - GameDimensions.ButtonSize.SMALL.getSize(), GameDimensions.BUTTON_PADDING, GameDimensions.ButtonSize.SMALL.getSize(), GameDimensions.ButtonSize.SMALL.getSize());

        for(int i = 0; i < playing.getTileManager().tiles.size(); i++) {
            Tile tile = playing.getTileManager().tiles.get(i);
            tilesButtons.add(new TheButton(tile.getName(),GameDimensions.GAME_WIDTH , 2*GameDimensions.ButtonSize.MEDIUM.getSize(), GameDimensions.ButtonSize.MEDIUM.getSize(), GameDimensions.ButtonSize.MEDIUM.getSize()));
        }
    }

    private void drawButtons(Graphics g) {
        backMenu.draw(g);
    }

    public void draw(Graphics g){
        g.setColor(new Color(157,209,153,255));     // color given in the project's example image
        g.fillRect(x,y,width,height);                           // fill rectangular

        drawButtons(g);
    }

    public void mouseClicked(int x, int y) {
        if (backMenu.getBounds().contains(x, y)) {
            setGameState(MENU);
        }
    }

    public void mouseMoved(int x, int y) {
        backMenu.setMouseOver(false);
        if (backMenu.getBounds().contains(x, y)) {
            backMenu.setMouseOver(true);
        }
    }

    public void mousePressed(int x, int y) {
        if (backMenu.getBounds().contains(x, y)) {
            backMenu.setMousePressed(true);
        }
    }

    public void mouseReleased(int x, int y) {
        backMenu.resetBooleans();
    }

}
