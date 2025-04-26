package popUps;

import java.awt.*;

public class DialogueFactory {
    private final Frame owner;

    public DialogueFactory(Frame owner) {
        this.owner = owner;
    }

    public Dialogue<String> createSaveLevelDialog() {
        return new SaveLevelDialogue(owner);
    }

    // add more dialog-creators here...
}