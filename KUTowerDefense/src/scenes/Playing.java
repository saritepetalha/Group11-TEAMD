package scenes;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import constants.GameDimensions;
import helpMethods.LoadSave;
import main.Game;

import managers.TileManager;
import managers.TowerManager;
import objects.Tower;
import ui_p.DeadTree;
import ui_p.EditTiles;
import ui_p.TheButton;

import static main.GameStates.*;
import objects.Tile;
import managers.EnemyManager;
import ui_p.PlayingBar;

public class Playing extends GameScene implements SceneMethods {


public class Playing extends GameScene implements SceneMethods {
    private int[][] level;
    private int[][] overlay;
    private PlayingBar bottomPlayingBar;
    private int mouseX, mouseY;
    private List<DeadTree> trees;

    private TowerManager towerManager;
    private TileManager tileManager;

    private final TileManager tileManager;

    private EnemyManager enemyManager;


    public Playing(Game game, TileManager tileManager) {
        super(game);
        loadDefaultLevel();
        this.tileManager = tileManager;

        towerManager = new TowerManager(this);
        tileManager = new TileManager();
        if(towerManager.findDeadTrees(level) != null) {
            trees = towerManager.findDeadTrees(level);
        }
        
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

    public void update() {
        towerManager.update();
    }
    public void drawTowerButtons(Graphics g) {
        for (DeadTree deadTree : trees) {
            deadTree.draw(g);
        }
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
        towerManager.draw(g);
        drawTowerButtons(g);

    }

    private void drawMap(Graphics g) {
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

        for (int i = 0; i < level.length; i++) {
            for (int j = 0; j < level[i].length; j++) {
                g.drawImage(tileManager.getSprite(level[i][j]), j * GameDimensions.TILE_DISPLAY_SIZE, i * GameDimensions.TILE_DISPLAY_SIZE, null);
            }
        }
    }

    private void modifyTile(int x, int y, String tile) {

        x /= 64;
        y /= 64;

        if (tile.equals("ARCHER")) {
            level[y][x] = 26;
        }
        else if (tile.equals("MAGE")) {
            level[y][x] = 20;
        }
        if (tile.equals("ARTILERRY")) {
            level[y][x] = 21;
        }

    }

    @Override
    public void mouseClicked(int x, int y) {

        this.mouseX = x;
        this.mouseY = y;

        boolean clickedOnTree = false;
        for(DeadTree tree: trees){
            if (tree.isShowChoices()){
                int tileX = tree.getX();
                int tileY = tree.getY();
                if (tree.getArcherButton().isMousePressed(mouseX, mouseY)) {
                    towerManager.buildArcherTower(tree.getX(), tree.getY());
                    tree.setShowChoices(false);
                    trees.remove(tree);
                    modifyTile(tileX, tileY, "ARCHER");
                    return;
                }
                if (tree.getMageButton().isMousePressed(mouseX, mouseY)) {
                    towerManager.buildMageTower(tree.getX(), tree.getY());
                    tree.setShowChoices(false);
                    trees.remove(tree);
                    modifyTile(tileX, tileY, "MAGE");
                    return;
                }
                if (tree.getArtilleryButton().isMousePressed(mouseX, mouseY)) {
                    towerManager.buildArtilerryTower(tree.getX(), tree.getY());
                    tree.setShowChoices(false);
                    trees.remove(tree);
                    modifyTile(tileX, tileY, "ARTILERRY");
                    return;
                }
            }
        }

        for (DeadTree tree : trees) {
            if (tree.isClicked(mouseX, mouseY)) {
                for (DeadTree other : trees) {
                    other.setShowChoices(false);
                }
                tree.setShowChoices(true);
                return;
            }
        }


    }

    public TowerManager getTowerManager() {
        return towerManager;
    }

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
