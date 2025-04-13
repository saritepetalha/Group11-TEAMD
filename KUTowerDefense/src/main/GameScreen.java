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
	

	private void setPanelSize() {
		size = new Dimension (GameDimensions.GAME_WIDTH + 4*GameDimensions.ButtonSize.MEDIUM.getSize(), GameDimensions.GAME_HEIGHT);
		setMinimumSize(size);
		setMaximumSize(size);
		setPreferredSize(size);
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		game.getRender().render(g);
		


	}
	

	
	
	

}
