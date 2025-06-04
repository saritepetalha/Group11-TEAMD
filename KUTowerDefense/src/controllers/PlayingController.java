package controllers;

import java.awt.event.MouseWheelEvent;
import java.util.Observable;
import java.util.Observer;

import config.GameOptions;
import constants.GameDimensions;
import enemies.Enemy;
import helpMethods.OptionsIO;
import main.Game;
import managers.*;
import models.PlayingModel;
import objects.Tower;
import objects.Warrior;
import scenes.Playing;
import stats.GameStatsRecord;
import ui_p.DeadTree;
import views.PlayingView;

/**
 * PlayingController - Handles input and coordinates between Model and View
 * Part of the MVC architecture for the Playing scene
 * 
 * Responsibilities:
 * - Handle all user input (mouse, keyboard)
 * - Coordinate between Model and View
 * - Initialize and manage game managers
 * - Handle game flow control (pause, speed, options)
 * - Manage tower/warrior placement and interactions
 */
@SuppressWarnings("deprecation")
public class PlayingController implements Observer {
    
    private PlayingModel model;
    private PlayingView view;
    private Game game;
    
    // Managers that need special handling or aren't part of the model
    private AudioManager audioManager;
    
    public PlayingController(Game game) {
        this.game = game;
        this.model = new PlayingModel();
        this.view = new PlayingView(model);
        
        // Observe model changes to handle game state transitions
        this.model.addObserver(this);
        
        initializeManagersForModel();
        audioManager = AudioManager.getInstance();
    }
    
    public PlayingController(Game game, managers.TileManager tileManager) {
        this.game = game;
        this.model = new PlayingModel(tileManager);
        this.view = new PlayingView(model);
        
        this.model.addObserver(this);
        
        initializeManagersForModel();
        audioManager = AudioManager.getInstance();
    }
    
    public PlayingController(Game game, managers.TileManager tileManager, int[][] customLevel, int[][] customOverlay) {
        this.game = game;
        this.model = new PlayingModel(tileManager, customLevel, customOverlay);
        this.view = new PlayingView(model);
        
        this.model.addObserver(this);
        
        initializeManagersForModel();
        audioManager = AudioManager.getInstance();
    }
    
    /**
     * Initialize all managers and inject them into the model
     */
    private void initializeManagersForModel() {
        // For now, we'll create managers using the PlayingAdapter pattern
        // In a full refactor, these would be updated to work directly with the model
        
        PlayingAdapter adapter = new PlayingAdapter();
        
        WeatherManager weatherManager = new WeatherManager();
        ProjectileManager projectileManager = new ProjectileManager(adapter);
        TreeInteractionManager treeInteractionManager = new TreeInteractionManager(adapter);
        FireAnimationManager fireAnimationManager = new FireAnimationManager();
        WaveManager waveManager = new WaveManager(adapter, model.getGameOptions());
        EnemyManager enemyManager = new EnemyManager(adapter, model.getOverlay(), model.getLevel(), model.getGameOptions());
        TowerManager towerManager = new TowerManager(adapter);
        
        // Set TowerManager reference in WeatherManager for lighting effects
        weatherManager.setTowerManager(towerManager);
        
        PlayerManager playerManager = new PlayerManager(model.getGameOptions());
        UltiManager ultiManager = new UltiManager(adapter);
        GoldBagManager goldBagManager = new GoldBagManager();
        
        // Inject all managers into the model
        model.initializeManagers(waveManager, towerManager, playerManager, projectileManager,
                               enemyManager, ultiManager, weatherManager, fireAnimationManager,
                               goldBagManager, treeInteractionManager);
    }
    
    /**
     * Main update method - called every frame
     */
    public void update() {
        model.update();
        checkButtonStates();
    }
    
    /**
     * Main render method - delegates to view
     */
    public void render(java.awt.Graphics g) {
        view.render(g);
    }
    
