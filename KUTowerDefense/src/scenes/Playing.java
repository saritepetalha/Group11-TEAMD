package scenes;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import dimensions.GameDimensions;
import helpMethods.LevelBuilder;
import helpMethods.LoadSave;
import main.Game;
import managers.TileManager;
import managers.TowerManager;
import objects.Tower;
import ui_p.EditTiles;
import ui_p.TheButton;

import static main.GameStates.*;
import objects.Tile;

public class Playing extends GameScene implements SceneMethods {

    private int[][] level;

    private int mouseX, mouseY;

    private Tile selectedTile;

    private TowerManager towerManager;

    public Playing(Game game) {
        super(game);
        level = LevelBuilder.getLevelData();
        loadDefaultLevel();
        towerManager = new TowerManager(this);
    }


    public void saveLevel(String filename) {
        LoadSave.saveLevel(filename,level);
    }


    private void loadDefaultLevel() {
        int[][] lvl = LoadSave.getLevelData("defaultleveltest1");
        //THIS LINE IS JUST TO SEE WHETHER THE BACKEND OF THE getLevelData function works or not
        //IT WORKS!!!
        System.out.println(java.util.Arrays.deepToString(lvl));
    }

    public void loadLevel(String levelName) {
        level = LoadSave.getLevelData(levelName);
    }

    public void update() {
        towerManager.update();
    }


    public void constructTower(){

    }


    @Override
    public void render(Graphics g) {

        g.setColor(new Color(134,177,63,255));
        g.fillRect(0, 0, GameDimensions.GAME_WIDTH, GameDimensions.GAME_HEIGHT);

        towerManager.draw(g);
    }

    @Override
    public void mouseClicked(int x, int y) {


    }

    @Override
    public void mouseMoved(int x, int y) {}

    @Override
    public void mousePressed(int x, int y) {}

    @Override
    public void mouseReleased(int x, int y) {}

    @Override
    public void mouseDragged(int x, int y) {

    }
}
