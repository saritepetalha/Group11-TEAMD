package strategies;

import helpMethods.LoadSave;
import main.Game;
import main.GameStates;

public class LoadGameStrategy implements LevelLoadingStrategy {

    @Override
    public void loadLevel(String levelName, Game game) {
        // Load level data
        int[][] levelData = LoadSave.loadLevel(levelName);
        if (levelData == null) {
            System.err.println("Failed to load level: " + levelName);
            return;
        }

        // Load overlay
        int[][] overlay = LoadSave.loadOverlay(levelName);
        if (overlay == null) {
            overlay = new int[levelData.length][levelData[0].length];
            if (levelData.length > 4 && levelData[0].length > 15) {
                overlay[4][0] = 1;
                overlay[4][15] = 2;
            }
        }

        // Start playing and then load the saved game state
        game.startPlayingWithLevel(levelData, overlay, levelName);

        // Load the saved game state after initializing the level
        if (game.getPlaying() != null) {
            game.getPlaying().loadGameState();
        }

        game.changeGameState(GameStates.PLAYING);
    }

    @Override
    public String getDescription() {
        return "Continue Saved Game";
    }
}