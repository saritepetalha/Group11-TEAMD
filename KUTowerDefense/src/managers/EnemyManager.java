package managers;

import constants.GameDimensions;
import enemies.Enemy;
import enemies.*;
import scenes.Playing;
import helpMethods.LoadSave;
import objects.Point;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Queue;
import java.util.LinkedList;

import static constants.Constants.PathPoints.*;
import static constants.Constants.Tiles.*;
import static constants.Constants.Enemies.*;

public class EnemyManager {
    private Playing playing;
    private BufferedImage[] enemyImages;
    //private Enemy enemyTest;
    private ArrayList<Enemy> enemies = new ArrayList<>();
    private ArrayList<Point> pathPoints = new ArrayList<>();
    private Point startPoint, endPoint;
    private int tileSize = GameDimensions.TILE_DISPLAY_SIZE;
    private int nextEnemyID = 0;
    private boolean pathFound = false;

    public EnemyManager(Playing playing, int[][] overlayData, int [][] tileData) {
        this.playing = playing;
        enemyImages = extractEnemyFrames();

        findStartAndEndPoints(overlayData);

        // generate path only if start and end were found
        if (startPoint != null && endPoint != null) {
            generatePath(tileData);
        }
        addEnemy(GOBLIN);
        addEnemy(WARRIOR);
        //enemyTest = new Enemy(64*3, 64*3, 0, 0);
    }

    private void findStartAndEndPoints(int[][] overlayData) {
        for (int y = 0; y < overlayData.length; y++) {
            for (int x = 0; x < overlayData[y].length; x++) {
                if (overlayData[y][x] == START_POINT) {
                    startPoint = new Point(x, y);
                }
                else if (overlayData[y][x] == END_POINT) {
                    endPoint = new Point(x, y);
                }
            }
        }
    }

    private boolean isValidPosition(int x, int y, int rows, int cols) {
        return x >= 0 && x < cols && y >= 0 && y < rows;
    }


    private void generatePath(int[][] tileData) {
        // implementation of Breadth-First Search to find path from start to end
        if (startPoint == null || endPoint == null) return;

        int rows = tileData.length;
        int cols = tileData[0].length;

        // direction arrays for 4-directional movement
        int[] dx = {-1, 0, 1, 0}; // left, up, right, down
        int[] dy = {0, -1, 0, 1};

        // initialize visited array and parent map for path reconstruction
        boolean[][] visited = new boolean[rows][cols];
        Point[][] parent = new Point[rows][cols];

        // BFS queue
        Queue<Point> queue = new LinkedList<>();
        queue.add(startPoint);
        visited[startPoint.getY()][startPoint.getX()] = true;

        boolean foundEnd = false;

        // BFS to find path
        while (!queue.isEmpty() && !foundEnd) {
            Point current = queue.poll();

            // check if we reached the end
            if (current.equals(endPoint)) {
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

                    Point next = new Point(newX, newY);
                    queue.add(next);
                    visited[newY][newX] = true;
                    parent[newY][newX] = current;
                }
            }
        }

