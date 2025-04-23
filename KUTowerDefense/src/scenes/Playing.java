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
    private TileManager tileManager;
    private Tile selectedTile;
    private boolean drawSelected = false;

    private EditTiles editTiles;

    private int mouseX, mouseY;

    public Playing(Game game) {
        super(game);
        level = LevelBuilder.getLevelData();
        tileManager = new TileManager();
        editTiles = new EditTiles(GameDimensions.GAME_WIDTH,0,4*GameDimensions.ButtonSize.MEDIUM.getSize(), GameDimensions.GAME_HEIGHT,this, game);

        createDefaultLevel();
        loadDefaultLevel();
    }


    public void saveLevel() {
        LoadSave.saveLevel("defaultleveltest1",level);
    }

    private void createDefaultLevel() {
        int[][] bruh = new int[20][20];
        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 20; j++) {
                bruh[i][j] = 0;
            }
        }
        LoadSave.createLevel("defaultleveltest1", bruh);
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

    public void setDrawSelected(boolean drawSelected) {
        this.drawSelected = drawSelected;
    }

    private void drawSelectedTile(Graphics g) {
        if (selectedTile != null && drawSelected) {
            int tileSize = GameDimensions.TILE_DISPLAY_SIZE;

            BufferedImage spriteToDraw;

            if (selectedTile.getName().equals("Castle")) {
                spriteToDraw = tileManager.getFullCastleSprite(); // using the complete 2x2 castle sprite
                g.drawImage(spriteToDraw, mouseX, mouseY, tileSize * 2, tileSize * 2, null);
            } else {
                spriteToDraw = selectedTile.getSprite();
                g.drawImage(spriteToDraw, mouseX, mouseY, tileSize, tileSize, null);
            }

        }

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
        drawSelectedTile(g);

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
    private void modifyTile(int x, int y) {

        x /= 64;
        y /= 64;

        if (selectedTile == null) {
            return;
        }

        if (selectedTile.getName().equals("Castle")) {
            // place Castle in 2x2 area
            if (y + 1 < level.length && x + 1 < level[0].length) {

                level[y][x] = tileManager.CastleTopLeft.getId();                   // top-left: ID 24
                level[y][x + 1] = tileManager.CastleTopRight.getId();           // top-right: ID 25
                level[y + 1][x] = tileManager.CastleBottomLeft.getId();           // bottom-left: ID 28
                level[y + 1][x + 1] = tileManager.CastleBottomRight.getId();       // bottom-right: ID 29
            }
        } else {
            level[y][x] = selectedTile.getId();
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
