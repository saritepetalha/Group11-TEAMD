package helpMethods;

import java.awt.image.BufferedImage;

/**
 * Utility class to generate and save rotated projectile frames (arrows and fireballs).
 * This should be run once to pre-generate frames and avoid runtime generation.
 * 
 * Usage: Run this class's main method to generate all frames before running the game.
 */
public class RotatedProjectileFrameGenerator {
    
    public static void generateAndSaveArrowFrames() {
        System.out.println("Generating arrow frames...");
        
        // Load base arrow image
        BufferedImage baseArrow = LoadSave.getImageFromPath("/TowerAssets/arrow.png");
        if (baseArrow == null) {
            System.err.println("Failed to load base arrow image!");
            return;
        }
        
        // Generate 72 frames with 5.0 degree steps (same as original)
        int frameCount = 72;
        double angleStep = 5.0;
        BufferedImage[] originalFrames = RotSprite.generateSpriteSheet(baseArrow, null, frameCount, angleStep);
        
        // Save each frame after resizing to 24x24
        for (int i = 0; i < originalFrames.length; i++) {
            BufferedImage resizedFrame = LoadSave.resizeImage(originalFrames[i], 24, 24);
            String outputPath = "KUTowerDefense/resources/TowerAssets/ArrowFrames/arrow_frame_" + i + ".png";
            LoadSave.saveImage(resizedFrame, outputPath);
            
            if (i % 10 == 0 || i == originalFrames.length - 1) {
                System.out.println("Generated arrow frame " + (i + 1) + "/" + originalFrames.length);
            }
        }
        
        System.out.println("Arrow frames generation complete!");
    }
    
    public static void generateAndSaveFireballFrames() {
        System.out.println("Generating fireball frames...");
        
        // Load original fireball animation frames directly
        BufferedImage spriteSheet = LoadSave.getImageFromPath("/TowerAssets/Firaball_Animated.png");
        if (spriteSheet == null) {
            System.err.println("Failed to load fireball sprite sheet!");
            return;
        }
        
        // Extract individual fireball frames (keep original 48x32 size)
        final int frameCount = 5;
        final int frameWidth = 48;
        final int frameHeight = 32;
        BufferedImage[] fireballFrames = new BufferedImage[frameCount];
        
        for (int i = 0; i < frameCount; i++) {
            fireballFrames[i] = spriteSheet.getSubimage(i * frameWidth, 0, frameWidth, frameHeight);
        }
        
        // Generate rotated versions for each animation frame
        final int rotationFrames = 36; // 360° / 10° = 36 rotation frames
        final double angleStep = 10.0; // 10-degree intervals
        
        for (int animFrame = 0; animFrame < frameCount; animFrame++) {
            BufferedImage baseFrame = fireballFrames[animFrame];
            
            for (int rotFrame = 0; rotFrame < rotationFrames; rotFrame++) {
                double angle = angleStep * rotFrame;
                // Apply RotSprite rotation WITHOUT resizing - keep original 48x32 dimensions
                BufferedImage rotatedFrame = RotSprite.rotsprite(baseFrame, null, angle);
                
                String outputPath = "KUTowerDefense/resources/TowerAssets/FireballFrames/fireball_anim_" +
                                  animFrame + "_rot_" + rotFrame + ".png";
                LoadSave.saveImage(rotatedFrame, outputPath);
            }
            
            System.out.println("Generated rotation frames for animation frame " + (animFrame + 1) + "/" + frameCount);
        }
        
        System.out.println("Fireball frame generation complete! Total frames: " + (frameCount * rotationFrames));
        System.out.println("Fireball frames kept at original size: 48x32 pixels with transparent background");
    }
    
    public static void generateAllProjectileFrames() {
        System.out.println("=== Generating All Rotated Projectile Frames ===");
        System.out.println("Note: This will create frames with transparent backgrounds");
        System.out.println();
        
        generateAndSaveArrowFrames();
        System.out.println();
        generateAndSaveFireballFrames();
        
        System.out.println();
        System.out.println("=== All projectile frames generated! ===");
        System.out.println("Arrow frames: 72 frames (5° intervals, resized to 24x24)");
        System.out.println("Fireball frames: 180 frames (5 animation × 36 rotation, original 48x32 size)");
        System.out.println("All frames saved with transparent backgrounds for proper game rendering.");
    }
    
    public static void main(String[] args) {
        generateAllProjectileFrames();
    }
} 