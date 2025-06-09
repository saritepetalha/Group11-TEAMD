package scenes;

import helpMethods.LoadSave;
import main.Game;
import main.GameStates;
import managers.ReplayManager;
import stats.GameStatsRecord;
import stats.ReplayRecord;
import ui_p.TheButton;

import java.awt.*;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;

public class StatisticsScene extends GameScene implements SceneMethods {

    private List<GameStatsRecord> stats;
    private int selectedIndex = -1;
    private TheButton backButton;
    private TheButton replayButton;
    private int scrollOffset = 0;
    private final int cardHeight = 120;
    private final int spacing = 20;
    int visibleAreaHeight = 500 - 100 - 20;
    private BufferedImage bg;
    private boolean scrollbarDragging = false;
    private int dragStartY = 0;
    private int dragStartScrollOffset = 0;

    private void refreshStats() {
        game.getStatsManager().loadFromFiles();
        this.stats = game.getStatsManager().getRecords();

        // Filter out duplicate records
        Set<String> uniqueStats = new HashSet<>();
        List<GameStatsRecord> filteredStats = new ArrayList<>();
        for (GameStatsRecord record : this.stats) {
            String key = record.getMapName() + "-" + record.getGold() + "-" + record.getEnemiesSpawned() + "-" + record.getTowersBuilt() + "-" + record.getTimePlayed() + "-" + record.isVictory();
            if (uniqueStats.add(key)) {
                filteredStats.add(record);
            }
        }
        this.stats = filteredStats;
    }

    public StatisticsScene(Game game) {
        super(game);
        int buttonWidth = 180;
        int buttonHeight = 50;
        int x = 400;
        int y = 400;
        backButton = new TheButton("Back", x, y, buttonWidth, buttonHeight);
        replayButton = new TheButton("Watch Replay", x, y - 60, buttonWidth, buttonHeight);
        bg = LoadSave.getImageFromPath("/KuTowerDefence1.jpg");
        refreshStats();
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
        int bottomPadding = 20;
        return Math.max(0, totalHeight - visibleAreaHeight + bottomPadding);
    }

    private Rectangle getScrollbarThumbBounds() {
        int cardX = 40;
        int cardYStart = 100;
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

            return new Rectangle(scrollbarX, thumbY, scrollbarWidth, thumbHeight);
        }

        return null; // No scrollbar visible
    }

    public void update() {}

    @Override
    public void render(Graphics g) {
        refreshStats();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, 1024, 768);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.25f));
        g2d.drawImage(bg, 0, 0, null);
        g2d.dispose();

        g.setFont(new Font("MV Boli", Font.BOLD, 28));
        g.setColor(Color.WHITE);
        g.drawString("GAME STATISTICS", 40, 60);

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

            replayButton.drawStyled(g);
        }

        backButton.drawStyled(g);
        int totalHeight = getTotalContentHeight();

        if (totalHeight > visibleAreaHeight) {
            int scrollbarX = cardX - 10;
            int scrollbarY = cardYStart;
            int scrollbarWidth = 10;
            int scrollbarHeight = visibleAreaHeight;
            int maxScrollOffset = getMaxScrollOffset();

            float ratio = visibleAreaHeight / (float) totalHeight;
            int thumbHeight = Math.max(20, (int) (scrollbarHeight * ratio)); // Minimum thumb size
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
        if (backButton.getBounds().contains(x, y)) {
            GameStates.gameState = GameStates.MENU;
        }

        if (selectedIndex >= 0 && selectedIndex < stats.size()) {
            if (replayButton.getBounds().contains(x, y)) {
                GameStatsRecord selectedRecord = stats.get(selectedIndex);
                // Load all replays and find the one with matching stats
                List<ReplayRecord> replays = ReplayManager.getInstance().loadAllReplays();
                ReplayRecord matchingReplay = null;
                
                for (ReplayRecord replay : replays) {
                    if (replay.getMapName().equals(selectedRecord.getMapName()) &&
                        replay.getTimePlayed() == selectedRecord.getTimePlayed() &&
                        replay.isVictory() == selectedRecord.isVictory()) {
                        matchingReplay = replay;
                        break;
                    }
                }
                
                if (matchingReplay != null) {
                    ReplayManager.getInstance().setCurrentReplay(matchingReplay);
                    GameStates.gameState = GameStates.PLAYING;
                    // Start the replay
                    game.getPlaying().getController().startReplay();
                } else {
                    System.err.println("No matching replay found for the selected game");
                }
            }
        }

        // Handle card selection
        int cardX = 40;
        int cardYStart = 100;
        int cardWidth = 300;
        int cardHeight = 120;
        int spacing = 20;

        for (int i = 0; i < stats.size(); i++) {
            int cardY = cardYStart + i * (cardHeight + spacing) - scrollOffset;
            Rectangle cardBounds = new Rectangle(cardX, cardY, cardWidth, cardHeight);
            if (cardBounds.contains(x, y)) {
                selectedIndex = i;
                break;
            }
        }
    }

    @Override
    public void mouseMoved(int x, int y) {
        backButton.setMouseOver(backButton.getBounds().contains(x, y));
        if (selectedIndex >= 0) {
            replayButton.setMouseOver(replayButton.getBounds().contains(x, y));
        }
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
