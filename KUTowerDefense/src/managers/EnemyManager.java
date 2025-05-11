package managers;

import constants.GameDimensions;
import enemies.Enemy;
import enemies.*;
import scenes.Playing;
import helpMethods.LoadSave;
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

    public EnemyManager(Playing playing, int[][] overlayData, int[][] tileData) {
        this.playing = playing;
        enemyImages = extractEnemyFrames();
        enemies = new ArrayList<>();
        pathPoints = new ArrayList<>();
        pathFound = false;
        startPoint = null;
        endPoint = null;
        findStartAndEndPoints(overlayData);
        if (startPoint != null && endPoint != null) {
            generatePath(tileData);
        }
        System.out.println("Path found? " + pathFound);
        System.out.println("Start: " + startPoint + ", End: " + endPoint);
        System.out.println("Path length: " + pathPoints.size());
    }

    private void findStartAndEndPoints(int[][] overlayData) {
        for (int y = 0; y < overlayData.length; y++) {
            for (int x = 0; x < overlayData[y].length; x++) {
                if (overlayData[y][x] == 1) {
                    startPoint = new GridPoint(x, y);
                }
                else if (overlayData[y][x] == 2) {
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
        if (startPoint == null || endPoint == null) {
            return;
        }

        int rows = tileData.length;
        int cols = tileData[0].length;

        int[] dx = {-1, 0, 1, 0};
        int[] dy = {0, -1, 0, 1};

        boolean[][] visited = new boolean[rows][cols];
        GridPoint[][] parent = new GridPoint[rows][cols];

        Queue<GridPoint> queue = new LinkedList<>();
        queue.add(startPoint);
        visited[startPoint.getY()][startPoint.getX()] = true;

        boolean foundEnd = false;

        while (!queue.isEmpty() && !foundEnd) {
            GridPoint current = queue.poll();

            if (current.equals(endPoint)) {
                foundEnd = true;
                break;
            }

            for (int i = 0; i < 4; i++) {
                int newX = current.getX() + dx[i];
                int newY = current.getY() + dy[i];

                if (isValidPosition(newX, newY, rows, cols) &&
                        isRoadTile(tileData[newY][newX]) &&
                        !visited[newY][newX]) {
                    GridPoint next = new GridPoint(newX, newY);
                    queue.add(next);
                    visited[newY][newX] = true;
                    parent[newY][newX] = current;
                }
            }
        }

        if (foundEnd) {
            reconstructPath(parent);
            pathFound = true;
        }
    }

    /*
    The primary goal of this method is to reconstruct the shortest path found by the Breadth-First Search (BFS) algorithm
    from the start point to the end point. During BFS, each visited tile's parent is recorded, allowing us to trace back
    the path once the end point is reached. This reconstructed path is then used to guide enemy movement.
     */
    private void reconstructPath(GridPoint[][] parent) {
        pathPoints.clear();
        GridPoint current = endPoint;
        ArrayList<GridPoint> reversedPath = new ArrayList<>();
        reversedPath.add(current);

        while (!current.equals(startPoint)) {
            current = parent[current.getY()][current.getX()];
            if (current == null) break;
            reversedPath.add(current);
        }

        for (int i = reversedPath.size() - 1; i >= 0; i--) {
            pathPoints.add(reversedPath.get(i));
        }
    }

    public void update(float speedMultiplier){
        for (Enemy enemy : enemies) {
            if (enemy.isAlive()) {
                // adjust animation speed when game speed changes
                enemy.adjustAnimationForGameSpeed(speedMultiplier);
            }
        }

        if (!pathFound || pathPoints.isEmpty()) return;

        ArrayList<Enemy> enemiesToRemove = new ArrayList<>();

        for (Enemy e : enemies) {
            if (!e.isAlive()) {
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
            System.out.println("Enemy added!");
            return;
        }

        GridPoint firstPoint = pathPoints.get(0);

        // calculate starting position (center of the start tile)
        int x = firstPoint.getX() * tileSize + tileSize / 2;
        int y = firstPoint.getY() * tileSize + tileSize / 2;

        try {
            switch(enemyType){
                case GOBLIN:
                    enemies.add(new Goblin(x, y, nextEnemyID++));
                    break;
                case WARRIOR:
                    enemies.add(new Warrior(x, y, nextEnemyID++));
                    break;
                case TNT:
                    enemies.add(new TNT(x, y, nextEnemyID++));
                    break;
                case BARREL:
                    enemies.add(new Barrel(x, y, nextEnemyID++));
                    break;
                case TROLL:
                    enemies.add(new Troll(x, y, nextEnemyID++));
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void moveEnemy(Enemy e, float speedMultiplier) {
        int pathIndex = e.getCurrentPathIndex();

        if (pathIndex >= pathPoints.size() - 1) {
            e.setReachedEnd(true);
            return;
        }

        GridPoint currentPoint = pathPoints.get(pathIndex);
        GridPoint nextPoint = pathPoints.get(pathIndex + 1);

        int targetX = nextPoint.getX() * tileSize + tileSize / 2;
        int targetY = nextPoint.getY() * tileSize + tileSize / 2;

        float xDiff = targetX - e.getX();
        float yDiff = targetY - e.getY();
        float distance = (float) Math.sqrt(xDiff * xDiff + yDiff * yDiff);

        if (distance < e.getSpeed() * speedMultiplier) {
            e.setCurrentPathIndex(pathIndex + 1);
            return;
        }

        float xSpeed = (xDiff / distance) * e.getSpeed() * speedMultiplier;
        float ySpeed = (yDiff / distance) * e.getSpeed() * speedMultiplier;

        e.move(xSpeed, ySpeed);
    }

    public void draw(Graphics g){
        for (Enemy enemy: enemies){
            if (enemy.isAlive()) {
                // Update animation in normal speed
                enemy.updateAnimationTick();
                drawEnemy(enemy, g);
            }
        }
    }

    public void draw(Graphics g, boolean gamePaused){
        for (Enemy enemy: enemies){
            if (enemy.isAlive()) {
                // only update animation if game is not paused
                if (!gamePaused) {
                    enemy.updateAnimationTick();
                }
                drawEnemy(enemy, g);
                System.out.println("Drawing " + enemies.size() + " enemies");
            }
        }
        System.out.println("DRAWING ENEMIES: " + enemies.size());
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
            enemyFrames[i] = goblinFrame.getSubimage(20, 50, 120, 92);
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
            enemyFrames[18 + i] = barrelFrame.getSubimage(15, 15,100, 100);
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
