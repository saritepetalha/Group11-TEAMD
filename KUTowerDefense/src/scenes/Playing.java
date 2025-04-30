package scenes;

import java.awt.*;
import java.util.List;

import constants.GameDimensions;
import helpMethods.LoadSave;
import main.Game;

import managers.TileManager;
import managers.TowerManager;

import ui_p.DeadTree;

import managers.EnemyManager;
import ui_p.PlayingBar;

public class Playing extends GameScene implements SceneMethods {
    private int[][] level;
    private int[][] overlay;
    private PlayingBar bottomPlayingBar;
    private int mouseX, mouseY;
    private List<DeadTree> trees;

    private TowerManager towerManager;
    private TileManager tileManager;

    private EnemyManager enemyManager;


    public Playing(Game game, TileManager tileManager) {
        super(game);
        loadDefaultLevel();
        this.tileManager = tileManager;

        towerManager = new TowerManager(this);

        //OVERLAY IS HARDCODED BECAUSE IT IS NOT LOADED WITH LOAD DEFAULT LEVEL METHOD YET
        //IT HAS TO BE LOADED FIRST TO HAVE ENEMY MANAGER. FOR JUST NOW IT IS HARDCODED
        //JSON FILE WILL HAVE INFORMATION ON THAT

        this.overlay = new int[][]{
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2}, // ← start and end points
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}
        };

        enemyManager = new EnemyManager(this, overlay, level);

        if(towerManager.findDeadTrees(level) != null) {
            trees = towerManager.findDeadTrees(level);
        }
        
        bottomPlayingBar = new PlayingBar(0, GameDimensions.GAME_HEIGHT, GameDimensions.GAME_WIDTH, 100, this);

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

    public void drawTowerButtons(Graphics g) {
        for (DeadTree deadTree : trees) {
            deadTree.draw(g);
        }
    }

    private void drawMap(Graphics g) {
        g.setColor(new Color(134, 177, 63, 255));
        g.fillRect(0, 0, GameDimensions.GAME_WIDTH, GameDimensions.GAME_HEIGHT);

        for (int i = 0; i < level.length; i++) {
            for (int j = 0; j < level[i].length; j++) {
                g.drawImage(tileManager.getSprite(level[i][j]), j * GameDimensions.TILE_DISPLAY_SIZE, i * GameDimensions.TILE_DISPLAY_SIZE, null);
            }
        }
        enemyManager.draw(g);

    }

    public void update() {
        enemyManager.update();
        towerManager.update();
    }

    @Override
    public void render(Graphics g) {
        drawMap(g);
        towerManager.draw(g);
        drawTowerButtons(g);

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
