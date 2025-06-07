package objects;

import enemies.Enemy;
import helpMethods.LoadSave;
import constants.Constants;
import scenes.Playing;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

public class TNTWarrior {
    private float x, y;
    private float targetX, targetY;
    private Enemy targetEnemy;
    private boolean active = true;
    private boolean exploding = false;
    private int explosionFrame = 0;
    private long explosionStartTime = 0;
    private Playing playingScene; // Reference to trigger screen shake
    
    // Movement properties
    private final float moveSpeed = 1.2f; // Slower, more tactical movement speed
    private final float explosionRange = 80f; // Explosion damage radius
    private final int explosionDamage = 50; // Damage dealt to enemies in range
    private final float triggerDistance = 20f; // Distance to enemy to trigger explosion
    
    // Animation properties
    private BufferedImage[] runFrames;
    private int animationIndex = 0;
    private int animationTick = 0;
    private final int animationSpeed = 12; // Animation speed
    
    // Visual properties
    private boolean facingLeft = false;
    
    // Explosion effect
    private BufferedImage[] explosionImages;
    private final long explosionDuration = 500_000_000L; // 500ms in nanoseconds
    
    // Particle system for explosion debris
    private java.util.List<ExplosionParticle> particles;
    private java.util.List<FlameParticle> flames;
    
    public TNTWarrior(float startX, float startY) {
        this.x = startX;
        this.y = startY;
        this.playingScene = null; // Will be set by TowerManager
        this.particles = new java.util.ArrayList<>();
        this.flames = new java.util.ArrayList<>();
        loadAnimationFrames();
        loadExplosionImages();
    }
    
    public void setPlayingScene(Playing playing) {
        this.playingScene = playing;
    }
    
    private void loadAnimationFrames() {
        // Use the same TNT sprite extraction logic as enemies
        BufferedImage tntSheet = LoadSave.getEnemyAtlas("tnt");
        
        if (tntSheet != null) {
            // Extract TNT frames using the same logic as EnemyManager
            // TNT: 1344x192 sized walk animation asset with 6 respective sprites
            final int frameCount = 6;
            runFrames = new BufferedImage[frameCount];
            
            for (int i = 0; i < frameCount; i++) {
                // Extract each 192x192 frame, then crop to the TNT area (60,60,100,72)
                BufferedImage tntFrame = tntSheet.getSubimage(i * 192, 0, 192, 192);
                runFrames[i] = tntFrame.getSubimage(60, 60, 100, 72);
            }
            
            System.out.println("Loaded " + frameCount + " TNT warrior animation frames successfully!");
        } else {
            // Fallback: create a simple colored rectangle if sprites aren't available
            System.out.println("Warning: Could not load TNT sprites, using fallback");
            BufferedImage fallback = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = fallback.createGraphics();
            g.setColor(new Color(139, 69, 19)); // Brown color for TNT
            g.fillRect(0, 0, 32, 32);
            g.setColor(Color.BLACK);
            g.drawRect(0, 0, 31, 31);
            g.setColor(Color.RED);
            g.drawString("TNT", 6, 20);
            g.dispose();
            runFrames = new BufferedImage[]{fallback};
        }
    }
    
    private void loadExplosionImages() {
        explosionImages = LoadSave.getExplosionAnimation();
        if (explosionImages == null || explosionImages.length == 0) {
            // Create enhanced explosion effect if images aren't available
            explosionImages = new BufferedImage[10]; // More frames for better effect
            for (int i = 0; i < 10; i++) {
                int baseSize = 60; // Larger base size
                int size = baseSize + i * 20; // Growing explosion
                BufferedImage explosion = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = explosion.createGraphics();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Multi-layered explosion effect
                // Outer ring - red/orange
                int alpha1 = Math.max(30, 220 - i * 22);
                g.setColor(new Color(255, 80, 0, alpha1));
                g.fillOval(0, 0, size, size);
                
                // Middle ring - orange/yellow
                int middleSize = (int)(size * 0.7f);
                int alpha2 = Math.max(40, 200 - i * 20);
                g.setColor(new Color(255, 150, 0, alpha2));
                g.fillOval(size/2 - middleSize/2, size/2 - middleSize/2, middleSize, middleSize);
                
                // Inner core - bright yellow/white
                int coreSize = (int)(size * 0.4f);
                int alpha3 = Math.max(50, 255 - i * 25);
                g.setColor(new Color(255, 255, 100, alpha3));
                g.fillOval(size/2 - coreSize/2, size/2 - coreSize/2, coreSize, coreSize);
                
                // Hot center - white
                if (i < 6) {
                    int hotSize = (int)(size * 0.2f);
                    int alpha4 = Math.max(0, 200 - i * 35);
                    g.setColor(new Color(255, 255, 255, alpha4));
                    g.fillOval(size/2 - hotSize/2, size/2 - hotSize/2, hotSize, hotSize);
                }
                
                g.dispose();
                explosionImages[i] = explosion;
            }
        }
    }
    
