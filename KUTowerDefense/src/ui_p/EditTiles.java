package ui_p;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import static main.GameStates.MENU;

import constants.GameDimensions;
import controllers.MapController;
import main.Game;
import managers.AudioManager;
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

    // Support for both old and new architecture
    private MapEditing mapEditing;  // For backwards compatibility
    private MapController mapController;  // For new MVC architecture
    private Tile selectedTile;

    private ArrayList<TheButton> tilesButtons = new ArrayList<>();

    private TheButton startPoint, endPoint;

    // Constructor for original MapEditing compatibility
    public EditTiles(int x, int y, int width, int height, MapEditing mapEditing, Game game, Window owner) {
        super(x, y, width, height);
        this.mapEditing = mapEditing;
        this.game = game;
        this.owner = owner;

        initButtons();
    }

    // Constructor for MVC architecture
    public EditTiles(int x, int y, int width, int height, Object mapEditingObject, Game game, Window owner) {
        super(x, y, width, height);

        // Handle both MapEditing and MVC objects
        if (mapEditingObject instanceof MapEditing) {
            this.mapEditing = (MapEditing) mapEditingObject;
        }
        // For MVC objects, we'll set the controller later

        this.game = game;
        this.owner = owner;

        initButtons();
    }

    // Method to set the MapController for MVC architecture
    public void setMapController(MapController mapController) {
        this.mapController = mapController;
    }

    private void initButtons() {
        backMenu = new TheButton("Back",
                GameDimensions.GAME_WIDTH + 4 * GameDimensions.ButtonSize.MEDIUM.getSize() - GameDimensions.ButtonSize.SMALL.getSize(),
                GameDimensions.BUTTON_PADDING,
                GameDimensions.ButtonSize.SMALL.getSize(),
                GameDimensions.ButtonSize.SMALL.getSize(),
                AssetsLoader.getInstance().buttonImages.get(0)
        );

        // Add 4 pixels to Y position for better spacing
        int buttonYPos = GameDimensions.ButtonSize.MEDIUM.getSize() + 4;

        draw = new TheButton("Draw",
                GameDimensions.GAME_WIDTH,
                buttonYPos,
                GameDimensions.ButtonSize.SMALL.getSize(),
                GameDimensions.ButtonSize.SMALL.getSize(),
                AssetsLoader.getInstance().buttonImages.get(11)
        );

        erase = new TheButton("Erase",
                GameDimensions.GAME_WIDTH + GameDimensions.ButtonSize.SMALL.getSize(),
                buttonYPos,
                GameDimensions.ButtonSize.SMALL.getSize(),
                GameDimensions.ButtonSize.SMALL.getSize(),
                AssetsLoader.getInstance().buttonImages.get(10)
        );

        fill = new TheButton("Fill",
                GameDimensions.GAME_WIDTH + 2 * GameDimensions.ButtonSize.SMALL.getSize(),
                buttonYPos,
                GameDimensions.ButtonSize.SMALL.getSize(),
                GameDimensions.ButtonSize.SMALL.getSize(),
                AssetsLoader.getInstance().buttonImages.get(6)
        );

        trash = new TheButton("Trash",
                GameDimensions.GAME_WIDTH + 3 * GameDimensions.ButtonSize.SMALL.getSize(),
                buttonYPos,
                GameDimensions.ButtonSize.SMALL.getSize(),
                GameDimensions.ButtonSize.SMALL.getSize(),
                AssetsLoader.getInstance().buttonImages.get(3)
        );

        save = new TheButton("Save",
                GameDimensions.GAME_WIDTH + 4 * GameDimensions.ButtonSize.SMALL.getSize(),
                buttonYPos,
                GameDimensions.ButtonSize.SMALL.getSize(),
                GameDimensions.ButtonSize.SMALL.getSize(),
                AssetsLoader.getInstance().buttonImages.get(7)
        );

        mode = new ModeButton(currentMode + " Mode",
                GameDimensions.GAME_WIDTH,
                GameDimensions.BUTTON_PADDING,
                192*2/3,
                64*2/3,
                AssetsLoader.getInstance().modeImage
        );

        // Initialize tile buttons based on available tile manager
        initTileButtons();
    }

    private void initTileButtons() {
        managers.TileManager tileManager = getTileManager();
        if (tileManager == null) return;

        int widthButton = GameDimensions.ButtonSize.MEDIUM.getSize();
        int heightButton = GameDimensions.ButtonSize.MEDIUM.getSize();
        int gameWidth = GameDimensions.GAME_WIDTH;

        int i;
        for(i = 0; i < tileManager.tiles.size(); i++) {
            Tile tile = tileManager.tiles.get(i);

            // skip extra Castle tiles (one button for Castle)
            if (tile.getName().equals("Castle") && tile != tileManager.CastleTopLeft) {
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

        // Positioning path point buttons at buttonsize*13 (higher than before)
        int pathPointY = GameDimensions.ButtonSize.MEDIUM.getSize() * 13;

        startPoint = new TheButton("Start",
                GameDimensions.GAME_WIDTH,
                pathPointY,
                widthButton,
                heightButton,
                i++);

        endPoint = new TheButton("End",
                GameDimensions.GAME_WIDTH + GameDimensions.ButtonSize.MEDIUM.getSize(),
                pathPointY,
                widthButton,
                heightButton,
                i++);
    }

    // Get tile manager from either architecture
    private managers.TileManager getTileManager() {
        if (mapController != null) {
            return mapController.getMapModel().getTileManager();
        } else if (mapEditing != null) {
            return mapEditing.getTileManager();
        }
        return null;
    }

    private void saveLevel(String levelName){
        if (mapController != null) {
            mapController.saveLevel(levelName);
        } else if (mapEditing != null) {
            mapEditing.saveLevel(levelName);
        }
    }

    private void setSelectedTile(Tile tile) {
        this.selectedTile = tile;
        if (mapController != null) {
            mapController.setSelectedTile(tile);
        } else if (mapEditing != null) {
            mapEditing.setSelectedTile(tile);
        }
    }

    private void fillAllTiles() {
        if (mapController != null) {
            mapController.setMode("Fill");
        } else if (mapEditing != null) {
            mapEditing.fillAllTiles();
        }
    }

    private void resetAllTiles() {
        if (mapController != null) {
            mapController.setMode("Trash");
        } else if (mapEditing != null) {
            mapEditing.resetAllTiles();
        }
    }

    private String getCurrentLevelName() {
        if (mapController != null) {
            return mapController.getMapModel().getCurrentLevelName();
        } else if (mapEditing != null) {
            return mapEditing.getCurrentLevelName();
        }
        return null;
    }

    private void setCurrentLevelName(String name) {
        if (mapController != null) {
            mapController.getMapModel().setCurrentLevelName(name);
        } else if (mapEditing != null) {
            mapEditing.setCurrentLevelName(name);
        }
    }

    private void drawButtons(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        mode.setText(currentMode + " Mode");
        mode.draw(g);

        drawActionButton(g2d, draw, AssetsLoader.getInstance().buttonImages.get(11));
        drawActionButton(g2d, erase, AssetsLoader.getInstance().buttonImages.get(10));
        drawActionButton(g2d, fill, AssetsLoader.getInstance().buttonImages.get(6));
        drawActionButton(g2d, trash, AssetsLoader.getInstance().buttonImages.get(3));
        drawActionButton(g2d, save, AssetsLoader.getInstance().buttonImages.get(7));
        drawActionButton(g2d, backMenu, AssetsLoader.getInstance().buttonImages.get(0));

        for (TheButton btn : tilesButtons){
            drawTilesButtonEffect(g2d, btn);
        }

        drawPathPointButton(g2d, startPoint, AssetsLoader.getInstance().startPointImg);
        drawPathPointButton(g2d, endPoint, AssetsLoader.getInstance().endPointImg);

    }

    // method for drawing path point buttons
    private void drawPathPointButton(Graphics2D g2d, TheButton button, BufferedImage normalImg) {
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
            imageX += 2; // add slight offset for pressed effect
            imageY += 2; // add slight offset for pressed effect
        } else if (button.isMouseOver()) {
            // add subtle animation for hover - subtle pulsing or glow effect
            long currentTime = System.currentTimeMillis();
            float pulseAmount = (float) Math.sin(currentTime * 0.005) * 0.1f + 0.9f;
            width = (int)(width * pulseAmount);
            height = (int)(height * pulseAmount);
            imageX = x + (button.getWidth() - width) / 2;
            imageY = y + (button.getHeight() - height) / 2;
        }

        // draw the appropriate image
        g2d.drawImage(normalImg, imageX, imageY, width, height, null);

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
        else {
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
        managers.TileManager tileManager = getTileManager();

        if (tilesButton.getText().equals("Castle")) {
            spriteToDraw = tileManager.getFullCastleSprite();
        } else {
            spriteToDraw = tileManager.getSprite(tilesButton.getId());
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
            BufferedImage pressedEffect = AssetsLoader.getInstance().buttonPressedEffectImages.get(buttonIdP);
            g2d.drawImage(pressedEffect, x, y, width, height, null);

        } else if (button.isMouseOver()) {
            // if mouse is hovering, draw the hover effect with animation
            BufferedImage hoverEffect = AssetsLoader.getInstance().buttonHoverEffectImages.get(buttonIdH);

            // create a shining animation effect
            long currentTime = System.currentTimeMillis();
            float alpha = (float) (0.5f + 0.5f * Math.sin(currentTime * 0.003)); // Oscillate between 0.5 and 1.0

            // set the alpha composite for the hover glow effect
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2d.drawImage(hoverEffect, x, y, width, height, null);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }


    }


    public void draw(Graphics g){
        g.setColor(new Color(157,209,153,255));     // color given in the project's example image
        g.fillRect(x,y,width,height);               // fill rectangular

        drawButtons(g);

        drawEditPanelGrid(g);
    }


    private void drawEditPanelGrid(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(new Color(40, 40, 40, 70));

        float[] dashPattern = {2, 2};
        BasicStroke dottedStroke = new BasicStroke(
                1,
                BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER,
                10.0f,
                dashPattern,
                0.0f
        );

        Stroke originalStroke = g2d.getStroke();
        g2d.setStroke(dottedStroke);

        int gridSize = GameDimensions.ButtonSize.MEDIUM.getSize();

        g2d.drawRect(x, y, width, height);

        g2d.drawLine(x, y + gridSize, x + width, y + gridSize);
        g2d.drawLine(x, y + 2 * gridSize, x + width, y + 2 * gridSize);

        for (int gridY = y + 3 * gridSize; gridY <= y + height; gridY += gridSize) {
            g2d.drawLine(x, gridY, x + width, gridY);
        }

        for (int gridX = x + gridSize; gridX < x + width; gridX += gridSize) {
            g2d.drawLine(gridX, y + 2 * gridSize, gridX, y + height);
        }

        g2d.setStroke(originalStroke);
    }

    public void mouseClicked(int x, int y) {
        if (backMenu.getBounds().contains(x, y)) {
            AudioManager.getInstance().playButtonClickSound();
            game.changeGameState(MENU);
        }
        else if (draw.getBounds().contains(x, y)) {
            AudioManager.getInstance().playButtonClickSound();
            currentMode = "Draw";
            if (mapController != null) {
                mapController.setMode("Draw");
            }
        }
        else if (erase.getBounds().contains(x, y)) {
            AudioManager.getInstance().playButtonClickSound();
            currentMode = "Erase";
            if (mapController != null) {
                mapController.setMode("Erase");
            }
        }
        else if (fill.getBounds().contains(x, y)) {
            AudioManager.getInstance().playButtonClickSound();
            currentMode = "Fill";
            fillAllTiles();
        }
        else if (trash.getBounds().contains(x, y)) {
            AudioManager.getInstance().playButtonClickSound();
            currentMode = "Trash";
            resetAllTiles();
        }
        else if (save.getBounds().contains(x, y)) {
            AudioManager.getInstance().playButtonClickSound();
            String currentLevel = getCurrentLevelName();
            if (currentLevel != null) {
                saveLevel(currentLevel);
            } else {
                String levelName = JOptionPane.showInputDialog(owner, "Enter level name:");
                if (levelName != null && !levelName.trim().isEmpty()) {
                    setCurrentLevelName(levelName.trim());
                    saveLevel(levelName.trim());
                }
            }
        }
        else if (startPoint.getBounds().contains(x, y)) {
            selectedTile = new Tile(AssetsLoader.getInstance().startPointImg,-1,"Start Point");
            setSelectedTile(selectedTile);
            System.out.println("Start point selected"); // Debug output
        }
        else if (endPoint.getBounds().contains(x, y)) {
            selectedTile = new Tile(AssetsLoader.getInstance().endPointImg, -2, "End Point");
            setSelectedTile(selectedTile);
            System.out.println("End point selected"); // Debug output
        }
        else{
            for (TheButton tilesButton : tilesButtons) {
                if (tilesButton.getBounds().contains(x, y)){
                    selectedTile = getTileManager().getTile(tilesButton.getId());
                    setSelectedTile(selectedTile);
                    return;
                }
            }
        }
    }

    public void mouseDragged(int x, int y) {
        if (x < GameDimensions.GAME_WIDTH && y < GameDimensions.GAME_HEIGHT) {
            if (currentMode.equals("Draw")) {
                if (mapController != null) {
                    // For MVC architecture - let controller handle the drag
                    // The controller will call the strategy pattern
                } else if (mapEditing != null) {
                    mapEditing.modifyTile(x, y);
                }
            } else if (currentMode.equals("Erase")) {
                if (mapController != null) {
                    // For MVC architecture - let controller handle the drag
                } else if (mapEditing != null) {
                    mapEditing.eraseTile(x, y);
                }
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
            setSelectedTile(null);
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

    public boolean isMouseOver() {
        return backMenu.isMouseOver() || draw.isMouseOver() || erase.isMouseOver() || 
               fill.isMouseOver() || trash.isMouseOver() || save.isMouseOver() || 
               startPoint.isMouseOver() || endPoint.isMouseOver() || 
               tilesButtons.stream().anyMatch(TheButton::isMouseOver);
    }

    public String getCurrentMode() {
        return currentMode;
    }

}
