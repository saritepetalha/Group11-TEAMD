package scenes;

import dimensions.GameDimensions;
import main.Game;
import managers.TileManager;
import objects.Tile;
import ui_p.EditTiles;

import java.awt.*;
import java.awt.image.BufferedImage;

// a class to edit map. map editor part on the main screen.

public class MapEditing extends GameScene implements SceneMethods{

    private int[][] level;
    private TileManager tileManager;
    private Tile selectedTile;
    private boolean drawSelected = false;
    private EditTiles editTiles;

    private int mouseX, mouseY;

    public MapEditing(Game game) {
        super(game);

    }

    @Override
    public void render(Graphics g) {

        drawSelectedTile(g);
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

    public void setDrawSelected(boolean drawSelected) {
        this.drawSelected = drawSelected;
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
