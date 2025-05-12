package ui_p;

import config.*;
import helpMethods.OptionsIO;
import main.Game;
import main.GameStates;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;

/**
 * UI component for editing game options.
 * This panel allows modifying all game settings that are stored in GameOptions.
 */
public class GameOptionsUI extends JPanel {

    private final Game game;
    private final GameOptions options;
    private final JTabbedPane tabbedPane;

    private JTextField startingGoldField;
    private JTextField startingPlayerHPField;
    private JTextField interWaveDelayField;

    private final Map<EnemyType, Map<String, JTextField>> enemyStatFields = new EnumMap<>(EnemyType.class);
    private final Map<TowerType, Map<String, JTextField>> towerStatFields = new EnumMap<>(TowerType.class);
    private final List<WavePanel> wavePanels = new ArrayList<>();
    private JPanel wavesContainer;

    private final Font labelFont = new Font("MV Boli", Font.BOLD, 14);
    private final Font fieldFont = new Font("MV Boli", Font.PLAIN, 14);
    private final Font headerFont = new Font("MV Boli", Font.BOLD, 16);
    private final Font buttonFont = new Font("MV Boli", Font.BOLD, 14);
    private final Insets generalInsets = new Insets(8, 8, 8, 8);
    private final Color panelBackgroundColor = new Color(240, 240, 240);
    private final Color buttonBackgroundColor = new Color(220, 220, 220);
    private final Color buttonTextColor = Color.BLACK;

