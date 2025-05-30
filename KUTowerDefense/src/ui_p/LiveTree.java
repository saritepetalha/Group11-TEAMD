package ui_p;

import constants.GameDimensions;
import helpMethods.LoadSave;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class LiveTree {

    public int x,y;
    public boolean showChoices = false;
    public TheButton fireButton;

    private BufferedImage fireButtonImage;
    private static BufferedImage buttonSheetImg;

    public LiveTree(int x, int y) {
        this.x = x;
        this.y = y;
        int size = 32;

        loadButtonImageFile();
        fireButtonImage = buttonSheetImg.getSubimage(GameDimensions.TILE_DISPLAY_SIZE * 2, GameDimensions.TILE_DISPLAY_SIZE * 2, GameDimensions.TILE_DISPLAY_SIZE, GameDimensions.TILE_DISPLAY_SIZE);
        fireButton = new TheButton("Fire", x + 16, y - size, size, size, fireButtonImage);

    }
    public static void loadButtonImageFile() {
        InputStream is = LoadSave.class.getResourceAsStream("/UI/kutowerbuttons4.png");
        try {
            buttonSheetImg = ImageIO.read(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void draw(Graphics g) {
        if (showChoices){
            fireButton.draw(g);
        }
    }

    public boolean isClicked(int mouseX, int mouseY) {
        Rectangle bounds = new Rectangle(x, y, 64, 64);
        return bounds.contains(mouseX, mouseY);
    }
    public boolean isShowChoices() {
        return showChoices;
    }

    public void setShowChoices(boolean showChoices) {
        this.showChoices = showChoices;
    }

    public int getX() {return x;}

    public int getY() {return y;}

    public TheButton getFireButton() {
        return fireButton;
    }
}
