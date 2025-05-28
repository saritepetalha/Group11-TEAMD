package managers;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import config.GameOptions;
import static constants.Constants.Enemies.BARREL;
import static constants.Constants.Enemies.GOBLIN;
import static constants.Constants.Enemies.TNT;
import static constants.Constants.Enemies.TROLL;
import static constants.Constants.Enemies.WARRIOR;
import static constants.Constants.PathPoints.END_POINT;
import static constants.Constants.PathPoints.START_POINT;
import constants.GameDimensions;
import enemies.Barrel;
import enemies.Enemy;
import enemies.Goblin;
import enemies.TNT;
import enemies.Troll;
import enemies.Warrior;
import helpMethods.LoadSave;
import helpMethods.OptionsIO;
import helpMethods.Utils;
import objects.GridPoint;
import scenes.Playing;
import objects.Tower;
import constants.Constants;
import ui_p.FireAnimation;

public class EnemyManager {
    private Playing playing;
    private BufferedImage[] enemyImages;
    private ArrayList<Enemy> enemies = new ArrayList<>();
    private ArrayList<GridPoint> pathPoints = new ArrayList<>();
    private GridPoint startPoint, endPoint;
    private int tileSize = GameDimensions.TILE_DISPLAY_SIZE;
    private int nextEnemyID = 0;
    private boolean pathFound = false;
    private static final Set<Integer> ROAD_IDS = Set.of(0,1,2,3,4,6,7,8,9,10,11,12,13,14,32);
    private GameOptions gameOptions;
    private WeatherManager weatherManager;
    private Map<Enemy, Long> enemySpawnTimes;

    public EnemyManager(Playing playing, int[][] overlayData, int [][] tileData, GameOptions options) {
        this.playing = playing;
        this.gameOptions = options;
        if (this.gameOptions == null) {
            System.out.println("Warning: Received null GameOptions in EnemyManager, using defaults.");
            this.gameOptions = GameOptions.defaults();
        }
        enemyImages = extractEnemyFrames();
        this.enemySpawnTimes = new HashMap<>();
        this.weatherManager = playing.getWeatherManager();

        findStartAndEndPoints(overlayData);

        if (startPoint != null && endPoint != null) {
            generatePath(tileData);
        }

        System.out.println("EnemyManager Initialized. Path found? " + pathFound);
    }

    private void findStartAndEndPoints(int[][] overlayData) {
        for (int y = 0; y < overlayData.length; y++) {
            for (int x = 0; x < overlayData[y].length; x++) {
                if (overlayData[y][x] == START_POINT) {
                    startPoint = new GridPoint(x, y);
                }
                else if (overlayData[y][x] == END_POINT) {
                    endPoint = new GridPoint(x, y);
                    if (isGateTile(x, y, overlayData)) {
                        GridPoint newEndPoint = findLastRoadTileBeforeGate(x, y, overlayData);
                        if (newEndPoint != null) {
                            endPoint = newEndPoint;
                        }
                    }
                }
            }
        }
    }

    private boolean isValidPosition(int x, int y, int rows, int cols) {
        return x >= 0 && x < cols && y >= 0 && y < rows;
    }

    private boolean isRoadTile(int tileId) {
        return ROAD_IDS.contains(tileId) || tileId == -4;
    }

    private boolean isGateTile(int x, int y, int[][] overlayData) {
        return overlayData[y][x] == -4;
    }

    private GridPoint findLastRoadTileBeforeGate(int gateX, int gateY, int[][] overlayData) {
        int[] dx = {-1, 0, 1, 0};
        int[] dy = {0, -1, 0, 1};

        for (int i = 0; i < 4; i++) {
            int newX = gateX + dx[i];
            int newY = gateY + dy[i];

            if (isValidPosition(newX, newY, overlayData.length, overlayData[0].length) &&
                    isRoadTile(overlayData[newY][newX])) {
                return new GridPoint(newX, newY);
            }
        }
        return null;
    }

