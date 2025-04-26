package popUps;

import javax.swing.*;
import java.awt.*;

public abstract class AbstractDialogue<T> extends JDialog implements Dialogue<T> {
    protected JPanel content = new JPanel(new BorderLayout());

    protected AbstractDialogue(Frame owner, String title) {
        super(owner, title, true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setContentPane(content);
    }

    /**
     * Subclasses implement this to build the controls
     * and wire up OK/Cancel buttons, etc.
     */
    protected abstract void buildUI();

    @Override
    public T showAndWait() {
        pack();
        setLocationRelativeTo(getOwner());
        setVisible(true);
        return getResult();
    }

    /** Called internally when user clicks “OK” (or equivalent). */
    protected abstract T getResult();
}