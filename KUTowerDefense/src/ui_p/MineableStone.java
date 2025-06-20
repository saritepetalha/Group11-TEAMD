package ui_p;

import constants.GameDimensions;
import java.awt.*;
import java.awt.image.BufferedImage;

public class MineableStone {
    private int x, y;
    private boolean showChoices = false;
    private TheButton mineButton;
    public MineableStone(int x, int y) {
        this.x = x;
        this.y = y;
        int size = 32;

        // Use the proper pickaxe button image from AssetsLoader
        BufferedImage mineButtonImage = AssetsLoader.getInstance().pickaxeButtonImg;
        mineButton = new TheButton("Mine", x + 16, y - size, size, size, mineButtonImage);
    }

    public void draw(Graphics g) {
        if (showChoices) {
            // Simply draw the button without extra hover effects
            // Custom hover effects are now handled by StoneMiningManager
            mineButton.draw(g);
        }
    }

    public int getX() { return x; }

    public int getY() { return y; }


    public void mouseMoved(int mouseX, int mouseY) {
        if (mineButton != null) {
            mineButton.setMouseOver(mineButton.getBounds().contains(mouseX, mouseY));
        }
    }
}
