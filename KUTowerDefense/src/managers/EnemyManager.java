package managers;

import constants.GameDimensions;
import enemies.Enemy;
import enemies.*;
import scenes.MapEditing;
import scenes.Playing;
import helpMethods.LoadSave;
import constants.Constants;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import static constants.Constants.PathPoints.*;

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
        //addEnemy(64*3, 64*3, Constants.Enemies.GOBLIN);
        //addEnemy(64*3, 64*3, Constants.Enemies.WARRIOR);
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
    }

    private void reconstructPath(Point[][] parent) {
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
        int x = (int) (firstPoint.getX() * tileSize);
        int y = (int) (firstPoint.getY() * tileSize);

        Enemy enemy = null;
        switch(enemyType){
            case Constants.Enemies.GOBLIN:
                enemies.add(new Goblin(x,y, nextEnemyID++));
                break;
            case Constants.Enemies.WARRIOR:
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
        int targetX = (int) (nextPoint.getX() * tileSize);
        int targetY = (int) (nextPoint.getY() * tileSize);

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
            drawEnemy(enemy, g);
        }
        //drawEnemy(enemyTest, g);
    }

    // method to extract all enemy animation frames (6 goblin + 6 warrior)
    // returns an array: 0-5 goblin, 6-11 warrior
    public static BufferedImage[] extractEnemyFrames() {
        BufferedImage[] enemyFrames = new BufferedImage[12];

        // load both sprite atlases
        BufferedImage goblinSheet = LoadSave.getEnemyAtlas("goblin");
        BufferedImage warriorSheet = LoadSave.getEnemyAtlas("warrior");

        // each sprite sheet has 6 frames, each frame is 192x192
        for (int i = 0; i < 6; i++) {
            // goblin frames
            BufferedImage goblinFrame = goblinSheet.getSubimage(i * 192, 0, 192, 192);
            enemyFrames[i] = goblinFrame.getSubimage(64, 64, 64, 64); // center 64x64

            // warrior frames
            BufferedImage warriorFrame = warriorSheet.getSubimage(i * 192, 0, 192, 192);
            enemyFrames[6 + i] = warriorFrame.getSubimage(64, 64, 64, 64); // center 64x64
        }

        return enemyFrames;
    }

    private void drawEnemy(Enemy enemy, Graphics g){
        g.drawImage(enemyImages[enemy.getEnemyType()], (int) enemy.getX(), (int) enemy.getY(), null);
    }
}
