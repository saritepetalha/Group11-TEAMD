package ui_p;

import helpMethods.LoadSave;

import java.awt.*;
import java.awt.image.BufferedImage;

public class FireAnimation {
    private static final BufferedImage[] frames;

    static {
        BufferedImage fireSheet = LoadSave.getImageFromPath("/Effects/fire_animation.png");
        frames = new BufferedImage[6];
        for (int i = 0; i < 6; i++) {
            frames[i] = fireSheet.getSubimage(i * 399, 0, 399, 392);
        }
    }

    public static BufferedImage[] getFrames() {
        return frames;
    }

    private int x, y;
    private int frameIndex = 0;
    private int tick = 0;
    private final int tickLimit = 10;
    private boolean finished = false;

    public FireAnimation(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void update() {
        tick++;
        if (tick >= tickLimit) {
            tick = 0;
            frameIndex++;
            if (frameIndex >= frames.length) {
                finished = true;
            }
        }
    }

    public void draw(Graphics g) {
        if (!finished && frameIndex < frames.length) {
            g.drawImage(frames[frameIndex], x, y, 64, 64, null);
        }
    }

    public boolean isFinished() {
        return finished;
    }
}