package main;

import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;

import managers.AudioManager;
import managers.GameStatsManager;
import managers.TileManager;
import scenes.GameOverScene;
import scenes.Intro;
import scenes.LevelSelectionScene;
import scenes.LoadGameMenu;
import scenes.Loaded;
import scenes.MapEditing;
import scenes.Menu;
import scenes.Options;
import scenes.Playing;
import scenes.StatisticsScene;

public class Game extends JFrame implements Runnable{

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
	private Loaded loaded;
	private LoadGameMenu loadGameMenu;
	private LevelSelectionScene newGameLevelSelection;
	private GameOverScene gameOverScene;
	private StatisticsScene statisticsScene;
	private TileManager tileManager;
	private GameStatsManager statsManager;


	public Game() {
		System.out.println("Game Starting...");

		// Quick resource test for IntelliJ debugging
		java.net.URL resourceTest = Game.class.getResource("/UI/Save_Button_For_In_Game_Options.png");
		if (resourceTest != null) {
			System.out.println("✅ Save button resource found at: " + resourceTest);
		} else {
			System.err.println("❌ Save button resource NOT found!");
			System.err.println("   This indicates a classpath/resource issue in IntelliJ");
		}

		this.tileManager = new TileManager();

		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setResizable(false);
		initClasses();
		add(gamescreen);
		pack();
		setVisible(true);
		setLocationRelativeTo(null);
	}

	private void createDefaultLevel() {
		int[][] bruh = new int[20][20];
		for (int i = 0; i < 20; i++) {
			for (int j = 0; j < 20; j++) {
				bruh[i][j] = 0;
			}
		}
		//LoadSave.createLevel("defaultleveltest1", bruh);
	}

	public void changeGameState(GameStates newState) {
		GameStates previousState = GameStates.gameState;
		GameStates.gameState = newState;

		AudioManager audioManager = AudioManager.getInstance();
		boolean isMenuRelatedToggle = (
				(previousState == GameStates.MENU && (newState == GameStates.OPTIONS || newState == GameStates.EDIT || newState == GameStates.LOAD_GAME)) ||
						((previousState == GameStates.OPTIONS || previousState == GameStates.EDIT || previousState == GameStates.LOAD_GAME) && newState == GameStates.MENU)
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
					audioManager.playMusic("lonelyhood");
					break;
				case PLAYING:
					// Reset game state BEFORE making window visible to prevent black flash
					if (playing != null && previousState != GameStates.LOAD_GAME) {
						playing.resetGameState();
					}
					audioManager.playRandomGameMusic();
					break;
				case NEW_GAME_LEVEL_SELECT:
					audioManager.playMusic("lonelyhood");
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

		pack();
		setLocationRelativeTo(null);

		if (newState == GameStates.LOAD_GAME) {
			if (loadGameMenu != null) {
				loadGameMenu.refreshMapPreviews();
			}
		}

		if (gamescreen != null) {
			gamescreen.setPanelSize();
			gamescreen.revalidate();
			gamescreen.repaint();
		}
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
		loaded = new Loaded(this);
		loadGameMenu = new LoadGameMenu(this);
		newGameLevelSelection = new LevelSelectionScene(this, new levelselection.AllLevelsStrategy());
		gameOverScene = new GameOverScene(this);
		statisticsScene = new StatisticsScene(this);
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

	public void startPlayingWithLevel(int[][] levelData, int[][] overlayData) {
		this.playing = new Playing(this, tileManager, levelData, overlayData);
	}

	public void startPlayingWithLevel(int[][] levelData, int[][] overlayData, String mapName) {
		this.playing = new Playing(this, tileManager, levelData, overlayData);
		this.playing.setCurrentMapName(mapName);
		this.playing.loadGameState();
	}

	public void resetGameWithSameLevel() {
		if (playing != null) {
			playing.resetGameState();
			GameStates.setGameState(GameStates.PLAYING);

			if (gamescreen != null) {
				gamescreen.setPanelSize();
				gamescreen.revalidate();
				gamescreen.repaint();
			}

			pack();
			setLocationRelativeTo(null);
		}
	}
	public GameStatsManager getStatsManager() {
		return statsManager;
	}
	public StatisticsScene getStatisticsScene() {
		return statisticsScene;
	}
	public void repaintGameScreen() {
		if (gamescreen != null)
			gamescreen.repaint();
	}
}
