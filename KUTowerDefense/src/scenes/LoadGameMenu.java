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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import constants.GameDimensions;
import helpMethods.FontLoader;
import helpMethods.LoadSave;
import helpMethods.ThumbnailCache;
import levelselection.SavedLevelsOnlyStrategy;
import main.Game;
import main.GameStates;
import managers.TileManager;
import ui_p.AssetsLoader;
import ui_p.TheButton;

public class LoadGameMenu extends JPanel { // Changed to JPanel
    private Game game;
    private TileManager tileManager;
    private BufferedImage backgroundImg;
    private TheButton backButton; // Changed to TheButton
    private SavedLevelsOnlyStrategy levelStrategy;

    private static final int PREVIEW_WIDTH = 192;
    private static final int PREVIEW_HEIGHT = 108;
    private static final int PREVIEW_MARGIN = 10;
    private static final int PREVIEWS_PER_ROW = 2;
    private static final int PREVIEWS_PER_PAGE = 4; // 2 columns x 2 rows - reduced to show pagination
    private static final int HEADER_HEIGHT = 80; // Reserve space for title/header
    private static final int FOOTER_HEIGHT = 80; // Reserve space for back button and pagination

    private Font medodicaFontSmall;
    private Font medodicaFontSmallBold;
    private Font medodicaFontMedium;
    private Font mvBoliFontBold;

    // Colors for pagination buttons
    private final Color PAGE_BUTTON_BG_COLOR = new Color(70, 130, 200);
    private final Color PAGE_BUTTON_HOVER_COLOR = new Color(90, 150, 220);
    private final Color PAGE_BUTTON_DISABLED_COLOR = new Color(120, 120, 120);
    private final Color PAGE_INDICATOR_BG_COLOR = new Color(40, 40, 40, 180);
    private final Color PAGE_INDICATOR_TEXT_COLOR = new Color(255, 255, 255);

    // Store the main content panel for refreshing
    private JPanel mainContentPanel;

    // Pagination variables
    private int currentPage = 0;
    private int totalPages = 0;
    private ArrayList<String> allSavedLevels = new ArrayList<>();
    private JButton prevPageButton;
    private JButton nextPageButton;
    private JPanel pageIndicatorPanel;

    public LoadGameMenu(Game game) {
        this.game = game;
        this.tileManager = game.getTileManager();
        this.backgroundImg = AssetsLoader.getInstance().loadGameMenuBackgroundImg;
        this.levelStrategy = new SavedLevelsOnlyStrategy();
        this.medodicaFontSmall = FontLoader.loadMedodicaFont(14f);
        this.medodicaFontSmallBold = FontLoader.loadMedodicaFont(14f).deriveFont(Font.BOLD);
        this.medodicaFontMedium = FontLoader.loadMedodicaFont(16f);
        this.mvBoliFontBold = new Font("MV Boli", Font.BOLD, 14);

        setPreferredSize(new Dimension(GameDimensions.MAIN_MENU_SCREEN_WIDTH, GameDimensions.MAIN_MENU_SCREEN_HEIGHT));
        setLayout(new BorderLayout());

        initUI();
    }

    /**
     * Refreshes the map previews by regenerating the UI content.
     * This should be called when entering the LoadGameMenu to ensure
     * newly added or edited maps are visible.
     */
    public void refreshMapPreviews() {
        System.out.println("Refreshing map previews...");
        System.out.println("Cache stats before refresh: " + ThumbnailCache.getInstance().getCacheStats());

        // Reload saved levels using strategy
        allSavedLevels = levelStrategy.getLevelsToShow();
        totalPages = (int) Math.ceil((double) allSavedLevels.size() / PREVIEWS_PER_PAGE);

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

        // Update pagination controls to reflect any changes in total pages
        updatePaginationControls();

        // Refresh the display
        revalidate();
        repaint();

        System.out.println("Map previews refreshed successfully.");
        System.out.println("Cache stats after refresh: " + ThumbnailCache.getInstance().getCacheStats());
    }

    private void initUI() {
        // Load saved levels using strategy and calculate pagination
        allSavedLevels = levelStrategy.getLevelsToShow();
        totalPages = Math.max(1, (int) Math.ceil((double) allSavedLevels.size() / PREVIEWS_PER_PAGE));
        currentPage = 0;

        System.out.println("=== PAGINATION DEBUG ===");
        System.out.println("Total maps: " + allSavedLevels.size());
        System.out.println("Maps per page: " + PREVIEWS_PER_PAGE);
        System.out.println("Total pages: " + totalPages);
        System.out.println("Current page: " + (currentPage + 1));
        System.out.println("========================");

        createMainContent();
        createNavigationButtons();
    }

