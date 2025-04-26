package ui_p;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import static main.GameStates.MENU;

import dimensions.GameDimensions;
import helpMethods.LoadSave;
import main.Game;
import popUps.DialogueFactory;
import scenes.MapEditing;
import objects.Tile;

import javax.imageio.ImageIO;
import javax.swing.*;

public class EditTiles extends EditBar{
    private Game game;
    private int x,y, width, height; // starting position x,y, and width and height of the edit tiles bar

    private TheButton backMenu;
    private TheButton draw, erase, fill, trash, save;
    private ModeButton mode;
    private static BufferedImage img;
    private String currentMode = "Draw";

    private final Window owner;

    private MapEditing mapEditing;
    private Tile selectedTile;

    private ArrayList<TheButton> tilesButtons = new ArrayList<>();
    private ArrayList<BufferedImage> ButtonImages = new ArrayList<>();

    private static BufferedImage buttonSheetImg;
    private static BufferedImage modeLabelImg;
    private BufferedImage modeImage;

    public EditTiles(int x, int y, int width, int height, MapEditing mapEditing, Game game, Window owner) {
        super(x, y, width, height);
        this.mapEditing = mapEditing;
        this.game = game;
        this.owner = owner;

        loadButtonImageFile();
        loadButtonImages();

        loadModeImageFile();
        loadModeImage();

        initButtons();
    }

