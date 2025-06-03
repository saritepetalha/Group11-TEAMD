package scenes;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Composite;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import config.GameOptions;
import constants.Constants;
import constants.GameDimensions;
import enemies.Enemy;
import helpMethods.LoadSave;
import helpMethods.OptionsIO;
import main.Game;
import managers.*;
import objects.ArcherTower;
import objects.ArtilleryTower;
import objects.MageTower;
import objects.Tower;
import objects.UpgradedArcherTower;
import objects.UpgradedArtilleryTower;
import objects.UpgradedMageTower;
import stats.GameStatsRecord;
import ui_p.DeadTree;
import ui_p.LiveTree;
import ui_p.PlayingUI;
import ui_p.TowerSelectionUI;
import objects.Warrior;
import java.awt.Toolkit;
import java.awt.Image;
import java.awt.Cursor;
import java.awt.Point;
import javax.swing.JPanel;
import java.awt.RenderingHints;

public class Playing extends GameScene implements SceneMethods {
    private int[][] level;
    private int[][] overlay;
    private int[][] originalLevelData;
    private int[][] originalOverlayData;

    private PlayingUI playingUI;
    private int mouseX, mouseY;
    private List<DeadTree> deadTrees;
    private List<LiveTree> liveTrees;
    private WaveManager waveManager;
    private TowerManager towerManager;
    private TileManager tileManager;
    private PlayerManager playerManager;
    private ProjectileManager projectileManager;

    private UltiManager ultiManager;
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

    private int totalEnemiesSpawned = 0;
    private int enemiesReachedEnd = 0;
    private int enemyDefeated = 0;
    private int totalDamage = 0;
    private int timePlayedInSeconds = 0;

    private int updateCounter = 0;

    private GoldBagManager goldBagManager;

    // Add field to store upgrade button bounds
    private Rectangle upgradeButtonBounds = null;

    // Border images
    private BufferedImage wallImage;
    private BufferedImage gateImage;

    private int castleMaxHealth;
    private int castleCurrentHealth;
    private static final int CASTLE_HEALTH_BAR_WIDTH = 100;
    private static final int CASTLE_HEALTH_BAR_HEIGHT = 10;
    private static final int CASTLE_HEALTH_BAR_X = 50;
    private static final int CASTLE_HEALTH_BAR_Y = 50;

    private GameStateManager gameStateManager;
    private String currentMapName = "defaultlevel"; // Default map name
    private boolean isFirstReset = true;

    private TowerSelectionUI towerSelectionUI;

    private long gameTimeMillis = 0;

    private WeatherManager weatherManager;

    private Warrior pendingWarriorPlacement = null;

    private JPanel gamePanel;

    private BufferedImage spawnPointIndicator;

    public Playing(Game game, JPanel gamePanel) {
        super(game);
        this.gamePanel = gamePanel;
        this.tileManager = new TileManager();
        this.gameOptions = loadOptionsOrDefault();
        loadDefaultLevel();
        loadBorderImages();
        initializeManagers();
        this.castleMaxHealth = calculateCastleMaxHealth();
        this.castleCurrentHealth = castleMaxHealth;
        this.gameStateManager = new GameStateManager();
        loadSpawnPointIndicator();
    }

    public Playing(Game game) {
        super(game);
        this.tileManager = new TileManager();
        this.gameOptions = loadOptionsOrDefault();
        loadDefaultLevel();
        loadBorderImages();
        initializeManagers();
        this.castleMaxHealth = calculateCastleMaxHealth();
        this.castleCurrentHealth = castleMaxHealth;
        this.gameStateManager = new GameStateManager();
        loadSpawnPointIndicator();
    }

    public Playing(Game game, TileManager tileManager) {
        super(game);
        this.tileManager = tileManager;
        this.gameOptions = loadOptionsOrDefault();
        loadDefaultLevel();
        loadBorderImages();
        initializeManagers();
        this.castleMaxHealth = calculateCastleMaxHealth();
        this.castleCurrentHealth = castleMaxHealth;
        this.gameStateManager = new GameStateManager();
        loadSpawnPointIndicator();
    }

    public Playing(Game game, TileManager tileManager, int[][] customLevel, int[][] customOverlay) {
        super(game);
        this.tileManager = tileManager;
        this.level = customLevel;
        this.originalLevelData = deepCopy2DArray(customLevel);

        this.overlay = customOverlay;
        this.originalOverlayData = deepCopy2DArray(customOverlay);

        this.gameOptions = loadOptionsOrDefault();
        loadBorderImages();
        initializeManagers();
        this.castleMaxHealth = calculateCastleMaxHealth();
        this.castleCurrentHealth = castleMaxHealth;
        this.gameStateManager = new GameStateManager();
        loadSpawnPointIndicator();
    }

    private GameOptions loadOptionsOrDefault() {
        GameOptions loadedOptions = OptionsIO.load();
        if (loadedOptions == null) {
            System.out.println("Playing: Failed to load GameOptions, using defaults.");
            return GameOptions.defaults();
        }
        System.out.println("Playing: Successfully loaded GameOptions.");
        return loadedOptions;
    }

    private void initializeManagers() {
        if (this.gameOptions == null) {
            System.out.println("Critical Error: gameOptions is null during initializeManagers. Using defaults.");
            this.gameOptions = GameOptions.defaults();
        }

        weatherManager = new WeatherManager();

        projectileManager = new ProjectileManager(this);
        treeInteractionManager = new TreeInteractionManager(this);
        fireAnimationManager = new FireAnimationManager();
        waveManager = new WaveManager(this, this.gameOptions);
        enemyManager = new EnemyManager(this, overlay, level, this.gameOptions);
        towerManager = new TowerManager(this);

        // Set TowerManager reference in WeatherManager for lighting effects
        weatherManager.setTowerManager(towerManager);

        playerManager = new PlayerManager(this.gameOptions);
        ultiManager = new UltiManager(this);
        this.selectedDeadTree = null;

        if (towerManager.findDeadTrees(level) != null)
            deadTrees = towerManager.findDeadTrees(level);
        if (towerManager.findLiveTrees(level) != null)
            liveTrees = towerManager.findLiveTrees(level);

        playingUI = new PlayingUI(this);

        goldBagManager = new GoldBagManager();
        towerSelectionUI = new TowerSelectionUI(this);

        updateUIResources();
    }