    private void createMainContent() {
        mainContentPanel = new JPanel(new BorderLayout());
        mainContentPanel.setOpaque(false);

        // Debug: Print saved levels and their content
        System.out.println("=== DEBUG: createMainContent() ===");
        System.out.println("Found " + allSavedLevels.size() + " saved levels, " + totalPages + " pages");
        System.out.println("Current page: " + (currentPage + 1) + "/" + totalPages);
        System.out.println("Thumbnail cache stats: " + ThumbnailCache.getInstance().getCacheStats());
        System.out.println("=== END DEBUG ===");

        if (allSavedLevels.isEmpty()) {
            createNoLevelsPanel();
        } else {
            createCurrentPageGrid();
        }

        add(mainContentPanel, BorderLayout.CENTER);
    }

    private void createNoLevelsPanel() {
        JLabel noLevelsLabel = new JLabel("No saved games found. Start a new game first!");
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
        int endIndex = Math.min(startIndex + PREVIEWS_PER_PAGE, allSavedLevels.size());

        System.out.println("Creating page " + (currentPage + 1) + " with levels " + startIndex + " to " + (endIndex - 1));

        // Create a panel that will hold the map previews for current page
        JPanel previewsContainer = new JPanel(new GridBagLayout());
        previewsContainer.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(PREVIEW_MARGIN, PREVIEW_MARGIN, PREVIEW_MARGIN, PREVIEW_MARGIN);
        gbc.anchor = GridBagConstraints.CENTER;

        int currentRow = 0;
        int currentCol = 0;

        for (int i = startIndex; i < endIndex; i++) {
            String levelName = allSavedLevels.get(i);
            int[][] levelData = LoadSave.loadLevel(levelName);

            if (levelData != null) {
                BufferedImage thumbnail = generateThumbnailWithCache(levelName, levelData);
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
                nameLabel.setForeground(Color.BLACK);
                nameLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

                buttonPanel.add(previewButton, BorderLayout.CENTER);
                buttonPanel.add(nameLabel, BorderLayout.SOUTH);

                previewButton.addActionListener(e -> showPlayEditDialog(levelName, levelData));

                // Set grid position
                gbc.gridx = currentCol;
                gbc.gridy = currentRow;

                previewsContainer.add(buttonPanel, gbc);

                // Move to next position
                currentCol++;
                if (currentCol >= PREVIEWS_PER_ROW) {
                    currentCol = 0;
                    currentRow++;
                }
            }
        }

        // Add top margin to prevent content from going into header area
        JPanel contentWrapper = new JPanel(new BorderLayout());
        contentWrapper.setOpaque(false);
        contentWrapper.setBorder(BorderFactory.createEmptyBorder(HEADER_HEIGHT, 0, 0, 0));
        contentWrapper.add(previewsContainer, BorderLayout.CENTER);

        mainContentPanel.add(contentWrapper, BorderLayout.CENTER);
    }

