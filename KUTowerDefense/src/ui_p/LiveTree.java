package ui_p;

import java.awt.*;
import java.awt.image.BufferedImage;

public class LiveTree {

    public int x,y;
    public boolean showChoices = false;
    public TheButton fireButton;
    private CostTooltip tooltip;

    public LiveTree(int x, int y) {
        this.x = x;
        this.y = y;
        int size = 32;

        fireButton = new TheButton("Fire", x + 16, y - size, size, size, AssetsLoader.getInstance().fireButtonNormal);

        this.tooltip = new CostTooltip();
    }

    public void draw(Graphics g) {
        if (showChoices){
            BufferedImage buttonImage = AssetsLoader.getInstance().fireButtonNormal;
            if (fireButton.isMouseOver()) {
                buttonImage = AssetsLoader.getInstance().fireButtonHover;
            }

            if (buttonImage != null) {
                g.drawImage(buttonImage, fireButton.getX(), fireButton.getY(),
                        fireButton.getWidth(), fireButton.getHeight(), null);
            }

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
