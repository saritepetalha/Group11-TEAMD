package main;

public enum GameStates {
    PLAYING,
    INTRO,
    MENU,
    EDIT,
    OPTIONS,
    LOADED,
    LOAD_GAME,
    NEW_GAME_LEVEL_SELECT,
    SKILL_SELECTION,
    QUIT,
    GAME_OVER,
    STATISTICS,
    REPLAY;

    public static GameStates gameState = GameStates.INTRO;
    public static void setGameState(GameStates gameState) {
        GameStates.gameState = gameState;
    }
}
