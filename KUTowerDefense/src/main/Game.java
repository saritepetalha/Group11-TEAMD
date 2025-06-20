package main;

import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.swing.JFrame;

import managers.AudioManager;
import managers.FullscreenManager;
import managers.GameStatsManager;
import managers.TileManager;
import scenes.GameOverScene;
import scenes.Intro;
import scenes.LevelSelectionScene;
import scenes.LoadGameMenu;
import scenes.MapEditing;
import scenes.Menu;
import scenes.Options;
import scenes.Playing;
import scenes.StatisticsScene;
import scenes.SkillSelectionScene;

public class  Game extends JFrame implements Runnable{

	private GameScreen gamescreen;

	private Thread gameThread;

	private final double FPS_SET = 120.0;
	private final double UPS_SET = 60.0;


	private Render render;
	private Intro intro;
	private Menu menu;
	private Options options;
	private Playing playing;
	private MapEditing mapEditing;
	private LoadGameMenu loadGameMenu;
	private LevelSelectionScene newGameLevelSelection;
	private GameOverScene gameOverScene;
	private StatisticsScene statisticsScene;
	private TileManager tileManager;
	private GameStatsManager statsManager;
	private SkillSelectionScene skillSelectionScene;
	private FullscreenManager fullscreenManager;

	// State tracking for map editing context
	private GameStates previousGameState = GameStates.MENU;

	public Game() {
		// Debug information for IntelliJ compatibility
		printDebugInfo();

		System.out.println("Game Starting...");

		// Initialize the enhanced options system
		helpMethods.OptionsIO.initialize();

		this.tileManager = new TileManager();
		this.fullscreenManager = new FullscreenManager(this);

		// Set window properties before initialization
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		initClasses();
		add(gamescreen);

		// Pack first to get proper size, then center
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}

	/**
	 * Print debug information to help with IntelliJ path issues
	 */
	private void printDebugInfo() {
		System.out.println("=== GAME DEBUG INFO ===");
		System.out.println("Working Directory: " + System.getProperty("user.dir"));
		System.out.println("Java Version: " + System.getProperty("java.version"));
		System.out.println("OS: " + System.getProperty("os.name"));

		// Check if key directories exist
		String[] checkPaths = {
				"src/main/resources/Saves",
				"src/main/resources/Levels",
				"src/main/resources/Options",
				"demo/src/main/resources/Saves",
				"./src/main/resources/Saves"
		};

		System.out.println("Directory existence check:");
		for (String path : checkPaths) {
			File dir = new File(path);
			System.out.println("  " + path + " - exists: " + dir.exists() + ", isDirectory: " + dir.isDirectory());
		}
		System.out.println("========================");
	}


