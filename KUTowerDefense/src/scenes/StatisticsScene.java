package scenes;

import helpMethods.LoadSave;
import main.Game;
import main.GameStates;
import stats.GameStatsRecord;
import ui_p.TheButton;
import constants.GameDimensions;

import java.awt.*;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.util.List;

public class StatisticsScene extends GameScene implements SceneMethods {

    private List<GameStatsRecord> stats;
    private int selectedIndex = -1;
    private TheButton backButton;
    private int scrollOffset = 0;
    private final int cardHeight = 120;
    private final int spacing = 20;
    private BufferedImage bg;
    private boolean scrollbarDragging = false;
    private int dragStartY = 0;
    private int dragStartScrollOffset = 0;

    public StatisticsScene(Game game) {
        super(game);
        bg = LoadSave.getImageFromPath("/KuTowerDefence1.jpg");

        game.getStatsManager().loadFromFiles();
        this.stats = game.getStatsManager().getRecords();
        updateButtonPosition();
    }

    private void updateButtonPosition() {
        int screenWidth = getScreenWidth();
        int screenHeight = getScreenHeight();

        int buttonWidth = 180;
        int buttonHeight = 50;
        int x = screenWidth - buttonWidth - 40;
        int y = screenHeight - buttonHeight - 40;

        backButton = new TheButton("Back", x, y, buttonWidth, buttonHeight);
    }

    private int getScreenWidth() {
        if (game.getFullscreenManager() != null && game.getFullscreenManager().isFullscreen()) {
            return game.getFullscreenManager().getScreenWidth();
        }
        return GameDimensions.MAIN_MENU_SCREEN_WIDTH;
    }

    private int getScreenHeight() {
        if (game.getFullscreenManager() != null && game.getFullscreenManager().isFullscreen()) {
            return game.getFullscreenManager().getScreenHeight();
        }
        return GameDimensions.MAIN_MENU_SCREEN_HEIGHT;
    }

    private int getVisibleAreaHeight() {
        return getScreenHeight() - 200; // 100 for header, 100 for bottom padding
    }

    private void drawCard(Graphics g, GameStatsRecord record, int x, int y, int width, int height, boolean selected) {
        Color bgColor = record.isVictory() ? new Color(0, 128, 64) : new Color(160, 32, 32);
        Color borderColor = selected ? Color.YELLOW : Color.DARK_GRAY;

        g.setColor(bgColor);
        g.fillRoundRect(x, y, width, height, 15, 15);

        g.setColor(borderColor);
        g.drawRoundRect(x, y, width, height, 15, 15);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Gill Sans MT", Font.BOLD, 17));
        g.drawString(record.isVictory() ? "VICTORY" : "DEFEAT", x + 10, y + 25);

        g.setFont(new Font("Gill Sans MT", Font.PLAIN, 15));
        g.drawString("Map: " + record.getMapName(), x + 10, y + 50);
        g.drawString("Gold: " + record.getGold(), x + 10, y + 70);
        g.drawString("Enemy Spawned: " + record.getEnemiesSpawned(), x + 10, y + 90);
        g.drawString("Tower Built: " + record.getTowersBuilt(), x + 10, y + 110);
    }

    private void drawDetails(Graphics g, GameStatsRecord record, int x, int y) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Gill Sans MT", Font.BOLD, 18));
        g.drawString("Details", x, y);

        g.setFont(new Font("Gill Sans MT", Font.PLAIN, 16));
        FontMetrics fm = g.getFontMetrics();

        int lineY = y + 30;
        int labelX = x;
        int rightAlignX = x + 200;

        g.drawString("Map Name:", labelX, lineY);
        String mapName = record.getMapName();
        g.drawString(mapName, rightAlignX - fm.stringWidth(mapName), lineY);
        lineY += 20;

        g.drawString("Result:", labelX, lineY);
        String result = record.isVictory() ? "Victory" : "Defeat";
        g.drawString(result, rightAlignX - fm.stringWidth(result), lineY);
        lineY += 20;

        g.drawString("Gold Earned:", labelX, lineY);
        String gold = String.valueOf(record.getGold());
        g.drawString(gold, rightAlignX - fm.stringWidth(gold), lineY);
        lineY += 20;

        g.drawString("Enemies Spawned:", labelX, lineY);
        String enemiesSpawned = String.valueOf(record.getEnemiesSpawned());
        g.drawString(enemiesSpawned, rightAlignX - fm.stringWidth(enemiesSpawned), lineY);
        lineY += 20;

        g.drawString("Enemies Reached End:", labelX, lineY);
        String enemiesReached = String.valueOf(record.getEnemiesReachedEnd());
        g.drawString(enemiesReached, rightAlignX - fm.stringWidth(enemiesReached), lineY);
        lineY += 20;

        g.drawString("Enemies Defeated:", labelX, lineY);
        String enemiesDefeated = String.valueOf(record.getEnemyDefeated());
        g.drawString(enemiesDefeated, rightAlignX - fm.stringWidth(enemiesDefeated), lineY);
        lineY += 20;

