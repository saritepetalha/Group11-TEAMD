package ui_p;

import java.awt.*;
import java.awt.image.BufferedImage;

public class TheButton {
    protected String text;
    protected int x;
    protected int y;
    protected int width;
    protected int height;
    private int id;
    private Rectangle bounds;
    private boolean mouseOver, mousePressed;
    private BufferedImage sprite;

    // constructor for normal buttons
    public TheButton (String text, int x, int y, int width, int height) {
        this.text = text;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.id = -1;
        this.bounds = new Rectangle(x, y, width, height);

    }

    // constructor for tile buttons
    public TheButton (String text, int x, int y, int width, int height, int id) {
        this.text = text;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.id = id;
        this.bounds = new Rectangle(x, y, width, height);
    }

    // constructor for buttons with sprite
    public TheButton(String text, int x, int y, int width, int height, BufferedImage sprite) {
        this.text = text;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.sprite = sprite;
    }

    public String getText() {
        return text;
    }

    public int getId() {
        return id;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void draw(Graphics g) {
        if (sprite != null) {
            g.drawImage(sprite, x, y, width, height, null);
            return;
        }


        g.setColor(Color.BLACK);
        g.drawRect(x, y, width, height);

        if (mousePressed) {
            g.drawRect(x + 1, y + 1, width - 2, height - 2);
            g.drawRect(x + 2, y + 2, width - 4, height - 4);
        }

        int w = g.getFontMetrics().stringWidth(text);
        int h = g.getFontMetrics().getHeight();
        g.drawString(text, x + (width - w) / 2, y + (height + h / 2) / 2);
    }




    public void drawStyled(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        // enable anti-aliasing for smoother text
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        Font font = new Font("MV Boli", Font.PLAIN, 45);  // You can change the font here
        g2d.setFont(font);

        // set color depending on hover state
        if (mouseOver) {
            g2d.setColor(new Color(255, 99, 71)); // tomato red when hovered
        } else {
            g2d.setColor(new Color(255, 255, 255, 230)); // soft white
        }

        // center text inside bounds
        FontMetrics fm = g2d.getFontMetrics();
        int textX = x + (width - fm.stringWidth(text)) / 2;
        int textY = y + ((height - fm.getHeight()) / 2) + fm.getAscent();

        g2d.drawString(text, textX, textY);
    }



    public void setMouseOver(boolean mouseOver) {
        this.mouseOver = mouseOver;
    }

    public boolean isMouseOver() {
        return mouseOver;
    }

    public Rectangle getBounds(){
        return new Rectangle(x, y, width, height);
    }

    public void setMousePressed(boolean mousePressed) {
        this.mousePressed = mousePressed;
    }

    public boolean isMousePressed() {
        return mousePressed;
    }
    public boolean isMousePressed(int mouseX, int mouseY) {
        Rectangle bounds = new Rectangle(x, y, width, height);
        return bounds.contains(mouseX, mouseY);
    }

    public void resetBooleans() {
        mouseOver = false;
        mousePressed = false;
    }

    public void setText(String text) {
        this.text = text;
    }


}
