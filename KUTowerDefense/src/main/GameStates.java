package main;

public enum GameStates {
    PLAYING,
    MENU,
    OPTIONS,
    QUIT;

    public static GameStates gameState = GameStates.MENU;
}
