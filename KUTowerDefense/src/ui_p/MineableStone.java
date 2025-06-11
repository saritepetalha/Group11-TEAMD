package ui_p;

import constants.GameDimensions;
import helpMethods.LoadSave;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.IOException;

public class MineableStone {
    private int x, y;
    private boolean showChoices = false;
    private TheButton mineButton;
    private static BufferedImage buttonSheetImg;
    private BufferedImage mineButtonImage;
    private BufferedImage mineButtonHoverImage;

    public MineableStone(int x, int y) {
        this.x = x;
        this.y = y;
        int size = 32;

        loadButtonImageFile();
        mineButtonImage = buttonSheetImg.getSubimage(
                GameDimensions.TILE_DISPLAY_SIZE * 2,
                GameDimensions.TILE_DISPLAY_SIZE * 2,
                GameDimensions.TILE_DISPLAY_SIZE,
                GameDimensions.TILE_DISPLAY_SIZE
        );
        // Try to load hover image from AssetsLoader (if available)
        mineButtonHoverImage = null;
        try {
            mineButtonHoverImage = helpMethods.LoadSave.getImageFromPath("/UI/buttonHoveredAssets/mine_hover.png");
        } catch (Exception e) {
            mineButtonHoverImage = null;
        }
        mineButton = new TheButton("Mine", x + 16, y - size, size, size, mineButtonImage);
    }

    public void draw(Graphics g) {
        if (showChoices) {
            if (mineButton.isMouseOver() && mineButtonHoverImage != null) {
                g.drawImage(mineButtonHoverImage, mineButton.getX(), mineButton.getY(), mineButton.getWidth(), mineButton.getHeight(), null);
            } else {
                mineButton.draw(g);
            }
        }
    }

    public boolean isClicked(int mouseX, int mouseY) {
        Rectangle bounds = new Rectangle(x, y, GameDimensions.TILE_DISPLAY_SIZE, GameDimensions.TILE_DISPLAY_SIZE);
        return bounds.contains(mouseX, mouseY);
    }

    public boolean isMineButtonClicked(int mouseX, int mouseY) {
        return showChoices && mineButton.getBounds().contains(mouseX, mouseY);
    }

    public void setShowChoices(boolean showChoices) {
        this.showChoices = showChoices;
    }

    public boolean isShowChoices() {
        return showChoices;
    }

    public TheButton getMineButton() {
        return mineButton;
    }

    public int getX() { return x; }

    public int getY() { return y; }

    public static void loadButtonImageFile() {
        InputStream is = LoadSave.class.getResourceAsStream("/UI/kutowerbuttons4.png");
        try {
            buttonSheetImg = ImageIO.read(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void mouseMoved(int mouseX, int mouseY) {
        if (mineButton != null) {
            mineButton.setMouseOver(mineButton.getBounds().contains(mouseX, mouseY));
        }
    }
}
