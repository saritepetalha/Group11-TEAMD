package scenes;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import constants.GameDimensions;
import main.Game;
import main.GameStates;
import managers.AudioManager;
import ui_p.AssetsLoader;
import ui_p.TheButton;

public class Menu extends GameScene implements SceneMethods {

    private BufferedImage img;
    private BufferedImage backgroundImg;
    private TheButton playButton, loadGameButton, mapEditorButton, optionButton, exitButton, statsButton;


    // Music button
    private TheButton musicButton;
    private boolean musicMuted = false;

    private Game game;

    public Menu(Game game) {
        super(game);
        this.game = game;
        initButtons();
    }

    private void initButtons() {
        int buttonWidth = 270;
        int buttonHeight = 55;
        int centerX = GameDimensions.MAIN_MENU_SCREEN_WIDTH / 2 - buttonWidth / 2;
        int centerY = GameDimensions.MAIN_MENU_SCREEN_HEIGHT / 2 - buttonHeight / 2;

        playButton = new TheButton("New Game", centerX, centerY - buttonHeight, buttonWidth, buttonHeight);
        loadGameButton = new TheButton("Load Game", centerX, centerY, buttonWidth, buttonHeight);
        mapEditorButton = new TheButton("Edit Mode", centerX, centerY + buttonHeight, buttonWidth, buttonHeight);
        optionButton = new TheButton("Options", centerX, centerY + buttonHeight*2, buttonWidth, buttonHeight);
        statsButton = new TheButton("View Stats", centerX, centerY + buttonHeight*3, buttonWidth, buttonHeight);
        exitButton = new TheButton("Quit", centerX, centerY + buttonHeight*4, buttonWidth, buttonHeight);


        int musicButtonSize = 32;
        musicButton = new TheButton("Toggle Music",
                GameDimensions.MAIN_MENU_SCREEN_WIDTH - musicButtonSize - 15,
                15,
                musicButtonSize,
                musicButtonSize,
                AssetsLoader.getInstance().regularMusicImg);

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
        g.drawImage(AssetsLoader.getInstance().menuBackgroundImg, 0, 0, GameDimensions.MAIN_MENU_SCREEN_WIDTH,GameDimensions.MAIN_MENU_SCREEN_HEIGHT, null);
    }

    private void drawButtons(Graphics g) {
        Font buttonFont = new Font("MV Boli",Font.PLAIN,45);
        g.setFont(buttonFont);

        playButton.drawStyled(g);
        loadGameButton.drawStyled(g);
        mapEditorButton.drawStyled(g);
        optionButton.drawStyled(g);
        statsButton.drawStyled(g);
        exitButton.drawStyled(g);
    }

    private void drawMusicButton(Graphics g) {
        // draw music button in top right corner
        BufferedImage img = musicMuted ? AssetsLoader.getInstance().pressedMusicImg : AssetsLoader.getInstance().regularMusicImg;

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
            game.changeGameState(GameStates.NEW_GAME_LEVEL_SELECT);
        } else if (loadGameButton.getBounds().contains(x, y)) {
            playButtonClickSound();
            game.changeGameState(GameStates.LOAD_GAME);
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
        } else if (statsButton.getBounds().contains(x, y)) {
            playButtonClickSound();
            GameStates.setGameState(GameStates.STATISTICS);
        }

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
        statsButton.setMouseOver(false);
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
        } else if (statsButton.getBounds().contains(x, y)) {
            statsButton.setMouseOver(true);
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
        } else if (statsButton.getBounds().contains(x, y)) {
            statsButton.setMousePressed(true);
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
        statsButton.resetBooleans();
        exitButton.resetBooleans();
        musicButton.resetBooleans();
    }

    @Override
    public void mouseDragged(int x, int y) {

    }

}
