package scenes;

import java.awt.*;
import java.awt.image.BufferedImage;

import constants.GameDimensions;
import helpMethods.LoadSave;
import main.Game;
import managers.EnemyManager;
import ui_p.PlayingBar;

public class Playing extends GameScene implements SceneMethods {
    private int[][] level;
    private int[][] overlay;
    private PlayingBar bottomPlayingBar;
    private int mouseX, mouseY;
    private EnemyManager enemyManager;

    public Playing(Game game) {
        super(game);
        bottomPlayingBar = new PlayingBar(0, GameDimensions.GAME_HEIGHT, GameDimensions.GAME_WIDTH, 100, this);

        //enemyManager = new EnemyManager(this, overlay, level);
        loadDefaultLevel();
    }

    public void saveLevel() {
        LoadSave.saveLevel("defaultleveltest1",level);
    }


    private void loadDefaultLevel() {
        int[][] lvl = LoadSave.getLevelData("defaultleveltest1");
        //THIS LINE IS JUST TO SEE WHETHER THE BACKEND OF THE getLevelData function works or not
        //IT WORKS!!!
        System.out.println(java.util.Arrays.deepToString(lvl));
    }

    public void loadLevel(String levelName) {
        level = LoadSave.getLevelData(levelName);
    }


    public void update() {
        enemyManager.update();
    }

    @Override
    public void render(Graphics g) {

        for (int y = 0; y < level.length; y++) {
            for (int x = 0; x < level[y].length; x++) {
                int id = level[y][x];
                g.drawImage(getSprite(id), x * 64, y * 64, null);
            }
        }

        g.setColor(new Color(134,177,63,255));
        g.fillRect(0, 0, GameDimensions.GAME_WIDTH, GameDimensions.GAME_HEIGHT);
        enemyManager.draw(g);

    }

    private BufferedImage getSprite(int spriteID) {
        return game.getTileManager().getSprite(spriteID);
    }

    @Override
    public void mouseClicked(int x, int y) {}


    @Override
    public void mouseMoved(int x, int y) {}

    @Override
    public void mousePressed(int x, int y) {}

    @Override
    public void mouseReleased(int x, int y) {}

    @Override
    public void mouseDragged(int x, int y) {

    }
}