    // Input handling methods
    public void mouseClicked(int x, int y) {
        // Handle gold factory placement if selected
        if (model.getUltiManager() != null && model.getUltiManager().isGoldFactorySelected()) {
            boolean placed = model.getUltiManager().tryPlaceGoldFactory(x, y);
            if (!placed) {
                // If placement failed, keep factory selected for another try
            }
            return;
        }

        // Handle warrior placement if a warrior is pending placement
        if (model.getPendingWarriorPlacement() != null) {
            boolean placed = tryPlaceWarrior(x, y);
            if (placed) {
                return; // Successfully placed, exit early
            }
        }

        // Handle tower selection UI clicks first
        if (view.getTowerSelectionUI() != null && view.getTowerSelectionUI().hasTowerSelected()) {
            boolean uiHandledClick = view.getTowerSelectionUI().mouseClicked(x, y);
            if (uiHandledClick) {
                return; // UI button was clicked, don't process other interactions
            }
        }

        // Handle dead trees
        if (model.getDeadTrees() != null && model.getTreeInteractionManager() != null) {
            model.getTreeInteractionManager().handleDeadTreeInteraction(x, y);
        }
        
        // Handle live trees
        if (model.getLiveTrees() != null && model.getTreeInteractionManager() != null) {
            model.getTreeInteractionManager().handleLiveTreeInteraction(x, y);
        }

        // Gold bag collection
        if (model.getGoldBagManager() != null) {
            var collectedBag = model.getGoldBagManager().tryCollect(x, y);
            if (collectedBag != null && model.getPlayerManager() != null) {
                model.getPlayerManager().addGold(collectedBag.getGoldAmount());
            }
        }

        // Check for tower selection/deselection
        Tower clickedTower = getTowerAt(x, y);
        if (clickedTower != null) {
            model.setDisplayedTower(clickedTower);
            view.getTowerSelectionUI().setSelectedTower(clickedTower);
            audioManager.playButtonClickSound();
        } else {
            model.setDisplayedTower(null);
            view.getTowerSelectionUI().setSelectedTower(null);
        }
    }
    
    public void mouseMoved(int x, int y) {
        // Only snap to grid if gold factory is selected
        if (model.getUltiManager() != null && model.getUltiManager().isGoldFactorySelected()) {
            x = (x / GameDimensions.TILE_DISPLAY_SIZE) * GameDimensions.TILE_DISPLAY_SIZE;
            y = (y / GameDimensions.TILE_DISPLAY_SIZE) * GameDimensions.TILE_DISPLAY_SIZE;
        }
        
        view.mouseMoved(x, y);
    }
    
    public void mousePressed(int x, int y) {
        // Handle lightning targeting
        if (model.getUltiManager() != null && model.getUltiManager().isWaitingForLightningTarget()) {
            // Trigger lightning at the clicked position
            model.getUltiManager().triggerLightningAt(x, y);
            return;
        }

        // Handle gold factory placement
        if (model.getUltiManager() != null && model.getUltiManager().isGoldFactorySelected() &&
                !model.isOptionsMenuOpen() && !model.isGamePaused()) {
            // Don't handle placement here - let mouseClicked handle it
            return;
        }

        view.mousePressed(x, y);
    }
    
    public void mouseReleased(int x, int y) {
        view.mouseReleased(x, y);
    }
    
    public void mouseDragged(int x, int y) {
        view.mouseDragged(x, y);
    }
    
    public void mouseWheelMoved(MouseWheelEvent e) {
        view.mouseWheelMoved(e);
    }
    
    // Game control methods
    private void checkButtonStates() {
        if (view.getPlayingUI() == null) return;
        
        // Check the states of control buttons
        if (view.getPlayingUI().getPauseButton().isMousePressed()) {
            handlePauseButton(true);
        } else {
            handlePauseButton(false);
        }

        if (view.getPlayingUI().getFastForwardButton().isMousePressed()) {
            handleFastForwardButton(true);
        } else {
            handleFastForwardButton(false);
        }

        if (view.getPlayingUI().getOptionsButton().isMousePressed()) {
            handleOptionsButton(true);
        } else if (view.getPlayingUI().getBackOptionsButton().isMousePressed()) {
            handleBackOptionsButton(true);
        } else if (view.getPlayingUI().getMainMenuButton().isMousePressed()) {
            handleMainMenuButton(true);
        } else {
            handleOptionsButton(false);
        }
    }
    