    private void generatePath(int[][] tileData) {
        if (startPoint == null || endPoint == null) {
            System.out.println("Cannot generate path: Start or end point is null");
            return;
        }

        System.out.println("Generating path from " + startPoint + " to " + endPoint);

        int rows = tileData.length;
        int cols = tileData[0].length;

        // direction arrays for 4-directional movement
        int[] dx = {-1, 0, 1, 0}; // left, up, right, down
        int[] dy = {0, -1, 0, 1};

        // initialize visited array and parent map for path reconstruction
        boolean[][] visited = new boolean[rows][cols];
        GridPoint[][] parent = new GridPoint[rows][cols];

        // BFS queue
        Queue<GridPoint> queue = new LinkedList<>();
        queue.add(startPoint);
        visited[startPoint.getY()][startPoint.getX()] = true;

        boolean foundEnd = false;

        // BFS to find path
        while (!queue.isEmpty() && !foundEnd) {
            GridPoint current = queue.poll();

            // check if we reached the end
            if (current.equals(endPoint)) {
                System.out.println("Found end point!");
                foundEnd = true;
                break;
            }

            // try all four directions respectively
            for (int i = 0; i < 4; i++) {
                int newX = current.getX() + dx[i];
                int newY = current.getY() + dy[i];

                // check bounds and if it's a valid road and not visited
                if (isValidPosition(newX, newY, rows, cols) &&
                        (isRoadTile(tileData[newY][newX]) || (newX == endPoint.getX() && newY == endPoint.getY())) &&
                        !visited[newY][newX]) {
                    System.out.println("Found valid road tile at: " + newX + "," + newY);
                    GridPoint next = new GridPoint(newX, newY);
                    queue.add(next);
                    visited[newY][newX] = true;
                    parent[newY][newX] = current;
                }
            }
        }

        // if end found, reconstruct the path
        if (foundEnd) {
            System.out.println("Reconstructing path...");
            reconstructPath(parent);
            pathFound = true;
            System.out.println("Path found with " + pathPoints.size() + " points");
        } else {
            System.out.println("No path found!");

            System.out.println("Start point: " + startPoint);
            System.out.println("End point: " + endPoint);
            System.out.println("Current map state:");
            for (int y = 0; y < rows; y++) {
                for (int x = 0; x < cols; x++) {
                    System.out.print(tileData[y][x] + " ");
                }
                System.out.println();
            }
        }
    }


    /*
    The primary goal of this method is to reconstruct the shortest path found by the Breadth-First Search (BFS) algorithm
    from the start point to the end point. During BFS, each visited tile's parent is recorded, allowing us to trace back
    the path once the end point is reached. This reconstructed path is then used to guide enemy movement.
     */
    private void reconstructPath(GridPoint[][] parent) {
        // clear existing path points
        pathPoints.clear();

        // start from the end and work backward
        GridPoint current = endPoint;

        // temporary list to store reversed path
        ArrayList<GridPoint> reversedPath = new ArrayList<>();
        reversedPath.add(current);

        // follow parent pointers back to start
        while (!current.equals(startPoint)) {
            current = parent[current.getY()][current.getX()];
            if (current == null) break;
            reversedPath.add(current);
        }

        // reverse the path to get start-to-end order
        for (int i = reversedPath.size() - 1; i >= 0; i--) {
            pathPoints.add(reversedPath.get(i));
        }
    }

    public void update(float speedMultiplier){
        // Create a copy of the enemies list to avoid concurrent modification
        ArrayList<Enemy> enemiesCopy = new ArrayList<>(enemies);

        // First pass: Update combat synergy
        updateCombatSynergy();

        for (Enemy enemy : enemiesCopy) {
            if (enemy.isAlive()) {
                // adjust animation speed when game speed changes
                enemy.adjustAnimationForGameSpeed(speedMultiplier);
                // call enemy's own update method to handle internal logic like timers
                enemy.update();
            }
        }

        if (!pathFound || pathPoints.isEmpty()) return;

        ArrayList<Enemy> enemiesToRemove = new ArrayList<>();

        for (Enemy e : enemiesCopy) {
            if (!e.isAlive()) {
                playing.getPlayerManager().addGold(e.getGoldReward());
                playing.updateUIResources();
                System.out.println("Enemy " + e.getId() + " killed. + " + e.getGoldReward() + " gold!");
                // 50% chance to spawn a gold bag
                if (Math.random() < 0.5) {
                    float bagX = e.getSpriteCenterX();
                    float bagY = e.getSpriteCenterY();
                    playing.getGoldBagManager().spawnGoldBag(bagX, bagY, 2, 30);
                }
                enemiesToRemove.add(e);
                continue;
            }

            if (e.hasReachedEnd()) {
                playing.enemyReachedEnd(e);
                enemiesToRemove.add(e);
                continue;
            }

            moveEnemy(e, speedMultiplier);
        }

        // remove dead enemies
        enemies.removeAll(enemiesToRemove);
    }

