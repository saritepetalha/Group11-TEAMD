package ui_p;

import helpMethods.LoadSave;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class DeadTree {

    int x,y, mouseX, mouseY;
    boolean showChoices = false;
    TheButton mageButton, archerButton, artilleryButton;
    private ArrayList<TheButton> theButtons = new ArrayList<>();
    private ArrayList<BufferedImage> ButtonImages = new ArrayList<>();

    private static BufferedImage buttonSheetImg;
    private static BufferedImage modeLabelImg;


    public DeadTree(int x, int y) {

        this.x =x;
        this.y = y;
        int size = 32;

        loadButtonImageFile();
        loadButtonImages();

        theButtons.add(mageButton = new TheButton("Mage", x - size + 16, y, size, size, ButtonImages.get(0)));
        theButtons.add(archerButton = new TheButton("Archer", x + 16, y - size, size, size, ButtonImages.get(1)));
        theButtons.add(artilleryButton = new TheButton("Artillery", x + size + 16, y, size, size, ButtonImages.get(2)));
    }

    public void draw(Graphics g) {
        if (showChoices){
            mageButton.draw(g);
            archerButton.draw(g);
            artilleryButton.draw(g);
        }
    }

    public static void loadButtonImageFile() {
        InputStream is = LoadSave.class.getResourceAsStream("/UI/kutowerbuttons4.png");
        try {
            buttonSheetImg = ImageIO.read(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void loadButtonImages() {
        int size = 64;

        ButtonImages.add(buttonSheetImg.getSubimage(size * 0, size * 2, size, size));
        ButtonImages.add(buttonSheetImg.getSubimage(size * 2, size * 2, size, size));
        ButtonImages.add(buttonSheetImg.getSubimage(size * 3, size * 2, size, size));


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

    public TheButton getArcherButton() {
        return archerButton;
    }

    public void setArcherButton(TheButton archerButton) {
        this.archerButton = archerButton;
    }

    public TheButton getMageButton() {
        return mageButton;
    }

    public void setMageButton(TheButton mageButton) {
        this.mageButton = mageButton;
    }

    public TheButton getArtilleryButton() {
        return artilleryButton;
    }

    public void setArtilleryButton(TheButton artilleryButton) {
        this.artilleryButton = artilleryButton;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
