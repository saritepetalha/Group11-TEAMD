package scenes;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import constants.GameDimensions;
import enemies.Enemy;
import helpMethods.LoadSave;
import main.Game;

import managers.*;

import managers.WaveManager;
import objects.Tower;

import ui_p.DeadTree;

import ui_p.FireAnimation;
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

    private final TreeInteractionManager treeInteractionManager;
    private final FireAnimationManager fireAnimationManager;


    public Playing(Game game, TileManager tileManager) {
        super(game);
        loadDefaultLevel();
        this.tileManager = tileManager;
        this.selectedDeadTree = null;
        this.playerManager = new PlayerManager();
      
        towerManager = new TowerManager(this);
        projectileManager = new ProjectileManager(this);
        treeInteractionManager = new TreeInteractionManager(this);
        fireAnimationManager = new FireAnimationManager();

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
            deadTrees = towerManager.findDeadTrees(level);
        }
        if(towerManager.findLiveTrees(level) != null) {
            liveTrees = towerManager.findLiveTrees(level);
        }


        playingUI = new PlayingUI(this);
        updateUIResources();    // Update the UI with player's starting resources
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
        for (DeadTree deadTree : deadTrees) {
            deadTree.draw(g);
        }
    }
    public void drawLiveTreeButtons(Graphics g) {
        for (LiveTree live : liveTrees) {
            live.draw(g);
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
        drawLiveTreeButtons(g);
        projectileManager.draw(g);
        drawHighlight(g);
        drawDisplayedTower(g);
        fireAnimationManager.draw(g);
        playingUI.draw(g);

        if (optionsMenuOpen) {
            drawOptionsMenu(g);
        }
        if (gamePaused) {
            drawPauseOverlay(g);
        }
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

    // ... (previous unchanged code remains here)

    @Override
    public void mouseClicked(int x, int y) {
        this.mouseX = x;
        this.mouseY = y;
        displayedTower = null;

        treeInteractionManager.handleDeadTreeInteraction(mouseX, mouseY);
        treeInteractionManager.handleLiveTreeInteraction(mouseX, mouseY);

        handleTowerClick();
    }

    private void handleDeadTreeButtonClicks() {
        for (DeadTree tree : deadTrees) {
            if (tree.isShowChoices()) {
                int tileX = tree.getX();
                int tileY = tree.getY();

                if (tree.getArcherButton().isMousePressed(mouseX, mouseY)) {
                    towerManager.buildArcherTower(tileX, tileY);
                    closeAllTreeChoices();
                    deadTrees.remove(tree);
                    modifyTile(tileX, tileY, "ARCHER");
                    return;
                }
                if (tree.getMageButton().isMousePressed(mouseX, mouseY)) {
                    towerManager.buildMageTower(tileX, tileY);
                    closeAllTreeChoices();
                    deadTrees.remove(tree);
                    modifyTile(tileX, tileY, "MAGE");
                    return;
                }
                if (tree.getArtilleryButton().isMousePressed(mouseX, mouseY)) {
                    towerManager.buildArtilerryTower(tileX, tileY);
                    closeAllTreeChoices();
                    deadTrees.remove(tree);
                    modifyTile(tileX, tileY, "ARTILERRY");
                    return;
                }
            }
        }
    }

    private void handleDeadTreeSelection() {
        for (DeadTree tree : deadTrees) {
            if (tree.isClicked(mouseX, mouseY)) {
                closeAllTreeChoices();
                tree.setShowChoices(true);
                setSelectedDeadTree(tree);
                return;
            }
        }
    }

    private void handleLiveTreeButtonClicks() {
        for (LiveTree tree : liveTrees) {
            if (tree.isShowChoices()) {
                int tileX = tree.getX();
                int tileY = tree.getY();

                if (tree.getFireButton().isMousePressed(mouseX, mouseY)) {
                    liveTrees.remove(tree);
                    deadTrees.add(new DeadTree(tileX, tileY));
                    closeAllTreeChoices();
                    modifyTile(tileX, tileY, "DEADTREE");
                    return;
                }
            }
        }
    }

    private void handleLiveTreeSelection() {
        for (LiveTree tree : liveTrees) {
            if (tree.isClicked(mouseX, mouseY)) {
                closeAllTreeChoices();
                tree.setShowChoices(true);
                return;
            }
        }
    }

    private void handleTowerClick() {
        for (Tower tower : towerManager.getTowers()) {
            if (tower.isClicked(mouseX, mouseY)) {
                displayedTower = tower;
                return;
            }
        }
    }

    private void closeAllTreeChoices() {
        for (DeadTree tree : deadTrees) {
            tree.setShowChoices(false);
        }
        for (LiveTree tree : liveTrees) {
            tree.setShowChoices(false);
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

        // stop any ongoing waves/spawning
        waveManager.resetWaveIndex();
        enemyManager.getEnemies().clear();

        // for now, we'll just wait 2 seconds and return to the menu, we will implement a proper game over screen soon

        // use a separate thread to avoid blocking the game loop
        new Thread(() -> {
            try {
                Thread.sleep(2000);
                game.changeGameState(main.GameStates.MENU);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void drawPauseOverlay(Graphics g) {
        // Create a semi-transparent overlay
        Graphics2D g2d = (Graphics2D) g;

        // Semi-transparent black overlay
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRect(0, 0, GameDimensions.GAME_WIDTH, GameDimensions.GAME_HEIGHT);

        // Draw "PAUSED" text
        g2d.setFont(new Font("Arial", Font.BOLD, 48));
        String pauseText = "PAUSED";

        // Calculate text position to center it
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(pauseText);
        int textX = (GameDimensions.GAME_WIDTH - textWidth) / 2;
        int textY = GameDimensions.GAME_HEIGHT / 2;

        // Draw text shadow
        g2d.setColor(new Color(0, 0, 0, 200));
        g2d.drawString(pauseText, textX + 3, textY + 3);

        // Draw text
        g2d.setColor(Color.WHITE);
        g2d.drawString(pauseText, textX, textY);

        // Draw hint text
        g2d.setFont(new Font("Arial", Font.PLAIN, 20));
        String hintText = "Click pause button again to resume";

        fm = g2d.getFontMetrics();
        textWidth = fm.stringWidth(hintText);
        textX = (GameDimensions.GAME_WIDTH - textWidth) / 2;
        textY = GameDimensions.GAME_HEIGHT / 2 + 50;

        g2d.setColor(new Color(200, 200, 200));
        g2d.drawString(hintText, textX, textY);
    }

    private void drawOptionsMenu(Graphics g) {
        // create a semi-transparent panel for the options menu
        Graphics2D g2d = (Graphics2D) g;

        int menuWidth = 300;
        int menuHeight = 280;
        int menuX = (GameDimensions.GAME_WIDTH - menuWidth) / 2;
        int menuY = (GameDimensions.GAME_HEIGHT - menuHeight) / 2;

        // draw menu background
        g2d.setColor(new Color(50, 50, 50, 220));
        g2d.fillRoundRect(menuX, menuY, menuWidth, menuHeight, 20, 20);

        // draw border
        g2d.setColor(new Color(200, 200, 200));
        g2d.setStroke(new BasicStroke(2.0f));
        g2d.drawRoundRect(menuX, menuY, menuWidth, menuHeight, 20, 20);

        // draw title
        g2d.setFont(new Font("Arial", Font.BOLD, 28));
        g2d.setColor(Color.WHITE);
        String title = "OPTIONS";
        FontMetrics fm = g2d.getFontMetrics();
        int titleWidth = fm.stringWidth(title);
        g2d.drawString(title, menuX + (menuWidth - titleWidth) / 2, menuY + 40);

        // draw options
        g2d.setFont(new Font("Arial", Font.PLAIN, 20));

        // option items
        String[] options = {
                "Sound: ON",
                "Music: ON",
                "Difficulty: Normal",
                "Return to Main Menu"
        };

        int optionY = menuY + 90;
        int spacing = 40;

        for (int i = 0; i < options.length; i++) {
            // draw option background
            if (i == options.length - 1) {
                // special styling for the last option
                g2d.setColor(new Color(80, 80, 120));
                g2d.fillRoundRect(menuX + 40, optionY + i * spacing - 25, menuWidth - 80, 36, 10, 10);
                g2d.setColor(new Color(120, 120, 180));
                g2d.drawRoundRect(menuX + 40, optionY + i * spacing - 25, menuWidth - 80, 36, 10, 10);
            } else {
                g2d.setColor(new Color(70, 70, 70));
                g2d.fillRoundRect(menuX + 30, optionY + i * spacing - 25, menuWidth - 60, 36, 10, 10);
            }

            // draw option text
            g2d.setColor(Color.WHITE);
            g2d.drawString(options[i], menuX + 50, optionY + i * spacing);
        }

        // draw close button hint
        g2d.setFont(new Font("MV Boli", Font.ITALIC, 14));
        g2d.setColor(new Color(200, 200, 200));
        String closeHint = "Click Options button again to close";
        fm = g2d.getFontMetrics();
        int hintWidth = fm.stringWidth(closeHint);
        g2d.drawString(closeHint, menuX + (menuWidth - hintWidth) / 2, menuY + menuHeight - 20);
    }

    public void shootEnemy(Tower tower, Enemy enemy) {
        projectileManager.newProjectile(tower, enemy);
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
}