    public void setTarget(Enemy enemy) {
        this.targetEnemy = enemy;
        if (enemy != null) {
            this.targetX = enemy.getSpriteCenterX();
            this.targetY = enemy.getSpriteCenterY();
            
            // Set facing direction towards target
            this.facingLeft = (targetX < x);
        }
    }
    
    public void update(float gameSpeedMultiplier, List<Enemy> allEnemies) {
        if (!active) return;
        
        if (exploding) {
            updateExplosion();
            return;
        }
        
        // Check if target is still alive, if not find new closest enemy
        if (targetEnemy == null || !targetEnemy.isAlive()) {
            findClosestEnemy(allEnemies);
        }
        
        // Update target position if we have a valid target
        if (targetEnemy != null && targetEnemy.isAlive()) {
            targetX = targetEnemy.getSpriteCenterX();
            targetY = targetEnemy.getSpriteCenterY();
            facingLeft = (targetX < x);
        }
        
        // Move towards target
        if (targetEnemy != null) {
            moveTowardsTarget(gameSpeedMultiplier);
            
            // Check if close enough to explode
            float distance = getDistanceToTarget();
            if (distance <= triggerDistance) {
                triggerExplosion(allEnemies);
            }
        }
        
        // Update animation
        updateAnimation();
    }
    
    private void findClosestEnemy(List<Enemy> enemies) {
        targetEnemy = null;
        float closestDistance = Float.MAX_VALUE;
        
        for (Enemy enemy : enemies) {
            if (enemy.isAlive()) {
                float distance = getDistanceToEnemy(enemy);
                if (distance < closestDistance) {
                    closestDistance = distance;
                    targetEnemy = enemy;
                }
            }
        }
        
        if (targetEnemy != null) {
            targetX = targetEnemy.getSpriteCenterX();
            targetY = targetEnemy.getSpriteCenterY();
            facingLeft = (targetX < x);
        }
    }
    
    private void moveTowardsTarget(float gameSpeedMultiplier) {
        float dx = targetX - x;
        float dy = targetY - y;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        
        if (distance > 0) {
            float moveX = (dx / distance) * moveSpeed * gameSpeedMultiplier;
            float moveY = (dy / distance) * moveSpeed * gameSpeedMultiplier;
            
            x += moveX;
            y += moveY;
        }
    }
    
    private float getDistanceToTarget() {
        if (targetEnemy == null) return Float.MAX_VALUE;
        return getDistanceToEnemy(targetEnemy);
    }
    
    private float getDistanceToEnemy(Enemy enemy) {
        float dx = enemy.getSpriteCenterX() - x;
        float dy = enemy.getSpriteCenterY() - y;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }
    
