package main;

import javax.swing.JFrame;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import managers.AudioManager;

import managers.TileManager;
import scenes.*;

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
	private TileManager tileManager;

	public Game() {
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

		if (gamescreen != null) {
			gamescreen.updateContentForState(newState, previousState);
		}

		if (gamescreen != null) {
			gamescreen.setPanelSize();
		}
		pack();
		setLocationRelativeTo(null);

		AudioManager audioManager = AudioManager.getInstance();
		boolean isMenuRelatedToggle = (
				(previousState == GameStates.MENU && (newState == GameStates.OPTIONS || newState == GameStates.EDIT)) ||
						((previousState == GameStates.OPTIONS || previousState == GameStates.EDIT) && newState == GameStates.MENU)
		);

		// Reload game options in Playing when returning from Options to Menu
		if (previousState == GameStates.OPTIONS && newState == GameStates.MENU) {
			if (playing != null) {
				playing.reloadGameOptions();
			}
		}

		if (!isMenuRelatedToggle) {
			switch (newState) {
				case MENU:
					audioManager.playMusic("lonelyhood");
					break;
				case PLAYING:
					if (playing != null) {
						// Reset game state and reload options
						playing.resetGameState();
					}
					audioManager.playRandomGameMusic();
					break;
				case INTRO:
				case OPTIONS:
				case EDIT:
				case LOADED:
				default:
					break;
			}
		}
	}

	private void initClasses() {
		AudioManager.getInstance();
		gamescreen = new GameScreen(this);
		render = new Render(this);
		intro = new Intro(this);
		menu = new Menu(this);
		options = new Options(this);
		if (playing == null) {
			playing = new Playing(this, tileManager);
		}
		mapEditing = new MapEditing(this, this);
		loaded = new Loaded(this);
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
				break;
			case EDIT:
				break;
			case PLAYING:
				if (playing != null) playing.update();
				break;
			case OPTIONS:
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

	public TileManager getTileManager() { return tileManager; }


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
}
