package managers;

import constants.GameDimensions;
import enemies.Enemy;
import helpMethods.LoadSave;
import objects.ArcherTower;
import objects.ArtilleryTower;
import objects.MageTower;
import objects.Tower;
import objects.TowerDecorator;
import strategies.TargetingStrategy;
import scenes.Playing;
import ui_p.DeadTree;
import helpMethods.Utils;
import ui_p.LiveTree;
import objects.Warrior;
import objects.WizardWarrior;
import objects.ArcherWarrior;
import objects.LightDecorator;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TowerManager {
    private Playing playing;
    private BufferedImage[] towerImages;
    private BufferedImage[] nightTowerImages; // [bomb, mage, archer] for night
    private BufferedImage[] nightUpTowerImages; // [bomb, mage, archer] for night upgraded
    private ArrayList<Tower> towers = new ArrayList<>();
    private List<TowerUpgradeEffect> upgradeEffects = new ArrayList<>();
    private List<Warrior> warriors = new ArrayList<>();

    public TowerManager(Playing playing) {
        this.playing = playing;
        loadTowerImages();
    }

    private void loadTowerImages() {
        BufferedImage tilesetImage = LoadSave.getSpriteAtlas();
        towerImages = new BufferedImage[3];
        // Load from correct positions in tileset
        towerImages[0] = tilesetImage.getSubimage(2 * 64, 6 * 64, 64, 64); // Archer Tower (row 6, col 2)
        towerImages[1] = tilesetImage.getSubimage(0 * 64, 5 * 64, 64, 64); // Artillery Tower (row 5, col 0)
        towerImages[2] = tilesetImage.getSubimage(1 * 64, 5 * 64, 64, 64); // Mage Tower (row 5, col 1)

        // Load night mode sprites
        nightTowerImages = new BufferedImage[3];
        nightUpTowerImages = new BufferedImage[3];
        BufferedImage nightImg = LoadSave.getImageFromPath("/TowerAssets/towerNight.png");
        BufferedImage nightUpImg = LoadSave.getImageFromPath("/TowerAssets/towerUpNight.png");
        if (nightImg != null) {
            for (int i = 0; i < 3; i++) {
                nightTowerImages[i] = nightImg.getSubimage(i * 384, 0, 384, 384);
            }
        }
        if (nightUpImg != null) {
            for (int i = 0; i < 3; i++) {
                nightUpTowerImages[i] = nightUpImg.getSubimage(i * 384, 0, 384, 384);
            }
        }
    }

    public void update() {
        attackEnemyIfInRange();
    }

    public void update(float speedMultiplier) {
        for (Tower tower : towers) {
            tower.update(speedMultiplier);
            if (!tower.isDestroyed()) {
                attackEnemyIfInRange(tower);
            }
        }
        updateWarriors(speedMultiplier); // Ensure warriors are updated
    }

    private void attackEnemyIfInRange() {
        attackEnemyIfInRange(1.0f);
    }

    private void attackEnemyIfInRange(float speedMultiplier) {
        for (Tower tower : towers) {
            tower.update(speedMultiplier);
            if (!tower.isDestroyed()) {
                attackEnemyIfInRange(tower);
            }
        }
    }

    private void attackEnemyIfInRange(Tower tower) {
        if (tower.isDestroyed()) return;
        if (!tower.isCooldownOver()) {
            return; // Tower is on cooldown
        }

        // Collect all enemies in range
        List<Enemy> enemiesInRange = new ArrayList<>();
        for (Enemy enemy : playing.getEnemyManager().getEnemies()) {
            if (enemy.isAlive() && isEnemyInRange(tower, enemy)) {
                enemiesInRange.add(enemy);
            }
        }

        // Use the tower's targeting strategy to select the best target
        if (!enemiesInRange.isEmpty()) {
            TargetingStrategy strategy = tower.getTargetingStrategy();
            Enemy target = strategy.selectTarget(enemiesInRange, tower);

            if (target != null) {
                playing.shootEnemy(tower, target);
                tower.resetCooldown();

                // Windy weather effect: Archer towers have 30% chance to miss
                if (playing.getWeatherManager().isWindy() && tower.getType() == constants.Constants.Towers.ARCHER) {
                    // 30% chance to miss in windy weather
                    if (Math.random() < 0.3) {
                        // Miss the shot - don't actually shoot
                        System.out.println("Archer tower missed due to windy weather!");
                        return; // Exit without shooting
                    }
                }
            }
        }
    }

    private boolean isEnemyInRange(Tower tower, Enemy enemy) {
        float effectiveRange = tower.getRange();
        if (playing.getWeatherManager().isRaining()) {
            effectiveRange *= playing.getWeatherManager().getTowerRangeMultiplier();
        }

        // Use tower center position for range calculation
        int towerCenterX = tower.getX() + tower.getWidth() / 2;
        int towerCenterY = tower.getY() + tower.getHeight() / 2;

        // Get enemy center position
        float enemyCenterX = enemy.getSpriteCenterX();
        float enemyCenterY = enemy.getSpriteCenterY();

        // Calculate distance between centers
        float dx = enemyCenterX - towerCenterX;
        float dy = enemyCenterY - towerCenterY;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        // Add a small buffer (half of enemy size) to account for enemy hitbox
        float enemySize = enemy.getWidth() / 2f;
        float adjustedDistance = distance - enemySize;

        boolean canTarget = playing.getEnemyManager().canTargetEnemy(enemy);

        return adjustedDistance < effectiveRange && canTarget;
    }

    public BufferedImage[] getTowerImages() {
        return towerImages;
    }

    public void draw(Graphics g) {
        boolean isNight = playing.getWeatherManager() != null && playing.getWeatherManager().isNight();
        Graphics2D g2d = (Graphics2D) g;

        // Draw towers ONLY if it's NOT night. Night drawing is handled by drawLightEffects.
        if (!isNight) {
            for (Tower tower : towers) {
                BufferedImage spriteToDraw = null;
                Tower towerForDaySpriteLookup = tower;

                if (tower instanceof LightDecorator) {
                    towerForDaySpriteLookup = ((LightDecorator) tower).decoratedTower;
                }

                if (tower.isDestroyed() && tower.getDestroyedSprite() != null) {
                    spriteToDraw = tower.getDestroyedSprite();
                } else {
                    // Try to get sprite from TowerDecorator.getSprite() if applicable
                    // (e.g., for Upgraded<Type>Towers returning their specific DAY sprite)
                    if (towerForDaySpriteLookup instanceof TowerDecorator) {
                        spriteToDraw = ((TowerDecorator) towerForDaySpriteLookup).getSprite();
                    }

                    // If spriteToDraw is still null (e.g., it was a base tower, or a decorator that returned null like LightDecorator)
                    // then fall back to basic tower types for day sprites.
                    if (spriteToDraw == null) {
                        if (towerForDaySpriteLookup instanceof objects.ArcherTower) {
                            spriteToDraw = towerImages[0];
                        } else if (towerForDaySpriteLookup instanceof objects.ArtilleryTower) {
                            spriteToDraw = towerImages[1];
                        } else if (towerForDaySpriteLookup instanceof objects.MageTower) {
                            spriteToDraw = towerImages[2];
                        }
                    }
                }

                if (spriteToDraw != null) {
                    int x = tower.getX(); // Use original tower for position
                    int y = tower.getY();
                    int w = 64, h = 64;
                    if (tower.isDestroyed()) {
                        g.drawImage(spriteToDraw, x, y, 56, 56, null);
                        // Draw and update debris (existing logic for daytime)
                        if (tower.debrisList != null) {
                            long now = System.currentTimeMillis();
                            float dt = 1.0f;
                            java.util.Iterator<objects.Tower.Debris> it_debris = tower.debrisList.iterator();
                            while (it_debris.hasNext()) {
                                objects.Tower.Debris d = it_debris.next();
                                d.x += d.vx * dt;
                                d.y += d.vy * dt;
                                d.vy += 0.2f * dt;
                                d.age++;
                                d.alpha = 1f - (float)d.age / d.lifetime;
                                if (d.age > d.lifetime) it_debris.remove();
                                else {
                                    g.setColor(new java.awt.Color(d.color, true));
                                    ((java.awt.Graphics2D)g).setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, Math.max(0f, d.alpha)));
                                    g.fillRect((int)d.x, (int)d.y, d.size, d.size);
                                    ((java.awt.Graphics2D)g).setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 1f));
                                }
                            }
                            if (tower.debrisList.isEmpty() || now - tower.debrisStartTime > objects.Tower.DEBRIS_DURATION_MS) {
                                tower.debrisList = null;
                            }
                        }
                    } else {
                        g.drawImage(spriteToDraw, x, y, w, h, null);
                    }
                }
            }
        }
        // Draw warriors
        drawWarriors(g);
        // Draw upgrade effects (these are general visual effects, not tied to day/night sprites)
        Graphics2D g2d_effects = (Graphics2D) g;
        Iterator<TowerUpgradeEffect> it = upgradeEffects.iterator();
        while (it.hasNext()) {
            TowerUpgradeEffect eff = it.next();
            if (eff.isAlive()) {
                eff.draw(g2d_effects);
            } else {
                it.remove();
            }
        }
    }

    /**
     * Draws light effects for towers with lights - should be called AFTER night overlay
     * This ensures light effects appear on top of the night filter
     */
    public void drawLightEffects(Graphics g) {
        boolean isNight = playing.getWeatherManager() != null && playing.getWeatherManager().isNight();

        if (isNight) {
            Graphics2D g2d = (Graphics2D) g;
            for (Tower tower : towers) {
                if (tower.isDestroyed()) {
                    BufferedImage destroyedSprite = tower.getDestroyedSprite();
                    if (destroyedSprite != null) {
                        g.drawImage(destroyedSprite, tower.getX(), tower.getY(), 56, 56, null);
                        // Draw and update debris for destroyed towers at night
                        if (tower.debrisList != null) {
                            long now = System.currentTimeMillis();
                            float dt = 1.0f;
                            java.util.Iterator<objects.Tower.Debris> it_debris = tower.debrisList.iterator();
                            while (it_debris.hasNext()) {
                                objects.Tower.Debris d = it_debris.next();
                                d.x += d.vx * dt;
                                d.y += d.vy * dt;
                                d.vy += 0.2f * dt; // gravity
                                d.age++;
                                d.alpha = 1f - (float)d.age / d.lifetime;
                                if (d.age > d.lifetime) it_debris.remove();
                                else {
                                    g.setColor(new java.awt.Color(d.color, true));
                                    g2d.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, Math.max(0f, d.alpha)));
                                    g.fillRect((int)d.x, (int)d.y, d.size, d.size);
                                    g2d.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 1f));
                                }
                            }
                            if (tower.debrisList.isEmpty() || now - tower.debrisStartTime > objects.Tower.DEBRIS_DURATION_MS) {
                                tower.debrisList = null;
                            }
                        }
                    }
                    continue; // Skip further processing for destroyed towers
                }

                BufferedImage spriteToDraw = null;

                // Determine the tower instance whose state (type, upgraded status) we need to check.
                // This is the tower directly under a LightDecorator, or the tower itself if no LightDecorator.
                Tower towerToCheck = tower;
                if (towerToCheck instanceof LightDecorator) {
                    towerToCheck = ((LightDecorator) towerToCheck).decoratedTower;
                }

                // Determine the fundamental type (Archer, Mage, Artillery) for typeIdx.
                // This requires unwrapping further decorators like Upgraded<Type>Tower.
                Tower fundamentalTypeProvider = towerToCheck;
                while (fundamentalTypeProvider instanceof TowerDecorator) {
                    // Safety break for misconfigured decorators (e.g., decorating self)
                    Tower nextDecorator = ((TowerDecorator) fundamentalTypeProvider).decoratedTower;
                    if (nextDecorator == fundamentalTypeProvider) break;
                    fundamentalTypeProvider = nextDecorator;
                }

                int typeIdx = -1;
                // Based on sprite array order: nightTowerImages[0=Artillery, 1=Mage, 2=Archer]
                if (fundamentalTypeProvider instanceof objects.ArtilleryTower) typeIdx = 0;
                else if (fundamentalTypeProvider instanceof objects.MageTower) typeIdx = 1;
                else if (fundamentalTypeProvider instanceof objects.ArcherTower) typeIdx = 2;

                if (typeIdx != -1) {
                    // Determine if we should use the upgraded night sprite.
                    // This check is based on `towerToCheck` (the instance under LightDecorator or original tower).
                    boolean useEffectiveUpgradedSprite = false;
                    if (towerToCheck instanceof objects.UpgradedArtilleryTower ||
                            towerToCheck instanceof objects.UpgradedMageTower ||
                            towerToCheck instanceof objects.UpgradedArcherTower) {
                        useEffectiveUpgradedSprite = true;
                    } else if (towerToCheck.getLevel() == 2) { // Catches base towers upgraded to L2
                        useEffectiveUpgradedSprite = true;
                    }

                    spriteToDraw = useEffectiveUpgradedSprite ? nightUpTowerImages[typeIdx] : nightTowerImages[typeIdx];
                }

                if (spriteToDraw != null) {
                    g.drawImage(spriteToDraw, tower.getX(), tower.getY(), 64, 64, null);
                }

                // After drawing the tower's night sprite, draw its light effect if it's a LightDecorator
                if (tower instanceof LightDecorator && !tower.isDestroyed()) {
                    ((LightDecorator) tower).drawLightEffect(g2d, true);
                }
            }
        }
    }

    public List<DeadTree> findDeadTrees(int[][] level){
        List<DeadTree> trees = new ArrayList<>();
        for (int row = 0; row < level.length; row++) {
            for (int col = 0; col < level[row].length; col++) {
                if (level[row][col] == 15) {
                    int x = col * GameDimensions.TILE_DISPLAY_SIZE;
                    int y = row * GameDimensions.TILE_DISPLAY_SIZE;
                    trees.add(new DeadTree(x, y));
                }
            }
        }
        return trees;
    }
    public List<LiveTree> findLiveTrees(int[][] level){
        List<LiveTree> trees = new ArrayList<>();
        for (int row = 0; row < level.length; row++) {
            for (int col = 0; col < level[row].length; col++) {
                if (level[row][col] == 16 || level[row][col] == 17 || level[row][col] == 18) {
                    int x = col * GameDimensions.TILE_DISPLAY_SIZE;
                    int y = row * GameDimensions.TILE_DISPLAY_SIZE;
                    trees.add(new LiveTree(x, y));
                }
            }
        }
        return trees;
    }

    public void buildArcherTower(int x, int y) {
        towers.add(new ArcherTower(x, y));
    }

    // Method to build tower with custom targeting strategy
    public void buildArcherTower(int x, int y, TargetingStrategy targetingStrategy) {
        towers.add(new ArcherTower(x, y, targetingStrategy));
    }

    public void buildMageTower(int x, int y) {
        towers.add(new MageTower(x, y));
    }

    // Method to build tower with custom targeting strategy
    public void buildMageTower(int x, int y, TargetingStrategy targetingStrategy) {
        towers.add(new MageTower(x, y, targetingStrategy));
    }

    public void buildArtilerryTower(int x, int y) {
        towers.add(new ArtilleryTower(x, y));
    }

    // Method to build tower with custom targeting strategy
    public void buildArtilleryTower(int x, int y, TargetingStrategy targetingStrategy) {
        towers.add(new ArtilleryTower(x, y, targetingStrategy));
    }

    public ArrayList<Tower> getTowers() {return towers;}

    public void clearTowers() {
        towers.clear();
    }

    public void addTower(Tower tower) {
        towers.add(tower);
    }

    // Method to replace an old tower instance with a new one (e.g., after upgrading)
    public void replaceTower(Tower oldTower, Tower newTower) {
        int index = towers.indexOf(oldTower);
        if (index != -1) {
            towers.set(index, newTower);
        } else {
            // This case should ideally not happen if oldTower was in the list.
            // Log an error or handle as appropriate.
            System.err.println("Error: Old tower not found in list for replacement.");
        }
    }

    // Inner class for upgrade visual effect
    private static class TowerUpgradeEffect {
        private final int x, y;
        private final long startTime;
        private static final long DURATION = 500_000_000L; // 0.5 seconds in nanoseconds
        public TowerUpgradeEffect(int x, int y) {
            this.x = x;
            this.y = y;
            this.startTime = System.nanoTime();
        }
        public boolean isAlive() {
            return System.nanoTime() - startTime < DURATION;
        }
        public float getProgress() {
            return Math.min(1f, (System.nanoTime() - startTime) / (float)DURATION);
        }
        public void draw(Graphics2D g2d) {
            float progress = getProgress();
            float alpha = 0.7f * (1f - progress);
            int size = (int)(64 + 32 * progress);
            int offset = (size - 64) / 2;
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2d.setColor(new Color(255, 255, 0));
            g2d.setStroke(new BasicStroke(4f));
            g2d.drawOval(x - offset, y - offset, size, size);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        }
    }

    public void triggerUpgradeEffect(Tower tower) {
        upgradeEffects.add(new TowerUpgradeEffect(tower.getX() + 32, tower.getY() + 32));
    }

    // Method to spawn a Wizard Warrior
    public void spawnWizardWarrior(int x, int y) {
        warriors.add(new WizardWarrior(x, y));
    }

    // Method to spawn an Archer Warrior
    public void spawnArcherWarrior(int x, int y) {
        warriors.add(new ArcherWarrior(x, y));
    }

    public List<Warrior> getWarriors() {
        return warriors;
    }

    public void updateWarriors(float speedMultiplier) {
        for (Warrior warrior : warriors) {
            warrior.update(speedMultiplier); // Handles cooldown
            warrior.updateAnimationTick(); // Update animation frame
            if (!warrior.isCooldownOver()) {
                continue;
            }
            // Attack logic for warriors
            attackEnemyIfInRange(warrior);
        }
    }

    private void attackEnemyIfInRange(Warrior warrior) {
        // Collect all enemies in range
        List<Enemy> enemiesInRange = new ArrayList<>();
        for (Enemy enemy : playing.getEnemyManager().getEnemies()) {
            if (enemy.isAlive() && isEnemyInRange(warrior, enemy)) {
                enemiesInRange.add(enemy);
            }
        }

        // Use the warrior's targeting strategy to select the best target
        if (!enemiesInRange.isEmpty()) {
            TargetingStrategy strategy = warrior.getTargetingStrategy();
            Enemy target = strategy.selectTarget(enemiesInRange, warrior);

            if (target != null) {
                playing.shootEnemy(warrior, target);
                warrior.resetCooldown();
            }
        }
    }

    private boolean isEnemyInRange(Warrior warrior, Enemy enemy) {
        float effectiveRange = warrior.getRange();
        if (playing.getWeatherManager().isRaining()) {
            effectiveRange *= playing.getWeatherManager().getTowerRangeMultiplier();
        }

        // Use warrior center position for range calculation
        int warriorCenterX = warrior.getX() + warrior.getWidth() / 2;
        int warriorCenterY = warrior.getY() + warrior.getHeight() / 2;
        
        // Get enemy center position
        float enemyCenterX = enemy.getSpriteCenterX();
        float enemyCenterY = enemy.getSpriteCenterY();
        
        // Calculate distance between centers
        float dx = enemyCenterX - warriorCenterX;
        float dy = enemyCenterY - warriorCenterY;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        
        // Add a small buffer (half of enemy size) to account for enemy hitbox
        float enemySize = enemy.getWidth() / 2f;
        float adjustedDistance = distance - enemySize;

        boolean canTarget = playing.getEnemyManager().canTargetEnemy(enemy);

        return adjustedDistance < effectiveRange && canTarget;
    }

    public void drawWarriors(Graphics g) {
        for (Warrior warrior : warriors) {
            BufferedImage[] frames = warrior.getAnimationFrames();
            if (frames != null && frames.length > 0) {
                int frameIndex = warrior.getAnimationIndex();
                if (frameIndex < frames.length) {
                    BufferedImage sprite = frames[frameIndex];
                    if (sprite != null) {
                        int x = warrior.getX();
                        int y = warrior.getY();
                        // Adjust drawing position to center the warrior on the tile if necessary
                        // Assuming warrior images are similar in size to tower images (64x64 or need scaling)
                        // For now, direct drawing at x,y. May need to adjust x,y if sprite is not tile-centered.
                        g.drawImage(sprite, x, y, 64, 64, null);
                    }
                }
            }
        }
    }
    /**
     * Checks if any tower with light is illuminating the given position at night
     * @param x The x coordinate to check
     * @param y The y coordinate to check
     * @return true if the position is lit by any tower's light, false otherwise
     */
    public boolean isPositionLit(float x, float y) {
        if (playing.getWeatherManager() == null || !playing.getWeatherManager().isNight()) {
            return true; // During day, everything is visible
        }

        for (Tower tower : towers) {
            if (tower instanceof LightDecorator && !tower.isDestroyed()) {
                LightDecorator lightTower = (LightDecorator) tower;
                if (lightTower.isPositionLit(x, y)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if any tower with light is illuminating the given enemy at night
     * @param enemy The enemy to check
     * @return true if the enemy is lit by any tower's light, false otherwise
     */
    public boolean isEnemyLit(enemies.Enemy enemy) {
        return isPositionLit(enemy.getSpriteCenterX(), enemy.getSpriteCenterY());
    }

    /**
     * Upgrades a tower with a light decorator
     * @param tower The tower to upgrade with light
     * @return The new LightDecorator or null if upgrade failed
     */
    public LightDecorator upgradeTowerWithLight(Tower tower) {
        if (tower == null || tower.isDestroyed()) {
            return null;
        }

        // Don't allow double decoration with light
        if (tower instanceof LightDecorator) {
            return (LightDecorator) tower;
        }

        LightDecorator lightTower = new LightDecorator(tower);
        replaceTower(tower, lightTower);
        triggerUpgradeEffect(lightTower);

        return lightTower;
    }

    /**
     * Checks if a tower can be upgraded with light
     * @param tower The tower to check
     * @return true if the tower can be upgraded with light
     */
    public boolean canUpgradeWithLight(Tower tower) {
        return tower != null && !tower.isDestroyed() && !(tower instanceof LightDecorator);
    }

    /**
     * Gets the cost for upgrading a tower with light
     * @return The gold cost for light upgrade
     */
    public int getLightUpgradeCost() {
        return 50; // Changed from 100 to 50
    }
}