    private void loadBorderImages() {
        wallImage = LoadSave.getImageFromPath("/Borders/wall.png");
        gateImage = LoadSave.getImageFromPath("/Borders/gate.png");
        if (wallImage != null && gateImage != null) {
            System.out.println("Border images loaded successfully in Playing mode");
        } else {
            System.err.println("Error loading border images in Playing mode");
        }
    }

    public void updateUIResources() {
        if (playerManager == null) return;
        playingUI.setGoldAmount(playerManager.getGold());
        playingUI.setHealthAmount(playerManager.getHealth());
        playingUI.setShieldAmount(playerManager.getShield());

        if (gameOptions == null) return;
        playingUI.setStartingHealthAmount(gameOptions.getStartingPlayerHP());
        playingUI.setStartingShieldAmount(gameOptions.getStartingShield());
    }

    public void saveLevel(String filename) {
        LoadSave.saveLevel(filename, level);

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

    public void loadLevel(String levelName) {
        int[][] loadedLevel = LoadSave.loadLevel(levelName);
        if (loadedLevel != null) {
            this.currentMapName = levelName; // Update current map name
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

            // Update WeatherManager reference when tower manager is recreated
            if (weatherManager != null) {
                weatherManager.setTowerManager(towerManager);
            }

            deadTrees = towerManager.findDeadTrees(level);
            liveTrees = towerManager.findLiveTrees(level);

            enemyManager = new EnemyManager(this, overlay, level, this.gameOptions);
            waveManager = new WaveManager(this, this.gameOptions);

            // Only reset game state if we're not loading from a save
            if (!gameStateManager.saveFileExists(currentMapName)) {
                resetGameState();
            }
            startEnemySpawning();

            updateUIResources();

            // Load the game state if it exists
            loadGameState();
        }
    }

    public void reloadGameOptions() {
        try {
            // Load fresh options
            this.gameOptions = loadOptionsOrDefault();

            // Update all managers with new options
            if (waveManager != null) waveManager.reloadFromOptions();
            if (enemyManager != null) enemyManager.reloadFromOptions();
            if (playerManager != null) playerManager.reloadFromOptions();

            // Update UI
            updateUIResources();
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
        // Fill background color
        g.setColor(new Color(134, 177, 63, 255));
        g.fillRect(0, 0, GameDimensions.GAME_WIDTH, GameDimensions.GAME_HEIGHT);

        int rowCount = level.length;
        int colCount = level[0].length;

        // Check if we're in snow mode
        boolean isSnowActive = tileManager != null && tileManager.getSnowState() !=
                managers.SnowTransitionManager.SnowState.NORMAL;

        // Detect which edge contains the gate (endpoint) for border rendering
        int gateEdge = detectGateEdge(rowCount, colCount);

        if (isSnowActive) {
            drawSnowLayeredMap(g, rowCount, colCount, gateEdge);
        } else {
            drawNormalMap(g, rowCount, colCount, gateEdge);
        }
    }

    /**
     * Draws the map with proper snow layering:
     * 1. First draw snowy grass base across entire map
     * 2. Then draw other elements (roads, trees, etc.) on top
     */
    private void drawSnowLayeredMap(Graphics g, int rowCount, int colCount, int gateEdge) {
        // LAYER 1: Draw snowy grass base across the entire map
        BufferedImage snowyGrassSprite = getSnowGrassSprite();
        if (snowyGrassSprite != null) {
            for (int i = 0; i < rowCount; i++) {
                for (int j = 0; j < colCount; j++) {
                    // Draw snowy grass everywhere as the base layer
                    g.drawImage(snowyGrassSprite,
                            j * GameDimensions.TILE_DISPLAY_SIZE,
                            i * GameDimensions.TILE_DISPLAY_SIZE, null);
                }
            }
        }

        // LAYER 2: Draw all non-grass elements on top of the snowy grass base
        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < colCount; j++) {
                int tileId = level[i][j];

                // Skip rendering certain tower tiles (they're handled elsewhere)
                if (tileId == 20 || tileId == 21 || tileId == 26) {
                    continue;
                }

                // Handle special border tiles (walls and gates)
                if (tileId == -3 || tileId == -4) {
                    drawBorderTile(g, tileId, j, i, gateEdge);
                }
                // Handle all normal tiles (new snow system automatically applies snow variants)
                else {
                    BufferedImage tileSprite = tileManager.getSprite(tileId);
                    if (tileSprite != null) {
                        g.drawImage(tileSprite,
                                j * GameDimensions.TILE_DISPLAY_SIZE,
                                i * GameDimensions.TILE_DISPLAY_SIZE, null);
                    }
                }
            }
        }
    }

