package ui_p;

import config.*;
import helpMethods.OptionsIO;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;

/**
 * UI component for editing game options.
 * This panel allows modifying all game settings that are stored in GameOptions.
 */
public class GameOptionsUI extends JPanel {

    private final GameOptions options;
    private final JTabbedPane tabbedPane;

    // General settings components
    private JTextField startingGoldField;
    private JTextField startingPlayerHPField;
    private JTextField interWaveDelayField;

    // Maps to store the various stat input fields
    private final Map<EnemyType, Map<String, JTextField>> enemyStatFields = new EnumMap<>(EnemyType.class);
    private final Map<TowerType, Map<String, JTextField>> towerStatFields = new EnumMap<>(TowerType.class);

    // Wave editor components
    private final List<WavePanel> wavePanels = new ArrayList<>();
    private JPanel wavesContainer;

    public GameOptionsUI() {
        this(OptionsIO.load());
    }

    public GameOptionsUI(GameOptions options) {
        this.options = options;
        setLayout(new BorderLayout());

        tabbedPane = new JTabbedPane();

        // Create the different tabs
        tabbedPane.addTab("General", createGeneralPanel());
        tabbedPane.addTab("Enemy Stats", createEnemyStatsPanel());
        tabbedPane.addTab("Tower Stats", createTowerStatsPanel());
        tabbedPane.addTab("Wave Config", createWavesPanel());

        add(tabbedPane, BorderLayout.CENTER);

        // Add buttons panel at the bottom
        add(createButtonPanel(), BorderLayout.SOUTH);

        // Load all values from the provided options
        loadValuesFromOptions();
    }

    /**
     * Creates the panel for general game settings
     */
    private JPanel createGeneralPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Starting Gold
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Starting Gold:"), gbc);

        gbc.gridx = 1;
        startingGoldField = new JTextField(10);
        panel.add(startingGoldField, gbc);

        // Starting Player HP
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Starting Player HP:"), gbc);

        gbc.gridx = 1;
        startingPlayerHPField = new JTextField(10);
        panel.add(startingPlayerHPField, gbc);

        // Inter-wave Delay
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Inter-wave Delay (s):"), gbc);

        gbc.gridx = 1;
        interWaveDelayField = new JTextField(10);
        panel.add(interWaveDelayField, gbc);

        // Add some glue to push everything to the top
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(Box.createVerticalGlue(), gbc);

