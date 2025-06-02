package scenes;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
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

import gamestate.EditLevelStrategy;
import gamestate.LoadGameStrategy;
import helpMethods.LoadSave;
import helpMethods.ThumbnailCache;
import main.Game;
import managers.TileManager;

public class LoadGameMenu extends LevelSelectionScreen {
    private TileManager tileManager;
    private LoadGameStrategy loadGameStrategy;
    private EditLevelStrategy editLevelStrategy;
    private Font medodicaFontSmallBold;

    public LoadGameMenu(Game game) {
        super(game);
        this.tileManager = game.getTileManager();
        this.loadGameStrategy = new LoadGameStrategy();
        this.editLevelStrategy = new EditLevelStrategy();
        this.medodicaFontSmallBold = medodicaFontSmall.deriveFont(Font.BOLD);

        // Initialize the display
        displayLevels();
    }

    @Override
    protected ArrayList<String> getLevelsToDisplay() {
        return LoadSave.getLevelsWithSaveStates();
    }

    @Override
    protected String getScreenTitle() {
        return "Load Saved Game";
    }

    @Override
    protected void onLevelSelected(String levelName) {
        int[][] levelData = LoadSave.loadLevel(levelName);
        if (levelData != null) {
            showPlayEditDialog(levelName, levelData);
        }
    }

    @Override
    protected JPanel createMainContentPanel() {
        if (filteredLevels.isEmpty()) {
            return createNoSavesPanel();
        }

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

    private JPanel createNoSavesPanel() {
        JPanel noSavesPanel = new JPanel();
        noSavesPanel.setOpaque(false);
        noSavesPanel.setLayout(new BorderLayout());

        JLabel noSavesLabel = new JLabel("No saved games found", SwingConstants.CENTER);
        noSavesLabel.setFont(medodicaFontMedium.deriveFont(Font.BOLD, 20f));
        noSavesLabel.setForeground(Color.WHITE);

        JLabel instructionLabel = new JLabel("Play some levels first to create save states", SwingConstants.CENTER);
        instructionLabel.setFont(medodicaFontSmall);
        instructionLabel.setForeground(Color.LIGHT_GRAY);

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BorderLayout());
        textPanel.add(noSavesLabel, BorderLayout.CENTER);
        textPanel.add(instructionLabel, BorderLayout.SOUTH);

        noSavesPanel.add(textPanel, BorderLayout.CENTER);

        return noSavesPanel;
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
            thumbnail = generateThumbnailWithCache(levelName, levelData);
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

        // Save indicator
        JLabel saveLabel = new JLabel("💾 Saved Game", SwingConstants.CENTER);
        saveLabel.setFont(medodicaFontSmall.deriveFont(Font.ITALIC));
        saveLabel.setForeground(Color.GREEN);

        // Buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout());
        buttonsPanel.setOpaque(false);

        JButton loadButton = new JButton("Load");
        JButton editButton = new JButton("Edit");

        loadButton.setFont(mvBoliFontBold);
        editButton.setFont(mvBoliFontBold);

        // Style buttons
        styleButton(loadButton, new Color(70, 130, 200));
        styleButton(editButton, new Color(200, 130, 70));

        // Add action listeners
        loadButton.addActionListener(e -> loadGameStrategy.loadLevel(levelName, game));
        editButton.addActionListener(e -> editLevelStrategy.loadLevel(levelName, game));

        buttonsPanel.add(loadButton);
        buttonsPanel.add(editButton);

        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setOpaque(false);
        infoPanel.add(nameLabel, BorderLayout.NORTH);
        infoPanel.add(saveLabel, BorderLayout.CENTER);

        levelPanel.add(imageLabel, BorderLayout.CENTER);
        levelPanel.add(infoPanel, BorderLayout.NORTH);
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

    private void styleButton(JButton button, Color backgroundColor) {
        button.setBackground(backgroundColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setPreferredSize(new Dimension(60, 25));
    }

    private BufferedImage generateThumbnail(int[][] levelData) {
        BufferedImage thumbnail = new BufferedImage(PREVIEW_WIDTH, PREVIEW_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = thumbnail.createGraphics();

        // Handle cases where levelData might be problematic for rendering
        if (levelData == null || levelData.length == 0 || levelData[0].length == 0) {
            g2d.setColor(Color.DARK_GRAY);
            g2d.fillRect(0, 0, PREVIEW_WIDTH, PREVIEW_HEIGHT);
            g2d.setColor(Color.WHITE);
            g2d.setFont(medodicaFontSmallBold != null ? medodicaFontSmallBold : mvBoliFontBold);
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
            g2d.setFont(medodicaFontSmallBold != null ? medodicaFontSmallBold : mvBoliFontBold);
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

    private BufferedImage generateThumbnailWithCache(String levelName, int[][] levelData) {
        // Calculate hash of level data for cache validation
        int levelDataHash = java.util.Arrays.deepHashCode(levelData);

        // Try to get from cache first
        ThumbnailCache cache = ThumbnailCache.getInstance();
        BufferedImage cachedThumbnail = cache.getCachedThumbnail(levelName, levelDataHash);

        if (cachedThumbnail != null) {
            return cachedThumbnail;
        }

        // Cache miss - generate new thumbnail
        BufferedImage newThumbnail = generateThumbnail(levelData);

        // Cache the generated thumbnail
        cache.cacheThumbnail(levelName, newThumbnail, levelDataHash);

        return newThumbnail;
    }

    private void showPlayEditDialog(String levelName, int[][] levelData) {
        Object[] options = {"Load", "Edit", "Cancel"};
        UIManager.put("OptionPane.messageFont", mvBoliFontBold);
        UIManager.put("OptionPane.buttonFont", mvBoliFontBold);

        int choice = JOptionPane.showOptionDialog(this,
                "Map: " + levelName,
                "Load Saved Game",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null, options, options[0]);

        if (choice == JOptionPane.YES_OPTION) { // Load
            loadGameStrategy.loadLevel(levelName, game);
        } else if (choice == JOptionPane.NO_OPTION) { // Edit
            editLevelStrategy.loadLevel(levelName, game);
        }
    }

    /**
     * Refreshes the level list - called when returning to this screen
     */
    public void refreshLevels() {
        displayLevels();
    }
}