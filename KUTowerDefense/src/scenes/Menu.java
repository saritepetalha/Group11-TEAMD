package scenes;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Random;
import javax.imageio.ImageIO;

import main.Game;

public class Menu extends GameScene implements SceneMethods {
    private ArrayList<BufferedImage> sprites = new ArrayList<>();
	private BufferedImage img;
    private Random random;

    
    public Menu(Game game) {
        super(game);
        random = new Random();
        importImg();
        loadSprites();
    }

    @Override
    public void render(Graphics g) {
        for (int y = 0; y < 20; y++) {
            for (int x = 0; x < 20; x++) {
                g.drawImage(sprites.get(getRandomInt()), x * 64, y * 64, null);
            }		
        }
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


    private int getRandomInt() {
		return random.nextInt(sprites.size());
	}

}
