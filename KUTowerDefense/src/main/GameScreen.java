package main;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;
import java.awt.Dimension;
import javax.swing.JPanel;

import inputs.KeyboardListener;
import inputs.MyMouseListener;

public class GameScreen extends JPanel {
	private Dimension size;

	private KeyboardListener keyboardListener;
	private MyMouseListener mouseListener;

	private Game game;
	

	public GameScreen(Game game) {
		
		this.game = game;

		
		setPanelSize();	
	}
	

	private void setPanelSize() {
		size = new Dimension(640, 640);
		setMinimumSize(size);
		setMaximumSize(size);
		setPreferredSize(size);
	}


	

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		game.getRender().render(g);
		


	}
	

	
	
	

}
