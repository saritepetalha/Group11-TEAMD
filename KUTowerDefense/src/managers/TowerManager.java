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

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TowerManager {
    private Playing playing;
    private BufferedImage[] towerImages;
    private ArrayList<Tower> towers = new ArrayList<>();
    private List<TowerUpgradeEffect> upgradeEffects = new ArrayList<>();

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

        int range = Utils.GetHypo(tower.getX(), tower.getY(), enemy.getX(), enemy.getY());

        boolean canTarget = playing.getEnemyManager().canTargetEnemy(enemy);

        return range < effectiveRange && canTarget;
    }

    public BufferedImage[] getTowerImages() {
        return towerImages;
    }

    public void draw(Graphics g) {
        for (Tower tower : towers) {
            BufferedImage sprite = null;
            if (tower.isDestroyed() && tower.getDestroyedSprite() != null) {
                sprite = tower.getDestroyedSprite();
                // Draw at 10% size and center on tile
                int smallW = 56;
                int smallH = 56;
                int centerX = tower.getX() + 32 - smallW / 2;
                int centerY = tower.getY() + 32 - smallH / 2;
                g.drawImage(sprite, centerX, centerY, smallW, smallH, null);
                continue;
            } else if (tower instanceof TowerDecorator) {
                sprite = ((TowerDecorator) tower).getSprite();
            } else if (tower instanceof objects.ArcherTower) {
                sprite = towerImages[0];
            } else if (tower instanceof objects.ArtilleryTower) {
                sprite = towerImages[1];
            } else if (tower instanceof objects.MageTower) {
                sprite = towerImages[2];
            }

            if (sprite != null) {
                g.drawImage(sprite, tower.getX(), tower.getY(), 64, 64, null);
            }
        }
        // Draw upgrade effects
        Graphics2D g2d = (Graphics2D) g;
        Iterator<TowerUpgradeEffect> it = upgradeEffects.iterator();
        while (it.hasNext()) {
            TowerUpgradeEffect eff = it.next();
            if (eff.isAlive()) {
                eff.draw(g2d);
            } else {
                it.remove();
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
}


