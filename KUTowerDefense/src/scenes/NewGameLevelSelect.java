package scenes;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import helpMethods.LoadSave;
import helpMethods.ThumbnailCache;
import main.Game;
import managers.TileManager;
import strategies.EditLevelStrategy;
import strategies.NewGameStrategy;

public class NewGameLevelSelect extends LevelSelectionScreen {

    private NewGameStrategy newGameStrategy;
    private EditLevelStrategy editLevelStrategy;
    private TileManager tileManager;

    public NewGameLevelSelect(Game game) {
        super(game);
        this.newGameStrategy = new NewGameStrategy();
        this.editLevelStrategy = new EditLevelStrategy();
        this.tileManager = game.getTileManager();

        // Initialize the display
        displayLevels();
    }

    @Override
    protected ArrayList<String> getLevelsToDisplay() {
        return LoadSave.getAllAvailableLevels();
    }

    @Override
    protected String getScreenTitle() {
        return "Select Level - New Game";
    }

    @Override
    protected void onLevelSelected(String levelName) {
        // Show options: Play or Edit
        showLevelOptionsDialog(levelName);
    }

    @Override
    protected JPanel createMainContentPanel() {
        JPanel contentPanel = new JPanel();
        contentPanel.setOpaque(false);
        contentPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(PREVIEW_MARGIN, PREVIEW_MARGIN, PREVIEW_MARGIN, PREVIEW_MARGIN);

        // Calculate which levels to show on current page
        int startIndex = currentPage * PREVIEWS_PER_PAGE;
        int endIndex = Math.min(startIndex + PREVIEWS_PER_PAGE, filteredLevels.size());

        int row = 0, col = 0;

        for (int i = startIndex; i < endIndex; i++) {
            String levelName = filteredLevels.get(i);
            JPanel levelPanel = createLevelPanel(levelName);

            gbc.gridx = col;
            gbc.gridy = row;
            gbc.anchor = GridBagConstraints.CENTER;

            contentPanel.add(levelPanel, gbc);

            col++;
            if (col >= PREVIEWS_PER_ROW) {
                col = 0;
                row++;
            }
        }

        // Add "Create New Level" button
        if (currentPage == 0) { // Only show on first page
            JPanel createNewPanel = createNewLevelPanel();

            gbc.gridx = col;
            gbc.gridy = row;
            gbc.anchor = GridBagConstraints.CENTER;

            contentPanel.add(createNewPanel, gbc);
        }

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(scrollPane, BorderLayout.CENTER);

        return wrapper;
    }

