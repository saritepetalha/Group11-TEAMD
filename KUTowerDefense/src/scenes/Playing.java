package scenes;

import java.awt.*;

import java.awt.event.MouseWheelEvent;

import java.util.List;
import java.util.ArrayList;
import main.Game;

import constants.GameDimensions;
import enemies.Enemy;

import helpMethods.LoadSave;
import helpMethods.OptionsIO;
import config.GameOptions;

import managers.*;
import managers.WaveManager;
import managers.AudioManager;

import objects.Tower;

import ui_p.DeadTree;
import ui_p.PlayingUI;
import ui_p.LiveTree;

public class Playing extends GameScene implements SceneMethods {
    private int[][] level;
    private int[][] overlay;

    private PlayingUI playingUI;
    private int mouseX, mouseY;
    private List<DeadTree> deadTrees;
    private List<LiveTree> liveTrees;
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

    private TreeInteractionManager treeInteractionManager;
    private FireAnimationManager fireAnimationManager;

    private boolean gameOverHandled = false;
    private boolean victoryHandled = false;

    private GameOptions gameOptions;

    public Playing(Game game, TileManager tileManager) {
        super(game);
        this.tileManager = tileManager;
        this.gameOptions = OptionsIO.load();
        loadDefaultLevel();
        initializeManagers();
    }
    public Playing(Game game, TileManager tileManager, int[][] customLevel, int[][] customOverlay) {
        super(game);
        this.tileManager = tileManager;
        this.level = customLevel;
        this.overlay = customOverlay;
        initializeManagers();
    }

    private void initializeManagers() {
        projectileManager = new ProjectileManager(this);
        treeInteractionManager = new TreeInteractionManager(this);
        fireAnimationManager = new FireAnimationManager();
        waveManager = new WaveManager(this);
        enemyManager = new EnemyManager(this, overlay, level);
        towerManager = new TowerManager(this);
        playerManager = new PlayerManager();
        this.selectedDeadTree = null;

        if(towerManager.findDeadTrees(level) != null)
            deadTrees = towerManager.findDeadTrees(level);
        if(towerManager.findLiveTrees(level) != null)
            liveTrees = towerManager.findLiveTrees(level);

        playingUI = new PlayingUI(this);
        updateUIResources();
    }


    public void updateUIResources() {
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
        this.overlay = new int[lvl.length][lvl[0].length];
        overlay[4][0] = 1;
        overlay[4][15] = 2;
    }

