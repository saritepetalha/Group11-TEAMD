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
import models.Tile;
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
        
        // Initialize Stone Mining Manager
        StoneMiningManager stoneMiningManager = StoneMiningManager.getInstance();
        stoneMiningManager.initialize(model, view);
        model.setStoneMiningManager(stoneMiningManager);
        
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
        // Handle warrior placement
        if (model.getPendingWarriorPlacement() != null) {
            handleWarriorPlacement(x, y);
            return;
        }

        // Handle gold factory placement
        if (model.getUltiManager() != null && model.getUltiManager().isGoldFactorySelected()) {
            handleGoldFactoryPlacement(x, y);
            return;
        }

        // Handle tower selection
        if (model.getTowerManager() != null) {
            for (Tower tower : model.getTowerManager().getTowers()) {
                if (tower.isClicked(x, y)) {
                    model.setDisplayedTower(tower);
                    return;
                }
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

        // Handle stone mining
        if (model.getStoneMiningManager() != null) {
            int tileX = x / GameDimensions.TILE_DISPLAY_SIZE;
            int tileY = y / GameDimensions.TILE_DISPLAY_SIZE;
            if (tileX >= 0 && tileX < model.getLevel()[0].length && 
                tileY >= 0 && tileY < model.getLevel().length) {
                int tileId = model.getLevel()[tileY][tileX];
                if (tileId == 19 || tileId == 23) {
                    model.getStoneMiningManager().handleStoneClick(new objects.Tile(tileX, tileY, tileId));
                }
            }
        }
    }

    public void mouseMoved(int x, int y) {
        // Handle stone mining manager mouse events
        if (model.getStoneMiningManager() != null) {
            model.getStoneMiningManager().mouseMoved(x, y);
        }

        view.mouseMoved(x, y);
    }

    public void mousePressed(int x, int y) {
        // Handle lightning targeting
        if (model.getUltiManager() != null && model.getUltiManager().isWaitingForLightningTarget()) {
            model.getUltiManager().triggerLightningAt(x, y);
            return;
        }

        if (model.getStoneMiningManager() != null &&
                model.getStoneMiningManager().getMineButton() != null &&
                model.getStoneMiningManager().getMineButton().getBounds().contains(x, y)) {
            model.getStoneMiningManager().mousePressed(x, y);
            return; // Don't process anything else
        }

        // Handle stone mining
        if (model.getStoneMiningManager() != null) {
            int tileX = x / GameDimensions.TILE_DISPLAY_SIZE;
            int tileY = y / GameDimensions.TILE_DISPLAY_SIZE;
            if (tileX >= 0 && tileX < model.getLevel()[0].length &&
                    tileY >= 0 && tileY < model.getLevel().length) {
                int tileId = model.getLevel()[tileY][tileX];
                if (tileId == 19 || tileId == 23) {
                    model.getStoneMiningManager().handleStoneClick(new objects.Tile(tileX, tileY, tileId));
                } else {
                    model.getStoneMiningManager().clearMiningButton();
                }
            }
        }

        // Handle stone mining manager mouse events
        if (model.getStoneMiningManager() != null) {
            model.getStoneMiningManager().mousePressed(x, y);
        }

        // Handle warrior placement
        if (model.getPendingWarriorPlacement() != null) {
            // Handle warrior placement in mouseClicked
            return;
        }

        // Handle gold factory placement
        if (model.getUltiManager() != null && model.getUltiManager().isGoldFactorySelected()) {
            // Handle gold factory placement in mouseClicked
            return;
        }

        // Handle tower selection
        if (model.getTowerManager() != null) {
            // Tower selection is handled in mouseClicked
            return;
        }

        // Handle tree interactions
        if (model.getTreeInteractionManager() != null) {
            model.getTreeInteractionManager().handleDeadTreeInteraction(x, y);
            model.getTreeInteractionManager().handleLiveTreeInteraction(x, y);
        }

        view.mousePressed(x, y);
    }

    public void mouseReleased(int x, int y) {
        // Handle stone mining manager mouse events
        if (model.getStoneMiningManager() != null) {
            model.getStoneMiningManager().mouseReleased(x, y);
        }

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

            // Set the target destination with slight upward offset for final position
            int finalY = tileY - 8;
            pendingWarrior.setTargetDestination(tileX, finalY);
            
            // Add warrior to manager (it will start running to the target)
            if (model.getTowerManager() != null) {
                model.getTowerManager().getWarriors().add(pendingWarrior);
            }
            
            System.out.println("Warrior will run from (" + pendingWarrior.getSpawnX() + ", " + pendingWarrior.getSpawnY() + 
                             ") to (" + tileX + ", " + finalY + ") for " + pendingWarrior.getCost() + " gold.");
            
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
    
    /**
     * Reload difficulty configuration and reinitialize managers
     * This should be called when the difficulty is changed
     */
    public void reloadDifficultyConfiguration() {
        // Reload the GameOptions from disk (which should have the new difficulty settings)
        model.reloadGameOptions();
        
        // Reinitialize managers with the new options
        reinitializeManagersWithNewOptions();
        
        // Apply difficulty stats to existing game entities
        applyDifficultyToExistingEntities();
        
        System.out.println("Difficulty configuration reloaded and managers updated");
        System.out.println("Current GameOptions: Gold=" + model.getGameOptions().getStartingGold() + 
                         ", Health=" + model.getGameOptions().getStartingPlayerHP() + 
                         ", Shield=" + model.getGameOptions().getStartingShield());
    }
    
    /**
     * Reinitialize managers with updated GameOptions
     */
    private void reinitializeManagersWithNewOptions() {
        // Update existing managers with new GameOptions where possible
        // This is more efficient than recreating everything
        
        if (model.getEnemyManager() != null) {
            model.getEnemyManager().updateGameOptions(model.getGameOptions());
        }
        
        if (model.getWaveManager() != null) {
            // WaveManager will get updated via applyDifficultyToExistingEntities
        }
        
        if (model.getPlayerManager() != null) {
            model.getPlayerManager().updateGameOptions(model.getGameOptions());
        }
        
        System.out.println("Updated existing managers with new GameOptions");
    }
    
    /**
     * Apply difficulty settings to existing towers, enemies, and waves
     */
    private void applyDifficultyToExistingEntities() {
        // Apply new enemy stats to all existing enemies
        if (model.getEnemyManager() != null) {
            model.getEnemyManager().updateAllEnemyStatsFromOptions();
        }
        
        // Apply new tower stats to all existing towers
        if (model.getTowerManager() != null) {
            model.getTowerManager().updateAllTowerStatsFromOptions(model.getGameOptions());
        }
        
        // Update wave manager with new wave configuration
        if (model.getWaveManager() != null) {
            model.getWaveManager().updateWaveConfigurationFromOptions(model.getGameOptions());
        }
        
        System.out.println("Applied difficulty settings to existing game entities");
    }
    
    // ================ GAME STATE MANAGEMENT ================
    
    /**
     * Save the current game state
     * @param filename The filename to save to (without extension)
     * @return true if save was successful, false otherwise
     */
    public boolean saveGameState(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            filename = "quicksave";
        }
        
        boolean success = model.saveGameState(filename);
        if (success) {
            System.out.println("Game saved successfully as: " + filename);
        } else {
            System.err.println("Failed to save game: " + filename);
        }
        return success;
    }
    
    /**
     * Load a game state from file
     * @param filename The filename to load from (without extension)
     * @return true if load was successful, false otherwise
     */
    public boolean loadGameState(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            filename = "quicksave";
        }
        
        boolean success = model.loadGameState(filename);
        if (success) {
            System.out.println("Game loaded successfully from: " + filename);
            // Refresh the view after loading
            view.update(model, "gameStateLoaded");
        } else {
            System.err.println("Failed to load game: " + filename);
        }
        return success;
    }
    
    /**
     * Reset the game to initial state
     */
    public void resetGameState() {
        model.resetGameState();
        System.out.println("Game state reset to initial conditions");
    }
    
    /**
     * Quick save functionality - saves to default filename
     */
    public boolean quickSave() {
        return saveGameState("quicksave");
    }
    
    /**
     * Quick load functionality - loads from default filename
     */
    public boolean quickLoad() {
        return loadGameState("quicksave");
    }
    
    // ================ PROJECTILE ABSTRACTION ================
    
    /**
     * Handle projectile creation - abstracted from direct manager access
     * @param shooter The shooter (Tower or Warrior)
     * @param target The target enemy
     */
    public void createProjectile(Object shooter, Enemy target) {
        model.createProjectile(shooter, target);
    }
    
    /**
     * Get active projectile count
     * @return Number of active projectiles
     */
    public int getActiveProjectileCount() {
        return model.getActiveProjectileCount();
    }
    
    /**
     * Check if there are active projectiles
     * @return true if projectiles are active
     */
    public boolean hasActiveProjectiles() {
        return model.hasActiveProjectiles();
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
            // Use the abstracted method instead of direct manager access
            createProjectile(shooter, enemy);
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

    private void handleWarriorPlacement(int x, int y) {
        boolean placed = tryPlaceWarrior(x, y);
        if (placed) {
            model.setPendingWarriorPlacement(null);
        }
    }

    private void handleGoldFactoryPlacement(int x, int y) {
        boolean placed = model.getUltiManager().tryPlaceGoldFactory(x, y);
        if (!placed) {
            // If placement failed, keep factory selected for another try
        }
    }
} 