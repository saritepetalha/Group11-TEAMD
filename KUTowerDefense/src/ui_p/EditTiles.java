package ui_p;

import java.awt.*;
import java.awt.image.BufferedImage;
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
        backMenu = new TheButton("Back", GameDimensions.GAME_WIDTH + 4* GameDimensions.ButtonSize.MEDIUM.getSize() - GameDimensions.ButtonSize.SMALL.getSize(),
                GameDimensions.BUTTON_PADDING, GameDimensions.ButtonSize.SMALL.getSize(), GameDimensions.ButtonSize.SMALL.getSize());

        int widthButton = GameDimensions.ButtonSize.MEDIUM.getSize();
        int heightButton = GameDimensions.ButtonSize.MEDIUM.getSize();
        int gameWidth = GameDimensions.GAME_WIDTH;

        for(int i = 0; i < playing.getTileManager().tiles.size(); i++) {
            Tile tile = playing.getTileManager().tiles.get(i);
            tilesButtons.add(new TheButton(tile.getName(),gameWidth + widthButton * (i % 4),
                    2*heightButton + widthButton * (i / 4),
                    widthButton,
                    heightButton,
                    i));
        }
    }

    private void drawButtons(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        backMenu.draw(g);

        for (TheButton tilesButton : tilesButtons) {
            x = tilesButton.getX();
            y = tilesButton.getY();
            width = tilesButton.getWidth();
            height = tilesButton.getHeight();

            // changing opacity when the mouse is over tiles.
            if (tilesButton.isMouseOver()) {
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
            } else {
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            }

            g2d.setColor(new Color(157,209,153,255));
            g2d.fillRect(x, y, width, height);

            g2d.drawImage(playing.getTileManager().getSprite(tilesButton.getId()), x, y, width, height, null);
        }

        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
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

        for (TheButton tilesButton : tilesButtons) {
            tilesButton.setMouseOver(false);
        }
        if (backMenu.getBounds().contains(x, y)) {
            backMenu.setMouseOver(true);
        }
        else{
            for (TheButton tilesButton : tilesButtons) {
                if (tilesButton.getBounds().contains(x, y)){
                    backMenu.setMouseOver(true);
                }
                else {
                    for(TheButton tileButtons: tilesButtons){
                        if(tileButtons.getBounds().contains(x, y)){
                            tileButtons.setMouseOver(true);
                            return;
                        }
                    }
                }
            }
        }
    }

    public void mousePressed(int x, int y) {
        if (backMenu.getBounds().contains(x, y)) {
            backMenu.setMousePressed(true);
        }
        else{
            for (TheButton tilesButton : tilesButtons) {
                tilesButton.setMousePressed(true);
                return;
            }
        }
    }

    public void mouseReleased(int x, int y) {
        backMenu.resetBooleans();
    }

}
