package scenes;

import constants.GameDimensions;
import helpMethods.LoadSave;
import main.Game;
import main.GameStates;
import ui_p.TheButton;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

import static helpMethods.LoadSave.resizeImage;

public class GameOverScene extends GameScene implements SceneMethods{

    private int goldEarned;
    private int enemiesSpawned;
    private int enemiesReachedEnd;
    private int towersBuilt;
    private int enemyDefeated;
    private int totalDamage;
    private int timePlayed;
    private TheButton replayButton;
    private TheButton menuButton;
    private BufferedImage victoryBg;
    private BufferedImage defeatBg;
    private BufferedImage resizedVictory;
    private BufferedImage resizedDefeat;
    private boolean isVictory;


    public GameOverScene(Game game) {
        super(game);
        int buttonWidth = 240;
        int buttonHeight = 60;
        int centerX = (1024 - buttonWidth) / 2;

        replayButton = new TheButton("Replay", centerX, 350, buttonWidth, buttonHeight);
        menuButton = new TheButton("Main Menu", centerX, 400, buttonWidth, buttonHeight);

        victoryBg = LoadSave.getImageFromPath("/Victory.png");
        defeatBg = LoadSave.getImageFromPath("/Defeat.png");
        if (victoryBg == null || defeatBg == null) {
            System.err.println("Failed to load background images for GameOverScene!");
        } else {
            resizedVictory = resizeImage(victoryBg, GameDimensions.GAME_WIDTH, GameDimensions.GAME_HEIGHT);
            resizedDefeat = resizeImage(defeatBg, GameDimensions.GAME_WIDTH, GameDimensions.GAME_HEIGHT);
        }

    }

    public void update() {
    }

    public void render(Graphics g) {

        if (isVictory) {
            g.drawImage(resizedVictory, 0, 0, null);
        } else {
            g.drawImage(resizedDefeat, 0, 0, null);
        }
        g.setColor(new Color(0, 0, 0, 100));
        g.fillRect(0, 0, 1024, 768);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 48));
        String gameOverText = isVictory ? "VICTORY!" : "GAME OVER";
        g.drawString(gameOverText, (1024 - g.getFontMetrics().stringWidth(gameOverText)) / 2, 120);

        g.setFont(new Font("Arial", Font.PLAIN, 28));
        g.drawString("Total Gold: " + goldEarned, 50, 200);
        g.drawString("Enemies Spawned: " + enemiesSpawned, 50, 240);
        g.drawString("Enemies Reached End: " + enemiesReachedEnd, 50, 280);
        g.drawString("Towers Built: " + towersBuilt, 50, 320);
        g.drawString("Enemies Defeated: " + enemyDefeated, 50, 360);
        g.drawString("Total Damage Dealt: " + totalDamage, 50, 400);

        int minutes = timePlayed / 60;
        int seconds = timePlayed % 60;
        g.drawString("Time Played: " + minutes + "m " + seconds + "s", 50, 440);

        replayButton.drawStyled(g);
        menuButton.drawStyled(g);
    }

    public void setStats(boolean isVictory, int goldEarned, int enemiesSpawned, int enemiesReachedEnd, int towersBuilt,
                         int enemyDefeated, int totalDamage, int timePlayed) {
        this.isVictory = isVictory;
        this.goldEarned = goldEarned;
        this.enemiesSpawned = enemiesSpawned;
        this.enemiesReachedEnd = enemiesReachedEnd;
        this.towersBuilt = towersBuilt;
        this.enemyDefeated = enemyDefeated;
        this.totalDamage = totalDamage;
        this.timePlayed = timePlayed;
    }



    @Override
    public void mouseClicked(int x, int y) {
        if (replayButton.getBounds().contains(x, y)) {
            playButtonClickSound();
            game.resetGameWithSameLevel();
        } else if (menuButton.getBounds().contains(x, y)) {
            playButtonClickSound();
            game.changeGameState(GameStates.MENU);
        }
    }

    @Override
    public void mouseMoved(int x, int y) {
        replayButton.setMouseOver(replayButton.getBounds().contains(x, y));
        menuButton.setMouseOver(menuButton.getBounds().contains(x, y));
    }

    @Override
    public void mousePressed(int x, int y) {
        replayButton.setMousePressed(replayButton.getBounds().contains(x, y));
        menuButton.setMousePressed(menuButton.getBounds().contains(x, y));
    }

    @Override
    public void mouseReleased(int x, int y) {
        replayButton.resetBooleans();
        menuButton.resetBooleans();
    }


    @Override
    public void mouseDragged(int x, int y) {

    }
}
