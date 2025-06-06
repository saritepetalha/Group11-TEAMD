package helpMethods;

import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Utility class to generate and save rotated projectile frames (arrows and fireballs).
 * This should be run once to pre-generate frames and avoid runtime generation.
 *
 * Usage: Run this class's main method to generate all frames before running the game.
 */
public class RotatedProjectileFrameGenerator {

    /**
     * Finds the project root directory by looking for key indicators
     */
    private static File findProjectRoot() {
        File currentDir = new File(System.getProperty("user.dir"));
        File checkDir = currentDir;

        // Look for project root indicators going up the directory tree
        for (int i = 0; i < 5; i++) { // Limit search to 5 levels up
            // Check for Maven project root indicators
            if (new File(checkDir, "pom.xml").exists() ||
                    new File(checkDir, "demo/pom.xml").exists() ||
                    (new File(checkDir, "src/main/resources").exists() && new File(checkDir, "pom.xml").exists())) {
                return checkDir;
            }

            // Check if we're inside a demo directory structure
            if (checkDir.getName().equals("demo") && new File(checkDir, "pom.xml").exists()) {
                return checkDir;
            }

            File parent = checkDir.getParentFile();
            if (parent == null) break;
            checkDir = parent;
        }

        // If no clear project root found, return current directory
        return currentDir;
    }

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

        // Use proper Maven structure for output paths
        File projectRoot = findProjectRoot();
        File demoDir = new File(projectRoot, "demo");
        File defaultPath;

        if (demoDir.exists() && new File(demoDir, "pom.xml").exists()) {
            defaultPath = new File(demoDir, "src/main/resources/TowerAssets/ArrowFrames");
        } else {
            defaultPath = new File(projectRoot, "src/main/resources/TowerAssets/ArrowFrames");
        }

        String basePath;
        try {
            basePath = defaultPath.getCanonicalPath() + "/";
        } catch (Exception e) {
            basePath = defaultPath.getAbsolutePath() + "/";
        }

        // Save each frame after resizing to 24x24
        for (int i = 0; i < originalFrames.length; i++) {
            BufferedImage resizedFrame = LoadSave.resizeImage(originalFrames[i], 24, 24);
            String outputPath = basePath + "arrow_frame_" + i + ".png";
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

        // Use proper Maven structure for output paths
        File projectRoot = findProjectRoot();
        File demoDir = new File(projectRoot, "demo");
        File defaultPath;

        if (demoDir.exists() && new File(demoDir, "pom.xml").exists()) {
            defaultPath = new File(demoDir, "src/main/resources/TowerAssets/FireballFrames");
        } else {
            defaultPath = new File(projectRoot, "src/main/resources/TowerAssets/FireballFrames");
        }

        String basePath;
        try {
            basePath = defaultPath.getCanonicalPath() + "/";
        } catch (Exception e) {
            basePath = defaultPath.getAbsolutePath() + "/";
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

                String outputPath = basePath + "fireball_anim_" +
                        animFrame + "_rot_" + rotFrame + ".png";
                LoadSave.saveImage(rotatedFrame, outputPath);
            }

            System.out.println("Generated rotation frames for animation frame " + (animFrame + 1) + "/" + frameCount);
        }

        System.out.println("Fireball frame generation complete! Total frames: " + (frameCount * rotationFrames));
        System.out.println("Fireball frames kept at original size: 48x32 pixels with transparent background");
    }

    public static void generateAndSaveWizardFrames() {
        System.out.println("Generating wizard projectile frames...");

        // Load base wizard projectile image
        BufferedImage baseWizardProjectile = LoadSave.getImageFromPath("/TowerAssets/WizardProjectile.png");
        if (baseWizardProjectile == null) {
            System.err.println("Failed to load base wizard projectile image!");
            return;
        }

        // Generate 72 frames with 5.0 degree steps (same as arrows)
        int frameCount = 72;
        double angleStep = 5.0;
        BufferedImage[] originalFrames = RotSprite.generateSpriteSheet(baseWizardProjectile, null, frameCount, angleStep);

        // Use proper Maven structure for output paths
        File projectRoot = findProjectRoot();
        File demoDir = new File(projectRoot, "demo");
        File defaultPath;

        if (demoDir.exists() && new File(demoDir, "pom.xml").exists()) {
            defaultPath = new File(demoDir, "src/main/resources/TowerAssets/WizardFrames");
        } else {
            defaultPath = new File(projectRoot, "src/main/resources/TowerAssets/WizardFrames");
        }

        String basePath;
        try {
            basePath = defaultPath.getCanonicalPath() + "/";
        } catch (Exception e) {
            basePath = defaultPath.getAbsolutePath() + "/";
        }

        // Save each frame after resizing to 24x24
        for (int i = 0; i < originalFrames.length; i++) {
            BufferedImage resizedFrame = LoadSave.resizeImage(originalFrames[i], 24, 24);
            String outputPath = basePath + "wizard_frame_" + i + ".png";
            LoadSave.saveImage(resizedFrame, outputPath);

            if (i % 10 == 0 || i == originalFrames.length - 1) {
                System.out.println("Generated wizard frame " + (i + 1) + "/" + originalFrames.length);
            }
        }

        System.out.println("Wizard projectile frames generation complete!");
    }

    public static void generateAllProjectileFrames() {
        System.out.println("=== Generating All Rotated Projectile Frames ===");
        System.out.println("Note: This will create frames with transparent backgrounds");
        System.out.println();

        generateAndSaveArrowFrames();
        System.out.println();
        generateAndSaveFireballFrames();
        System.out.println();
        generateAndSaveWizardFrames();

        System.out.println();
        System.out.println("=== All projectile frames generated! ===");
        System.out.println("Arrow frames: 72 frames (5° intervals, resized to 24x24)");
        System.out.println("Fireball frames: 180 frames (5 animation × 36 rotation, original 48x32 size)");
        System.out.println("Wizard frames: 72 frames (5° intervals, resized to 24x24)");
        System.out.println("All frames saved with transparent backgrounds for proper game rendering.");
    }

    public static void main(String[] args) {
        generateAllProjectileFrames();
    }
}