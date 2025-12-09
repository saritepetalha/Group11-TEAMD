package objects;

import managers.GoldBagManager;
import ui_p.AssetsLoader;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

public class GoldFactory {
    private static final int SPAWN_INTERVAL_MILLIS = 5000; // 5 seconds
    private static final int MIN_GOLD = 15;
    private static final int MAX_GOLD = 25;
    private static final int FACTORY_SIZE = 64;
    private static final long LIFETIME_MILLIS = 30000; // 30 seconds lifetime

    private int tileX, tileY; // Store as tile coordinates
    private long creationTime;
    private GoldBagManager goldBagManager;
    private boolean destroyed = false;
    private Random random = new Random();
    
    // Accumulated time tracking (similar to Warrior fix)
    private float accumulatedLifetime = 0f;
    private float accumulatedSpawnTime = 0f;
    private long lastUpdateTime = 0;

    // Adjacent tile offsets: up, down, left, right
    private static final int[][] ADJACENT_OFFSETS = {
        {0, -32}, // up
        {0, 32},  // down
        {-32, 0}, // left
        {32, 0}   // right
    };

    public GoldFactory(int tileX, int tileY, GoldBagManager goldBagManager) {
        this.tileX = tileX;
        this.tileY = tileY;
        this.goldBagManager = goldBagManager;
        this.creationTime = System.currentTimeMillis();
        this.lastUpdateTime = System.currentTimeMillis();
        this.accumulatedLifetime = 0f;
        this.accumulatedSpawnTime = 0f;
    }

    public void update(float gameSpeedMultiplier) {
        if (destroyed) return;

        long currentTime = System.currentTimeMillis();
        if (lastUpdateTime > 0) {
            float deltaTime = currentTime - lastUpdateTime;
            accumulatedLifetime += deltaTime * gameSpeedMultiplier;
            accumulatedSpawnTime += deltaTime * gameSpeedMultiplier;
        }
        lastUpdateTime = currentTime;

        // Check if lifetime has expired
        if (accumulatedLifetime >= LIFETIME_MILLIS) {
            destroyed = true;
            return;
        }

        // Check if it's time to spawn a gold bag
        if (accumulatedSpawnTime >= SPAWN_INTERVAL_MILLIS) {
            spawnRandomGoldBag();
            accumulatedSpawnTime = 0f; // Reset spawn timer
        }
    }

    private void spawnRandomGoldBag() {
        // Pick a random adjacent direction
        int randomDirection = random.nextInt(ADJACENT_OFFSETS.length);
        int[] offset = ADJACENT_OFFSETS[randomDirection];
        
        // Calculate spawn position (center of the adjacent tile)
        float spawnX = tileX + offset[0] + FACTORY_SIZE / 2f;
        float spawnY = tileY + offset[1] + FACTORY_SIZE / 2f;
        
        // Add some random variation within the tile (±10 pixels)
        spawnX += (random.nextFloat() - 0.5f) * 20;
        spawnY += (random.nextFloat() - 0.5f) * 20;
        
        goldBagManager.spawnGoldBag(spawnX, spawnY, MIN_GOLD, MAX_GOLD);
    }

    public void draw(Graphics g, float gameSpeedMultiplier) {
        if (destroyed) return;

        BufferedImage factorySprite = AssetsLoader.getInstance().goldFactorySprite;
        if (factorySprite != null) {
            g.drawImage(factorySprite, tileX, tileY, FACTORY_SIZE, FACTORY_SIZE, null);
        } else {
            // Fallback drawing if sprite not found
            Graphics2D g2d = (Graphics2D) g;
            g2d.setColor(new Color(255, 215, 0)); // Gold color
            g2d.fillRect(tileX, tileY, FACTORY_SIZE, FACTORY_SIZE);
            g2d.setColor(Color.BLACK);
            g2d.drawRect(tileX, tileY, FACTORY_SIZE, FACTORY_SIZE);
            
            // Draw a simple "G" for gold
            g2d.setFont(new Font("Arial", Font.BOLD, 24));
            FontMetrics fm = g2d.getFontMetrics();
            String text = "G";
            int textX = tileX + FACTORY_SIZE / 2 - fm.stringWidth(text) / 2;
            int textY = tileY + FACTORY_SIZE / 2 + fm.getAscent() / 2;
            g2d.drawString(text, textX, textY);
        }
        
        // Draw lifetime indicator
        drawLifetimeBar(g, gameSpeedMultiplier);
    }

    private void drawLifetimeBar(Graphics g, float gameSpeedMultiplier) {
        float timeRemaining = LIFETIME_MILLIS - accumulatedLifetime;
        float lifePercentage = Math.max(0, timeRemaining / LIFETIME_MILLIS);

        int barWidth = FACTORY_SIZE;
        int barHeight = 4;
        int barX = tileX;
        int barY = tileY - 8;

        // Background
        g.setColor(Color.DARK_GRAY);
        g.fillRect(barX, barY, barWidth, barHeight);

        // Life remaining
        g.setColor(lifePercentage > 0.3f ? Color.GREEN : Color.RED);
        g.fillRect(barX, barY, (int)(barWidth * lifePercentage), barHeight);

        // Border
        g.setColor(Color.BLACK);
        g.drawRect(barX, barY, barWidth, barHeight);
    }


    public int getTileX() {
        return tileX;
    }

    public int getTileY() {
        return tileY;
    }

    public boolean contains(int mouseX, int mouseY) {
        return mouseX >= tileX && mouseX <= tileX + FACTORY_SIZE && 
               mouseY >= tileY && mouseY <= tileY + FACTORY_SIZE;
    }
} 