    public void addEnemy(int enemyType){
        if (!pathFound || pathPoints.isEmpty()) {
            System.out.println("Cannot add enemy: Path not found or empty");
            return;
        }

        GridPoint spawnPoint = pathPoints.get(0);

        if (!isValidSpawnPoint(spawnPoint)) {
            for (int i = 0; i < pathPoints.size(); i++) {
                if (isValidSpawnPoint(pathPoints.get(i))) {
                    spawnPoint = pathPoints.get(i);
                    break;
                }
            }
        }


        int x = spawnPoint.getX() * tileSize + tileSize / 2;
        int y = spawnPoint.getY() * tileSize + tileSize / 2;

        System.out.println("Adding enemy at position: " + x + "," + y);

        Enemy enemy = null;

        switch(enemyType){
            case GOBLIN:
                System.out.println("Adding Goblin");
                enemy = new Goblin(x, y, nextEnemyID++);
                break;
            case WARRIOR:
                System.out.println("Adding Warrior");
                enemy = new Warrior(x, y, nextEnemyID++);
                break;
            case TNT:
                System.out.println("Adding TNT");
                enemy = new TNT(x, y, nextEnemyID++);
                break;
            case BARREL:
                System.out.println("Adding Barrel");
                enemy = new Barrel(x, y, nextEnemyID++);
                break;
            case TROLL:
                System.out.println("Adding Troll");
                enemy = new Troll(x, y, nextEnemyID++);
                break;
            default:
                System.out.println("Unknown enemy type: " + enemyType);
                return;
        }

        if (enemy != null) {
            applyOptionsToEnemy(enemy);
            enemies.add(enemy);
            long spawnTime = System.currentTimeMillis();
            enemySpawnTimes.put(enemy, spawnTime);

        }
    }

    private boolean isValidSpawnPoint(GridPoint point) {
        int[][] overlay = playing.getOverlay();
        return point != null &&
                point != endPoint &&
                !isGateTile(point.getX(), point.getY(), overlay);
    }