	public void changeGameState(GameStates newState) {
		GameStates previousState = GameStates.gameState;

		// Special handling for MapEditing based on entry context
		if (newState == GameStates.EDIT) {
			handleMapEditingStateChange(previousState);
		}

		// Track previous state for future reference
		this.previousGameState = previousState;

		GameStates.gameState = newState;

		// Update fullscreen scaling when game state changes
		if (fullscreenManager != null) {
			fullscreenManager.onGameStateChanged();
		}

		AudioManager audioManager = AudioManager.getInstance();
		boolean isMenuRelatedToggle = (
				(previousState == GameStates.MENU && (newState == GameStates.OPTIONS || newState == GameStates.EDIT || newState == GameStates.LOAD_GAME || newState == GameStates.NEW_GAME_LEVEL_SELECT)) ||
						((previousState == GameStates.OPTIONS || previousState == GameStates.EDIT || previousState == GameStates.LOAD_GAME || previousState == GameStates.NEW_GAME_LEVEL_SELECT) && newState == GameStates.MENU)
		);

		// Reload game options in Playing when returning from Options to Menu
		if (previousState == GameStates.OPTIONS && newState == GameStates.MENU) {
			if (playing != null) {
				playing.reloadGameOptions();
			}
		}

		// Handle heavy initialization BEFORE UI updates to prevent black flash
		if (!isMenuRelatedToggle) {
			switch (newState) {
				case MENU:
					audioManager.stopWeatherSounds();
					// Stop all sounds when coming from game over to ensure clean transition
					if (previousState == GameStates.GAME_OVER) {
						audioManager.stopAllSounds();
					}
					audioManager.playMusic("lonelyhood");
					// Only restore custom settings if coming from game over
					if (previousState == GameStates.GAME_OVER) {
						helpMethods.OptionsIO.restoreCustomSettings();
						System.out.println("Restored custom settings after gameplay");
					}
					// Reset skills when returning to menu
					if (skillSelectionScene != null) {
						skillSelectionScene.resetSkillSelections();
					}
					break;
				case PLAYING:
					// Reset game state BEFORE making window visible to prevent black flash
					if (playing != null && previousState != GameStates.LOAD_GAME) {
						playing.resetGameState();
					}
					audioManager.playRandomGameMusic();
					break;
				case SKILL_SELECTION:
					// Clear any previously selected skills
					if (skillSelectionScene != null) {
						skillSelectionScene.resetSkillSelections();
					}
					break;
				case NEW_GAME_LEVEL_SELECT:
					if (newGameLevelSelection != null) {
						newGameLevelSelection.refreshLevelList();
					}
					break;
				case LOAD_GAME:
					audioManager.playMusic("lonelyhood");
					break;
				case INTRO:
				case OPTIONS:
				case EDIT:
				case LOADED:
				case GAME_OVER:
					audioManager.stopAllWeatherAndMusic();
					break;
				default:
					break;
			}
		}

		if (gamescreen != null) {
			gamescreen.updateContentForState(newState, previousState);
		}

		if (gamescreen != null) {
			gamescreen.setPanelSize();
		}
		getContentPane().removeAll();
		add(gamescreen);

		// Only pack and center window if not in fullscreen mode
		if (fullscreenManager == null || !fullscreenManager.isFullscreen()) {
			pack();
			setLocationRelativeTo(null);
		}

		// Ensure the game window and GameScreen have focus for keyboard input
		this.requestFocus();
		if (gamescreen != null) {
			gamescreen.requestFocusInWindow();
		}

		// Refresh map previews when entering LOAD_GAME
		// This ensures any newly saved games show updated thumbnails
		if (newState == GameStates.LOAD_GAME) {
			if (loadGameMenu != null) {
				loadGameMenu.refreshMapPreviews();
			}
		}

		// Refresh level selection scene when entering NEW_GAME_LEVEL_SELECT
		// This ensures any edited maps show updated thumbnails
		if (newState == GameStates.NEW_GAME_LEVEL_SELECT) {
			if (newGameLevelSelection != null) {
				newGameLevelSelection.refreshLevelList();
			}
		}

		if (gamescreen != null) {
			gamescreen.setPanelSize();
			gamescreen.revalidate();
			gamescreen.repaint();
		}
	}

	/**
	 * Handle MapEditing initialization based on entry context
	 */
	private void handleMapEditingStateChange(GameStates previousState) {
		if (previousState == GameStates.MENU) {
			// Coming from main menu - create fresh empty map (9x16)
			mapEditing = new MapEditing(this, this, true);
			System.out.println("MapEditing: Created fresh map (from main menu)");
		} else if (previousState == GameStates.NEW_GAME_LEVEL_SELECT) {
			// Coming from level selection - keep current instance that was set up by editLevel()
			// The level data was already loaded by LevelSelectionScene.editLevel()
			System.out.println("MapEditing: Using existing map data (from level selection)");
		}
		// For other states, keep existing instance
	}

	private void initClasses() {
		AudioManager.getInstance();

		statsManager = new GameStatsManager();

		gamescreen = new GameScreen(this);
		render = new Render(this);
		intro = new Intro(this);
		menu = new Menu(this);
		options = new Options(this);
		if (playing == null) {
			playing = new Playing(this);
		}
		mapEditing = new MapEditing(this, this);
		loadGameMenu = new LoadGameMenu(this);
		newGameLevelSelection = new LevelSelectionScene(this, new levelselection.AllLevelsStrategy());
		gameOverScene = new GameOverScene(this);
		statisticsScene = new StatisticsScene(this);
		skillSelectionScene = new SkillSelectionScene(this, playing);
		tileManager = new TileManager();

	}


