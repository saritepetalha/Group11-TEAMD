package scenes;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import constants.GameDimensions;
import helpMethods.FontLoader;
import helpMethods.LoadSave;
import helpMethods.ThumbnailCache;
import levelselection.LevelSelectionStrategy;
import main.Game;
import main.GameStates;
import managers.TileManager;
import ui_p.AssetsLoader;
import ui_p.TheButton;

public class LevelSelectionScene extends JPanel {
    private Game game;
    private TileManager tileManager;
    private BufferedImage backgroundImg;
    private TheButton backButton;
    private TheButton editLevelButton;
    private LevelSelectionStrategy strategy;

    private static final int PREVIEW_WIDTH = 192;
    private static final int PREVIEW_HEIGHT = 108;
    private static final int PREVIEW_MARGIN = 15;
    private static final int PREVIEWS_PER_ROW = 2;
    private static final int PREVIEWS_PER_PAGE = 4; // 2 columns x 2 rows
    private static final int HEADER_HEIGHT = 100;
    private static final int FOOTER_HEIGHT = 80;

    private Font medodicaFontSmall;
    private Font medodicaFontMedium;
    private Font medodicaFontLarge;

    // Pagination variables
    private int currentPage = 0;
    private int totalPages = 0;
    private ArrayList<String> availableLevels = new ArrayList<>();
    private JButton prevPageButton;
    private JButton nextPageButton;
    private JPanel mainContentPanel;

    // Colors
    private final Color PAGE_BUTTON_BG_COLOR = new Color(70, 130, 200);
    private final Color PAGE_BUTTON_HOVER_COLOR = new Color(90, 150, 220);
    private final Color PAGE_BUTTON_DISABLED_COLOR = new Color(120, 120, 120);
    private final Color TITLE_COLOR = new Color(255, 255, 255);
    private final Color DESCRIPTION_COLOR = new Color(200, 200, 200);

    public LevelSelectionScene(Game game, LevelSelectionStrategy strategy) {
        this.game = game;
        this.strategy = strategy;
        this.tileManager = game.getTileManager();
        this.backgroundImg = AssetsLoader.getInstance().loadGameMenuBackgroundImg;

        // Load fonts
        this.medodicaFontSmall = FontLoader.loadMedodicaFont(14f);
        this.medodicaFontMedium = FontLoader.loadMedodicaFont(18f);
        this.medodicaFontLarge = FontLoader.loadMedodicaFont(24f);

        setPreferredSize(new Dimension(GameDimensions.MAIN_MENU_SCREEN_WIDTH, GameDimensions.MAIN_MENU_SCREEN_HEIGHT));
        setLayout(new BorderLayout());

        initUI();
    }

    public void refreshLevelList() {
        availableLevels = strategy.getLevelsToShow();
        totalPages = Math.max(1, (int) Math.ceil((double) availableLevels.size() / PREVIEWS_PER_PAGE));

        // Reset to first page if current page is out of bounds
        if (currentPage >= totalPages) {
            currentPage = Math.max(0, totalPages - 1);
        }

        // Remove the current main content
        if (mainContentPanel != null) {
            remove(mainContentPanel);
        }

        // Recreate the UI content
        createMainContent();

        // Refresh the display
        revalidate();
        repaint();
    }

    private void initUI() {
        // Load levels using strategy
        availableLevels = strategy.getLevelsToShow();
        totalPages = Math.max(1, (int) Math.ceil((double) availableLevels.size() / PREVIEWS_PER_PAGE));
        currentPage = 0;

        createMainContent();
        createNavigationButtons();
    }

    private void createMainContent() {
        mainContentPanel = new JPanel(new BorderLayout());
        mainContentPanel.setOpaque(false);

        // Create header with title and description
        JPanel headerPanel = createHeaderPanel();
        mainContentPanel.add(headerPanel, BorderLayout.NORTH);

        if (availableLevels.isEmpty()) {
            createNoLevelsPanel();
        } else {
            createCurrentPageGrid();
        }

        add(mainContentPanel, BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new GridBagLayout());
        headerPanel.setOpaque(false);
        headerPanel.setPreferredSize(new Dimension(GameDimensions.MAIN_MENU_SCREEN_WIDTH, HEADER_HEIGHT));

        GridBagConstraints gbc = new GridBagConstraints();

        // Title
        JLabel titleLabel = new JLabel(strategy.getSelectionTitle(), SwingConstants.CENTER);
        titleLabel.setFont(medodicaFontLarge.deriveFont(Font.BOLD));
        titleLabel.setForeground(TITLE_COLOR);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(20, 0, 5, 0);
        headerPanel.add(titleLabel, gbc);

        // Description
        JLabel descLabel = new JLabel(strategy.getSelectionDescription(), SwingConstants.CENTER);
        descLabel.setFont(medodicaFontMedium);
        descLabel.setForeground(DESCRIPTION_COLOR);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 20, 0);
        headerPanel.add(descLabel, gbc);

