package ui_p;

import java.awt.*;

public class TheButton {
    private String text;
    private int x;
    private int y;
    private int width;
    private int height;
    private Rectangle bounds;
    private boolean mouseOver, mousePressed;

    public TheButton (String text, int x, int y, int width, int height) {
        this.text = text;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.bounds = new Rectangle(x, y, width, height);
    }

    public void draw(Graphics g) {
        if (mouseOver)
            g.setColor(Color.gray);
        else
            g.setColor(Color.WHITE);
        g.fillRect(x, y, width, height);

        g.setColor(Color.black);
        g.drawRect(x, y, width, height);
        if (mousePressed) {
            g.drawRect(x + 1, y + 1, width - 2, height - 2);
            g.drawRect(x + 2, y + 2, width - 4, height - 4);
        }

        int w = g.getFontMetrics().stringWidth(text);
        int h = g.getFontMetrics().getHeight();
        g.drawString(text, x - w / 2 + width / 2, y + h / 2 + height / 2);
    }


    public void setMouseOver(boolean mouseOver) {
        this.mouseOver = mouseOver;
    }

    public Rectangle getBounds(){
        return bounds;
    }

    public void setMousePressed(boolean mousePressed) {
        this.mousePressed = mousePressed;
    }

    public void resetBooleans() {
        mouseOver = false;
        mousePressed = false;
    }


}