    public GameOptionsUI(Game game, GameOptions options) {
        this.game = game;
        this.options = options;
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));
        setBackground(new Color(225, 225, 225));

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(headerFont.deriveFont(Font.PLAIN, 14f));

        tabbedPane.addTab("General", createGeneralPanel());
        tabbedPane.addTab("Enemy Stats", createEnemyStatsPanel());
        tabbedPane.addTab("Tower Stats", createTowerStatsPanel());
        tabbedPane.addTab("Wave Config", createWavesPanel());

        add(tabbedPane, BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);
        loadValuesFromOptions();
    }

    public GameOptionsUI(GameOptions options) {
        this(null, options);
        if (this.game == null) {
            System.err.println("GameOptionsUI created without Game context - Back button will be non-functional.");
        }
    }

    private void styleLabel(JLabel label) {
        label.setFont(labelFont);
    }

    private void styleField(JTextField field) {
        field.setFont(fieldFont);
        field.setColumns(8); // Default column width
        field.setHorizontalAlignment(JTextField.RIGHT);
        field.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(1, 1, 1, 1, Color.GRAY),
                new EmptyBorder(2, 5, 2, 5)
        ));
    }

    private void styleButton(JButton button) {
        button.setFont(buttonFont);
        button.setBackground(buttonBackgroundColor);
        button.setForeground(buttonTextColor);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.DARK_GRAY, 1),
                new EmptyBorder(5, 15, 5, 15)
        ));
    }

    /**
     * Creates the panel for general game settings
     */
    private JPanel createGeneralPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(panelBackgroundColor);
        panel.setBorder(new EmptyBorder(10,10,10,10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = generalInsets;
        gbc.anchor = GridBagConstraints.WEST;

        JLabel goldLabel = new JLabel("Starting Gold:");
        styleLabel(goldLabel);
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(goldLabel, gbc);
        startingGoldField = new JTextField(10); styleField(startingGoldField);
        gbc.gridx = 1; panel.add(startingGoldField, gbc);

        JLabel hpLabel = new JLabel("Starting Player HP:");
        styleLabel(hpLabel);
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(hpLabel, gbc);
        startingPlayerHPField = new JTextField(10); styleField(startingPlayerHPField);
        gbc.gridx = 1; panel.add(startingPlayerHPField, gbc);

        JLabel delayLabel = new JLabel("Inter-wave Delay (s):");
        styleLabel(delayLabel);
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(delayLabel, gbc);
        interWaveDelayField = new JTextField(10); styleField(interWaveDelayField);
        gbc.gridx = 1; panel.add(interWaveDelayField, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.weighty = 1.0; gbc.fill = GridBagConstraints.VERTICAL;
        panel.add(Box.createVerticalGlue(), gbc);
        return panel;
    }

    /**
     * Creates the panel for enemy stats configuration
     */
    private JPanel createEnemyStatsPanel() {
        String[] headers = {"Hit Points", "Move Speed", "Gold Reward"};
        return createStatsPanelTemplate(headers, EnemyType.values(), (Map) enemyStatFields, "");
    }

    /**
     * Creates the panel for tower stats configuration
     */
    private JPanel createTowerStatsPanel() {
        String[] headers = {"Build Cost", "Range", "Fire Rate", "AOE Radius", "Damage"};
        return createStatsPanelTemplate(headers, TowerType.values(), (Map) towerStatFields, "");
    }

    /**
     * Creates the panel for wave configuration
     */
    private JPanel createWavesPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(5,5));
        mainPanel.setBackground(panelBackgroundColor);
        mainPanel.setBorder(new EmptyBorder(10,10,10,10));

        wavesContainer = new JPanel();
        wavesContainer.setLayout(new BoxLayout(wavesContainer, BoxLayout.Y_AXIS));
        wavesContainer.setBackground(panelBackgroundColor);

        JScrollPane scrollPane = new JScrollPane(wavesContainer);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setBackground(panelBackgroundColor);
        JButton addWaveButton = new JButton("Add Wave");
        styleButton(addWaveButton);
        addWaveButton.addActionListener(e -> addNewWave());
        buttonPanel.add(addWaveButton);

        mainPanel.add(buttonPanel, BorderLayout.NORTH);
        return mainPanel;
    }

    /**
     * Creates the bottom button panel with Save and Reset buttons
     */
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panel.setBorder(new EmptyBorder(10, 0, 0, 0));
        panel.setBackground(new Color(225, 225, 225));

        JButton backButton = new JButton("Back to Menu");
        styleButton(backButton);
        backButton.addActionListener(e -> {
            if (this.game != null) {
                saveOptions();
                this.game.changeGameState(GameStates.MENU);
            } else {
                JOptionPane.showMessageDialog(this, "Cannot return to menu: Game context not available.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        panel.add(backButton);

        panel.add(Box.createHorizontalStrut(10));

        JButton resetButton = new JButton("Reset to Defaults");
        styleButton(resetButton);
        resetButton.addActionListener(e -> resetToDefaults());
        panel.add(resetButton);

        JButton saveButton = new JButton("Save Settings");
        styleButton(saveButton);
        saveButton.setBackground(new Color(180, 220, 180));
        saveButton.addActionListener(e -> saveOptions());
        panel.add(saveButton);

        return panel;
    }

    /**
     * Adds a new empty wave to the configuration
     */
    private void addNewWave() {
        Wave newWave = new Wave(new ArrayList<>(), 1.0);
        options.getWaves().add(newWave);

        WavePanel wavePanel = new WavePanel(newWave, wavePanels.size() + 1);
        wavePanels.add(wavePanel);
        wavesContainer.add(wavePanel);
        if (wavePanels.size() > 1) { // Add separator if more than one wave panel
            wavesContainer.add(Box.createRigidArea(new Dimension(0, 10)));
        }
        wavesContainer.revalidate();
        wavesContainer.repaint();
    }

    /**
     * Loads all values from the options object into the UI
     */
    public void loadValuesFromOptions() {
        startingGoldField.setText(String.valueOf(options.getStartingGold()));
        startingPlayerHPField.setText(String.valueOf(options.getStartingPlayerHP()));
        interWaveDelayField.setText(String.valueOf(options.getInterWaveDelay()));

        for (Map.Entry<EnemyType, EnemyStats> entry : options.getEnemyStats().entrySet()) {
            EnemyType type = entry.getKey();
            EnemyStats stats = entry.getValue();
            Map<String, JTextField> fields = enemyStatFields.get(type);
            if (fields != null) {
                fields.get("hitPoints").setText(String.valueOf(stats.getHitPoints()));
                fields.get("moveSpeed").setText(String.valueOf(stats.getMoveSpeed()));
                fields.get("goldReward").setText(String.valueOf(stats.getGoldReward()));
            }
        }

        for (Map.Entry<TowerType, TowerStats> entry : options.getTowerStats().entrySet()) {
            TowerType type = entry.getKey();
            TowerStats stats = entry.getValue();
            Map<String, JTextField> fields = towerStatFields.get(type);
            if (fields != null) {
                fields.get("buildCost").setText(String.valueOf(stats.getBuildCost()));
                fields.get("range").setText(String.valueOf(stats.getRange()));
                fields.get("fireRate").setText(String.valueOf(stats.getFireRate()));
                fields.get("aoeRadius").setText(String.valueOf(stats.getAoeRadius()));
                fields.get("damage").setText(String.valueOf(stats.getDamage()));
            }
        }

        wavesContainer.removeAll();
        wavePanels.clear();
        List<Wave> waves = options.getWaves();
        for (int i = 0; i < waves.size(); i++) {
            Wave wave = waves.get(i);
            WavePanel wavePanel = new WavePanel(wave, i + 1);
            wavePanels.add(wavePanel);
            wavesContainer.add(wavePanel);
            if (i < waves.size() -1) {
                wavesContainer.add(Box.createRigidArea(new Dimension(0, 10))); // Separator
            }
        }
        wavesContainer.revalidate();
        wavesContainer.repaint();
    }

    /**
     * Saves the UI values back to the options object and writes to disk
     */
    private void saveOptions() {
        try {
            options.setStartingGold(Integer.parseInt(startingGoldField.getText()));
            options.setStartingPlayerHP(Integer.parseInt(startingPlayerHPField.getText()));
            options.setInterWaveDelay(Double.parseDouble(interWaveDelayField.getText()));

            for (Map.Entry<EnemyType, Map<String, JTextField>> entry : enemyStatFields.entrySet()) {
                EnemyType type = entry.getKey();
                Map<String, JTextField> fields = entry.getValue();
                EnemyStats stats = options.getEnemyStats().getOrDefault(type, new EnemyStats());
                stats.setHitPoints(Integer.parseInt(fields.get("hitPoints").getText()));
                stats.setMoveSpeed(Double.parseDouble(fields.get("moveSpeed").getText()));
                stats.setGoldReward(Integer.parseInt(fields.get("goldReward").getText()));
                options.getEnemyStats().put(type, stats);
            }

            for (Map.Entry<TowerType, Map<String, JTextField>> entry : towerStatFields.entrySet()) {
                TowerType type = entry.getKey();
                Map<String, JTextField> fields = entry.getValue();
                TowerStats stats = options.getTowerStats().getOrDefault(type, new TowerStats());
                stats.setBuildCost(Integer.parseInt(fields.get("buildCost").getText()));
                stats.setRange(Double.parseDouble(fields.get("range").getText()));
                stats.setFireRate(Double.parseDouble(fields.get("fireRate").getText()));
                stats.setAoeRadius(Double.parseDouble(fields.get("aoeRadius").getText()));
                stats.setDamage(Integer.parseInt(fields.get("damage").getText()));
                options.getTowerStats().put(type, stats);
            }

            List<Wave> waves = options.getWaves();
            waves.clear();
            for (WavePanel wavePanel : wavePanels) {
                waves.add(wavePanel.getWave());
            }

            OptionsIO.save(options);
            JOptionPane.showMessageDialog(this, "Settings saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid number format: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, "Invalid value: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Reset all options to defaults and reload the UI
     */
    private void resetToDefaults() {
        int result = JOptionPane.showConfirmDialog(this, "Reset all settings to defaults?", "Confirm Reset", JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION) {
            OptionsIO.resetToDefaults();
            GameOptions defaults = OptionsIO.load();
            options.setStartingGold(defaults.getStartingGold());
            options.setStartingPlayerHP(defaults.getStartingPlayerHP());
            options.setInterWaveDelay(defaults.getInterWaveDelay());
            options.getEnemyStats().clear();
            options.getEnemyStats().putAll(defaults.getEnemyStats());
            options.getTowerStats().clear();
            options.getTowerStats().putAll(defaults.getTowerStats());
            options.getWaves().clear();
            options.getWaves().addAll(defaults.getWaves());
            loadValuesFromOptions();
        }
    }

    /**
     * Inner class to represent a Wave in the UI
     */
    private class WavePanel extends JPanel {
        private final Wave wave;
        private int waveNumber; // Made non-final to allow update
        private final List<GroupPanel> groupPanels = new ArrayList<>();
        private JTextField intraGroupDelayField;
        private JPanel groupsContainer;

        public WavePanel(Wave wave, int waveNumber) {
            this.wave = wave;
            this.waveNumber = waveNumber;
            setLayout(new BorderLayout(5, 5));
            setBackground(panelBackgroundColor.brighter()); // Slightly brighter for distinction
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.DARK_GRAY), "Wave " + waveNumber,
                            javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                            javax.swing.border.TitledBorder.DEFAULT_POSITION,
                            headerFont.deriveFont(14f), Color.BLACK),
                    new EmptyBorder(5,5,5,5) // Inner padding
            ));

            JPanel propertiesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5,0));
            propertiesPanel.setOpaque(false);
            JLabel delayLabel = new JLabel("Intra-Group Delay (s):"); styleLabel(delayLabel);
            propertiesPanel.add(delayLabel);
            intraGroupDelayField = new JTextField(String.valueOf(wave.getIntraGroupDelay()), 5);
            styleField(intraGroupDelayField);
            propertiesPanel.add(intraGroupDelayField);

            JButton removeWaveButton = new JButton("Remove This Wave");
            styleButton(removeWaveButton);
            removeWaveButton.setBackground(new Color(220, 180, 180)); // Light red for remove
            removeWaveButton.addActionListener(e -> removeWave());
            propertiesPanel.add(Box.createHorizontalStrut(10)); // Spacer
            propertiesPanel.add(removeWaveButton);
            add(propertiesPanel, BorderLayout.NORTH);

            groupsContainer = new JPanel();
            groupsContainer.setLayout(new BoxLayout(groupsContainer, BoxLayout.Y_AXIS));
            groupsContainer.setOpaque(false);
            JScrollPane groupsScrollPane = new JScrollPane(groupsContainer);
            groupsScrollPane.setPreferredSize(new Dimension(550, 180)); // Adjusted preferred size
            groupsScrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
            add(groupsScrollPane, BorderLayout.CENTER);

            JPanel groupButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
            groupButtonsPanel.setOpaque(false);
            JButton addGroupButton = new JButton("Add Group to Wave");
            styleButton(addGroupButton);
            groupButtonsPanel.add(addGroupButton);
            groupButtonsPanel.add(Box.createHorizontalStrut(10)); // Spacer
            addGroupButton.addActionListener(e -> addNewGroup());
            add(groupButtonsPanel, BorderLayout.SOUTH);

            for (Group group : wave.getGroups()) {
                GroupPanel groupPanel = new GroupPanel(group, groupPanels.size() + 1);
                groupPanels.add(groupPanel);
                groupsContainer.add(groupPanel);
                if (groupPanels.size() > 1) {
                    groupsContainer.add(Box.createRigidArea(new Dimension(0,5)));
                }
            }
        }

        public Wave getWave() {
            wave.setIntraGroupDelay(Double.parseDouble(intraGroupDelayField.getText()));
            List<Group> groups = wave.getGroups();
            groups.clear();
            for (GroupPanel panel : groupPanels) groups.add(panel.getGroup());
            return wave;
        }

        private void addNewGroup() {
            Group newGroup = new Group(new EnumMap<>(EnemyType.class), 0.5);
            wave.getGroups().add(newGroup);
            GroupPanel groupPanel = new GroupPanel(newGroup, groupPanels.size() + 1);
            groupPanels.add(groupPanel);
            groupsContainer.add(groupPanel);
            if (groupPanels.size() > 1) {
                groupsContainer.add(Box.createRigidArea(new Dimension(0,5)), groupPanels.size() -1); // Insert separator before new group if not first
            }
            groupsContainer.revalidate();
            groupsContainer.repaint();
        }

        private void removeWave() {
            int result = JOptionPane.showConfirmDialog(GameOptionsUI.this, "Remove Wave " + waveNumber + "?", "Confirm Removal", JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.YES_OPTION) {
                options.getWaves().remove(wave);
                wavePanels.remove(this);
                Container parent = getParent(); // This should be wavesContainer
                if (parent != null) {
                    parent.remove(this);
                    // Also remove any separator associated with this wave panel
                    Component[] comps = parent.getComponents();
                    for(int i=0; i < wavePanels.size(); i++) { // wavePanels is already updated
                        if (i < comps.length && comps[i] instanceof Box.Filler) { // If there was a separator after this one
                            // This logic for removing separator is complex and might need refinement
                        }
                    }
                    parent.revalidate();
                    parent.repaint();
                }
                // Renumber remaining waves
                for (int i = 0; i < wavePanels.size(); i++) {
                    wavePanels.get(i).updateWaveNumber(i + 1);
                }
                // Reloading all waves is simpler to ensure correct separators and numbering
                loadValuesFromOptions(); // This will rebuild the waves UI correctly
            }
        }

        public void updateWaveNumber(int newNumber) {
            this.waveNumber = newNumber; // Store new number
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.DARK_GRAY), "Wave " + newNumber,
                            javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                            javax.swing.border.TitledBorder.DEFAULT_POSITION,
                            headerFont.deriveFont(14f), Color.BLACK),
                    new EmptyBorder(5,5,5,5)
            ));
        }

        /**
         * Inner class to represent a Group in the UI
         */
        private class GroupPanel extends JPanel {
            private final Group group;
            private int groupNumber; // Made non-final
            private JTextField intraEnemyDelayField;
            private Map<EnemyType, JTextField> enemyCountFields = new EnumMap<>(EnemyType.class);

            public GroupPanel(Group group, int groupNumber) {
                this.group = group;
                this.groupNumber = groupNumber;
                setOpaque(false); // Inherit background from WavePanel
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY),"Group " + groupNumber,
                                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                                labelFont.deriveFont(Font.PLAIN) , Color.DARK_GRAY),
                        new EmptyBorder(3,3,3,3)
                ));
                setLayout(new GridBagLayout());
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.insets = new Insets(4, 4, 4, 4);
                gbc.anchor = GridBagConstraints.WEST;

                gbc.gridx = 0; gbc.gridy = 0;
                JLabel delayLabel = new JLabel("Intra-Enemy Delay (s):"); styleLabel(delayLabel);
                add(delayLabel, gbc);
                gbc.gridx = 1; gbc.weightx = 0.1;
                intraEnemyDelayField = new JTextField(String.valueOf(group.getIntraEnemyDelay()), 4);
                styleField(intraEnemyDelayField);
                add(intraEnemyDelayField, gbc);
                gbc.weightx = 0; // Reset weight

                gbc.gridx = 2; gbc.gridy = 0; gbc.gridheight = 2; // Span 2 rows
                gbc.anchor = GridBagConstraints.CENTER; // Center remove button
                JButton removeButton = new JButton("Remove");
                styleButton(removeButton);
                removeButton.setFont(buttonFont.deriveFont(12f));
                removeButton.setMargin(new Insets(2,5,2,5)); // Smaller margin
                removeButton.setBackground(new Color(220, 200, 200));
                removeButton.addActionListener(e -> removeGroup());
                add(removeButton, gbc);
                gbc.gridheight = 1; // Reset span
                gbc.anchor = GridBagConstraints.WEST; // Reset anchor

                gbc.gridx = 0; gbc.gridy = 1;
                JLabel compLabel = new JLabel("Composition:"); styleLabel(compLabel);
                add(compLabel, gbc);

                JPanel compositionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 0));
                compositionPanel.setOpaque(false);
                for (EnemyType type : EnemyType.values()) {
                    JLabel enemyLabel = new JLabel(type.name() + ":"); styleLabel(enemyLabel);
                    enemyLabel.setFont(labelFont.deriveFont(12f)); // Slightly smaller
                    compositionPanel.add(enemyLabel);
                    JTextField countField = new JTextField(3); styleField(countField);
                    countField.setText(String.valueOf(group.getComposition().getOrDefault(type, 0)));
                    compositionPanel.add(countField);
                    enemyCountFields.put(type, countField);
                    compositionPanel.add(Box.createHorizontalStrut(5)); // Spacer between enemy types
                }
                gbc.gridx = 1; gbc.gridwidth = 1; // Take available space
                gbc.fill = GridBagConstraints.HORIZONTAL;
                add(compositionPanel, gbc);
                gbc.fill = GridBagConstraints.NONE; // Reset fill
            }

            public Group getGroup() {
                group.setIntraEnemyDelay(Double.parseDouble(intraEnemyDelayField.getText()));
                Map<EnemyType, Integer> composition = group.getComposition();
                composition.clear();
                for (Map.Entry<EnemyType, JTextField> entry : enemyCountFields.entrySet()) {
                    int count = Integer.parseInt(entry.getValue().getText());
                    if (count > 0) composition.put(entry.getKey(), count);
                }
                return group;
            }

            private void removeGroup() {
                int result = JOptionPane.showConfirmDialog(GameOptionsUI.this, "Remove Group " + groupNumber + " from Wave "+ WavePanel.this.waveNumber +"?", "Confirm Removal", JOptionPane.YES_NO_OPTION);
                if (result == JOptionPane.YES_OPTION) {
                    wave.getGroups().remove(group);
                    groupPanels.remove(this);
                    Container parent = getParent(); // This should be groupsContainer
                    if (parent != null) {
                        parent.remove(this);
                        parent.revalidate();
                        parent.repaint();
                    }
                    // Renumber remaining groups in this wave
                    for (int i = 0; i < groupPanels.size(); i++) {
                        groupPanels.get(i).updateGroupNumber(i + 1);
                    }
                }
            }

            public void updateGroupNumber(int newNumber) {
                this.groupNumber = newNumber;
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY),"Group " + newNumber,
                                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                                labelFont.deriveFont(Font.PLAIN) , Color.DARK_GRAY),
                        new EmptyBorder(3,3,3,3)
                ));
            }
        }
    }

    /**
     * Returns the current game options
     */
    public GameOptions getGameOptions() {
        return options;
    }

    private JPanel createStatsPanelTemplate(String[] headers, Enum<?>[] types, Map<Enum<?>, Map<String, JTextField>> statFieldsMap, String typeLabelPrefix) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(panelBackgroundColor);
        panel.setBorder(new EmptyBorder(10,10,10,10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = generalInsets;


        // Headers
        gbc.anchor = GridBagConstraints.CENTER; // Center headers
        for (int i = 0; i < headers.length; i++) {
            JLabel headerLabel = new JLabel(headers[i]);
            headerLabel.setFont(headerFont.deriveFont(14f));
            headerLabel.setHorizontalAlignment(SwingConstants.CENTER);
            gbc.gridx = i + 1;
            gbc.gridy = 0;
            gbc.weightx = 0.1; // Give headers some weight too
            gbc.fill = GridBagConstraints.HORIZONTAL;
            panel.add(headerLabel, gbc);
        }
        gbc.weightx = 0.0; // Reset for type labels
        gbc.fill = GridBagConstraints.NONE; // Reset for type labels

        int row = 1;
        for (Enum<?> type : types) {
            Map<String, JTextField> fields = new HashMap<>();

            JLabel typeNameLabel = new JLabel(typeLabelPrefix + type.name() + ":");
            styleLabel(typeNameLabel);
            gbc.gridx = 0;
            gbc.gridy = row;
            gbc.anchor = GridBagConstraints.EAST; // Align type names to the right of their cell
            gbc.fill = GridBagConstraints.NONE;
            panel.add(typeNameLabel, gbc);

            for (int i = 0; i < headers.length; i++) {
                JTextField field = new JTextField(6); // Initial columns
                styleField(field); // Applies font, final columns(8), alignment, border
                gbc.gridx = i + 1;
                gbc.gridy = row;
                gbc.anchor = GridBagConstraints.CENTER;
                gbc.fill = GridBagConstraints.HORIZONTAL;
                gbc.weightx = 0.1;
                panel.add(field, gbc);

                String statKey = headers[i].toLowerCase().replace(" ", "");
                if (headers[i].equalsIgnoreCase("Fire Rate")) statKey = "fireRate";
                if (headers[i].equalsIgnoreCase("AOE Radius")) statKey = "aoeRadius";
                if (headers[i].equalsIgnoreCase("Build Cost")) statKey = "buildCost";
                if (headers[i].equalsIgnoreCase("Move Speed")) statKey = "moveSpeed";
                if (headers[i].equalsIgnoreCase("Gold Reward")) statKey = "goldReward";
                if (headers[i].equalsIgnoreCase("Hit Points")) statKey = "hitPoints";
                // For "Range", "Damage" the default toLowerCase() is fine.
                fields.put(statKey, field);
            }
            gbc.weightx = 0.0; // Reset weightx for the next row of type label or glue
            statFieldsMap.put(type, fields);
            row++;
        }

        gbc.gridx = 0; gbc.gridy = row; gbc.weighty = 1.0; gbc.gridwidth = headers.length + 1; gbc.fill = GridBagConstraints.VERTICAL;
        panel.add(Box.createVerticalGlue(), gbc);
        return panel;
    }
}