        g.drawString("Towers Built:", labelX, lineY);
        String towersBuilt = String.valueOf(record.getTowersBuilt());
        g.drawString(towersBuilt, rightAlignX - fm.stringWidth(towersBuilt), lineY);
        lineY += 20;

        g.drawString("Total Damage:", labelX, lineY);
        String totalDamage = String.valueOf(record.getTotalDamage());
        g.drawString(totalDamage, rightAlignX - fm.stringWidth(totalDamage), lineY);
        lineY += 20;

        g.drawString("Time Played:", labelX, lineY);
        String timePlayed = record.getTimePlayed() + "s";
        g.drawString(timePlayed, rightAlignX - fm.stringWidth(timePlayed), lineY);
    }

    private int getTotalContentHeight() {
        if (stats.size() > 0) {
            return (stats.size() - 1) * (cardHeight + spacing) + cardHeight;
        }
        return 0;
    }

    private int getMaxScrollOffset() {
        int totalHeight = getTotalContentHeight();
        int visibleAreaHeight = getVisibleAreaHeight();
        int bottomPadding = 20;
        return Math.max(0, totalHeight - visibleAreaHeight + bottomPadding);
    }

    private Rectangle getScrollbarThumbBounds() {
        int screenWidth = getScreenWidth();
        int cardX = (int) (screenWidth * 0.05); // 5% margin from left
        int cardYStart = 100;
        int totalHeight = getTotalContentHeight();
        int visibleAreaHeight = getVisibleAreaHeight();

        if (totalHeight > visibleAreaHeight) {
            int scrollbarX = cardX - 10;
            int scrollbarY = cardYStart;
            int scrollbarWidth = 10;
            int scrollbarHeight = visibleAreaHeight;
            int maxScrollOffset = getMaxScrollOffset();

            float ratio = visibleAreaHeight / (float) totalHeight;
            int thumbHeight = Math.max(20, (int) (scrollbarHeight * ratio));
            int maxThumbY = scrollbarY + scrollbarHeight - thumbHeight;
            int thumbY = scrollbarY + (int) ((scrollOffset / (float) maxScrollOffset) * (scrollbarHeight - thumbHeight));

            thumbY = Math.max(scrollbarY, Math.min(thumbY, maxThumbY));

            return new Rectangle(scrollbarX, thumbY, scrollbarWidth, thumbHeight);
        }

        return null; // No scrollbar visible
    }

    public void update() {
        // Update button position in case fullscreen mode changed
        updateButtonPosition();
    }

    @Override
    public void render(Graphics g) {
        int screenWidth = getScreenWidth();
        int screenHeight = getScreenHeight();

        // Fill background
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, screenWidth, screenHeight);

        // Draw background image scaled to screen
        if (bg != null) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.25f));
            g2d.drawImage(bg, 0, 0, screenWidth, screenHeight, null);
            g2d.dispose();
        }

        // Draw title
        g.setFont(new Font("MV Boli", Font.BOLD, 28));
        g.setColor(Color.WHITE);
        int titleX = (int) (screenWidth * 0.05); // 5% margin from left
        g.drawString("GAME STATISTICS", titleX, 60);

        // Calculate responsive dimensions
        int cardX = titleX;
        int cardYStart = 100;
        int cardWidth = (int) (screenWidth * 0.3); // 30% of screen width
        int visibleAreaHeight = getVisibleAreaHeight();

        // Create clipping area for cards
        Graphics clippedG = g.create();
        clippedG.setClip(cardX, cardYStart, cardWidth + 20, visibleAreaHeight);

        // Draw cards
        for (int i = 0; i < stats.size(); i++) {
            int y = cardYStart + i * (cardHeight + spacing) - scrollOffset;

            if (y + cardHeight >= cardYStart && y <= cardYStart + visibleAreaHeight) {
                drawCard(clippedG, stats.get(i), cardX, y, cardWidth, cardHeight, i == selectedIndex);
            }
        }

        clippedG.dispose();

        // Draw details panel
        if (selectedIndex >= 0 && selectedIndex < stats.size()) {
            GameStatsRecord selected = stats.get(selectedIndex);

            int detailX = cardX + cardWidth + 40;
            int detailY = 120;
            int detailWidth = (int) (screenWidth * 0.25); // 25% of screen width
            int detailHeight = 260;

            g.setColor(new Color(255, 255, 255, 20));
            g.fillRoundRect(detailX - 20, 90, detailWidth, detailHeight, 15, 15);

            drawDetails(g, selected, detailX, detailY);
        }

        // Draw back button
        backButton.drawStyled(g);

        // Draw scrollbar
        int totalHeight = getTotalContentHeight();
        if (totalHeight > visibleAreaHeight) {
            int scrollbarX = cardX - 10;
            int scrollbarY = cardYStart;
            int scrollbarWidth = 10;
            int scrollbarHeight = visibleAreaHeight;
            int maxScrollOffset = getMaxScrollOffset();

            float ratio = visibleAreaHeight / (float) totalHeight;
            int thumbHeight = Math.max(20, (int) (scrollbarHeight * ratio));
            int maxThumbY = scrollbarY + scrollbarHeight - thumbHeight;
            int thumbY = scrollbarY + (int) ((scrollOffset / (float) maxScrollOffset) * (scrollbarHeight - thumbHeight));

            thumbY = Math.max(scrollbarY, Math.min(thumbY, maxThumbY));

            g.setColor(new Color(100, 100, 100, 100));
            g.fillRoundRect(scrollbarX, scrollbarY, scrollbarWidth, scrollbarHeight, 5, 5);

            Color thumbColor = scrollbarDragging ?
                    new Color(255, 255, 255, 200) :
                    new Color(200, 200, 200, 200);
            g.setColor(thumbColor);
            g.fillRoundRect(scrollbarX, thumbY, scrollbarWidth, thumbHeight, 5, 5);
        }
    }

    @Override
    public void mouseClicked(int x, int y) {
        int screenWidth = getScreenWidth();
        int cardX = (int) (screenWidth * 0.05);
        int cardYStart = 100;
        int cardWidth = (int) (screenWidth * 0.3);
        int visibleAreaHeight = getVisibleAreaHeight();

        // Only check for card clicks if the click is within the scrollable area
        if (x >= cardX && x <= cardX + cardWidth && y >= cardYStart && y <= cardYStart + visibleAreaHeight) {
            for (int i = 0; i < stats.size(); i++) {
                int cardY = cardYStart + i * (cardHeight + spacing) - scrollOffset;

                // Only check cards that are visible within the clipped area
                if (cardY + cardHeight >= cardYStart && cardY <= cardYStart + visibleAreaHeight) {
                    Rectangle cardBounds = new Rectangle(cardX, cardY, cardWidth, cardHeight);
                    if (cardBounds.contains(x, y)) {
                        selectedIndex = i;
                        return;
                    }
                }
            }
        }

        if (backButton.getBounds().contains(x, y)) {
            playButtonClickSound();
            GameStates.setGameState(GameStates.MENU);
        }
    }

    @Override
    public void mouseMoved(int x, int y) {
        backButton.setMouseOver(backButton.getBounds().contains(x, y));
    }

    @Override
    public void mousePressed(int x, int y) {
        Rectangle thumbBounds = getScrollbarThumbBounds();
        if (thumbBounds != null && thumbBounds.contains(x, y)) {
            scrollbarDragging = true;
            dragStartY = y;
            dragStartScrollOffset = scrollOffset;
            return;
        }

        if (thumbBounds != null) {
            int screenWidth = getScreenWidth();
            int cardX = (int) (screenWidth * 0.05);
            int cardYStart = 100;
            int scrollbarX = cardX - 10;
            int scrollbarWidth = 10;
            int visibleAreaHeight = getVisibleAreaHeight();

            Rectangle scrollbarTrack = new Rectangle(scrollbarX, cardYStart, scrollbarWidth, visibleAreaHeight);
            if (scrollbarTrack.contains(x, y)) {
                int relativeY = y - cardYStart;
                float scrollRatio = (float) relativeY / visibleAreaHeight;
                int maxScrollOffset = getMaxScrollOffset();

                scrollOffset = (int) (scrollRatio * maxScrollOffset);
                scrollOffset = Math.max(0, Math.min(scrollOffset, maxScrollOffset));
                return;
            }
        }

        backButton.setMousePressed(backButton.getBounds().contains(x, y));
    }

    @Override
    public void mouseReleased(int x, int y) {
        scrollbarDragging = false;
        backButton.resetBooleans();
    }

    @Override
    public void mouseDragged(int x, int y) {
        if (scrollbarDragging) {
            int totalHeight = getTotalContentHeight();
            int visibleAreaHeight = getVisibleAreaHeight();

            if (totalHeight <= visibleAreaHeight) {
                return;
            }

            int maxScrollOffset = getMaxScrollOffset();

            if (maxScrollOffset > 0) {
                int deltaY = y - dragStartY;

                int scrollbarHeight = visibleAreaHeight;
                float ratio = visibleAreaHeight / (float) totalHeight;
                int thumbHeight = Math.max(20, (int) (scrollbarHeight * ratio));
                int availableThumbTravel = scrollbarHeight - thumbHeight;

                if (availableThumbTravel > 0) {
                    float scrollRatio = (float) deltaY / availableThumbTravel;
                    int scrollDelta = (int) (scrollRatio * maxScrollOffset);

                    scrollOffset = dragStartScrollOffset + scrollDelta;
                    scrollOffset = Math.max(0, Math.min(scrollOffset, maxScrollOffset));
                }
            }
        }
    }

    public void mouseWheelMoved(MouseWheelEvent e) {
        int rotation = e.getWheelRotation();
        int scrollAmount = rotation * 20;
        int maxScrollOffset = getMaxScrollOffset();

        scrollOffset += scrollAmount;
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScrollOffset));
    }
}
