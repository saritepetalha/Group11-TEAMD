package main;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;
import java.awt.Dimension;
import javax.swing.JPanel;

import dimensions.GameDimensions;
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
		keyboardListener = new KeyboardListener();

		addMouseListener(myMouseListener);
		addMouseMotionListener(myMouseListener);
		addKeyListener(keyboardListener);

		requestFocus();
	}
	

	public void setPanelSize() {
		int width, height;

		switch (GameStates.gameState) {
			case MENU:
				width = GameDimensions.MAIN_MENU_SCREEN_WIDTH;
				height = GameDimensions.MAIN_MENU_SCREEN_HEIGHT;
				break;

			// must be changed to normal game mode, since the editting mode is now on the playing case I didn't change it
			case PLAYING:
				width = GameDimensions.TOTAL_GAME_WIDTH;	//width = GameDimensions.GAME_WIDTH
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

		size = new Dimension (width, height);
		setMinimumSize(size);
		setMaximumSize(size);
		setPreferredSize(size);

		revalidate();  // ensures proper refresh
		repaint();
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		game.getRender().render(g);
		


	}
	

	
	
	

}