	public void start() {
		gameThread = new Thread(this);
		gameThread.start();
	}

	private void updateGame() {
		switch (GameStates.gameState) {
			case INTRO:
				if (intro != null) intro.update();
				break;
			case MENU:
			case OPTIONS:
			case EDIT:
			case LOAD_GAME:
			case NEW_GAME_LEVEL_SELECT:
			case SKILL_SELECTION:
				break;
			case PLAYING:
				if (playing != null) playing.update();
				break;
			case GAME_OVER:
				gameOverScene.update();
				break;
			case STATISTICS:
				statisticsScene.update();
				break;
			default:
				break;
		}
	}

	public static void main(String[] args) {
		System.out.println("Game Starting...");
		Game game = new Game();
		game.start();

		if (GameStates.gameState == GameStates.MENU) {
			AudioManager.getInstance().playMusic("lonelyhood");
		} else if (GameStates.gameState != GameStates.INTRO) {
			AudioManager.getInstance().playRandomGameMusic();
		}
	}


	@Override
	public void run() {
		double timePerFrame = 1000000000.0 / FPS_SET;
		double timePerUpdate = 1000000000.0 / UPS_SET;

		long lastFrame = System.nanoTime();
		long lastUpdate = System.nanoTime();
		int frames = 0;
		long lastTimeCheck = System.currentTimeMillis();
		int updates = 0;

		long now;
		while (true) {

			now = System.nanoTime();
			if (now - lastFrame >= timePerFrame) {
				if (gamescreen != null) gamescreen.repaint();
				lastFrame = now;
				frames++;
			}

			if (now - lastUpdate >= timePerUpdate) {
				updateGame();
				lastUpdate = now;
				updates++;
			}
			if (System.currentTimeMillis() - lastTimeCheck >= 1000) {
				System.out.println("FPS: " + frames + " | UPS: " + updates);
				frames = 0;
				updates = 0;
				lastTimeCheck = System.currentTimeMillis();
			}
		}
	}

	public Render getRender() {
		return render;
	}

	public Menu getMenu() {
		return menu;
	}

	public Options getOptions() {
		return options;
	}

	public Playing getPlaying() {
		return playing;
	}

	public Intro getIntro() {  return intro; }

	public MapEditing getMapEditing() { return mapEditing; }

	public LoadGameMenu getLoadGameMenu() { return loadGameMenu; }

	public LevelSelectionScene getNewGameLevelSelection() { return newGameLevelSelection; }

	public TileManager getTileManager() { return tileManager; }

	public GameOverScene getGameOverScene() {
		return gameOverScene;
	}

	public Cursor getCursor() {
		try {
			java.io.InputStream is = getClass().getResourceAsStream("/UI/01.png");
			if (is == null) {
				System.err.println("Draggable cursor image not found at /UI/01.png");
				return Cursor.getDefaultCursor();
			}

			BufferedImage originalImg = javax.imageio.ImageIO.read(is);

			int newWidth = originalImg.getWidth() / 2;
			int newHeight = originalImg.getHeight() / 2;

			BufferedImage resizedImg = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2d = resizedImg.createGraphics();
			g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g2d.drawImage(originalImg, 0, 0, newWidth, newHeight, null);
			g2d.dispose();

			return Toolkit.getDefaultToolkit().createCustomCursor(
					resizedImg,
					new Point(newWidth/2, newHeight/2),
					"Custom Draggable Cursor"
			);

		} catch (java.io.IOException e) {
			System.err.println("Error loading draggable cursor image: " + e.getMessage());
			return Cursor.getDefaultCursor();
		}
	}

