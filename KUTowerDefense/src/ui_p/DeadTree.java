package ui_p;

import helpMethods.LoadSave;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class DeadTree {

    public int x,y;
    public boolean showChoices = false;
    public TheButton mageButton, archerButton, artilleryButton, poisonButton;
    private CostTooltip tooltip;
    private ArrayList<TheButton> theButtons = new ArrayList<>();
    private ArrayList<BufferedImage> ButtonImages = new ArrayList<>();

    private static BufferedImage buttonSheetImg;


    public DeadTree(int x, int y) {

        this.x =x;
        this.y = y;
        int size = 32;

        loadButtonImageFile();
        loadButtonImages();

        // Updated layout for 4 towers - arranged in a cross pattern
        theButtons.add(mageButton = new TheButton("Mage", x - size + 16, y, size, size, ButtonImages.get(1)));
        theButtons.add(archerButton = new TheButton("Archer", x + 16, y - size, size, size, ButtonImages.get(0)));
        theButtons.add(artilleryButton = new TheButton("Artillery", x + size + 16, y, size, size, ButtonImages.get(2)));
        theButtons.add(poisonButton = new TheButton("Poison", x + 16, y + size, size, size, ButtonImages.get(3))); // Bottom position

        this.tooltip = new CostTooltip();
    }

    public void draw(Graphics g) {
        if (showChoices){
            drawButtonWithHover(g, mageButton, 0);
            drawButtonWithHover(g, archerButton, 1);
            drawButtonWithHover(g, artilleryButton, 2);
            drawButtonWithHover(g, poisonButton, 3); // Draw poison button
            // Update and draw tooltip
            tooltip.update();
            tooltip.draw((Graphics2D) g);
        }
    }

    private void drawButtonWithHover(Graphics g, TheButton button, int index) {
        if (button.isMouseOver()) {
            Graphics2D g2d = (Graphics2D) g;
            long currentTime = System.currentTimeMillis();
            float alpha = (float) (0.5f + 0.5f * Math.sin(currentTime * 0.003));
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2d.setColor(new Color(255, 255, 255, 100));
            g2d.setStroke(new BasicStroke(3f));
            g2d.drawRoundRect(button.getX() - 2, button.getY() - 2, button.getWidth() + 4, button.getHeight() + 4, 8, 8);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }
        button.draw(g);
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
        ButtonImages.add(buttonSheetImg.getSubimage(size * 1, size * 2, size, size));

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

    public TheButton getPoisonButton() {
        return poisonButton;
    }

    public void setPoisonButton(TheButton poisonButton) {
        this.poisonButton = poisonButton;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    /**
     * Handles mouse hover for tooltips - should be called from Playing/Controller
     */
    public void handleMouseHover(int mouseX, int mouseY, models.PlayingModel playing) {
        if (!showChoices) {
            tooltip.hide();
            return;
        }

        if (archerButton.getBounds().contains(mouseX, mouseY)) {
            int cost = playing.getTowerManager().getTowerCostFromOptions(constants.Constants.Towers.ARCHER, playing.getGameOptions());
            boolean canAfford = playing.getPlayerManager().getGold() >= cost;
            tooltip.show("Archer Tower", cost,
                    "Fast firing, long range. Effective against light enemies.",
                    canAfford, mouseX, mouseY);
        } else if (mageButton.getBounds().contains(mouseX, mouseY)) {
            int cost = playing.getTowerManager().getTowerCostFromOptions(constants.Constants.Towers.MAGE, playing.getGameOptions());
            boolean canAfford = playing.getPlayerManager().getGold() >= cost;
            tooltip.show("Mage Tower", cost,
                    "Magical attacks with special effects. Can spawn wizard warriors.",
                    canAfford, mouseX, mouseY);
        } else if (artilleryButton.getBounds().contains(mouseX, mouseY)) {
            int cost = playing.getTowerManager().getTowerCostFromOptions(constants.Constants.Towers.ARTILLERY, playing.getGameOptions());
            boolean canAfford = playing.getPlayerManager().getGold() >= cost;
            tooltip.show("Artillery Tower", cost,
                    "Slow but powerful. Area of effect damage.",
                    canAfford, mouseX, mouseY);
        } else if (poisonButton.getBounds().contains(mouseX, mouseY)) {
            int cost = constants.Constants.Towers.getCost(constants.Constants.Towers.POISON);
            boolean canAfford = playing.getPlayerManager().getGold() >= cost;
            tooltip.show("Poison Tower", cost,
                    "Applies poison damage over time. Special ability affects all enemies.",
                    canAfford, mouseX, mouseY);
        } else {
            tooltip.hide();
        }
    }

    public void mouseMoved(int mouseX, int mouseY) {
        mageButton.setMouseOver(mageButton.getBounds().contains(mouseX, mouseY));
        archerButton.setMouseOver(archerButton.getBounds().contains(mouseX, mouseY));
        artilleryButton.setMouseOver(artilleryButton.getBounds().contains(mouseX, mouseY));
        poisonButton.setMouseOver(poisonButton.getBounds().contains(mouseX, mouseY));
    }
}
