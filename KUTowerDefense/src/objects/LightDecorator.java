package objects;

import enemies.Enemy;


import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class LightDecorator extends TowerDecorator {
    private float lightRadius;
    private List<LightParticle> lightParticles;
    private long animationStartTime;
    private static final int LIGHT_PARTICLE_COUNT = 15;

    public LightDecorator(Tower decoratedTower) {
        super(decoratedTower);
        // Light radius is same as tower's range
        this.lightRadius = decoratedTower.getRange();
        initializeLightParticles();
        this.animationStartTime = System.nanoTime();
    }


    private void initializeLightParticles() {
        lightParticles = new ArrayList<>();
        for (int i = 0; i < LIGHT_PARTICLE_COUNT; i++) {
            lightParticles.add(new LightParticle(
                    getX() + 32, // Center of tower
                    getY() + 32,
                    lightRadius
            ));
        }
    }

    @Override
    public BufferedImage getSprite() {
        // Return null, let TowerManager handle sprite selection for night mode
        return null;
    }

    public float getLightRadius() {
        return lightRadius;
    }

    /**
     * Checks if a given position is within the light radius
     */
    public boolean isPositionLit(float x, float y) {
        float towerCenterX = getX() + 32; // Tower center
        float towerCenterY = getY() + 32;
        float distance = (float) Math.sqrt(
                Math.pow(x - towerCenterX, 2) + Math.pow(y - towerCenterY, 2)
        );
        return distance <= lightRadius;
    }

    /**
     * Draws the light effect (called by TowerManager during night)
     */
    public void drawLightEffect(Graphics2D g2d, boolean isNight) {
        if (!isNight) return;

        float towerCenterX = getX() + 32;
        float towerCenterY = getY() + 32;

        // Update light particles for subtle flickering
        updateLightParticles();

        // Draw subtle ambient light that doesn't overshadow the sprite's natural glow
        drawSubtleAmbientLight(g2d, towerCenterX, towerCenterY);

        // Draw lantern-aligned light effects based on tower type
        drawLanternAlignedEffects(g2d, towerCenterX, towerCenterY);

        // Draw very subtle light boundary for gameplay clarity
        drawSubtleLightBoundary(g2d, towerCenterX, towerCenterY);
    }

    private void drawSubtleAmbientLight(Graphics2D g2d, float centerX, float centerY) {
        // Very subtle ambient glow that enhances the existing sprite lighting
        long currentTime = System.nanoTime();
        float flickerPhase = (float)((currentTime - animationStartTime) / 1_000_000_000.0) * 3.0f;
        float flickerIntensity = (float)(Math.sin(flickerPhase) * 0.1 + 0.9); // Subtle flicker between 0.8 and 1.0

        // Create a very soft radial gradient for ambient light
        RadialGradientPaint ambientGradient = new RadialGradientPaint(
                centerX, centerY, lightRadius * 0.8f,
                new float[]{0.0f, 0.4f, 1.0f},
                new Color[]{
                        new Color(255, 220, 120, (int)(25 * flickerIntensity)), // Warm center
                        new Color(255, 200, 80, (int)(15 * flickerIntensity)),  // Medium warm
                        new Color(255, 180, 60, 0)                              // Transparent edge
                }
        );

        g2d.setPaint(ambientGradient);
        g2d.fillOval(
                (int)(centerX - lightRadius * 0.8f),
                (int)(centerY - lightRadius * 0.8f),
                (int)(lightRadius * 1.6f),
                (int)(lightRadius * 1.6f)
        );
    }

    private void drawLanternAlignedEffects(Graphics2D g2d, float centerX, float centerY) {
        // Get the base tower type to determine lantern positions
        Tower baseTower = decoratedTower;

        // Define lantern positions relative to tower center based on sprite analysis
        java.util.List<java.awt.geom.Point2D.Float> lanternPositions = getLanternPositions(baseTower, centerX, centerY);

        long currentTime = System.nanoTime();
        float timePhase = (float)((currentTime - animationStartTime) / 1_000_000_000.0);

        // Draw individual lantern glows
        for (int i = 0; i < lanternPositions.size(); i++) {
            java.awt.geom.Point2D.Float lanternPos = lanternPositions.get(i);

            // Each lantern flickers slightly out of phase
            float lanternPhase = timePhase * 4.0f + (i * (float)Math.PI / 3);
            float lanternIntensity = (float)(Math.sin(lanternPhase) * 0.15 + 0.85); // Gentle flicker

            drawLanternGlow(g2d, lanternPos.x, lanternPos.y, lanternIntensity);
        }
    }

    private java.util.List<java.awt.geom.Point2D.Float> getLanternPositions(Tower baseTower, float centerX, float centerY) {
        java.util.List<java.awt.geom.Point2D.Float> positions = new java.util.ArrayList<>();

        // Based on the sprite analysis, define lantern positions for each tower type
        if (baseTower instanceof objects.ArtilleryTower) {
            // Artillery tower - lanterns on sides
            positions.add(new java.awt.geom.Point2D.Float(centerX - 18, centerY - 12));
            positions.add(new java.awt.geom.Point2D.Float(centerX + 18, centerY - 12));
        } else if (baseTower instanceof objects.MageTower) {
            // Mage tower - central fire/lantern
            positions.add(new java.awt.geom.Point2D.Float(centerX, centerY - 8));
        } else if (baseTower instanceof objects.ArcherTower) {
            // Archer tower - side lanterns
            positions.add(new java.awt.geom.Point2D.Float(centerX - 16, centerY - 10));
            positions.add(new java.awt.geom.Point2D.Float(centerX + 16, centerY - 10));
        }

        return positions;
    }

    private void drawLanternGlow(Graphics2D g2d, float x, float y, float intensity) {
        // Very subtle glow around individual lanterns
        int glowSize = 12;

        RadialGradientPaint lanternGlow = new RadialGradientPaint(
                x, y, glowSize,
                new float[]{0.0f, 0.6f, 1.0f},
                new Color[]{
                        new Color(255, 200, 100, (int)(40 * intensity)), // Warm center
                        new Color(255, 180, 60, (int)(20 * intensity)),  // Medium
                        new Color(255, 160, 40, 0)                       // Transparent edge
                }
        );

        g2d.setPaint(lanternGlow);
        g2d.fillOval(
                (int)(x - glowSize),
                (int)(y - glowSize),
                glowSize * 2,
                glowSize * 2
        );
    }

    private void drawSubtleLightBoundary(Graphics2D g2d, float centerX, float centerY) {
        // Very subtle boundary circle for gameplay clarity
        long currentTime = System.nanoTime();
        float pulsePhase = (float)((currentTime - animationStartTime) / 1_000_000_000.0) * 1.5f;
        float pulseIntensity = (float)(Math.sin(pulsePhase) * 0.2 + 0.3); // Gentle pulse between 0.1 and 0.5

        g2d.setStroke(new BasicStroke(1.0f));
        g2d.setColor(new Color(255, 220, 120, (int)(pulseIntensity * 60))); // Very subtle
        g2d.drawOval(
                (int)(centerX - lightRadius),
                (int)(centerY - lightRadius),
                (int)(lightRadius * 2),
                (int)(lightRadius * 2)
        );
    }

    private void updateLightParticles() {
        // Reduce particle count and make them more subtle for the new sprites
        for (LightParticle particle : lightParticles) {
            particle.updateSubtle();
        }
    }

    @Override
    public Tower upgrade() {
        // If the decorated tower can be upgraded, upgrade it and wrap it in a new LightDecorator
        if (decoratedTower.isUpgradeable()) {
            Tower upgradedInnerTower = decoratedTower.upgrade();
            return new LightDecorator(upgradedInnerTower);
        }
        // If the decorated tower cannot be upgraded, return this LightDecorator unchanged
        return this;
    }

    // Inner class for more subtle light particles
    private static class LightParticle {
        private float x, y;
        private float angle;
        private float distance;
        private float speed;
        private float size;
        private float alpha;
        private float baseX, baseY;
        private float maxRadius;

        public LightParticle(float centerX, float centerY, float maxRadius) {
            this.baseX = centerX;
            this.baseY = centerY;
            this.maxRadius = maxRadius * 0.6f; // Keep particles closer to tower
            reset();
        }

        private void reset() {
            this.angle = (float)(Math.random() * Math.PI * 2);
            this.distance = (float)(Math.random() * maxRadius * 0.3f); // Start closer
            this.speed = 0.2f + (float)(Math.random() * 0.2f); // Slower movement
            this.size = 1.0f + (float)(Math.random() * 1.5f); // Smaller particles
            this.alpha = 0.1f + (float)(Math.random() * 0.2f); // More transparent
            updatePosition();
        }

        public void updateSubtle() {
            angle += speed * 0.015f; // Slower rotation
            distance += speed * 0.05f; // Slower outward movement

            if (distance > maxRadius) {
                reset();
            }

            updatePosition();
        }

        private void updatePosition() {
            x = baseX + (float)(Math.cos(angle) * distance);
            y = baseY + (float)(Math.sin(angle) * distance);
        }

    }
}