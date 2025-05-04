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
import managers.AudioManager;
import ui_p.ButtonAssets;
import ui_p.TheButton;

public class Menu extends GameScene implements SceneMethods {
    private ArrayList<BufferedImage> sprites = new ArrayList<>();
    private BufferedImage img;
    private Random random;
    private BufferedImage backgroundImg;
    private TheButton playButton, loadGameButton, mapEditorButton, optionButton, exitButton;

    // Music button
    private TheButton musicButton;
    private boolean musicMuted = false;

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

        int musicButtonSize = 32;
        musicButton = new TheButton("Toggle Music",
                GameDimensions.MAIN_MENU_SCREEN_WIDTH - musicButtonSize - 15,
                15,
                musicButtonSize,
                musicButtonSize,
                ButtonAssets.regularMusicImg);

        musicMuted = AudioManager.getInstance().isMusicMuted();
    }

    @Override
    public void render(Graphics g) {
        // Always sync our musicMuted state with AudioManager
        musicMuted = AudioManager.getInstance().isMusicMuted();

        setCustomCursor();
        drawBackground(g);
        drawButtons(g);
        drawMusicButton(g);
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

    private void drawMusicButton(Graphics g) {
        // draw music button in top right corner
        BufferedImage img = musicMuted ? ButtonAssets.pressedMusicImg : ButtonAssets.regularMusicImg;

        if (img != null) {
            g.drawImage(img, musicButton.getX(), musicButton.getY(), musicButton.getWidth(), musicButton.getHeight(), null);

            // draw hover effect if needed
            if (musicButton.isMouseOver()) {
                g.setColor(new Color(255, 255, 255, 50));
                g.fillRect(musicButton.getX(), musicButton.getY(), musicButton.getWidth(), musicButton.getHeight());
            }
        } else {
            // fallback if image is not available
            g.setColor(musicMuted ? Color.RED : Color.GREEN);
            g.fillRect(musicButton.getX(), musicButton.getY(), musicButton.getWidth(), musicButton.getHeight());
        }
    }

    @Override
    public void mouseClicked(int x, int y) {
        if (playButton.getBounds().contains(x, y)) {
            playButtonClickSound();
            game.changeGameState(GameStates.PLAYING);
        } else if (loadGameButton.getBounds().contains(x, y)) {
            playButtonClickSound();
            showLoadGameDialog();
        } else if (mapEditorButton.getBounds().contains(x, y)) {
            playButtonClickSound();
            game.changeGameState(GameStates.EDIT);
        } else if (optionButton.getBounds().contains(x, y)) {
            playButtonClickSound();
            game.changeGameState(GameStates.OPTIONS);
        } else if (exitButton.getBounds().contains(x, y)) {
            playButtonClickSound();
            System.exit(0);
        } else if (musicButton.getBounds().contains(x, y)) {
            playButtonClickSound();
            toggleMusicMute();
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

        LoadGameMenu loadGameMenu = new LoadGameMenu(game);
        loadGameMenu.showMenu();
    }

    private void toggleMusicMute() {
        // Use AudioManager to toggle music and then sync our state with it
        AudioManager.getInstance().toggleMusicMute();
        musicMuted = AudioManager.getInstance().isMusicMuted();
    }

    @Override
    public void mouseMoved(int x, int y) {
        playButton.setMouseOver(false);
        loadGameButton.setMouseOver(false);
        mapEditorButton.setMouseOver(false);
        optionButton.setMouseOver(false);
        exitButton.setMouseOver(false);
        musicButton.setMouseOver(false);

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
        } else if (musicButton.getBounds().contains(x, y)) {
            musicButton.setMouseOver(true);
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
        } else if (musicButton.getBounds().contains(x, y)) {
            musicButton.setMousePressed(true);
        }
    }

    @Override
    public void mouseReleased(int x, int y) {
        playButton.resetBooleans();
        loadGameButton.resetBooleans();
        mapEditorButton.resetBooleans();
        optionButton.resetBooleans();
        exitButton.resetBooleans();
        musicButton.resetBooleans();
    }

    @Override
    public void mouseDragged(int x, int y) {

    }


    private int getRandomInt() {
        return random.nextInt(sprites.size());
    }

}
