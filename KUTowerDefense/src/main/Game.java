package main;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import inputs.MyMouseListener;
import inputs.KeyboardListener;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import scenes.Menu;
import scenes.Options;
import scenes.Playing;
public class Game extends JFrame implements Runnable{
	
	private GameScreen gamescreen;

	private Thread gameThread;

	private final double FPS_SET = 120.0;
	private final double UPS_SET = 60.0;

	private Render render;
	private Menu menu;
	private Options options;
	private Playing playing;
	public Game() {

		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setResizable(false);
		initClasses();

		add(gamescreen);
		pack();
		setVisible(true);
		setLocationRelativeTo(null);
	}

	public void changeGameState(GameStates newState) {
		GameStates.gameState = newState;
		gamescreen.setPanelSize(); // adjust GameScreen size
		pack();                    // resize JFrame according to new dimensions
		setLocationRelativeTo(null); // re-center the window
	}


	private void initClasses() {
		gamescreen = new GameScreen(this);
		render = new Render(this);
		menu = new Menu(this);
		options = new Options(this);
		playing = new Playing(this);
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
		game.gamescreen.initInputs();
		game.start();
	}

	
	@Override
	public void run() {
		double timePerFrame = 1000000000.0 / FPS_SET;
		double timePerUpdate = 1000000000.0 / UPS_SET;

		long lastFrame = System.nanoTime();
		long lastUpdate = System.nanoTime();
		int frames = 0;
		long lastTimeCheck = System.currentTimeMillis(); 
		int updates = 0;
		
		long now;
		while (true) {

			now = System.nanoTime();
			if (now - lastFrame >= timePerFrame) {
				repaint();
				lastFrame = now;
	
				frames++;
			}

			if (now - lastUpdate >= timePerUpdate) {
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

	public Render getRender() {
		return render;
	}

	public Menu getMenu() {
		return menu;
	}

	public Options getOptions() {
		return options;
	}

	public Playing getPlaying() {
		return playing;
	}
}
