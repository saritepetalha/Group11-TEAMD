package ui_p;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import static main.GameStates.MENU;

import constants.GameDimensions;
import main.Game;
import managers.AudioManager;
import popUps.DialogueFactory;
import scenes.MapEditing;
import objects.Tile;
import javax.swing.*;

public class EditTiles extends Bar {
    private Game game;

    private TheButton backMenu;
    private TheButton draw, erase, fill, trash, save;
    private ModeButton mode;
    private String currentMode = "Draw";

    private final Window owner;

    private MapEditing mapEditing;
    private Tile selectedTile;

    private ArrayList<TheButton> tilesButtons = new ArrayList<>();

    private TheButton startPoint, endPoint;

    public EditTiles(int x, int y, int width, int height, MapEditing mapEditing, Game game, Window owner) {
        super(x, y, width, height);
        this.mapEditing = mapEditing;
        this.game = game;
        this.owner = owner;

        initButtons();
    }


    private void initButtons() {
        backMenu = new TheButton("Back",
                GameDimensions.GAME_WIDTH + 4 * GameDimensions.ButtonSize.MEDIUM.getSize() - GameDimensions.ButtonSize.SMALL.getSize(),
                GameDimensions.BUTTON_PADDING,
                GameDimensions.ButtonSize.SMALL.getSize(),
                GameDimensions.ButtonSize.SMALL.getSize(),
                ButtonAssets.buttonImages.get(0)
        );

        draw = new TheButton("Draw",
                GameDimensions.GAME_WIDTH,
                GameDimensions.ButtonSize.MEDIUM.getSize(),
                GameDimensions.ButtonSize.SMALL.getSize(),
                GameDimensions.ButtonSize.SMALL.getSize(),
                ButtonAssets.buttonImages.get(11)
        );

        erase = new TheButton("Erase",
                GameDimensions.GAME_WIDTH + GameDimensions.ButtonSize.SMALL.getSize(),
                GameDimensions.ButtonSize.MEDIUM.getSize(),
                GameDimensions.ButtonSize.SMALL.getSize(),
                GameDimensions.ButtonSize.SMALL.getSize(),
                ButtonAssets.buttonImages.get(10)
        );

        fill = new TheButton("Fill",
                GameDimensions.GAME_WIDTH + 2 * GameDimensions.ButtonSize.SMALL.getSize(),
                GameDimensions.ButtonSize.MEDIUM.getSize(),
                GameDimensions.ButtonSize.SMALL.getSize(),
                GameDimensions.ButtonSize.SMALL.getSize(),
                ButtonAssets.buttonImages.get(6)
        );

        trash = new TheButton("Trash",
                GameDimensions.GAME_WIDTH + 3 * GameDimensions.ButtonSize.SMALL.getSize(),
                GameDimensions.ButtonSize.MEDIUM.getSize(),
                GameDimensions.ButtonSize.SMALL.getSize(),
                GameDimensions.ButtonSize.SMALL.getSize(),
                ButtonAssets.buttonImages.get(3)
        );

        save = new TheButton("Save",
                GameDimensions.GAME_WIDTH + 4 * GameDimensions.ButtonSize.SMALL.getSize(),
                GameDimensions.ButtonSize.MEDIUM.getSize(),
                GameDimensions.ButtonSize.SMALL.getSize(),
                GameDimensions.ButtonSize.SMALL.getSize(),
                ButtonAssets.buttonImages.get(7)
        );

        mode = new ModeButton(currentMode + " Mode",
                GameDimensions.GAME_WIDTH,
                GameDimensions.BUTTON_PADDING,
                192*2/3,
                64*2/3,
                ButtonAssets.modeImage
        );

        int widthButton = GameDimensions.ButtonSize.MEDIUM.getSize();
        int heightButton = GameDimensions.ButtonSize.MEDIUM.getSize();
        int gameWidth = GameDimensions.GAME_WIDTH;

        int i;
        for(i = 0; i < mapEditing.getTileManager().tiles.size(); i++) {
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

        startPoint = new TheButton("Start", GameDimensions.GAME_WIDTH,
                GameDimensions.GAME_HEIGHT -  GameDimensions.BUTTON_PADDING - GameDimensions.ButtonSize.MEDIUM.getSize(),
                widthButton,
                heightButton,
                i++);

        endPoint = new TheButton("End",
                GameDimensions.GAME_WIDTH + GameDimensions.ButtonSize.MEDIUM.getSize(),
                GameDimensions.GAME_HEIGHT -  GameDimensions.BUTTON_PADDING - GameDimensions.ButtonSize.MEDIUM.getSize(),
                widthButton,
                heightButton,
                i++);

    }

    private void saveLevel(String levelName){
        mapEditing.saveLevel(levelName);
    }


    private void drawButtons(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        mode.setText(currentMode + " Mode");
        mode.draw(g);

        drawActionButton(g2d, draw, ButtonAssets.buttonImages.get(11));
        drawActionButton(g2d, erase, ButtonAssets.buttonImages.get(10));
        drawActionButton(g2d, fill, ButtonAssets.buttonImages.get(6));
        drawActionButton(g2d, trash, ButtonAssets.buttonImages.get(3));
        drawActionButton(g2d, save, ButtonAssets.buttonImages.get(7));
        drawActionButton(g2d, backMenu, ButtonAssets.buttonImages.get(0));

        for (TheButton btn : tilesButtons){
            drawTilesButtonEffect(g2d, btn);
        }

        drawPathPointButton(g2d, startPoint, ButtonAssets.startPointImg, ButtonAssets.startPointHoverImg, ButtonAssets.startPointPressedImg);
        drawPathPointButton(g2d, endPoint, ButtonAssets.endPointImg, ButtonAssets.endPointHoverImg, ButtonAssets.endPointPressedImg);

    }

    // method for drawing path point buttons with hover/press animations
    private void drawPathPointButton(Graphics2D g2d, TheButton button, BufferedImage normalImg, BufferedImage hoverImg, BufferedImage pressedImg) {
        int x = button.getX();
        int y = button.getY();
        int width = button.getWidth();
        int height = button.getHeight();

        // draw base button background
        g2d.setColor(new Color(157, 209, 153, 255));
        g2d.fillRect(x, y, width, height);

        // determine which image to use based on button state
        BufferedImage toDraw;
        int imageX = x;
        int imageY = y;

        if (button.isMousePressed()) {
            toDraw = pressedImg;
            imageX += 2; // add slight offset for pressed effect
            imageY += 2; // add slight offset for pressed effect
        } else if (button.isMouseOver()) {
            toDraw = hoverImg;

            // add subtle animation for hover - subtle pulsing or glow effect
            long currentTime = System.currentTimeMillis();
            float pulseAmount = (float) Math.sin(currentTime * 0.005) * 0.1f + 0.9f;
            width = (int)(width * pulseAmount);
            height = (int)(height * pulseAmount);
            imageX = x + (button.getWidth() - width) / 2;
            imageY = y + (button.getHeight() - height) / 2;
        } else {
            toDraw = normalImg;
        }

        // draw the appropriate image
        g2d.drawImage(toDraw, imageX, imageY, width, height, null);

        // draw text label below the image for clarity
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("MV Boli", Font.BOLD, 11));
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(button.getText());
        g2d.drawString(button.getText(), x + (button.getWidth() - textWidth) / 2,
                y + button.getHeight() + fm.getAscent());
    }