        return headerPanel;
    }

    private void createNoLevelsPanel() {
        String message = availableLevels.isEmpty() && strategy instanceof levelselection.SavedLevelsOnlyStrategy
                ? "No saved games found. Start a new game first!"
                : "No levels found. Create a map in Edit Mode!";

        JLabel noLevelsLabel = new JLabel(message);
        noLevelsLabel.setFont(medodicaFontMedium);
        noLevelsLabel.setForeground(Color.WHITE);
        noLevelsLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);
        centerPanel.add(noLevelsLabel);

        mainContentPanel.add(centerPanel, BorderLayout.CENTER);
    }

    private void createCurrentPageGrid() {
        // Calculate which levels to show on current page
        int startIndex = currentPage * PREVIEWS_PER_PAGE;
        int endIndex = Math.min(startIndex + PREVIEWS_PER_PAGE, availableLevels.size());

        // Create a panel that will hold the map previews for current page
        JPanel previewsContainer = new JPanel(new GridBagLayout());
        previewsContainer.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(PREVIEW_MARGIN, PREVIEW_MARGIN, PREVIEW_MARGIN, PREVIEW_MARGIN);
        gbc.anchor = GridBagConstraints.CENTER;

        int currentRow = 0;
        int currentCol = 0;

        for (int i = startIndex; i < endIndex; i++) {
            String levelName = availableLevels.get(i);
            int[][] levelData = LoadSave.loadLevel(levelName);

            if (levelData != null) {
                BufferedImage thumbnail = generateThumbnailWithCache(levelName, levelData);
                JPanel levelPanel = createLevelPanel(levelName, thumbnail, levelData);

                // Set grid position
                gbc.gridx = currentCol;
                gbc.gridy = currentRow;

                previewsContainer.add(levelPanel, gbc);

                // Move to next position
                currentCol++;
                if (currentCol >= PREVIEWS_PER_ROW) {
                    currentCol = 0;
                    currentRow++;
                }
            }
        }

        // Wrap the previews in a scrollable panel if needed
        JScrollPane scrollPane = new JScrollPane(previewsContainer);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        mainContentPanel.add(scrollPane, BorderLayout.CENTER);

        // Add pagination controls if needed
        if (totalPages > 1) {
            createPaginationPanel();
        }
    }

    private JPanel createLevelPanel(String levelName, BufferedImage thumbnail, int[][] levelData) {
        JPanel levelPanel = new JPanel(new BorderLayout());
        levelPanel.setOpaque(false);
        levelPanel.setPreferredSize(new Dimension(PREVIEW_WIDTH, PREVIEW_HEIGHT + 60));

        // Create the main preview button
        RoundedButton previewButton = new RoundedButton("");
        previewButton.setIcon(new ImageIcon(thumbnail));
        previewButton.setPreferredSize(new Dimension(PREVIEW_WIDTH, PREVIEW_HEIGHT));
        previewButton.setToolTipText("Play " + levelName);
        previewButton.setBackground(new Color(0, 0, 0, 0)); // Transparent background
        previewButton.setBorder(null); // Remove any border

        // Add click handler for playing the level
        previewButton.addActionListener(e -> playLevel(levelName, levelData));

        // Create button panel for edit/play actions
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        buttonPanel.setOpaque(false);

        // Edit button (always show for level editing)
        JButton editButton = new JButton("Edit");
        editButton.setFont(medodicaFontSmall);
        editButton.setPreferredSize(new Dimension(60, 25));
        editButton.addActionListener(e -> editLevel(levelName, levelData));
        styleSmallButton(editButton, new Color(200, 120, 50));

        // Play button
        JButton playButton = new JButton("Play");
        playButton.setFont(medodicaFontSmall);
        playButton.setPreferredSize(new Dimension(60, 25));
        playButton.addActionListener(e -> playLevel(levelName, levelData));
        styleSmallButton(playButton, new Color(50, 150, 50));

        buttonPanel.add(playButton);
        buttonPanel.add(editButton);

        // Level name label
        JLabel nameLabel = new JLabel(levelName, SwingConstants.CENTER);
        nameLabel.setFont(medodicaFontSmall.deriveFont(Font.BOLD));
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        levelPanel.add(previewButton, BorderLayout.CENTER);
        levelPanel.add(nameLabel, BorderLayout.SOUTH);
        levelPanel.add(buttonPanel, BorderLayout.PAGE_END);

        return levelPanel;
    }

    private void styleSmallButton(JButton button, Color bgColor) {
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bgColor.brighter());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });
    }

    private void createPaginationPanel() {
        JPanel paginationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        paginationPanel.setOpaque(false);

        prevPageButton = new JButton("◀ Previous");
        nextPageButton = new JButton("Next ▶");

        stylePageButton(prevPageButton);
        stylePageButton(nextPageButton);

        prevPageButton.addActionListener(e -> {
            if (currentPage > 0) {
                currentPage--;
                refreshCurrentPage();
            }
        });

        nextPageButton.addActionListener(e -> {
            if (currentPage < totalPages - 1) {
                currentPage++;
                refreshCurrentPage();
            }
        });

        // Page indicator
        JLabel pageIndicator = new JLabel((currentPage + 1) + " / " + totalPages);
        pageIndicator.setFont(medodicaFontMedium);
        pageIndicator.setForeground(Color.WHITE);
        pageIndicator.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

        paginationPanel.add(prevPageButton);
        paginationPanel.add(pageIndicator);
        paginationPanel.add(nextPageButton);

        mainContentPanel.add(paginationPanel, BorderLayout.SOUTH);
    }

    private void stylePageButton(JButton button) {
        button.setFont(medodicaFontSmall);
        button.setBackground(PAGE_BUTTON_BG_COLOR);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(100, 35));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(PAGE_BUTTON_HOVER_COLOR);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(PAGE_BUTTON_BG_COLOR);
                } else {
                    button.setBackground(PAGE_BUTTON_DISABLED_COLOR);
                }
            }
        });
    }

    private void refreshCurrentPage() {
        // Remove the current main content
        if (mainContentPanel != null) {
            remove(mainContentPanel);
        }

        // Recreate the UI content
        createMainContent();

        // Refresh the display
        revalidate();
        repaint();
    }

    private void createNavigationButtons() {
        JPanel navigationPanel = new JPanel(new BorderLayout());
        navigationPanel.setOpaque(false);
        navigationPanel.setPreferredSize(new Dimension(GameDimensions.MAIN_MENU_SCREEN_WIDTH, FOOTER_HEIGHT));

        // Back button
        int buttonWidth = 120;
        int buttonHeight = 40;
        backButton = new TheButton("Back", 20, 20, buttonWidth, buttonHeight);

        JPanel backWrapper = createTheButtonWrapper(backButton);
        navigationPanel.add(backWrapper, BorderLayout.WEST);

        add(navigationPanel, BorderLayout.SOUTH);
    }

    private JPanel createTheButtonWrapper(TheButton button) {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                button.drawStyled(g);
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(button.getWidth() + 40, button.getHeight() + 40);
            }

            {
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        button.setMouseOver(true);
                        repaint();
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        button.setMouseOver(false);
                        repaint();
                    }

                    @Override
                    public void mousePressed(MouseEvent e) {
                        button.setMousePressed(true);
                        repaint();
                    }

                    @Override
                    public void mouseReleased(MouseEvent e) {
                        button.resetBooleans();
                        repaint();
                    }

                    @Override
                    public void mouseClicked(MouseEvent e) {
                        game.changeGameState(GameStates.MENU);
                    }
                });
                setOpaque(false);
            }
        };
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

    private BufferedImage generateThumbnail(int[][] levelData) {
        BufferedImage thumbnail = new BufferedImage(PREVIEW_WIDTH, PREVIEW_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = thumbnail.createGraphics();

        // Handle cases where levelData might be problematic for rendering
        if (levelData == null || levelData.length == 0 || levelData[0].length == 0) {
            g2d.setColor(Color.DARK_GRAY);
            g2d.fillRect(0, 0, PREVIEW_WIDTH, PREVIEW_HEIGHT);
            g2d.setColor(Color.WHITE);
            g2d.setFont(medodicaFontSmall.deriveFont(Font.BOLD));
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
            g2d.setFont(medodicaFontSmall.deriveFont(Font.BOLD));
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

    private void playLevel(String levelName, int[][] levelData) {
        System.out.println("Playing level: " + levelName);

        // Load overlay data
        int[][] overlayData = LoadSave.loadOverlay(levelName);
        if (overlayData == null) {
            // Create default overlay if none exists
            overlayData = new int[levelData.length][levelData[0].length];
            // Set default start/end points if the level is big enough
            if (levelData.length > 4 && levelData[0].length > 15) {
                overlayData[4][0] = 1; // Start point
                overlayData[4][15] = 2; // End point
            }
        }

        // Start the game with the level
        game.startPlayingWithLevel(levelData, overlayData, levelName);
        game.changeGameState(GameStates.PLAYING);
    }

    private void editLevel(String levelName, int[][] levelData) {
        System.out.println("Editing level: " + levelName);
        game.getMapEditing().setLevel(levelData);
        game.getMapEditing().setCurrentLevelName(levelName);

        // Load overlay data for editing
        int[][] overlayData = LoadSave.loadOverlay(levelName);
        if (overlayData == null) {
            overlayData = new int[levelData.length][levelData[0].length];
        }
        game.getMapEditing().setOverlayData(overlayData);

        game.changeGameState(GameStates.EDIT);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw background
        if (backgroundImg != null) {
            g.drawImage(backgroundImg, 0, 0, getWidth(), getHeight(), null);
        }
    }

    // Rounded button class for thumbnails
    private static class RoundedButton extends JButton {
        private int cornerRadius = 10;
        private Shape shape;

        public RoundedButton(String text) {
            super(text);
            setOpaque(false);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setBackground(new Color(0, 0, 0, 0)); // Transparent background
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Don't paint any background - just paint the icon
            super.paintComponent(g);
            g2.dispose();
        }

        @Override
        protected void paintBorder(Graphics g) {
            // Don't paint any border
        }

        @Override
        public boolean contains(int x, int y) {
            if (shape == null || !shape.getBounds().equals(getBounds())) {
                shape = new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);
            }
            return shape.contains(x, y);
        }
    }
}