package scenes;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import dimensions.GameDimensions;
import helpMethods.LevelBuilder;
import helpMethods.LoadSave;
import main.Game;
import managers.TileManager;
import managers.TowerManager;
import objects.Tower;
import ui_p.DeadTree;
import ui_p.EditTiles;
import ui_p.TheButton;

import static main.GameStates.*;
import objects.Tile;

public class Playing extends GameScene implements SceneMethods {

    private int[][] level = {{0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15},
            {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15},
            {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15},
            {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15},
            {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15},
            {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15},
            {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15},
            {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15},
            {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15}
    };

    private int mouseX, mouseY;

    private Tile selectedTile;
    private List<DeadTree> trees;

    private TowerManager towerManager;
    private TileManager tileManager;

    public Playing(Game game) {
        super(game);
        //level = LevelBuilder.getLevelData();
        //loadDefaultLevel();

        towerManager = new TowerManager(this);
        tileManager = new TileManager();
        //if(towerManager.findDeadTrees(level) != null) {
            trees = towerManager.findDeadTrees(level);
        //}
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
    public void drawTowerButtons(Graphics g) {
        for (DeadTree deadTree : trees) {
            deadTree.draw(g);
        }
    }


    @Override
    public void render(Graphics g) {

        drawMap(g);
        towerManager.draw(g);
        drawTowerButtons(g);

    }

    private void drawMap(Graphics g) {

        g.setColor(new Color(134,177,63,255));
        g.fillRect(0, 0, GameDimensions.GAME_WIDTH, GameDimensions.GAME_HEIGHT);

        for (int i = 0; i < level.length; i++) {
            for (int j = 0; j < level[i].length; j++) {
                g.drawImage(tileManager.getSprite(level[i][j]), j * GameDimensions.TILE_DISPLAY_SIZE, i * GameDimensions.TILE_DISPLAY_SIZE, null);
            }
        }
    }

    @Override
    public void mouseClicked(int x, int y) {

        this.mouseX = x;
        this.mouseY = y;

        boolean clickedOnTree = false;
        for(DeadTree tree: trees){
            if (tree.isShowChoices()){
                if (tree.getArcherButton().isMousePressed(mouseX, mouseY)) {
                    towerManager.buildArcherTower();
                    tree.setShowChoices(false);
                    return;
                }
                if (tree.getMageButton().isMousePressed(mouseX, mouseY)) {
                    towerManager.buildMageTower();
                    tree.setShowChoices(false);
                    return;
                }
                if (tree.getArtilleryButton().isMousePressed(mouseX, mouseY)) {
                    towerManager.buildArtilerryTower();
                    tree.setShowChoices(false);
                    return;
                }
            }
        }

        for (DeadTree tree : trees) {
            if (tree.isClicked(mouseX, mouseY)) {
                for (DeadTree other : trees) {
                    other.setShowChoices(false);
                }
                tree.setShowChoices(true);
                return;
            }
        }


    }

    public TowerManager getTowerManager() {
        return towerManager;
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
