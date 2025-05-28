package managers;

import enemies.Enemy;
import helpMethods.LoadSave;
import main.Game;
import scenes.Playing;
import ui_p.AssetsLoader;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class UltiManager {

    private Playing playing;
    private boolean earthquakeActive = false;
    private long shakeStartTime;
    private int shakeDuration = 1000;
    private int shakeOffsetX = 0;
    private int shakeOffsetY = 0;
    private Random rand = new Random();

    private long lastEarthquakeUsedGameTime = -999999;
    private final long earthquakeCooldownMillis = 15000;
    private final int earthquakeCost = 50;
    private final int earthquakeDamage = 20;

    private long lastLightningUsedGameTime = -999999;
    private final long lightningCooldownMillis = 20000;
    private final int lightningCost = 75;
    private final int lightningDamage = 80;
    private final int lightningRadius = 100;

    private boolean waitingForLightningTarget = false;
    private final List<LightningStrike> activeStrikes = new ArrayList<>();


    public UltiManager(Playing playing) {
        this.playing = playing;
    }

    public void triggerEarthquake() {
        if (!canUseEarthquake())
            return;

        if (playing.getPlayerManager().getGold() < earthquakeCost) {
            System.out.println("Not enough gold!");
            return;
        }

        playing.getPlayerManager().spendGold(earthquakeCost);
        playing.updateUIResources();

        lastEarthquakeUsedGameTime = playing.getGameTime();

        for (Enemy enemy : playing.getEnemyManager().getEnemies()) {
            if (enemy.isAlive()) {
                enemy.hurt(earthquakeDamage, true);
            }
        }

        startScreenShake();
        AudioManager.getInstance().playSound("earthquake");
    }



    private void startScreenShake() {
        earthquakeActive = true;
        shakeStartTime = System.currentTimeMillis();
    }

    public void applyShakeIfNeeded(Graphics g) {
        if (earthquakeActive) {
            long elapsed = System.currentTimeMillis() - shakeStartTime;
            if (elapsed > shakeDuration) {
                earthquakeActive = false;
                shakeOffsetX = 0;
                shakeOffsetY = 0;
            } else {
                shakeOffsetX = rand.nextInt(9) - 4;
                shakeOffsetY = rand.nextInt(9) - 4;
                g.translate(shakeOffsetX, shakeOffsetY);
            }
        }
    }

    public void reverseShake(Graphics g) {
        if (earthquakeActive) {
            g.translate(-shakeOffsetX, -shakeOffsetY);
        }
    }

    public boolean canUseEarthquake() {
        long currentGameTime = playing.getGameTime();
        return currentGameTime - lastEarthquakeUsedGameTime >= earthquakeCooldownMillis;
    }

    public void toggleLightningTargeting() {
        waitingForLightningTarget = !waitingForLightningTarget;
    }

    public boolean canUseLightning() {
        long currentGameTime = playing.getGameTime();
        return currentGameTime - lastLightningUsedGameTime >= lightningCooldownMillis;
    }

    public boolean isWaitingForLightningTarget() {
        return waitingForLightningTarget;
    }

    public void setWaitingForLightningTarget(boolean waiting) {
        this.waitingForLightningTarget = waiting;
    }

    public int getLightningRadius() {
        return lightningRadius;
    }

    public void triggerLightningAt(int x, int y) {
        if (!canUseLightning()) return;
        if (playing.getPlayerManager().getGold() < lightningCost) return;

        playing.getPlayerManager().spendGold(lightningCost);
        playing.updateUIResources();
        lastLightningUsedGameTime = playing.getGameTime();
        waitingForLightningTarget = false;

        for (Enemy enemy : playing.getEnemyManager().getEnemies()) {
            if (!enemy.isAlive()) continue;
            float dx = enemy.getX() - x;
            float dy = enemy.getY() - y;
            if (Math.sqrt(dx * dx + dy * dy) <= lightningRadius) {
                enemy.hurt(lightningDamage, true);
            }
        }

        activeStrikes.add(new LightningStrike(x, y));
        AudioManager.getInstance().playSound("lightning");
    }

    public void update(long gameTime) {
        activeStrikes.removeIf(LightningStrike::isFinished);
        for (LightningStrike strike : activeStrikes) {
            strike.update(gameTime);
        }
    }

    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        for (LightningStrike strike : activeStrikes) {
            strike.draw(g2d);
        }
    }

    public boolean isLightningPlaying() {
        return !activeStrikes.isEmpty();
    }



    private class LightningStrike {
        int x, y;
        int currentFrame = 0;
        long startTime;
        final int frameDurationMillis = 60;
        final int totalFrames = AssetsLoader.getInstance().lightningFrames.length;

        LightningStrike(int x, int y) {
            this.x = x + 32;
            this.y = y + 64;
            this.startTime = playing.getGameTime();
        }

        boolean isFinished() {
            long elapsed = playing.getGameTime() - startTime;
            return (elapsed / frameDurationMillis) >= totalFrames;
        }

        void update(long gameTime) {
            long elapsed = gameTime - startTime;
            currentFrame = (int) (elapsed / frameDurationMillis);
        }

        void draw(Graphics2D g2d) {
            if (!isFinished()) {
                BufferedImage frame = AssetsLoader.getInstance().lightningFrames[currentFrame];
                int frameHeight = frame.getHeight();
                int topY = y - frameHeight;
                int drawStartY = Math.max(0, topY);
                int pixelsToDraw = y - drawStartY;

                if (pixelsToDraw <= 0 || pixelsToDraw > frameHeight) return;

                BufferedImage visiblePart = frame.getSubimage(0, frameHeight - pixelsToDraw, frame.getWidth(), pixelsToDraw);
                g2d.drawImage(visiblePart, x - frame.getWidth() / 2, drawStartY, null);
            }
        }


    }
}

