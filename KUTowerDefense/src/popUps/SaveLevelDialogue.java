package popUps;

import javax.swing.*;
import java.awt.*;

public class SaveLevelDialogue extends AbstractDialogue<String> {
    private JTextField nameField;
    private String result;

    public SaveLevelDialogue(Frame owner) {
        super(owner, "Save Level As…");
        buildUI();
    }

    @Override
    protected void buildUI() {
        nameField = new JTextField(20);
        content.add(new JLabel("Level name:"), BorderLayout.NORTH);
        content.add(nameField, BorderLayout.CENTER);

        JPanel buttons = new JPanel();
        JButton ok = new JButton("Save");
        JButton cancel = new JButton("Cancel");
        buttons.add(ok);
        buttons.add(cancel);
        content.add(buttons, BorderLayout.SOUTH);

        ok.addActionListener(e -> {
            result = nameField.getText().trim();
            dispose();
        });
        cancel.addActionListener(e -> {
            result = null;
            dispose();
        });
    }

    @Override
    protected String getResult() {
        return result;
    }
}
