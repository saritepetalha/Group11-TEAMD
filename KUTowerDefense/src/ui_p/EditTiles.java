package ui_p;

import java.awt.*;

public class EditTiles {

    private int x,y, width, height; // starting position x,y, and width and height of the edit tiles bar

    public EditTiles(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void draw(Graphics g){
        g.setColor(new Color(157,209,153,255));
        g.fillRect(x,y,width,height);
    }

}
