package main;

import javax.swing.JFrame;

public class Game extends JFrame {
	
	private GameScreen gamescreen;
	
	public Game() {
		setSize(400, 400);
		setVisible(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		
		gamescreen = new GameScreen();
		add(gamescreen);
		
	}

	public static void main(String[] args) {
		
		System.out.println("HELLO");
		Game game = new Game();

	}

}
