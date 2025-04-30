package scenes;

import java.awt.*;
import java.util.List;

import constants.GameDimensions;
import enemies.Enemy;
import helpMethods.LoadSave;
import main.Game;

import managers.*;

import ui_p.DeadTree;

import ui_p.PlayingBar;
import ui_p.PlayingUI;

public class Playing extends GameScene implements SceneMethods {
    private int[][] level;
    private int[][] overlay;

    private PlayingBar bottomPlayingBar;
    private PlayingUI playingUI;
    private int mouseX, mouseY;
    private List<DeadTree> trees;
    private WaveManager waveManager;
    private TowerManager towerManager;
    private TileManager tileManager;
    private PlayerManager playerManager;

    private EnemyManager enemyManager;


    public Playing(Game game, TileManager tileManager) {
        super(game);
        loadDefaultLevel();
        this.tileManager = tileManager;
        this.playerManager = new PlayerManager();

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
        waveManager = new WaveManager(this);

        if(towerManager.findDeadTrees(level) != null) {
            trees = towerManager.findDeadTrees(level);
        }

        bottomPlayingBar = new PlayingBar(0, GameDimensions.GAME_HEIGHT, GameDimensions.GAME_WIDTH, 100, this);

        playingUI = new PlayingUI(this);
        updateUIResources();    // Update the UI with player's starting resources
    }

    private void updateUIResources() {
        playingUI.setGoldAmount(playerManager.getGold());
        playingUI.setHealthAmount(playerManager.getHealth());
        playingUI.setShieldAmount(playerManager.getShield());
    }

    public void saveLevel(String filename) {
        LoadSave.saveLevel(filename,level);

    }

    private void loadDefaultLevel() {
        int[][] lvl = LoadSave.getLevelData("defaultlevel");
        if (lvl == null) {
            lvl = new int[9][16];
            System.out.println("Level not found, creating default level.");
            for (int i = 0; i < lvl.length; i++) {
                for (int j = 0; j < lvl[i].length; j++) {
                    if (i == 4) {
                        lvl[i][j] = 13;
                    } else {
                        lvl[i][j] = 5;
                    }
                }
            }
            LoadSave.saveLevel("defaultlevel", lvl);
        }
        this.level = lvl;
    }

    public void loadLevel(String levelName) {
        int[][] loadedLevel = LoadSave.loadLevel(levelName);
        if (loadedLevel != null) {
            this.level = loadedLevel;
            this.overlay = new int[loadedLevel.length][loadedLevel[0].length];
            for (int i = 0; i < loadedLevel.length; i++) {
                for (int j = 0; j < loadedLevel[i].length; j++) {
                    if (loadedLevel[i][j] == 1) {
                        overlay[i][j] = 1;
                    } else if (loadedLevel[i][j] == 2) {
                        overlay[i][j] = 2;
                    }
                }
            }
            enemyManager = new EnemyManager(this, overlay, level);
        }
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
        waveManager.update();

        if (isAllEnemiesDead()) {
            if (isThereMoreWaves()) {
                if (!waveManager.isWaveTimerOver()) {
                    waveManager.startTimer();
                } else {
                    waveManager.incrementWaveIndex();
                    enemyManager.getEnemies().clear();
                    waveManager.resetEnemyIndex();
                }
            }
        }

        if (isTimeForNewEnemy()){
            spawnEnemy();
        }

        enemyManager.update();
        towerManager.update();
        updateUIResources();
    }

    private boolean isWaveTimerOver() {
        return waveManager.isWaveTimerOver();
    }

    private boolean isThereMoreWaves() {
        return waveManager.isThereMoreWaves();
    }

    private boolean isAllEnemiesDead() {
        if (waveManager.isWaveFinished()) {
            for (Enemy enemy : enemyManager.getEnemies()) {
                if (enemy.isAlive() || !enemy.hasReachedEnd()) {
                    return false;
                }
            }
            return true;
        }
        return false;
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

    public PlayingUI getPlayingUI() {return playingUI;
    }

    @Override
    public void mouseMoved(int x, int y) {
        playingUI.mouseMoved(x, y);
    }

    @Override
    public void mousePressed(int x, int y) {
        playingUI.mousePressed(x, y);
    }

    @Override
    public void mouseReleased(int x, int y) {
        playingUI.mouseReleased();
    }

    @Override
    public void mouseDragged(int x, int y) {}

    // Add helper methods to handle control button actions
    private void handlePauseButton(boolean isPressed) {
        // Toggle game pause state based on button state
        if (isPressed) {
            // Game is now paused
            System.out.println("Game paused");
            // TODO: Implement actual pause functionality
        } else {
            // Game is now unpaused
            System.out.println("Game resumed");
            // TODO: Implement actual resume functionality
        }
    }

    private void handleFastForwardButton(boolean isPressed) {
        // Toggle game speed based on button state
        if (isPressed) {
            // Game is now in fast forward mode
            System.out.println("Game speed increased");
            // TODO: Implement actual speed increase functionality
        } else {
            // Game is now at normal speed
            System.out.println("Game speed normal");
            // TODO: Implement actual speed reset functionality
        }
    }

    private void handleOptionsButton(boolean isPressed) {
        // Show/hide options menu based on button state
        if (isPressed) {
            // Show options menu
            System.out.println("Options menu opened");
            // TODO: Implement actual options menu display
        } else {
            // Hide options menu
            System.out.println("Options menu closed");
            // TODO: Implement actual options menu hiding
        }
    }

    public WaveManager getWaveManager() {
        return waveManager;
    }

    private void spawnEnemy() {
        enemyManager.spawnEnemy(waveManager.getNextEnemy());
    }

    private boolean isTimeForNewEnemy() {
        if(waveManager.isTimeForNewEnemy()){
            return !waveManager.isWaveFinished();
        }
        return false;
    }
}
