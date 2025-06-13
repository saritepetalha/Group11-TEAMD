package scenes;

import main.Game;
import main.GameStates;
import skills.SkillType;
import skills.SkillTree;
import ui_p.TheButton;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import static helpMethods.LoadSave.resizeImage;

public class SkillSelectionScene extends JPanel {
    private static final int BUTTON_WIDTH = 180;
    private static final int BUTTON_HEIGHT = 75;
    private static final int BUTTON_SPACING = 30;
    private static final int TITLE_Y = 40;
    private static final int SKILLS_START_Y = 80;
    private static final int SKILLS_PER_ROW = 3;
    private static final int ROW_SPACING = 100;
    private static final int CATEGORY_TITLE_OFFSET = 10;

    private final List<SkillButton> skillButtons;
    private final SkillTree skillTree;
    private final Playing playing;
    private final Game game;
    private final JPanel contentPanel;
    private TheButton startGameButtonStyled;
    private BufferedImage backgroundImg;
    private TheButton backButtonStyled;
    private JPanel styledButtonPanel;

    public SkillSelectionScene(Game game, Playing playing) {
        this.game = game;
        this.playing = playing;
        this.skillTree = SkillTree.getInstance();
        this.skillButtons = new ArrayList<>();

        // Try to load background image
        try {
            backgroundImg = ImageIO.read(getClass().getResource("/KuTowerDefence1.jpg"));
        } catch (IOException | IllegalArgumentException e) {
            backgroundImg = null;
        }

        setLayout(new BorderLayout());
        // Keep preferred size for proper scaling with wrapper
        setPreferredSize(new Dimension(1280, 720));

        // İçerik paneli (skill grid)
        contentPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Only draw skill content, no background!
                drawSkillContent(g);
            }
        };
        contentPanel.setOpaque(false);
        contentPanel.setPreferredSize(new Dimension(1280, 520));
        contentPanel.setLayout(null);

        initializeButtons();

        JScrollPane scrollPane = new JScrollPane(contentPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        add(scrollPane, BorderLayout.CENTER);

        // Alt panel ve buton
        styledButtonPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (backButtonStyled != null) backButtonStyled.drawStyled(g);
                if (startGameButtonStyled != null) startGameButtonStyled.drawStyled(g);
            }
        };
        styledButtonPanel.setOpaque(false);
        styledButtonPanel.setPreferredSize(new Dimension(1280, 100));
        styledButtonPanel.setLayout(null);

        int buttonY = 20;
        int backButtonWidth = 180, backButtonHeight = 60;
        int startButtonWidth = 250, startButtonHeight = 60;
        int backButtonX = 1280/2 - backButtonWidth - 300;
        int startButtonX = 1280/2 - 260;

        backButtonStyled = new TheButton("Back", backButtonX, buttonY, backButtonWidth, backButtonHeight);
        startGameButtonStyled = new TheButton("Start Game", startButtonX, buttonY, startButtonWidth, startButtonHeight);

        styledButtonPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                if (backButtonStyled.getBounds().contains(e.getX(), e.getY())) {
                    backButtonStyled.setMousePressed(true);
                    styledButtonPanel.repaint();
                } else if (startGameButtonStyled.getBounds().contains(e.getX(), e.getY())) {
                    startGameButtonStyled.setMousePressed(true);
                    styledButtonPanel.repaint();
                }
            }
            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
                if (backButtonStyled.isMousePressed() && backButtonStyled.getBounds().contains(e.getX(), e.getY())) {
                    backButtonStyled.setMousePressed(false);
                    game.changeGameState(main.GameStates.MENU);
                } else if (startGameButtonStyled.isMousePressed() && startGameButtonStyled.getBounds().contains(e.getX(), e.getY())) {
                    startGameButtonStyled.setMousePressed(false);
                    startGame();
                }
                backButtonStyled.setMousePressed(false);
                startGameButtonStyled.setMousePressed(false);
                styledButtonPanel.repaint();
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                backButtonStyled.setMouseOver(false);
                startGameButtonStyled.setMouseOver(false);
                styledButtonPanel.repaint();
            }
        });
        styledButtonPanel.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseMoved(java.awt.event.MouseEvent e) {
                boolean repaint = false;
                if (backButtonStyled.getBounds().contains(e.getX(), e.getY())) {
                    if (!backButtonStyled.isMouseOver()) repaint = true;
                    backButtonStyled.setMouseOver(true);
                } else {
                    if (backButtonStyled.isMouseOver()) repaint = true;
                    backButtonStyled.setMouseOver(false);
                }
                if (startGameButtonStyled.getBounds().contains(e.getX(), e.getY())) {
                    if (!startGameButtonStyled.isMouseOver()) repaint = true;
                    startGameButtonStyled.setMouseOver(true);
                } else {
                    if (startGameButtonStyled.isMouseOver()) repaint = true;
                    startGameButtonStyled.setMouseOver(false);
                }
                if (repaint) styledButtonPanel.repaint();
            }
        });
        add(styledButtonPanel, BorderLayout.SOUTH);

        // Mouse eventleri
        contentPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                handleMouseClick(e.getX(), e.getY());
            }
        });
        contentPanel.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseMoved(java.awt.event.MouseEvent e) {
                handleMouseMove(e.getX(), e.getY());
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Draw background image for the whole panel
        if (backgroundImg != null) {
            BufferedImage scaled = resizeImage(backgroundImg, getWidth(), getHeight());
            g.drawImage(scaled, 0, 0, null);
            // Semi-transparent overlay for readability
            g.setColor(new Color(0, 0, 0, 180));
            g.fillRect(0, 0, getWidth(), getHeight());
        } else {
            g.setColor(new Color(0, 0, 0, 200));
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    private void initializeButtons() {
        // Calculate dynamic positioning based on panel width
        int panelWidth = 1280; // Use base width for calculations
        int availableWidth = panelWidth - 40; // Subtract left/right margins
        int totalButtonsWidth = BUTTON_WIDTH * 3 + BUTTON_SPACING * 2;
        int extraSpace = availableWidth - totalButtonsWidth;

        // Distribute buttons evenly across the available width
        int startX = 20 + extraSpace / 6; // Add some of the extra space as left margin
        int adjustedSpacing = BUTTON_SPACING + extraSpace / 3; // Increase spacing between buttons

        int y = SKILLS_START_Y;

        // Economy Skills
        addSkillButton(startX, y, SkillType.BOUNTIFUL_START, "Economy");
        addSkillButton(startX + BUTTON_WIDTH + adjustedSpacing, y, SkillType.PLUNDERER_BONUS, "Economy");
        addSkillButton(startX + (BUTTON_WIDTH + adjustedSpacing) * 2, y, SkillType.INTEREST_SYSTEM, "Economy");

        // Tower Skills
        y += ROW_SPACING;
        addSkillButton(startX, y, SkillType.SHARP_ARROW_TIPS, "Tower");
        addSkillButton(startX + BUTTON_WIDTH + adjustedSpacing, y, SkillType.EAGLE_EYE, "Tower");
        addSkillButton(startX + (BUTTON_WIDTH + adjustedSpacing) * 2, y, SkillType.MAGIC_PIERCING, "Tower");

        // Ultimate Skills
        y += ROW_SPACING;
        addSkillButton(startX, y, SkillType.SHATTERING_FORCE, "Ultimate");
        addSkillButton(startX + BUTTON_WIDTH + adjustedSpacing, y, SkillType.DIVINE_WRATH, "Ultimate");
        addSkillButton(startX + (BUTTON_WIDTH + adjustedSpacing) * 2, y, SkillType.BATTLE_READINESS, "Ultimate");
    }

    private void addSkillButton(int x, int y, SkillType skillType, String category) {
        skillButtons.add(new SkillButton(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, skillType, category));
    }

    private void drawSkillContent(Graphics g) {
        // Remove all background drawing here!
        // Draw title (left aligned, smaller font)
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 26));
        String title = "Skill Selection";
        g.drawString(title, 20, TITLE_Y);

        // Calculate category title positions to match button positions
        int panelWidth = 1280;
        int availableWidth = panelWidth - 40;
        int totalButtonsWidth = BUTTON_WIDTH * 3 + BUTTON_SPACING * 2;
        int extraSpace = availableWidth - totalButtonsWidth;
        int categoryStartX = 20 + extraSpace / 6;

        // Draw category titles
        g.setFont(new Font("Arial", Font.BOLD, 18));
        g.drawString("Economy Skills", categoryStartX, SKILLS_START_Y - CATEGORY_TITLE_OFFSET);
        g.drawString("Tower Skills", categoryStartX, SKILLS_START_Y + ROW_SPACING - CATEGORY_TITLE_OFFSET);
        g.drawString("Ultimate Skills", categoryStartX, SKILLS_START_Y + ROW_SPACING * 2 - CATEGORY_TITLE_OFFSET);

        // Draw skill buttons
        for (SkillButton button : skillButtons) {
            button.draw(g);
        }
    }

    private void handleMouseClick(int x, int y) {
        for (SkillButton button : skillButtons) {
            if (button.getBounds().contains(x, y)) {
                handleSkillButtonClick(button);
                contentPanel.repaint();
                break;
            }
        }
    }

    private void handleMouseMove(int x, int y) {
        boolean needsRepaint = false;
        for (SkillButton button : skillButtons) {
            boolean wasHovered = button.isHovered();
            button.setHovered(button.getBounds().contains(x, y));
            if (wasHovered != button.isHovered()) {
                needsRepaint = true;
            }
        }
        if (needsRepaint) {
            contentPanel.repaint();
        }
    }

    private void handleSkillButtonClick(SkillButton button) {
        // Aynı kategoride başka bir skill seçiliyse onu kaldır
        if (!button.isSelected()) {
            for (SkillButton other : skillButtons) {
                if (other != button && other.getCategory().equals(button.getCategory()) && other.isSelected()) {
                    other.setSelected(false);
                    skillTree.unselectSkill(other.getSkillType());
                }
            }
        }
        button.setSelected(!button.isSelected());
        if (button.isSelected()) {
            skillTree.selectSkill(button.getSkillType());
        } else {
            skillTree.unselectSkill(button.getSkillType());
        }
    }

    private void startGame() {
        game.changeGameState(GameStates.PLAYING);
    }

    // Inner class for skill buttons
    private static class SkillButton {
        private final Rectangle bounds;
        private final SkillType skillType;
        private final String category;
        private boolean selected;
        private boolean hovered;

        public SkillButton(int x, int y, int width, int height, SkillType skillType, String category) {
            this.bounds = new Rectangle(x, y, width, height);
            this.skillType = skillType;
            this.category = category;
            this.selected = false;
            this.hovered = false;
        }

        public void draw(Graphics g) {
            if (selected) {
                g.setColor(new Color(0, 150, 0));
            } else if (hovered) {
                g.setColor(new Color(100, 100, 100));
            } else {
                g.setColor(new Color(50, 50, 50));
            }
            g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);

            g.setColor(Color.WHITE);
            g.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);

            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 14));
            String name = getEnglishSkillName(skillType);
            int nameWidth = g.getFontMetrics().stringWidth(name);
            g.drawString(name, bounds.x + (bounds.width - nameWidth) / 2, bounds.y + 23);

            g.setFont(new Font("Arial", Font.PLAIN, 11));
            String description = getEnglishSkillDescription(skillType);
            int maxWidth = bounds.width - 16;
            List<String> lines = wrapText(description, g.getFontMetrics(), maxWidth);
            int lineY = bounds.y + 42;
            for (String line : lines) {
                int descWidth = g.getFontMetrics().stringWidth(line);
                g.drawString(line, bounds.x + (bounds.width - descWidth) / 2, lineY);
                lineY += 13;
            }
        }

        public Rectangle getBounds() {
            return bounds;
        }

        public SkillType getSkillType() {
            return skillType;
        }

        public String getCategory() {
            return category;
        }

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        public boolean isHovered() {
            return hovered;
        }

        public void setHovered(boolean hovered) {
            this.hovered = hovered;
        }

        // Helper: metni satırlara böl
        private static List<String> wrapText(String text, FontMetrics fm, int maxWidth) {
            List<String> lines = new ArrayList<>();
            String[] words = text.split(" ");
            StringBuilder line = new StringBuilder();
            for (String word : words) {
                String testLine = line.length() == 0 ? word : line + " " + word;
                if (fm.stringWidth(testLine) > maxWidth) {
                    lines.add(line.toString());
                    line = new StringBuilder(word);
                } else {
                    line = new StringBuilder(testLine);
                }
            }
            if (!line.toString().isEmpty()) {
                lines.add(line.toString());
            }
            return lines;
        }

        // English skill names
        private static String getEnglishSkillName(SkillType type) {
            switch (type) {
                case BOUNTIFUL_START: return "Bountiful Start";
                case PLUNDERER_BONUS: return "Plunderer Bonus";
                case INTEREST_SYSTEM: return "Interest System";
                case SHARP_ARROW_TIPS: return "Sharp Arrow Tips";
                case EAGLE_EYE: return "Eagle Eye";
                case MAGIC_PIERCING: return "Magic Piercing";
                case SHATTERING_FORCE: return "Shattering Force";
                case DIVINE_WRATH: return "Divine Wrath";
                case BATTLE_READINESS: return "Battle Readiness";
                default: return type.name();
            }
        }
        // English skill descriptions
        private static String getEnglishSkillDescription(SkillType type) {
            switch (type) {
                case BOUNTIFUL_START: return "+150 gold at the start of the game.";
                case PLUNDERER_BONUS: return "+1 extra gold per enemy kill.";
                case INTEREST_SYSTEM: return "Earn 5% interest on your gold after each wave.";
                case SHARP_ARROW_TIPS: return "Archer towers deal +10% more damage.";
                case EAGLE_EYE: return "+1 range for all towers.";
                case MAGIC_PIERCING: return "Mage towers deal extra damage to armored enemies.";
                case SHATTERING_FORCE: return "Earthquake deals 25% more damage.";
                case DIVINE_WRATH: return "Lightning cooldown is 20% shorter.";
                case BATTLE_READINESS: return "Ultimate cooldowns are 10% shorter.";
                default: return type.getDescription();
            }
        }
    }
} 