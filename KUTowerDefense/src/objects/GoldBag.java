package objects;

import helpMethods.LoadSave;
import java.awt.*;
import java.awt.image.BufferedImage;

public class GoldBag {
    private static final BufferedImage[] frames = LoadSave.getGoldBagAnimation();
    private static final int FRAME_COUNT = 8;
    private static final int FRAME_WIDTH = 128;
    private static final int FRAME_HEIGHT = 128;
    private static final long LIFETIME_NANOS = 10_000_000_000L; // 10 seconds
    private static final int DRAW_SIZE = 64; // display size in game
    private static final int COLLECTION_BOX_SIZE = 96; // bigger bounding box for easier collection

    private float x, y;
    private int goldAmount;
    private int frameIndex = 0;
    private int tick = 0;
    private final int tickLimit = 7; // controls animation speed
    private boolean collected = false;
    private long spawnTime;

    // Collection animation properties
    private boolean showCollectionEffect = false;
    private long collectionEffectStartTime = 0;
    private static final long COLLECTION_EFFECT_DURATION = 500_000_000L; // 0.5 seconds

    public GoldBag(float x, float y, int goldAmount) {
        this.x = x;
        this.y = y;
        this.goldAmount = goldAmount;
        this.spawnTime = System.nanoTime();
    }

    public void update() {
        tick++;
        if (tick >= tickLimit) {
            tick = 0;
            frameIndex = (frameIndex + 1) % FRAME_COUNT;
        }

        // Update collection effect timer
        if (showCollectionEffect) {
            long currentTime = System.nanoTime();
            if (currentTime - collectionEffectStartTime > COLLECTION_EFFECT_DURATION) {
                showCollectionEffect = false;
            }
        }
    }

    public void draw(Graphics g) {
        if (!collected && frameIndex < frames.length) {
            g.drawImage(frames[frameIndex], (int)x - DRAW_SIZE/2, (int)y - DRAW_SIZE/2, DRAW_SIZE, DRAW_SIZE, null);
        }

        // Draw collection effect
        if (showCollectionEffect) {
            drawCollectionEffect(g);
        }
    }

    public boolean isExpired() {
        return !collected && (System.nanoTime() - spawnTime > LIFETIME_NANOS);
    }

    public boolean isCollected() {
        return collected;
    }

    public boolean isShowingCollectionEffect() {
        return showCollectionEffect;
    }

    public int getGoldAmount() {
        return goldAmount;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public boolean contains(int mouseX, int mouseY) {
        int left = (int)x - COLLECTION_BOX_SIZE/2;
        int top = (int)y - COLLECTION_BOX_SIZE/2;
        return mouseX >= left && mouseX <= left + COLLECTION_BOX_SIZE && mouseY >= top && mouseY <= top + COLLECTION_BOX_SIZE;
    }

    public void collect() {
        collected = true;
        showCollectionEffect = true;
        collectionEffectStartTime = System.nanoTime();
    }

    /**
     * Draws a sparkly collection effect when the gold bag is collected
     */
    private void drawCollectionEffect(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        long currentTime = System.nanoTime();
        long elapsed = currentTime - collectionEffectStartTime;
        float progress = Math.min(1.0f, (float) elapsed / COLLECTION_EFFECT_DURATION);

        // Create a sparkly explosion effect
        int centerX = (int) x;
        int centerY = (int) y;

        // Draw multiple expanding circles with decreasing opacity
        for (int i = 0; i < 3; i++) {
            float circleProgress = Math.max(0, progress - i * 0.1f);
            if (circleProgress <= 0) continue;

            int radius = (int) (circleProgress * 40 + i * 10);
            float alpha = (1.0f - circleProgress) * 0.8f;

            // Golden sparkle color
            Color sparkleColor = new Color(255, 215, 0, (int) (alpha * 255));
            g2d.setColor(sparkleColor);
            g2d.setStroke(new BasicStroke(3.0f));
            g2d.drawOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
        }

        // Draw radiating lines for extra sparkle effect
        if (progress < 0.7f) {
            float lineAlpha = (0.7f - progress) / 0.7f;
            Color lineColor = new Color(255, 255, 255, (int) (lineAlpha * 200));
            g2d.setColor(lineColor);
            g2d.setStroke(new BasicStroke(2.0f));

            for (int angle = 0; angle < 360; angle += 45) {
                double radians = Math.toRadians(angle);
                int lineLength = (int) (progress * 25);
                int x1 = centerX + (int) (Math.cos(radians) * 5);
                int y1 = centerY + (int) (Math.sin(radians) * 5);
                int x2 = centerX + (int) (Math.cos(radians) * lineLength);
                int y2 = centerY + (int) (Math.sin(radians) * lineLength);
                g2d.drawLine(x1, y1, x2, y2);
            }
        }

        // Draw gold amount text that floats upward
        String goldText = "+" + goldAmount;
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(goldText);

        int textX = centerX - textWidth / 2;
        int textY = centerY - (int) (progress * 30); // Float upward

        // Text with outline for better visibility
        g2d.setColor(Color.BLACK);
        g2d.drawString(goldText, textX + 1, textY + 1);
        g2d.setColor(new Color(255, 215, 0, (int) ((1.0f - progress) * 255)));
        g2d.drawString(goldText, textX, textY);
    }
} 