    public void loadLevel(String levelName) {
        int[][] loadedLevel = LoadSave.loadLevel(levelName);
        if (loadedLevel != null) {
            level = loadedLevel;
            overlay = new int[loadedLevel.length][loadedLevel[0].length];
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
                return;
            }

            if (enemyManager != null) {
                enemyManager.getEnemies().clear();
            }
            towerManager = new TowerManager(this);
            deadTrees = towerManager.findDeadTrees(level);
            liveTrees = towerManager.findLiveTrees(level);

            enemyManager = new EnemyManager(this, overlay, level);
            waveManager = new WaveManager(this);

            resetGameState();
            startEnemySpawning();

            updateUIResources();
        }
    }

    /**
     * Reloads game options from file and applies them to all managers
     */
    public void reloadGameOptions() {
        try {
            System.out.println("Reloading game options...");
            this.gameOptions = OptionsIO.load();

            if (gameOptions == null) {
                System.out.println("Warning: Failed to load game options, using default values");
                return;
            }

            // Apply options to all managers
            if (waveManager != null) {
                waveManager.reloadFromOptions();
            } else {
                System.out.println("Warning: WaveManager is null");
            }

            if (enemyManager != null) {
                enemyManager.reloadFromOptions();
            } else {
                System.out.println("Warning: EnemyManager is null");
            }

            if (playerManager != null) {
                playerManager.reloadFromOptions();
            } else {
                System.out.println("Warning: PlayerManager is null");
            }

            System.out.println("Game options reloaded successfully");
        } catch (Exception e) {
            System.out.println("Error reloading game options: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void drawTowerButtons(Graphics g) {
        if (deadTrees != null) {
            for (DeadTree deadTree : deadTrees) {
                deadTree.draw(g);
            }
        }
    }

    public void drawLiveTreeButtons(Graphics g) {
        if (liveTrees != null) {
            for (LiveTree live : liveTrees) {
                live.draw(g);
            }
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
            fireAnimationManager.update();

            if (isAllEnemiesDead()) {
                if (isThereMoreWaves()) {
                    if (!waveManager.isWaveTimerStarted()) {
                        waveManager.startTimer();
                    } else if (waveManager.isWaveTimerOver()) {
                        waveManager.incrementWaveIndex();
                        enemyManager.getEnemies().clear();
                        waveManager.resetEnemyIndex();
                    }
                } else {
                    handleVictory();
                }
            }

            if (isTimeForNewEnemy()) {
                int nextEnemy = waveManager.getNextEnemy();
                if (nextEnemy != -1) {
                    spawnEnemy();
                }
            }

            enemyManager.update(gameSpeedMultiplier);
            towerManager.update(gameSpeedMultiplier);
            updateUIResources();

            if (!playerManager.isAlive()) {
                handleGameOver();
            }
        }
        checkButtonStates();
    }

    private boolean isWaveTimerOver() {
        return waveManager.isWaveTimerOver();
    }

    private boolean isThereMoreWaves() {
        return waveManager.isThereMoreWaves();
    }

    private boolean isAllEnemiesDead() {
        boolean waveFinished = waveManager.isWaveFinished();

        if (waveFinished) {
            if (enemyManager.getEnemies().isEmpty()) {
                return true;
            }

            for (Enemy enemy : enemyManager.getEnemies()) {
                if (enemy.isAlive() && !enemy.hasReachedEnd()) {
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
        drawLiveTreeButtons(g);
        projectileManager.draw(g);
        drawHighlight(g);
        drawDisplayedTower(g);
        fireAnimationManager.draw(g);
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

    public void modifyTile(int x, int y, String tile) {

        x /= 64;
        y /= 64;

        if (tile.equals("ARCHER")) {
            level[y][x] = 26;
        }
        else if (tile.equals("MAGE")) {
            level[y][x] = 20;
        }
        else if (tile.equals("ARTILERRY")) {
            level[y][x] = 21;
        }
        else if (tile.equals("DEADTREE")) {
            level[y][x] = 15;
        }

    }


    @Override
    public void mouseClicked(int x, int y) {
        this.mouseX = x;
        this.mouseY = y;
        displayedTower = null;

        if (deadTrees != null) {
            treeInteractionManager.handleDeadTreeInteraction(mouseX, mouseY);
        }
        if (liveTrees != null) {
            treeInteractionManager.handleLiveTreeInteraction(mouseX, mouseY);
        }

        handleTowerClick();
    }

    private void handleTowerClick() {
        for (Tower tower : towerManager.getTowers()) {
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
        System.out.println("Spawning enemy...");
    }

    private boolean isTimeForNewEnemy() {
        boolean timeElapsed = waveManager.isTimeForNewEnemy();
        boolean waveHasEnemiesLeft = !waveManager.isWaveFinished();

        return timeElapsed && waveHasEnemiesLeft;
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
        // Prevent multiple calls to handleGameOver
        if (gameOverHandled) return;

        System.out.println("Game Over!");
        gameOverHandled = true;

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

    // add a method to handle victory
    private void handleVictory() {
        // Prevent multiple calls to handleVictory
        if (victoryHandled) return;

        System.out.println("Victory!");
        victoryHandled = true;

        // play a random victory sound
        AudioManager.getInstance().playRandomVictorySound();

        // Return to the menu after a short delay
        new Thread(() -> {
            try {
                Thread.sleep(5000);
                game.changeGameState(main.GameStates.MENU);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
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
        System.out.println("Returning to main menu");
        game.changeGameState(main.GameStates.MENU);

    }

    public FireAnimationManager getFireAnimationManager() {
        return fireAnimationManager;
    }

    public List<DeadTree> getDeadTrees() {
        return deadTrees;
    }

    public List<LiveTree> getLiveTrees() {
        return liveTrees;
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    public void resetGameState() {
        gameOverHandled = false;
        victoryHandled = false;
        gamePaused = false;
        gameSpeedIncreased = false;
        optionsMenuOpen = false;
        gameSpeedMultiplier = 1.0f;
        enemyManager.getEnemies().clear();
        displayedTower = null;
        selectedDeadTree = null;
        startEnemySpawning();
        playerManager = new PlayerManager();
        updateUIResources();
    }
}