	public void startPlayingWithDifficulty(int[][] levelData, int[][] overlayData, String mapName, String difficulty) {
		// Load the appropriate difficulty configuration
		config.GameOptions gameOptions = null;
		try {
			System.out.println("=== DIFFICULTY LOADING DEBUG ===");
			System.out.println("Selected difficulty: " + difficulty);

			// Before starting gameplay, save current custom settings to preserve them
			config.GameOptions currentSettings = helpMethods.OptionsIO.load();
			if (currentSettings != null) {
				helpMethods.OptionsIO.saveCustom(currentSettings);
				System.out.println("Saved current settings as custom settings for preservation");
			}

			if ("Easy".equals(difficulty)) {
				gameOptions = helpMethods.OptionsIO.load("easy");
				System.out.println("Loaded Easy difficulty. Starting gold: " + (gameOptions != null ? gameOptions.getStartingGold() : "NULL"));
			} else if ("Normal".equals(difficulty)) {
				gameOptions = helpMethods.OptionsIO.load("normal");
				System.out.println("Loaded Normal difficulty. Starting gold: " + (gameOptions != null ? gameOptions.getStartingGold() : "NULL"));
			} else if ("Hard".equals(difficulty)) {
				gameOptions = helpMethods.OptionsIO.load("hard");
				System.out.println("Loaded Hard difficulty. Starting gold: " + (gameOptions != null ? gameOptions.getStartingGold() : "NULL"));
			} else if ("Custom".equals(difficulty)) {
				// Use custom settings from custom_options.json
				gameOptions = helpMethods.OptionsIO.load("custom");
				if (gameOptions == null) {
					// Fallback to current options.json if custom doesn't exist
					gameOptions = helpMethods.OptionsIO.load();
				}
				System.out.println("Loaded Custom difficulty. Starting gold: " + (gameOptions != null ? gameOptions.getStartingGold() : "NULL"));
			}

			if (gameOptions != null) {
				// Save the difficulty options temporarily for gameplay (does not overwrite custom settings)
				helpMethods.OptionsIO.saveForGameplay(gameOptions);
				System.out.println("Saved temporary gameplay settings to options.json. Starting gold: " + gameOptions.getStartingGold());

				// Wait a moment to ensure file I/O completes
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}

				// Verify the save worked
				config.GameOptions verifyOptions = helpMethods.OptionsIO.load();
				System.out.println("Verification: Reloaded options.json. Starting gold: " + verifyOptions.getStartingGold());
			} else {
				System.out.println("ERROR: Failed to load difficulty configuration!");
			}
		} catch (Exception e) {
			System.err.println("Error loading difficulty configuration: " + e.getMessage());
			e.printStackTrace();
			// Fallback to default if loading fails
		}

		this.playing = new Playing(this, tileManager, levelData, overlayData);
		this.playing.setCurrentMapName(mapName);
		// Set the difficulty in the Playing scene (this will also update PlayingUI)
		this.playing.setCurrentDifficulty(difficulty);

		// Force reload of GameOptions after setting difficulty to ensure it takes effect
		if (this.playing != null) {
			this.playing.reloadGameOptions();
			System.out.println("=== FORCED RELOAD AFTER DIFFICULTY SET ===");
		}
	}

	public void resetGameWithSameLevel() {
		if (playing != null) {
			AudioManager.getInstance().stopAllSounds();

			playing.resetGameState();
			GameStates.setGameState(GameStates.PLAYING);

			if (gamescreen != null) {
				gamescreen.setPanelSize();
				gamescreen.revalidate();
				gamescreen.repaint();
			}

			pack();
			setLocationRelativeTo(null);

			AudioManager.getInstance().playRandomGameMusic();
		}
	}
	public GameStatsManager getStatsManager() {
		return statsManager;
	}
	public StatisticsScene getStatisticsScene() {
		return statisticsScene;
	}

	public SkillSelectionScene getSkillSelectionScene() {
		return skillSelectionScene;
	}

	public GameScreen getGameScreen() {
		return gamescreen;
	}

	public FullscreenManager getFullscreenManager() {
		return fullscreenManager;
	}
}