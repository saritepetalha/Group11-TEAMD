package scenes;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import helpMethods.FontLoader;
import helpMethods.LoadSave;
import main.Game;
import main.GameStates;
import managers.TileManager;
import ui_p.ButtonAssets;
import constants.GameDimensions;

public class LoadGameMenu extends JPanel { // Changed to JPanel
    private Game game;
    private TileManager tileManager;
    private BufferedImage backgroundImg;
    private JButton backButton; // Changed back to JButton

    private static final int PREVIEW_WIDTH = 192;
    private static final int PREVIEW_HEIGHT = 108;
    private static final int PREVIEW_MARGIN = 80;
    private static final int PREVIEWS_PER_ROW = 3;

    private Font medodicaFontSmall;
    private Font medodicaFontSmallBold;
    private Font medodicaFontMedium;
    private Font mvBoliFontBold;

    // Colors for the back button, matching GameOptionsUI
    private final Color BUTTON_BG_COLOR = new Color(60, 120, 190);
    private final Color BUTTON_TEXT_COLOR = Color.WHITE;
    private final Color BUTTON_HOVER_BG_COLOR = new Color(80, 140, 210);
    private final Color BUTTON_BORDER_COLOR = new Color(40, 80, 140);


    public LoadGameMenu(Game game) {
        this.game = game;
        this.tileManager = game.getTileManager();
        this.backgroundImg = ButtonAssets.loadGameMenuBackgroundImg;
        this.medodicaFontSmall = FontLoader.loadMedodicaFont(14f);
        this.medodicaFontSmallBold = FontLoader.loadMedodicaFont(14f).deriveFont(Font.BOLD);
        this.medodicaFontMedium = FontLoader.loadMedodicaFont(16f);
        this.mvBoliFontBold = new Font("MV Boli", Font.BOLD, 14);

        setPreferredSize(new Dimension(GameDimensions.MAIN_MENU_SCREEN_WIDTH, GameDimensions.MAIN_MENU_SCREEN_HEIGHT));
        setLayout(new BorderLayout());

        initUI();
    }

    private void initUI() {
        JPanel previewsContainer = new JPanel();
        previewsContainer.setOpaque(false);
        previewsContainer.setLayout(new GridLayout(2, 2, PREVIEW_MARGIN, PREVIEW_MARGIN));
        previewsContainer.setBorder(BorderFactory.createEmptyBorder(PREVIEW_MARGIN, PREVIEW_MARGIN, PREVIEW_MARGIN, PREVIEW_MARGIN));

        ArrayList<String> savedLevels = LoadSave.getSavedLevels();
        if (savedLevels.isEmpty()) {
            JLabel noLevelsLabel = new JLabel("No saved levels found. Create a map in Edit Mode!");
            noLevelsLabel.setFont(medodicaFontMedium);
            noLevelsLabel.setForeground(Color.WHITE);
            noLevelsLabel.setHorizontalAlignment(SwingConstants.CENTER);
            JPanel centerPanel = new JPanel(new GridBagLayout());
            centerPanel.setOpaque(false);
            centerPanel.add(noLevelsLabel);
            previewsContainer.setLayout(new BorderLayout());
            previewsContainer.add(centerPanel, BorderLayout.CENTER);
        } else {
            for (String levelName : savedLevels) {
                int[][] levelData = LoadSave.loadLevel(levelName);
                if (levelData != null) {
                    BufferedImage thumbnail = generateThumbnail(levelData);
                    RoundedButton previewButton = new RoundedButton("");
                    previewButton.setIcon(new ImageIcon(thumbnail));
                    previewButton.setFont(medodicaFontSmall);
                    previewButton.setToolTipText("Load " + levelName);
                    previewButton.setPreferredSize(new Dimension(PREVIEW_WIDTH, PREVIEW_HEIGHT));

                    JPanel buttonPanel = new JPanel(new BorderLayout());
                    buttonPanel.setOpaque(false);
                    buttonPanel.setPreferredSize(new Dimension(PREVIEW_WIDTH, PREVIEW_HEIGHT + 25));

                    JLabel nameLabel = new JLabel(levelName, SwingConstants.CENTER);
                    nameLabel.setFont(medodicaFontSmallBold);
                    nameLabel.setForeground(Color.black);
                    nameLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

                    buttonPanel.add(previewButton, BorderLayout.CENTER);
                    buttonPanel.add(nameLabel, BorderLayout.SOUTH);

                    previewButton.addActionListener(e -> showPlayEditDialog(levelName, levelData));

                    JPanel cellWrapperPanel = new JPanel(new GridBagLayout());
                    cellWrapperPanel.setOpaque(false);
                    cellWrapperPanel.add(buttonPanel);

                    previewsContainer.add(cellWrapperPanel);
                }
            }
        }

        JScrollPane scrollPane = new JScrollPane(previewsContainer);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        // Adjust scroll speed
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);

