package main;

import java.awt.Graphics;

public class Render {

    private Game game;
    

    public Render(Game game) {
        this.game = game;
        
    }
    
    public void render(Graphics g) {
        switch (GameStates.gameState) {
            case INTRO:
                game.getIntro().render(g);
                break;
            case MENU:
                game.getMenu().render(g);
                break;
            case PLAYING:
                if (game.getPlaying() != null) {
                    game.getPlaying().render(g);
                }
                break;
            case OPTIONS:
                game.getOptions().render(g);
                break;
            case EDIT:
                game.getMapEditing().render(g);
                break;
            case QUIT:
                break;
            case GAME_OVER:
                game.getGameOverScene().render(g);
                break;
            case STATISTICS:
                game.getStatisticsScene().render(g);
                break;
        }
    }
}
