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
    
    // Initialization flag
    private boolean isFirstReset = true;
    
    public PlayingModel() {
        this.tileManager = new TileManager();
        this.gameOptions = loadOptionsOrDefault();
        this.gameStateManager = new GameStateManager();
        loadDefaultLevel();
        // Managers will be initialized by the controller
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
        notifyObservers("pendingWarriorChanged");
    }
} 