package scenes;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Composite;
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
import java.io.File;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import constants.GameDimensions;
import helpMethods.FontLoader;
import helpMethods.LoadSave;
import helpMethods.ThumbnailCache;
import helpMethods.BorderImageRotationGenerator;
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
    private LevelSelectionStrategy strategy;

    private static final int PREVIEW_WIDTH = 192;
    private static final int PREVIEW_HEIGHT = 108;
    private static final int PREVIEW_MARGIN = 15;
    private static final int PREVIEWS_PER_ROW = 2;
    private static final int PREVIEWS_PER_PAGE = 4;
    private static final int HEADER_HEIGHT = 80;
    private static final int FOOTER_HEIGHT = 80;

    private Font medodicaFontSmall;
    private Font medodicaFontSmallBold;
    private Font medodicaFontMedium;
    private Font mvBoliFontBold;

    private int currentPage = 0;
    private int totalPages = 0;
    private ArrayList<String> availableLevels = new ArrayList<>();
    private JButton prevPageButton;
    private JButton nextPageButton;
    private JPanel mainContentPanel;
    private JPanel pageIndicatorPanel;
    private JPanel bottomPanel;

    // Difficulty Selection Overlay
    private boolean difficultyOverlayVisible = false;
    private String selectedLevelName;
    private int[][] selectedLevelData;
    private int[][] selectedOverlayData;
    private JPanel difficultyOverlay;

    private final Color PAGE_BUTTON_BG_COLOR = new Color(70, 130, 200);
    private final Color PAGE_BUTTON_HOVER_COLOR = new Color(90, 150, 220);
    private final Color PAGE_BUTTON_DISABLED_COLOR = new Color(120, 120, 120);
    private final Color PAGE_INDICATOR_BG_COLOR = new Color(40, 40, 40, 180);
    private final Color PAGE_INDICATOR_TEXT_COLOR = new Color(255, 255, 255);

    public LevelSelectionScene(Game game, LevelSelectionStrategy strategy) {
        this.game = game;
        this.strategy = strategy;
        this.tileManager = game.getTileManager();
        this.backgroundImg = AssetsLoader.getInstance().selectMapBackgroundImg;

        this.medodicaFontSmall = FontLoader.loadMedodicaFont(14f);
        this.medodicaFontSmallBold = FontLoader.loadMedodicaFont(14f).deriveFont(Font.BOLD);
        this.medodicaFontMedium = FontLoader.loadMedodicaFont(16f);
        this.mvBoliFontBold = new Font("MV Boli", Font.BOLD, 14);

        setPreferredSize(new Dimension(GameDimensions.MAIN_MENU_SCREEN_WIDTH, GameDimensions.MAIN_MENU_SCREEN_HEIGHT));
        setLayout(new BorderLayout());

        initUI();
    }

    public void refreshLevelList() {
        System.out.println("Refreshing level list...");
        System.out.println("Cache stats before refresh: " + ThumbnailCache.getInstance().getCacheStats());

        availableLevels = strategy.getLevelsToShow();
        totalPages = Math.max(1, (int) Math.ceil((double) availableLevels.size() / PREVIEWS_PER_PAGE));

        if (currentPage >= totalPages) {
            currentPage = Math.max(0, totalPages - 1);
        }

        if (mainContentPanel != null) {
            remove(mainContentPanel);
        }

        createMainContent();
        revalidate();
        repaint();

        System.out.println("Level list refreshed successfully.");
        System.out.println("Cache stats after refresh: " + ThumbnailCache.getInstance().getCacheStats());
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

        if (availableLevels.isEmpty()) {
            createNoLevelsPanel();
        } else {
            createCurrentPageGrid();
        }

        add(mainContentPanel, BorderLayout.CENTER);
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
        int startIndex = currentPage * PREVIEWS_PER_PAGE;
        int endIndex = Math.min(startIndex + PREVIEWS_PER_PAGE, availableLevels.size());

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

                gbc.gridx = currentCol;
                gbc.gridy = currentRow;

                previewsContainer.add(levelPanel, gbc);

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

    private JPanel createLevelPanel(String levelName, BufferedImage thumbnail, int[][] levelData) {
        JPanel levelPanel = new JPanel(new BorderLayout());
        levelPanel.setOpaque(false);
        levelPanel.setPreferredSize(new Dimension(PREVIEW_WIDTH, PREVIEW_HEIGHT + 35));

        RoundedButton previewButton = new RoundedButton("");
        previewButton.setIcon(new ImageIcon(thumbnail));
        previewButton.setFont(medodicaFontSmall);
        previewButton.setPreferredSize(new Dimension(PREVIEW_WIDTH, PREVIEW_HEIGHT));
        previewButton.setCursor(AssetsLoader.getInstance().customHandCursor);

        previewButton.addActionListener(e -> {
            managers.AudioManager.getInstance().playButtonClickSound();
            showDifficultyOverlay(levelName, levelData);
        });

        int buttonSize = 28;
        int spacing = 8;
        int totalButtonWidth = buttonSize * 3 + spacing * 2;
        int totalButtonHeight = buttonSize + 10;

        JPanel buttonPanel = new JPanel() {
            private TheButton playButton;
            private TheButton editButton;
            private TheButton trashButton;
            private String hoverText = "";
            private int hoveredButtonX = 0;
            private int hoveredButtonY = 0;

            {
                setOpaque(false);
                setPreferredSize(new Dimension(totalButtonWidth, totalButtonHeight));

                playButton = new TheButton("", 0, 5, buttonSize, buttonSize,
                        AssetsLoader.getInstance().buttonImages.get(4));

                editButton = new TheButton("", buttonSize + spacing, 5, buttonSize, buttonSize,
                        AssetsLoader.getInstance().buttonImages.get(1));

                trashButton = new TheButton("", (buttonSize + spacing) * 2, 5, buttonSize, buttonSize,
                        AssetsLoader.getInstance().buttonImages.get(3));

                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (playButton.getBounds().contains(e.getPoint())) {
                            managers.AudioManager.getInstance().playButtonClickSound();
                            showDifficultyOverlay(levelName, levelData);
                        } else if (editButton.getBounds().contains(e.getPoint())) {
                            managers.AudioManager.getInstance().playButtonClickSound();
                            editLevel(levelName, levelData);
                        } else if (trashButton.getBounds().contains(e.getPoint())) {
                            managers.AudioManager.getInstance().playButtonClickSound();
                            Font originalFont = javax.swing.UIManager.getFont("OptionPane.messageFont");
                            Font originalButtonFont = javax.swing.UIManager.getFont("OptionPane.buttonFont");
                            javax.swing.UIManager.put("OptionPane.messageFont", mvBoliFontBold);
                            javax.swing.UIManager.put("OptionPane.buttonFont", mvBoliFontBold);

                            // Create custom rounded buttons with proper functionality
                            final JOptionPane optionPane = new JOptionPane(
                                    "Are you sure you want to delete the level '" + levelName + "'?\nThis action cannot be undone.",
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

                            yesButton.addActionListener(evt -> optionPane.setValue(JOptionPane.YES_OPTION));
                            noButton.addActionListener(evt -> optionPane.setValue(JOptionPane.NO_OPTION));

                            optionPane.setOptions(new Object[]{yesButton, noButton});
                            optionPane.setInitialValue(noButton); // Default to No

                            JDialog dialog = optionPane.createDialog(LevelSelectionScene.this, "Delete Level");
                            dialog.setVisible(true);

                            Object result = optionPane.getValue();

                            javax.swing.UIManager.put("OptionPane.messageFont", originalFont);
                            javax.swing.UIManager.put("OptionPane.buttonFont", originalButtonFont);

                            if (result != null && result.equals(JOptionPane.YES_OPTION)) {
                                deleteLevel(levelName);
                            }
                        }
                    }

                    @Override
                    public void mousePressed(MouseEvent e) {
                        if (playButton.getBounds().contains(e.getPoint())) {
                            playButton.setMousePressed(true);
                        } else if (editButton.getBounds().contains(e.getPoint())) {
                            editButton.setMousePressed(true);
                        } else if (trashButton.getBounds().contains(e.getPoint())) {
                            trashButton.setMousePressed(true);
                        }
                        repaint();
                    }

                    @Override
                    public void mouseReleased(MouseEvent e) {
                        playButton.setMousePressed(false);
                        editButton.setMousePressed(false);
                        trashButton.setMousePressed(false);
                        repaint();
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        setCursor(AssetsLoader.getInstance().customNormalCursor);
                        hoverText = "";
                        repaint();
                    }
                });

                addMouseMotionListener(new MouseAdapter() {
                    @Override
                    public void mouseMoved(MouseEvent e) {
                        boolean playHover = playButton.getBounds().contains(e.getPoint());
                        boolean editHover = editButton.getBounds().contains(e.getPoint());
                        boolean trashHover = trashButton.getBounds().contains(e.getPoint());

                        // Set cursor based on button hover
                        if (playHover || editHover || trashHover) {
                            setCursor(AssetsLoader.getInstance().customHandCursor);
                        } else {
                            setCursor(AssetsLoader.getInstance().customNormalCursor);
                        }

                        // Set hover text and position
                        if (playHover) {
                            hoverText = "Play";
                            hoveredButtonX = playButton.getX() + playButton.getWidth() / 2;
                            hoveredButtonY = playButton.getY() - 5;
                        } else if (editHover) {
                            hoverText = "Edit";
                            hoveredButtonX = editButton.getX() + editButton.getWidth() / 2;
                            hoveredButtonY = editButton.getY() - 5;
                        } else if (trashHover) {
                            hoverText = "Delete";
                            hoveredButtonX = trashButton.getX() + trashButton.getWidth() / 2;
                            hoveredButtonY = trashButton.getY() - 5;
                        } else {
                            hoverText = "";
                        }

                        repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                playButton.setMouseOver(false);
                editButton.setMouseOver(false);
                trashButton.setMouseOver(false);

                playButton.draw(g);
                editButton.draw(g);
                trashButton.draw(g);

                if (!hoverText.isEmpty()) {
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    g2d.setFont(medodicaFontSmall.deriveFont(Font.BOLD, 14f));
                    FontMetrics fm = g2d.getFontMetrics();
                    int textWidth = fm.stringWidth(hoverText);
                    int textHeight = fm.getHeight();
                    int textX = hoveredButtonX - textWidth / 2;
                    int textY = hoveredButtonY - 5;
                    textX = Math.max(2, Math.min(textX, getWidth() - textWidth - 2));
                    textY = Math.max(textHeight - 2, textY);
                    g2d.setColor(new Color(0, 0, 0, 150));
                    g2d.fillRoundRect(textX - 6, textY - textHeight + 2, textWidth + 12, textHeight + 4, 8, 8);
                    g2d.setColor(new Color(255, 255, 255, 80));
                    g2d.drawRoundRect(textX - 6, textY - textHeight + 2, textWidth + 12, textHeight + 4, 8, 8);
                    g2d.setColor(Color.WHITE);
                    g2d.drawString(hoverText, textX, textY);
                }
            }
        };

        JPanel buttonWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        buttonWrapper.setOpaque(false);
        buttonWrapper.add(buttonPanel);

        levelPanel.add(previewButton, BorderLayout.CENTER);
        levelPanel.add(buttonWrapper, BorderLayout.PAGE_END);

        return levelPanel;
    }

    private void createNavigationButtons() {
        bottomPanel = new JPanel(new BorderLayout());
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
                managers.AudioManager.getInstance().playButtonClickSound();
                currentPage--;
                refreshCurrentPage();
            }
        });

        pageIndicatorPanel = createSolidPageIndicator();

        nextPageButton = new JButton("→");
        stylePageButton(nextPageButton);
        nextPageButton.addActionListener(e -> {
            if (currentPage < totalPages - 1) {
                managers.AudioManager.getInstance().playButtonClickSound();
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
                // Play button click sound for back button
                managers.AudioManager.getInstance().playButtonClickSound();
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
                g2d.setFont(mvBoliFontBold);
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
        if (totalPages <= 1 && availableLevels.size() <= 4) {
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

    private BufferedImage generateThumbnailWithCache(String levelName, int[][] levelData) {
        // Calculate hash of level data for cache validation
        int levelDataHash = java.util.Arrays.deepHashCode(levelData);

        // Try to get from cache first
        ThumbnailCache cache = ThumbnailCache.getInstance();
        BufferedImage cachedThumbnail = cache.getCachedThumbnail(levelName, levelDataHash);

        if (cachedThumbnail != null) {
            System.out.println("LevelSelection: Thumbnail cache HIT for " + levelName + " (hash: " + levelDataHash + ")");
            return cachedThumbnail;
        }

        // Cache miss - generate new thumbnail
        System.out.println("LevelSelection: Thumbnail cache MISS for " + levelName + " (hash: " + levelDataHash + ") - generating new thumbnail");
        BufferedImage newThumbnail = generateThumbnail(levelData);

        // Cache the generated thumbnail
        cache.cacheThumbnail(levelName, newThumbnail, levelDataHash);

        return newThumbnail;
    }

    private BufferedImage generateThumbnail(int[][] levelData) {
        BufferedImage thumbnail = new BufferedImage(PREVIEW_WIDTH, PREVIEW_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = thumbnail.createGraphics();

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

    private void showDifficultyOverlay(String levelName, int[][] levelData) {
        this.selectedLevelName = levelName;
        this.selectedLevelData = levelData;

        // Prepare overlay data
        int[][] overlayData = LoadSave.loadOverlay(levelName);
        if (overlayData == null) {
            overlayData = new int[levelData.length][levelData[0].length];
            if (levelData.length > 4 && levelData[0].length > 15) {
                overlayData[4][0] = 1; // Start point
                overlayData[4][15] = 2; // End point
            }
        }
        this.selectedOverlayData = overlayData;

        // Hide main content and bottom panel
        if (mainContentPanel != null) {
            mainContentPanel.setVisible(false);
        }
        if (this.bottomPanel != null) {
            this.bottomPanel.setVisible(false);
        }

        createDifficultyOverlay();
        difficultyOverlayVisible = true;
        difficultyOverlay.setVisible(true);

        setComponentZOrder(difficultyOverlay, 0);
        revalidate();
        repaint();
    }

    private void createDifficultyOverlay() {
        if (difficultyOverlay != null) {
            remove(difficultyOverlay);
        }

        difficultyOverlay = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setColor(new Color(0, 0, 0, 150));
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        difficultyOverlay.setLayout(new GridBagLayout());
        difficultyOverlay.setOpaque(false);

        difficultyOverlay.setBounds(0, 0,
                GameDimensions.MAIN_MENU_SCREEN_WIDTH,
                GameDimensions.MAIN_MENU_SCREEN_HEIGHT);

        difficultyOverlay.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // If the event was consumed by a child component (like difficultyPanel or its buttons),
                // it means the click was handled by that child. In that case, we do nothing here.
                if (e.isConsumed()) {
                    return;
                }
                // If the event was not consumed, it implies the click was on the
                // background of difficultyOverlay itself (the semi-transparent area).
                hideDifficultyOverlay();
            }
        });

        JPanel difficultyPanel = new JPanel(new GridBagLayout());
        difficultyPanel.setBackground(new Color(40, 40, 40, 240));
        difficultyPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(100, 100, 100), 3),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        difficultyPanel.setPreferredSize(new Dimension(280, 420));
        difficultyPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                e.consume();
            }
        });

        GridBagConstraints gbc = new GridBagConstraints();

        JLabel titleLabel = new JLabel("Select Difficulty");
        titleLabel.setFont(medodicaFontMedium.deriveFont(Font.BOLD, 18f));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(0, 0, 15, 0);
        gbc.anchor = GridBagConstraints.CENTER;
        difficultyPanel.add(titleLabel, gbc);

        BufferedImage easyImg = LoadSave.getImageFromPath("/UI/Easy.png");
        BufferedImage normalImg = LoadSave.getImageFromPath("/UI/Normal.png");
        BufferedImage hardImg = LoadSave.getImageFromPath("/UI/Hard.png");
        BufferedImage customImg = LoadSave.getImageFromPath("/UI/Custom.png");

        int buttonPanelWidth = 220;
        int buttonPanelHeight = 60;
        int spacing = 8;

        JPanel easyPanel = createImageDifficultyButton(easyImg, "Easy", buttonPanelWidth, buttonPanelHeight);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.insets = new Insets(spacing, 0, spacing, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        difficultyPanel.add(easyPanel, gbc);

        JPanel normalPanel = createImageDifficultyButton(normalImg, "Normal", buttonPanelWidth, buttonPanelHeight);
        gbc.gridy = 2;
        difficultyPanel.add(normalPanel, gbc);

        JPanel hardPanel = createImageDifficultyButton(hardImg, "Hard", buttonPanelWidth, buttonPanelHeight);
        gbc.gridy = 3;
        difficultyPanel.add(hardPanel, gbc);

        JPanel customPanel = createImageDifficultyButton(customImg, "Custom", buttonPanelWidth, buttonPanelHeight);
        gbc.gridy = 4;
        difficultyPanel.add(customPanel, gbc);

        JLabel closeHintLabel = new JLabel("Click outside this panel to close");
        closeHintLabel.setFont(medodicaFontSmall.deriveFont(14f));
        closeHintLabel.setForeground(new Color(200, 200, 200));
        closeHintLabel.setHorizontalAlignment(SwingConstants.CENTER);

        gbc.gridy = 5;
        gbc.insets = new Insets(15, 0, 0, 0);
        difficultyPanel.add(closeHintLabel, gbc);

        GridBagConstraints overlayGbc = new GridBagConstraints();
        overlayGbc.anchor = GridBagConstraints.CENTER;
        difficultyOverlay.add(difficultyPanel, overlayGbc);

        add(difficultyOverlay);
    }

    private JPanel createImageDifficultyButton(BufferedImage buttonImage, String difficulty, int panelWidth, int panelHeight) {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                boolean isHovered = Boolean.TRUE.equals(getClientProperty("isHovered"));
                boolean isPressed = Boolean.TRUE.equals(getClientProperty("isPressed"));

                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int targetIconHeight = panelHeight - 16;
                int iconX, iconY, scaledIconWidth, scaledIconHeight;

                if (buttonImage != null) {
                    int originalIconWidth = buttonImage.getWidth();
                    int originalIconHeight = buttonImage.getHeight();
                    double aspectRatio = (double) originalIconWidth / originalIconHeight;

                    scaledIconHeight = targetIconHeight;
                    scaledIconWidth = (int) (scaledIconHeight * aspectRatio);

                    if (scaledIconWidth > panelWidth - 16) {
                        scaledIconWidth = panelWidth - 16;
                        scaledIconHeight = (int) (scaledIconWidth / aspectRatio);
                    }

                    iconX = (panelWidth - scaledIconWidth) / 2;
                    iconY = (panelHeight - scaledIconHeight) / 2;
                    Composite originalComposite = g2d.getComposite();

                    if (isPressed) {
                        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
                        g2d.setColor(new Color(0,0,0,50));
                        g2d.fillRoundRect(0, 0, panelWidth, panelHeight, 8,8);
                        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.9f));
                        g2d.drawImage(buttonImage, iconX + 1, iconY + 1, scaledIconWidth - 2, scaledIconHeight - 2, null);
                    } else if (isHovered) {
                        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f));
                        g2d.setColor(Color.WHITE);
                        g2d.fillRoundRect(0, 0, panelWidth, panelHeight, 12, 12);
                        g2d.setComposite(originalComposite);
                        g2d.drawImage(buttonImage, iconX, iconY, scaledIconWidth, scaledIconHeight, null);
                    } else {
                        g2d.drawImage(buttonImage, iconX, iconY, scaledIconWidth, scaledIconHeight, null);
                    }
                    g2d.setComposite(originalComposite);

                } else {
                    g.setColor(new Color(80, 80, 80));
                    g.fillRoundRect(2, 2, panelWidth - 4, panelHeight - 4, 8, 8);
                    g.setColor(Color.WHITE);
                    g.setFont(medodicaFontSmall.deriveFont(Font.BOLD, 14f));
                    FontMetrics fm = g.getFontMetrics();
                    String text = difficulty.toUpperCase();
                    int textWidth = fm.stringWidth(text);
                    int textX = (panelWidth - textWidth) / 2;
                    int textY = (panelHeight + fm.getAscent()) / 2 - fm.getDescent()/2;
                    g.drawString(text, textX, textY);

                    g.setColor(new Color(120,120,120));
                    ((Graphics2D) g).setStroke(new java.awt.BasicStroke(1));
                    g.drawRoundRect(2, 2, panelWidth - 5, panelHeight - 5, 8, 8);
                }
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(panelWidth, panelHeight);
            }

            @Override
            public Dimension getMinimumSize() {
                return new Dimension(panelWidth, panelHeight);
            }

            @Override
            public Dimension getMaximumSize() {
                return new Dimension(panelWidth, panelHeight);
            }
        };

        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(panelWidth, panelHeight));
        panel.setMinimumSize(new Dimension(panelWidth, panelHeight));
        panel.setMaximumSize(new Dimension(panelWidth, panelHeight));
        panel.setCursor(AssetsLoader.getInstance().customHandCursor);


        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                panel.putClientProperty("isHovered", true);
                panel.setCursor(AssetsLoader.getInstance().customHandCursor);
                panel.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                panel.putClientProperty("isHovered", false);
                panel.putClientProperty("isPressed", false);
                panel.setCursor(AssetsLoader.getInstance().customNormalCursor);
                panel.repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                panel.putClientProperty("isPressed", true);
                panel.repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                panel.putClientProperty("isPressed", false);
                panel.repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                // Play button click sound for difficulty selection
                managers.AudioManager.getInstance().playButtonClickSound();
                selectDifficultyAndPlay(difficulty);
            }
        });

        return panel;
    }

    private void selectDifficultyAndPlay(String difficulty) {
        hideDifficultyOverlay();

        // Starting a new game - no need to delete existing saves
        // The save system will handle creating new save files when needed
        System.out.println("🔄 LevelSelectionScene: Starting new game for level '" + selectedLevelName + "' with difficulty '" + difficulty + "'");

        game.startPlayingWithDifficulty(selectedLevelData, selectedOverlayData, selectedLevelName, difficulty);
        game.changeGameState(GameStates.SKILL_SELECTION);
    }

    private void hideDifficultyOverlay() {
        if (difficultyOverlay != null) {
            difficultyOverlay.setVisible(false);
            remove(difficultyOverlay);
            difficultyOverlay = null;
        }
        difficultyOverlayVisible = false;

        if (mainContentPanel != null) {
            mainContentPanel.setVisible(true);
        }
        if (this.bottomPanel != null) {
            this.bottomPanel.setVisible(true);
        }

        revalidate();
        repaint();
    }

    private void playLevel(String levelName, int[][] levelData) {
        // Starting a new game - no need to delete existing saves
        // The save system will handle creating new save files when needed
        System.out.println("🔄 LevelSelectionScene: Starting new game for level '" + levelName + "'");

        int[][] overlayData = LoadSave.loadOverlay(levelName);
        if (overlayData == null) {
            overlayData = new int[levelData.length][levelData[0].length];
            if (levelData.length > 4 && levelData[0].length > 15) {
                overlayData[4][0] = 1; // Start point
                overlayData[4][15] = 2; // End point
            }
        }

        game.startPlayingWithDifficulty(levelData, overlayData, levelName, "Normal");
        game.changeGameState(GameStates.SKILL_SELECTION);
    }

    private void editLevel(String levelName, int[][] levelData) {
        System.out.println("Editing level: " + levelName);
        game.getMapEditing().setLevel(levelData);
        game.getMapEditing().setCurrentLevelName(levelName);

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

        if (backgroundImg != null) {
            g.drawImage(backgroundImg, 0, 0, getWidth(), getHeight(), this);
        } else {
            g.setColor(Color.BLACK);
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
     * Deletes a level file and refreshes the UI
     * @param levelName The name of the level to delete
     */
    private void deleteLevel(String levelName) {
        try {
            // Delete the level file (.json)
            boolean levelDeleted = deleteLevelFile(levelName);

            if (levelDeleted) {
                // Remove from cache
                ThumbnailCache.getInstance().removeThumbnail(levelName);

                // Refresh the UI
                refreshLevelList();

                System.out.println("Successfully deleted level: " + levelName);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Failed to delete the level '" + levelName + "'.",
                        "Delete Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            System.err.println("Error deleting level " + levelName + ": " + e.getMessage());
            JOptionPane.showMessageDialog(this,
                    "An error occurred while deleting the level: " + e.getMessage(),
                    "Delete Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Deletes a level file from the levels directory
     * @param levelName The name of the level to delete (without .json extension)
     * @return true if deletion was successful, false otherwise
     */
    private boolean deleteLevelFile(String levelName) {
        try {
            // Try multiple possible paths in order of preference (same as LoadSave)
            String[] possiblePaths = {
                    "src/main/resources/Levels",           // Standard Maven structure from project root
                    "demo/src/main/resources/Levels",     // If running from parent directory
                    "main/resources/Levels",              // If running from src directory
                    "resources/Levels",                   // If running from src/main directory
                    "KUTowerDefense/resources/Levels"     // Legacy structure
            };

            File levelFile = null;
            for (String path : possiblePaths) {
                File levelsDir = new File(path);
                if (levelsDir.exists() && levelsDir.isDirectory()) {
                    File testFile = new File(levelsDir, levelName + ".json");
                    if (testFile.exists()) {
                        levelFile = testFile;
                        break;
                    }
                }
            }

            if (levelFile != null && levelFile.exists()) {
                boolean deleted = levelFile.delete();
                if (deleted) {
                    System.out.println("Level file deleted: " + levelFile.getAbsolutePath());

                    // Also try to delete overlay file if it exists
                    String[] overlayPaths = {
                            "src/main/resources/LevelOverlays",
                            "demo/src/main/resources/LevelOverlays",
                            "main/resources/LevelOverlays",
                            "resources/LevelOverlays",
                            "KUTowerDefense/resources/LevelOverlays"
                    };

                    for (String path : overlayPaths) {
                        File overlaysDir = new File(path);
                        if (overlaysDir.exists() && overlaysDir.isDirectory()) {
                            File overlayFile = new File(overlaysDir, levelName + "_overlay.json");
                            if (overlayFile.exists()) {
                                overlayFile.delete();
                                System.out.println("Overlay file deleted: " + overlayFile.getAbsolutePath());
                                break;
                            }
                        }
                    }

                    return true;
                } else {
                    System.err.println("Failed to delete level file: " + levelFile.getAbsolutePath());
                    return false;
                }
            } else {
                System.err.println("Level file not found: " + levelName + ".json");
                return false;
            }
        } catch (Exception e) {
            System.err.println("Error deleting level file " + levelName + ": " + e.getMessage());
            return false;
        }
    }

    private void startGame() {
        // Instead of going directly to PLAYING, go to SKILL_SELECTION first
        game.changeGameState(GameStates.SKILL_SELECTION);
    }
}