    private void createNavigationButtons() {
        // Create bottom panel for navigation and back button
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 35, 5));
        JPanel backButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 60, -5));
        backButtonPanel.setOpaque(false);
        backButton = new TheButton("Back", 0, 0, 160, 35);
        backButtonPanel.add(createTheButtonWrapper(backButton));
        JPanel paginationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        paginationPanel.setOpaque(false);


        prevPageButton = new JButton("←");
        stylePageButton(prevPageButton);
        prevPageButton.addActionListener(e -> {
            if (currentPage > 0) {
                currentPage--;
                refreshCurrentPage();
            }
        });


        pageIndicatorPanel = createSolidPageIndicator();

        nextPageButton = new JButton("→");
        stylePageButton(nextPageButton);
        nextPageButton.addActionListener(e -> {
            if (currentPage < totalPages - 1) {
                currentPage++;
                refreshCurrentPage();
            }
        });

        paginationPanel.add(prevPageButton);
        paginationPanel.add(pageIndicatorPanel);
        paginationPanel.add(nextPageButton);

        bottomPanel.add(backButtonPanel, BorderLayout.WEST);
        bottomPanel.add(paginationPanel, BorderLayout.CENTER);

        add(bottomPanel, BorderLayout.SOUTH);

        updatePaginationControls();
    }

    private JPanel createTheButtonWrapper(TheButton button) {
        JPanel wrapper = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                int offsetX = 10;
                int offsetY = 6;
                TheButton drawButton = new TheButton(button.getText(), offsetX, offsetY, button.getWidth(), button.getHeight());
                drawButton.setMouseOver(button.isMouseOver());
                drawButton.setMousePressed(button.isMousePressed());
                drawButton.drawStyled(g);
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(button.getWidth() + 20, button.getHeight() + 12);
            }
        };
        wrapper.setOpaque(false);
        wrapper.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setMouseOver(true);
                wrapper.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setMouseOver(false);
                wrapper.repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                button.setMousePressed(true);
                wrapper.repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                button.setMousePressed(false);
                wrapper.repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                game.changeGameState(GameStates.MENU);
            }
        });
        return wrapper;
    }

    private JPanel createSolidPageIndicator() {
        JPanel indicator = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw solid rounded background
                g2d.setColor(PAGE_INDICATOR_BG_COLOR);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);

                // Draw border
                g2d.setColor(new Color(80, 80, 80));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);

                // Draw text
                g2d.setColor(PAGE_INDICATOR_TEXT_COLOR);
                g2d.setFont(new Font("MV Boli", Font.BOLD, 12)); // MV Boli font to match
                String text = "Page " + (currentPage + 1) + " of " + totalPages;
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(text)) / 2;
                int y = (getHeight() + fm.getAscent()) / 2 - 2;
                g2d.drawString(text, x, y);
            }
        };
        indicator.setOpaque(false);
        indicator.setPreferredSize(new Dimension(100, 28)); // Smaller to match buttons
        return indicator;
    }

    private void stylePageButton(JButton button) {
        button.setFont(new Font("Arial", Font.BOLD, 16)); // Larger Arial font for arrows
        button.setBackground(PAGE_BUTTON_BG_COLOR);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(40, 28)); // Smaller width for arrow buttons
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(40, 80, 140), 1),
                BorderFactory.createEmptyBorder(6, 8, 6, 8) // Reduced padding
        ));

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                if (button.isEnabled()) {
                    button.setBackground(PAGE_BUTTON_HOVER_COLOR);
                }
            }

            public void mouseExited(MouseEvent evt) {
                if (button.isEnabled()) {
                    button.setBackground(PAGE_BUTTON_BG_COLOR);
                } else {
                    button.setBackground(PAGE_BUTTON_DISABLED_COLOR);
                }
            }
        });
    }

    private void updatePaginationControls() {
        if (totalPages <= 1 && allSavedLevels.size() <= 4) {
            // Hide pagination controls only if there are very few maps
            prevPageButton.setVisible(false);
            nextPageButton.setVisible(false);
            pageIndicatorPanel.setVisible(false);
        } else {
            prevPageButton.setVisible(true);
            nextPageButton.setVisible(true);
            pageIndicatorPanel.setVisible(true);

            // Update button states
            prevPageButton.setEnabled(currentPage > 0);
            nextPageButton.setEnabled(currentPage < totalPages - 1);

            // Update button colors based on enabled state
            if (!prevPageButton.isEnabled()) {
                prevPageButton.setBackground(PAGE_BUTTON_DISABLED_COLOR);
            } else {
                prevPageButton.setBackground(PAGE_BUTTON_BG_COLOR);
            }

            if (!nextPageButton.isEnabled()) {
                nextPageButton.setBackground(PAGE_BUTTON_DISABLED_COLOR);
            } else {
                nextPageButton.setBackground(PAGE_BUTTON_BG_COLOR);
            }

            // Repaint the page indicator to update the text
            pageIndicatorPanel.repaint();
        }
    }

    private void refreshCurrentPage() {
        // Remove current content
        if (mainContentPanel != null) {
            remove(mainContentPanel);
        }

        // Recreate content for current page
        createMainContent();
        updatePaginationControls();

        // Refresh display
        revalidate();
        repaint();
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

    /**
     * Generates a thumbnail with caching support
     * @param levelName The name of the level
     * @param levelData The level data array
     * @return BufferedImage thumbnail
     */
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
            game.startPlayingWithLevel(levelData, overlay, levelName);
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