    private JPanel createLevelPanel(String levelName) {
        JPanel levelPanel = new JPanel();
        levelPanel.setLayout(new BorderLayout());
        levelPanel.setPreferredSize(new Dimension(PREVIEW_WIDTH + 40, PREVIEW_HEIGHT + 80));
        levelPanel.setOpaque(false);
        levelPanel.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));

        // Level preview image
        BufferedImage thumbnail = null;
        int[][] levelData = LoadSave.loadLevel(levelName);
        if (levelData != null) {
            int levelDataHash = java.util.Arrays.deepHashCode(levelData);
            thumbnail = ThumbnailCache.getInstance().getCachedThumbnail(levelName, levelDataHash);

            // If not cached, generate thumbnail (similar to LoadGameMenu)
            if (thumbnail == null) {
                thumbnail = generateThumbnail(levelData);
                ThumbnailCache.getInstance().cacheThumbnail(levelName, thumbnail, levelDataHash);
            }
        }

        JLabel imageLabel = new JLabel();
        if (thumbnail != null) {
            imageLabel.setIcon(new ImageIcon(thumbnail));
        } else {
            imageLabel.setText("No Preview");
            imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
            imageLabel.setForeground(Color.WHITE);
        }
        imageLabel.setPreferredSize(new Dimension(PREVIEW_WIDTH, PREVIEW_HEIGHT));

        // Level name label
        JLabel nameLabel = new JLabel(levelName, SwingConstants.CENTER);
        nameLabel.setFont(medodicaFontSmall);
        nameLabel.setForeground(Color.WHITE);

        // Buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout());
        buttonsPanel.setOpaque(false);

        JButton playButton = new JButton("Play");
        JButton editButton = new JButton("Edit");

        playButton.setFont(mvBoliFontBold);
        editButton.setFont(mvBoliFontBold);

        // Style buttons
        styleButton(playButton, new Color(70, 130, 200));
        styleButton(editButton, new Color(200, 130, 70));

        // Add action listeners
        playButton.addActionListener(e -> newGameStrategy.loadLevel(levelName, game));
        editButton.addActionListener(e -> editLevelStrategy.loadLevel(levelName, game));

        buttonsPanel.add(playButton);
        buttonsPanel.add(editButton);

        levelPanel.add(imageLabel, BorderLayout.CENTER);
        levelPanel.add(nameLabel, BorderLayout.NORTH);
        levelPanel.add(buttonsPanel, BorderLayout.SOUTH);

        // Add hover effect
        levelPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                levelPanel.setBorder(BorderFactory.createLineBorder(Color.YELLOW, 2));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                levelPanel.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));
            }
        });

        return levelPanel;
    }

    private JPanel createNewLevelPanel() {
        JPanel newLevelPanel = new JPanel();
        newLevelPanel.setLayout(new BorderLayout());
        newLevelPanel.setPreferredSize(new Dimension(PREVIEW_WIDTH + 40, PREVIEW_HEIGHT + 80));
        newLevelPanel.setOpaque(false);
        newLevelPanel.setBorder(BorderFactory.createLineBorder(Color.GREEN, 2));

        // Plus icon or text
        JLabel createLabel = new JLabel("+ Create New Level", SwingConstants.CENTER);
        createLabel.setFont(medodicaFontMedium);
        createLabel.setForeground(Color.GREEN);

        JButton createButton = new JButton("Create");
        createButton.setFont(mvBoliFontBold);
        styleButton(createButton, new Color(70, 200, 70));

        createButton.addActionListener(e -> {
            // Open map editor with a blank level
            game.changeGameState(main.GameStates.EDIT);
        });

        newLevelPanel.add(createLabel, BorderLayout.CENTER);
        newLevelPanel.add(createButton, BorderLayout.SOUTH);

        // Add hover effect
        newLevelPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                newLevelPanel.setBorder(BorderFactory.createLineBorder(Color.CYAN, 2));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                newLevelPanel.setBorder(BorderFactory.createLineBorder(Color.GREEN, 2));
            }
        });

        return newLevelPanel;
    }

    private void styleButton(JButton button, Color backgroundColor) {
        button.setBackground(backgroundColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setPreferredSize(new Dimension(60, 25));
    }

    private void showLevelOptionsDialog(String levelName) {
        Object[] options = {"Play", "Edit", "Cancel"};
        UIManager.put("OptionPane.messageFont", mvBoliFontBold);
        UIManager.put("OptionPane.buttonFont", mvBoliFontBold);

        int choice = JOptionPane.showOptionDialog(
                this,
                "Choose action for level: " + levelName,
                "Level Options",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );

        switch (choice) {
            case JOptionPane.YES_OPTION: // Play
                newGameStrategy.loadLevel(levelName, game);
                break;
            case JOptionPane.NO_OPTION: // Edit
                editLevelStrategy.loadLevel(levelName, game);
                break;
            case JOptionPane.CANCEL_OPTION:
            default:
                // Do nothing
                break;
        }
    }

    /**
     * Refresh the level list (call this when returning from editor)
     */
    public void refreshLevels() {
        displayLevels();
    }

    private BufferedImage generateThumbnail(int[][] levelData) {
        BufferedImage thumbnail = new BufferedImage(PREVIEW_WIDTH, PREVIEW_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = thumbnail.createGraphics();

        // Handle cases where levelData might be problematic for rendering
        if (levelData == null || levelData.length == 0 || levelData[0].length == 0) {
            g2d.setColor(Color.DARK_GRAY);
            g2d.fillRect(0, 0, PREVIEW_WIDTH, PREVIEW_HEIGHT);
            g2d.setColor(Color.WHITE);
            g2d.setFont(medodicaFontSmall != null ? medodicaFontSmall : mvBoliFontBold);
            String noDataText = "No Data";
            FontMetrics fm = g2d.getFontMetrics();
            g2d.drawString(noDataText, (PREVIEW_WIDTH - fm.stringWidth(noDataText)) / 2, PREVIEW_HEIGHT / 2 + fm.getAscent() / 2);
            g2d.dispose();
            return thumbnail;
        }

        int numRows = levelData.length;
        int numCols = levelData[0].length;

        if (numRows <= 0 || numCols <= 0) {
            g2d.setColor(Color.DARK_GRAY);
            g2d.fillRect(0, 0, PREVIEW_WIDTH, PREVIEW_HEIGHT);
            g2d.setColor(Color.WHITE);
            g2d.setFont(medodicaFontSmall != null ? medodicaFontSmall : mvBoliFontBold);
            String errorText = "Invalid Map";
            FontMetrics fm = g2d.getFontMetrics();
            g2d.drawString(errorText, (PREVIEW_WIDTH - fm.stringWidth(errorText)) / 2, PREVIEW_HEIGHT / 2 + fm.getAscent() / 2);
            g2d.dispose();
            return thumbnail;
        }

        float tileRenderWidth = (float) PREVIEW_WIDTH / numCols;
        float tileRenderHeight = (float) PREVIEW_HEIGHT / numRows;

        // Step 1: Fill the entire thumbnail with grass tiles
        int grassTileId = 5; // ID for grass tile
        BufferedImage grassSprite = tileManager.getSprite(grassTileId);

        if (grassSprite != null) {
            for (int r = 0; r < numRows; r++) {
                for (int c = 0; c < numCols; c++) {
                    g2d.drawImage(grassSprite,
                            (int) (c * tileRenderWidth), (int) (r * tileRenderHeight),
                            (int) Math.ceil(tileRenderWidth), (int) Math.ceil(tileRenderHeight), null);
                }
            }
        } else {
            // Fallback if grass sprite is somehow unavailable: fill with a green color
            g2d.setColor(new Color(34, 139, 34)); // Forest green
            g2d.fillRect(0, 0, PREVIEW_WIDTH, PREVIEW_HEIGHT);
        }

        // Step 2: Render the actual map data on top of the grass
        for (int r = 0; r < numRows; r++) {
            for (int c = 0; c < numCols; c++) {
                int tileId = levelData[r][c];

                if (tileId == grassTileId && grassSprite != null) {
                    continue;
                }

                BufferedImage tileSprite = tileManager.getSprite(tileId);
                if (tileSprite != null) {
                    g2d.drawImage(tileSprite, (int) (c * tileRenderWidth), (int) (r * tileRenderHeight),
                            (int) Math.ceil(tileRenderWidth), (int) Math.ceil(tileRenderHeight), null);
                } else if (tileId != grassTileId) {
                    g2d.setColor(new Color(30,30,30));
                    g2d.fillRect((int) (c * tileRenderWidth), (int) (r * tileRenderHeight),
                            (int) Math.ceil(tileRenderWidth), (int) Math.ceil(tileRenderHeight));
                }
            }
        }
        g2d.dispose();
        return thumbnail;
    }
}