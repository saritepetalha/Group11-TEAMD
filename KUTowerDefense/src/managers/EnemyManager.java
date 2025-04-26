package managers;

import enemies.Enemy;
import enemies.*;
import scenes.Playing;
import helpMethods.LoadSave;
import constants.Constants;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class EnemyManager {
    private Playing playing;
    private BufferedImage[] enemyImages;
    //private Enemy enemyTest;
    private ArrayList<Enemy> enemies = new ArrayList<>();

    public EnemyManager(Playing playing) {
        this.playing = playing;
        enemyImages = extractEnemyFrames();
        addEnemy(64*3, 64*3, Constants.Enemies.GOBLIN);
        addEnemy(64*3, 64*3, Constants.Enemies.WARRIOR);
        //enemyTest = new Enemy(64*3, 64*3, 0, 0);
    }

    public void update(){
        for (Enemy enemy:enemies){
            enemy.move(0.3f, 0);
        }
        //enemyTest.move(0.3f,0);
    }

    public void addEnemy(int x, int y, int enemyType){
        switch(enemyType){
            case Constants.Enemies.GOBLIN:
                enemies.add(new Goblin(x,y,0));
                break;
            case Constants.Enemies.WARRIOR:
                enemies.add(new Warrior(x,y,0));
                break;
        }
        enemies.add(new Goblin(x,y,0));
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
