package models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Observable;

import config.GameOptions;
import constants.Constants;
import constants.GameDimensions;
import enemies.Enemy;
import helpMethods.LoadSave;
import helpMethods.OptionsIO;
import interfaces.GameContext;
import managers.*;
import objects.*;
import stats.GameStatsRecord;
import ui_p.DeadTree;
import ui_p.LiveTree;

/**
 * PlayingModel - Contains all game state data and business logic
 * Part of the MVC architecture for the Playing scene
 * 
 * Responsibilities:
 * - Manage game state (paused, speed, victory/defeat conditions)
 * - Manage game resources and statistics
 * - Handle save/load functionality
 * - Provide data access for View and Controller
 */
@SuppressWarnings("deprecation")
public class PlayingModel extends Observable implements GameContext {
    
    // Core game state
    private boolean gamePaused = false;
    private boolean gameSpeedIncreased = false;
    private boolean optionsMenuOpen = false;
    private float gameSpeedMultiplier = 1.0f;
    private boolean gameOverHandled = false;
    private boolean victoryHandled = false;
    
    // Map data
    private int[][] level;
    private int[][] overlay;
    private int[][] originalLevelData;
    private int[][] originalOverlayData;
    private String currentMapName = "defaultlevel";
    private String currentDifficulty = "Normal";
    
    // Game options and configuration
    private GameOptions gameOptions;
    
    // Game statistics
    private int totalEnemiesSpawned = 0;
    private int enemiesReachedEnd = 0;
    private int enemyDefeated = 0;
    private int totalDamage = 0;
    private int timePlayedInSeconds = 0;
    private int updateCounter = 0;
    private long gameTimeMillis = 0;
    
    // Castle health
    private int castleMaxHealth;
    private int castleCurrentHealth;
    
    // Game entities (these will be managed by the controllers)
    private List<DeadTree> deadTrees;
    private List<LiveTree> liveTrees;
    private Tower displayedTower;
    private DeadTree selectedDeadTree;
    private Warrior pendingWarriorPlacement = null;
    
    // Manager references (to be injected by controller)
    private WaveManager waveManager;
    private TowerManager towerManager;
    private TileManager tileManager;
    private PlayerManager playerManager;
    private ProjectileManager projectileManager;
    private EnemyManager enemyManager;
    private UltiManager ultiManager;
    private WeatherManager weatherManager;
    private FireAnimationManager fireAnimationManager;
    private GoldBagManager goldBagManager;
    private TreeInteractionManager treeInteractionManager;
    private GameStateManager gameStateManager;
    private StoneMiningManager stoneMiningManager;
    
    // Initialization flag
    private boolean isFirstReset = true;
    
    public PlayingModel() {
        this.tileManager = new TileManager();
        this.gameOptions = loadOptionsOrDefault();
        this.gameStateManager = new GameStateManager();
        loadDefaultLevel();
        // Managers will be initialized by the controller

        // Initialize stone mining manager
        stoneMiningManager = new StoneMiningManager();
    }
    
    public PlayingModel(TileManager tileManager) {
        this.tileManager = tileManager;
        this.gameOptions = loadOptionsOrDefault();
        this.gameStateManager = new GameStateManager();
        loadDefaultLevel();
        // Managers will be initialized by the controller
    }
    
    public PlayingModel(TileManager tileManager, int[][] customLevel, int[][] customOverlay) {
        this.tileManager = tileManager;
        this.level = customLevel;
        this.originalLevelData = deepCopy2DArray(customLevel);
        this.overlay = customOverlay;
        this.originalOverlayData = deepCopy2DArray(customOverlay);
        this.gameOptions = loadOptionsOrDefault();
        this.gameStateManager = new GameStateManager();
        // Managers will be initialized by the controller
    }
    
    private GameOptions loadOptionsOrDefault() {
        GameOptions loadedOptions = OptionsIO.load();
        if (loadedOptions == null) {
            System.out.println("PlayingModel: Failed to load GameOptions, using defaults.");
            return GameOptions.defaults();
        }
        System.out.println("PlayingModel: Successfully loaded GameOptions.");
        return loadedOptions;
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
        this.originalLevelData = deepCopy2DArray(this.level);

        this.overlay = new int[lvl.length][lvl[0].length];
        overlay[4][0] = 1;
        overlay[4][15] = 2;
        this.originalOverlayData = deepCopy2DArray(this.overlay);
    }
    