    private void triggerExplosion(List<Enemy> allEnemies) {
        exploding = true;
        explosionStartTime = System.nanoTime();
        explosionFrame = 0;
        
        // Deal damage to all enemies within explosion range
        for (Enemy enemy : allEnemies) {
            if (enemy.isAlive()) {
                float distance = getDistanceToEnemy(enemy);
                if (distance <= explosionRange) {
                    enemy.hurt(explosionDamage);
                    System.out.println("TNT Warrior explosion hit enemy for " + explosionDamage + " damage!");
                }
            }
        }
        
        // Check for nearby towers (castles) within explosion range and destroy them with 50% chance
        // Using the same pattern as earthquake ultimate
        if (playingScene != null && playingScene.getTowerManager() != null) {
            for (objects.Tower tower : playingScene.getTowerManager().getTowers()) {
                if (tower.getLevel() == 1 && !tower.isDestroyed()) {
                    // Calculate distance from TNT explosion center to tower center
                    float towerCenterX = tower.getX() + tower.getWidth() / 2f;
                    float towerCenterY = tower.getY() + tower.getHeight() / 2f;
                    float dx = towerCenterX - x;
                    float dy = towerCenterY - y;
                    float distanceToTower = (float) Math.sqrt(dx * dx + dy * dy);
                    
                    // 3 tiles distance = 3 * 64 pixels = 192 pixels
                    float tileDestructionRange = 3 * 64f; // 3 tiles
                    if (distanceToTower <= tileDestructionRange) {
                        // 50% chance to destroy tower within 3 tiles
                        System.out.println("TNT explosion within 3 tiles of " + tower.getClass().getSimpleName() + " at distance " + distanceToTower + " pixels (" + (distanceToTower/64f) + " tiles)");
                        if (Math.random() < 0.75) {
                            System.out.println("  -> Destroying tower (75% chance succeeded)!");
                            tower.setDestroyed(true);
                            if (tower instanceof objects.MageTower) {
                                tower.setDestroyedSprite(helpMethods.LoadSave.getImageFromPath("/TowerAssets/Tower_spell_destroyed.png"));
                            } else if (tower instanceof objects.ArtilleryTower) {
                                tower.setDestroyedSprite(helpMethods.LoadSave.getImageFromPath("/TowerAssets/Tower_bomb_destroyed.png"));
                            } else if (tower instanceof objects.ArcherTower) {
                                tower.setDestroyedSprite(helpMethods.LoadSave.getImageFromPath("/TowerAssets/Tower_archer_destroyed.png"));
                            }
                            // Spawn debris effect - exact same as earthquake
                            java.util.List<objects.Tower.Debris> debris = new java.util.ArrayList<>();
                            int debrisCount = 12 + (int)(Math.random() * 6);
                            int cx = tower.getX() + 32, cy = tower.getY() + 32;
                            for (int d = 0; d < debrisCount; d++) {
                                double angle = Math.random() * 2 * Math.PI;
                                float speed = 2f + (float)Math.random() * 2f;
                                float vx = (float)Math.cos(angle) * speed;
                                float vy = (float)Math.sin(angle) * speed;
                                int color = 0xFF7C5C2E; // brown debris
                                int size = 3 + (int)(Math.random() * 4);
                                int lifetime = 20 + (int)(Math.random() * 10);
                                debris.add(new objects.Tower.Debris(cx, cy, vx, vy, color, size, lifetime));
                            }
                            tower.debrisList = debris;
                            tower.debrisStartTime = System.currentTimeMillis();
                        } else {
                            System.out.println("  -> Tower survived (50% chance failed)");
                        }
                    }
                }
            }
        }
        
        // Create explosion particles and flames
        createExplosionParticles();
        createFlameParticles();
        
        // Trigger screen shake effect using the game's UltiManager
        if (playingScene != null && playingScene.getUltiManager() != null) {
            // Create a small TNT-specific screen shake
            playingScene.getUltiManager().triggerTNTShake();
            System.out.println("TNT EXPLOSION! *SCREEN SHAKE TRIGGERED*");
        }
        
        // Play TNT explosion sound
        try {
            managers.AudioManager.getInstance().playTNTExplosionSound();
        } catch (Exception e) {
            // Ignore if audio not available
        }
    }
    
    private void updateExplosion() {
        long currentTime = System.nanoTime();
        long elapsedTime = currentTime - explosionStartTime;
        
        // Update explosion frame based on time
        explosionFrame = (int) ((elapsedTime / (explosionDuration / explosionImages.length)));
        
        // Update particles
        updateParticles();
        updateFlames();
        
        if (elapsedTime >= explosionDuration) {
            active = false; // Remove the TNT warrior after explosion
        }
    }
    
