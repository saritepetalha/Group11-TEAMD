package managers;

import enemies.Enemy;
import helpMethods.LoadSave;
import objects.GoldFactory;
import scenes.Playing;
import ui_p.AssetsLoader;
import skills.SkillTree;
import skills.SkillType;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
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

    private long lastLightningUsedGameTime = -999999;
    private final long lightningCooldownMillis = 20000;
    private final int lightningCost = 75;
    private final int lightningDamage = 80;
    private final int lightningRadius = 100;

    private long lastGoldFactoryUsedGameTime = -999999;
    private final long goldFactoryCooldownMillis = 30000; // 30 seconds
    private final int goldFactoryCost = 100;

    private boolean waitingForLightningTarget = false;
    private boolean goldFactorySelected = false;
    private long goldFactorySelectedTime = 0; // Time when factory was selected
    private final long placementDelayMillis = 300; // 300ms delay before placement is allowed
    private final List<LightningStrike> activeStrikes = new ArrayList<>();
    private final List<GoldFactory> goldFactories = new ArrayList<>();

    private long lastFreezeUsedGameTime = -999999;
    private final long freezeCooldownMillis = 20000; // 20 seconds cooldown
    private final int freezeCost = 60; // Cost in gold
    private static final int freezeDuration = 300; // 5 seconds at 60 UPS

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

        int finalEarthquakeDamage = earthquakeCost;
        if (SkillTree.getInstance().isSkillSelected(SkillType.SHATTERING_FORCE)) {
            finalEarthquakeDamage = Math.round(earthquakeCost * 1.25f);
            System.out.println("[SHATTERING_FORCE] Earthquake deals bonus damage: " + earthquakeCost + " -> " + finalEarthquakeDamage);
        }

        for (Enemy enemy : playing.getEnemyManager().getEnemies()) {
            if (enemy.isAlive()) {
                enemy.hurt(finalEarthquakeDamage, true);
                // Track death location for confetti if enemy died from earthquake
                if (!enemy.isAlive() && playing.getController() != null && playing.getController().getModel() != null) {
                    playing.getController().getModel().enemyDiedAt((int)enemy.getX(), (int)enemy.getY());
                }
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
        shakeDuration = 1000; // Reset to normal earthquake duration
        AudioManager.getInstance().playSound("earthquake");
    }

    private void startScreenShake() {
        earthquakeActive = true;
        shakeStartTime = System.currentTimeMillis();
    }

    public void triggerTNTShake() {
        // Shorter, less intense shake for TNT explosions
        earthquakeActive = true;
        shakeStartTime = System.currentTimeMillis();
        shakeDuration = 300; // Shorter duration (300ms vs 1000ms)
        System.out.println("TNT shake activated!");
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

    private long getEffectiveEarthquakeCooldown() {
        long cooldown = earthquakeCooldownMillis;
        if (SkillTree.getInstance().isSkillSelected(SkillType.BATTLE_READINESS)) {
            cooldown = Math.round(cooldown * 0.9f);
        }
        return cooldown;
    }

    public boolean canUseEarthquake() {
        long currentGameTime = playing.getGameTime();
        return currentGameTime - lastEarthquakeUsedGameTime >= getEffectiveEarthquakeCooldown();
    }

    public void toggleLightningTargeting() {
        waitingForLightningTarget = !waitingForLightningTarget;
    }

    private long getEffectiveLightningCooldown() {
        long cooldown = lightningCooldownMillis;
        if (SkillTree.getInstance().isSkillSelected(SkillType.DIVINE_WRATH)) {
            cooldown = Math.round(cooldown * 0.8f);
        }
        if (SkillTree.getInstance().isSkillSelected(SkillType.BATTLE_READINESS)) {
            cooldown = Math.round(cooldown * 0.9f);
        }
        return cooldown;
    }

    public boolean canUseLightning() {
        long currentGameTime = playing.getGameTime();
        return currentGameTime - lastLightningUsedGameTime >= getEffectiveLightningCooldown();
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
        if (!canUseLightning()) {
            System.out.println("Lightning Strike is on cooldown!");
            waitingForLightningTarget = false;
            return;
        }
        if (SkillTree.getInstance().isSkillSelected(SkillType.DIVINE_WRATH)) {
            long cooldown = Math.round(lightningCooldownMillis * 0.8f);
            System.out.println("[DIVINE_WRATH] Lightning cooldown reduced: " + lightningCooldownMillis + " -> " + cooldown);
        }
        if (SkillTree.getInstance().isSkillSelected(SkillType.BATTLE_READINESS)) {
            long cooldown = getEffectiveLightningCooldown();
            System.out.println("[BATTLE_READINESS] Lightning cooldown reduced: " + lightningCooldownMillis + " -> " + cooldown);
        }

        if (playing.getPlayerManager().getGold() < lightningCost) {
            System.out.println("Not enough gold for Lightning Strike!");
            waitingForLightningTarget = false; // Exit targeting mode
            return;
        }

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
                // Track death location for confetti if enemy died from lightning
                if (!enemy.isAlive() && playing.getController() != null && playing.getController().getModel() != null) {
                    playing.getController().getModel().enemyDiedAt((int)enemy.getX(), (int)enemy.getY());
                }
            }
        }

        activeStrikes.add(new LightningStrike(x, y));
        AudioManager.getInstance().playSound("lightning");
    }

    public void update(long gameTimeMillis, float gameSpeedMultiplier) {
        activeStrikes.removeIf(LightningStrike::isFinished);
        for (LightningStrike strike : activeStrikes) {
            strike.update(gameTimeMillis);
        }

        // Update gold factories with speed multiplier
        for (GoldFactory factory : goldFactories) {
            factory.update(gameSpeedMultiplier);
        }
    }

    public void draw(Graphics g, float gameSpeedMultiplier) {
        Graphics2D g2d = (Graphics2D) g;
        for (LightningStrike strike : activeStrikes) {
            strike.draw(g2d);
        }

        // Draw gold factories - create defensive copy to avoid ConcurrentModificationException
        List<GoldFactory> factoriesCopy = new ArrayList<>(goldFactories);
        for (GoldFactory factory : factoriesCopy) {
            factory.draw(g, gameSpeedMultiplier);
        }
    }

    public boolean isLightningPlaying() {
        return !activeStrikes.isEmpty();
    }

    // Getter methods for cooldown calculations
    public long getLastEarthquakeTime() {
        return lastEarthquakeUsedGameTime;
    }

    public long getLastLightningTime() {
        return lastLightningUsedGameTime;
    }

    public long getLastGoldFactoryTime() {
        return lastGoldFactoryUsedGameTime;
    }

    public boolean hasActiveGoldFactory() {
        return !goldFactories.isEmpty();
    }

    private long getEffectiveGoldFactoryCooldown() {
        long cooldown = goldFactoryCooldownMillis;
        if (SkillTree.getInstance().isSkillSelected(SkillType.BATTLE_READINESS)) {
            cooldown = Math.round(cooldown * 0.9f);
        }
        return cooldown;
    }

    public boolean canUseGoldFactory() {
        long currentGameTime = playing.getGameTime();
        boolean cooldownReady = currentGameTime - lastGoldFactoryUsedGameTime >= getEffectiveGoldFactoryCooldown();
        boolean noActiveFactory = goldFactories.isEmpty();
        return cooldownReady && noActiveFactory;
    }

    public void selectGoldFactory() {
        if (canUseGoldFactory() && playing.getPlayerManager().getGold() >= goldFactoryCost) {
            if (SkillTree.getInstance().isSkillSelected(SkillType.BATTLE_READINESS)) {
                long cooldown = getEffectiveGoldFactoryCooldown();
                System.out.println("[BATTLE_READINESS] Gold Factory cooldown reduced: " + goldFactoryCooldownMillis + " -> " + cooldown);
            }
            goldFactorySelected = true;
            goldFactorySelectedTime = System.currentTimeMillis();
        }
    }

    public void deselectGoldFactory() {
        goldFactorySelected = false;
    }

    public boolean isGoldFactorySelected() {
        return goldFactorySelected;
    }

    public boolean tryPlaceGoldFactory(int mouseX, int mouseY) {
        if (!goldFactorySelected) return false;
        if (!canUseGoldFactory()) return false;
        if (playing.getPlayerManager().getGold() < goldFactoryCost) return false;

        long currentTime = System.currentTimeMillis();
        if (currentTime - goldFactorySelectedTime < placementDelayMillis) {
            System.out.println("Gold Factory placement delay not met!");
            return false;
        }

        // Convert mouse coordinates to tile coordinates
        int tileX = (mouseX / 64) * 64;
        int tileY = (mouseY / 64) * 64;

        // Check if the tile is valid for placement (grass tile)
        int[][] level = playing.getLevel();
        int levelTileX = mouseX / 64;
        int levelTileY = mouseY / 64;

        if (levelTileY >= 0 && levelTileY < level.length &&
                levelTileX >= 0 && levelTileX < level[0].length) {

            // Check if it's a grass tile (ID 5)
            if (level[levelTileY][levelTileX] != 5) {
                System.out.println("Gold Factory can only be placed on grass tiles!");
                return false;
            }

            // Check if there's already a factory at this location
            for (GoldFactory factory : goldFactories) {
                if (factory.getTileX() == tileX && factory.getTileY() == tileY) {
                    System.out.println("There's already a Gold Factory at this location!");
                    return false;
                }
            }

            // Place the factory
            playing.getPlayerManager().spendGold(goldFactoryCost);
            playing.updateUIResources();
            lastGoldFactoryUsedGameTime = playing.getGameTime();
            goldFactorySelected = false; // Deselect after placing

            GoldFactory factory = new GoldFactory(tileX, tileY, playing.getGoldBagManager());
            goldFactories.add(factory);
            AudioManager.getInstance().playSound("coin_drop");
            return true;
        }

        return false;
    }

    public void triggerFreeze() {
        long currentGameTime = playing.getGameTime();
        if (currentGameTime - lastFreezeUsedGameTime < getEffectiveFreezeCooldown()) {
            System.out.println("Freeze is on cooldown!");
            return;
        }
        if (SkillTree.getInstance().isSkillSelected(SkillType.BATTLE_READINESS)) {
            long cooldown = getEffectiveFreezeCooldown();
            System.out.println("[BATTLE_READINESS] Freeze cooldown reduced: " + freezeCooldownMillis + " -> " + cooldown);
        }
        if (playing.getPlayerManager().getGold() < freezeCost) {
            System.out.println("Not enough gold for Freeze!");
            return;
        }
        System.out.println("Freeze triggered successfully with duration: " + freezeDuration + " ticks");
        lastFreezeUsedGameTime = currentGameTime;
        playing.getPlayerManager().spendGold(freezeCost);
        playing.updateUIResources();

        for (Enemy enemy : playing.getEnemyManager().getEnemies()) {
            if (enemy.isAlive()) {
                enemy.freeze(freezeDuration);
            }
        }
    }

    private long getEffectiveFreezeCooldown() {
        long cooldown = freezeCooldownMillis;
        if (SkillTree.getInstance().isSkillSelected(SkillType.BATTLE_READINESS)) {
            cooldown = Math.round(cooldown * 0.9f);
        }
        return cooldown;
    }

    public boolean canUseFreeze() {
        long currentGameTime = playing.getGameTime();
        return currentGameTime - lastFreezeUsedGameTime >= getEffectiveFreezeCooldown();
    }

    public long getLastFreezeTime() {
        return lastFreezeUsedGameTime;
    }

    public long getLightningCooldownMillis() {
        long cooldown = lightningCooldownMillis;
        if (SkillTree.getInstance().isSkillSelected(SkillType.DIVINE_WRATH)) {
            cooldown = Math.round(lightningCooldownMillis * 0.8f);
        }
        return cooldown;
    }

    /**
     * Reset all UltiManager state for game restart
     */
    public void reset() {
        // Reset cooldown times to very old values (so all abilities are ready)
        lastEarthquakeUsedGameTime = -999999L;
        lastLightningUsedGameTime = -999999L;
        lastGoldFactoryUsedGameTime = -999999L;
        lastFreezeUsedGameTime = -999999L;

        goldFactories.clear();
        activeStrikes.clear();

        waitingForLightningTarget = false;
        goldFactorySelected = false;

        earthquakeActive = false;
        shakeStartTime = 0;
        shakeOffsetX = 0;
        shakeOffsetY = 0;
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

