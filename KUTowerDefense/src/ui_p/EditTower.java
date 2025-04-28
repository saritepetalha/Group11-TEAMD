package ui_p;

import main.Game;
import managers.TowerManager;
import scenes.SceneMethods;

import java.awt.*;

public class DeadTree {

    int x,y, mouseX, mouseY;
    boolean showChoices = false;
    TheButton mageButton, archerButton, artilleryButton;


    public DeadTree(int x, int y) {

        this.x =x;
        this.y = y;
        int size = 32;
        mageButton = new TheButton("Mage", x - size, y - size, size, size);
        archerButton = new TheButton("Archer", x + size, y + size, size, size);
        artilleryButton = new TheButton("Artillery", x , y + size, size, size);
    }


    public boolean isClicked(int x, int y) {
        Rectangle bounds = new Rectangle(x, y, 64, 64);
        return bounds.contains(mouseX, mouseY);
    }

    public void draw(Graphics g) {
        if (showChoices){
            mageButton.draw(g);
            archerButton.draw(g);
            artilleryButton.draw(g);
        }
    }


}
