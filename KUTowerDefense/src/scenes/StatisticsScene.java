package scenes;

import helpMethods.LoadSave;
import main.Game;
import main.GameStates;
import managers.GameStatsManager;
import stats.GameStatsRecord;
import ui_p.TheButton;

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
    int visibleAreaHeight = 500 - 100 - 20;
    private BufferedImage bg;

    private boolean scrollbarDragging = false;
    private int dragStartY = 0;
    private int dragStartScrollOffset = 0;

    public StatisticsScene(Game game) {
        super(game);
        int buttonWidth = 180;
        int buttonHeight = 50;
        int x = 400;
        int y = 400;
        backButton = new TheButton("Back", x, y, buttonWidth, buttonHeight);
        bg = LoadSave.getImageFromPath("/KuTowerDefence1.jpg");

        game.getStatsManager().loadFromFiles();

        this.stats = game.getStatsManager().getRecords();
    }

    private void drawCard(Graphics g, GameStatsRecord record, int x, int y, int width, int height, boolean selected) {
        Color bgColor = record.isVictory() ? new Color(0, 128, 64) : new Color(160, 32, 32);
        Color borderColor = selected ? Color.YELLOW : Color.DARK_GRAY;

        g.setColor(bgColor);
        g.fillRoundRect(x, y, width, height, 15, 15);

        g.setColor(borderColor);
        g.drawRoundRect(x, y, width, height, 15, 15);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString(record.isVictory() ? "Victory" : "Defeat", x + 10, y + 25);

        g.setFont(new Font("Arial", Font.PLAIN, 12));
        g.drawString("Map: " + record.getMapName(), x + 10, y + 45);
        g.drawString("Gold: " + record.getGold(), x + 10, y + 65);
        g.drawString("Enemy Spawned: " + record.getEnemiesSpawned(), x + 10, y + 85);
        g.drawString("Tower Built: " + record.getTowersBuilt(), x + 10, y + 105);
    }

    private void drawDetails(Graphics g, GameStatsRecord record, int x, int y) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 18));
        g.drawString("Details", x, y);

        g.setFont(new Font("Arial", Font.PLAIN, 14));
        int lineY = y + 30;
        g.drawString("Map Name: " + record.getMapName(), x, lineY);
        lineY += 20;
        g.drawString("Result: " + (record.isVictory() ? "Victory" : "Defeat"), x, lineY);
        lineY += 20;
        g.drawString("Gold Earned: " + record.getGold(), x, lineY);
        lineY += 20;
        g.drawString("Enemies Spawned: " + record.getEnemiesSpawned(), x, lineY);
        lineY += 20;
        g.drawString("Enemies Reached End: " + record.getEnemiesReachedEnd(), x, lineY);
        lineY += 20;
        g.drawString("Enemies Defeated: " + record.getEnemyDefeated(), x, lineY);
        lineY += 20;
        g.drawString("Towers Built: " + record.getTowersBuilt(), x, lineY);
        lineY += 20;
        g.drawString("Total Damage: " + record.getTotalDamage(), x, lineY);
        lineY += 20;
        g.drawString("Time Played: " + record.getTimePlayed() + "s", x, lineY);
    }

    private Rectangle getScrollbarThumbBounds() {
        int cardX = 40;
        int cardYStart = 100;

        int totalHeight;
        if (stats.size() > 0) {
            totalHeight = (stats.size() - 1) * (cardHeight + spacing) + cardHeight;
        } else {
            totalHeight = 0;
        }

        if (totalHeight > visibleAreaHeight) {
            int scrollbarX = cardX - 10;
            int scrollbarY = cardYStart;
            int scrollbarWidth = 10;
            int scrollbarHeight = visibleAreaHeight;

            int bottomPadding = 20;
            int maxScrollOffset = Math.max(0, totalHeight - visibleAreaHeight + bottomPadding);

            float ratio = visibleAreaHeight / (float) totalHeight;
            int thumbHeight = Math.max(20, (int) (scrollbarHeight * ratio));
            int maxThumbY = scrollbarY + scrollbarHeight - thumbHeight;
            int thumbY = scrollbarY + (int) ((scrollOffset / (float) maxScrollOffset) * (scrollbarHeight - thumbHeight));

            thumbY = Math.max(scrollbarY, Math.min(thumbY, maxThumbY));

            return new Rectangle(scrollbarX, thumbY, scrollbarWidth, thumbHeight);
        }

        return null;
    }

    public void update() {}

    @Override
    public void render(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, 1024, 768);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.25f));
        g2d.drawImage(bg, 0, 0, null);
        g2d.dispose();

        g.setFont(new Font("Arial", Font.BOLD, 28));
        g.setColor(Color.WHITE);
        g.drawString("Game Statistics", 40, 60);

        int cardX = 40;
        int cardYStart = 100;
        int cardWidth = 300;
        int cardHeight = 120;
        int spacing = 20;
        Graphics clippedG = g.create();
        clippedG.setClip(cardX, cardYStart, cardWidth + 20, visibleAreaHeight);

        for (int i = 0; i < stats.size(); i++) {
            int y = cardYStart + i * (cardHeight + spacing) - scrollOffset;

            if (y + cardHeight >= cardYStart && y <= cardYStart + visibleAreaHeight) {
                drawCard(clippedG, stats.get(i), cardX, y, cardWidth, cardHeight, i == selectedIndex);
            }
        }

        clippedG.dispose();

        if (selectedIndex >= 0 && selectedIndex < stats.size()) {
            GameStatsRecord selected = stats.get(selectedIndex);

            int detailX = 380;
            int detailY = 120;
            g.setColor(new Color(255, 255, 255, 20));
            g.fillRoundRect(detailX - 20, 90, 230, 260, 15, 15);

            drawDetails(g, selected, detailX, detailY);

        }
        backButton.drawStyled(g);

        int totalHeight;
        if (stats.size() > 0) {
            totalHeight = (stats.size() - 1) * (cardHeight + spacing) + cardHeight;
        } else {
            totalHeight = 0;
        }

        if (totalHeight > visibleAreaHeight) {
            int scrollbarX = cardX - 10;
            int scrollbarY = cardYStart;
            int scrollbarWidth = 10;
            int scrollbarHeight = visibleAreaHeight;

            int bottomPadding = 20;
            int maxScrollOffset = Math.max(0, totalHeight - visibleAreaHeight + bottomPadding);

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
        int cardX = 40;
        int cardYStart = 100;
        int cardWidth = 300;
        int cardHeight = 120;
        int spacing = 20;

        if (x >= cardX && x <= cardX + cardWidth && y >= cardYStart && y <= cardYStart + visibleAreaHeight) {
            for (int i = 0; i < stats.size(); i++) {
                int cardY = cardYStart + i * (cardHeight + spacing) - scrollOffset;

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
            int cardX = 40;
            int cardYStart = 100;
            int scrollbarX = cardX - 10;
            int scrollbarWidth = 10;

            Rectangle scrollbarTrack = new Rectangle(scrollbarX, cardYStart, scrollbarWidth, visibleAreaHeight);
            if (scrollbarTrack.contains(x, y)) {
                int relativeY = y - cardYStart;
                float scrollRatio = (float) relativeY / visibleAreaHeight;
                int totalHeight;
                if (stats.size() > 0) {
                    totalHeight = (stats.size() - 1) * (cardHeight + spacing) + cardHeight;
                } else {
                    totalHeight = 0;
                }

                int bottomPadding = 20;
                int maxScrollOffset = Math.max(0, totalHeight - visibleAreaHeight + bottomPadding);

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
            int cardYStart = 100;

            int totalHeight;
            if (stats.size() > 0) {
                totalHeight = (stats.size() - 1) * (cardHeight + spacing) + cardHeight;
            } else {
                totalHeight = 0;
            }

            int bottomPadding = 20;
            int maxScrollOffset = Math.max(0, totalHeight - visibleAreaHeight + bottomPadding);

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

        int totalHeight;
        if (stats.size() > 0) {
            totalHeight = (stats.size() - 1) * (cardHeight + spacing) + cardHeight;
        } else {
            totalHeight = 0;
        }

        int bottomPadding = 20;
        int maxScrollOffset = Math.max(0, totalHeight - visibleAreaHeight + bottomPadding);

        scrollOffset += scrollAmount;
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScrollOffset));
    }


}
