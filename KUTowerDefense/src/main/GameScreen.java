package main;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;

import javax.swing.JPanel;

import constants.GameDimensions;
import inputs.KeyboardListener;
import inputs.MyMouseListener;
import scenes.LoadGameMenu;

public class GameScreen extends JPanel {
	private Dimension size;
	private Dimension baseSize;
	private double scaleX = 1.0;
	private double scaleY = 1.0;

	private MyMouseListener myMouseListener;
	private KeyboardListener keyboardListener;

	private Game game;

	public GameScreen(Game game) {
		this.game = game;
		this.baseSize = new Dimension(GameDimensions.MAIN_MENU_SCREEN_WIDTH, GameDimensions.MAIN_MENU_SCREEN_HEIGHT);
		setPanelInitialSize();
		initInputs();
		setLayout(new BorderLayout());
	}

	private void initInputs() {
		myMouseListener = new MyMouseListener(game);
		keyboardListener = new KeyboardListener(game.getMapEditing() != null ? game.getMapEditing() : null, game);

		addMouseListener(myMouseListener);
		addMouseMotionListener(myMouseListener);
		addMouseWheelListener(myMouseListener);
		addKeyListener(keyboardListener);

		setFocusable(true);
		requestFocusInWindow();
	}

	private void setPanelInitialSize() {
		size = new Dimension(baseSize);
		setPreferredSize(size);
	}

	public void updateScreenSize() {
		if (game.isFullscreen()) {
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			size = new Dimension(screenSize);

			// Calculate scaling factors while maintaining aspect ratio
			double screenRatio = (double) screenSize.width / screenSize.height;
			double gameRatio = (double) baseSize.width / baseSize.height;

			if (screenRatio > gameRatio) {
				// Screen is wider than game ratio - fit to height
				scaleY = (double) screenSize.height / baseSize.height;
				scaleX = scaleY;
			} else {
				// Screen is taller than game ratio - fit to width
				scaleX = (double) screenSize.width / baseSize.width;
				scaleY = scaleX;
			}
		} else {
			size = new Dimension(baseSize);
			scaleX = 1.0;
			scaleY = 1.0;
		}
		setPreferredSize(size);
		revalidate();
		repaint();
	}

	public void setPanelSize() {
		if (GameStates.gameState == GameStates.MENU ||
				GameStates.gameState == GameStates.INTRO ||
				GameStates.gameState == GameStates.LOAD_GAME ||
				GameStates.gameState == GameStates.NEW_GAME_LEVEL_SELECT ||
				GameStates.gameState == GameStates.OPTIONS ||
				GameStates.gameState == GameStates.SKILL_SELECTION) {
			baseSize = new Dimension(GameDimensions.MAIN_MENU_SCREEN_WIDTH, GameDimensions.MAIN_MENU_SCREEN_HEIGHT);
		} else if (GameStates.gameState == GameStates.EDIT){
			baseSize = new Dimension(GameDimensions.TOTAL_GAME_WIDTH, GameDimensions.GAME_HEIGHT);
		} else if (GameStates.gameState == GameStates.PLAYING){
			baseSize = new Dimension(GameDimensions.GAME_WIDTH, GameDimensions.GAME_HEIGHT);
		}
		else if (GameStates.gameState == GameStates.GAME_OVER) {
			baseSize = new Dimension(GameDimensions.GAME_WIDTH, GameDimensions.GAME_HEIGHT);
		}
		else {
			baseSize = new Dimension(GameDimensions.MAIN_MENU_SCREEN_WIDTH, GameDimensions.MAIN_MENU_SCREEN_HEIGHT);
		}
		updateScreenSize();
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;

		// Enable antialiasing for smoother rendering
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		if (game.isFullscreen()) {
			// Calculate centered position for the game area
			int gameX = (getWidth() - (int)(baseSize.width * scaleX)) / 2;
			int gameY = (getHeight() - (int)(baseSize.height * scaleY)) / 2;

			// Translate to center the game area
			g2d.translate(gameX, gameY);

			// Scale the graphics context
			g2d.scale(scaleX, scaleY);
		}

		if (GameStates.gameState != GameStates.OPTIONS &&
				GameStates.gameState != GameStates.LOAD_GAME &&
				GameStates.gameState != GameStates.NEW_GAME_LEVEL_SELECT &&
				GameStates.gameState != GameStates.SKILL_SELECTION) {
			game.getRender().render(g2d);
		}

		if (game.isFullscreen()) {
			// Reset transform
			g2d.scale(1/scaleX, 1/scaleY);
			g2d.translate(-(getWidth() - (int)(baseSize.width * scaleX)) / 2,
					-(getHeight() - (int)(baseSize.height * scaleY)) / 2);
		}
	}

	public void updateContentForState(GameStates newState, GameStates oldState) {
		if (oldState == GameStates.OPTIONS && game.getOptions() instanceof scenes.Options) {
			((scenes.Options) game.getOptions()).cleanUp();
		}
		if (oldState == GameStates.LOAD_GAME && game.getLoadGameMenu() instanceof LoadGameMenu) {
			// No specific cleanup needed for LoadGameMenu panel removal by default
		}
		if (oldState == GameStates.NEW_GAME_LEVEL_SELECT) {
			// No specific cleanup needed for LevelSelectionScene panel removal by default
		}

		this.removeAll();

		if (newState == GameStates.OPTIONS) {
			if (game.getOptions() instanceof JPanel) {
				this.add((JPanel) game.getOptions(), BorderLayout.CENTER);
			} else {
				System.err.println("Error: Options scene is not a JPanel!");
			}
		} else if (newState == GameStates.LOAD_GAME) {
			if (game.getLoadGameMenu() instanceof LoadGameMenu) {
				this.add(game.getLoadGameMenu(), BorderLayout.CENTER);
			} else {
				System.err.println("Error: LoadGameMenu is not a JPanel or is null!");
			}
		} else if (newState == GameStates.NEW_GAME_LEVEL_SELECT) {
			if (game.getNewGameLevelSelection() instanceof JPanel) {
				this.add(game.getNewGameLevelSelection(), BorderLayout.CENTER);
			} else {
				System.err.println("Error: NewGameLevelSelection is not a JPanel or is null!");
			}
		} else if (newState == GameStates.SKILL_SELECTION) {
			if (game.getSkillSelectionScene() instanceof JPanel) {
				this.add(game.getSkillSelectionScene(), BorderLayout.CENTER);
			} else {
				System.err.println("Error: SkillSelectionScene is not a JPanel or is null!");
			}
		}

		this.revalidate();
		this.repaint();
	}

	public MyMouseListener getMyMouseListener() {
		return myMouseListener;
	}

	// Add methods to handle mouse coordinates for proper scaling
	public int getScaledMouseX(int mouseX) {
		if (game.isFullscreen()) {
			int gameX = (getWidth() - (int)(baseSize.width * scaleX)) / 2;
			return (int)((mouseX - gameX) / scaleX);
		}
		return mouseX;
	}

	public int getScaledMouseY(int mouseY) {
		if (game.isFullscreen()) {
			int gameY = (getHeight() - (int)(baseSize.height * scaleY)) / 2;
			return (int)((mouseY - gameY) / scaleY);
		}
		return mouseY;
	}
}
