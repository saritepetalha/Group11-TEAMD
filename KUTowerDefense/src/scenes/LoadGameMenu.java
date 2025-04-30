package scenes;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import helpMethods.LoadSave;
import main.Game;
import main.GameStates;

public class LoadGameMenu extends JFrame {
    private Game game;
    private JList<String> levelList;

    public LoadGameMenu(Game game) {
        this.game = game;
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Load Game");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel listPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Select a Level:", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        listPanel.add(titleLabel, BorderLayout.NORTH);

        ArrayList<String> savedLevels = LoadSave.getSavedLevels();
        levelList = new JList<>(savedLevels.toArray(new String[0]));
        levelList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(levelList);
        listPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton playButton = new JButton("Play Mode");
        JButton editButton = new JButton("Edit Mode");
        JButton cancelButton = new JButton("Cancel");

        buttonPanel.add(playButton);
        buttonPanel.add(editButton);
        buttonPanel.add(cancelButton);

        playButton.addActionListener(e -> loadLevel(GameStates.PLAYING));
        editButton.addActionListener(e -> loadLevel(GameStates.EDIT));
        cancelButton.addActionListener(e -> dispose());

        mainPanel.add(listPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(mainPanel);
    }

    private void loadLevel(GameStates mode) {
        String selectedLevel = levelList.getSelectedValue();
        if (selectedLevel != null) {
            int[][] levelData = LoadSave.loadLevel(selectedLevel);
            if (levelData != null) {
                if (mode == GameStates.EDIT) {
                    game.getMapEditing().setLevel(levelData);
                    game.getMapEditing().setOverlayData(new int[levelData.length][levelData[0].length]);
                    game.getMapEditing().setCurrentLevelName(selectedLevel);
                }
                game.changeGameState(mode);
            }
        }
    }

    public void showMenu() {
        setVisible(true);
    }
}