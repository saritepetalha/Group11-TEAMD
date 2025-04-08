package main;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

public class Game extends JFrame implements Runnable{
	
	private GameScreen gamescreen;
	private BufferedImage img;
	private Thread gameThread;

	private final double FPS_SET = 120.0;
	private final double UPS_SET = 60.0;

	public Game() {

		importImg();
		
		setSize(640, 640);
		
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		
		gamescreen = new GameScreen(img);
		add(gamescreen);
		setVisible(true);
	}

	private void importImg() {
		
		InputStream is = getClass().getResourceAsStream("/spriteatlas.png");
		
		try {
			img = ImageIO.read(is);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	public void start() {
		gameThread = new Thread(this);
		gameThread.start();
	}


	private void updateGame() {
		//System.out.println("GAME UPDATED");
	}

	

	public static void main(String[] args) {
		
		System.out.println("HELLO");
		Game game = new Game();
		game.start();
	}

	@Override
	public void run() {
		double 	timePerUpdate = 1000000000.0 / FPS_SET;
		double timePerFrame = 1000000000.0 / UPS_SET;

		long lastFrame = System.nanoTime();
		long lastUpdate = System.nanoTime();
		int frames = 0;
		long lastTimeCheck = System.currentTimeMillis(); 
		int updates = 0;
		
		while (true) {
			if (System.nanoTime() - lastFrame >= timePerFrame) {
				repaint();
				lastFrame = System.nanoTime();
	
				frames++;
			}

			if (System.nanoTime() - lastUpdate >= timePerUpdate) {
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
}
