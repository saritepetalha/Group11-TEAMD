package main;

import java.awt.Graphics;
import java.awt.Dimension;
import java.awt.BorderLayout;
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
		if (GameStates.gameState == GameStates.MENU ||
				GameStates.gameState == GameStates.INTRO ||
				GameStates.gameState == GameStates.LOAD_GAME ||
				GameStates.gameState == GameStates.OPTIONS) {
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
		setPreferredSize(size);
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (GameStates.gameState != GameStates.OPTIONS && GameStates.gameState != GameStates.LOAD_GAME) {
			game.getRender().render(g);
		}
	}

	public void updateContentForState(GameStates newState, GameStates oldState) {
		if (oldState == GameStates.OPTIONS && game.getOptions() instanceof scenes.Options) {
			((scenes.Options) game.getOptions()).cleanUp();
		}
		if (oldState == GameStates.LOAD_GAME && game.getLoadGameMenu() instanceof LoadGameMenu) {
			// No specific cleanup needed for LoadGameMenu panel removal by default
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
		}

		this.revalidate();
		this.repaint();
	}

	public MyMouseListener getMyMouseListener() {
		return myMouseListener;
	}

}
