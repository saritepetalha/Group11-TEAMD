package scenes;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.SwingConstants;

import constants.GameDimensions;
import helpMethods.BorderImageRotationGenerator;
import helpMethods.FontLoader;
import helpMethods.LoadSave;
import helpMethods.ThumbnailCache;
import levelselection.SavedLevelsOnlyStrategy;
import main.Game;
import main.GameStates;
import managers.TileManager;
import managers.GameStateManager;
import managers.GameStateMemento;
import config.GameOptions;
import ui_p.AssetsLoader;
import ui_p.TheButton;

import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class LoadGameMenu extends JPanel {
    private Game game;
    private TileManager tileManager;
    private BufferedImage backgroundImg;
    private TheButton backButton;
    private SavedLevelsOnlyStrategy levelStrategy;
    private GameStateManager gameStateManager;
    private static final int PREVIEW_WIDTH = 192;
    private static final int PREVIEW_HEIGHT = 108;
    private static final int PREVIEW_MARGIN = 10;
    private static final int PREVIEWS_PER_ROW = 2;
    private static final int PREVIEWS_PER_PAGE = 4;
    private static final int HEADER_HEIGHT = 80;
    private Font medodicaFontSmall;
    private Font medodicaFontSmallBold;
    private Font medodicaFontMedium;
    private Font mvBoliFontBold;
    private final Color PAGE_BUTTON_BG_COLOR = new Color(70, 130, 200);
    private final Color PAGE_BUTTON_HOVER_COLOR = new Color(90, 150, 220);
    private final Color PAGE_BUTTON_DISABLED_COLOR = new Color(120, 120, 120);
    private final Color PAGE_INDICATOR_BG_COLOR = new Color(40, 40, 40, 180);
    private final Color PAGE_INDICATOR_TEXT_COLOR = new Color(255, 255, 255);
    private JPanel mainContentPanel;
    private int currentPage = 0;
    private int totalPages = 0;
    private ArrayList<String> allSavedLevels = new ArrayList<>();
    private JButton prevPageButton;
    private JButton nextPageButton;
    private JPanel pageIndicatorPanel;
    private JWindow currentTooltipWindow;
    private javax.swing.Timer tooltipTimer;

    public LoadGameMenu(Game game) {
        this.game = game;
        this.tileManager = game.getTileManager();
        this.backgroundImg = AssetsLoader.getInstance().loadGameMenuBackgroundImg;
        this.levelStrategy = new SavedLevelsOnlyStrategy();
        this.gameStateManager = new GameStateManager();
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

        /*
        // Debug: Print saved levels and their content
        System.out.println("=== DEBUG: createMainContent() ===");
        System.out.println("Found " + allSavedLevels.size() + " saved levels, " + totalPages + " pages");
        System.out.println("Current page: " + (currentPage + 1) + "/" + totalPages);
        System.out.println("Thumbnail cache stats: " + ThumbnailCache.getInstance().getCacheStats());
        System.out.println("=== END DEBUG ===");*/

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
        int startIndex = currentPage * PREVIEWS_PER_PAGE;
        int endIndex = Math.min(startIndex + PREVIEWS_PER_PAGE, allSavedLevels.size());

        System.out.println("Creating page " + (currentPage + 1) + " with levels " + startIndex + " to " + (endIndex - 1));

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
                previewButton.setToolTipText(null);
                addCustomTooltipBehavior(previewButton, levelName);
                previewButton.setPreferredSize(new Dimension(PREVIEW_WIDTH, PREVIEW_HEIGHT));
                previewButton.setCursor(AssetsLoader.getInstance().customHandCursor);

                // Add mouse listener for cursor management on preview button
                previewButton.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        previewButton.setCursor(AssetsLoader.getInstance().customHandCursor);
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        previewButton.setCursor(AssetsLoader.getInstance().customNormalCursor);
                    }
                });

                // Create a container panel for the preview button and trash button
                JPanel previewContainer = new JPanel();
                previewContainer.setLayout(null); // Use absolute positioning
                previewContainer.setOpaque(false);
                previewContainer.setPreferredSize(new Dimension(PREVIEW_WIDTH, PREVIEW_HEIGHT));

                // Add the preview button
                previewButton.setBounds(0, 0, PREVIEW_WIDTH, PREVIEW_HEIGHT);
                previewContainer.add(previewButton);

                // Create trash button with improved visibility
                TheButton trashButton = new TheButton("", 0, 0, 20, 20, AssetsLoader.getInstance().buttonImages.get(3));
                JPanel trashWrapper = new JPanel() {
                    @Override
                    protected void paintComponent(Graphics g) {
                        // Simply draw the trash button without background or hover effects
                        trashButton.draw(g);
                    }

                    @Override
                    public Dimension getPreferredSize() {
                        return new Dimension(20, 20);
                    }
                };
                trashWrapper.setOpaque(false);
                trashWrapper.setBounds(PREVIEW_WIDTH - 20, 0, 20, 20); // Exact top-right corner
                trashWrapper.setCursor(AssetsLoader.getInstance().customHandCursor);

                trashWrapper.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        trashWrapper.setCursor(AssetsLoader.getInstance().customHandCursor);
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        trashWrapper.setCursor(AssetsLoader.getInstance().customNormalCursor);
                    }

                    @Override
                    public void mousePressed(MouseEvent e) {
                        trashButton.setMousePressed(true);
                        trashWrapper.repaint();
                        e.consume(); // Prevent event from propagating to preview button
                    }

                    @Override
                    public void mouseReleased(MouseEvent e) {
                        trashButton.setMousePressed(false);
                        trashWrapper.repaint();
                        e.consume(); // Prevent event from propagating to preview button
                    }

                    @Override
                    public void mouseClicked(MouseEvent e) {
                        e.consume(); // Prevent event from propagating to preview button

                        // Hide any visible tooltip before showing confirmation
                        hideCustomTooltip();

                        Font originalFont = javax.swing.UIManager.getFont("OptionPane.messageFont");
                        Font originalButtonFont = javax.swing.UIManager.getFont("OptionPane.buttonFont");
                        javax.swing.UIManager.put("OptionPane.messageFont", mvBoliFontBold);
                        javax.swing.UIManager.put("OptionPane.buttonFont", mvBoliFontBold);

                        // Create custom rounded buttons with proper functionality
                        final JOptionPane optionPane = new JOptionPane(
                                "Are you sure you want to delete the saved game '" + levelName + "'?\nThis action cannot be undone.",
                                JOptionPane.WARNING_MESSAGE,
                                JOptionPane.YES_NO_OPTION,
                                null,
                                new Object[]{}, // Empty options - we'll add custom buttons
                                null
                        );

                        // custom yes button
                        JButton yesButton = new JButton("Yes") {
                            @Override
                            protected void paintComponent(Graphics g) {
                                Graphics2D g2d = (Graphics2D) g.create();
                                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                                Color bgColor = getModel().isRollover() ? new Color(46, 155, 46) : new Color(34, 139, 34);
                                g2d.setColor(bgColor);
                                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);

                                g2d.setColor(getForeground());
                                g2d.setFont(getFont());
                                FontMetrics fm = g2d.getFontMetrics();
                                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                                int y = (getHeight() + fm.getAscent()) / 2 - 2;
                                g2d.drawString(getText(), x, y);

                                g2d.dispose();
                            }
                        };
                        yesButton.setForeground(Color.WHITE);
                        yesButton.setFont(mvBoliFontBold);
                        yesButton.setFocusPainted(false);
                        yesButton.setBorderPainted(false);
                        yesButton.setContentAreaFilled(false);
                        yesButton.setPreferredSize(new Dimension(70, 30));

                        // custom no button
                        JButton noButton = new JButton("No") {
                            @Override
                            protected void paintComponent(Graphics g) {
                                Graphics2D g2d = (Graphics2D) g.create();
                                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                                Color bgColor = getModel().isRollover() ? new Color(240, 30, 70) : new Color(220, 20, 60);
                                g2d.setColor(bgColor);
                                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);

                                g2d.setColor(getForeground());
                                g2d.setFont(getFont());
                                FontMetrics fm = g2d.getFontMetrics();
                                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                                int y = (getHeight() + fm.getAscent()) / 2 - 2;
                                g2d.drawString(getText(), x, y);

                                g2d.dispose();
                            }
                        };
                        noButton.setForeground(Color.WHITE);
                        noButton.setFont(mvBoliFontBold);
                        noButton.setFocusPainted(false);
                        noButton.setBorderPainted(false);
                        noButton.setContentAreaFilled(false);
                        noButton.setPreferredSize(new Dimension(70, 30));

                        // Add action listeners to close dialog with proper values
                        yesButton.addActionListener(evt -> optionPane.setValue(JOptionPane.YES_OPTION));
                        noButton.addActionListener(evt -> optionPane.setValue(JOptionPane.NO_OPTION));

                        // Set the custom buttons
                        optionPane.setOptions(new Object[]{yesButton, noButton});
                        optionPane.setInitialValue(noButton); // Default to No

                        // Create and show the dialog
                        JDialog dialog = optionPane.createDialog(LoadGameMenu.this, "Delete Saved Game");
                        dialog.setVisible(true);

                        // Get the result
                        Object result = optionPane.getValue();

                        // Restore original fonts
                        javax.swing.UIManager.put("OptionPane.messageFont", originalFont);
                        javax.swing.UIManager.put("OptionPane.buttonFont", originalButtonFont);

                        if (result != null && result.equals(JOptionPane.YES_OPTION)) {
                            deleteSavedGame(levelName);
                        }
                    }
                });

                // Add trash wrapper on top of preview button (higher z-order)
                previewContainer.add(trashWrapper);
                previewContainer.setComponentZOrder(trashWrapper, 0); // Bring to front

                JPanel buttonPanel = new JPanel(new BorderLayout());
                buttonPanel.setOpaque(false);
                buttonPanel.setPreferredSize(new Dimension(PREVIEW_WIDTH, PREVIEW_HEIGHT + 25));

                JLabel nameLabel = new JLabel(levelName, SwingConstants.CENTER);
                nameLabel.setFont(medodicaFontSmallBold);
                nameLabel.setForeground(Color.BLACK);
                nameLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

                buttonPanel.add(previewContainer, BorderLayout.CENTER);
                buttonPanel.add(nameLabel, BorderLayout.SOUTH);

                previewButton.addActionListener(e -> {
                    // Hide any visible tooltip before loading game
                    hideCustomTooltip();

                    // Prepare overlay data
                    int[][] overlay = LoadSave.loadOverlay(levelName);
                    if (overlay == null) {
                        overlay = new int[levelData.length][levelData[0].length];
                        if (levelData.length > 4 && levelData[0].length > 15) {
                            overlay[4][0] = 1;
                            overlay[4][15] = 2;
                        }
                    }

                    // Load saved game state to get the difficulty
                    GameStateMemento saveData = gameStateManager.loadGameState(levelName);
                    String difficulty = "Normal"; // Default fallback

                    // Get difficulty directly from saved game state
                    if (saveData != null) {
                        difficulty = saveData.getDifficulty();
                    }

                    // Directly start the game with the saved difficulty
                    game.startPlayingWithDifficulty(levelData, overlay, levelName, difficulty);
                    game.changeGameState(GameStates.PLAYING);
                });

                gbc.gridx = currentCol;
                gbc.gridy = currentRow;

                previewsContainer.add(buttonPanel, gbc);

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
                // Hide any visible tooltip before returning to menu
                hideCustomTooltip();
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
                g2d.setColor(PAGE_INDICATOR_BG_COLOR);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2d.setColor(new Color(80, 80, 80));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2d.setColor(PAGE_INDICATOR_TEXT_COLOR);
                g2d.setFont(new Font("MV Boli", Font.BOLD, 12));
                String text = "Page " + (currentPage + 1) + " of " + totalPages;
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(text)) / 2;
                int y = (getHeight() + fm.getAscent()) / 2 - 2;
                g2d.drawString(text, x, y);
            }
        };
        indicator.setOpaque(false);
        indicator.setPreferredSize(new Dimension(100, 28));
        return indicator;
    }

    private void stylePageButton(JButton button) {
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setBackground(PAGE_BUTTON_BG_COLOR);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(40, 28));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(40, 80, 140), 1),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
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

            prevPageButton.setEnabled(currentPage > 0);
            nextPageButton.setEnabled(currentPage < totalPages - 1);

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

            pageIndicatorPanel.repaint();
        }
    }

    private void refreshCurrentPage() {
        if (mainContentPanel != null) {
            remove(mainContentPanel);
        }

        createMainContent();
        updatePaginationControls();
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

        // Detect gate edge for proper border rotation
        int gateEdge = BorderImageRotationGenerator.getInstance().detectGateEdge(levelData);

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

                BufferedImage tileSprite = null;

                if (tileId == -3) {
                    tileSprite = BorderImageRotationGenerator.getInstance().getRotatedBorderImage(true, gateEdge);
                    // Fallback
                    if (tileSprite == null) {
                        tileSprite = tileManager.getSprite(tileId);
                    }
                } else if (tileId == -4) { // Gate
                    tileSprite = BorderImageRotationGenerator.getInstance().getRotatedBorderImage(false, gateEdge);

                    // Fallback
                    if (tileSprite == null) {
                        tileSprite = tileManager.getSprite(tileId);
                    }
                } else {
                    // Regular tile
                    tileSprite = tileManager.getSprite(tileId);
                }

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

    /**
     * Adds custom tooltip behavior with fixed positioning and cute fonts
     */
    private void addCustomTooltipBehavior(RoundedButton button, String levelName) {
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                // Delay showing tooltip slightly for better UX
                if (tooltipTimer != null) {
                    tooltipTimer.stop();
                }

                tooltipTimer = new javax.swing.Timer(500, evt -> {
                    showCustomTooltip(button, levelName);
                });
                tooltipTimer.setRepeats(false);
                tooltipTimer.start();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hideCustomTooltip();
                if (tooltipTimer != null) {
                    tooltipTimer.stop();
                }
            }
        });
    }

    /**
     * Shows a custom positioned tooltip with cute styling
     */
    private void showCustomTooltip(RoundedButton button, String levelName) {
        hideCustomTooltip(); // Hide any existing tooltip first

        try {
            // Create tooltip content
            JPanel tooltipPanel = createTooltipPanel(levelName);

            // Calculate centered position relative to button
            Point buttonLocation = button.getLocationOnScreen();
            int tooltipX = buttonLocation.x + (button.getWidth() / 2) - 90; // Center horizontally (tooltip width ~180)
            int tooltipY = buttonLocation.y - 120; // Position above button

            // Ensure tooltip stays on screen
            java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
            if (tooltipX < 0) tooltipX = 10;
            if (tooltipX + 180 > screenSize.width) tooltipX = screenSize.width - 190;
            if (tooltipY < 0) tooltipY = buttonLocation.y + button.getHeight() + 10; // Show below if no space above

            // Create transparent window for tooltip
            currentTooltipWindow = new JWindow();
            currentTooltipWindow.setBackground(new Color(0, 0, 0, 0)); // Fully transparent
            currentTooltipWindow.setAlwaysOnTop(true);
            currentTooltipWindow.setFocusable(false);
            currentTooltipWindow.add(tooltipPanel);
            currentTooltipWindow.pack();
            currentTooltipWindow.setLocation(tooltipX, tooltipY);
            currentTooltipWindow.setVisible(true);

        } catch (Exception e) {
            System.err.println("Error showing custom tooltip: " + e.getMessage());
        }
    }

    /**
     * Hides the current custom tooltip
     */
    private void hideCustomTooltip() {
        if (tooltipTimer != null) {
            tooltipTimer.stop();
            tooltipTimer = null;
        }

        if (currentTooltipWindow != null) {
            currentTooltipWindow.setVisible(false);
            currentTooltipWindow.dispose();
            currentTooltipWindow = null;
        }
    }

    /**
     * Creates a beautifully styled tooltip panel with cute fonts
     */
    private JPanel createTooltipPanel(String levelName) {
        try {
            // Load the game state
            GameStateMemento saveData = gameStateManager.loadGameState(levelName);

            JPanel panel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    // Clear the entire background to transparent first
                    g2d.setComposite(java.awt.AlphaComposite.Clear);
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                    g2d.setComposite(java.awt.AlphaComposite.SrcOver);

                    // Draw rounded background with gradient
                    Color startColor = new Color(255, 248, 220, 245); // Slightly more opaque
                    Color endColor = new Color(255, 240, 200, 245);   // Slightly more opaque
                    java.awt.GradientPaint gradient = new java.awt.GradientPaint(
                            0, 0, startColor, 0, getHeight(), endColor);
                    g2d.setPaint(gradient);
                    g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);

                    // Draw cute border
                    g2d.setColor(new Color(180, 140, 100, 220)); // Slightly more opaque brown border
                    g2d.setStroke(new java.awt.BasicStroke(2));
                    g2d.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 15, 15);
                }
            };

            panel.setLayout(new BorderLayout());
            panel.setPreferredSize(new Dimension(180, 140));
            panel.setOpaque(false);
            panel.setBorder(null);
            panel.setBackground(new Color(0, 0, 0, 0));

            JPanel contentPanel = new JPanel();
            contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
            contentPanel.setOpaque(false);
            contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
            contentPanel.setBackground(new Color(0, 0, 0, 0));

            Font titleFont = new Font(Font.SANS_SERIF, Font.BOLD, 14);
            Font infoFont = new Font(Font.SANS_SERIF, Font.PLAIN, 11);
            Font timeFont = new Font(Font.SANS_SERIF, Font.ITALIC, 10);

            if (saveData == null) {
                JLabel errorLabel = new JLabel("No save data found");
                errorLabel.setFont(infoFont);
                errorLabel.setForeground(new Color(180, 100, 100));
                errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                errorLabel.setOpaque(false);
                contentPanel.add(errorLabel);
                panel.add(contentPanel, BorderLayout.CENTER);
                return panel;
            }

            JLabel titleLabel = new JLabel(levelName);
            titleLabel.setFont(titleFont);
            titleLabel.setForeground(new Color(101, 67, 33));
            titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            titleLabel.setOpaque(false);
            contentPanel.add(titleLabel);

            contentPanel.add(Box.createRigidArea(new Dimension(0, 6)));

            JLabel healthLabel = new JLabel("❤️ Health: " + saveData.getHealth() +
                    (saveData.getGameOptions() != null ? "/" + saveData.getGameOptions().getStartingPlayerHP() : ""));
            healthLabel.setFont(infoFont);
            healthLabel.setForeground(new Color(220, 20, 60));
            healthLabel.setOpaque(false);
            contentPanel.add(healthLabel);


            JLabel shieldLabel = new JLabel("🛡️ Shield: " + saveData.getShield() +
                    (saveData.getGameOptions() != null ? "/" + saveData.getGameOptions().getStartingShield() : ""));
            shieldLabel.setFont(infoFont);
            shieldLabel.setForeground(new Color(30, 144, 255));
            shieldLabel.setOpaque(false);
            contentPanel.add(shieldLabel);


            JLabel goldLabel = new JLabel("💰 Gold: " + saveData.getGold());
            goldLabel.setFont(infoFont);
            goldLabel.setForeground(new Color(255, 165, 0));
            goldLabel.setOpaque(false);
            contentPanel.add(goldLabel);

            String waveText = "🌊 Wave: " + (saveData.getWaveIndex() + 1);
            if (saveData.getGroupIndex() > 0) {
                waveText += " (Group " + (saveData.getGroupIndex() + 1) + ")";
            }
            JLabel waveLabel = new JLabel(waveText);
            waveLabel.setFont(infoFont);
            waveLabel.setForeground(new Color(0, 128, 128));
            waveLabel.setOpaque(false);
            contentPanel.add(waveLabel);

            // Add difficulty information
            String difficultyText = "⚔️ Difficulty: ";
            if (saveData != null) {
                difficultyText += saveData.getDifficulty();
            } else {
                difficultyText += "Unknown";
            }

            JLabel difficultyLabel = new JLabel(difficultyText);
            difficultyLabel.setFont(infoFont);
            difficultyLabel.setForeground(new Color(128, 0, 128));
            difficultyLabel.setOpaque(false);
            contentPanel.add(difficultyLabel);

            contentPanel.add(Box.createRigidArea(new Dimension(0, 4)));

            String timeAgo = getFileTimeAgo(levelName);
            JLabel timeLabel = new JLabel("⏰ Saved: " + timeAgo);
            timeLabel.setFont(timeFont);
            timeLabel.setForeground(new Color(128, 128, 128));
            timeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            timeLabel.setOpaque(false);
            contentPanel.add(timeLabel);

            panel.add(contentPanel, BorderLayout.CENTER);
            return panel;

        } catch (Exception e) {
            System.err.println("Error creating tooltip panel for " + levelName + ": " + e.getMessage());

            JPanel fallbackPanel = new JPanel();
            fallbackPanel.setOpaque(false);
            fallbackPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
            fallbackPanel.setBackground(new Color(255, 255, 220));
            JLabel label = new JLabel("Load " + levelName);
            label.setOpaque(false);
            fallbackPanel.add(label);
            return fallbackPanel;
        }
    }

    /**
     * Gets the relative time when the save file was last modified
     * @param levelName The name of the level
     * @return Formatted relative time string
     */
    private String getFileTimeAgo(String levelName) {
        try {
            // Try multiple possible paths in order of preference (same as LoadSave)
            String[] possiblePaths = {
                    "src/main/resources/Saves",           // Standard Maven structure from project root
                    "demo/src/main/resources/Saves",     // If running from parent directory
                    "main/resources/Saves",              // If running from src directory
                    "resources/Saves",                   // If running from src/main directory
                    "KUTowerDefense/resources/Saves"     // Legacy structure
            };

            File saveFile = null;
            for (String path : possiblePaths) {
                File savesDir = new File(path);
                if (savesDir.exists() && savesDir.isDirectory()) {
                    File testFile = new File(savesDir, levelName + ".json");
                    if (testFile.exists()) {
                        saveFile = testFile;
                        break;
                    }
                }
            }

            if (saveFile == null || !saveFile.exists()) {
                return "Unknown";
            }

            long lastModified = saveFile.lastModified();
            LocalDateTime fileTime = LocalDateTime.ofInstant(
                    java.time.Instant.ofEpochMilli(lastModified),
                    ZoneId.systemDefault()
            );
            LocalDateTime now = LocalDateTime.now();

            // Calculate time difference
            long minutes = ChronoUnit.MINUTES.between(fileTime, now);
            long hours = ChronoUnit.HOURS.between(fileTime, now);
            long days = ChronoUnit.DAYS.between(fileTime, now);

            if (minutes < 1) {
                return "Just now";
            } else if (minutes < 60) {
                return minutes + " minute" + (minutes == 1 ? "" : "s") + " ago";
            } else if (hours < 24) {
                return hours + " hour" + (hours == 1 ? "" : "s") + " ago";
            } else if (days < 7) {
                return days + " day" + (days == 1 ? "" : "s") + " ago";
            } else {
                // For older saves, show the actual date
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d, yyyy");
                return fileTime.format(formatter);
            }

        } catch (Exception e) {
            System.err.println("Error getting file time for " + levelName + ": " + e.getMessage());
            return "Unknown";
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImg != null) {
            g.drawImage(backgroundImg, 0, 0, getWidth(), getHeight(), this);
        } else {
            g.setColor(Color.BLACK); // Fallback
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    private static class RoundedButton extends JButton {
        private int cornerRadius = 15;
        private Shape shape;

        public RoundedButton(String text) {
            super(text);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setForeground(Color.WHITE);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (getModel().isArmed()) {
                g2.setColor(Color.DARK_GRAY.darker());
            } else if (getModel().isRollover()) {
                g2.setColor(Color.GRAY.brighter());
            } else {
                g2.setColor(Color.GRAY);
            }
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius));

            super.paintComponent(g2);

            g2.dispose();
        }


        @Override
        protected void paintBorder(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.BLACK);
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

    /**
     * Cleanup method to be called when the LoadGameMenu is no longer active
     * This ensures all tooltips and timers are properly disposed
     */
    public void cleanup() {
        hideCustomTooltip();

        // Additional cleanup if needed
        if (tooltipTimer != null) {
            tooltipTimer.stop();
            tooltipTimer = null;
        }
    }

    /**
     * Deletes a saved game and refreshes the UI
     * @param levelName The name of the saved game to delete
     */
    private void deleteSavedGame(String levelName) {
        try {
            // Delete the save file (.json)
            boolean saveDeleted = LoadSave.deleteSavedGame(levelName);

            if (saveDeleted) {
                // Remove from cache
                ThumbnailCache.getInstance().removeThumbnail(levelName);

                // Refresh the UI
                refreshMapPreviews();

                System.out.println("Successfully deleted saved game: " + levelName);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Failed to delete the saved game '" + levelName + "'.",
                        "Delete Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            System.err.println("Error deleting saved game " + levelName + ": " + e.getMessage());
            JOptionPane.showMessageDialog(this,
                    "An error occurred while deleting the saved game: " + e.getMessage(),
                    "Delete Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}