    private int[][] deepCopy2DArray(int[][] source) {
        if (source == null) return null;
        int[][] destination = new int[source.length][];
        for (int i = 0; i < source.length; i++) {
            if (source[i] != null) {
                destination[i] = Arrays.copyOf(source[i], source[i].length);
            }
        }
        return destination;
    }
    
    /**
     * Initialize managers - called by the controller
     */
    public void initializeManagers(WaveManager waveManager, TowerManager towerManager,
                                 PlayerManager playerManager, ProjectileManager projectileManager,
                                 EnemyManager enemyManager, UltiManager ultiManager,
                                 WeatherManager weatherManager, FireAnimationManager fireAnimationManager,
                                 GoldBagManager goldBagManager, TreeInteractionManager treeInteractionManager) {
        this.waveManager = waveManager;
        this.towerManager = towerManager;
        this.playerManager = playerManager;
        this.projectileManager = projectileManager;
        this.enemyManager = enemyManager;
        this.ultiManager = ultiManager;
        this.weatherManager = weatherManager;
        this.fireAnimationManager = fireAnimationManager;
        this.goldBagManager = goldBagManager;
        this.treeInteractionManager = treeInteractionManager;
        
        this.castleMaxHealth = calculateCastleMaxHealth();
        this.castleCurrentHealth = castleMaxHealth;
        
        // Initialize tree lists
        if (towerManager != null) {
            if (towerManager.findDeadTrees(level) != null)
                deadTrees = towerManager.findDeadTrees(level);
            if (towerManager.findLiveTrees(level) != null)
                liveTrees = towerManager.findLiveTrees(level);
        }
        
        setChanged();
        notifyObservers("managersInitialized");
    }
    
    /**
     * Main game update method - called every frame by controller
     */
    public void update() {
        if (!gamePaused && managersInitialized()) {
            updateGame();
        }
        setChanged();
        notifyObservers("gameUpdated");
    }
    
    private boolean managersInitialized() {
        return waveManager != null && enemyManager != null && towerManager != null && 
               playerManager != null && projectileManager != null;
    }
    
    private void updateGame() {
        long delta = (long)(16 * gameSpeedMultiplier);
        gameTimeMillis += delta;
        float deltaTimeSeconds = delta / 1000.0f;

        if (waveManager != null) waveManager.update();
        if (projectileManager != null) projectileManager.update();
        if (fireAnimationManager != null) fireAnimationManager.update();
        if (ultiManager != null) ultiManager.update(gameTimeMillis);
        if (weatherManager != null) weatherManager.update(deltaTimeSeconds);

        if (tileManager != null && weatherManager != null) {
            tileManager.updateSnowTransition(deltaTimeSeconds, weatherManager.isSnowing());
        }

        // Check enemy status and handle wave completion
        if (isAllEnemiesDead()) {
            if (waveManager.isThereMoreWaves()) {
                // Just let WaveManager handle the wave timing and progression
            } else if (waveManager.isAllWavesFinished()) {
                // Only trigger victory if all waves are processed and finished
                handleVictory();
            }
        }

        // Update other game elements
        if (enemyManager != null) enemyManager.update(gameSpeedMultiplier);
        if (towerManager != null) towerManager.update(gameSpeedMultiplier);

        if (playerManager != null && !playerManager.isAlive()) {
            handleGameOver();
        }

        if (goldBagManager != null) goldBagManager.update();

        updateCounter++;
        if (updateCounter >= 60) {
            timePlayedInSeconds++;
            updateCounter = 0;
        }
        
        setChanged();
        notifyObservers("resourcesUpdated");
    }
    
