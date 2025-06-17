package managers;

import constants.GameDimensions;
import enemies.Enemy;
import helpMethods.LoadSave;
import objects.ArcherTower;
import objects.ArtilleryTower;
import objects.MageTower;
import objects.PoisonTower;
import objects.Tower;
import objects.TowerDecorator;
import strategies.TargetingStrategy;
import scenes.Playing;
import ui_p.AssetsLoader;
import ui_p.DeadTree;
import ui_p.LiveTree;
import objects.Warrior;
import objects.WizardWarrior;
import objects.ArcherWarrior;
import objects.TNTWarrior;
import java.util.Map;
import java.util.HashMap;
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
    private List<objects.TNTWarrior> tntWarriors = new ArrayList<>();
    private Map<Tower, Integer> towerTNTCounts = new HashMap<>(); // Track total TNT warriors spawned per tower

    public TowerManager(Playing playing) {
        this.playing = playing;
        loadTowerImages();
    }

    private void loadTowerImages() {
        BufferedImage tilesetImage = LoadSave.getSpriteAtlas();
        towerImages = new BufferedImage[4];
        // Load from correct positions in tileset
        towerImages[0] = tilesetImage.getSubimage(2 * 64, 6 * 64, 64, 64); // Archer Tower (row 6, col 2)
        towerImages[1] = tilesetImage.getSubimage(0 * 64, 5 * 64, 64, 64); // Artillery Tower (row 5, col 0)
        towerImages[2] = tilesetImage.getSubimage(1 * 64, 5 * 64, 64, 64); // Mage Tower (row 5, col 1)
        towerImages[3] = AssetsLoader.getInstance().poisonTowerImg; // Poison Tower from asset

        // Load night mode sprites
        nightTowerImages = new BufferedImage[4];
        nightUpTowerImages = new BufferedImage[4];
        BufferedImage nightImg = LoadSave.getImageFromPath("/TowerAssets/towerNight.png");
        BufferedImage nightUpImg = LoadSave.getImageFromPath("/TowerAssets/towerUpNight.png");
        if (nightImg != null) {
            for (int i = 0; i < 3; i++) {
                nightTowerImages[i] = nightImg.getSubimage(i * 384, 0, 384, 384);
            }
            // For poison tower, reuse mage tower night sprite for now
            nightTowerImages[3] = nightTowerImages[2];
        }
        if (nightUpImg != null) {
            for (int i = 0; i < 3; i++) {
                nightUpTowerImages[i] = nightUpImg.getSubimage(i * 384, 0, 384, 384);
            }
            // For poison tower, reuse mage tower night sprite for now
            nightUpTowerImages[3] = nightUpTowerImages[2];
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

        // Towers can always attack - night effects are handled through enemy targeting

        // Collect all enemies in range
        List<Enemy> enemiesInRange = new ArrayList<>();
        for (Enemy enemy : playing.getEnemyManager().getEnemies()) {
            if (enemy.isAlive() && isEnemyInRange(tower, enemy)) {
                enemiesInRange.add(enemy);
            }
        }

        // Use the tower's targeting strategy to select the best target
        if (!enemiesInRange.isEmpty()) {
            // Special handling for PoisonTower - it attacks ALL enemies in range, not just one
            if (tower.getType() == constants.Constants.Towers.POISON) {
                // PoisonTower attacks ALL enemies in range directly
                tower.incrementUsage(); // Increment usage count when tower attacks
                tower.resetCooldown();

                // Apply poison effect to all enemies in range
                for (Enemy target : enemiesInRange) {
                    tower.applyOnHitEffect(target, playing);
                }

                System.out.println("Poison Tower attacked " + enemiesInRange.size() + " enemies in range");
                return;
            }

            TargetingStrategy strategy = tower.getTargetingStrategy();
            Enemy target = strategy.selectTarget(enemiesInRange, tower);

            if (target != null) {

                tower.incrementUsage(); // Increment usage count when tower attacks
                tower.resetCooldown();
                tower.applyOnHitEffect(target, playing);

                // Windy weather effect: Archer towers have 30% chance to miss
                // Check windy weather effect BEFORE shooting
                boolean shouldMiss = false;

                if (playing.getWeatherManager().isWindy() && tower.getType() == constants.Constants.Towers.ARCHER) {
                    // 30% chance to miss in windy weather
                    if (Math.random() < 0.3) {
                        shouldMiss = true;
                        System.out.println("Archer tower missed due to windy weather!");
                    }
                }

                // Always shoot (for cooldown consistency), but the projectile will handle miss logic
                playing.shootEnemy(tower, target);
                tower.resetCooldown();

                // Note: The ProjectileManager will handle the actual miss behavior using willMiss flag
            }
        }
    }

    private boolean isEnemyInRange(Tower tower, Enemy enemy) {
        float effectiveRange = tower.getConditionBasedRange();
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


    public void draw(Graphics g, float gameSpeedMultiplier) {
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
                        } else if (towerForDaySpriteLookup instanceof objects.PoisonTower) {
                            spriteToDraw = towerImages[3];
                        }
                    }
                }
                if (spriteToDraw != null) {
                    int x = tower.getX();
                    int y = tower.getY();
                    int w = tower.getWidth();
                    int h = tower.getHeight();
                    g.drawImage(spriteToDraw, x, y, w, h, null);

                    // Draw green smoke effect for poison towers
                    if (tower instanceof objects.PoisonTower && !tower.isDestroyed()) {
                        objects.PoisonTower poisonTower = (objects.PoisonTower) tower;
                        if (poisonTower.isShowingSmokeEffect()) {
                            drawPoisonSmokeEffect(g, poisonTower);
                        }
                    }
                }
            }
        }
        // Draw warriors
        drawWarriors(g, gameSpeedMultiplier);
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

                // Check if this tower has light upgrade
                boolean hasLightUpgrade = tower instanceof LightDecorator;

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
                else if (fundamentalTypeProvider instanceof objects.PoisonTower) typeIdx = 3;

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

                    // Only use light-equipped sprites if tower has light upgrade
                    if (hasLightUpgrade) {
                        spriteToDraw = useEffectiveUpgradedSprite ? nightUpTowerImages[typeIdx] : nightTowerImages[typeIdx];
                    } else {
                        // Towers without light should use basic day sprites or be dimmed/not visible
                        // For now, we'll use a dimmed version of day sprites to show they're inactive
                        if (fundamentalTypeProvider instanceof objects.ArcherTower) {
                            spriteToDraw = towerImages[0];
                        } else if (fundamentalTypeProvider instanceof objects.ArtilleryTower) {
                            spriteToDraw = towerImages[1];
                        } else if (fundamentalTypeProvider instanceof objects.MageTower) {
                            spriteToDraw = towerImages[2];
                        } else if (fundamentalTypeProvider instanceof objects.PoisonTower) {
                            spriteToDraw = towerImages[3];
                        }
                    }
                }

                if (spriteToDraw != null) {
                    if (hasLightUpgrade) {
                        // Draw light-equipped towers normally
                        g.drawImage(spriteToDraw, tower.getX(), tower.getY(), 64, 64, null);
                    } else {
                        // Draw non-light towers dimmed to show they're inactive
                        Graphics2D g2dDim = (Graphics2D) g.create();
                        g2dDim.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f)); // 50% opacity
                        g2dDim.drawImage(spriteToDraw, tower.getX(), tower.getY(), 64, 64, null);
                        g2dDim.dispose();
                    }
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
        // Don't notify tower placement during wave - wave start states should remain unchanged
    }

    // Method to build tower with custom targeting strategy
    public void buildArcherTower(int x, int y, TargetingStrategy targetingStrategy) {
        towers.add(new ArcherTower(x, y, targetingStrategy));
        // Don't notify tower placement during wave - wave start states should remain unchanged
    }

    public void buildMageTower(int x, int y) {
        towers.add(new MageTower(x, y));
        // Don't notify tower placement during wave - wave start states should remain unchanged
    }

    // Method to build tower with custom targeting strategy
    public void buildMageTower(int x, int y, TargetingStrategy targetingStrategy) {
        towers.add(new MageTower(x, y, targetingStrategy));
        // Don't notify tower placement during wave - wave start states should remain unchanged
    }

    public void buildArtilerryTower(int x, int y) {
        towers.add(new ArtilleryTower(x, y));
        // Don't notify tower placement during wave - wave start states should remain unchanged
    }

    // Method to build tower with custom targeting strategy
    public void buildArtilleryTower(int x, int y, TargetingStrategy targetingStrategy) {
        towers.add(new ArtilleryTower(x, y, targetingStrategy));
        // Don't notify tower placement during wave - wave start states should remain unchanged
    }

    public void buildPoisonTower(int x, int y) {
        towers.add(new PoisonTower(x, y));
        // Don't notify tower placement during wave - wave start states should remain unchanged
    }



    public ArrayList<Tower> getTowers() {return towers;}

    public void clearTowers() {
        towers.clear();
    }

    public void addTower(Tower tower) {
        towers.add(tower);
        // Don't notify for tower placement during restore - this would cause infinite loops
        // Only notify for new tower placements during gameplay
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

    // Method to remove a tower (e.g., when selling)
    public void removeTower(Tower tower) {
        boolean removed = towers.remove(tower);
        if (removed) {
            // Reset the tile data back to dead tree where the tower was located
            resetTileToDeadTree(tower);

            // Create a dead tree object at the tower's position
            createDeadTreeAtPosition(tower.getX(), tower.getY());

            // Don't notify tower removal during wave - wave start states should remain unchanged
            // Only update wave start states when towers are sold before wave starts

            System.out.println("Tower removed successfully and dead tree created");
        } else {
            System.err.println("Error: Tower not found in list for removal.");
        }
    }

    /**
     * Resets the tile at the tower's position back to grass (ID 5)
     */
    private void resetTileToGrass(Tower tower) {
        if (tower == null || playing == null) return;

        // Convert tower pixel coordinates to tile coordinates
        int tileX = tower.getX() / GameDimensions.TILE_DISPLAY_SIZE;
        int tileY = tower.getY() / GameDimensions.TILE_DISPLAY_SIZE;

        // Get the level data
        int[][] level = playing.getLevel();
        if (level == null) return;

        // Check bounds
        if (tileY >= 0 && tileY < level.length && tileX >= 0 && tileX < level[0].length) {
            // Reset to grass tile (ID 5)
            level[tileY][tileX] = 5;
            System.out.println("Reset tile at (" + tileX + ", " + tileY + ") to grass");
        }
    }

    /**
     * Resets the tile at the tower's position back to dead tree (ID 15)
     */
    private void resetTileToDeadTree(Tower tower) {
        if (tower == null || playing == null) return;

        // Convert tower pixel coordinates to tile coordinates
        int tileX = tower.getX() / GameDimensions.TILE_DISPLAY_SIZE;
        int tileY = tower.getY() / GameDimensions.TILE_DISPLAY_SIZE;

        // Get the level data
        int[][] level = playing.getLevel();
        if (level == null) return;

        // Check bounds
        if (tileY >= 0 && tileY < level.length && tileX >= 0 && tileX < level[0].length) {
            // Reset to dead tree tile (ID 15)
            level[tileY][tileX] = 15;
            System.out.println("Reset tile at (" + tileX + ", " + tileY + ") to dead tree");
        }
    }

    /**
     * Creates a dead tree object at the specified position and adds it to the game
     */
    private void createDeadTreeAtPosition(int x, int y) {
        if (playing == null) return;

        // Create a new dead tree at the tower's position
        DeadTree deadTree = new DeadTree(x, y);

        // Add it to the playing scene's dead trees list
        if (playing.getDeadTrees() != null) {
            playing.getDeadTrees().add(deadTree);
            System.out.println("Created dead tree at position (" + x + ", " + y + ")");
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


    public List<Warrior> getWarriors() {
        return warriors;
    }

    public void updateWarriors(float speedMultiplier) {
        // Update all warriors and handle lifetime/removal logic
        warriors.removeIf(warrior -> {
            warrior.update(speedMultiplier); // Handles cooldown, movement, and lifetime

            // Remove warriors that have returned to tower after lifetime expired
            if (warrior.shouldBeRemoved()) {
                // Decrement the tower's warrior count when removing the warrior
                Tower spawnTower = warrior.getSpawnedFromTower();
                if (spawnTower != null) {
                    spawnTower.removeWarrior();
                }
                System.out.println("Removing warrior that returned to tower");
                return true;
            }

            // Only allow attacking if warrior has reached destination and isn't returning
            if (!warrior.hasReachedDestination() || warrior.isReturning()) {
                return false; // Skip attacking logic for running/returning warriors
            }

            // Check if there are enemies in range
            boolean hasEnemiesInRange = false;
            for (Enemy enemy : playing.getEnemyManager().getEnemies()) {
                if (enemy.isAlive() && isEnemyInRange(warrior, enemy)) {
                    hasEnemiesInRange = true;
                    break;
                }
            }

            // Update warrior state based on enemy presence
            if (hasEnemiesInRange) {
                warrior.setAttackingState();

                // Only attack if cooldown is over
                if (warrior.isCooldownOver()) {
                    attackEnemyIfInRange(warrior);
                }
            } else {
                warrior.setIdleState();
            }

            return false; // Keep warrior
        });

        // Update TNT warriors
        updateTNTWarriors(speedMultiplier);
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
                // Update warrior's facing direction to face the target enemy
                warrior.updateFacingDirectionForTarget(target);

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

    public void drawWarriors(Graphics g, float gameSpeedMultiplier) {
        for (Warrior warrior : warriors) {
            BufferedImage[] frames = warrior.getAnimationFrames();
            if (frames != null && frames.length > 0) {
                int frameIndex = warrior.getAnimationIndex();
                if (frameIndex < frames.length) {
                    BufferedImage sprite = frames[frameIndex];
                    if (sprite != null) {
                        int x = warrior.getX();
                        int y = warrior.getY();

                        // Determine sprite dimensions and scaling based on warrior type
                        int drawWidth, drawHeight;
                        if (warrior instanceof WizardWarrior) {
                            drawWidth = 92;
                            drawHeight = 76;
                            x += (64 - drawWidth) / 2;
                            y += (64 - drawHeight) + 12;
                        } else {
                            drawWidth = 84;
                            drawHeight = 84;
                            x += (64 - drawWidth) / 2;
                            y += (64 - drawHeight) + 20;
                        }

                        if (warrior.isFacingLeft()) {
                            g.drawImage(sprite, x + drawWidth, y, -drawWidth, drawHeight, null);
                        } else {
                            g.drawImage(sprite, x, y, drawWidth, drawHeight, null);
                        }
                    }
                }
            }
            // Draw lifetime bar for the warrior with speed multiplier
            warrior.drawLifetimeBar(g, gameSpeedMultiplier);
        }

        // Draw TNT warriors
        for (TNTWarrior tnt : tntWarriors) {
            tnt.draw(g);
        }
    }

    private void updateTNTWarriors(float speedMultiplier) {
        List<TNTWarrior> tntToRemove = new ArrayList<>();
        List<enemies.Enemy> allEnemies = playing.getEnemyManager().getEnemies();

        for (TNTWarrior tnt : tntWarriors) {
            tnt.update(speedMultiplier, allEnemies);

            // Mark for removal if explosion is complete
            if (!tnt.isActive()) {
                tntToRemove.add(tnt);
            }
        }

        // Remove completed TNT warriors (but don't decrease total spawn count)
        for (TNTWarrior tnt : tntToRemove) {
            tntWarriors.remove(tnt);
            // Note: We don't decrease towerTNTCounts because it tracks total spawns, not active TNT warriors
        }
    }

    public void spawnTNTWarrior(Tower artilleryTower) {
        // Check if tower can spawn more TNT warriors (max 2 total)
        int totalSpawned = towerTNTCounts.getOrDefault(artilleryTower, 0);
        if (totalSpawned >= 2) {
            System.out.println("Artillery tower has already spawned maximum TNT warriors (2 total)");
            return;
        }

        // Check if there are any enemies to target
        List<enemies.Enemy> allEnemies = playing.getEnemyManager().getEnemies();
        enemies.Enemy closestEnemy = null;
        float closestDistance = Float.MAX_VALUE;

        int towerCenterX = artilleryTower.getX() + artilleryTower.getWidth() / 2;
        int towerCenterY = artilleryTower.getY() + artilleryTower.getHeight() / 2;

        for (enemies.Enemy enemy : allEnemies) {
            if (enemy.isAlive()) {
                float dx = enemy.getSpriteCenterX() - towerCenterX;
                float dy = enemy.getSpriteCenterY() - towerCenterY;
                float distance = (float) Math.sqrt(dx * dx + dy * dy);
                if (distance < closestDistance) {
                    closestDistance = distance;
                    closestEnemy = enemy;
                }
            }
        }

        if (closestEnemy == null) {
            System.out.println("No enemies available for TNT warrior to target");
            return;
        }

        // Create TNT warrior at tower position
        TNTWarrior tntWarrior = new TNTWarrior(towerCenterX, towerCenterY);
        tntWarrior.setTarget(closestEnemy);
        tntWarrior.setPlayingScene(playing); // Pass playing scene for screen shake effects

        // Add to lists and update total spawn count
        tntWarriors.add(tntWarrior);
        towerTNTCounts.put(artilleryTower, totalSpawned + 1);

        // Play TNT spawn sound
        AudioManager.getInstance().playTNTSpawnSound();

        System.out.println("Spawned TNT warrior from artillery tower at position (" + towerCenterX + ", " + towerCenterY + "), targeting closest enemy at (" + closestEnemy.getSpriteCenterX() + ", " + closestEnemy.getSpriteCenterY() + ")");
    }

    public boolean canSpawnTNTWarrior(Tower tower) {
        // Only artillery towers can spawn TNT warriors
        if (tower.getType() != constants.Constants.Towers.ARTILLERY) {
            return false;
        }

        // Check if tower has reached maximum total TNT warrior spawns
        int totalSpawned = towerTNTCounts.getOrDefault(tower, 0);
        return totalSpawned < 2;
    }

    public int getTNTWarriorCount(Tower tower) {
        // Returns total spawned count (for tooltip display)
        return towerTNTCounts.getOrDefault(tower, 0);
    }

    public List<TNTWarrior> getTNTWarriors() {
        return tntWarriors;
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

    /**
     * Update all existing towers with new stats from GameOptions
     * This is called when difficulty changes to apply new tower stats
     */
    public void updateAllTowerStatsFromOptions(config.GameOptions gameOptions) {
        if (gameOptions == null) {
            System.out.println("Warning: Cannot update tower stats - GameOptions is null");
            return;
        }

        System.out.println("Updating all existing towers with new difficulty settings...");
        for (Tower tower : towers) {
            if (tower != null && !tower.isDestroyed()) {
                applyGameOptionsToTower(tower, gameOptions);
            }
        }
        System.out.println("Updated " + towers.size() + " towers with new difficulty settings");
    }

    /**
     * Apply GameOptions stats to a specific tower
     */
    private void applyGameOptionsToTower(Tower tower, config.GameOptions gameOptions) {
        try {
            config.TowerType towerType = getTowerTypeFromConstants(tower.getType());
            if (towerType != null && gameOptions.getTowerStats().containsKey(towerType)) {
                config.TowerStats stats = gameOptions.getTowerStats().get(towerType);

                // Apply stats using reflection to access protected fields
                java.lang.reflect.Field damageField = Tower.class.getDeclaredField("damage");
                damageField.setAccessible(true);
                damageField.setInt(tower, stats.getDamage());

                java.lang.reflect.Field rangeField = Tower.class.getDeclaredField("range");
                rangeField.setAccessible(true);
                rangeField.setFloat(tower, (float)stats.getRange() * GameDimensions.TILE_DISPLAY_SIZE); // Convert to pixels

                java.lang.reflect.Field cooldownField = Tower.class.getDeclaredField("cooldown");
                cooldownField.setAccessible(true);
                cooldownField.setFloat(tower, (float)(60.0 / stats.getFireRate())); // Convert fire rate to cooldown ticks

                System.out.println("Applied stats to " + towerType + " tower: Damage=" + stats.getDamage() +
                        ", Range=" + stats.getRange() + ", FireRate=" + stats.getFireRate());
            }
        } catch (Exception e) {
            System.out.println("Error applying stats to tower: " + e.getMessage());
        }
    }

    /**
     * Convert Constants tower type to TowerType enum
     */
    private config.TowerType getTowerTypeFromConstants(int towerType) {
        switch (towerType) {
            case constants.Constants.Towers.ARCHER:
                return config.TowerType.ARCHER;
            case constants.Constants.Towers.ARTILLERY:
                return config.TowerType.ARTILLERY;
            case constants.Constants.Towers.MAGE:
                return config.TowerType.MAGE;
            case constants.Constants.Towers.POISON:
                return null; // No config for poison tower yet
            default:
                return null;
        }
    }

    /**
     * Get tower build cost from GameOptions
     */
    public int getTowerCostFromOptions(int towerType, config.GameOptions gameOptions) {
        if (gameOptions == null) {
            return constants.Constants.Towers.getCost(towerType); // Fallback to Constants
        }

        config.TowerType type = getTowerTypeFromConstants(towerType);
        if (type != null && gameOptions.getTowerStats().containsKey(type)) {
            return gameOptions.getTowerStats().get(type).getBuildCost();
        }

        return constants.Constants.Towers.getCost(towerType); // Fallback
    }

    /**
     * Clear all warriors for game reset
     */
    public void clearWarriors() {
        warriors.clear();
        tntWarriors.clear();

        for (Tower tower : towers) {
            if (tower != null) {
                try {
                    java.lang.reflect.Field currentWarriorsField = Tower.class.getDeclaredField("currentWarriors");
                    currentWarriorsField.setAccessible(true);
                    currentWarriorsField.setInt(tower, 0);
                } catch (Exception e) {
                    System.out.println("Could not reset warrior count for tower: " + e.getMessage());
                }
            }
        }

        towerTNTCounts.clear();

        System.out.println("TowerManager cleared: " + warriors.size() + " warriors, " + tntWarriors.size() + " TNT warriors, reset all tower warrior counts");
    }

    /**
     * Draw green smoke effect around poison tower when it attacks
     */
    private void drawPoisonSmokeEffect(Graphics g, objects.PoisonTower poisonTower) {
        Graphics2D g2d = (Graphics2D) g.create();

        float progress = poisonTower.getSmokeEffectProgress();
        float alpha = progress * 0.6f;

        Color smokeColor = new Color(0, 255, 0, (int)(alpha * 255));
        g2d.setColor(smokeColor);

        g2d.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, alpha));

        int centerX = poisonTower.getX() + poisonTower.getWidth() / 2;
        int centerY = poisonTower.getY() + poisonTower.getHeight() / 2;

        for (int i = 0; i < 3; i++) {
            int radius = (int)(20 + i * 15 + (1.0f - progress) * 20); // Expanding smoke
            int smokeX = centerX - radius;
            int smokeY = centerY - radius;
            int diameter = radius * 2;

            float ringAlpha = alpha * (0.8f - i * 0.2f);
            g2d.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, ringAlpha));

            g2d.fillOval(smokeX, smokeY, diameter, diameter);
        }

        g2d.dispose();
    }
}


