package models;

import static constants.GameDimensions.GAME_HEIGHT;
import static constants.GameDimensions.GAME_WIDTH;

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
import skills.SkillTree;
import stats.GameStatsRecord;
import ui_p.DeadTree;
import ui_p.LiveTree;
import ui_p.MineableStone;

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

    // Save system state tracking
    private boolean isNewGame = true; // true if started as new game, false if loaded
    private String loadedSaveFileName = null; // the filename of the loaded save, null if new game

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

    // Wave-start tracking for save/load
    private int waveStartGold = 0;

    // Game-start tracking for save/load
    private int gameStartHealth = 0;
    private int gameStartShield = 0;

    // Wave-start tracking for health and shield
    private int waveStartHealth = 0;
    private int waveStartShield = 0;

    // Wave-start tracking for weather state
    private Object waveStartWeatherData = null;

    // Wave-start tracking for tower states
    private java.util.List<GameStateMemento.TowerState> waveStartTowerStates = new java.util.ArrayList<>();

    // Wave-start tracking for tree states
    private java.util.List<DeadTree> waveStartDeadTrees = new java.util.ArrayList<>();
    private java.util.List<LiveTree> waveStartLiveTrees = new java.util.ArrayList<>();

    // Flag to prevent overwriting wave start states during save loading
    private boolean isLoadingFromSave = false;

    // Castle health
    private int castleMaxHealth;
    private int castleCurrentHealth;

    // Game entities (these will be managed by the controllers)
    private List<DeadTree> deadTrees;
    private List<LiveTree> liveTrees;
    private List<MineableStone> mineableStones = new ArrayList<>();
    private Tower displayedTower;
    private DeadTree selectedDeadTree;
    private Warrior pendingWarriorPlacement = null;

    // Victory confetti animation
    private ui_p.ConfettiAnimation victoryConfetti = null;
    private int lastEnemyDeathX = -1;
    private int lastEnemyDeathY = -1;

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
        stoneMiningManager = StoneMiningManager.getInstance(this);

        // Mark as new game by default
        markAsNewGame();
    }

    public PlayingModel(TileManager tileManager) {
        this.tileManager = tileManager;
        this.gameOptions = loadOptionsOrDefault();
        this.gameStateManager = new GameStateManager();
        loadDefaultLevel();
        // Managers will be initialized by the controller

        // Mark as new game by default
        markAsNewGame();
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

        // Mark as new game by default
        markAsNewGame();
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
        if (ultiManager != null) ultiManager.update(gameTimeMillis, gameSpeedMultiplier);
        if (weatherManager != null) {
            // Apply speed multiplier to weather system for faster day/night cycles
            weatherManager.update(deltaTimeSeconds * gameSpeedMultiplier);
        }

        if (tileManager != null && weatherManager != null) {
            tileManager.updateSnowTransition(deltaTimeSeconds, weatherManager.isSnowing());
        }

        // Check enemy status and handle wave completion
        if (isAllEnemiesDead()) {
            if (waveManager.isThereMoreWaves()) {
                // Just let WaveManager handle the wave timing and progression
            } else if (waveManager.isAllWavesFinished()) {
                // Last wave completed, all enemies dead - trigger confetti first if not already triggered
                if (victoryConfetti == null) {
                    triggerVictoryConfetti();
                }
                // Then handle victory after confetti is finished
                if (victoryConfetti != null && victoryConfetti.isFinished()) {
                    handleVictory();
                }
            }
        }

        // Update other game elements
        if (enemyManager != null) enemyManager.update(gameSpeedMultiplier);
        if (towerManager != null) towerManager.update(gameSpeedMultiplier);

        if (playerManager != null && !playerManager.isAlive()) {
            handleGameOver();
        }

        if (goldBagManager != null) goldBagManager.update(gameSpeedMultiplier);

        if (stoneMiningManager != null) {
            stoneMiningManager.update();
        }

        // Update victory confetti animation
        if (victoryConfetti != null) {
            victoryConfetti.update();
        }

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

        SkillTree.getInstance().resetAllSkills();

        // play a random victory sound
        AudioManager.getInstance().playRandomVictorySound();

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

        // Play a random lose sound
        AudioManager.getInstance().playRandomLoseSound();

        // stop any ongoing waves/spawning
        if (enemyManager != null) enemyManager.getEnemies().clear();

        SkillTree.getInstance().resetAllSkills();

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

    /**
     * Called when an enemy dies - tracks location for potential confetti
     */
    public void enemyDiedAt(int x, int y) {
        incrementEnemyDefeated();
        // Always update last death location (the final one will be used for confetti)
        lastEnemyDeathX = x;
        lastEnemyDeathY = y;
    }

    /**
     * Triggers the victory confetti animation at the last enemy death location
     */
    private void triggerVictoryConfetti() {
        if (lastEnemyDeathX >= 0 && lastEnemyDeathY >= 0) {
            victoryConfetti = new ui_p.ConfettiAnimation(lastEnemyDeathX, lastEnemyDeathY);
            System.out.println("🎉 Victory confetti triggered at (" + lastEnemyDeathX + ", " + lastEnemyDeathY + ")!");
        } else {
            System.out.println("❌ Cannot trigger confetti - no enemy death location recorded!");
        }
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

    public void setWeatherManager(WeatherManager weatherManager) {
        this.weatherManager = weatherManager;
        setChanged();
        notifyObservers("weatherManagerSet");
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
            if (gameStateManager == null) {
                System.err.println("GameStateManager is null, cannot save game state");
                return false;
            }

            String saveFileName;

            if (isNewGame) {
                // New game: create new save file with incremented number
                saveFileName = generateNewSaveFileName(currentMapName);
                System.out.println("New game save: Creating new save file: " + saveFileName);
            } else {
                // Loaded game: save to the same file that was loaded
                saveFileName = loadedSaveFileName != null ? loadedSaveFileName : filename;
                System.out.println("Loaded game save: Saving to original file: " + saveFileName);
            }

            GameStateMemento memento = createGameStateMemento();
            gameStateManager.saveGameState(memento, saveFileName);
            System.out.println("Game state saved successfully as: " + saveFileName);
            return true;
        } catch (Exception e) {
            System.err.println("Failed to save game state: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Generates a new save file name with incremented save number
     * Format: (levelName)_saveno_(number)
     * @param levelName The base level name
     * @return New save file name with incremented number
     */
    private String generateNewSaveFileName(String levelName) {
        if (gameStateManager == null) {
            return levelName + "_saveno_1";
        }

        int highestSaveNumber = 0;

        // Find the highest existing save number for this level
        for (int i = 1; i <= 100; i++) { // Check up to 100 save slots
            String testFileName = levelName + "_saveno_" + i;
            if (gameStateManager.saveFileExists(testFileName)) {
                highestSaveNumber = i;
            }
        }

        // Return the next available save number
        return levelName + "_saveno_" + (highestSaveNumber + 1);
    }

    /**
     * Load a game state from a file
     * @param filename The name of the save file
     * @return true if load was successful, false otherwise
     */
    public boolean loadGameState(String filename) {
        try {
            if (gameStateManager == null) {
                System.err.println("GameStateManager is null, cannot load game state");
                return false;
            }

            GameStateMemento memento = gameStateManager.loadGameState(filename);
            if (memento == null) {
                System.err.println("Failed to load game state: " + filename);
                return false;
            }

            applyGameStateMemento(memento);

            // Mark this as a loaded game and remember the save file name
            isNewGame = false;
            loadedSaveFileName = filename;
            System.out.println("Game marked as loaded from: " + filename);

            setChanged();
            notifyObservers("gameStateLoaded");
            System.out.println("Game state loaded successfully: " + filename);
            return true;
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
            // Reload GameOptions to ensure we have the current difficulty settings
            this.gameOptions = loadOptionsOrDefault();
            System.out.println("=== RESET GAME STATE DEBUG ===");
            System.out.println("Reloaded GameOptions. Starting gold: " + gameOptions.getStartingGold());

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

            // Reset victory confetti animation
            victoryConfetti = null;
            lastEnemyDeathX = GAME_WIDTH/2;
            lastEnemyDeathY = GAME_HEIGHT/2;

            // Reset managers if they exist
            if (managersInitialized()) {
                // Reset player manager resources
                if (playerManager != null) {
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
                    System.out.println("Enemy manager cleared");
                }
                if (projectileManager != null) {
                    projectileManager.clearProjectiles();
                    System.out.println("Projectile manager cleared");
                }
                if (waveManager != null) {
                    waveManager.resetWaveManager();
                    System.out.println("Wave manager reset successfully");
                }
                if (towerManager != null) {
                    towerManager.clearTowers();
                    towerManager.clearWarriors();
                    System.out.println("Tower manager and warriors cleared");
                }

                if (ultiManager != null) {
                    ultiManager.reset();
                    System.out.println("Ultimate manager reset successfully");
                }

                if (goldBagManager != null) {
                    goldBagManager.clear();
                    System.out.println("Gold bag manager cleared");
                }

                if (fireAnimationManager != null) {
                    fireAnimationManager.clear();
                    System.out.println("Fire animation manager cleared");
                }

                if (weatherManager != null) {
                    weatherManager.reset();
                    System.out.println("Weather manager reset");
                }


                if (stoneMiningManager != null) {
                    stoneMiningManager.reset();
                    System.out.println("Stone mining manager reset");
                }

                if (towerManager != null) {
                    deadTrees = towerManager.findDeadTrees(level);
                    liveTrees = towerManager.findLiveTrees(level);
                }
            }

            // Ekonomi skilleri için başlangıç bonusunu uygula
            initializeGame();

            // Mark as new game when resetting
            markAsNewGame();

            setChanged();
            notifyObservers("gameStateReset");

            System.out.println("Game state reset successfully");
        } catch (Exception e) {
            System.err.println("Failed to reset game state: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Marks the current game as a new game (not loaded from save)
     * This should be called when starting a fresh game
     */
    public void markAsNewGame() {
        isNewGame = true;
        loadedSaveFileName = null;
        System.out.println("Game marked as new game");
    }

    /**
     * Create a GameStateMemento object representing the current game state for JSON save
     */
    private GameStateMemento createGameStateMemento() {
        // Use wave start gold instead of default starting gold
        int gold = waveStartGold > 0 ? waveStartGold : (gameOptions != null ? gameOptions.getStartingGold() : 0);

        // Use wave start health and shield values
        int health = waveStartHealth > 0 ? waveStartHealth :
                (playerManager != null ? playerManager.getHealth() :
                        (gameOptions != null ? gameOptions.getStartingPlayerHP() : castleCurrentHealth));
        int shield = waveStartShield > 0 ? waveStartShield :
                (playerManager != null ? playerManager.getShield() :
                        (gameOptions != null ? gameOptions.getStartingShield() : 0));

        // Save current wave state (not reset to beginning)
        int waveIndex = waveManager != null ? waveManager.getWaveIndex() : 0;
        int groupIndex = waveManager != null ? waveManager.getCurrentGroupIndex() : 0;

        // Use tower states that were captured at wave start
        java.util.List<GameStateMemento.TowerState> towerStates = waveStartTowerStates;

        // No enemies for round start - empty enemy states
        java.util.List<GameStateMemento.EnemyState> enemyStates = new java.util.ArrayList<>();

        // Get skills that were selected at round start
        java.util.Set<skills.SkillType> selectedSkills = skills.SkillTree.getInstance().getSelectedSkills();

        // Get wave start weather state instead of current weather state
        Object weatherData = waveStartWeatherData != null ? waveStartWeatherData :
                (weatherManager != null ? weatherManager.getWeatherState() : null);

        // Create tree states from CURRENT trees (not wave start trees) to preserve burned/built states
        java.util.List<GameStateMemento.TreeState> deadTreeStatesToSave = createTreeStatesFromDeadTrees(deadTrees);
        java.util.List<GameStateMemento.TreeState> liveTreeStatesToSave = createTreeStatesFromLiveTrees(liveTrees);

        System.out.println("Saving wave start values: Gold=" + gold + ", Health=" + health + ", Shield=" + shield + ", Wave=" + (waveIndex + 1));
        System.out.println("Saving tree states: " + deadTreeStatesToSave.size() + " dead trees, " + liveTreeStatesToSave.size() + " live trees");

        return new GameStateMemento(
                gold, health, shield, waveIndex, groupIndex,
                towerStates, enemyStates, gameOptions, currentDifficulty, selectedSkills, weatherData,
                deadTreeStatesToSave, liveTreeStatesToSave
        );
    }

    /**
     * Create tower states from current towers
     */
    private java.util.List<GameStateMemento.TowerState> createTowerStates() {
        java.util.List<GameStateMemento.TowerState> towerStates = new java.util.ArrayList<>();

        if (towerManager != null && towerManager.getTowers() != null) {
            for (Tower tower : towerManager.getTowers()) {
                GameStateMemento.TowerState towerState = new GameStateMemento.TowerState(
                        tower.getX(), tower.getY(), tower.getType(), tower.getLevel()
                );
                towerStates.add(towerState);
            }
        }

        return towerStates;
    }

    /**
     * Create tower states for wave start saves - includes targeting strategy and light information
     */
    private java.util.List<GameStateMemento.TowerState> createWaveStartTowerStates() {
        java.util.List<GameStateMemento.TowerState> towerStates = new java.util.ArrayList<>();

        if (towerManager != null && towerManager.getTowers() != null) {
            for (Tower tower : towerManager.getTowers()) {
                // Get targeting strategy name
                String targetingStrategy = tower.getTargetingStrategy().getStrategyName();

                // Check if tower has light upgrade
                boolean hasLight = tower instanceof objects.LightDecorator;

                // Create tower state with full information
                GameStateMemento.TowerState towerState = new GameStateMemento.TowerState(
                        tower.getX(), tower.getY(), tower.getType(), tower.getLevel(),
                        targetingStrategy, hasLight
                );
                towerStates.add(towerState);
            }
        }

        System.out.println("Saved " + towerStates.size() + " towers with targeting and light information for wave start");
        return towerStates;
    }

    /**
     * Create dead tree states for wave start saves - creates deep copies of current dead trees
     */
    private java.util.List<DeadTree> createWaveStartDeadTreeStates() {
        java.util.List<DeadTree> deadTreeStates = new java.util.ArrayList<>();

        if (deadTrees != null) {
            for (DeadTree deadTree : deadTrees) {
                // Create a new DeadTree with the same position
                DeadTree waveStartDeadTree = new DeadTree(deadTree.getX(), deadTree.getY());
                deadTreeStates.add(waveStartDeadTree);
            }
        }

        System.out.println("Captured " + deadTreeStates.size() + " dead trees for wave start");
        return deadTreeStates;
    }

    /**
     * Create live tree states for wave start saves - creates deep copies of current live trees
     */
    private java.util.List<LiveTree> createWaveStartLiveTreeStates() {
        java.util.List<LiveTree> liveTreeStates = new java.util.ArrayList<>();

        if (liveTrees != null) {
            for (LiveTree liveTree : liveTrees) {
                // Create a new LiveTree with the same position
                LiveTree waveStartLiveTree = new LiveTree(liveTree.getX(), liveTree.getY());
                liveTreeStates.add(waveStartLiveTree);
            }
        }

        System.out.println("Captured " + liveTreeStates.size() + " live trees for wave start");
        return liveTreeStates;
    }

    /**
     * Convert dead tree objects to tree states for saving
     */
    private java.util.List<GameStateMemento.TreeState> createTreeStatesFromDeadTrees(java.util.List<DeadTree> deadTreeList) {
        java.util.List<GameStateMemento.TreeState> treeStates = new java.util.ArrayList<>();

        if (deadTreeList != null) {
            for (DeadTree deadTree : deadTreeList) {
                GameStateMemento.TreeState treeState = new GameStateMemento.TreeState(deadTree.getX(), deadTree.getY());
                treeStates.add(treeState);
            }
        }

        return treeStates;
    }

    /**
     * Convert live tree objects to tree states for saving
     */
    private java.util.List<GameStateMemento.TreeState> createTreeStatesFromLiveTrees(java.util.List<LiveTree> liveTreeList) {
        java.util.List<GameStateMemento.TreeState> treeStates = new java.util.ArrayList<>();

        if (liveTreeList != null) {
            for (LiveTree liveTree : liveTreeList) {
                GameStateMemento.TreeState treeState = new GameStateMemento.TreeState(liveTree.getX(), liveTree.getY());
                treeStates.add(treeState);
            }
        }

        return treeStates;
    }

    /**
     * Convert tree states to dead tree objects for loading
     */
    private java.util.List<DeadTree> createDeadTreesFromTreeStates(java.util.List<GameStateMemento.TreeState> treeStates) {
        java.util.List<DeadTree> deadTrees = new java.util.ArrayList<>();

        if (treeStates != null) {
            for (GameStateMemento.TreeState treeState : treeStates) {
                DeadTree deadTree = new DeadTree(treeState.getX(), treeState.getY());
                deadTrees.add(deadTree);
            }
        }

        return deadTrees;
    }

    /**
     * Convert tree states to live tree objects for loading
     */
    private java.util.List<LiveTree> createLiveTreesFromTreeStates(java.util.List<GameStateMemento.TreeState> treeStates) {
        java.util.List<LiveTree> liveTrees = new java.util.ArrayList<>();

        if (treeStates != null) {
            for (GameStateMemento.TreeState treeState : treeStates) {
                LiveTree liveTree = new LiveTree(treeState.getX(), treeState.getY());
                liveTrees.add(liveTree);
            }
        }

        return liveTrees;
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
        waveData.put("currentGroupIndex", waveManager.getCurrentGroupIndex());
        // Note: Additional wave state can be added when methods are available
        return waveData;
    }

    /**
     * Create simple weather save data
     */
    private Object createWeatherSaveData() {
        if (weatherManager == null) return null;
        return weatherManager.getWeatherState();
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

        // Restore skills selected at the start of the game
        if (saveData.getSelectedSkills() != null) {
            skills.SkillTree.getInstance().setSelectedSkills(saveData.getSelectedSkills());
            System.out.println("Restored " + saveData.getSelectedSkills().size() + " skills from save data");
        } else {
            // Clear skills if none were saved (backward compatibility)
            System.out.println("No skills found in save data, cleared skill tree");
        }

        // Apply manager states (if managers are available) - basic version
        if (managersInitialized()) {
            if (saveData.getPlayerData() != null) {
                applyPlayerSaveData(saveData.getPlayerData());
            }
            if (saveData.getWaveData() != null) {
                applyWaveSaveData(saveData.getWaveData());
            }
            if (saveData.getWeatherData() != null) {
                applyWeatherSaveData(saveData.getWeatherData());
            }
        }
    }

    /**
     * Apply loaded game state memento to the current game
     */
    private void applyGameStateMemento(GameStateMemento memento) {
        // Set loading flag to prevent wave start from overwriting tower states
        isLoadingFromSave = true;

        // Set difficulty
        if (memento.getDifficulty() != null) {
            currentDifficulty = memento.getDifficulty();
        }

        // Restore skills selected at the start of the game
        if (memento.getSelectedSkills() != null && !memento.getSelectedSkills().isEmpty()) {
            skills.SkillTree.getInstance().setSelectedSkills(memento.getSelectedSkills());
            System.out.println("Restored " + memento.getSelectedSkills().size() + " skills from save data");
        }

        // Apply player state if managers are available
        if (managersInitialized()) {
            if (playerManager != null) {
                playerManager.setGold(memento.getGold());
                playerManager.setHealth(memento.getHealth());
                playerManager.setShield(memento.getShield());
                // Update wave start tracking values
                waveStartGold = memento.getGold();
                waveStartHealth = memento.getHealth();
                waveStartShield = memento.getShield();
                // Update game start tracking (for consistency)
                gameStartHealth = memento.getHealth();
                gameStartShield = memento.getShield();
                // Update wave start tower states
                waveStartTowerStates = memento.getTowerStates() != null ?
                        new java.util.ArrayList<>(memento.getTowerStates()) : new java.util.ArrayList<>();

                // Update wave start tree states
                if (memento.getDeadTreeStates() != null) {
                    waveStartDeadTrees = createDeadTreesFromTreeStates(memento.getDeadTreeStates());
                    System.out.println("Restored " + waveStartDeadTrees.size() + " dead trees for wave start tracking");
                } else {
                    waveStartDeadTrees = new java.util.ArrayList<>();
                }

                if (memento.getLiveTreeStates() != null) {
                    waveStartLiveTrees = createLiveTreesFromTreeStates(memento.getLiveTreeStates());
                    System.out.println("Restored " + waveStartLiveTrees.size() + " live trees for wave start tracking");
                } else {
                    waveStartLiveTrees = new java.util.ArrayList<>();
                }

                // Also restore current game tree states to match the loaded wave start states
                deadTrees = createDeadTreesFromTreeStates(memento.getDeadTreeStates());
                liveTrees = createLiveTreesFromTreeStates(memento.getLiveTreeStates());
                System.out.println("Restored current game tree states: " +
                        (deadTrees != null ? deadTrees.size() : 0) + " dead trees, " +
                        (liveTrees != null ? liveTrees.size() : 0) + " live trees");
                System.out.println("Restored player state: Gold=" + memento.getGold() +
                        ", Health=" + memento.getHealth() +
                        ", Shield=" + memento.getShield());
                System.out.println("Wave start tracking updated: Gold=" + waveStartGold + ", Health=" + waveStartHealth + ", Shield=" + waveStartShield);
            }

            // Apply wave state
            if (waveManager != null) {
                waveManager.restoreWaveState(memento.getWaveIndex(), memento.getGroupIndex());
                System.out.println("Restored wave state: Wave=" + memento.getWaveIndex() +
                        ", Group=" + memento.getGroupIndex());
            }

            // Apply weather state
            if (weatherManager != null && memento.getWeatherData() != null) {
                try {
                    @SuppressWarnings("unchecked")
                    java.util.Map<String, Object> weatherState = (java.util.Map<String, Object>) memento.getWeatherData();

                    // Prepare weather manager for loading to override any random initialization
                    weatherManager.prepareForLoading();

                    // Restore the saved weather state
                    weatherManager.restoreWeatherState(weatherState);

                    // Update wave start weather tracking
                    waveStartWeatherData = memento.getWeatherData();
                    System.out.println("Restored weather state from save data");
                    System.out.println("Wave start weather tracking updated");
                } catch (Exception e) {
                    System.err.println("Failed to restore weather state: " + e.getMessage());
                }
            }

            // Apply tower states
            if (towerManager != null && memento.getTowerStates() != null) {
                restoreTowerStates(memento.getTowerStates());
            }
        }

        // Update castle health to match player health
        castleCurrentHealth = memento.getHealth();

        // Clear loading flag after everything is restored
        isLoadingFromSave = false;

        System.out.println("Game state memento applied successfully");
    }

    /**
     * Restore tower states from saved data
     */
    private void restoreTowerStates(java.util.List<GameStateMemento.TowerState> towerStates) {
        if (towerManager == null) {
            System.err.println("Cannot restore towers: TowerManager is null");
            return;
        }

        // Clear existing towers first
        towerManager.clearTowers();

        int restoredCount = 0;
        for (GameStateMemento.TowerState towerState : towerStates) {
            try {
                // Convert strategy name to strategy instance
                strategies.TargetingStrategy targetingStrategy = convertStrategyNameToStrategy(towerState.getTargetingStrategy());

                // Create tower based on type
                Tower tower = createTowerFromState(towerState, targetingStrategy);

                if (tower != null) {
                    // Handle tower upgrades (level 2)
                    if (towerState.getLevel() == 2) {
                        tower = tower.upgrade();
                    }

                    // Handle light upgrades
                    if (towerState.hasLight()) {
                        objects.LightDecorator lightTower = towerManager.upgradeTowerWithLight(tower);
                        if (lightTower != null) {
                            tower = lightTower;
                        }
                    }

                    // Add the tower to the manager
                    towerManager.addTower(tower);

                    // Update tile data to reflect tower placement
                    updateTileDataForTower(tower);

                    // Remove any dead tree at this position
                    removeDeadTreeAtPosition(tower.getX(), tower.getY());

                    restoredCount++;

                    System.out.println("Restored tower at (" + towerState.getX() + "," + towerState.getY() +
                            ") Type=" + towerState.getType() + " Level=" + towerState.getLevel() +
                            " Strategy=" + towerState.getTargetingStrategy() + " Light=" + towerState.hasLight());
                }
            } catch (Exception e) {
                System.err.println("Failed to restore tower at (" + towerState.getX() + "," + towerState.getY() + "): " + e.getMessage());
                e.printStackTrace();
            }
        }

        System.out.println("Successfully restored " + restoredCount + " out of " + towerStates.size() + " towers from save data");
    }

    /**
     * Convert strategy name to TargetingStrategy instance
     */
    private strategies.TargetingStrategy convertStrategyNameToStrategy(String strategyName) {
        if (strategyName == null) strategyName = "First";

        strategies.TargetingStrategyFactory.StrategyType strategyType;
        switch (strategyName) {
            case "First": strategyType = strategies.TargetingStrategyFactory.StrategyType.FIRST; break;
            case "Last": strategyType = strategies.TargetingStrategyFactory.StrategyType.LAST; break;
            case "Strongest": strategyType = strategies.TargetingStrategyFactory.StrategyType.STRONGEST; break;
            case "Weakest": strategyType = strategies.TargetingStrategyFactory.StrategyType.WEAKEST; break;
            default:
                System.out.println("Unknown targeting strategy: " + strategyName + ", defaulting to First");
                strategyType = strategies.TargetingStrategyFactory.StrategyType.FIRST;
        }

        return strategies.TargetingStrategyFactory.createStrategy(strategyType);
    }

    /**
     * Create tower from saved state with proper targeting strategy
     */
    private Tower createTowerFromState(GameStateMemento.TowerState towerState, strategies.TargetingStrategy targetingStrategy) {
        int x = towerState.getX();
        int y = towerState.getY();
        int type = towerState.getType();

        switch (type) {
            case constants.Constants.Towers.ARCHER: // Archer Tower
                return new objects.ArcherTower(x, y, targetingStrategy);
            case constants.Constants.Towers.ARTILLERY: // Artillery Tower
                return new objects.ArtilleryTower(x, y, targetingStrategy);
            case constants.Constants.Towers.MAGE: // Mage Tower
                return new objects.MageTower(x, y, targetingStrategy);
            case constants.Constants.Towers.POISON: // Poison Tower (no targeting strategy needed)
                return new objects.PoisonTower(x, y);
            default:
                System.err.println("Unknown tower type: " + type);
                return null;
        }
    }

    /**
     * Update tile data to reflect tower placement (removes dead trees)
     */
    private void updateTileDataForTower(Tower tower) {
        if (tower == null || level == null) return;

        // Convert tower pixel coordinates to tile coordinates
        int tileX = tower.getX() / constants.GameDimensions.TILE_DISPLAY_SIZE;
        int tileY = tower.getY() / constants.GameDimensions.TILE_DISPLAY_SIZE;

        // Check bounds
        if (tileY >= 0 && tileY < level.length && tileX >= 0 && tileX < level[0].length) {
            // Set the appropriate tile ID based on tower type
            int tileId;
            switch (tower.getType()) {
                case constants.Constants.Towers.ARCHER:
                    tileId = 26; // Archer Tower tile ID
                    break;
                case constants.Constants.Towers.ARTILLERY:
                    tileId = 21; // Artillery Tower tile ID
                    break;
                case constants.Constants.Towers.MAGE:
                    tileId = 20; // Mage Tower tile ID
                    break;
                case constants.Constants.Towers.POISON:
                    tileId = 39; // Poison Tower tile ID
                    break;
                default:
                    System.err.println("Unknown tower type for tile update: " + tower.getType());
                    return;
            }

            level[tileY][tileX] = tileId;

            // Clear overlay data to remove dead trees or other overlays
            if (overlay != null && tileY < overlay.length && tileX < overlay[0].length) {
                overlay[tileY][tileX] = 0; // NO_OVERLAY = 0
                System.out.println("Cleared overlay at (" + tileX + ", " + tileY + ")");
            }

            System.out.println("Updated tile at (" + tileX + ", " + tileY + ") to tower tile ID: " + tileId);
        }
    }

    /**
     * Remove dead tree at the specified pixel position
     */
    private void removeDeadTreeAtPosition(int pixelX, int pixelY) {
        if (deadTrees == null) return;

        // Remove any dead tree at this exact position
        deadTrees.removeIf(deadTree -> deadTree.getX() == pixelX && deadTree.getY() == pixelY);

        System.out.println("Removed dead tree at pixel position (" + pixelX + ", " + pixelY + ")");
    }

    /**
     * Update wave start tower states to reflect current tower configuration
     * This should be called when towers are sold/removed during gameplay
     */
    public void updateWaveStartTowerStates() {
        if (towerManager != null) {
            waveStartTowerStates = createWaveStartTowerStates();
            System.out.println("Updated wave start tower states - now tracking " + waveStartTowerStates.size() + " towers");

            // Debug: Print current tower states
            for (GameStateMemento.TowerState towerState : waveStartTowerStates) {
                System.out.println("  - Tower at (" + towerState.getX() + "," + towerState.getY() +
                        ") Type=" + towerState.getType() + " Level=" + towerState.getLevel() +
                        " Strategy=" + towerState.getTargetingStrategy() + " Light=" + towerState.hasLight());
            }
        }
    }

    /**
     * Update wave start tree states to reflect current tree configuration
     * This should be called when trees are burned or towers are built on dead trees during gameplay
     */
    public void updateWaveStartTreeStates() {
        waveStartDeadTrees = createWaveStartDeadTreeStates();
        waveStartLiveTrees = createWaveStartLiveTreeStates();
        System.out.println("Updated wave start tree states - now tracking " + waveStartDeadTrees.size() + " dead trees and " + waveStartLiveTrees.size() + " live trees");
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

    /**
     * Apply weather save data
     */
    @SuppressWarnings("unchecked")
    private void applyWeatherSaveData(Object weatherDataObj) {
        if (weatherManager == null || weatherDataObj == null) return;

        try {
            java.util.Map<String, Object> weatherData = (java.util.Map<String, Object>) weatherDataObj;
            weatherManager.restoreWeatherState(weatherData);
            System.out.println("Restored weather state from save data");
        } catch (Exception e) {
            System.err.println("Failed to apply weather save data: " + e.getMessage());
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
    public List<MineableStone> getMineableStones() {
        return mineableStones;
    }

    private void initializeGame() {
        // Apply starting gold bonus from skill tree
        int startingGoldBonus = SkillTree.getInstance().getStartingGold();
        if (playerManager != null) {
            playerManager.addGold(startingGoldBonus);
            // Track the gold at the start of the first wave
            waveStartGold = playerManager.getGold();
            // Track the health and shield at game start
            gameStartHealth = playerManager.getHealth();
            gameStartShield = playerManager.getShield();
            System.out.println("Game initialized - Wave start gold tracked: " + waveStartGold);
            System.out.println("Game initialized - Health tracked: " + gameStartHealth + ", Shield tracked: " + gameStartShield);
        }
    }

    public void addGold(int amount) {
        if (playerManager != null) {
            playerManager.addGold(amount);
        }
    }

    public void onWaveComplete() {
        // Apply interest from skill tree
        if (playerManager != null) {
            int currentGold = playerManager.getGold();
            System.out.println("Wave complete - Current gold: " + currentGold);
            int interest = SkillTree.getInstance().calculateInterest(currentGold);
            if (interest > 0) {
                playerManager.addGold(interest);
                System.out.println("Interest earned: " + interest + " gold. New total: " + playerManager.getGold());
            } else {
                System.out.println("No interest earned this wave");
            }
        } else {
            System.out.println("PlayerManager is null, cannot calculate interest");
        }
    }

    /**
     * Called when a wave starts to track the gold at wave start
     */
    public void onWaveStart() {
        if (playerManager != null) {
            waveStartGold = playerManager.getGold();
            waveStartHealth = playerManager.getHealth();
            waveStartShield = playerManager.getShield();
            System.out.println("Wave started - Tracking gold at wave start: " + waveStartGold);
            System.out.println("Wave started - Tracking health/shield at wave start: " + waveStartHealth + "/" + waveStartShield);
        } else {
            waveStartGold = gameOptions != null ? gameOptions.getStartingGold() : 0;
            waveStartHealth = gameOptions != null ? gameOptions.getStartingPlayerHP() : 0;
            waveStartShield = gameOptions != null ? gameOptions.getStartingShield() : 0;
            System.out.println("PlayerManager is null, using default starting values: Gold=" + waveStartGold + ", Health=" + waveStartHealth + ", Shield=" + waveStartShield);
        }

        // Track weather state at wave start
        if (weatherManager != null) {
            waveStartWeatherData = weatherManager.getWeatherState();
            System.out.println("Wave started - Tracking weather state at wave start");
        } else {
            waveStartWeatherData = null;
            System.out.println("WeatherManager is null, no weather state tracked");
        }

        // Track tower states at wave start (but don't overwrite when loading from save)
        if (!isLoadingFromSave) {
            waveStartTowerStates = createWaveStartTowerStates();
            System.out.println("Wave started - Captured " + waveStartTowerStates.size() + " tower states at wave start");
        } else {
            System.out.println("Wave started - Preserving loaded tower states (" + waveStartTowerStates.size() + " towers) from save file");
        }

        // Track tree states at wave start (but don't overwrite when loading from save)
        if (!isLoadingFromSave) {
            waveStartDeadTrees = createWaveStartDeadTreeStates();
            waveStartLiveTrees = createWaveStartLiveTreeStates();
            System.out.println("Wave started - Captured " + waveStartDeadTrees.size() + " dead trees and " + waveStartLiveTrees.size() + " live trees at wave start");
        } else {
            System.out.println("Wave started - Preserving loaded tree states (" + waveStartDeadTrees.size() + " dead trees, " + waveStartLiveTrees.size() + " live trees) from save file");
        }
    }

    /**
     * Get the gold value at wave start (for saving)
     */
    public int getWaveStartGold() {
        return waveStartGold;
    }

    /**
     * Set the gold value at wave start (for loading)
     */
    public void setWaveStartGold(int gold) {
        this.waveStartGold = gold;
    }

    /**
     * Get the health value at game start (for saving)
     */
    public int getGameStartHealth() {
        return gameStartHealth;
    }

    /**
     * Set the health value at game start (for loading)
     */
    public void setGameStartHealth(int health) {
        this.gameStartHealth = health;
    }

    /**
     * Get the shield value at game start (for saving)
     */
    public int getGameStartShield() {
        return gameStartShield;
    }

    /**
     * Set the shield value at game start (for loading)
     */
    public void setGameStartShield(int shield) {
        this.gameStartShield = shield;
    }

    /**
     * Get the current victory confetti animation (for rendering)
     */
    public ui_p.ConfettiAnimation getVictoryConfetti() {
        return victoryConfetti;
    }

    /**
     * Get the health value at wave start (for saving)
     */
    public int getWaveStartHealth() {
        return waveStartHealth;
    }

    /**
     * Set the health value at wave start (for loading)
     */
    public void setWaveStartHealth(int health) {
        this.waveStartHealth = health;
    }

    /**
     * Get the shield value at wave start (for saving)
     */
    public int getWaveStartShield() {
        return waveStartShield;
    }

    /**
     * Set the shield value at wave start (for loading)
     */
    public void setWaveStartShield(int shield) {
        this.waveStartShield = shield;
    }

    /**
     * Get the weather data at wave start (for saving)
     */
    public Object getWaveStartWeatherData() {
        return waveStartWeatherData;
    }

    /**
     * Set the weather data at wave start (for loading)
     */
    public void setWaveStartWeatherData(Object weatherData) {
        this.waveStartWeatherData = weatherData;
    }

    /**
     * Get the dead trees at wave start (for saving)
     */
    public java.util.List<DeadTree> getWaveStartDeadTrees() {
        return waveStartDeadTrees;
    }

    /**
     * Set the dead trees at wave start (for loading)
     */
    public void setWaveStartDeadTrees(java.util.List<DeadTree> deadTrees) {
        this.waveStartDeadTrees = deadTrees;
    }

    /**
     * Get the live trees at wave start (for saving)
     */
    public java.util.List<LiveTree> getWaveStartLiveTrees() {
        return waveStartLiveTrees;
    }

    /**
     * Set the live trees at wave start (for loading)
     */
    public void setWaveStartLiveTrees(java.util.List<LiveTree> liveTrees) {
        this.waveStartLiveTrees = liveTrees;
    }

}