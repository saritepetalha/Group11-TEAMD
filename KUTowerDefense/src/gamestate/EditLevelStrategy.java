package gamestate;

import helpMethods.LoadSave;
import main.Game;
import main.GameStates;

public class EditLevelStrategy implements LevelLoadingStrategy {

    @Override
    public void loadLevel(String levelName, Game game) {
        // Load level data
        int[][] levelData = LoadSave.loadLevel(levelName);
        if (levelData == null) {
            System.err.println("Failed to load level: " + levelName);
            return;
        }

        // Load overlay for editing
        int[][] overlay = LoadSave.loadOverlay(levelName);
        if (overlay == null) {
            overlay = new int[levelData.length][levelData[0].length];
        }

        // Set up map editor with the level
        if (game.getMapEditing() != null) {
            game.getMapEditing().setLevel(levelData);
            game.getMapEditing().setOverlayData(overlay);
            game.getMapEditing().setCurrentLevelName(levelName);
        }

        game.changeGameState(GameStates.EDIT);
    }

    @Override
    public String getDescription() {
        return "Edit Level";
    }
}