        // if end found, reconstruct the path
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
    private void reconstructPath(Point[][] parent) {
        // clear existing path points
        pathPoints.clear();

        // start from the end and work backward
        Point current = endPoint;

        // temporary list to store reversed path
        ArrayList<Point> reversedPath = new ArrayList<>();
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

    public void update(){
        for (Enemy enemy:enemies){
            enemy.move(0.3f, 0);
        }

        if (!pathFound || pathPoints.isEmpty()) return;

        ArrayList<Enemy> enemiesToRemove = new ArrayList<>();

        for (Enemy e : enemies) {
            if (!e.isAlive()) {
                enemiesToRemove.add(e);
                continue;
            }

            if (e.hasReachedEnd()) {
                //playing.enemyReachedEnd(); will be implemented soon
                enemiesToRemove.add(e);
                continue;
            }

            moveEnemy(e);
        }

        // remove dead enemies
        enemies.removeAll(enemiesToRemove);
        //enemyTest.move(0.3f,0);
    }



    public void addEnemy(int enemyType){
        if (!pathFound || pathPoints.isEmpty()) return;

        Point firstPoint = pathPoints.get(0);

        // calculate starting position (center of the start tile)
        int x = firstPoint.getX() * tileSize + tileSize / 2;;
        int y = firstPoint.getY() * tileSize + tileSize / 2;;

        Enemy enemy = null;
        switch(enemyType){
            case GOBLIN:
                enemies.add(new Goblin(x,y, nextEnemyID++));
                break;
            case WARRIOR:
                enemies.add(new Warrior(x,y,nextEnemyID++));
                break;
        }

        if (enemy != null) {
            enemies.add(enemy);
        }
    }

    private void moveEnemy(Enemy e) {
        int pathIndex = e.getCurrentPathIndex();

        // if enemy has reached the last path point, it has reached the end
        if (pathIndex >= pathPoints.size() - 1) {
            e.setReachedEnd(true);
            return;
        }

        // get current path point and next path point
        Point currentPoint = pathPoints.get(pathIndex);
        Point nextPoint = pathPoints.get(pathIndex + 1);

        // calculate target position
        int targetX = nextPoint.getX() * tileSize + tileSize / 2;
        int targetY = nextPoint.getY() * tileSize + tileSize / 2;

        // calculate direction to move
        float xDiff = targetX - e.getX();
        float yDiff = targetY - e.getY();
        float distance = (float) Math.sqrt(xDiff * xDiff + yDiff * yDiff);

        // if enemy is very close to the path point, move to next path point
        if (distance < e.getSpeed()) {
            e.setCurrentPathIndex(pathIndex + 1);
            return;
        }

        // calculate movement speed components
        float xSpeed = (xDiff / distance) * e.getSpeed();
        float ySpeed = (yDiff / distance) * e.getSpeed();

        e.move(xSpeed, ySpeed);
    }


    public void draw(Graphics g){
        for (Enemy enemy: enemies){
            enemy.updateAnimationTick();
            drawEnemy(enemy, g);
        }
    }

    // method to extract all enemy animation frames (6 goblin + 6 warrior)
    // returns an array: 0-5 goblin animation, 6-11 warrior animation
    public static BufferedImage[] extractEnemyFrames() {
        BufferedImage[] enemyFrames = new BufferedImage[12];

        // load both sprite atlases
        BufferedImage goblinSheet = LoadSave.getEnemyAtlas("goblin");
        BufferedImage warriorSheet = LoadSave.getEnemyAtlas("warrior");

        // each sprite sheet has 6 frames, each frame is 192x192
        for (int i = 0; i < 6; i++) {
            // goblin frames
            BufferedImage goblinFrame = goblinSheet.getSubimage(i * 192, 0, 192, 192);
            enemyFrames[i] = goblinFrame.getSubimage(30, 40, 120, 100); // center 64x64

            // warrior frames
            BufferedImage warriorFrame = warriorSheet.getSubimage(i * 192, 0, 192, 192);
            enemyFrames[6 + i] = warriorFrame.getSubimage(30, 40,120, 100); // center 64x64
        }

        return enemyFrames;
    }

    private void drawEnemy(Enemy enemy, Graphics g){
        int baseIndex = enemy.getEnemyType() * 6; // Goblin=0, Warrior=1 → 0 or 6
        int frame = baseIndex + enemy.getAnimationIndex();

        BufferedImage sprite = enemyImages[frame];

        // ALIGNMENT LOGIC MUST BE CHANGED TO A CONSISTENT ONE
        int drawX = (int) (enemy.getX() - (float) sprite.getWidth() / 2);
        int drawY = (int) (enemy.getY() - (float) sprite.getHeight() + tileSize/2);

        g.drawImage(sprite, drawX, drawY, null);
    }
}
