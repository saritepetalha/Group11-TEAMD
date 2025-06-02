package gamestate;

import helpMethods.LoadSave;
import main.Game;
import main.GameStates;

public class NewGameStrategy implements LevelLoadingStrategy {

    @Override
    public void loadLevel(String levelName, Game game) {
        // Load level data
        int[][] levelData = LoadSave.loadLevel(levelName);
        if (levelData == null) {
            System.err.println("Failed to load level: " + levelName);
            return;
        }

        // Load or create default overlay
        int[][] overlay = LoadSave.loadOverlay(levelName);
        if (overlay == null) {
            overlay = new int[levelData.length][levelData[0].length];
            // Set default start and end positions if level is large enough
            if (levelData.length > 4 && levelData[0].length > 15) {
                overlay[4][0] = 1; // Start position
                overlay[4][15] = 2; // End position
            }
        }

        // Start playing with fresh game state
        game.startPlayingWithLevel(levelData, overlay, levelName);
        game.changeGameState(GameStates.PLAYING);
    }

    @Override
    public String getDescription() {
        return "Start New Game";
    }
}