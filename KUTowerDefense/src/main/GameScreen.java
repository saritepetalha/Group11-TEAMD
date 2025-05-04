package main;

import java.awt.Graphics;
import java.awt.Dimension;
import javax.swing.JPanel;

import constants.GameDimensions;
import inputs.KeyboardListener;
import inputs.MyMouseListener;

public class GameScreen extends JPanel {
	private Dimension size;

	private MyMouseListener myMouseListener;
	private KeyboardListener keyboardListener;

	private Game game;

	public GameScreen(Game game) {
		this.game = game;
		setPanelSize();
	}

	public void initInputs() {
		myMouseListener = new MyMouseListener(game);
		keyboardListener = new KeyboardListener(game.getMapEditing(), game);

		addMouseListener(myMouseListener);
		addMouseMotionListener(myMouseListener);
		addMouseWheelListener(myMouseListener);
		addKeyListener(keyboardListener);

		requestFocus();
	}


	public void setPanelSize() {
		int width, height;

		switch (GameStates.gameState) {
			case INTRO:
				width = GameDimensions.MAIN_MENU_SCREEN_WIDTH;
				height = GameDimensions.MAIN_MENU_SCREEN_HEIGHT;
				break;

			case MENU:
				width = GameDimensions.MAIN_MENU_SCREEN_WIDTH;
				height = GameDimensions.MAIN_MENU_SCREEN_HEIGHT;
				break;

			// must be changed to normal game mode, since the editting mode is now on the playing case I didn't change it
			case PLAYING:
				width = GameDimensions.GAME_WIDTH;
				height = GameDimensions.GAME_HEIGHT;
				break;

			case EDIT:
				width = GameDimensions.TOTAL_GAME_WIDTH;
				height = GameDimensions.GAME_HEIGHT;
				break;

			default:
				width = GameDimensions.GAME_WIDTH;
				height = GameDimensions.GAME_HEIGHT;
				break;
		}

		size = new Dimension(width, height);
		setMinimumSize(size);
		setMaximumSize(size);
		setPreferredSize(size);

		setCursor(game.getCursor());

		revalidate();  // ensures proper refresh
		repaint();

	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		game.getRender().render(g);

	}

}
