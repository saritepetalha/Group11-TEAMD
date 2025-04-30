package scenes;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.swing.*;
import helpMethods.LoadSave;

import constants.GameDimensions;
import main.Game;
import main.GameStates;
import ui_p.TheButton;

public class Menu extends GameScene implements SceneMethods {
    private ArrayList<BufferedImage> sprites = new ArrayList<>();
    private BufferedImage img;
    private Random random;
    private BufferedImage backgroundImg;
    private TheButton playButton, loadGameButton, mapEditorButton, optionButton, exitButton;
    private Game game;

    public Menu(Game game) {
        super(game);
        this.game = game;
        random = new Random();
        importImg();
        loadSprites();
        initButtons();
        loadBackground();
    }

    private void loadBackground() {
        InputStream is = getClass().getResourceAsStream("/KuTowerDefence2.jpg");
        try {
            backgroundImg = ImageIO.read(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void importImg() {
        InputStream is = getClass().getResourceAsStream("/Tiles/Tileset64.png");
        try {
            img = ImageIO.read(is);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void loadSprites() {
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 4; x++) {
                sprites.add(img.getSubimage(x * GameDimensions.TILE_DISPLAY_SIZE, y * GameDimensions.TILE_DISPLAY_SIZE, GameDimensions.TILE_DISPLAY_SIZE, GameDimensions.TILE_DISPLAY_SIZE));
            }
        }
    }

    private void initButtons() {
        int buttonWidth = 270;
        int buttonHeight = 55;
        int centerX = GameDimensions.MAIN_MENU_SCREEN_WIDTH / 2 - buttonWidth / 2;
        int centerY = GameDimensions.MAIN_MENU_SCREEN_HEIGHT / 2 - buttonHeight / 2;

        playButton = new TheButton("New Game", centerX, centerY, buttonWidth, buttonHeight);
        loadGameButton = new TheButton("Load Game", centerX, centerY + buttonHeight, buttonWidth, buttonHeight);
        mapEditorButton = new TheButton("Edit Mode", centerX, centerY + buttonHeight*2, buttonWidth, buttonHeight);
        optionButton = new TheButton("Options", centerX, centerY + buttonHeight*3, buttonWidth, buttonHeight);
        exitButton = new TheButton("Quit", centerX, centerY + buttonHeight*4, buttonWidth, buttonHeight);
    }

    @Override
    public void render(Graphics g) {
        setCustomCursor();
        drawBackground(g);
        drawButtons(g);
    }

    private void drawBackground(Graphics g) {
        g.drawImage(backgroundImg, 0, 0, GameDimensions.MAIN_MENU_SCREEN_WIDTH,GameDimensions.MAIN_MENU_SCREEN_HEIGHT, null);
    }

    private void drawButtons(Graphics g) {
        Font buttonFont = new Font("MV Boli",Font.PLAIN,45);
        g.setFont(buttonFont);

        playButton.drawStyled(g);
        loadGameButton.drawStyled(g);
        mapEditorButton.drawStyled(g);
        optionButton.drawStyled(g);
        exitButton.drawStyled(g);
    }


    @Override
    public void mouseClicked(int x, int y) {
        if (playButton.getBounds().contains(x, y)) {
            game.changeGameState(GameStates.PLAYING);
        } else if (loadGameButton.getBounds().contains(x, y)) {
            showLoadGameDialog();
        } else if (mapEditorButton.getBounds().contains(x, y)) {
            game.changeGameState(GameStates.EDIT);
        } else if (optionButton.getBounds().contains(x, y)) {
            game.changeGameState(GameStates.OPTIONS);
        } else if (exitButton.getBounds().contains(x, y)) {
            System.exit(0);
        }
    }

    private void showLoadGameDialog() {
        System.out.println("\n=== Level Loading Dialog ===");
        ArrayList<String> savedLevels = LoadSave.getSavedLevels();
        System.out.println("Number of levels found: " + savedLevels.size());

        if (savedLevels.isEmpty()) {
            System.out.println("No saved levels found!");
            JOptionPane.showMessageDialog(
                    null,
                    "No saved levels found!\nPlease create a level in Edit Mode first.",
                    "Warning",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        String[] levelNames = savedLevels.toArray(new String[0]);
        System.out.println("Available levels: " + String.join(", ", levelNames));

        String selectedLevel = (String) JOptionPane.showInputDialog(
                null,
                "Select a level to load:",
                "Load Level",
                JOptionPane.QUESTION_MESSAGE,
                null,
                levelNames,
                levelNames[0]
        );

        if (selectedLevel != null) {
            System.out.println("Selected level: " + selectedLevel);
            game.getPlaying().loadLevel(selectedLevel);
            game.changeGameState(GameStates.PLAYING);
            System.out.println("Switched to playing mode");
        } else {
            System.out.println("No level selected");
        }
        System.out.println("=== Dialog Closed ===\n");
    }

    @Override
    public void mouseMoved(int x, int y) {
        playButton.setMouseOver(false);
        loadGameButton.setMouseOver(false);
        mapEditorButton.setMouseOver(false);
        optionButton.setMouseOver(false);
        exitButton.setMouseOver(false);

        if (playButton.getBounds().contains(x, y)) {
            playButton.setMouseOver(true);
        } else if (loadGameButton.getBounds().contains(x, y)) {
            loadGameButton.setMouseOver(true);
        } else if (mapEditorButton.getBounds().contains(x, y)) {
            mapEditorButton.setMouseOver(true);
        } else if (optionButton.getBounds().contains(x, y)) {
            optionButton.setMouseOver(true);
        } else if (exitButton.getBounds().contains(x, y)) {
            exitButton.setMouseOver(true);
        }
    }

    @Override
    public void mousePressed(int x, int y) {
        if (playButton.getBounds().contains(x, y)) {
            playButton.setMousePressed(true);
        } else if (loadGameButton.getBounds().contains(x, y)) {
            loadGameButton.setMousePressed(true);
        } else if (mapEditorButton.getBounds().contains(x, y)) {
            mapEditorButton.setMousePressed(true);
        } else if (optionButton.getBounds().contains(x, y)) {
            optionButton.setMousePressed(true);
        } else if (exitButton.getBounds().contains(x, y)) {
            exitButton.setMousePressed(true);
        }
    }

    @Override
    public void mouseReleased(int x, int y) {
        playButton.resetBooleans();
        loadGameButton.resetBooleans();
        mapEditorButton.resetBooleans();
        optionButton.resetBooleans();
        exitButton.resetBooleans();
    }

    @Override
    public void mouseDragged(int x, int y) {

    }


    private int getRandomInt() {
        return random.nextInt(sprites.size());
    }

}
