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

    private float x, y;
    private int goldAmount;
    private int frameIndex = 0;
    private int tick = 0;
    private final int tickLimit = 7; // controls animation speed
    private boolean collected = false;
    private long spawnTime;

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
    }

    public void draw(Graphics g) {
        if (!collected && frameIndex < frames.length) {
            g.drawImage(frames[frameIndex], (int)x - DRAW_SIZE/2, (int)y - DRAW_SIZE/2, DRAW_SIZE, DRAW_SIZE, null);
        }
    }

    public boolean isExpired() {
        return !collected && (System.nanoTime() - spawnTime > LIFETIME_NANOS);
    }

    public boolean isCollected() {
        return collected;
    }

    public int getGoldAmount() {
        return goldAmount;
    }

    public boolean contains(int mouseX, int mouseY) {
        int left = (int)x - DRAW_SIZE/2;
        int top = (int)y - DRAW_SIZE/2;
        return mouseX >= left && mouseX <= left + DRAW_SIZE && mouseY >= top && mouseY <= top + DRAW_SIZE;
    }

    public void collect() {
        collected = true;
    }
} 