    private void updateAnimation() {
        animationTick++;
        if (animationTick >= animationSpeed) {
            animationTick = 0;
            animationIndex++;
            if (animationIndex >= runFrames.length) {
                animationIndex = 0;
            }
        }
    }
    
    public void draw(Graphics g) {
        if (!active) return;
        
        if (exploding) {
            drawExplosion(g);
        } else {
            drawTNTWarrior(g);
        }
    }
    
    private void drawTNTWarrior(Graphics g) {
        if (runFrames != null && runFrames.length > 0) {
            BufferedImage sprite = runFrames[animationIndex];
            if (sprite != null) {
                // Use smaller sizing for more tactical feel
                int drawWidth = 48;  // Smaller for better balance
                int drawHeight = 36; // Maintain aspect ratio (72/100 * 48 ≈ 35, rounded to 36)
                int drawX = (int) (x - drawWidth / 2);
                int drawY = (int) (y - drawHeight / 2);
                
                if (facingLeft) {
                    // Flip horizontally for left-facing
                    g.drawImage(sprite, drawX + drawWidth, drawY, -drawWidth, drawHeight, null);
                } else {
                    g.drawImage(sprite, drawX, drawY, drawWidth, drawHeight, null);
                }
                return; // Successfully drew sprite
            }
        }
        
        // Fallback: simple visual indicator if sprite loading failed
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(new Color(139, 69, 19)); // Brown color for TNT
        g2d.fillRect((int)(x - 16), (int)(y - 16), 32, 32);
        g2d.setColor(Color.BLACK);
        g2d.drawRect((int)(x - 16), (int)(y - 16), 32, 32);
        g2d.setColor(Color.WHITE);
        g2d.drawString("TNT", (int)(x - 12), (int)(y + 4));
    }
    
    private void drawExplosion(Graphics g) {
        // Draw particles first (behind explosion)
        drawParticles(g);
        
        // Draw main explosion
        if (explosionImages != null && explosionFrame < explosionImages.length) {
            BufferedImage explosion = explosionImages[explosionFrame];
            if (explosion != null) {
                int drawX = (int) (x - explosion.getWidth() / 2);
                int drawY = (int) (y - explosion.getHeight() / 2);
                g.drawImage(explosion, drawX, drawY, null);
            }
        }
        
        // Draw flames on top
        drawFlames(g);
    }
    
    // Getters
    public float getX() { return x; }
    public float getY() { return y; }
    public boolean isActive() { return active; }
    public boolean isExploding() { return exploding; }
    public float getExplosionRange() { return explosionRange; }
    public int getExplosionDamage() { return explosionDamage; }
    
    // For collision detection or other purposes
    public Rectangle getBounds() {
        return new Rectangle((int)(x - 24), (int)(y - 24), 48, 48);
    }
    
    // ========== PARTICLE SYSTEM ==========
    
    private static class ExplosionParticle {
        float x, y, vx, vy;
        int life, maxLife;
        Color color;
        int size;
        
        public ExplosionParticle(float x, float y, float vx, float vy, Color color, int size, int life) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.color = color;
            this.size = size;
            this.life = life;
            this.maxLife = life;
        }
        
        public void update() {
            x += vx;
            y += vy;
            vy += 0.1f; // Gravity
            vx *= 0.98f; // Air resistance
            vy *= 0.98f;
            life--;
        }
        
        public boolean isAlive() {
            return life > 0;
        }
        
