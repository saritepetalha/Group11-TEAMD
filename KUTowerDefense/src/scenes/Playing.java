package scenes;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import dimensions.GameDimensions;
import helpMethods.LevelBuilder;
import helpMethods.LoadSave;
import main.Game;
import managers.TileManager;
import ui_p.EditTiles;
import ui_p.TheButton;

import static main.GameStates.*;
import objects.Tile;

public class Playing extends GameScene implements SceneMethods {
    private int[][] level;
    //private Tile selectedTile;
    //private boolean drawSelected = false;

    private EditTiles editTiles;

    private int mouseX, mouseY;

    public Playing(Game game) {
        super(game);
        loadDefaultLevel();

        editTiles = new EditTiles(GameDimensions.GAME_WIDTH,0,4*GameDimensions.ButtonSize.MEDIUM.getSize(), GameDimensions.GAME_HEIGHT,this, game);

    }


    public void saveLevel() {
        LoadSave.saveLevel("defaultleveltest1",level);
    }


    private void loadDefaultLevel() {
        int[][] lvl = LoadSave.getLevelData("defaultleveltest1");
        //THIS LINE IS JUST TO SEE WHETHER THE BACKEND OF THE getLevelData function works or not
        //IT WORKS!!!
        System.out.println(java.util.Arrays.deepToString(lvl));
    }

    public TileManager getTileManager() {
        return tileManager;
    }

    public void setSelectedTile(Tile selectedTile) {
        this.selectedTile = selectedTile;
        drawSelected = true;
    }



    @Override
    public void render(Graphics g) {
        g.setColor(new Color(134,177,63,255));
        g.fillRect(0, 0, GameDimensions.GAME_WIDTH, GameDimensions.GAME_HEIGHT);

        for (int i = 0; i < level.length; i++) {
            for (int j = 0; j < level[i].length; j++) {
                g.drawImage(tileManager.getSprite(level[i][j]), j * GameDimensions.TILE_DISPLAY_SIZE, i * GameDimensions.TILE_DISPLAY_SIZE, null);
            }
        }

        editTiles.draw(g);

    }

    @Override
    public void mouseClicked(int x, int y) {
        if( x >= GameDimensions.GAME_WIDTH){
            editTiles.mouseClicked(x,y);
        }
        else {
            modifyTile(x, y);
        }

    }


    @Override
    public void mouseMoved(int x, int y) {
        if( x >= GameDimensions.GAME_WIDTH){
            editTiles.mouseMoved(x,y);
            drawSelected = false;
        }
        else{
            mouseX = x / 64;
            mouseY = y / 64;
            mouseX *= 64;
            mouseY *= 64;
            drawSelected = true;
        }

    }

    @Override
    public void mousePressed(int x, int y) {
        if( x >= GameDimensions.GAME_WIDTH){
            editTiles.mousePressed(x,y);
        }

    }

    @Override
    public void mouseReleased(int x, int y) {editTiles.mouseReleased(x,y);}
}
