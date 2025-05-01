package scenes;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.List;

import constants.GameDimensions;
import enemies.Enemy;
import helpMethods.LoadSave;
import main.Game;

import managers.*;

import managers.WaveManager;
import objects.Tower;

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

    private DeadTree selectedDeadTree;
    private Tower displayedTower;


    public Playing(Game game, TileManager tileManager) {
        super(game);
        loadDefaultLevel();
        this.tileManager = tileManager;
        this.selectedDeadTree = null;
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
            System.out.println("Loading level: " + levelName);
            this.level = loadedLevel;

            this.overlay = new int[loadedLevel.length][loadedLevel[0].length];
            System.out.println("Overlay size: " + overlay.length + "x" + overlay[0].length);

            boolean foundStart = false;
            boolean foundEnd = false;

            for (int i = 0; i < loadedLevel.length; i++) {
                for (int j = 0; j < loadedLevel[i].length; j++) {
                    overlay[i][j] = 0;

                    if (loadedLevel[i][j] == 1) {
                        overlay[i][j] = 1;
                        foundStart = true;
                        System.out.println("Start point found at: " + i + "," + j);
                    } else if (loadedLevel[i][j] == 2) {
                        overlay[i][j] = 2;
                        foundEnd = true;
                        System.out.println("End point found at: " + i + "," + j);
                    }
                }
            }

            if (!foundStart || !foundEnd) {
                System.out.println("Warning: Start or end point not found in level!");
            }

            System.out.println("Updating EnemyManager with new level and overlay");
            enemyManager = new EnemyManager(this, overlay, level);

            System.out.println("Resetting WaveManager");
            waveManager = new WaveManager(this);
            startEnemySpawning();
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
            System.out.println("All enemies are dead");
            if (isThereMoreWaves()) {
                System.out.println("There are more waves");
                if (!waveManager.isWaveTimerOver()) {
                    System.out.println("Starting wave timer");
                    waveManager.startTimer();
                } else {
                    System.out.println("Wave timer over, incrementing wave index");
                    waveManager.incrementWaveIndex();
                    enemyManager.getEnemies().clear();
                    waveManager.resetEnemyIndex();
                }
            }
        }

        if (isTimeForNewEnemy()) {
            System.out.println("Spawning new enemy");
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
        drawHighlight(g);
        drawDisplayedTower(g);
        playingUI.draw(g);
    }

    private void drawHighlight(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();

        // Set transparency level (0.0f = fully transparent, 1.0f = fully opaque)
        float alpha = 0.2f; // Adjust the transparency as needed
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        // Set fill color to white
        g2d.setColor(Color.WHITE);
        g2d.fillRect(mouseX, mouseY, 64, 64);

        // Optional: draw white border with full opacity
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
        g2d.setColor(Color.WHITE);
        g2d.drawRect(mouseX, mouseY, 64, 64);

        g2d.dispose();
    }

    private void drawDisplayedTower(Graphics g) {
        if (displayedTower == null) return;
        drawDisplayedTowerBorder(g);
        drawDisplayedTowerRange(g);
    }

    private void drawDisplayedTowerRange(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();

        int range = (int) displayedTower.getRange();
        int centerX = displayedTower.getX() + 32;
        int centerY = displayedTower.getY() + 32;
        int topLeftX = centerX - range;
        int topLeftY = centerY - range;

        // Brown fill (solid)
        Color brownFill = new Color(139, 69, 19, 60); // SaddleBrown
        g2d.setColor(brownFill);
        g2d.fillOval(topLeftX, topLeftY, range * 2, range * 2);

        float[] dashPattern = {10f, 5f}; // 10px dash, 5px gap

        // Yellow outline (semi-transparent)
        Color yellowOutline = new Color(255, 255, 0); // Yellow with 50% opacity
        g2d.setColor(yellowOutline);
        g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, dashPattern, 0));
        g2d.drawOval(topLeftX, topLeftY, range * 2, range * 2);

        g2d.dispose();

    }


    private void drawDisplayedTowerBorder(Graphics g) {
        g.setColor(Color.CYAN);
        g.drawRect(displayedTower.getX(), displayedTower.getY(), 64, 64);
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
        displayedTower = null;

        boolean clickedOnTree = false;
        for(DeadTree tree: trees) {
            if (tree.isShowChoices()) {
                int tileX = tree.getX();
                int tileY = tree.getY();
                if (tree.getArcherButton().isMousePressed(mouseX, mouseY)) {
                    towerManager.buildArcherTower(tree.getX(), tree.getY());
                    tree.setShowChoices(false);
                    trees.remove(tree);
                    modifyTile(tileX, tileY, "ARCHER");
                    setSelectedDeadTree(null);
                    return;
                }
                if (tree.getMageButton().isMousePressed(mouseX, mouseY)) {
                    towerManager.buildMageTower(tree.getX(), tree.getY());
                    tree.setShowChoices(false);
                    trees.remove(tree);
                    modifyTile(tileX, tileY, "MAGE");
                    setSelectedDeadTree(null);
                    return;
                }
                if (tree.getArtilleryButton().isMousePressed(mouseX, mouseY)) {
                    towerManager.buildArtilerryTower(tree.getX(), tree.getY());
                    tree.setShowChoices(false);
                    trees.remove(tree);
                    modifyTile(tileX, tileY, "ARTILERRY");
                    setSelectedDeadTree(null);
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
                setSelectedDeadTree(tree);
                displayedTower = null;
                return;
            }
        }

        for (Tower tower: towerManager.getTowers()) {
            if (tower.isClicked(mouseX, mouseY)) {
                displayedTower = tower;
                return;
            }
        }

    }

    public TowerManager getTowerManager() {
        return towerManager;
    }

    public EnemyManager getEnemyManager() {
        return enemyManager;
    }

    public PlayingUI getPlayingUI() {
        return playingUI;
    }

    @Override
    public void mouseMoved(int x, int y) {
        mouseX = (x / 64) * 64;
        mouseY = (y / 64) * 64;

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

    public DeadTree getSelectedDeadTree() {return selectedDeadTree;}

    public void setSelectedDeadTree(DeadTree deadTree) {this.selectedDeadTree = deadTree;}

    public Tower getDisplayedTower() {return displayedTower;}

    private void spawnEnemy() {
        enemyManager.spawnEnemy(waveManager.getNextEnemy());
    }

    private boolean isTimeForNewEnemy() {
        if(waveManager.isTimeForNewEnemy()){
            return !waveManager.isWaveFinished();
        }
        return false;
    }

    public void setDisplayedTower(Tower tower) {displayedTower = tower;}

    public void startEnemySpawning() {
        waveManager.resetWaveIndex();
        waveManager.resetEnemyIndex();
        waveManager.startTimer();
    }
}