    private boolean isAllEnemiesDead() {
        if (waveManager == null || enemyManager == null) return false;
        
        // Check if the current wave is finished according to WaveManager
        boolean waveFinished = waveManager.isWaveFinished();

        if (waveFinished) {
            // If the wave is marked as finished, but there are still enemies on the board
            // we need to check if they're all either dead or have reached the end
            if (enemyManager.getEnemies().isEmpty()) {
                return true; // No enemies left
            }

            // Check each enemy
            for (Enemy enemy : enemyManager.getEnemies()) {
                if (enemy.isAlive() && !enemy.hasReachedEnd()) {
                    return false; // Found at least one alive enemy that hasn't reached the end
                }
            }
            return true; // All enemies are either dead or have reached the end
        }

        return false; // Current wave is not yet finished
    }
    
    private void handleVictory() {
        // Prevent multiple calls to handleVictory
        if (victoryHandled || !waveManager.isAllWavesFinished() || !playerManager.isAlive()) return;

        System.out.println("Victory!");
        victoryHandled = true;

        if (weatherManager != null) weatherManager.stopAllWeatherSounds();

        // Delete the save file
        if (gameStateManager != null) gameStateManager.deleteSaveFile(currentMapName);

        // play the specific victory sound
        AudioManager.getInstance().playSound("win4");
        
        setChanged();
        notifyObservers("victory");
    }
    
    private void handleGameOver() {
        // Prevent multiple calls to handleGameOver
        if (gameOverHandled) return;

        System.out.println("Game Over!");
        gameOverHandled = true;

        if (weatherManager != null) weatherManager.stopAllWeatherSounds();

        // Delete the save file
        if (gameStateManager != null) gameStateManager.deleteSaveFile(currentMapName);

        // Play the specific lose sound
        AudioManager.getInstance().playSound("lose5");

        // stop any ongoing waves/spawning
        if (enemyManager != null) enemyManager.getEnemies().clear();
        
        setChanged();
        notifyObservers("gameOver");
    }
    
    // Game state control methods
    public void togglePause() {
        gamePaused = !gamePaused;
        setChanged();
        notifyObservers("pauseToggled");
    }
    
    public void toggleFastForward() {
        gameSpeedIncreased = !gameSpeedIncreased;
        gameSpeedMultiplier = gameSpeedIncreased ? 2.0f : 1.0f;
        setChanged();
        notifyObservers("speedToggled");
    }
    
    public void toggleOptionsMenu() {
        optionsMenuOpen = !optionsMenuOpen;
        if (optionsMenuOpen) {
            gamePaused = true;
        }
        setChanged();
        notifyObservers("optionsToggled");
    }
    
    private int calculateCastleMaxHealth() {
        if (waveManager != null) {
            return waveManager.getWaveCount() * 100;
        }
        return 500; // Default value
    }
    
    // GameContext interface implementation
    @Override
    public EnemyManager getEnemyManager() { return enemyManager; }
    
    @Override
    public WeatherManager getWeatherManager() { return weatherManager; }
    
    @Override
    public PlayerManager getPlayerManager() { return playerManager; }
    
    @Override
    public TowerManager getTowerManager() { return towerManager; }
    
    @Override
    public GoldBagManager getGoldBagManager() { return goldBagManager; }
    
    @Override
    public UltiManager getUltiManager() { return ultiManager; }
    
    @Override
    public void incrementEnemyDefeated() {
        enemyDefeated++;
        setChanged();
        notifyObservers("enemyDefeated");
    }
    
    @Override
    public void addTotalDamage(int damage) {
        totalDamage += damage;
    }
    
    @Override
    public void enemyReachedEnd(Enemy enemy) {
        System.out.println("Enemy reached end: " + enemy.getId());

        enemiesReachedEnd++;
        if (playerManager != null) {
            playerManager.takeDamage(1);
            this.castleCurrentHealth = playerManager.getHealth();

            if (!playerManager.isAlive()) {
                handleGameOver();
            }
        }
        
        setChanged();
        notifyObservers("enemyReachedEnd");
    }
    
    @Override
    public void spawnEnemy(int enemyType) {
        if (enemyType != -1 && enemyManager != null) {
            enemyManager.spawnEnemy(enemyType);
            totalEnemiesSpawned++;
            System.out.println("Spawning enemy of type: " + enemyType);
        } else {
            System.out.println("Invalid enemy type (-1) received, skipping spawn");
        }
    }
    
