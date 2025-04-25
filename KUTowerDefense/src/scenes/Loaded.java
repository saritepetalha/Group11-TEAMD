package scenes;

import java.awt.*;
import java.util.ArrayList;

import dimensions.GameDimensions;
import helpMethods.LoadSave;
import main.Game;
import ui_p.TheButton;

import static main.GameStates.*;

public class Loaded extends GameScene implements SceneMethods {
    private ArrayList<TheButton> levelButtons = new ArrayList<>();
    private TheButton backButton;
    private Game game;

    public Loaded(Game game) {
        super(game);
        this.game = game;
        initButtons();
    }


    private void initButtons() {
        backButton = new TheButton("Back", 
            GameDimensions.GAME_WIDTH / 2 - GameDimensions.ButtonSize.MEDIUM.getSize() / 2,
            GameDimensions.GAME_HEIGHT - GameDimensions.ButtonSize.MEDIUM.getSize() - 20,
            GameDimensions.ButtonSize.MEDIUM.getSize(),
            GameDimensions.ButtonSize.MEDIUM.getSize(),
            null
        );

        ArrayList<String> savedLevels = LoadSave.getSavedLevels();
        int buttonY = 50;
        for (String levelName : savedLevels) {
            levelButtons.add(new TheButton(levelName,
                GameDimensions.GAME_WIDTH / 2 - GameDimensions.ButtonSize.MEDIUM.getSize() / 2,
                buttonY,
                GameDimensions.ButtonSize.MEDIUM.getSize(),
                GameDimensions.ButtonSize.MEDIUM.getSize(),
                null
            ));
            buttonY += GameDimensions.ButtonSize.MEDIUM.getSize() + 10;
        }
    }

    @Override
    public void render(Graphics g) {
        g.setColor(new Color(134,177,63,255));
        g.fillRect(0, 0, GameDimensions.GAME_WIDTH, GameDimensions.GAME_HEIGHT);

        backButton.draw(g);
        for (TheButton button : levelButtons) {
            button.draw(g);
        }
    }

    @Override
    public void mouseClicked(int x, int y) {
        if (backButton.getBounds().contains(x, y)) {
            game.changeGameState(MENU);
        } else {
            for (TheButton button : levelButtons) {
                if (button.getBounds().contains(x, y)) {
                    game.getPlaying().loadLevel(button.getText());
                    game.changeGameState(PLAYING);
                    return;
                }
            }
        }
    }

    @Override
    public void mouseMoved(int x, int y) {
        backButton.setMouseOver(false);
        for (TheButton button : levelButtons) {
            button.setMouseOver(false);
        }

        if (backButton.getBounds().contains(x, y)) {
            backButton.setMouseOver(true);
        } else {
            for (TheButton button : levelButtons) {
                if (button.getBounds().contains(x, y)) {
                    button.setMouseOver(true);
                    return;
                }
            }
        }
    }

    @Override
    public void mousePressed(int x, int y) {
        if (backButton.getBounds().contains(x, y)) {
            backButton.setMousePressed(true);
        } else {
            for (TheButton button : levelButtons) {
                if (button.getBounds().contains(x, y)) {
                    button.setMousePressed(true);
                    return;
                }
            }
        }
    }

    @Override
    public void mouseReleased(int x, int y) {
        backButton.resetBooleans();
        for (TheButton button : levelButtons) {
            button.resetBooleans();
        }
    }

    @Override
    public void mouseDragged(int x, int y) {
        // Not needed for this scene
    }
} 