package ui_p;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ModeButton extends TheButton {

    public ModeButton(String text, int x, int y, int width, int height, BufferedImage sprite) {
        super(text, x, y, width, height, sprite);
    }

    @Override
    public void draw(Graphics g) {
        super.draw(g);

        if (text != null && !text.isEmpty()) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 16));
            int w = g.getFontMetrics().stringWidth(text);
            int h = g.getFontMetrics().getHeight();
            g.drawString(text, x + (width - w) / 2, y + (height + h / 5) / 2);
        }
    }
}
