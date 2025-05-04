package scenes;

import java.awt.*;
import java.awt.event.MouseWheelEvent;
import java.util.List;
import java.util.ArrayList;

import constants.GameDimensions;
import enemies.Enemy;
import helpMethods.LoadSave;
import main.Game;

import managers.*;

import managers.WaveManager;
import objects.Tower;

import ui_p.DeadTree;

import ui_p.PlayingUI;

import managers.AudioManager;

public class Playing extends GameScene implements SceneMethods {
    private int[][] level;
    private int[][] overlay;

    private PlayingUI playingUI;
    private int mouseX, mouseY;
    private List<DeadTree> trees;
    private WaveManager waveManager;
    private TowerManager towerManager;
    private TileManager tileManager;
    private PlayerManager playerManager;
    private ProjectileManager projectileManager;

    private EnemyManager enemyManager;

    private DeadTree selectedDeadTree;
    private Tower displayedTower;

    private boolean gamePaused = false;
    private boolean gameSpeedIncreased = false;
    private boolean optionsMenuOpen = false;
    private float gameSpeedMultiplier = 1.0f;

    public Playing(Game game, TileManager tileManager) {
        super(game);
        this.tileManager = tileManager;
        loadDefaultLevel();

        projectileManager = new ProjectileManager(this);

        // Use the same constructor that was working before
        waveManager = new WaveManager(this);

        // Set up the overlay for pathfinding
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
        towerManager = new TowerManager(this);
        playerManager = new PlayerManager();

        this.selectedDeadTree = null;

        if(towerManager.findDeadTrees(level) != null) {
            trees = towerManager.findDeadTrees(level);
        } else {
            trees = new ArrayList<>();
        }

        // Initialize UI
        playingUI = new PlayingUI(this);
        updateUIResources();

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

    }

    public void update() {
        if (!gamePaused) {
            waveManager.update();
            projectileManager.update();

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
                } else {
                    // No more waves and all enemies dead = victory!
                    handleVictory();
                }
            }

            if (isTimeForNewEnemy()) {
                System.out.println("Spawning new enemy");
                spawnEnemy();
            }

            enemyManager.update(gameSpeedMultiplier);
            towerManager.update(gameSpeedMultiplier);
            updateUIResources();

            // check if game over
            if (!playerManager.isAlive()) {
                handleGameOver();
            }
        }
        // Check button states from PlayingUI even when paused
        checkButtonStates();
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
        enemyManager.draw(g, gamePaused);         // pass the paused state to enemyManager.draw
        drawTowerButtons(g);
        projectileManager.draw(g);
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
                    playButtonClickSound();
                    towerManager.buildArcherTower(tree.getX(), tree.getY());
                    tree.setShowChoices(false);
                    trees.remove(tree);
                    modifyTile(tileX, tileY, "ARCHER");
                    setSelectedDeadTree(null);
                    return;
                }
                if (tree.getMageButton().isMousePressed(mouseX, mouseY)) {
                    playButtonClickSound();
                    towerManager.buildMageTower(tree.getX(), tree.getY());
                    tree.setShowChoices(false);
                    trees.remove(tree);
                    modifyTile(tileX, tileY, "MAGE");
                    setSelectedDeadTree(null);
                    return;
                }
                if (tree.getArtilleryButton().isMousePressed(mouseX, mouseY)) {
                    playButtonClickSound();
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
                playButtonClickSound();
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
                playButtonClickSound();
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
    public void mouseDragged(int x, int y) {
        playingUI.mouseDragged(x, y);
    }

    private void checkButtonStates() {
        // Check the states of control buttons
        if (playingUI.getPauseButton().isMousePressed()) {
            handlePauseButton(true);
        } else {
            handlePauseButton(false);
        }

        if (playingUI.getFastForwardButton().isMousePressed()) {
            handleFastForwardButton(true);
        } else {
            handleFastForwardButton(false);
        }

        if (playingUI.getOptionsButton().isMousePressed()) {
            handleOptionsButton(true);
        } else {
            handleOptionsButton(false);
        }
    }

    // Add helper methods to handle control button actions
    private void handlePauseButton(boolean isPressed) {
        // Toggle game pause state based on button state
        if (isPressed) {
            // Game is now paused
            gamePaused = true;
        } else {
            // Game is now unpaused
            gamePaused = false;
        }
    }

    private void handleFastForwardButton(boolean isPressed) {
        // Toggle game speed based on button state
        if (isPressed && !gameSpeedIncreased) {
            // Game is now in fast forward mode
            gameSpeedIncreased = true;
            gameSpeedMultiplier = 2.0f; // Double game speed
            System.out.println("Game speed increased");
        } else if (!isPressed && gameSpeedIncreased) {
            // Game is now at normal speed
            gameSpeedIncreased = false;
            gameSpeedMultiplier = 1.0f; // Normal game speed
            System.out.println("Game speed normal");
        }
    }

    private void handleOptionsButton(boolean isPressed) {
        // Show/hide options menu based on button state
        if (isPressed && !optionsMenuOpen) {
            // Show options menu
            optionsMenuOpen = true;
            System.out.println("Options menu opened");
        } else if (!isPressed && optionsMenuOpen) {
            // Hide options menu
            optionsMenuOpen = false;
            System.out.println("Options menu closed");
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

    public void enemyReachedEnd(Enemy enemy) {
        System.out.println("Enemy reached end: " + enemy.getId());

        // each enemy that reaches the end causes 1 damage
        playerManager.takeDamage(1);

        // update UI to show new shield/health values
        updateUIResources();
    }

    private void handleGameOver() {
        System.out.println("Game Over!");

        // Play a random lose sound
        AudioManager.getInstance().playRandomLoseSound();

        // stop any ongoing waves/spawning
        waveManager.resetWaveIndex();
        enemyManager.getEnemies().clear();

        // for now, we'll just wait 2 seconds and return to the menu, we will implement a proper game over screen soon

        // use a separate thread to avoid blocking the game loop
        new Thread(() -> {
            try {
                Thread.sleep(3000);
                game.changeGameState(main.GameStates.MENU);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    // Add a method to handle victory
    private void handleVictory() {
        System.out.println("Victory!");

        // Play a random victory sound
        AudioManager.getInstance().playRandomVictorySound();

        // use a separate thread to avoid blocking the game loop
        new Thread(() -> {
            try {
                Thread.sleep(5000);
                game.changeGameState(main.GameStates.MENU);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void shootEnemy(Tower tower, Enemy enemy) {
        projectileManager.newProjectile(tower, enemy);
    }

    public boolean isGamePaused() {
        return gamePaused;
    }

    public boolean isOptionsMenuOpen() {
        return optionsMenuOpen;
    }

    public void returnToMainMenu() {
        game.changeGameState(main.GameStates.MENU);
    }

    @Override
    public void playButtonClickSound() {
        AudioManager.getInstance().playButtonClickSound();
    }

    /**
     * Handles mouse wheel events and forwards them to PlayingUI
     * @param e The mouse wheel event
     */
    public void mouseWheelMoved(MouseWheelEvent e) {
        playingUI.mouseWheelMoved(e);
    }
}