        return panel;
    }

    /**
     * Creates the panel for enemy stats configuration
     */
    private JPanel createEnemyStatsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Headers
        gbc.gridx = 1;
        gbc.gridy = 0;
        panel.add(new JLabel("Hit Points"), gbc);

        gbc.gridx = 2;
        panel.add(new JLabel("Move Speed"), gbc);

        gbc.gridx = 3;
        panel.add(new JLabel("Gold Reward"), gbc);

        int row = 1;

        // Create input fields for each enemy type
        for (EnemyType type : EnemyType.values()) {
            Map<String, JTextField> fields = new HashMap<>();

            gbc.gridx = 0;
            gbc.gridy = row;
            panel.add(new JLabel(type.name()), gbc);

            gbc.gridx = 1;
            JTextField hpField = new JTextField(6);
            panel.add(hpField, gbc);
            fields.put("hitPoints", hpField);

            gbc.gridx = 2;
            JTextField moveSpeedField = new JTextField(6);
            panel.add(moveSpeedField, gbc);
            fields.put("moveSpeed", moveSpeedField);

            gbc.gridx = 3;
            JTextField goldRewardField = new JTextField(6);
            panel.add(goldRewardField, gbc);
            fields.put("goldReward", goldRewardField);

            enemyStatFields.put(type, fields);
            row++;
        }

        // Add some glue to push everything to the top
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weighty = 1.0;
        gbc.gridwidth = 4;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(Box.createVerticalGlue(), gbc);

        return panel;
    }

    /**
     * Creates the panel for tower stats configuration
     */
    private JPanel createTowerStatsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Headers
        gbc.gridx = 1;
        gbc.gridy = 0;
        panel.add(new JLabel("Build Cost"), gbc);

        gbc.gridx = 2;
        panel.add(new JLabel("Range"), gbc);

        gbc.gridx = 3;
        panel.add(new JLabel("Fire Rate"), gbc);

        gbc.gridx = 4;
        panel.add(new JLabel("AOE Radius"), gbc);

        gbc.gridx = 5;
        panel.add(new JLabel("Damage"), gbc);

        int row = 1;

        // Create input fields for each tower type
        for (TowerType type : TowerType.values()) {
            Map<String, JTextField> fields = new HashMap<>();

            gbc.gridx = 0;
            gbc.gridy = row;
            panel.add(new JLabel(type.name()), gbc);

            gbc.gridx = 1;
            JTextField buildCostField = new JTextField(6);
            panel.add(buildCostField, gbc);
            fields.put("buildCost", buildCostField);

            gbc.gridx = 2;
            JTextField rangeField = new JTextField(6);
            panel.add(rangeField, gbc);
            fields.put("range", rangeField);

            gbc.gridx = 3;
            JTextField fireRateField = new JTextField(6);
            panel.add(fireRateField, gbc);
            fields.put("fireRate", fireRateField);

            gbc.gridx = 4;
            JTextField aoeRadiusField = new JTextField(6);
            panel.add(aoeRadiusField, gbc);
            fields.put("aoeRadius", aoeRadiusField);

            gbc.gridx = 5;
            JTextField damageField = new JTextField(6);
            panel.add(damageField, gbc);
            fields.put("damage", damageField);

            towerStatFields.put(type, fields);
            row++;
        }

        // Add some glue to push everything to the top
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weighty = 1.0;
        gbc.gridwidth = 6;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(Box.createVerticalGlue(), gbc);

        return panel;
    }

    /**
     * Creates the panel for wave configuration
     */
    private JPanel createWavesPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Create a container for the wave panels with a vertical BoxLayout
        wavesContainer = new JPanel();
        wavesContainer.setLayout(new BoxLayout(wavesContainer, BoxLayout.Y_AXIS));

        // Create a scroll pane in case we have many waves
        JScrollPane scrollPane = new JScrollPane(wavesContainer);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Panel for buttons to add/remove waves
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addWaveButton = new JButton("Add Wave");
        addWaveButton.addActionListener(e -> addNewWave());
        buttonPanel.add(addWaveButton);

        mainPanel.add(buttonPanel, BorderLayout.NORTH);

        return mainPanel;
    }

    /**
     * Creates the bottom button panel with Save and Reset buttons
     */
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton resetButton = new JButton("Reset to Defaults");
        resetButton.addActionListener(e -> resetToDefaults());
        panel.add(resetButton);

        JButton saveButton = new JButton("Save");
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
        wavePanel.setBorder(BorderFactory.createTitledBorder("Wave " + (wavePanels.size() + 1)));

        wavePanels.add(wavePanel);
        wavesContainer.add(wavePanel);
        wavesContainer.revalidate();
        wavesContainer.repaint();
    }

    /**
     * Loads all values from the options object into the UI
     */
    private void loadValuesFromOptions() {
        // Load general settings
        startingGoldField.setText(String.valueOf(options.getStartingGold()));
        startingPlayerHPField.setText(String.valueOf(options.getStartingPlayerHP()));
        interWaveDelayField.setText(String.valueOf(options.getInterWaveDelay()));

        // Load enemy stats
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

        // Load tower stats
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

        // Load waves
        wavesContainer.removeAll();
        wavePanels.clear();

        List<Wave> waves = options.getWaves();
        for (int i = 0; i < waves.size(); i++) {
            Wave wave = waves.get(i);
            WavePanel wavePanel = new WavePanel(wave, i + 1);
            wavePanel.setBorder(BorderFactory.createTitledBorder("Wave " + (i + 1)));

            wavePanels.add(wavePanel);
            wavesContainer.add(wavePanel);
        }

        wavesContainer.revalidate();
        wavesContainer.repaint();
    }

    /**
     * Saves the UI values back to the options object and writes to disk
     */
    private void saveOptions() {
        try {
            // Save general settings
            options.setStartingGold(Integer.parseInt(startingGoldField.getText()));
            options.setStartingPlayerHP(Integer.parseInt(startingPlayerHPField.getText()));
            options.setInterWaveDelay(Double.parseDouble(interWaveDelayField.getText()));

            // Save enemy stats
            for (Map.Entry<EnemyType, Map<String, JTextField>> entry : enemyStatFields.entrySet()) {
                EnemyType type = entry.getKey();
                Map<String, JTextField> fields = entry.getValue();

                EnemyStats stats = options.getEnemyStats().getOrDefault(type, new EnemyStats());
                stats.setHitPoints(Integer.parseInt(fields.get("hitPoints").getText()));
                stats.setMoveSpeed(Double.parseDouble(fields.get("moveSpeed").getText()));
                stats.setGoldReward(Integer.parseInt(fields.get("goldReward").getText()));

                options.getEnemyStats().put(type, stats);
            }

            // Save tower stats
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

            // Save wave configurations
            List<Wave> waves = options.getWaves();
            waves.clear();
            for (WavePanel wavePanel : wavePanels) {
                waves.add(wavePanel.getWave());
            }

            // Write to disk
            OptionsIO.save(options);
            JOptionPane.showMessageDialog(this, "Settings saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Invalid number format in one of the fields: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this,
                    "Invalid value: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Reset all options to defaults and reload the UI
     */
    private void resetToDefaults() {
        int result = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to reset all settings to defaults?",
                "Confirm Reset", JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION) {
            OptionsIO.resetToDefaults();
            GameOptions defaults = OptionsIO.load(); // Reload from disk

            // Copy all values to our current options object
            options.setStartingGold(defaults.getStartingGold());
            options.setStartingPlayerHP(defaults.getStartingPlayerHP());
            options.setInterWaveDelay(defaults.getInterWaveDelay());

            options.getEnemyStats().clear();
            options.getEnemyStats().putAll(defaults.getEnemyStats());

            options.getTowerStats().clear();
            options.getTowerStats().putAll(defaults.getTowerStats());

            options.getWaves().clear();
            options.getWaves().addAll(defaults.getWaves());

            // Reload the UI
            loadValuesFromOptions();
        }
    }

    /**
     * Inner class to represent a Wave in the UI
     */
    private class WavePanel extends JPanel {
        private final Wave wave;
        private final int waveNumber;
        private final List<GroupPanel> groupPanels = new ArrayList<>();
        private JTextField intraGroupDelayField;
        private JPanel groupsContainer;

        public WavePanel(Wave wave, int waveNumber) {
            this.wave = wave;
            this.waveNumber = waveNumber;

            setLayout(new BorderLayout());

            // Wave properties panel
            JPanel propertiesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            propertiesPanel.add(new JLabel("Intra-Group Delay (s):"));
            intraGroupDelayField = new JTextField(String.valueOf(wave.getIntraGroupDelay()), 5);
            propertiesPanel.add(intraGroupDelayField);

            JButton removeWaveButton = new JButton("Remove Wave");
            removeWaveButton.addActionListener(e -> removeWave());
            propertiesPanel.add(removeWaveButton);

            add(propertiesPanel, BorderLayout.NORTH);

            // Groups container
            groupsContainer = new JPanel();
            groupsContainer.setLayout(new BoxLayout(groupsContainer, BoxLayout.Y_AXIS));

            JScrollPane groupsScrollPane = new JScrollPane(groupsContainer);
            groupsScrollPane.setPreferredSize(new Dimension(550, 200));
            add(groupsScrollPane, BorderLayout.CENTER);

            // Group control buttons
            JPanel groupButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JButton addGroupButton = new JButton("Add Group");
            addGroupButton.addActionListener(e -> addNewGroup());
            groupButtonsPanel.add(addGroupButton);

            add(groupButtonsPanel, BorderLayout.SOUTH);

            // Load existing groups
            for (Group group : wave.getGroups()) {
                GroupPanel groupPanel = new GroupPanel(group, groupPanels.size() + 1);
                groupPanels.add(groupPanel);
                groupsContainer.add(groupPanel);
            }
        }

        public Wave getWave() {
            // Update the wave with current UI values
            wave.setIntraGroupDelay(Double.parseDouble(intraGroupDelayField.getText()));

            List<Group> groups = wave.getGroups();
            groups.clear();

            for (GroupPanel panel : groupPanels) {
                groups.add(panel.getGroup());
            }

            return wave;
        }

        private void addNewGroup() {
            Group newGroup = new Group(new EnumMap<>(EnemyType.class), 0.5);
            wave.getGroups().add(newGroup);

            GroupPanel groupPanel = new GroupPanel(newGroup, groupPanels.size() + 1);
            groupPanels.add(groupPanel);
            groupsContainer.add(groupPanel);

            groupsContainer.revalidate();
            groupsContainer.repaint();
        }

        private void removeWave() {
            int result = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to remove Wave " + waveNumber + "?",
                    "Confirm Removal", JOptionPane.YES_NO_OPTION);

            if (result == JOptionPane.YES_OPTION) {
                options.getWaves().remove(wave);
                wavePanels.remove(this);
                wavesContainer.remove(this);

                // Renumber remaining waves
                for (int i = 0; i < wavePanels.size(); i++) {
                    WavePanel wp = wavePanels.get(i);
                    wp.updateWaveNumber(i + 1);
                }

                wavesContainer.revalidate();
                wavesContainer.repaint();
            }
        }

        public void updateWaveNumber(int newNumber) {
            setBorder(BorderFactory.createTitledBorder("Wave " + newNumber));
        }

        /**
         * Inner class to represent a Group in the UI
         */
        private class GroupPanel extends JPanel {
            private final Group group;
            private final int groupNumber;
            private JTextField intraEnemyDelayField;
            private Map<EnemyType, JTextField> enemyCountFields = new EnumMap<>(EnemyType.class);

            public GroupPanel(Group group, int groupNumber) {
                this.group = group;
                this.groupNumber = groupNumber;

                setBorder(BorderFactory.createTitledBorder("Group " + groupNumber));
                setLayout(new GridBagLayout());
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.insets = new Insets(3, 3, 3, 3);

                // Group properties
                gbc.gridx = 0;
                gbc.gridy = 0;
                add(new JLabel("Intra-Enemy Delay (s):"), gbc);

                gbc.gridx = 1;
                intraEnemyDelayField = new JTextField(String.valueOf(group.getIntraEnemyDelay()), 5);
                add(intraEnemyDelayField, gbc);

                // Enemy counts
                gbc.gridx = 0;
                gbc.gridy = 1;
                add(new JLabel("Enemy Composition:"), gbc);

                JPanel compositionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

                for (EnemyType type : EnemyType.values()) {
                    compositionPanel.add(new JLabel(type.name() + ":"));

                    JTextField countField = new JTextField(5);
                    countField.setText(String.valueOf(group.getComposition().getOrDefault(type, 0)));
                    compositionPanel.add(countField);

                    enemyCountFields.put(type, countField);
                }

                gbc.gridx = 1;
                gbc.gridwidth = 2;
                add(compositionPanel, gbc);

                // Remove button
                gbc.gridx = 3;
                gbc.gridy = 0;
                gbc.gridwidth = 1;
                JButton removeButton = new JButton("Remove");
                removeButton.addActionListener(e -> removeGroup());
                add(removeButton, gbc);
            }

            public Group getGroup() {
                // Update the group with current UI values
                group.setIntraEnemyDelay(Double.parseDouble(intraEnemyDelayField.getText()));

                Map<EnemyType, Integer> composition = group.getComposition();
                composition.clear();

                for (Map.Entry<EnemyType, JTextField> entry : enemyCountFields.entrySet()) {
                    EnemyType type = entry.getKey();
                    int count = Integer.parseInt(entry.getValue().getText());

                    if (count > 0) {
                        composition.put(type, count);
                    }
                }

                return group;
            }

            private void removeGroup() {
                int result = JOptionPane.showConfirmDialog(this,
                        "Are you sure you want to remove Group " + groupNumber + "?",
                        "Confirm Removal", JOptionPane.YES_NO_OPTION);

                if (result == JOptionPane.YES_OPTION) {
                    wave.getGroups().remove(group);
                    groupPanels.remove(this);
                    groupsContainer.remove(this);

                    // Renumber remaining groups
                    for (int i = 0; i < groupPanels.size(); i++) {
                        GroupPanel gp = groupPanels.get(i);
                        gp.updateGroupNumber(i + 1);
                    }

                    groupsContainer.revalidate();
                    groupsContainer.repaint();
                }
            }

            public void updateGroupNumber(int newNumber) {
                setBorder(BorderFactory.createTitledBorder("Group " + newNumber));
            }
        }
    }

    /**
     * Returns the current game options
     */
    public GameOptions getGameOptions() {
        return options;
    }
}