    /**
     * Draws the map normally (without snow effects)
     */
    private void drawNormalMap(Graphics g, int rowCount, int colCount, int gateEdge) {
        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < colCount; j++) {
                int tileId = level[i][j];

                // Skip rendering certain tower tiles (they're handled elsewhere)
                if (tileId == 20 || tileId == 21 || tileId == 26) {
                    continue;
                }

                // Handle special border tiles (walls and gates)
                if (tileId == -3 || tileId == -4) {
                    drawBorderTile(g, tileId, j, i, gateEdge);
                }
                // Handle all normal tiles (new snow system automatically applies snow variants)
                else {
                    BufferedImage tileSprite = tileManager.getSprite(tileId);
                    if (tileSprite != null) {
                        g.drawImage(tileSprite,
                                j * GameDimensions.TILE_DISPLAY_SIZE,
                                i * GameDimensions.TILE_DISPLAY_SIZE, null);
                    }
                }
            }
        }
    }

    /**
     * Gets the appropriate snowy grass sprite based on current snow state
     */
    private BufferedImage getSnowGrassSprite() {
        if (tileManager == null) return null;

        // Get the snowy grass sprite based on current snow transition state
        // Use grass tile ID (5) to get the snow variant
        return tileManager.getSprite(5); // This will automatically return snow variant if active
    }

    /**
     * Draws border tiles (walls and gates) with proper rotation
     */
    private void drawBorderTile(Graphics g, int tileId, int j, int i, int gateEdge) {
        BufferedImage img = null;

        if (tileId == -3 && wallImage != null) {
            img = wallImage;
        } else if (tileId == -4 && gateImage != null) {
            img = gateImage;
        }

        if (img == null) return;

        Graphics2D g2d = (Graphics2D) g.create();
        int x = j * GameDimensions.TILE_DISPLAY_SIZE;
        int y = i * GameDimensions.TILE_DISPLAY_SIZE;
        int ts = GameDimensions.TILE_DISPLAY_SIZE;

        try {
            switch (gateEdge) {
                case 0: // top
                    g2d.drawImage(img, x, y, ts, ts, null);
                    break;
                case 1: // bottom
                    g2d.drawImage(img, x, y + ts, ts, -ts, null); // flip vertically
                    break;
                case 2: // left
                    g2d.rotate(Math.PI / 2, x + ts / 2, y + ts / 2);
                    g2d.drawImage(img, x, y, ts, ts, null);
                    break;
                case 3: // right
                    g2d.rotate(Math.PI / 2, x + ts / 2, y + ts / 2);
                    g2d.drawImage(img, x, y, ts, ts, null);
                    break;
                default:
                    g2d.drawImage(img, x, y, ts, ts, null);
                    break;
            }
        } finally {
            g2d.dispose();
        }
    }

    /**
     * Detects which edge contains the gate for proper border rendering
     */
    private int detectGateEdge(int rowCount, int colCount) {
        // Check edges for gate (-4)
        for (int i = 0; i < rowCount; i++) {
            if (level[i][0] == -4) return 2; // left
            if (level[i][colCount - 1] == -4) return 3; // right
        }
        for (int j = 0; j < colCount; j++) {
            if (level[0][j] == -4) return 0; // top
            if (level[rowCount - 1][j] == -4) return 1; // bottom
        }
        return -1; // no gate found
    }

    public void update() {
        if (!gamePaused) {
            updateGame();
        }
        checkButtonStates();
    }

    private void updateGame() {
        long delta = (long)(16 * gameSpeedMultiplier);
        gameTimeMillis += delta;
        float deltaTimeSeconds = delta / 1000.0f;

        waveManager.update();
        projectileManager.update();
        fireAnimationManager.update();
        ultiManager.update(gameTimeMillis);
        weatherManager.update(deltaTimeSeconds);

        if (tileManager != null && weatherManager != null) {
            tileManager.updateSnowTransition(deltaTimeSeconds, weatherManager.isSnowing());
        }

        // Check enemy status and handle wave completion
        if (isAllEnemiesDead()) {
            if (waveManager.isThereMoreWaves()) {
                // Just let WaveManager handle the wave timing and progression
                // The WaveManager.update() call above will handle all the wave timing
            } else if (waveManager.isAllWavesFinished()) {
                // Only trigger victory if all waves are processed and finished
                handleVictory();
            }
        }

        // Update other game elements
        enemyManager.update(gameSpeedMultiplier);
        towerManager.update(gameSpeedMultiplier);
        updateUIResources();

        if (!playerManager.isAlive()) {
            handleGameOver();
        }

        goldBagManager.update();

        updateCounter++;
        if (updateCounter >= 60) {
            timePlayedInSeconds++;
            updateCounter = 0;
        }

    }

    private boolean isWaveTimerOver() {
        return waveManager.isWaveTimerOver();
    }

    private boolean isThereMoreWaves() {
        return waveManager.isThereMoreWaves();
    }

    private boolean isAllEnemiesDead() {
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

    @Override
    public void render(Graphics g) {
        ultiManager.applyShakeIfNeeded(g);
        drawMap(g);
        ultiManager.draw(g);
        towerManager.draw(g);
        enemyManager.draw(g, gamePaused);
        drawTowerButtons(g);
        drawLiveTreeButtons(g);
        projectileManager.draw(g);
        fireAnimationManager.draw(g);
        weatherManager.draw(g);

        // Draw tower selection UI (range indicators, buttons, etc.)
        if (towerSelectionUI != null) {
            towerSelectionUI.draw(g);
        }

        towerManager.drawLightEffects(g);

        goldBagManager.draw(g);


        // Draw Gold Factory preview if selected (in game world, before UI)
        if (ultiManager.isGoldFactorySelected()) {
            drawGoldFactoryPreview((Graphics2D) g);
        }

        if (!optionsMenuOpen) {
            drawCastleHealthBar(g);
        }

        ultiManager.reverseShake(g);

        playingUI.draw(g);

        // Draw warrior placement message if pending
        if (pendingWarriorPlacement != null) {
            drawWarriorPlacementMessage(g);
        }

        // Draw gold factory placement message if selected
        if (ultiManager.isGoldFactorySelected()) {
            drawGoldFactoryPlacementMessage(g);
        }
    }

    private void drawWarriorPlacementMessage(Graphics g) {
        String message = "Place the ";
        if (pendingWarriorPlacement instanceof objects.WizardWarrior) {
            message += "Wizard";
        } else if (pendingWarriorPlacement instanceof objects.ArcherWarrior) {
            message += "Archer";
        } else {
            message += "Warrior"; // Fallback
        }

        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        int stringWidth = g.getFontMetrics().stringWidth(message);
        int x = (GameDimensions.GAME_WIDTH - stringWidth) / 2;
        int y = 30; // Adjust Y position as needed
        g.drawString(message, x, y);

        // Highlight valid placement tiles with an indicator image
        if (pendingWarriorPlacement != null && spawnPointIndicator != null) {
            for (int r = 0; r < level.length; r++) {
                for (int c = 0; c < level[0].length; c++) {
                    int tilePixelX = c * GameDimensions.TILE_DISPLAY_SIZE;
                    int tilePixelY = r * GameDimensions.TILE_DISPLAY_SIZE;
                    if (isValidTileForPlacement(tilePixelX, tilePixelY)) {
                        // Draw the indicator centered on the tile
                        int indicatorX = tilePixelX + (GameDimensions.TILE_DISPLAY_SIZE - spawnPointIndicator.getWidth()) / 2;
                        int indicatorY = tilePixelY + (GameDimensions.TILE_DISPLAY_SIZE - spawnPointIndicator.getHeight()) / 2;
                        g.drawImage(spawnPointIndicator, indicatorX, indicatorY, null);
                    }
                }
            }
        }
    }

    private boolean isValidTileForPlacement(int pixelX, int pixelY) {
        int tileC = pixelX / GameDimensions.TILE_DISPLAY_SIZE;
        int tileR = pixelY / GameDimensions.TILE_DISPLAY_SIZE;

        if (tileR >= 0 && tileR < level.length && tileC >= 0 && tileC < level[0].length) {
            // Check if the tile type is grass (ID 5)
            boolean isGrass = level[tileR][tileC] == 5;
            if (!isGrass) return false;

            // Check if the tile is already occupied by a tower
            // getTowerAt expects world coordinates, not necessarily snapped if it iterates through towers with their own precise x,y
            // However, since we are checking a tile, converting tile's pixelX, pixelY to center for getTowerAt might be more robust if getTowerAt uses a radius check
            // For simplicity, assuming getTowerAt can work with top-left tile coords if its bounds check is inclusive
            if (getTowerAt(pixelX, pixelY) != null) return false;

            // Check if the tile is already occupied by another warrior
            if (isWarriorAt(pixelX, pixelY)) return false;

            return true; // It's a grass tile and not occupied
        }
        return false; // Out of bounds
    }

    public void modifyTile(int x, int y, String tile) {
        x /= 64;
        y /= 64;

        if (tile.equals("ARCHER")) {
            level[y][x] = 26;
        } else if (tile.equals("MAGE")) {
            level[y][x] = 20;
        } else if (tile.equals("ARTILERRY")) {
            level[y][x] = 21;
        } else if (tile.equals("DEADTREE")) {
            level[y][x] = 15;
        }
    }

    @Override
    public void mouseClicked(int x, int y) {
        this.mouseX = x;
        this.mouseY = y;

        // Handle gold factory placement if selected
        if (ultiManager.isGoldFactorySelected()) {
            boolean placed = ultiManager.tryPlaceGoldFactory(x, y);
            if (!placed) {
                // If placement failed, keep factory selected for another try
                // User can click the button again to deselect if they want
            }
            return; // Exit early to prevent other interactions
        }

        // Handle warrior placement if a warrior is pending placement
        if (pendingWarriorPlacement != null) {
            // Snap click coordinates to the grid
            int tileX = (x / GameDimensions.TILE_DISPLAY_SIZE) * GameDimensions.TILE_DISPLAY_SIZE;
            int tileY = (y / GameDimensions.TILE_DISPLAY_SIZE) * GameDimensions.TILE_DISPLAY_SIZE;

            // Check if the clicked position is a valid tile for placement
            if (isValidTileForPlacement(tileX, tileY)) { // Use snapped coordinates for validation too
                // Deduct gold for the warrior
                this.playerManager.spendGold(pendingWarriorPlacement.getCost());
                updateUIResources(); // Update gold display and other UI elements

                // Place the warrior at the top-left of the tile, with a slight upward offset
                int placementY = tileY - 8; // Adjust this offset as needed (e.g., 5-10 pixels up)
                pendingWarriorPlacement.setX(tileX);
                pendingWarriorPlacement.setY(placementY);
                towerManager.getWarriors().add(pendingWarriorPlacement);
                System.out.println("Warrior placed at tile coordinates: (" + tileX + ", " + placementY + ") for " + pendingWarriorPlacement.getCost() + " gold.");
                pendingWarriorPlacement = null; // Clear pending placement
            }
            return; // Exit early to prevent other interactions
        }

        // Handle tower selection UI clicks first - only return early if actually handled
        if (towerSelectionUI != null && towerSelectionUI.hasTowerSelected()) {
            boolean uiHandledClick = towerSelectionUI.mouseClicked(x, y);
            if (uiHandledClick) {
                return; // UI button was clicked, don't process other interactions
            }
        }

        // Handle dead trees
        if (deadTrees != null) {
            treeInteractionManager.handleDeadTreeInteraction(mouseX, mouseY);
        }
        if (liveTrees != null) {
            treeInteractionManager.handleLiveTreeInteraction(mouseX, mouseY);
        }

        // Gold bag collection
        var collectedBag = goldBagManager.tryCollect(x, y);
        if (collectedBag != null) {
            playerManager.addGold(collectedBag.getGoldAmount());
            updateUIResources();
        }

        // Check for tower selection/deselection
        Tower clickedTower = getTowerAt(x, y);
        if (clickedTower != null) {
            towerSelectionUI.setSelectedTower(clickedTower);
            playButtonClickSound();
        } else {
            towerSelectionUI.setSelectedTower(null); // Clear selection when clicking elsewhere
        }
    }

    /**
     * Helper method to find tower at mouse position
     */
    private Tower getTowerAt(int mouseX, int mouseY) {
        for (Tower tower : towerManager.getTowers()) {
            if (tower.isClicked(mouseX, mouseY)) {
                return tower;
            }
        }
        return null;
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

    public TileManager getTileManager() {
        return tileManager;
    }

    @Override
    public void mouseMoved(int x, int y) {
        // Only snap to grid if gold factory is selected (like map editing)
        if (ultiManager.isGoldFactorySelected()) {
            mouseX = (x / GameDimensions.TILE_DISPLAY_SIZE) * GameDimensions.TILE_DISPLAY_SIZE;
            mouseY = (y / GameDimensions.TILE_DISPLAY_SIZE) * GameDimensions.TILE_DISPLAY_SIZE;
        } else {
            mouseX = x;
            mouseY = y;
        }

        playingUI.mouseMoved(x, y);

        if (towerSelectionUI != null) {
            towerSelectionUI.mouseMoved(x, y);
        }
    }

    private void highlightValidTiles() {
        // Implement logic to highlight all valid tiles for warrior placement
        // For now, let's assume all tiles are valid and highlight them
        // This can be done by drawing a semi-transparent overlay on valid tiles
        // Example: g.setColor(new Color(0, 255, 0, 100)); // Green with transparency
        // g.fillRect(tileX, tileY, tileWidth, tileHeight);
    }

    @Override
    public void mousePressed(int x, int y) {
        playingUI.mousePressed(x, y);
        if (towerSelectionUI != null) {
            towerSelectionUI.mousePressed(x, y);
        }
    }

    @Override
    public void mouseReleased(int x, int y) {
        playingUI.mouseReleased();

        if (towerSelectionUI != null) {
            towerSelectionUI.mouseReleased();
        }
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
        } else if (playingUI.getBackOptionsButton().isMousePressed()) {
            handleBackOptionsButton(true);
        } else if (playingUI.getMainMenuButton().isMousePressed()) {
            handleMainMenuButton(true);
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
            // Show options menu and pause the game
            optionsMenuOpen = true;
            gamePaused = true;
            if (playingUI != null && playingUI.getPauseButton() != null) {
                playingUI.getPauseButton().setMousePressed(true);
            }
            System.out.println("Options menu opened and game paused");
        } else if (!isPressed && optionsMenuOpen) {
            // Hide options menu and unpause the game
            optionsMenuOpen = false;
            gamePaused = false;
            if (playingUI != null && playingUI.getPauseButton() != null) {
                playingUI.getPauseButton().setMousePressed(false);
            }
            System.out.println("Options menu closed and game resumed");
        }
    }

    private void handleBackOptionsButton(boolean isPressed) {
        // Close options menu and unpause the game when back button is pressed
        if (isPressed && optionsMenuOpen) {
            // Hide options menu and unpause the game
            optionsMenuOpen = false;
            gamePaused = false;
            if (playingUI != null && playingUI.getPauseButton() != null) {
                playingUI.getPauseButton().setMousePressed(false);
            }
            System.out.println("Options menu closed via back button and game resumed");
        }
    }

    private void handleMainMenuButton(boolean isPressed) {
        // Close options menu and return to main menu when main menu button is pressed
        if (isPressed && optionsMenuOpen) {
            // Hide options menu first
            optionsMenuOpen = false;
            System.out.println("Options menu closed via main menu button");
            // Then return to main menu
            returnToMainMenu();
        }
    }

    public WaveManager getWaveManager() {
        return waveManager;
    }

    public DeadTree getSelectedDeadTree() {
        return selectedDeadTree;
    }

    public void setSelectedDeadTree(DeadTree deadTree) {
        this.selectedDeadTree = deadTree;
    }

    public void spawnEnemy(int enemyType) {
        if (enemyType != -1) {
            enemyManager.spawnEnemy(enemyType);
            totalEnemiesSpawned++;
            System.out.println("Spawning enemy of type: " + enemyType);
        } else {
            System.out.println("Invalid enemy type (-1) received, skipping spawn");
        }
    }

    private boolean isTimeForNewEnemy() {
        return false;
    }

    public void startEnemySpawning() {
        if (waveManager != null) {
            waveManager.resetWaveManager();
        }
    }

    public void enemyReachedEnd(Enemy enemy) {
        System.out.println("Enemy reached end: " + enemy.getId());

        enemiesReachedEnd++;

        playerManager.takeDamage(1);

        this.castleCurrentHealth = playerManager.getHealth();

        checkCastleHealth();

        updateUIResources();
    }

    private void checkCastleHealth() {
        if (!playerManager.isAlive()) {
            handleGameOver();
        }
    }

    private void handleGameOver() {
        // Prevent multiple calls to handleGameOver
        if (gameOverHandled) return;

        System.out.println("Game Over!");
        gameOverHandled = true;

        weatherManager.stopAllWeatherSounds();

        // Delete the save file
        gameStateManager.deleteSaveFile(currentMapName);

        // Play the specific lose sound
        AudioManager.getInstance().playSound("lose5");

        // stop any ongoing waves/spawning
        enemyManager.getEnemies().clear();

        GameStatsRecord record = new GameStatsRecord(
                game.getPlaying().getMapName(),
                false,
                playerManager.getTotalGoldEarned(),
                totalEnemiesSpawned,
                enemiesReachedEnd,
                towerManager.getTowers().size(),
                enemyDefeated,
                totalDamage,
                timePlayedInSeconds
        );

        game.getStatsManager().addRecord(record);
        game.getStatsManager().saveToFile(record);

        game.getGameOverScene().setStats(
                false,
                playerManager.getTotalGoldEarned(),
                totalEnemiesSpawned,
                enemiesReachedEnd,
                towerManager.getTowers().size(),
                enemyDefeated,
                totalDamage,
                timePlayedInSeconds
        );

        game.changeGameState(main.GameStates.GAME_OVER);
    }

    // add a method to handle victory
    private void handleVictory() {
        // Prevent multiple calls to handleVictory
        // Victory only when all waves are finished and player is alive!
        if (victoryHandled || !waveManager.isAllWavesFinished() || !playerManager.isAlive()) return;

        System.out.println("Victory!");
        victoryHandled = true;

        weatherManager.stopAllWeatherSounds();

        // Delete the save file
        gameStateManager.deleteSaveFile(currentMapName);

        // play the specific victory sound
        AudioManager.getInstance().playSound("win4");
        game.getGameOverScene().setStats(
                true,
                playerManager.getTotalGoldEarned(),
                totalEnemiesSpawned,
                enemiesReachedEnd,
                towerManager.getTowers().size(),
                enemyDefeated,
                totalDamage,
                timePlayedInSeconds
        );
        GameStatsRecord record = new GameStatsRecord(
                currentMapName, true,
                playerManager.getTotalGoldEarned(),
                totalEnemiesSpawned,
                enemiesReachedEnd,
                towerManager.getTowers().size(),
                enemyDefeated,
                totalDamage,
                timePlayedInSeconds
        );
        game.getStatsManager().addRecord(record);
        game.getStatsManager().saveToFile(record);

        game.changeGameState(main.GameStates.GAME_OVER);
    }

    @Override
    public void playButtonClickSound() {
        AudioManager.getInstance().playButtonClickSound();
    }

    /**
     * Handles mouse wheel events and forwards them to PlayingUI
     *
     * @param e The mouse wheel event
     */
    public void mouseWheelMoved(MouseWheelEvent e) {
        playingUI.mouseWheelMoved(e);
    }

    public void shootEnemy(Object shooter, Enemy enemy) {
        if (shooter instanceof Tower) {
            Tower tower = (Tower) shooter;
            projectileManager.newProjectile(tower, enemy);
            tower.applyOnHitEffect(enemy, this);
        } else if (shooter instanceof Warrior) {
            Warrior warrior = (Warrior) shooter;
            projectileManager.newProjectile(warrior, enemy);
            // Warriors currently have no special on-hit effects
        }
    }

    public boolean isGamePaused() {
        return gamePaused;
    }

    public boolean isOptionsMenuOpen() {
        return optionsMenuOpen;
    }

    public float getGameSpeedMultiplier() {
        return gameSpeedMultiplier;
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
        // 1. Reload current game options (essential for PlayerManager starting stats)
        this.gameOptions = loadOptionsOrDefault();

        // 2. Reset game state flags
        gameOverHandled = false;
        victoryHandled = false;
        gamePaused = false;
        gameSpeedIncreased = false;
        optionsMenuOpen = false;
        gameSpeedMultiplier = 1.0f;

        // 3. Reset player and castle health
        if (this.gameOptions != null) {
            playerManager = new PlayerManager(this.gameOptions);
        } else {
            System.err.println("CRITICAL: gameOptions null during resetGameState after load. Using defaults.");
            playerManager = new PlayerManager(GameOptions.defaults());
        }
        this.castleMaxHealth = playerManager.getStartingHealthAmount(); // Max health is player's starting health
        this.castleCurrentHealth = playerManager.getHealth(); // Current health is player's current health

        // 4. Restore original map state from stored copies
        if (originalLevelData != null) {
            level = deepCopy2DArray(originalLevelData);
        } else {
            System.err.println("CRITICAL: originalLevelData is null in resetGameState. Cannot restore level.");
            return;
        }
        if (originalOverlayData != null) {
            overlay = deepCopy2DArray(originalOverlayData);
        } else {
            System.err.println("CRITICAL: originalOverlayData is null in resetGameState. Cannot restore overlay.");
            return;
        }

        // 5. Reset UI selections
        displayedTower = null;

        if (towerSelectionUI != null) {
            towerSelectionUI.setSelectedTower(null);
        }
        selectedDeadTree = null;

        // 6. Clear active entities from managers
        if (towerManager != null) {
            towerManager.clearTowers();
        }
        if (projectileManager != null) {
            projectileManager.clearProjectiles();
        }
        // Note: Enemies are cleared when EnemyManager is reconstructed below, no need for explicit clear here.

        // 7. Recreate or Reset managers that depend heavily on map state or need full reset
        if (this.gameOptions != null) {
            playerManager = new PlayerManager(this.gameOptions);
        } else {
            System.err.println("CRITICAL: gameOptions null during resetGameState after load. Using defaults.");
            playerManager = new PlayerManager(GameOptions.defaults());
        }

        if (this.gameOptions == null) this.gameOptions = GameOptions.defaults();
        enemyManager = new EnemyManager(this, overlay, level, this.gameOptions);

        if (waveManager != null) {
            waveManager.resetWaveManager();
        } else {
            waveManager = new WaveManager(this, this.gameOptions); // Should exist, but defensive
            waveManager.resetWaveManager();
        }

        // 8. Re-initialize map-derived lists (Dead/Live trees) based on the restored 'level'
        // This is crucial for ensuring tree states and interactions are correct after reset.
        if (towerManager != null) {
            deadTrees = towerManager.findDeadTrees(level);
            liveTrees = towerManager.findLiveTrees(level);
        } else {
            System.err.println("CRITICAL: TowerManager null during resetGameState. Tree lists not updated.");
        }

        // 9. Update UI to reflect the reset state
        updateUIResources();

        // Warm-up render on first reset to prevent initial flash
        if (isFirstReset) {
            System.out.println("Performing first-time render warm-up for Playing scene...");
            BufferedImage dummyImg = new BufferedImage(GameDimensions.GAME_WIDTH, GameDimensions.GAME_HEIGHT, BufferedImage.TYPE_INT_ARGB);
            Graphics g = dummyImg.getGraphics();
            if (g != null) {
                this.render(g); // Call render once to an offscreen buffer
                g.dispose();
            }
            isFirstReset = false;
            System.out.println("Render warm-up complete.");
        }
    }

    /**
     * Add a helper method to get and display wave status
     *
     * @return Current wave and state information
     */
    public String getWaveStatus() {
        int currentWave = waveManager.getWaveIndex() + 1; // Convert to 1-based for display
        String stateInfo = waveManager.getCurrentStateInfo();

        return "Wave " + currentWave + "\n" + stateInfo;
    }

    public GoldBagManager getGoldBagManager() {
        return goldBagManager;
    }

    // Helper to get upgrade cost
    private int getUpgradeCost(Tower tower) {
        switch (tower.getType()) {
            case 0:
                return 75; // Archer
            case 1:
                return 120; // Artillery
            case 2:
                return 100; // Mage
            default:
                return 100;
        }
    }

    public int[][] getLevel() {
        return level;
    }

    public int[][] getOverlay() {
        return overlay;
    }

    private int calculateCastleMaxHealth() {
        return waveManager.getWaveCount() * 100;
    }

    private void drawCastleHealthBar(Graphics g) {
        int castleX = -1, castleY = -1;
        outer:
        for (int i = 0; i < level.length - 1; i++) {
            for (int j = 0; j < level[i].length - 1; j++) {
                if (level[i][j] == tileManager.CastleTopLeft.getId() &&
                        level[i][j + 1] == tileManager.CastleTopRight.getId() &&
                        level[i + 1][j] == tileManager.CastleBottomLeft.getId() &&
                        level[i + 1][j + 1] == tileManager.CastleBottomRight.getId()) {
                    castleX = j;
                    castleY = i;
                    break outer;
                }
            }
        }
        if (castleX == -1 || castleY == -1) return;

        int tileSize = constants.GameDimensions.TILE_DISPLAY_SIZE;
        int barWidth = tileSize * 2 - 8;
        int barHeight = 8;
        int barX = castleX * tileSize + 4;
        int barY = castleY * tileSize - 14;

        g.setColor(Color.DARK_GRAY);
        g.fillRoundRect(barX, barY, barWidth, barHeight, 6, 6);
        float healthPercent = Math.max(0, (float) playerManager.getHealth() / playerManager.getStartingHealthAmount());
        Color healthColor = new Color((int) (255 * (1 - healthPercent)), (int) (220 * healthPercent), 40);
        int healthBarWidth = (int) (barWidth * healthPercent);
        g.setColor(healthColor);
        g.fillRoundRect(barX, barY, healthBarWidth, barHeight, 6, 6);

        g.setColor(Color.BLACK);
        g.drawRoundRect(barX, barY, barWidth, barHeight, 6, 6);
    }

    public boolean isAllWavesFinished() {
        return waveManager.isAllWavesFinished();
    }

    public void incrementEnemyDefeated() {
        enemyDefeated++;
    }

    public void addTotalDamage(int damage) {
        totalDamage += damage;
    }

    // Save game state
    public void saveGameState() {
        // Create lists to store tower states
        List<GameStateMemento.TowerState> towerStates = new ArrayList<>();

        // Save tower states
        for (Tower tower : towerManager.getTowers()) {
            towerStates.add(new GameStateMemento.TowerState(
                    tower.getX(),
                    tower.getY(),
                    tower.getType(),
                    tower.getLevel()
            ));
        }

        // Create memento with all game state including GameOptions, but without enemy states
        GameStateMemento memento = new GameStateMemento(
                playerManager.getGold(),
                playerManager.getHealth(),
                playerManager.getShield(),
                waveManager.getWaveIndex(),
                waveManager.getCurrentGroupIndex(),
                towerStates,
                new ArrayList<>(), // Empty list for enemy states
                gameOptions
        );

        // Save the memento using the current map name
        gameStateManager.saveGameState(memento, currentMapName);
    }

    // Load game state
    public void loadGameState() {
        GameStateMemento memento = gameStateManager.loadGameState(currentMapName);
        if (memento == null) {
            System.out.println("Failed to load game state from " + currentMapName);
            return;
        }

        // Restore game options first
        this.gameOptions = memento.getGameOptions();

        // Restore player state
        playerManager.setGold(memento.getGold());
        playerManager.setHealth(memento.getHealth());
        playerManager.setShield(memento.getShield());

        // Restore wave state
        waveManager.setWaveIndex(memento.getWaveIndex());
        waveManager.setCurrentGroupIndex(memento.getGroupIndex());

        // Clear existing towers and enemies
        towerManager.clearTowers();
        enemyManager.clearEnemies();

        // Restore towers
        for (GameStateMemento.TowerState towerState : memento.getTowerStates()) {
            Tower tower = null;
            switch (towerState.getType()) {
                case Constants.Towers.ARCHER:
                    tower = new ArcherTower(towerState.getX(), towerState.getY());
                    break;
                case Constants.Towers.ARTILLERY:
                    tower = new ArtilleryTower(towerState.getX(), towerState.getY());
                    break;
                case Constants.Towers.MAGE:
                    tower = new MageTower(towerState.getX(), towerState.getY());
                    break;
            }
            if (tower != null) {
                tower.setLevel(towerState.getLevel());
                // If the tower is level 2, create the appropriate upgraded version
                if (towerState.getLevel() == 2) {
                    switch (towerState.getType()) {
                        case Constants.Towers.ARCHER:
                            tower = new UpgradedArcherTower(tower);
                            break;
                        case Constants.Towers.ARTILLERY:
                            tower = new UpgradedArtilleryTower(tower);
                            break;
                        case Constants.Towers.MAGE:
                            tower = new UpgradedMageTower(tower);
                            break;
                    }
                }
                towerManager.addTower(tower);
            }
        }

        // Update UI with restored player state
        updateUIResources();
    }

    public void setCurrentMapName(String mapName) {
        this.currentMapName = mapName;
    }
    public String getMapName() {
        return currentMapName != null ? currentMapName : "default";
    }

    public Tower getDisplayedTower() {
        // Return the currently selected tower from TowerSelectionUI for backward compatibility
        return towerSelectionUI != null ? towerSelectionUI.getSelectedTower() : null;
    }

    public void setDisplayedTower(Tower tower) {
        if (towerSelectionUI != null) {
            towerSelectionUI.setSelectedTower(tower);
        }
    }
    public UltiManager getUltiManager() {
        return ultiManager;
    }

    public void setUltiManager(UltiManager ultiManager) {
        this.ultiManager = ultiManager;
    }

    public long getGameTime() {
        return gameTimeMillis;
    }

    public WeatherManager getWeatherManager() {
        return weatherManager;
    }

    public void startWarriorPlacement(Warrior warrior) {
        this.pendingWarriorPlacement = warrior;
        // Close the tower menu
        towerSelectionUI.setSelectedTower(null);
        System.out.println("Warrior placement mode started for: " + warrior.getClass().getSimpleName());
        // Additional logic to highlight valid tiles for placement can be added here
    }

    private boolean isWarriorAt(int x, int y) {
        for (Warrior warrior : towerManager.getWarriors()) {
            if (warrior.getX() == x && warrior.getY() == y) {
                return true;
            }
        }
        return false;
    }

    private void loadSpawnPointIndicator() {
        // Create a placeholder graphic directly
        int indicatorSize = 24; // Size of the indicator
        spawnPointIndicator = new BufferedImage(indicatorSize, indicatorSize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = spawnPointIndicator.createGraphics();

        // Draw a yellow circle with a black border
        g.setColor(Color.YELLOW);
        g.fillOval(2, 2, indicatorSize - 4, indicatorSize - 4);
        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(2));
        g.drawOval(2, 2, indicatorSize - 4, indicatorSize - 4);

        g.dispose();
    }

    private void drawGoldFactoryPreview(Graphics2D g) {
        // Snap to tile grid
        int tileX = (mouseX / 64) * 64;
        int tileY = (mouseY / 64) * 64;

        // Check if tile is valid for placement
        boolean isValidTile = false;
        int levelTileX = mouseX / 64;
        int levelTileY = mouseY / 64;

        if (levelTileY >= 0 && levelTileY < level.length &&
                levelTileX >= 0 && levelTileX < level[0].length) {
            isValidTile = (level[levelTileY][levelTileX] == 5); // Grass tile
        }

        // Enable anti-aliasing for smoother graphics
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw cute rounded background with soft colors
        if (isValidTile) {
            // Soft green with gradient effect
            g.setColor(new Color(144, 238, 144, 80)); // Light green with transparency
        } else {
            // Soft red with gradient effect
            g.setColor(new Color(255, 182, 193, 80)); // Light pink/red with transparency
        }

        // Draw rounded rectangle instead of harsh rectangle
        g.fillRoundRect(tileX + 2, tileY + 2, 60, 60, 12, 12);

        // Draw the factory sprite with transparency
        try {
            BufferedImage factorySprite = ui_p.AssetsLoader.getInstance().goldFactorySprite;
            if (factorySprite != null) {
                // Draw the sprite with some transparency to show it's a preview
                Composite originalComposite = g.getComposite();
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8f));
                g.drawImage(factorySprite, tileX + 2, tileY + 2, 60, 60, null);
                g.setComposite(originalComposite);
            } else {
                // Fallback: Draw a cute "G" with better styling
                g.setColor(new Color(218, 165, 32, 180)); // Gold color
                g.setFont(new Font("Arial", Font.BOLD, 20));
                FontMetrics fm = g.getFontMetrics();
                String text = "G";
                int textX = tileX + 32 - fm.stringWidth(text) / 2;
                int textY = tileY + 32 + fm.getAscent() / 2;
                g.drawString(text, textX, textY);
            }
        } catch (Exception e) {
            // Fallback with better styling
            g.setColor(new Color(218, 165, 32, 180));
            g.setFont(new Font("Arial", Font.BOLD, 20));
            FontMetrics fm = g.getFontMetrics();
            String text = "G";
            int textX = tileX + 32 - fm.stringWidth(text) / 2;
            int textY = tileY + 32 + fm.getAscent() / 2;
            g.drawString(text, textX, textY);
        }

        // Draw cute border with rounded corners
        if (isValidTile) {
            g.setColor(new Color(34, 139, 34, 120)); // Forest green border
        } else {
            g.setColor(new Color(220, 20, 60, 120)); // Crimson border
        }
        g.setStroke(new BasicStroke(2f)); // Thicker, softer border
        g.drawRoundRect(tileX + 2, tileY + 2, 60, 60, 12, 12);

        // Add a subtle sparkle effect for valid placement
        if (isValidTile) {
            long time = System.currentTimeMillis();
            float sparkleAlpha = (float)(0.3f + 0.2f * Math.sin(time * 0.005f));
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, sparkleAlpha));
            g.setColor(new Color(255, 255, 255, 100));
            // Draw small sparkles at corners
            g.fillOval(tileX + 8, tileY + 8, 4, 4);
            g.fillOval(tileX + 52, tileY + 8, 4, 4);
            g.fillOval(tileX + 8, tileY + 52, 4, 4);
            g.fillOval(tileX + 52, tileY + 52, 4, 4);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }

        // Reset anti-aliasing
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    }

    private void drawGoldFactoryPlacementMessage(Graphics g) {
        String message = "✨ Click to place Gold Factory | Click button again to cancel ✨";

        g.setColor(new Color(255, 215, 0)); // Gold color for text
        g.setFont(new Font("Arial", Font.BOLD, 18));
        int stringWidth = g.getFontMetrics().stringWidth(message);
        int x = (GameDimensions.GAME_WIDTH - stringWidth) / 2;
        int y = 30; // Adjust Y position as needed

        // Draw text background for better readability
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(new Color(0, 0, 0, 100)); // Semi-transparent black
        g2d.fillRoundRect(x - 10, y - 20, stringWidth + 20, 30, 10, 10);

        g.setColor(new Color(255, 215, 0)); // Gold color for text
        g.drawString(message, x, y);

        // Enable anti-aliasing for smoother graphics
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Highlight valid placement tiles (grass tiles) with cute styling
        for (int r = 0; r < level.length; r++) {
            for (int c = 0; c < level[0].length; c++) {
                if (level[r][c] == 5) { // Grass tile
                    int tilePixelX = c * GameDimensions.TILE_DISPLAY_SIZE;
                    int tilePixelY = r * GameDimensions.TILE_DISPLAY_SIZE;

                    // Draw a soft green rounded background
                    g2d.setColor(new Color(144, 238, 144, 40)); // Light green with transparency
                    g2d.fillRoundRect(tilePixelX + 4, tilePixelY + 4,
                            GameDimensions.TILE_DISPLAY_SIZE - 8,
                            GameDimensions.TILE_DISPLAY_SIZE - 8, 8, 8);

                    // Draw a soft green border
                    g2d.setColor(new Color(34, 139, 34, 80)); // Forest green border
                    g2d.setStroke(new BasicStroke(1.5f));
                    g2d.drawRoundRect(tilePixelX + 4, tilePixelY + 4,
                            GameDimensions.TILE_DISPLAY_SIZE - 8,
                            GameDimensions.TILE_DISPLAY_SIZE - 8, 8, 8);
                }
            }
        }

        // Reset anti-aliasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    }

}