    private void drawTilesButtonEffect(Graphics2D g2d, TheButton tilesButton) {
        int x = tilesButton.getX();
        int y = tilesButton.getY();
        int width = tilesButton.getWidth();
        int height = tilesButton.getHeight();

        Composite originalComposite = g2d.getComposite();

        // changing opacity when the mouse is over tiles.
        if (tilesButton.isMouseOver()) {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
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

        g2d.setComposite(originalComposite);
    }

    private void drawActionButton(Graphics2D g2d, TheButton button, BufferedImage image) {
        int x = button.getX();
        int y = button.getY();
        int width = button.getWidth();
        int height = button.getHeight();

        // draw base button background
        g2d.setColor(new Color(157,209,153,255));
        g2d.fillRect(x, y, width, height);

        // first draw the normal image
        g2d.drawImage(image, x, y, width, height, null);

        // get the button index to use for hover effects
        int buttonIdH = 0;
        if (button == draw) {
            buttonIdH = 10;
        } else if (button == erase) {
            buttonIdH = 7;
        } else if (button == fill) {
            buttonIdH = 6;
        } else if (button == trash) {
            buttonIdH = 14;
        } else if (button == save) {
            buttonIdH = 1;
        } else if (button == backMenu) {
            buttonIdH = 11;
        }

        // get the button index to use for pressed effects
        int buttonIdP = 0;
        if (button == draw) {
            buttonIdP = 12;
        } else if (button == erase) {
            buttonIdP = 2;
        } else if (button == fill) {
            buttonIdP = 7;
        } else if (button == trash) {
            buttonIdP = 6;
        } else if (button == save) {
            buttonIdP = 10;
        } else if (button == backMenu) {
            buttonIdP = 8;
        }


        if (button.isMousePressed()) {
            // if button is pressed, draw the pressed effect
            BufferedImage pressedEffect = ButtonAssets.buttonPressedEffectImages.get(buttonIdP);
            g2d.drawImage(pressedEffect, x, y, width, height, null);

        } else if (button.isMouseOver()) {
            // if mouse is hovering, draw the hover effect with animation
            BufferedImage hoverEffect = ButtonAssets.buttonHoverEffectImages.get(buttonIdH);

            // create a shining animation effect
            long currentTime = System.currentTimeMillis();
            float alpha = (float) (0.5f + 0.5f * Math.sin(currentTime * 0.003)); // Oscillate between 0.5 and 1.0

            // set the alpha composite for the hover glow effect
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2d.drawImage(hoverEffect, x, y, width, height, null);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }

        /*
        // (temporary) Draw text centered, if needed
        if (button.isMouseOver() && button.getText() != null && !button.getText().isEmpty()) {
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("MV Boli", Font.BOLD, 11));
            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(button.getText());
            int textHeight = fm.getHeight();
            g2d.drawString(button.getText(), x + (width - textWidth) / 2, y + (height + textHeight / 5) / 2);
        }*/


    }


    public void draw(Graphics g){
        g.setColor(new Color(157,209,153,255));     // color given in the project's example image
        g.fillRect(x,y,width,height);                           // fill rectangular

        drawButtons(g);
    }

    public void mouseClicked(int x, int y) {
        if (backMenu.getBounds().contains(x, y)) {
            AudioManager.getInstance().playButtonClickSound();
            game.changeGameState(MENU);
        }
        else if (draw.getBounds().contains(x, y)) {
            AudioManager.getInstance().playButtonClickSound();
            currentMode = "Draw";
        }
        else if (erase.getBounds().contains(x, y)) {
            AudioManager.getInstance().playButtonClickSound();
            currentMode = "Erase";
        }
        else if (fill.getBounds().contains(x, y)) {
            AudioManager.getInstance().playButtonClickSound();
            currentMode = "Fill";
            mapEditing.fillAllTiles();
        }
        else if (trash.getBounds().contains(x, y)) {
            AudioManager.getInstance().playButtonClickSound();
            currentMode = "Trash";
            mapEditing.resetAllTiles();
        }
        else if (save.getBounds().contains(x, y)) {
            AudioManager.getInstance().playButtonClickSound();
            String currentLevel = mapEditing.getCurrentLevelName();
            if (currentLevel != null) {
                saveLevel(currentLevel);
            } else {
                String levelName = JOptionPane.showInputDialog(owner, "Enter level name:");
                if (levelName != null && !levelName.trim().isEmpty()) {
                    mapEditing.setCurrentLevelName(levelName.trim());
                    saveLevel(levelName.trim());
                }
            }
        }
        else if (startPoint.getBounds().contains(x, y)) {
            selectedTile = new Tile(ButtonAssets.startPointImg,-1,"Start Point");
            mapEditing.setSelectedTile(selectedTile);
            System.out.println("Start point selected"); // Debug output
        }
        else if (endPoint.getBounds().contains(x, y)) {
            selectedTile = new Tile(ButtonAssets.endPointImg, -2, "End Point");
            mapEditing.setSelectedTile(selectedTile);
            System.out.println("End point selected"); // Debug output
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
        startPoint.setMouseOver(false);
        endPoint.setMouseOver(false);

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
        else if (startPoint.getBounds().contains(x, y)) {
            startPoint.setMouseOver(true);
        }
        else if (endPoint.getBounds().contains(x, y)) {
            endPoint.setMouseOver(true);
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
        else if (draw.getBounds().contains(x, y)) {
            releaseAllExceptActive(draw);
        }
        else if (erase.getBounds().contains(x, y)) {
            releaseAllExceptActive(erase);
        }
        else if (fill.getBounds().contains(x, y)) {
            releaseAllExceptActive(fill);
        }
        else if (trash.getBounds().contains(x, y)) {
            releaseAllExceptActive(trash);
        }
        else if (save.getBounds().contains(x, y)) {
            releaseAllExceptActive(save);
        }
        else if (startPoint.getBounds().contains(x, y)) {
            startPoint.setMousePressed(true);
        }
        else if (endPoint.getBounds().contains(x, y)) {
            endPoint.setMousePressed(true);
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

    public void releaseAllExceptActive(TheButton activeButton) {
        draw.resetBooleans();
        erase.resetBooleans();
        fill.resetBooleans();
        trash.resetBooleans();
        save.resetBooleans();

        // only clear the selected tile if not switching to Fill mode
        if (activeButton != fill) {
            mapEditing.setSelectedTile(null);
        }
        activeButton.setMousePressed(true);
    }

    public void mouseReleased(int x, int y) {
        backMenu.resetBooleans();
        save.resetBooleans();
        startPoint.resetBooleans();
        endPoint.resetBooleans();
        for (TheButton tilesButton : tilesButtons) {
            tilesButton.resetBooleans();
        }
    }

    public String getCurrentMode() {
        return currentMode;
    }

}
