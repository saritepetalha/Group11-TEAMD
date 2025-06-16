package main;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

import constants.GameDimensions;
import inputs.KeyboardListener;
import inputs.MyMouseListener;
import scenes.LoadGameMenu;

public class GameScreen extends JPanel {
	private Dimension size;

	private MyMouseListener myMouseListener;
	private KeyboardListener keyboardListener;

	private Game game;

	public GameScreen(Game game) {
		this.game = game;
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
		size = new Dimension(GameDimensions.MAIN_MENU_SCREEN_WIDTH, GameDimensions.MAIN_MENU_SCREEN_HEIGHT);
		setPreferredSize(size);
	}

	public void setPanelSize() {
		// Check if in fullscreen mode and in PLAYING state
		if (game.getFullscreenManager() != null && game.getFullscreenManager().isFullscreen() &&
				GameStates.gameState == GameStates.PLAYING) {
			// In fullscreen mode for playing only, use the full screen size
			size = new Dimension(game.getFullscreenManager().getScreenWidth(),
					game.getFullscreenManager().getScreenHeight());
		} else {
			// In windowed mode or non-playing states, use appropriate dimensions based on game state
			if (GameStates.gameState == GameStates.MENU ||
					GameStates.gameState == GameStates.INTRO ||
					GameStates.gameState == GameStates.LOAD_GAME ||
					GameStates.gameState == GameStates.NEW_GAME_LEVEL_SELECT ||
					GameStates.gameState == GameStates.OPTIONS ||
					GameStates.gameState == GameStates.SKILL_SELECTION) {
				size = new Dimension(GameDimensions.MAIN_MENU_SCREEN_WIDTH, GameDimensions.MAIN_MENU_SCREEN_HEIGHT);
			} else if (GameStates.gameState == GameStates.EDIT){
				size = new Dimension(GameDimensions.TOTAL_GAME_WIDTH, GameDimensions.GAME_HEIGHT);
			} else if (GameStates.gameState == GameStates.PLAYING){
				size = new Dimension(GameDimensions.GAME_WIDTH, GameDimensions.GAME_HEIGHT);
			}
			else if (GameStates.gameState == GameStates.GAME_OVER) {
				size = new Dimension(GameDimensions.GAME_WIDTH, GameDimensions.GAME_HEIGHT);
			}
			else {
				size = new Dimension(GameDimensions.MAIN_MENU_SCREEN_WIDTH, GameDimensions.MAIN_MENU_SCREEN_HEIGHT);
			}
		}

		// Force size update and clear any cached dimensions
		setPreferredSize(size);
		setSize(size);
		setMinimumSize(size);
		setMaximumSize(size);

		revalidate();
		repaint();
	}

	@Override
	public void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;

		// Always fill background first
		if (game.getFullscreenManager() != null && game.getFullscreenManager().isFullscreen() &&
				GameStates.gameState == GameStates.PLAYING) {
			// Fill entire screen with black for letterboxing (only in playing mode)
			g2d.setColor(java.awt.Color.BLACK);
			g2d.fillRect(0, 0, getWidth(), getHeight());
		}

		super.paintComponent(g);

		// Check if current state uses custom rendering (not JPanel-based)
		boolean usesCustomRendering = (GameStates.gameState != GameStates.OPTIONS &&
				GameStates.gameState != GameStates.LOAD_GAME &&
				GameStates.gameState != GameStates.NEW_GAME_LEVEL_SELECT &&
				GameStates.gameState != GameStates.SKILL_SELECTION);

		// For custom-rendered scenes in fullscreen, apply consistent scaling (only for playing mode)
		if (usesCustomRendering && game.getFullscreenManager() != null && game.getFullscreenManager().isFullscreen() &&
				GameStates.gameState == GameStates.PLAYING) {
			// Get base dimensions for the playing state
			int baseWidth, baseHeight;

			// For playing state, use actual level dimensions if available
			if (game.getPlaying() != null && game.getPlaying().getLevel() != null) {
				int[][] level = game.getPlaying().getLevel();
				int levelCols = level[0].length;
				int levelRows = level.length;
				baseWidth = levelCols * GameDimensions.TILE_DISPLAY_SIZE;
				baseHeight = levelRows * GameDimensions.TILE_DISPLAY_SIZE;
			} else {
				// Fallback to default dimensions
				baseWidth = GameDimensions.GAME_WIDTH;
				baseHeight = GameDimensions.GAME_HEIGHT;
			}

			// Calculate scaling using the same method as createScaledWrapper
			double scaleX = (double) getWidth() / baseWidth;
			double scaleY = (double) getHeight() / baseHeight;
			double scale = Math.min(scaleX, scaleY);

			int scaledWidth = (int) (baseWidth * scale);
			int scaledHeight = (int) (baseHeight * scale);
			int offsetX = (getWidth() - scaledWidth) / 2;
			int offsetY = (getHeight() - scaledHeight) / 2;

			// Apply transformation
			g2d.translate(offsetX, offsetY);
			g2d.scale(scale, scale);

			// Render game content
			game.getRender().render(g2d);

			// Reset transformation
			g2d.scale(1.0/scale, 1.0/scale);
			g2d.translate(-offsetX, -offsetY);
		} else if (usesCustomRendering) {
			// In windowed mode or non-playing states, just render normally
			game.getRender().render(g2d);
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

		// Note: Fullscreen scaling only applies to PLAYING mode,
		// other states remain in windowed mode even if technically in fullscreen
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

		// Ensure focus is maintained for keyboard input
		this.requestFocusInWindow();

		// Debug: Log focus state
		System.out.println("GameScreen focus state after state change: " + this.hasFocus());
	}

	public MyMouseListener getMyMouseListener() {
		return myMouseListener;
	}

	public void refreshForFullscreenChange() {
		// This method is called when fullscreen state changes
		setPanelSize();
		revalidate();
		repaint();

		// Ensure focus is maintained after fullscreen change
		requestFocusInWindow();
	}
}
