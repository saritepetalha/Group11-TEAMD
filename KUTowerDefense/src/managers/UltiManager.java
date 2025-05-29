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

    /**
     * Requires: Earthquake ability must be ready to use. Player must have enough gold.
     * Modifies: Player’s gold, enemy health, cooldown state, and active earthquake animation.
     * Effects: Deals area damage to all enemies on screen, deducts gold, and sets earthquake on cooldown.
     */
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

        // Counter effect: 50% chance to destroy level 1 towers
        for (objects.Tower tower : playing.getTowerManager().getTowers()) {
            if (tower.getLevel() == 1 && !tower.isDestroyed()) {
                if (Math.random() < 0.5) {
                    tower.setDestroyed(true);
                    if (tower instanceof objects.MageTower) {
                        tower.setDestroyedSprite(LoadSave.getImageFromPath("/TowerAssets/Tower_spell_destroyed.png"));
                    } else if (tower instanceof objects.ArtilleryTower) {
                        tower.setDestroyedSprite(LoadSave.getImageFromPath("/TowerAssets/Tower_bomb_destroyed.png"));
                    } else if (tower instanceof objects.ArcherTower) {
                        tower.setDestroyedSprite(LoadSave.getImageFromPath("/TowerAssets/Tower_archer_destroyed.png"));
                    }
                    // Spawn debris effect
                    java.util.List<objects.Tower.Debris> debris = new java.util.ArrayList<>();
                    int debrisCount = 12 + (int)(Math.random() * 6);
                    int cx = tower.getX() + 32, cy = tower.getY() + 32;
                    for (int d = 0; d < debrisCount; d++) {
                        double angle = Math.random() * 2 * Math.PI;
                        float speed = 2f + (float)Math.random() * 2f;
                        float vx = (float)Math.cos(angle) * speed;
                        float vy = (float)Math.sin(angle) * speed;
                        int color = 0xFF7C5C2E; // brown debris
                        int size = 3 + (int)(Math.random() * 4);
                        int lifetime = 20 + (int)(Math.random() * 10);
                        debris.add(new objects.Tower.Debris(cx, cy, vx, vy, color, size, lifetime));
                    }
                    tower.debrisList = debris;
                    tower.debrisStartTime = System.currentTimeMillis();
                }
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

    /**
     * Requires: x and y must be within game world bounds. Player must have enough gold.
     * Modifies: player’s gold, list of enemies (by applying damage), ulti cooldown state.
     * Effects: Deals 80 damage to each enemy within 100 unit radius of (x, y), spawns lightning animation,
     *          reduces player’s gold by cost, and puts Lightning ultimate on cooldown.
     */
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
                if(playing.getWeatherManager().isRaining()) {
                    enemy.hurt((int)(lightningDamage * 1.25f), true);
                }
                else{
                    enemy.hurt(lightningDamage, true);
                }
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

