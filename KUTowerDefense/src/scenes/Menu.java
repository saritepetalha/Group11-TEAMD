package scenes;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Random;
import javax.imageio.ImageIO;


import main.Game;
import ui_p.TheButton;

import static main.GameStates.*;

public class Menu extends GameScene implements SceneMethods {
    private ArrayList<BufferedImage> sprites = new ArrayList<>();
	private BufferedImage img;
    private Random random;
    private TheButton playButton, optionButton, exitButton;

    
    public Menu(Game game) {
        super(game);
        random = new Random();
        importImg();
        loadSprites();
        initButtons();
    }

    private void importImg() {

        InputStream is = getClass().getResourceAsStream("/Tiles/Tileset64.png");

        try {
            img = ImageIO.read(is);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void loadSprites() {
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 4; x++) {
                sprites.add(img.getSubimage(x * 64, y * 64, 64, 64));
            }
        }
    }

    private void initButtons() {
        playButton = new TheButton("Play", 100, 100, 200, 50);
        optionButton = new TheButton("Options", 100, 200, 200, 50);
        exitButton = new TheButton("Exit", 100, 300, 200, 50);
    }

    @Override
    public void render(Graphics g) {
        drawButtons(g);
    }

    private void drawButtons(Graphics g) {
        playButton.draw(g);
        optionButton.draw(g);
        exitButton.draw(g);
    }

    @Override
    public void mouseClicked(int x, int y) {
        if (playButton.getBounds().contains(x, y)) {
            setGameState(PLAYING);
        } else if (optionButton.getBounds().contains(x, y)) {
            setGameState(OPTIONS);
        } else if (exitButton.getBounds().contains(x, y)) {
            System.exit(0);
        }
    }

    @Override
    public void mouseMoved(int x, int y) {
        playButton.setMouseOver(false);
        optionButton.setMouseOver(false);
        exitButton.setMouseOver(false);

        if (playButton.getBounds().contains(x, y)) {
            playButton.setMouseOver(true);
        }else if (optionButton.getBounds().contains(x, y)) {
            optionButton.setMouseOver(true);
        }else if (exitButton.getBounds().contains(x, y)) {
            exitButton.setMouseOver(true);
        }
    }

    @Override
    public void mousePressed(int x, int y) {
        if (playButton.getBounds().contains(x, y)) {
            playButton.setMousePressed(true);
        } else if (optionButton.getBounds().contains(x, y)) {
            optionButton.setMousePressed(true);
        } else if (exitButton.getBounds().contains(x, y)) {
            exitButton.setMousePressed(true);
        }
    }

    @Override
    public void mouseReleased(int x, int y) {
        playButton.resetBooleans();
        optionButton.resetBooleans();
        exitButton.resetBooleans();
    }


    private int getRandomInt() {
		return random.nextInt(sprites.size());
	}

}
