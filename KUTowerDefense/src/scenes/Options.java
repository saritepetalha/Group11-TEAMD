package scenes;

import config.GameOptions;
import constants.GameDimensions;
import helpMethods.OptionsIO;
import main.Game;
import ui_p.GameOptionsUI;
import ui_p.TheButton;

import java.awt.*;
import javax.swing.*;

import static main.GameStates.MENU;

/**
 * The Options scene that displays and manages game configuration settings.
 */
public class Options extends GameScene implements SceneMethods {
    private final Game game;
    private final TheButton backButton;
    private final GameOptionsUI optionsUI;
    private final JFrame optionsFrame;

    public Options(Game game) {
        super(game);
        this.game = game;

        // Initialize UI components
        this.backButton = new TheButton("Back", 2, 2, 100, 30);
        this.optionsUI = new GameOptionsUI(OptionsIO.load());

        // Create a separate frame for options UI
        this.optionsFrame = new JFrame("Game Options");
        this.optionsFrame.setContentPane(optionsUI);
        this.optionsFrame.setSize(800, 600);
        this.optionsFrame.setLocationRelativeTo(null);
        this.optionsFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
    }

    @Override
    public void render(Graphics g) {
        // Draw background
        g.setColor(new Color(70, 120, 200)); // Slightly different blue than the original
        g.fillRect(0, 0, GameDimensions.GAME_WIDTH, GameDimensions.GAME_HEIGHT);

        // Draw back button
        backButton.draw(g);

        // Draw help text
        g.setColor(Color.WHITE);
        Font originalFont = g.getFont();
        g.setFont(new Font("Arial", Font.BOLD, 18));
        g.drawString("Click anywhere (except back button) to open Options", 250, 300);
        g.setFont(originalFont);

        setCustomCursor();

        // Show options frame if it should be visible
        if (optionsFrame.isVisible() != optionsActive) {
            optionsFrame.setVisible(optionsActive);
        }
    }

    // Flag to track if options window is active
    private boolean optionsActive = false;

    @Override
    public void mouseClicked(int x, int y) {
        if (backButton.getBounds().contains(x, y)) {
            // Save options before going back to menu
            saveOptions();

            // Hide options frame if it's open
            if (optionsActive) {
                optionsActive = false;
                optionsFrame.setVisible(false);
            }

            game.changeGameState(MENU);
        } else {
            // Toggle options frame visibility when clicking anywhere else
            optionsActive = !optionsActive;
            optionsFrame.setVisible(optionsActive);
        }
    }

    @Override
    public void mouseMoved(int x, int y) {
        backButton.setMouseOver(false);

        if (backButton.getBounds().contains(x, y)) {
            backButton.setMouseOver(true);
        }
    }

    @Override
    public void mousePressed(int x, int y) {
        if (backButton.getBounds().contains(x, y)) {
            backButton.setMousePressed(true);
        }
    }

    @Override
    public void mouseReleased(int x, int y) {
        backButton.resetBooleans();
    }

    @Override
    public void mouseDragged(int x, int y) {
        // Not needed for options screen
    }

    /**
     * Saves the current options configuration to disk
     */
    private void saveOptions() {
        try {
            GameOptions options = optionsUI.getGameOptions();
            OptionsIO.save(options);
        } catch (Exception e) {
            System.err.println("Failed to save options: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Make sure the options frame is hidden when leaving this scene
     */
    public void cleanUp() {
        if (optionsFrame != null && optionsFrame.isVisible()) {
            optionsFrame.setVisible(false);
        }
        optionsActive = false;
    }
}
