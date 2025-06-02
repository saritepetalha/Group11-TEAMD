package scenes;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import constants.GameDimensions;
import helpMethods.FontLoader;
import main.Game;
import ui_p.AssetsLoader;
import ui_p.TheButton;

public abstract class LevelSelectionScreen extends JPanel {

    protected Game game;
    protected BufferedImage backgroundImg;
    protected TheButton backButton;

    // UI Constants
    protected static final int PREVIEW_WIDTH = 192;
    protected static final int PREVIEW_HEIGHT = 108;
    protected static final int PREVIEW_MARGIN = 10;
    protected static final int PREVIEWS_PER_ROW = 2;
    protected static final int PREVIEWS_PER_PAGE = 4;
    protected static final int HEADER_HEIGHT = 80;
    protected static final int FOOTER_HEIGHT = 80;

    // Fonts
    protected Font medodicaFontSmall;
    protected Font medodicaFontMedium;
    protected Font mvBoliFontBold;

    // Pagination
    protected int currentPage = 0;
    protected int totalPages = 0;
    protected ArrayList<String> filteredLevels = new ArrayList<>();
    protected JPanel mainContentPanel;

    public LevelSelectionScreen(Game game) {
        this.game = game;
        this.backgroundImg = AssetsLoader.getInstance().loadGameMenuBackgroundImg;

        // Initialize fonts
        this.medodicaFontSmall = FontLoader.loadMedodicaFont(14f);
        this.medodicaFontMedium = FontLoader.loadMedodicaFont(16f);
        this.mvBoliFontBold = new Font("MV Boli", Font.BOLD, 14);

        setPreferredSize(new Dimension(GameDimensions.MAIN_MENU_SCREEN_WIDTH, GameDimensions.MAIN_MENU_SCREEN_HEIGHT));
        setLayout(new BorderLayout());

        initBackButton();
    }

    private void initBackButton() {
        backButton = new TheButton("Back", 50, GameDimensions.MAIN_MENU_SCREEN_HEIGHT - 70, 120, 40);
    }

    /**
     * Template method that defines the algorithm for displaying levels
     */
    public final void displayLevels() {
        filteredLevels = getLevelsToDisplay();
        totalPages = Math.max(1, (int) Math.ceil((double) filteredLevels.size() / PREVIEWS_PER_PAGE));

        if (currentPage >= totalPages) {
            currentPage = Math.max(0, totalPages - 1);
        }

        createUI();
        refreshDisplay();
    }

    /**
     * Abstract method - subclasses define which levels to show
     */
    protected abstract ArrayList<String> getLevelsToDisplay();

    /**
     * Abstract method - subclasses define the title of the screen
     */
    protected abstract String getScreenTitle();

    /**
     * Abstract method - subclasses define what happens when a level is selected
     */
    protected abstract void onLevelSelected(String levelName);

    /**
     * Hook method - subclasses can override to add additional buttons per level
     */
    protected java.util.List<JButton> createAdditionalButtons(String levelName) {
        return new ArrayList<>();
    }

    /**
     * Creates the main UI structure
     */
    protected void createUI() {
        removeAll();

        // Header with title
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Main content area
        mainContentPanel = createMainContentPanel();
        add(mainContentPanel, BorderLayout.CENTER);

        // Footer with back button and pagination
        JPanel footerPanel = createFooterPanel();
        add(footerPanel, BorderLayout.SOUTH);
    }

    protected JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        headerPanel.setPreferredSize(new Dimension(GameDimensions.MAIN_MENU_SCREEN_WIDTH, HEADER_HEIGHT));

        JLabel titleLabel = new JLabel(getScreenTitle(), SwingConstants.CENTER);
        titleLabel.setFont(medodicaFontMedium.deriveFont(Font.BOLD, 24f));
        titleLabel.setForeground(Color.WHITE);

        headerPanel.add(titleLabel);

        return headerPanel;
    }

    protected abstract JPanel createMainContentPanel();

    protected JPanel createFooterPanel() {
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setOpaque(false);
        footerPanel.setPreferredSize(new Dimension(GameDimensions.MAIN_MENU_SCREEN_WIDTH, FOOTER_HEIGHT));

        // Back button on the left
        JPanel backButtonWrapper = createTheButtonWrapper(backButton);
        footerPanel.add(backButtonWrapper, BorderLayout.WEST);

        // Pagination in center (if needed)
        if (totalPages > 1) {
            JPanel paginationPanel = createPaginationPanel();
            footerPanel.add(paginationPanel, BorderLayout.CENTER);
        }

        return footerPanel;
    }

    protected JPanel createPaginationPanel() {
        // Implementation for pagination - similar to LoadGameMenu
        JPanel paginationPanel = new JPanel(new FlowLayout());
        paginationPanel.setOpaque(false);

        JButton prevButton = new JButton("Previous");
        JButton nextButton = new JButton("Next");
        JLabel pageLabel = new JLabel("Page " + (currentPage + 1) + " of " + totalPages);

        prevButton.setEnabled(currentPage > 0);
        nextButton.setEnabled(currentPage < totalPages - 1);

        prevButton.addActionListener(e -> {
            if (currentPage > 0) {
                currentPage--;
                refreshDisplay();
            }
        });

        nextButton.addActionListener(e -> {
            if (currentPage < totalPages - 1) {
                currentPage++;
                refreshDisplay();
            }
        });

        paginationPanel.add(prevButton);
        paginationPanel.add(pageLabel);
        paginationPanel.add(nextButton);

        return paginationPanel;
    }

    protected JPanel createTheButtonWrapper(TheButton button) {
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
                onBackButtonClicked();
            }
        });
        return wrapper;
    }

    protected void onBackButtonClicked() {
        game.changeGameState(main.GameStates.MENU);
    }

    protected void refreshDisplay() {
        if (mainContentPanel != null) {
            remove(mainContentPanel);
        }
        mainContentPanel = createMainContentPanel();
        add(mainContentPanel, BorderLayout.CENTER);
        revalidate();
        repaint();
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
}