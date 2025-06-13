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
		// Check if in fullscreen mode
		if (game.getFullscreenManager() != null && game.getFullscreenManager().isFullscreen()) {
			// In fullscreen mode, use the full screen size
			size = new Dimension(game.getFullscreenManager().getScreenWidth(),
					game.getFullscreenManager().getScreenHeight());
		} else {
			// In windowed mode, use appropriate dimensions based on game state
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
		setPreferredSize(size);
		revalidate();
		repaint();
	}

	@Override
	public void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;

		// Always fill background first
		if (game.getFullscreenManager() != null && game.getFullscreenManager().isFullscreen()) {
			// Fill entire screen with black for letterboxing
			g2d.setColor(java.awt.Color.BLACK);
			g2d.fillRect(0, 0, getWidth(), getHeight());
		}

		super.paintComponent(g);

		// Check if current state uses custom rendering (not JPanel-based)
		boolean usesCustomRendering = (GameStates.gameState != GameStates.OPTIONS &&
				GameStates.gameState != GameStates.LOAD_GAME &&
				GameStates.gameState != GameStates.NEW_GAME_LEVEL_SELECT &&
				GameStates.gameState != GameStates.SKILL_SELECTION);

		// For custom-rendered scenes in fullscreen, apply consistent scaling
		if (usesCustomRendering && game.getFullscreenManager() != null && game.getFullscreenManager().isFullscreen()) {
			// Get base dimensions for the current state
			int baseWidth, baseHeight;
			if (GameStates.gameState == GameStates.PLAYING || GameStates.gameState == GameStates.GAME_OVER) {
				// For playing state, use actual level dimensions if available
				if (game.getPlaying() != null && game.getPlaying().getController() != null
						&& game.getPlaying().getController().getModel() != null
						&& game.getPlaying().getController().getModel().getLevel() != null) {

					int[][] level = game.getPlaying().getController().getModel().getLevel();
					int levelCols = level[0].length;
					int levelRows = level.length;
					baseWidth = levelCols * GameDimensions.TILE_DISPLAY_SIZE;
					baseHeight = levelRows * GameDimensions.TILE_DISPLAY_SIZE;
				} else {
					// Fallback to default dimensions
					baseWidth = GameDimensions.GAME_WIDTH;
					baseHeight = GameDimensions.GAME_HEIGHT;
				}
			} else if (GameStates.gameState == GameStates.EDIT) {
				baseWidth = GameDimensions.TOTAL_GAME_WIDTH;
				baseHeight = GameDimensions.GAME_HEIGHT;
			} else {
				baseWidth = GameDimensions.MAIN_MENU_SCREEN_WIDTH;
				baseHeight = GameDimensions.MAIN_MENU_SCREEN_HEIGHT;
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
			// In windowed mode, just render normally
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

		if (newState == GameStates.OPTIONS) {
			if (game.getOptions() instanceof JPanel) {
				JPanel optionsPanel = (JPanel) game.getOptions();

				// Create a wrapper panel for proper fullscreen scaling
				if (game.getFullscreenManager() != null && game.getFullscreenManager().isFullscreen()) {
					JPanel wrapper = createScaledWrapper(optionsPanel,
							GameDimensions.MAIN_MENU_SCREEN_WIDTH,
							GameDimensions.MAIN_MENU_SCREEN_HEIGHT);
					this.add(wrapper, BorderLayout.CENTER);
				} else {
					this.add(optionsPanel, BorderLayout.CENTER);
				}
			} else {
				System.err.println("Error: Options scene is not a JPanel!");
			}
		} else if (newState == GameStates.LOAD_GAME) {
			if (game.getLoadGameMenu() instanceof LoadGameMenu) {
				LoadGameMenu loadMenu = game.getLoadGameMenu();

				if (game.getFullscreenManager() != null && game.getFullscreenManager().isFullscreen()) {
					JPanel wrapper = createScaledWrapper(loadMenu,
							GameDimensions.MAIN_MENU_SCREEN_WIDTH,
							GameDimensions.MAIN_MENU_SCREEN_HEIGHT);
					this.add(wrapper, BorderLayout.CENTER);
				} else {
					this.add(loadMenu, BorderLayout.CENTER);
				}
			} else {
				System.err.println("Error: LoadGameMenu is not a JPanel or is null!");
			}
		} else if (newState == GameStates.NEW_GAME_LEVEL_SELECT) {
			if (game.getNewGameLevelSelection() instanceof JPanel) {
				JPanel levelSelection = (JPanel) game.getNewGameLevelSelection();

				if (game.getFullscreenManager() != null && game.getFullscreenManager().isFullscreen()) {
					JPanel wrapper = createScaledWrapper(levelSelection,
							GameDimensions.MAIN_MENU_SCREEN_WIDTH,
							GameDimensions.MAIN_MENU_SCREEN_HEIGHT);
					this.add(wrapper, BorderLayout.CENTER);
				} else {
					this.add(levelSelection, BorderLayout.CENTER);
				}
			} else {
				System.err.println("Error: NewGameLevelSelection is not a JPanel or is null!");
			}
		} else if (newState == GameStates.SKILL_SELECTION) {
			if (game.getSkillSelectionScene() instanceof JPanel) {
				JPanel skillSelection = (JPanel) game.getSkillSelectionScene();

				if (game.getFullscreenManager() != null && game.getFullscreenManager().isFullscreen()) {
					JPanel wrapper = createScaledWrapper(skillSelection,
							GameDimensions.MAIN_MENU_SCREEN_WIDTH,
							GameDimensions.MAIN_MENU_SCREEN_HEIGHT);
					this.add(wrapper, BorderLayout.CENTER);
				} else {
					this.add(skillSelection, BorderLayout.CENTER);
				}
			} else {
				System.err.println("Error: SkillSelectionScene is not a JPanel or is null!");
			}
		}

		this.revalidate();
		this.repaint();
	}

	/**
	 * Creates a scaled wrapper panel for fullscreen mode
	 */
	private JPanel createScaledWrapper(JPanel content, int baseWidth, int baseHeight) {
		JPanel wrapper = new JPanel(null) { // Use null layout for absolute positioning
			@Override
			protected void paintComponent(Graphics g) {
				// Fill background with black for letterboxing
				g.setColor(java.awt.Color.BLACK);
				g.fillRect(0, 0, getWidth(), getHeight());
			}

			@Override
			public void doLayout() {
				if (getComponentCount() > 0 && game.getFullscreenManager() != null) {
					// Calculate scaling
					double scaleX = (double) getWidth() / baseWidth;
					double scaleY = (double) getHeight() / baseHeight;
					double scale = Math.min(scaleX, scaleY);

					int scaledWidth = (int) (baseWidth * scale);
					int scaledHeight = (int) (baseHeight * scale);
					int offsetX = (getWidth() - scaledWidth) / 2;
					int offsetY = (getHeight() - scaledHeight) / 2;

					// Set the content panel bounds to scaled size and centered position
					getComponent(0).setBounds(offsetX, offsetY, scaledWidth, scaledHeight);
				}
			}
		};

		// Set content panel to its preferred size
		content.setPreferredSize(new Dimension(baseWidth, baseHeight));
		content.setSize(baseWidth, baseHeight);

		wrapper.add(content);
		wrapper.setBackground(java.awt.Color.BLACK);

		return wrapper;
	}

	public MyMouseListener getMyMouseListener() {
		return myMouseListener;
	}

	/**
	 * Called when fullscreen mode changes to refresh the current state
	 */
	public void refreshForFullscreenChange() {
		GameStates currentState = GameStates.gameState;
		boolean isFullscreen = game.getFullscreenManager() != null && game.getFullscreenManager().isFullscreen();
		System.out.println("🔄 Refreshing screen for state: " + currentState + ", Fullscreen: " + isFullscreen);
		updateContentForState(currentState, currentState);
		setPanelSize();
	}

}
