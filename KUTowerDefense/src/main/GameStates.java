package main;

public enum GameStates {
    PLAYING,
    INTRO,
    MENU,
    EDIT,
    OPTIONS,
    LOADED,
    QUIT;

    public static GameStates gameState = GameStates.INTRO;
    public static void setGameState(GameStates gameState) {
        GameStates.gameState = gameState;
    }
}
