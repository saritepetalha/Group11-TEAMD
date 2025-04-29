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

    private final TileManager tileManager;

    private EnemyManager enemyManager;


    public Playing(Game game, TileManager tileManager) {
        super(game);

        this.tileManager = tileManager;

        bottomPlayingBar = new PlayingBar(0, GameDimensions.GAME_HEIGHT, GameDimensions.GAME_WIDTH, 100, this);

        //enemyManager = new EnemyManager(this, overlay, level);

        loadDefaultLevel();
    }

    public void saveLevel(String filename) {
        LoadSave.saveLevel(filename,level);

    }


    private void loadDefaultLevel() {
        int[][] lvl = LoadSave.getLevelData("defaultlevel");
        this.level = lvl;
        //THIS LINE IS JUST TO SEE WHETHER THE BACKEND OF THE getLevelData function works or not
        //IT WORKS!!!
        System.out.println(java.util.Arrays.deepToString(lvl));
    }

    public void loadLevel(String levelName) {
        level = LoadSave.getLevelData(levelName);
    }



    private void drawMap(Graphics g) {

        g.setColor(new Color(134,177,63,255));
        g.fillRect(0, 0, GameDimensions.GAME_WIDTH, GameDimensions.GAME_HEIGHT);

        for (int i = 0; i < level.length; i++) {
            for (int j = 0; j < level[i].length; j++) {
                g.drawImage(tileManager.getSprite(level[i][j]), j * GameDimensions.TILE_DISPLAY_SIZE, i * GameDimensions.TILE_DISPLAY_SIZE, null);
            }
        }

    public void update() {
        enemyManager.update();

    }

    @Override
    public void render(Graphics g) {


        drawMap(g);

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