        // Back button
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(5,0,10,0)); // Add some padding to the bottom panel
        backButton = new JButton("Back to Menu"); // Standard JButton
        styleButton(backButton);
        backButton.addActionListener(e -> game.changeGameState(GameStates.MENU));
        bottomPanel.add(backButton);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void styleButton(JButton button) {
        button.setFont(medodicaFontMedium);
        button.setBackground(BUTTON_BG_COLOR);
        button.setForeground(BUTTON_TEXT_COLOR);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BUTTON_BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(5, 15, 5, 15) // Padding
        ));

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(BUTTON_HOVER_BG_COLOR);
            }

            public void mouseExited(MouseEvent evt) {
                button.setBackground(BUTTON_BG_COLOR);
            }
        });
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

        // If numRows or numCols is zero, trying to divide by them will cause an error.
        // Also, if they are too large, individual tiles will be too small.
        // For thumbnail generation, we can define a maximum number of tiles to render
        // or simply fall back to a solid color if the map is too dense or empty for a good preview.
        if (numRows <= 0 || numCols <= 0) { // Added check for zero rows/cols
            g2d.setColor(Color.DARK_GRAY); // Fallback for invalid dimensions
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

                if (tileId == grassTileId && grassSprite != null) { // If it's a grass tile, and we already drew grass, skip.
                    continue;
                }

                BufferedImage tileSprite = tileManager.getSprite(tileId);
                if (tileSprite != null) {
                    g2d.drawImage(tileSprite, (int) (c * tileRenderWidth), (int) (r * tileRenderHeight),
                            (int) Math.ceil(tileRenderWidth), (int) Math.ceil(tileRenderHeight), null);
                } else if (tileId != grassTileId) {
                    g2d.setColor(new Color(30,30,30)); // Dark fallback for missing non-grass sprites
                    g2d.fillRect((int) (c * tileRenderWidth), (int) (r * tileRenderHeight),
                            (int) Math.ceil(tileRenderWidth), (int) Math.ceil(tileRenderHeight));
                }
            }
        }
        g2d.dispose();
        return thumbnail;
    }

    private void showPlayEditDialog(String levelName, int[][] levelData) {
        Object[] options = {"Play", "Edit", "Cancel"};
        UIManager.put("OptionPane.messageFont", mvBoliFontBold);
        UIManager.put("OptionPane.buttonFont", mvBoliFontBold);

        int choice = JOptionPane.showOptionDialog(this.game,
                "Map: " + levelName,
                "Load Option",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null, options, options[0]);

        if (choice == JOptionPane.YES_OPTION) { // Play
            int[][] overlay = LoadSave.loadOverlay(levelName);
            if (overlay == null) {
                overlay = new int[levelData.length][levelData[0].length];
                if (levelData.length > 4 && levelData[0].length > 15) { // Default start/end
                    overlay[4][0] = 1;
                    overlay[4][15] = 2;
                }
            }
            game.startPlayingWithLevel(levelName, levelData, overlay);
            game.changeGameState(GameStates.PLAYING);
        } else if (choice == JOptionPane.NO_OPTION) { // Edit
            game.getMapEditing().setLevel(levelData);
            int[][] overlayForEdit = LoadSave.loadOverlay(levelName);
            if (overlayForEdit == null) {
                overlayForEdit = new int[levelData.length][levelData[0].length];
            }
            game.getMapEditing().setOverlayData(overlayForEdit);
            game.getMapEditing().setCurrentLevelName(levelName);
            game.changeGameState(GameStates.EDIT);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImg != null) {
            // Scale background to fit 640x500 (or panel size)
            g.drawImage(backgroundImg, 0, 0, getWidth(), getHeight(), this);
        } else {
            g.setColor(Color.BLACK); // Fallback
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    // Custom JButton for rounded corners
    private static class RoundedButton extends JButton {
        private int cornerRadius = 15;
        private Shape shape;

        public RoundedButton(String text) {
            super(text);
            setContentAreaFilled(false); // We'll paint our own background
            setBorderPainted(false);     // No standard border
            setFocusPainted(false);      // No focus border
            setForeground(Color.WHITE);  // Default text color
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Paint background (rounded rectangle)
            if (getModel().isArmed()) { // Pressed
                g2.setColor(Color.DARK_GRAY.darker());
            } else if (getModel().isRollover()) { // Hover
                g2.setColor(Color.GRAY.brighter());
            } else {
                g2.setColor(Color.GRAY);
            }
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius));

            super.paintComponent(g2);

            g2.dispose();
        }

        // paint a rounded border
        @Override
        protected void paintBorder(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.BLACK); // Border color
            g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius));
            g2.dispose();
        }

        @Override
        public boolean contains(int x, int y) {
            if (shape == null || !shape.getBounds().equals(getBounds())) {
                shape = new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);
            }
            return shape.contains(x, y);
        }

    }
}