    @Override
    public boolean isGamePaused() { return gamePaused; }
    
    @Override
    public float getGameSpeedMultiplier() { return gameSpeedMultiplier; }
    
    @Override
    public long getGameTime() { return gameTimeMillis; }
    
    @Override
    public int[][] getLevel() { return level; }
    
    @Override
    public int[][] getOverlay() { return overlay; }
    
    // Basic getters and setters
    public boolean isGameSpeedIncreased() { return gameSpeedIncreased; }
    public boolean isOptionsMenuOpen() { return optionsMenuOpen; }
    public boolean isGameOverHandled() { return gameOverHandled; }
    public boolean isVictoryHandled() { return victoryHandled; }
    
    public String getCurrentMapName() { return currentMapName; }
    public String getCurrentDifficulty() { return currentDifficulty; }
    
    public void setCurrentMapName(String mapName) { 
        this.currentMapName = mapName;
        setChanged();
        notifyObservers("mapNameChanged");
    }
    
    public void setCurrentDifficulty(String difficulty) {
        if (difficulty == null) {
            this.currentDifficulty = "Normal";
        } else {
            switch (difficulty) {
                case "Easy":
                case "Normal":
                case "Hard":
                case "Custom":
                    this.currentDifficulty = difficulty;
                    break;
                default:
                    this.currentDifficulty = "Normal";
                    System.out.println("Invalid difficulty '" + difficulty + "', defaulting to Normal");
            }
        }
        setChanged();
        notifyObservers("difficultyChanged");
    }
    
    // Manager getters
    public WaveManager getWaveManager() { return waveManager; }
    public TileManager getTileManager() { return tileManager; }
    public ProjectileManager getProjectileManager() { return projectileManager; }
    public FireAnimationManager getFireAnimationManager() { return fireAnimationManager; }
    public TreeInteractionManager getTreeInteractionManager() { return treeInteractionManager; }
    public GameStateManager getGameStateManager() { return gameStateManager; }
    
    // Statistics getters
    public int getTotalEnemiesSpawned() { return totalEnemiesSpawned; }
    public int getEnemiesReachedEnd() { return enemiesReachedEnd; }
    public int getEnemyDefeated() { return enemyDefeated; }
    public int getTotalDamage() { return totalDamage; }
    public int getTimePlayedInSeconds() { return timePlayedInSeconds; }
    
    // Entity getters
    public List<DeadTree> getDeadTrees() { return deadTrees; }
    public List<LiveTree> getLiveTrees() { return liveTrees; }
    public Tower getDisplayedTower() { return displayedTower; }
    public DeadTree getSelectedDeadTree() { return selectedDeadTree; }
    public Warrior getPendingWarriorPlacement() { return pendingWarriorPlacement; }
    
    public void setDisplayedTower(Tower tower) { 
        this.displayedTower = tower;
        setChanged();
        notifyObservers("towerSelected");
    }
    
    public void setSelectedDeadTree(DeadTree deadTree) { 
        this.selectedDeadTree = deadTree;
        setChanged();
        notifyObservers("deadTreeSelected");
    }
    
    public GameOptions getGameOptions() {
        return gameOptions;
    }
    
    public String getWaveStatus() {
        if (waveManager == null) return "Loading...";
        int currentWave = waveManager.getWaveIndex() + 1;
        String stateInfo = waveManager.getCurrentStateInfo();
        return "Wave " + currentWave + "\n" + stateInfo;
    }
    
    public boolean isAllWavesFinished() {
        return waveManager != null && waveManager.isAllWavesFinished();
    }
    
