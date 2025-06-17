package scenes;

import config.GameOptions;

import helpMethods.OptionsIO;
import main.Game;
import ui_p.GameOptionsUI;

import javax.swing.*;
import java.awt.*;

/**
 * The Options scene that displays and manages game configuration settings
 * as a JPanel within the main game window.
 */
public class Options extends JPanel implements SceneMethods {
    private final Game game;
    private final GameOptionsUI optionsUI;

    public Options(Game game) {
        this.game = game;
        setLayout(new BorderLayout());
        setOpaque(false);

        this.optionsUI = new GameOptionsUI(this.game, OptionsIO.load());
        add(optionsUI, BorderLayout.CENTER);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
    }

    @Override
    public void render(Graphics g) {
        // This JPanel is managed by GameScreen.
        // Swing handles painting its child (optionsUI).
        // Calling repaint() ensures it and its children are redrawn if needed.
        repaint();
    }

    /**
     * Saves current options and performs any necessary cleanup before
     * this panel is removed from the UI
     */
    public void cleanUp() {
        GameOptions currentOptions = optionsUI.getGameOptions();
        OptionsIO.save(currentOptions);

        // Trigger options reload in the Playing instance
        if (game != null && game.getPlaying() != null) {
            game.getPlaying().reloadGameOptions();
        }
    }

    // Mouse interaction methods are removed as they were for the old back button in this class.
    // GameOptionsUI now handles all its own interactions, including its own Back button.
    @Override
    public void mouseClicked(int x, int y) { /* No longer needed */ }

    @Override
    public void mouseMoved(int x, int y) { /* No longer needed */ }

    @Override
    public void mousePressed(int x, int y) { /* No longer needed */ }

    @Override
    public void mouseReleased(int x, int y) { /* No longer needed */ }

    @Override
    public void mouseDragged(int x, int y) { /* No longer needed */ }

    @Override
    public void playButtonClickSound() {
        // This is handled by the default implementation in SceneMethods
    }

    public GameOptionsUI getGameOptionsPanelItself() {
        return optionsUI;
    }
}