    private void handlePauseButton(boolean isPressed) {
        if (isPressed != model.isGamePaused()) {
            model.togglePause();
        }
    }
    
    private void handleFastForwardButton(boolean isPressed) {
        if (isPressed && !model.isGameSpeedIncreased()) {
            model.toggleFastForward();
            System.out.println("Game speed increased");
        } else if (!isPressed && model.isGameSpeedIncreased()) {
            model.toggleFastForward();
            System.out.println("Game speed normal");
        }
    }
    
    private void handleOptionsButton(boolean isPressed) {
        if (isPressed && !model.isOptionsMenuOpen()) {
            model.toggleOptionsMenu();
            if (view.getPlayingUI().getPauseButton() != null) {
                view.getPlayingUI().getPauseButton().setMousePressed(true);
            }
            System.out.println("Options menu opened and game paused");
        } else if (!isPressed && model.isOptionsMenuOpen()) {
            model.toggleOptionsMenu();
            if (view.getPlayingUI().getPauseButton() != null) {
                view.getPlayingUI().getPauseButton().setMousePressed(false);
            }
            System.out.println("Options menu closed and game resumed");
        }
    }
    
    private void handleBackOptionsButton(boolean isPressed) {
        if (isPressed && model.isOptionsMenuOpen()) {
            model.toggleOptionsMenu();
            if (view.getPlayingUI().getPauseButton() != null) {
                view.getPlayingUI().getPauseButton().setMousePressed(false);
            }
            System.out.println("Options menu closed via back button and game resumed");
        }
    }
    
    private void handleMainMenuButton(boolean isPressed) {
        if (isPressed && model.isOptionsMenuOpen()) {
            model.toggleOptionsMenu();
            System.out.println("Options menu closed via main menu button");
            returnToMainMenu();
        }
    }
    
    // Helper methods
    private Tower getTowerAt(int mouseX, int mouseY) {
        if (model.getTowerManager() == null) return null;
        
        for (Tower tower : model.getTowerManager().getTowers()) {
            if (tower.isClicked(mouseX, mouseY)) {
                return tower;
            }
        }
        return null;
    }
    
    private boolean tryPlaceWarrior(int x, int y) {
        Warrior pendingWarrior = model.getPendingWarriorPlacement();
        if (pendingWarrior == null) return false;
        
        // Snap click coordinates to the grid
        int tileX = (x / GameDimensions.TILE_DISPLAY_SIZE) * GameDimensions.TILE_DISPLAY_SIZE;
        int tileY = (y / GameDimensions.TILE_DISPLAY_SIZE) * GameDimensions.TILE_DISPLAY_SIZE;

        // Check if the clicked position is a valid tile for placement
        if (isValidTileForPlacement(tileX, tileY)) {
            // Deduct gold for the warrior
            if (model.getPlayerManager() != null) {
                model.getPlayerManager().spendGold(pendingWarrior.getCost());
            }

            // Place the warrior at the top-left of the tile, with a slight upward offset
            int placementY = tileY - 8;
            pendingWarrior.setX(tileX);
            pendingWarrior.setY(placementY);
            
            if (model.getTowerManager() != null) {
                model.getTowerManager().getWarriors().add(pendingWarrior);
            }
            
            System.out.println("Warrior placed at tile coordinates: (" + tileX + ", " + placementY + ") for " + pendingWarrior.getCost() + " gold.");
            
            // Clear pending placement
            model.setPendingWarriorPlacement(null); // Properly clear the pending warrior
            model.setDisplayedTower(null); // Also clear tower selection
            return true;
        }
        return false;
    }
    
    private boolean isValidTileForPlacement(int pixelX, int pixelY) {
        int[][] level = model.getLevel();
        if (level == null) return false;
        
        int tileC = pixelX / GameDimensions.TILE_DISPLAY_SIZE;
        int tileR = pixelY / GameDimensions.TILE_DISPLAY_SIZE;

        if (tileR >= 0 && tileR < level.length && tileC >= 0 && tileC < level[0].length) {
            // Check if the tile type is grass (ID 5)
            boolean isGrass = level[tileR][tileC] == 5;
            if (!isGrass) return false;

            // Check if the tile is already occupied by a tower
            if (getTowerAt(pixelX, pixelY) != null) return false;

            // Check if the tile is already occupied by another warrior
            if (isWarriorAt(pixelX, pixelY)) return false;

            return true;
        }
        return false;
    }
    