        public void draw(Graphics g) {
            if (!isAlive()) return;
            
            Graphics2D g2d = (Graphics2D) g;
            float alpha = (float) life / maxLife;
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2d.setColor(color);
            g2d.fillRect((int)x, (int)y, size, size);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }
    }
    
    private static class FlameParticle {
        float x, y, vx, vy;
        int life, maxLife;
        float size;
        int frame;
        
        public FlameParticle(float x, float y, float vx, float vy, float size, int life) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.size = size;
            this.life = life;
            this.maxLife = life;
            this.frame = 0;
        }
        
        public void update() {
            x += vx;
            y += vy;
            vy -= 0.05f; // Float upward
            vx *= 0.95f; // Slow down horizontally
            size *= 0.98f; // Shrink over time
            life--;
            frame++;
        }
        
        public boolean isAlive() {
            return life > 0 && size > 1f;
        }
        
        public void draw(Graphics g) {
            if (!isAlive()) return;
            
            Graphics2D g2d = (Graphics2D) g;
            float alpha = (float) life / maxLife;
            
            // Draw flame-like effect with multiple colors
            int currentSize = (int) size;
            
            // Outer flame - red/orange
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha * 0.7f));
            g2d.setColor(new Color(255, 100, 0));
            g2d.fillOval((int)(x - currentSize), (int)(y - currentSize), currentSize * 2, currentSize * 2);
            
            // Inner flame - yellow/white
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha * 0.5f));
            g2d.setColor(new Color(255, 255, 100));
            int innerSize = currentSize / 2;
            g2d.fillOval((int)(x - innerSize), (int)(y - innerSize), innerSize * 2, innerSize * 2);
            
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }
    }
    
    private void createExplosionParticles() {
        particles.clear();
        
        // Create debris particles flying outward
        int particleCount = 15 + (int)(Math.random() * 10); // 15-25 particles
        for (int i = 0; i < particleCount; i++) {
            float angle = (float)(Math.random() * 2 * Math.PI);
            float speed = 2f + (float)(Math.random() * 4f); // 2-6 speed
            float vx = (float)Math.cos(angle) * speed;
            float vy = (float)Math.sin(angle) * speed;
            
            // Vary particle colors - browns, grays, oranges
            Color[] colors = {
                new Color(139, 69, 19),   // Brown
                new Color(101, 67, 33),   // Dark brown
                new Color(160, 82, 45),   // Saddle brown
                new Color(128, 128, 128), // Gray
                new Color(105, 105, 105), // Dim gray
                new Color(255, 140, 0),   // Dark orange
                new Color(205, 92, 92)    // Indian red
            };
            Color color = colors[(int)(Math.random() * colors.length)];
            
            int size = 2 + (int)(Math.random() * 4); // 2-6 pixel size
            int life = 30 + (int)(Math.random() * 30); // 30-60 frames
            
            particles.add(new ExplosionParticle(x, y, vx, vy, color, size, life));
        }
    }
    
    private void createFlameParticles() {
        flames.clear();
        
        // Create flame particles that rise upward
        int flameCount = 8 + (int)(Math.random() * 6); // 8-14 flames
        for (int i = 0; i < flameCount; i++) {
            float angle = (float)(Math.random() * Math.PI * 0.5 - Math.PI * 0.25); // Upward bias
            float speed = 1f + (float)(Math.random() * 2f);
            float vx = (float)Math.cos(angle) * speed;
            float vy = (float)Math.sin(angle) * speed - 1f; // Bias upward
            
            float size = 8f + (float)(Math.random() * 12f); // 8-20 size
            int life = 25 + (int)(Math.random() * 15); // 25-40 frames
            
            flames.add(new FlameParticle(x, y, vx, vy, size, life));
        }
    }
    
    private void updateParticles() {
        particles.removeIf(particle -> {
            particle.update();
            return !particle.isAlive();
        });
    }
    
    private void updateFlames() {
        flames.removeIf(flame -> {
            flame.update();
            return !flame.isAlive();
        });
    }
    
    private void drawParticles(Graphics g) {
        // Create defensive copy to avoid ConcurrentModificationException
        java.util.List<ExplosionParticle> particlesCopy = new java.util.ArrayList<>(particles);
        for (ExplosionParticle particle : particlesCopy) {
            particle.draw(g);
        }
    }
    
    private void drawFlames(Graphics g) {
        // Create defensive copy to avoid ConcurrentModificationException
        java.util.List<FlameParticle> flamesCopy = new java.util.ArrayList<>(flames);
        for (FlameParticle flame : flamesCopy) {
            flame.draw(g);
        }
    }
} 