    public void reloadGameOptions() {
        try {
            // Load fresh options
            this.gameOptions = loadOptionsOrDefault();

            // Update all managers with new options
            if (waveManager != null) waveManager.reloadFromOptions();
            if (enemyManager != null) enemyManager.reloadFromOptions();
            if (playerManager != null) playerManager.reloadFromOptions();

            setChanged();
            notifyObservers("optionsReloaded");
        } catch (Exception e) {
            System.out.println("Error reloading game options: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public GameStatsRecord createGameStatsRecord(boolean victory) {
        return new GameStatsRecord(
                currentMapName, victory,
                playerManager != null ? playerManager.getTotalGoldEarned() : 0,
                totalEnemiesSpawned,
                enemiesReachedEnd,
                towerManager != null ? towerManager.getTowers().size() : 0,
                enemyDefeated,
                totalDamage,
                timePlayedInSeconds
        );
    }
    
    public void setPendingWarriorPlacement(Warrior warrior) { 
        this.pendingWarriorPlacement = warrior;
        setChanged();
        notifyObservers("pendingWarriorPlacement");
    }
    
    // ================ GAME STATE MANAGEMENT ================
    
    /**
     * Save the current game state to a file
     * @param filename The name of the save file
     * @return true if save was successful, false otherwise
     */
    public boolean saveGameState(String filename) {
        try {
            GameSaveData saveData = createGameSaveData();
            // For now, use a simple file-based approach until GameStateManager is updated
            java.io.File saveDir = new java.io.File("saves");
            if (!saveDir.exists()) {
                saveDir.mkdirs();
            }
            
            java.io.File saveFile = new java.io.File(saveDir, filename + ".save");
            try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(
                    new java.io.FileOutputStream(saveFile))) {
                oos.writeObject(saveData);
                System.out.println("Game state saved successfully to: " + saveFile.getAbsolutePath());
                return true;
            }
        } catch (Exception e) {
            System.err.println("Failed to save game state: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Load a game state from a file
     * @param filename The name of the save file
     * @return true if load was successful, false otherwise
     */
    public boolean loadGameState(String filename) {
        try {
            java.io.File saveFile = new java.io.File("saves", filename + ".save");
            if (!saveFile.exists()) {
                System.err.println("Save file not found: " + saveFile.getAbsolutePath());
                return false;
            }
            
            try (java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                    new java.io.FileInputStream(saveFile))) {
                GameSaveData saveData = (GameSaveData) ois.readObject();
                applyGameSaveData(saveData);
                setChanged();
                notifyObservers("gameStateLoaded");
                System.out.println("Game state loaded successfully from: " + saveFile.getAbsolutePath());
                return true;
            }
        } catch (Exception e) {
            System.err.println("Failed to load game state: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Reset the game state to initial conditions
     */
    public void resetGameState() {
        try {
            // Reset game state flags
            gamePaused = false;
            gameSpeedIncreased = false;
            optionsMenuOpen = false;
            gameSpeedMultiplier = 1.0f;
            gameOverHandled = false;
            victoryHandled = false;
            
            // Reset statistics
            totalEnemiesSpawned = 0;
            enemiesReachedEnd = 0;
            enemyDefeated = 0;
            totalDamage = 0;
            timePlayedInSeconds = 0;
            updateCounter = 0;
            gameTimeMillis = 0;
            
            // Reset castle health
            castleCurrentHealth = castleMaxHealth;
            
            // Reset level data to original state
            if (originalLevelData != null) {
                level = deepCopy2DArray(originalLevelData);
            }
            if (originalOverlayData != null) {
                overlay = deepCopy2DArray(originalOverlayData);
            }
            
            // Reset UI selections
            displayedTower = null;
            selectedDeadTree = null;
            pendingWarriorPlacement = null;
            
            // Reset managers if they exist
            if (managersInitialized()) {
                // Reset player manager resources
                if (playerManager != null) {
                    // Use GameOptions starting values instead of hardcoded values
                    int startingGold = (gameOptions != null) ? gameOptions.getStartingGold() : 100;
                    int startingHealth = (gameOptions != null) ? gameOptions.getStartingPlayerHP() : castleMaxHealth;
                    int startingShield = (gameOptions != null) ? gameOptions.getStartingShield() : castleMaxHealth;
                    
                    playerManager.setGold(startingGold);
                    playerManager.setHealth(startingHealth);
                    playerManager.setShield(startingShield);
                    
                    System.out.println("Reset player values to difficulty settings: Gold=" + startingGold + 
                                     ", Health=" + startingHealth + ", Shield=" + startingShield);
                }
                
                // Clear manager states
                if (enemyManager != null) {
                    enemyManager.clearEnemies();
                }
                if (projectileManager != null) {
                    projectileManager.clearProjectiles();
                }
                if (waveManager != null) {
                    // Reset wave manager - basic reset without specific method
                    // Will be improved when proper reset methods are available
                    System.out.println("Wave manager reset - full implementation pending");
                }
                if (towerManager != null) {
                    towerManager.clearTowers();
                    // Clear warriors if method exists
                    try {
                        java.lang.reflect.Method clearWarriorsMethod = towerManager.getClass().getMethod("clearWarriors");
                        clearWarriorsMethod.invoke(towerManager);
                    } catch (Exception e) {
                        // Method doesn't exist, skip
                    }
                }
                if (goldBagManager != null) {
                    // Clear gold bags if method exists
                    try {
                        java.lang.reflect.Method clearMethod = goldBagManager.getClass().getMethod("clear");
                        clearMethod.invoke(goldBagManager);
                    } catch (Exception e) {
                        // Method doesn't exist, skip
                    }
                }
                
                // Reinitialize trees from reset level
                if (towerManager != null) {
                    deadTrees = towerManager.findDeadTrees(level);
                    liveTrees = towerManager.findLiveTrees(level);
                }
            }
            
            setChanged();
            notifyObservers("gameStateReset");
            
            System.out.println("Game state reset successfully");
        } catch (Exception e) {
            System.err.println("Failed to reset game state: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Create a GameSaveData object representing the current game state
     */
    private GameSaveData createGameSaveData() {
        GameSaveData saveData = new GameSaveData();
        
        // Core game state
        saveData.setGamePaused(gamePaused);
        saveData.setGameSpeedIncreased(gameSpeedIncreased);
        saveData.setGameSpeedMultiplier(gameSpeedMultiplier);
        saveData.setCurrentMapName(currentMapName);
        saveData.setCurrentDifficulty(currentDifficulty);
        
        // Game statistics
        saveData.setTotalEnemiesSpawned(totalEnemiesSpawned);
        saveData.setEnemiesReachedEnd(enemiesReachedEnd);
        saveData.setEnemyDefeated(enemyDefeated);
        saveData.setTotalDamage(totalDamage);
        saveData.setTimePlayedInSeconds(timePlayedInSeconds);
        saveData.setGameTimeMillis(gameTimeMillis);
        
        // Castle health
        saveData.setCastleMaxHealth(castleMaxHealth);
        saveData.setCastleCurrentHealth(castleCurrentHealth);
        
        // Level data
        saveData.setLevel(deepCopy2DArray(level));
        saveData.setOverlay(deepCopy2DArray(overlay));
        
        // Manager states (if managers are available) - basic version
        if (managersInitialized()) {
            // Save only basic state information
            saveData.setPlayerData(createPlayerSaveData());
            saveData.setWaveData(createWaveSaveData());
        }
        
        return saveData;
    }
    
    /**
     * Create simple player save data
     */
    private Object createPlayerSaveData() {
        if (playerManager == null) return null;
        
        java.util.Map<String, Object> playerData = new java.util.HashMap<>();
        playerData.put("gold", playerManager.getGold());
        playerData.put("health", playerManager.getHealth());
        playerData.put("shield", playerManager.getShield());
        return playerData;
    }
    
    /**
     * Create simple wave save data
     */
    private Object createWaveSaveData() {
        if (waveManager == null) return null;
        
        java.util.Map<String, Object> waveData = new java.util.HashMap<>();
        waveData.put("currentWaveIndex", waveManager.getWaveIndex());
        // Note: Additional wave state can be added when methods are available
        return waveData;
    }
    
    /**
     * Apply loaded game state data to the current game
     */
    private void applyGameSaveData(GameSaveData saveData) {
        // Core game state
        gamePaused = saveData.isGamePaused();
        gameSpeedIncreased = saveData.isGameSpeedIncreased();
        gameSpeedMultiplier = saveData.getGameSpeedMultiplier();
        currentMapName = saveData.getCurrentMapName();
        currentDifficulty = saveData.getCurrentDifficulty();
        
        // Game statistics
        totalEnemiesSpawned = saveData.getTotalEnemiesSpawned();
        enemiesReachedEnd = saveData.getEnemiesReachedEnd();
        enemyDefeated = saveData.getEnemyDefeated();
        totalDamage = saveData.getTotalDamage();
        timePlayedInSeconds = saveData.getTimePlayedInSeconds();
        gameTimeMillis = saveData.getGameTimeMillis();
        
        // Castle health
        castleMaxHealth = saveData.getCastleMaxHealth();
        castleCurrentHealth = saveData.getCastleCurrentHealth();
        
        // Level data
        if (saveData.getLevel() != null) {
            level = deepCopy2DArray(saveData.getLevel());
        }
        if (saveData.getOverlay() != null) {
            overlay = deepCopy2DArray(saveData.getOverlay());
        }
        
        // Apply manager states (if managers are available) - basic version  
        if (managersInitialized()) {
            if (saveData.getPlayerData() != null) {
                applyPlayerSaveData(saveData.getPlayerData());
            }
            if (saveData.getWaveData() != null) {
                applyWaveSaveData(saveData.getWaveData());
            }
        }
    }
    
    /**
     * Apply player save data
     */
    @SuppressWarnings("unchecked")
    private void applyPlayerSaveData(Object playerDataObj) {
        if (playerManager == null || playerDataObj == null) return;
        
        try {
            java.util.Map<String, Object> playerData = (java.util.Map<String, Object>) playerDataObj;
            if (playerData.containsKey("gold")) {
                playerManager.setGold((Integer) playerData.get("gold"));
            }
            if (playerData.containsKey("health")) {
                playerManager.setHealth((Integer) playerData.get("health"));
            }
            if (playerData.containsKey("shield")) {
                playerManager.setShield((Integer) playerData.get("shield"));
            }
        } catch (Exception e) {
            System.err.println("Failed to apply player save data: " + e.getMessage());
        }
    }
    
    /**
     * Apply wave save data  
     */
    @SuppressWarnings("unchecked")
    private void applyWaveSaveData(Object waveDataObj) {
        if (waveManager == null || waveDataObj == null) return;
        
        try {
            java.util.Map<String, Object> waveData = (java.util.Map<String, Object>) waveDataObj;
            // Wave manager state restoration can be implemented based on available methods
            // For now, we'll skip complex wave state restoration
            System.out.println("Wave save data available but detailed restoration not yet implemented");
        } catch (Exception e) {
            System.err.println("Failed to apply wave save data: " + e.getMessage());
        }
    }
    
    // ================ PROJECTILE MANAGER ABSTRACTION ================
    
    /**
     * Create a new projectile - abstracted method for clean MVC separation
     * @param shooter The object shooting the projectile (Tower or Warrior)
     * @param target The target enemy
     */
    public void createProjectile(Object shooter, Enemy target) {
        if (projectileManager != null) {
            projectileManager.newProjectile(shooter, target);
            setChanged();
            notifyObservers("projectileCreated");
        }
    }
    
    /**
     * Clear all projectiles from the game
     */
    public void clearProjectiles() {
        if (projectileManager != null) {
            projectileManager.clearProjectiles();
            setChanged();
            notifyObservers("projectillesCleared");
        }
    }
    
    /**
     * Get the current number of active projectiles
     * @return Number of active projectiles
     */
    public int getActiveProjectileCount() {
        if (projectileManager != null) {
            // Use size of projectiles list if available
            try {
                java.lang.reflect.Method getProjectilesMethod = projectileManager.getClass().getMethod("getProjectiles");
                Object projectiles = getProjectilesMethod.invoke(projectileManager);
                if (projectiles instanceof java.util.List) {
                    return ((java.util.List<?>) projectiles).size();
                }
            } catch (Exception e) {
                // Method not available, return 0
            }
        }
        return 0;
    }
    
    /**
     * Check if there are any active projectiles
     * @return true if there are active projectiles, false otherwise
     */
    public boolean hasActiveProjectiles() {
        return getActiveProjectileCount() > 0;
    }

    public StoneMiningManager getStoneMiningManager() {
        return stoneMiningManager;
    }

    public void setStoneMiningManager(StoneMiningManager stoneMiningManager) {
        this.stoneMiningManager = stoneMiningManager;
    }
} 