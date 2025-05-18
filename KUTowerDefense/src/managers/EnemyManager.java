package managers;

import config.GameOptions;
import constants.GameDimensions;
import enemies.Enemy;
import enemies.*;
import scenes.Playing;
import helpMethods.LoadSave;
import helpMethods.OptionsIO;
import objects.GridPoint;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Queue;
import java.util.LinkedList;
import java.util.Set;

import static constants.Constants.PathPoints.*;
import static constants.Constants.Enemies.*;

public class EnemyManager {
    private Playing playing;
    private BufferedImage[] enemyImages;
    private ArrayList<Enemy> enemies = new ArrayList<>();
    private ArrayList<GridPoint> pathPoints = new ArrayList<>();
    private GridPoint startPoint, endPoint;
    private int tileSize = GameDimensions.TILE_DISPLAY_SIZE;
    private int nextEnemyID = 0;
    private boolean pathFound = false;
    private static final Set<Integer> ROAD_IDS = Set.of(0,1,2,3,4,6,7,8,9,10,11,12,13,14,31);
    private GameOptions gameOptions;

    public EnemyManager(Playing playing, int[][] overlayData, int [][] tileData, GameOptions options) {
        this.playing = playing;
        this.gameOptions = options;
        if (this.gameOptions == null) {
            System.out.println("Warning: Received null GameOptions in EnemyManager, using defaults.");
            this.gameOptions = GameOptions.defaults();
        }
        enemyImages = extractEnemyFrames();

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
                }
            }
        }
    }

    private boolean isValidPosition(int x, int y, int rows, int cols) {
        return x >= 0 && x < cols && y >= 0 && y < rows;
    }

    private boolean isRoadTile(int tileId) {
        return ROAD_IDS.contains(tileId);
    }

    private void generatePath(int[][] tileData) {
        // implementation of Breadth-First Search to find path from start to end
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
                if (isValidPosition(newX, newY, rows, cols) && isRoadTile(tileData[newY][newX]) &&
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
        
        for (Enemy enemy : enemiesCopy) {
            if (enemy.isAlive()) {
                // adjust animation speed when game speed changes
                enemy.adjustAnimationForGameSpeed(speedMultiplier);
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

        GridPoint firstPoint = pathPoints.get(0);

        // calculate starting position (center of the start tile)
        int x = firstPoint.getX() * tileSize + tileSize / 2;
        int y = firstPoint.getY() * tileSize + tileSize / 2;

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

        // Apply options to the enemy
        if (enemy != null) {
            applyOptionsToEnemy(enemy);
            enemies.add(enemy);
        }
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
        if (distance < e.getSpeed() * speedMultiplier) {
            e.setCurrentPathIndex(pathIndex + 1);
            return;
        }

        // calculate movement speed components
        float xSpeed = (xDiff / distance) * e.getSpeed() * speedMultiplier;
        float ySpeed = (yDiff / distance) * e.getSpeed() * speedMultiplier;

        e.move(xSpeed, ySpeed);
    }

    public void draw(Graphics g){
        // Create a copy of the enemies list to avoid concurrent modification
        ArrayList<Enemy> enemiesCopy = new ArrayList<>(enemies);
        
        for (Enemy enemy: enemiesCopy){
            if (enemy.isAlive()) {
                // Update animation in normal speed
                enemy.updateAnimationTick();
                drawEnemy(enemy, g);
            }
        }
    }

    public void draw(Graphics g, boolean gamePaused){
        // Create a copy of the enemies list to avoid concurrent modification
        ArrayList<Enemy> enemiesCopy = new ArrayList<>(enemies);
        
        for (Enemy enemy: enemiesCopy){
            if (enemy.isAlive()) {
                // only update animation if game is not paused
                if (!gamePaused) {
                    enemy.updateAnimationTick();
                }
                drawEnemy(enemy, g);
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

        // draw the enemy with appropriate size
        g.drawImage(sprite, drawX, drawY, drawWidth, drawHeight, null);
        drawHealthBar(g, enemy, drawX, drawY, drawWidth, drawHeight);
        // Draw snowflake icon if slowed
        if (enemy.isSlowed()) {
            if (Enemy.snowflakeIcon == null) {
                Enemy.snowflakeIcon = helpMethods.LoadSave.getImageFromPath("/TowerAssets/snow flake icon.png");
            }
            if (Enemy.snowflakeIcon != null) {
                int healthBarWidth = 40;
                int healthBarHeight = 4;
                int healthBarX = drawX + (drawWidth - healthBarWidth) / 2;
                boolean drawAbove = enemy.getEnemyType() % 2 == 1;
                int healthBarY = drawAbove ? drawY - 8 : drawY + drawHeight + 2;
                g.drawImage(Enemy.snowflakeIcon, healthBarX - 14, healthBarY - 4, 12, 12, null);
            }
        }
    }

    private void drawHealthBar(Graphics g, Enemy enemy, int x, int y, int width, int height) {
        int healthBarWidth = 40; //enemy.getHealth()/3
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
    }
    public void spawnEnemy(int nextEnemy) {
        addEnemy(nextEnemy);
    }

    public ArrayList<Enemy> getEnemies() {
        return enemies;
    }
}