    private boolean isWarriorAt(int x, int y) {
        if (model.getTowerManager() == null) return false;
        
        for (Warrior warrior : model.getTowerManager().getWarriors()) {
            if (warrior.getX() == x && warrior.getY() == y) {
                return true;
            }
        }
        return false;
    }
    
    public void returnToMainMenu() {
        System.out.println("Returning to main menu");
        game.changeGameState(main.GameStates.MENU);
    }
    
    public void startWarriorPlacement(Warrior warrior) {
        // Clear tower selection
        if (view.getTowerSelectionUI() != null) {
            view.getTowerSelectionUI().setSelectedTower(null);
        }
        
        // Set pending warrior in model
        model.setPendingWarriorPlacement(warrior);
        
        System.out.println("Warrior placement mode started for: " + warrior.getClass().getSimpleName());
    }
    
    // Level and game state management
    public void loadLevel(String levelName) {
        // This would need to be implemented to work with the model
        // For now, delegate to model methods when available
        model.setCurrentMapName(levelName);
    }
    
    public void saveLevel(String filename) {
        // This would save the current level state
        // Implementation depends on how level saving is handled
    }
    
    public void reloadGameOptions() {
        model.reloadGameOptions();
    }
    
    @Override
    public void update(Observable o, Object arg) {
        // Handle model state changes
        String notification = (String) arg;
        switch (notification) {
            case "victory":
                handleVictory();
                break;
            case "gameOver":
                handleGameOver();
                break;
            case "managersInitialized":
                // Managers are now ready
                startEnemySpawning();
                break;
            // Add more cases as needed
        }
    }
    
    private void handleVictory() {
        GameStatsRecord record = model.createGameStatsRecord(true);
        game.getStatsManager().addRecord(record);
        game.getStatsManager().saveToFile(record);

        game.getGameOverScene().setStats(
                true,
                model.getPlayerManager() != null ? model.getPlayerManager().getTotalGoldEarned() : 0,
                model.getTotalEnemiesSpawned(),
                model.getEnemiesReachedEnd(),
                model.getTowerManager() != null ? model.getTowerManager().getTowers().size() : 0,
                model.getEnemyDefeated(),
                model.getTotalDamage(),
                model.getTimePlayedInSeconds()
        );

        game.changeGameState(main.GameStates.GAME_OVER);
    }
    
    private void handleGameOver() {
        GameStatsRecord record = model.createGameStatsRecord(false);
        game.getStatsManager().addRecord(record);
        game.getStatsManager().saveToFile(record);

        game.getGameOverScene().setStats(
                false,
                model.getPlayerManager() != null ? model.getPlayerManager().getTotalGoldEarned() : 0,
                model.getTotalEnemiesSpawned(),
                model.getEnemiesReachedEnd(),
                model.getTowerManager() != null ? model.getTowerManager().getTowers().size() : 0,
                model.getEnemyDefeated(),
                model.getTotalDamage(),
                model.getTimePlayedInSeconds()
        );

        game.changeGameState(main.GameStates.GAME_OVER);
    }
    
    private void startEnemySpawning() {
        if (model.getWaveManager() != null) {
            model.getWaveManager().resetWaveManager();
        }
    }
    
    // Getters for external access (if needed)
    public PlayingModel getModel() { return model; }
    public PlayingView getView() { return view; }
    
    // Utility methods for compatibility
    public boolean isGamePaused() { return model.isGamePaused(); }
    public boolean isOptionsMenuOpen() { return model.isOptionsMenuOpen(); }
    public float getGameSpeedMultiplier() { return model.getGameSpeedMultiplier(); }
    public String getCurrentMapName() { return model.getCurrentMapName(); }
    public String getCurrentDifficulty() { return model.getCurrentDifficulty(); }
    