    /**
     * Applies the current game options to an enemy
     * @param enemy The enemy to update with options
     */
    private void applyOptionsToEnemy(Enemy enemy) {
        try {
            if (enemy == null) {
                System.out.println("Warning: Cannot apply options to null enemy");
                return;
            }

            if (gameOptions == null) {
                System.out.println("Warning: GameOptions is null, using default stats for enemy ID " + enemy.getId());
                return;
            }

            enemy.updateStatsFromOptions(gameOptions);
        } catch (Exception e) {
            System.out.println("Error applying options to enemy: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Reloads options from the options file and applies them to all enemies
     */
    public void reloadFromOptions() {
        try {
            this.gameOptions = OptionsIO.load();

            // Apply new options to all existing enemies
            for (Enemy enemy : enemies) {
                if (enemy != null && enemy.isAlive()) {
                    applyOptionsToEnemy(enemy);
                }
            }
        } catch (Exception e) {
            System.out.println("Error reloading options: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void moveEnemy(Enemy e, float speedMultiplier) {
        int pathIndex = e.getCurrentPathIndex();

        // if enemy has reached the last path point, it has reached the end
        if (pathIndex >= pathPoints.size() - 1) {
            e.setReachedEnd(true);
            return;
        }

        // get current path point and next path point
        GridPoint currentPoint = pathPoints.get(pathIndex);
        GridPoint nextPoint = pathPoints.get(pathIndex + 1);

        // calculate target position
        int targetX = nextPoint.getX() * tileSize + tileSize / 2;
        int targetY = nextPoint.getY() * tileSize + tileSize / 2;

        // calculate direction to move
        float xDiff = targetX - e.getX();
        float yDiff = targetY - e.getY();
        float distance = (float) Math.sqrt(xDiff * xDiff + yDiff * yDiff);

        // if enemy is very close to the path point, move to next path point
        float sizeOffset = 0;
        switch (e.getSize()) {
            case LARGE: sizeOffset = 32; break; // adjust as needed for troll size
            case MEDIUM: sizeOffset = 16; break;
            case SMALL: sizeOffset = 8; break;
        }
        if (distance < e.getSpeed() * speedMultiplier + sizeOffset) {
            e.setCurrentPathIndex(pathIndex + 1);
            return;
        }

        // calculate movement speed components
        float baseSpeed = e.getSpeed() * speedMultiplier;

        // Apply weather effects to enemy speed
        if (weatherManager != null && weatherManager.isSnowing()) {
            baseSpeed *= weatherManager.getEnemySpeedMultiplier();
        }

        float xSpeed = (xDiff / distance) * baseSpeed;
        float ySpeed = (yDiff / distance) * baseSpeed;

        e.move(xSpeed, ySpeed);
    }

    public void draw(Graphics g, boolean gamePaused) {

        List<Enemy> enemiesCopy = new ArrayList<>(enemies);


        for (Enemy enemy : enemiesCopy) {
            if (enemy.isAlive() && !gamePaused) {
                enemy.updateAnimationTick();
            }
        }


        for (Enemy enemy : enemiesCopy) {
            if (enemy.isAlive()) {
                if (isGoblinInvisible(enemy)) {
                    drawEnemySilhouette(enemy, g);
                } else {
                    drawEnemy(enemy, g);
                }
            }
        }
    }

    // updated method to extract all enemy animation frames
    public static BufferedImage[] extractEnemyFrames() {
        // need space for: 6 goblin + 6 warrior + 6 tnt + 3 barrel + 10 troll = 31 frames
        BufferedImage[] enemyFrames = new BufferedImage[31];

        BufferedImage goblinSheet = LoadSave.getEnemyAtlas("goblin");
        BufferedImage warriorSheet = LoadSave.getEnemyAtlas("warrior");
        BufferedImage barrelSheet = LoadSave.getEnemyAtlas("barrel");
        BufferedImage tntSheet = LoadSave.getEnemyAtlas("tnt");
        BufferedImage trollSheet = LoadSave.getEnemyAtlas("troll");

        // Extract goblin frames (6 frames)
        for (int i = 0; i < 6; i++) {
            BufferedImage goblinFrame = goblinSheet.getSubimage(i * 192, 0, 192, 192);
            enemyFrames[i] = goblinFrame.getSubimage(20, 57, 115, 75);
        }

        // Extract warrior frames (6 frames)
        for (int i = 0; i < 6; i++) {
            BufferedImage warriorFrame = warriorSheet.getSubimage(i * 192, 0, 192, 192);
            enemyFrames[6 + i] = warriorFrame.getSubimage(50, 40, 120, 100);
        }

        // Extract TNT frames (6 frames)
        for (int i = 0; i < 6; i++) {
            // TNT: 1344x192 sized walk animation asset with 6 respective sprites
            BufferedImage tntFrame = tntSheet.getSubimage(i * 192, 0, 192, 192);
            enemyFrames[12 + i] = tntFrame.getSubimage(60, 60, 100, 72);
        }

        // Extract barrel frames (3 frames)
        for (int i = 0; i < 3; i++) {
            BufferedImage barrelFrame = barrelSheet.getSubimage(i * 128, 0, 128, 128);
            enemyFrames[18 + i] = barrelFrame.getSubimage(24, 24,80, 80);
        }

        // Extract troll frames (10 frames)
        for (int i = 0; i < 10; i++) {
            BufferedImage trollFrame = trollSheet.getSubimage(i * 401, 0, 401, 268);
            enemyFrames[21 + i] = trollFrame.getSubimage(0, 0, 401, 268);
        }

        return enemyFrames;
    }

    private void drawEnemy(Enemy enemy, Graphics g){
        // Calculate base index based on enemy type and get animation frame
        int baseIndex;

        // Map enemy types to their frame positions in the array
        switch (enemy.getEnemyType()) {
            case GOBLIN:
                baseIndex = 0;
                break;
            case WARRIOR:
                baseIndex = 6;
                break;
            case TNT:
                baseIndex = 12;
                break;
            case BARREL:
                baseIndex = 18;
                break;
            case TROLL:
                baseIndex = 21;
                break;
            default:
                baseIndex = 0;
                break;
        }

        int frame = baseIndex + enemy.getAnimationIndex();

        // ensure frame is within bounds and handle special case for barrel (only 3 frames)
        if (enemy.getEnemyType() == BARREL && enemy.getAnimationIndex() >= 3) {
            frame = baseIndex + (enemy.getAnimationIndex() % 3);
        } else if (frame >= enemyImages.length) {
            frame = baseIndex;
        }

        BufferedImage sprite = enemyImages[frame];

        // adjust drawing based on enemy size
        int drawX, drawY;
        int drawWidth, drawHeight;
        Enemy.Size size = enemy.getSize();

        // calculate position and scale based on enemy size
        switch (size) {
            case SMALL:
                // small enemies (goblin, tnt) - 60% of original size
                drawWidth = (int)(sprite.getWidth() * 0.6);
                drawHeight = (int)(sprite.getHeight() * 0.6);
                drawX = (int)(enemy.getX() - drawWidth/2 - 10);
                drawY = (int)(enemy.getY() - drawHeight/2 - 10);
                break;
            case MEDIUM:
                // medium enemies (warrior, barrel) - 80% of original size
                drawWidth = (int)(sprite.getWidth() * 0.8);
                drawHeight = (int)(sprite.getHeight() * 0.8);
                if (enemy.getEnemyType() == BARREL) {
                    drawX = (int)(enemy.getX() - drawWidth/2 - 15);
                    drawY = (int)(enemy.getY() - drawHeight/2 - 15);
                } else {
                    drawX = (int)(enemy.getX() - drawWidth/2 - 25);
                    drawY = (int)(enemy.getY() - drawHeight/2 - 25);
                }
                break;
            case LARGE:
                // large enemies (troll) - larger size with adjusted position
                if (enemy.getEnemyType() == TROLL) {
                    drawWidth = (int)(sprite.getWidth() * 0.4);
                    drawHeight = (int)(sprite.getHeight() * 0.4);
                    drawX = (int)(enemy.getX() - drawWidth/2 - 40);
                    drawY = (int)(enemy.getY() - drawHeight/2 - 40);
                } else {
                    drawWidth = (int)(sprite.getWidth() * 0.5);
                    drawHeight = (int)(sprite.getHeight() * 0.5);
                    drawX = (int)(enemy.getX() - drawWidth/2 - 50);
                    drawY = (int)(enemy.getY() - drawHeight/2 - 50);
                }
                break;
            default:
                // default 100% size
                drawWidth = sprite.getWidth();
                drawHeight = sprite.getHeight();
                drawX = (int)(enemy.getX() - drawWidth/2);
                drawY = (int)(enemy.getY() - drawHeight/2);
                break;
        }

        boolean facingLeft = enemy.getDirX() < 0;

        Graphics2D g2d = (Graphics2D) g;
        AffineTransform oldTransform = g2d.getTransform();

        if (enemy.isTeleporting()) {
            // Set a blue/cyan glow with pulse effect based on time
            long currentTime = System.nanoTime();
            float progress = 1.0f - ((float)(currentTime - enemy.getTeleportEffectTimer()) / enemy.TELEPORT_EFFECT_DURATION);
            float alpha = Math.max(0.1f, progress); // Fade out over time

            // Draw a pulsing blue glow
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha * 0.7f));
            g2d.setColor(new Color(0, 200, 255)); // Bright blue
            g2d.fillOval(
                    drawX - 10,
                    drawY - 10,
                    drawWidth + 20,
                    drawHeight + 20
            );

            // Draw some "sparkle" effects
            g2d.setColor(new Color(255, 255, 255, (int)(255 * alpha)));
            float pulseSize = 5.0f + (float)(Math.sin(currentTime * 0.00000002) * 3.0);
            int sparkleSize = (int)pulseSize;

            // Draw 5 random sparkles
            for (int i = 0; i < 5; i++) {
                double angle = Math.random() * Math.PI * 2;
                int offsetX = (int)(Math.cos(angle) * drawWidth/2);
                int offsetY = (int)(Math.sin(angle) * drawHeight/2);
                g2d.fillRect(
                        drawX + drawWidth/2 + offsetX - sparkleSize/2,
                        drawY + drawHeight/2 + offsetY - sparkleSize/2,
                        sparkleSize, sparkleSize
                );
            }

            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }

        if (facingLeft) {
            // Create a transform for flipping horizontally
            AffineTransform tx = new AffineTransform();
            tx.translate(drawX + drawWidth, drawY);
            tx.scale(-1, 1);
            tx.translate(-drawWidth, 0);
            g2d.setTransform(tx);

            g2d.drawImage(sprite, 0, 0, drawWidth, drawHeight, null);
            g2d.setTransform(oldTransform);
        } else {
            // Normal drawing for facing right
            g2d.drawImage(sprite, drawX, drawY, drawWidth, drawHeight, null);
        }

        // Health bar and effects should be drawn with the original transform
        drawHealthBar(g, enemy, drawX, drawY, drawWidth, drawHeight);
    }

    private void drawHealthBar(Graphics g, Enemy enemy, int x, int y, int width, int height) {
        int healthBarWidth = 40;
        int healthBarHeight = 4;
        int healthBarX = x + (width - healthBarWidth) / 2;

        // different enemy types get bars in different positions
        boolean drawAbove = enemy.getEnemyType() % 2 == 1; // even types below, odd types above

        int healthBarY;
        if (drawAbove) {
            healthBarY = y - 8;
        } else {
            healthBarY = y + height + 2;
        }

        // draw white rounded contour frame
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(1.5f));
        RoundRectangle2D roundedRect = new RoundRectangle2D.Float(
                healthBarX - 1, healthBarY - 1,
                healthBarWidth + 2, healthBarHeight + 2,
                4, 4);
        g2d.draw(roundedRect);

        // draw black background of health bar
        g.setColor(Color.BLACK);
        g.fillRect(healthBarX, healthBarY, healthBarWidth, healthBarHeight);

        // draw red health indicator
        g.setColor(new Color(255, 0, 0));
        int currentHealthWidth = (int) (healthBarWidth * enemy.getHealthBarPercentage());
        g.fillRect(healthBarX, healthBarY, currentHealthWidth, healthBarHeight);

        // Draw status effect icons
        int iconX = healthBarX - 14;
        int iconY = healthBarY - 4;
        
        // Draw slow effect icon if active
        if (enemy.isSlowed()) {
            if (Enemy.snowflakeIcon == null) {
                Enemy.snowflakeIcon = LoadSave.getImageFromPath("/TowerAssets/snow flake icon.png");
            }
            if (Enemy.snowflakeIcon != null) {
                g.drawImage(Enemy.snowflakeIcon, iconX, iconY, 12, 12, null);
                iconX -= 14; // Move left for next icon
            }
        }
        
        // Draw combat synergy icon if active
        if (enemy.hasCombatSynergy()) {
            if (Enemy.thunderIcon == null) {
                Enemy.thunderIcon = LoadSave.getImageFromPath("/TowerAssets/thunder_icon.png");
            }
            if (Enemy.thunderIcon != null) {
                g.drawImage(Enemy.thunderIcon, iconX, iconY, 12, 12, null);
            }
        }
    }
    public void spawnEnemy(int nextEnemy) {
        addEnemy(nextEnemy);
    }

    public ArrayList<Enemy> getEnemies() {
        return enemies;
    }

    public void clearEnemies() {
        enemies.clear();
    }

    public void addEnemy(Enemy enemy) {
        enemies.add(enemy);
        long spawnTime = System.currentTimeMillis();
        enemySpawnTimes.put(enemy, spawnTime);

    }

    /**
     * Teleports an enemy back to the starting point of the path.
     * The enemy retains its current HP and status effects.
     *
     * @param enemy The enemy to teleport
     * @return true if teleportation was successful, false otherwise
     */
    public boolean teleportEnemyToStart(Enemy enemy) {
        if (!pathFound || pathPoints.isEmpty()) {
            System.out.println("Cannot teleport enemy: Path not found or empty");
            return false;
        }

        GridPoint startingPoint = pathPoints.get(0);
        if (startingPoint == null) {
            System.out.println("Cannot teleport enemy: Invalid starting point");
            return false;
        }
        float newX = startingPoint.getX() * tileSize + tileSize / 2;
        float newY = startingPoint.getY() * tileSize + tileSize / 2;

        enemy.setX(newX);
        enemy.setY(newY);

        enemy.setCurrentPathIndex(0);
        enemy.applyTeleportEffect();

        System.out.println("Enemy " + enemy.getId() + " teleported back to start!");
        return true;
    }

    public int getNextEnemyID() {
        return nextEnemyID++;
    }

    public boolean canTargetEnemy(Enemy enemy) {
        return isEnemyTargetable(enemy);
    }

    public void render(Graphics g) {
        for (Enemy e : enemies) {
            if (e.isAlive()) {
                if (isGoblinInvisible(e)) {
                    drawEnemySilhouette(e, g);
                } else {
                    drawEnemy(e, g);
                }
            }
        }
    }

    private boolean wasNight = false;

    private boolean isGoblinInvisible(Enemy enemy) {
        if (weatherManager == null) {

            return false;
        }

        boolean isNight = weatherManager.isNight();

        if (isNight && !wasNight) {

            long currentTime = System.currentTimeMillis();
            for (Enemy e : enemies) {
                if (e.getEnemyType() == 0) { // GOBLIN
                    enemySpawnTimes.put(e, currentTime);

                }
            }
        }
        wasNight = isNight;

        if (isNight && enemy.getEnemyType() == 0) { // GOBLIN = 0
            Long spawnTime = enemySpawnTimes.get(enemy);

            if (spawnTime == null) {
                long currentTime = System.currentTimeMillis();
                enemySpawnTimes.put(enemy, currentTime);

                return true;
            }

            long currentTime = System.currentTimeMillis();
            boolean isInvisible = (currentTime - spawnTime) < 10000; // İlk 10 saniye görünmez

            if (isInvisible) {

            }

            return isInvisible;
        }

        return false;
    }

    private boolean isEnemyTargetable(Enemy enemy) {
        return !isGoblinInvisible(enemy);
    }

    private void drawEnemySilhouette(Enemy enemy, Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        AlphaComposite originalComposite = (AlphaComposite) g2d.getComposite();

        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
        g2d.setColor(new Color(0, 0, 0, 150));

        int drawX = (int)(enemy.getX() - 25);
        int drawY = (int)(enemy.getY() - 30);
        int drawWidth = 50;
        int drawHeight = 60;

        g2d.fillOval(drawX + 10, drawY + 20, drawWidth - 20, drawHeight - 25);

        g2d.fillOval(drawX + 15, drawY + 5, drawWidth - 30, 25);

        g2d.fillOval(drawX + 8, drawY + 8, 8, 12);
        g2d.fillOval(drawX + drawWidth - 16, drawY + 8, 8, 12);

        g2d.setColor(new Color(255, 0, 0, 100)); // Kırmızı parıltı
        g2d.fillOval(drawX + 18, drawY + 12, 3, 3);
        g2d.fillOval(drawX + 28, drawY + 12, 3, 3);

        g2d.setComposite(originalComposite);
    }

    private void drawEnemies(Graphics g) {
        for (Enemy e : enemies) {
            if (e.isAlive()) {
                drawEnemy(e, g);
            }
        }
    }

    private void updateCombatSynergy() {
        // Get all knights and goblins
        ArrayList<Enemy> knights = new ArrayList<>();
        ArrayList<Enemy> goblins = new ArrayList<>();
        
        for (Enemy enemy : enemies) {
            if (enemy.isAlive()) {
                if (enemy.getEnemyType() == Constants.Enemies.WARRIOR) {
                    knights.add(enemy);
                } else if (enemy.getEnemyType() == Constants.Enemies.GOBLIN) {
                    goblins.add(enemy);
                }
            }
        }

        // Check each knight's distance to goblins
        for (Enemy knight : knights) {
            boolean hasNearbyGoblin = false;
            float closestGoblinSpeed = 0;
            
            for (Enemy goblin : goblins) {
                float distance = calculateDistance(knight, goblin);
                if (distance < 64) { // One tile width
                    hasNearbyGoblin = true;
                    closestGoblinSpeed = goblin.getSpeed();
                    break;
                }
            }
            
            if (hasNearbyGoblin) {
                knight.applyCombatSynergy(closestGoblinSpeed);
            } else {
                knight.removeCombatSynergy();
            }
        }
    }

    private float calculateDistance(Enemy e1, Enemy e2) {
        float xDiff = e1.getSpriteCenterX() - e2.getSpriteCenterX();
        float yDiff = e1.getSpriteCenterY() - e2.getSpriteCenterY();
        return (float) Math.sqrt(xDiff * xDiff + yDiff * yDiff);
    }
}

