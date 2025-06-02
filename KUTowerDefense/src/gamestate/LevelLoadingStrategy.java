package gamestate;

import main.Game;

public interface LevelLoadingStrategy {
    void loadLevel(String levelName, Game game);
    String getDescription();
}