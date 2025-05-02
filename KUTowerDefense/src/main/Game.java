package main;

import javax.swing.JFrame;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import managers.SoundManager;

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
		createDefaultLevel();
		setCustomCursor();

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
		GameStates.gameState = newState;
		gamescreen.setPanelSize(); // adjust GameScreen size
		pack();                    // resize JFrame according to new dimensions
		setLocationRelativeTo(null); // re-center the window
		setCustomCursor();
	}

	private void initClasses() {
		SoundManager.getInstance();
		gamescreen = new GameScreen(this);
		render = new Render(this);
		intro = new Intro(this);
		menu = new Menu(this);
		options = new Options(this);
		playing = new Playing(this, this.tileManager);
		mapEditing = new MapEditing(this, this);
		loaded = new Loaded(this);
		tileManager = new TileManager();
	}


	public void start() {
		gameThread = new Thread(this);
		gameThread.start();
	}

	private void updateGame() {
		//System.out.println("GAME UPDATED");
		switch (GameStates.gameState) {
			case INTRO:
				intro.update();  // Update the intro animation
				break;
			case MENU:
				// menu update logic if needed
				break;
			case EDIT:
				break;
			case PLAYING:
				playing.update();
				// playing update logic
				break;
			case OPTIONS:
				// options update logic
				break;
			default:
				break;
		}
	}

	public static void main(String[] args) {

		System.out.println("HELLO");
		Game game = new Game();
		game.gamescreen.initInputs();
		game.start();
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
				repaint();
				lastFrame = now;

				frames++;
			}

			if (now - lastUpdate >= timePerUpdate) {
				lastUpdate = lastUpdate + (long)timePerUpdate;
				updateGame();
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

	public TileManager getTileManager() { return tileManager;
	}

	private void setCustomCursor() {
		try {
			java.io.InputStream is = getClass().getResourceAsStream("/UI/cursor.png");
			if (is == null) {
				return;
			}

			BufferedImage originalImg = javax.imageio.ImageIO.read(is);

			int newWidth = originalImg.getWidth() / 2;
			int newHeight = originalImg.getHeight() / 2;

			BufferedImage resizedImg = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2d = resizedImg.createGraphics();
			g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g2d.drawImage(originalImg, 0, 0, newWidth, newHeight, null);
			g2d.dispose();

			Cursor customCursor = Toolkit.getDefaultToolkit().createCustomCursor(
					resizedImg,
					new Point(newWidth/2, newHeight/2),
					"Custom Cursor"
			);

			setCursor(customCursor);

		} catch (java.io.IOException e) {
			System.err.println("Error loading cursor image: " + e.getMessage());
		}
	}

	public Cursor getCursor() {
		try {
			java.io.InputStream is = getClass().getResourceAsStream("/UI/01.png");
			if (is == null) {
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
					"Custom Cursor"
			);

		} catch (java.io.IOException e) {
			System.err.println("Error loading cursor image: " + e.getMessage());
			return Cursor.getDefaultCursor();
		}
	}
}
