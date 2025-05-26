package main;

public enum GameStates {
    PLAYING,
    INTRO,
    MENU,
    EDIT,
    OPTIONS,
    LOADED,
    LOAD_GAME,
    QUIT,
    GAME_OVER,
    STATISTICS;

    public static GameStates gameState = GameStates.INTRO;
    public static void setGameState(GameStates gameState) {
        GameStates.gameState = gameState;
    }
}
