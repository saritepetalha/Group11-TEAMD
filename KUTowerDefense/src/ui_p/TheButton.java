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

    public void setX(int x) {
        this.x = x;
        this.bounds = new Rectangle(x, y, width, height);
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
        this.bounds = new Rectangle(x, y, width, height);
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
        this.bounds = new Rectangle(x, y, width, height);
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
        this.bounds = new Rectangle(x, y, width, height);
    }

    public void draw(Graphics g) {
        if (sprite != null) {
            g.drawImage(sprite, x, y, width, height, null);

            // add hover and press visual effects
            if (mousePressed) {
                // create a darker overlay when pressed
                Graphics2D g2d = (Graphics2D) g;
                g2d.setColor(new Color(0, 0, 0, 80)); // Slight darkening
                g2d.fillRect(x + 2, y + 2, width - 4, height - 4);
            } else if (mouseOver) {
                // create a shining hover effect animation
                Graphics2D g2d = (Graphics2D) g;

                // create a shining animation effect
                long currentTime = System.currentTimeMillis();
                float alpha = (float) (0.4f + 0.3f * Math.sin(currentTime * 0.003)); // Oscillate between 0.4 and 0.7

                // draw a semi-transparent overlay
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                g2d.setColor(new Color(255, 255, 200)); // Yellowish glow
                g2d.fillRect(x, y, width, height);

                // reset the composite
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            }
        }
        /*
        // For buttons without a sprite
        g.setColor(Color.BLACK);
        g.drawRect(x, y, width, height);

        if (mouseOver) {
            // Create a hover effect with animation
            Graphics2D g2d = (Graphics2D) g;
            long currentTime = System.currentTimeMillis();
            float alpha = (float) (0.3f + 0.2f * Math.sin(currentTime * 0.003)); // Oscillate alpha

            // Draw a subtle glow effect
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2d.setColor(new Color(255, 255, 200)); // Yellowish glow
            g2d.fillRect(x, y, width, height);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }

        if (mousePressed) {
            g.drawRect(x + 1, y + 1, width - 2, height - 2);
            g.drawRect(x + 2, y + 2, width - 4, height - 4);
        }

        int w = g.getFontMetrics().stringWidth(text);
        int h = g.getFontMetrics().getHeight();
        g.drawString(text, x + (width - w) / 2, y + (height + h / 2) / 2);*/
    }


    public void drawStyled(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        // enable anti-aliasing for smoother text
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        Font font = new Font("MV Boli", Font.PLAIN, 45);  // You can change the font here
        g2d.setFont(font);

        /* set color depending on hover state
        if (mouseOver) {
            g2d.setColor(new Color(255, 99, 71)); // tomato red when hovered
        } else {
            g2d.setColor(new Color(255, 255, 255, 230)); // soft white
        }*/

        // Draw background with hover effects
        if (mouseOver) {
            // Create animated hover effect
            long currentTime = System.currentTimeMillis();
            float alpha = (float) (0.6f + 0.4f * Math.sin(currentTime * 0.003)); // Oscillate between 0.6 and 1.0
            float hueShift = (float) (0.1f * Math.sin(currentTime * 0.002)); // Subtle hue shift for a "shining" effect

            // Create a gradient background with animated color
            Color startColor = new Color(255, 100 + (int)(50 * hueShift), 71, (int)(alpha * 255)); // animated tomato red
            Color endColor = new Color(255, 120 + (int)(50 * hueShift), 90, (int)(alpha * 255)); // lighter shade

            // Create gradient for a more luxurious look
            GradientPaint gradient = new GradientPaint(
                    x, y, startColor,
                    x + width, y + height, endColor
            );

            g2d.setPaint(gradient);
            g2d.fillRoundRect(x - 5, y - 2, width + 10, height + 4, 15, 15); // slightly larger area with rounded corners

            // Add a subtle glow effect
            g2d.setColor(new Color(255, 220, 150, 60));
            int glowSize = 3 + (int)(3 * Math.sin(currentTime * 0.005)); // Animated glow size
            g2d.setStroke(new BasicStroke(glowSize));
            g2d.drawRoundRect(x - 8, y - 5, width + 16, height + 10, 18, 18);

            // Set text color to white for better contrast
            g2d.setColor(Color.WHITE);
        } else {
            // Normal state - soft white with subtle gradient
            g2d.setColor(new Color(255, 255, 255, 230)); // soft white
        }

        // center text inside bounds
        FontMetrics fm = g2d.getFontMetrics();
        int textX = x + (width - fm.stringWidth(text)) / 2;
        int textY = y + ((height - fm.getHeight()) / 2) + fm.getAscent();

        // For non-hovered buttons, draw text directly
        if (!mouseOver) {
            g2d.drawString(text, textX, textY);
        } else {
            // For hovered buttons, add text shadow for depth
            g2d.setColor(new Color(100, 50, 30, 120)); // Shadow color
            g2d.drawString(text, textX + 2, textY + 2); // Shadow offset

            // Draw the main text in white
            g2d.setColor(Color.WHITE);
            g2d.drawString(text, textX, textY);
        }
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

    public void resetButtonState() {
        mouseOver = false;
        mousePressed = false;
    }

    public void setText(String text) {
        this.text = text;
    }
}