    public static void loadButtonImageFile() {
        InputStream is = LoadSave.class.getResourceAsStream("/UI/kutowerbuttons4.png");
        try {
            buttonSheetImg = ImageIO.read(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadModeImageFile() {
        InputStream is = LoadSave.class.getResourceAsStream("/UI/Button_Blue_3Slides.png");
        try {
            modeLabelImg = ImageIO.read(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadButtonImages() {
        int tileSize = 64;

        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 4; x++) {
                int subX = x * tileSize;
                int subY = y * tileSize;
                ButtonImages.add(buttonSheetImg.getSubimage(subX, subY, tileSize, tileSize));
            }
        }
    }

    private void loadModeImage() {
        modeImage = modeLabelImg.getSubimage(0, 0, 192, 64);
    }



    private void initButtons() {
        backMenu = new TheButton("Back",
                GameDimensions.GAME_WIDTH + 4 * GameDimensions.ButtonSize.MEDIUM.getSize() - GameDimensions.ButtonSize.SMALL.getSize(),
                GameDimensions.BUTTON_PADDING,
                GameDimensions.ButtonSize.SMALL.getSize(),
                GameDimensions.ButtonSize.SMALL.getSize(),
                ButtonImages.get(3)
        );

        draw = new TheButton("Draw",
                GameDimensions.GAME_WIDTH,
                GameDimensions.ButtonSize.MEDIUM.getSize(),
                GameDimensions.ButtonSize.SMALL.getSize(),
                GameDimensions.ButtonSize.SMALL.getSize(),
                ButtonImages.get(0)
        );

        erase = new TheButton("Erase",
                GameDimensions.GAME_WIDTH + GameDimensions.ButtonSize.SMALL.getSize(),
                GameDimensions.ButtonSize.MEDIUM.getSize(),
                GameDimensions.ButtonSize.SMALL.getSize(),
                GameDimensions.ButtonSize.SMALL.getSize(),
                ButtonImages.get(13)
        );

        fill = new TheButton("Fill",
                GameDimensions.GAME_WIDTH + 2 * GameDimensions.ButtonSize.SMALL.getSize(),
                GameDimensions.ButtonSize.MEDIUM.getSize(),
                GameDimensions.ButtonSize.SMALL.getSize(),
                GameDimensions.ButtonSize.SMALL.getSize(),
                ButtonImages.get(12)
        );

        trash = new TheButton("Trash",
                GameDimensions.GAME_WIDTH + 3 * GameDimensions.ButtonSize.SMALL.getSize(),
                GameDimensions.ButtonSize.MEDIUM.getSize(),
                GameDimensions.ButtonSize.SMALL.getSize(),
                GameDimensions.ButtonSize.SMALL.getSize(),
                ButtonImages.get(1)
        );

        save = new TheButton("Save",
                GameDimensions.GAME_WIDTH + 4 * GameDimensions.ButtonSize.SMALL.getSize(),
                GameDimensions.ButtonSize.MEDIUM.getSize(),
                GameDimensions.ButtonSize.SMALL.getSize(),
                GameDimensions.ButtonSize.SMALL.getSize(),
                ButtonImages.get(2)
        );

        mode = new ModeButton(currentMode + " Mode",
                GameDimensions.GAME_WIDTH,
                GameDimensions.BUTTON_PADDING,
                192*2/3,
                64*2/3,
                modeImage
        );



        int widthButton = GameDimensions.ButtonSize.MEDIUM.getSize();
        int heightButton = GameDimensions.ButtonSize.MEDIUM.getSize();
        int gameWidth = GameDimensions.GAME_WIDTH;

        for(int i = 0; i < mapEditing.getTileManager().tiles.size(); i++) {
            Tile tile = mapEditing.getTileManager().tiles.get(i);

            // skip extra Castle tiles (one button for Castle)
            if (tile.getName().equals("Castle") && tile != mapEditing.getTileManager().CastleTopLeft) {
                continue;
            }

            // determining if the tile should be large by checking if its name is Castle.
            boolean isLarge = tile.getName().equals("Castle");

            int buttonWidth = isLarge ? 2 * widthButton : widthButton;
            int buttonHeight = isLarge ? 2 * heightButton : heightButton;

            int xPos = gameWidth + widthButton * (i % 4);
            int yPos = 2 * heightButton + widthButton * (i / 4);

            tilesButtons.add(new TheButton(tile.getName(),
                    xPos,
                    yPos,
                    buttonWidth,
                    buttonHeight,
                    i));
        }
    }


    private void saveLevel(String levelName){
        mapEditing.saveLevel(levelName);
    }


    private void drawButtons(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        backMenu.draw(g);
        draw.draw(g);
        erase.draw(g);
        fill.draw(g);
        trash.draw(g);
        save.draw(g);

        mode.setText(currentMode + " Mode");
        mode.draw(g);

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

            int imageX = x;
            int imageY = y;

            // applying offset if button is being pressed
            if (tilesButton.isMousePressed()) {
                imageX -= 4; // offset to left by 2 pixels
                imageY -= 4; // offset up by 2 pixels
            }

            BufferedImage spriteToDraw;

            if (tilesButton.getText().equals("Castle")) {
                spriteToDraw = mapEditing.getTileManager().getFullCastleSprite();
            } else {
                spriteToDraw = mapEditing.getTileManager().getSprite(tilesButton.getId());
            }

            g2d.drawImage(spriteToDraw,
                    imageX,
                    imageY,
                    width,
                    height,
                    null);

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
            game.changeGameState(MENU);
        }
        else if (draw.getBounds().contains(x, y)) {
            currentMode = "Draw";
        }
        else if (erase.getBounds().contains(x, y)) {
            currentMode = "Erase";
        }
        else if (fill.getBounds().contains(x, y)) {
            currentMode = "Fill";
            mapEditing.fillAllTiles();
        }
        else if (trash.getBounds().contains(x, y)) {
            currentMode = "Trash";
            mapEditing.resetAllTiles();
        }
        else if (save.getBounds().contains(x, y)) {
            DialogueFactory dialogs = new DialogueFactory((JFrame) owner);      // owner = the main window
            String levelName        = dialogs.createSaveLevelDialog()  // returns Dialog<String>
                    .showAndWait();           // blocks, waits for user

            /* 2) validate & save */
            if (levelName != null && !levelName.trim().isEmpty()) {
                saveLevel(levelName.trim());
            }
        }
        else{
            for (TheButton tilesButton : tilesButtons) {
                if (tilesButton.getBounds().contains(x, y)){
                    selectedTile = mapEditing.getTileManager().getTile(tilesButton.getId());
                    mapEditing.setSelectedTile(selectedTile);
                    return;
                }
            }
        }
    }

    public void mouseDragged(int x, int y) {
        if (x < GameDimensions.GAME_WIDTH && y < GameDimensions.GAME_HEIGHT) {
            if (currentMode.equals("Draw")) {
                mapEditing.modifyTile(x, y);
            } else if (currentMode.equals("Erase")) {
                mapEditing.eraseTile(x, y);
            }
        }
    }

    public void mouseMoved(int x, int y) {
        backMenu.setMouseOver(false);
        draw.setMouseOver(false);
        erase.setMouseOver(false);
        fill.setMouseOver(false);
        trash.setMouseOver(false);
        save.setMouseOver(false);

        for (TheButton tilesButton : tilesButtons) {
            tilesButton.setMouseOver(false);
        }
        if (backMenu.getBounds().contains(x, y)) {
            backMenu.setMouseOver(true);
        }
        else if (draw.getBounds().contains(x, y)) {
            draw.setMouseOver(true);
        }
        else if (erase.getBounds().contains(x, y)) {
            erase.setMouseOver(true);
        }
        else if (fill.getBounds().contains(x, y)) {
            fill.setMouseOver(true);
        }
        else if (trash.getBounds().contains(x, y)) {
            trash.setMouseOver(true);
        }
        else if (save.getBounds().contains(x, y)) {
            save.setMouseOver(true);
        }

        else{
            for (TheButton tilesButton : tilesButtons) {
                if (tilesButton.getBounds().contains(x, y)){
                    tilesButton.setMouseOver(true);
                    return;
                }

            }
        }
    }

    public void mousePressed(int x, int y) {
        if (backMenu.getBounds().contains(x, y)) {
            backMenu.setMousePressed(true);
        }

        else if (save.getBounds().contains(x, y)) {
            save.setMousePressed(true);
        }
        else{
            for (TheButton tilesButton : tilesButtons) {
                if(tilesButton.getBounds().contains(x, y)){
                    tilesButton.setMousePressed(true);
                    return;
                }
            }
        }
    }

    public void mouseReleased(int x, int y) {
        backMenu.resetBooleans();
        save.resetBooleans();
        for (TheButton tilesButton : tilesButtons) {
            tilesButton.resetBooleans();
        }
    }

    public String getCurrentMode() {
        return currentMode;
    }

}