    /**
     * Temporary adapter class to provide Playing-like interface to managers
     * In a full refactor, managers would be updated to work directly with the controller/model
     */
    private class PlayingAdapter extends Playing {
        public PlayingAdapter() {
            // Use the safe constructor that doesn't create a controller
            super(PlayingController.this.game, true); // true = isAdapter flag
        }
        
        @Override
        public EnemyManager getEnemyManager() { return model.getEnemyManager(); }
        
        @Override
        public WeatherManager getWeatherManager() { return model.getWeatherManager(); }
        
        @Override
        public PlayerManager getPlayerManager() { return model.getPlayerManager(); }
        
        @Override
        public TowerManager getTowerManager() { return model.getTowerManager(); }
        
        @Override
        public GoldBagManager getGoldBagManager() { return model.getGoldBagManager(); }
        
        @Override
        public UltiManager getUltiManager() { return model.getUltiManager(); }
        
        @Override
        public void incrementEnemyDefeated() { model.incrementEnemyDefeated(); }
        
        @Override
        public void addTotalDamage(int damage) { model.addTotalDamage(damage); }
        
        @Override
        public void enemyReachedEnd(Enemy enemy) { model.enemyReachedEnd(enemy); }
        
        @Override
        public void spawnEnemy(int enemyType) { model.spawnEnemy(enemyType); }
        
        @Override
        public boolean isGamePaused() { return model.isGamePaused(); }
        
        @Override
        public float getGameSpeedMultiplier() { return model.getGameSpeedMultiplier(); }
        
        @Override
        public long getGameTime() { return model.getGameTime(); }
        
        @Override
        public int[][] getLevel() { return model.getLevel(); }
        
        @Override
        public int[][] getOverlay() { return model.getOverlay(); }
        
        @Override
        public void startWarriorPlacement(Warrior warrior) {
            // Instead of calling controller (which is null), handle directly
            model.setPendingWarriorPlacement(warrior);
            // Clear tower selection
            model.setDisplayedTower(null);
            System.out.println("Warrior placement mode started for: " + warrior.getClass().getSimpleName());
        }
        
        @Override
        public void shootEnemy(Object shooter, Enemy enemy) {
            if (model.getProjectileManager() != null) {
                model.getProjectileManager().newProjectile(shooter, enemy);
            }
            // Handle tower effects
            if (shooter instanceof Tower) {
                Tower tower = (Tower) shooter;
                // tower.applyOnHitEffect(enemy, this); // Would need adapter for this
            }
        }
        
        // Override additional methods that might be called by managers
        @Override
        public managers.WaveManager getWaveManager() { return model.getWaveManager(); }
        
        @Override
        public managers.TileManager getTileManager() { return model.getTileManager(); }
        
        @Override
        public managers.FireAnimationManager getFireAnimationManager() { return model.getFireAnimationManager(); }
        
        @Override
        public String getCurrentMapName() { return model.getCurrentMapName(); }
        
        @Override
        public String getCurrentDifficulty() { return model.getCurrentDifficulty(); }
        
        @Override
        public boolean isAllWavesFinished() { return model.isAllWavesFinished(); }
        
        @Override
        public config.GameOptions getGameOptions() { return model.getGameOptions(); }
        
        @Override
        public String getWaveStatus() { return model.getWaveStatus(); }
        
        // Override tree-related methods
        @Override
        public java.util.List<ui_p.DeadTree> getDeadTrees() { return model.getDeadTrees(); }
        
        @Override
        public java.util.List<ui_p.LiveTree> getLiveTrees() { return model.getLiveTrees(); }
        
        // Override methods that are called by UI components
        @Override
        public void modifyTile(int x, int y, String tile) {
            // Handle tile modification directly through model (same logic as Playing.java)
            x /= 64;
            y /= 64;

            int[][] level = model.getLevel();
            if (level == null) return;
            
            if (tile.equals("ARCHER")) {
                level[y][x] = 26;
            } else if (tile.equals("MAGE")) {
                level[y][x] = 20;
            } else if (tile.equals("ARTILERRY")) {
                level[y][x] = 21;
            } else if (tile.equals("DEADTREE")) {
                level[y][x] = 15;
            }
            System.out.println("Tile modified at (" + x + ", " + y + ") to: " + tile);
        }
    }
} 