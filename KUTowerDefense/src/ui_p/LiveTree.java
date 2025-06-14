package ui_p;

import constants.GameDimensions;
import helpMethods.LoadSave;
import ui_p.AssetsLoader;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class LiveTree {

    public int x,y;
    public boolean showChoices = false;
    public TheButton fireButton;
    private CostTooltip tooltip;

    private BufferedImage fireButtonImage;
    private static BufferedImage buttonSheetImg;

    public LiveTree(int x, int y) {
        this.x = x;
        this.y = y;
        int size = 32;

        loadButtonImageFile();
        fireButtonImage = buttonSheetImg.getSubimage(GameDimensions.TILE_DISPLAY_SIZE * 2, GameDimensions.TILE_DISPLAY_SIZE * 2, GameDimensions.TILE_DISPLAY_SIZE, GameDimensions.TILE_DISPLAY_SIZE);
        fireButton = new TheButton("Fire", x + 16, y - size, size, size, fireButtonImage);
        
        this.tooltip = new CostTooltip();
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
            if (fireButton.isMouseOver()) {
                Graphics2D g2d = (Graphics2D) g;
                long currentTime = System.currentTimeMillis();
                float alpha = (float) (0.5f + 0.5f * Math.sin(currentTime * 0.003));
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                g2d.setColor(new Color(255, 255, 255, 100));
                g2d.setStroke(new BasicStroke(3f));
                g2d.drawRoundRect(fireButton.getX() - 2, fireButton.getY() - 2, fireButton.getWidth() + 4, fireButton.getHeight() + 4, 8, 8);
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            }
            fireButton.draw(g);
            // Update and draw tooltip
            tooltip.update();
            tooltip.draw((Graphics2D) g);
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
    
    /**
     * Handles mouse hover for tooltips - should be called from Playing/Controller
     */
    public void handleMouseHover(int mouseX, int mouseY, models.PlayingModel playing) {
        if (!showChoices) {
            tooltip.hide();
            return;
        }
        
        if (fireButton.getBounds().contains(mouseX, mouseY)) {
            int cost = constants.Constants.BURN_TREE_COST;
            boolean canAfford = playing.getPlayerManager().getGold() >= cost;
            tooltip.show("Burn Tree", cost, 
                "Remove this tree to build towers here. Creates a dead tree.", 
                canAfford, mouseX, mouseY);
        } else {
            tooltip.hide();
        }
    }

    public void mouseMoved(int mouseX, int mouseY) {
        fireButton.setMouseOver(fireButton.getBounds().contains(mouseX, mouseY));
    }
}
