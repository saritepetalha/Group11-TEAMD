package ui_p;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ConfettiAnimation {
    private int x, y;
    private int frameIndex = 0;
    private int tick = 0;
    private final int tickLimit = 2;
    private boolean finished = false;
    public ConfettiAnimation(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void update() {
        if (finished) return;

        tick++;
        if (tick >= tickLimit) {
            tick = 0;
            frameIndex++;

            BufferedImage[] frames = AssetsLoader.getInstance().confettiAnimationFrames;
            if (frames != null && frameIndex >= frames.length) {
                finished = true;
            }
        }
    }

    public void draw(Graphics g) {
        if (finished) return;

        BufferedImage[] frames = AssetsLoader.getInstance().confettiAnimationFrames;
        if (frames == null) {
            System.out.println("⚠️ Confetti frames are null!");
            return;
        }

        if (frameIndex < frames.length) {
            BufferedImage frame = frames[frameIndex];
            if (frame == null) {
                System.out.println("⚠️ Confetti frame " + frameIndex + " is null!");
                return;
            }

            int drawWidth = frame.getWidth();
            int drawHeight = frame.getHeight();

            // Center the confetti animation on the specified position
            int drawX = x - drawWidth / 2;
            int drawY = y - drawHeight / 2;

            g.drawImage(frame, drawX, drawY, drawWidth, drawHeight, null);
        }
    }

    public boolean isFinished() {
        return finished;
    }

    /**
     * Get the total duration of the confetti animation in ticks
     * This helps determine how long to delay the victory screen
     */
    public static int getAnimationDurationTicks() {
        BufferedImage[] frames = AssetsLoader.getInstance().confettiAnimationFrames;
        if (frames != null) {
            return frames.length * 2; // frames.length=64, tickLimit=2, so 64*2 = 128 ticks (~2.1 seconds)
        }
        return 120; // Fallback to 2 seconds if frames not loaded
    }

}