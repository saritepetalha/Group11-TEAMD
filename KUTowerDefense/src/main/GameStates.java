package main;

public enum GameStates {
    PLAYING,
    MENU,
    EDIT,
    OPTIONS,
    LOADED,
    QUIT;

    public static GameStates gameState = GameStates.MENU;
    public static void setGameState(GameStates gameState) {
        GameStates.gameState = gameState;
    }
}
