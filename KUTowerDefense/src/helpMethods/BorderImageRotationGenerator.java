package helpMethods;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import helpMethods.LoadSave;

/**
 * Manages wall and gate border images with rotation and caching for optimal performance.
 * Handles loading, rotating, and caching of border images to avoid expensive runtime operations.
 */
public class BorderImageRotationGenerator {
    // Singleton instance
    private static BorderImageRotationGenerator instance;

    // Source border images
    private BufferedImage wallImage;
    private BufferedImage gateImage;
    
    // Optimized cache: only 4 rotated versions total (one per direction)
    // [0] = original (top), [1] = bottom, [2] = left, [3] = right
    private BufferedImage[] rotatedWallImages = new BufferedImage[4]; 
    private BufferedImage[] rotatedGateImages = new BufferedImage[4];
    private boolean borderImagesRotated = false;

    // Private constructor for singleton
    private BorderImageRotationGenerator() {
        loadBorderImages();
    }

    // Singleton getInstance method
    public static BorderImageRotationGenerator getInstance() {
        if (instance == null) {
            instance = new BorderImageRotationGenerator();
        }
        return instance;
    }

    /**
     * Loads the wall and gate images during initialization
     */
    private void loadBorderImages() {
        try {
            wallImage = LoadSave.getImageFromPath("/Borders/wall.png");
            gateImage = LoadSave.getImageFromPath("/Borders/gate.png");
            if (wallImage != null && gateImage != null) {
                System.out.println("Border images loaded successfully in BorderImageManager");
                // Pre-generate all rotated versions for performance
                generateRotatedBorderImages();
            } else {
                System.err.println("Error loading border images in BorderImageManager");
                if (wallImage == null) System.err.println("  - Wall image failed to load");
                if (gateImage == null) System.err.println("  - Gate image failed to load");
            }
        } catch (Exception e) {
            System.err.println("Exception loading border images: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Pre-generates all rotated versions of border images for performance
     * Optimized to generate only 4 total rotations (one per direction) instead of redundant copies
     */
    private void generateRotatedBorderImages() {
        if (wallImage == null || gateImage == null) {
            System.err.println("Cannot generate rotated border images: source images not loaded");
            return;
        }

        try {
            System.out.println("Pre-generating optimized border image rotations (4 total per image type)...");

            // Generate the 4 essential rotated wall images
            rotatedWallImages[0] = wallImage;                                    // Top (original)
            rotatedWallImages[1] = rotateBorderImage(wallImage, 1);             // Bottom (flip vertically)
            rotatedWallImages[2] = rotateBorderImage(wallImage, 2);             // Left (rotate -90°)
            rotatedWallImages[3] = rotateBorderImage(wallImage, 3);             // Right (rotate 90°)

            // Generate the 4 essential rotated gate images  
            rotatedGateImages[0] = gateImage;                                    // Top (original)
            rotatedGateImages[1] = rotateBorderImage(gateImage, 1);             // Bottom (flip vertically)
            rotatedGateImages[2] = rotateBorderImage(gateImage, 2);             // Left (rotate -90°)
            rotatedGateImages[3] = rotateBorderImage(gateImage, 3);             // Right (rotate 90°)

            borderImagesRotated = true;
            System.out.println("Successfully pre-generated optimized border images:");
            System.out.println("  - 4 wall rotations (top, bottom, left, right)");
            System.out.println("  - 4 gate rotations (top, bottom, left, right)");
            System.out.println("  - Total: 8 images vs previous redundant approach");

        } catch (Exception e) {
            System.err.println("Error generating rotated border images: " + e.getMessage());
            e.printStackTrace();
            borderImagesRotated = false;
        }
    }

    /**
     * Detects which edge contains the gate for proper border rendering
     * @param level The level data array
     * @return gate edge: 0=top, 1=bottom, 2=left, 3=right, -1=no gate found
     */
    public int detectGateEdge(int[][] level) {
        if (level == null || level.length == 0 || level[0].length == 0) {
            return -1;
        }
        
        int rowCount = level.length;
        int colCount = level[0].length;
        
        // Check edges for gate (-4)
        for (int i = 0; i < rowCount; i++) {
            if (level[i][0] == -4) return 2; // left
            if (level[i][colCount - 1] == -4) return 3; // right
        }
        for (int j = 0; j < colCount; j++) {
            if (level[0][j] == -4) return 0; // top
            if (level[rowCount - 1][j] == -4) return 1; // bottom
        }
        return -1; // no gate found
    }

    public BufferedImage getWallImage() {
        return wallImage;
    }

    public BufferedImage getGateImage() {
        return gateImage;
    }

    /**
     * Gets a properly rotated wall or gate image based on the gate edge
     * Optimized to use pre-generated rotations with direct lookup - no redundant operations
     * @param isWall true for wall (-3), false for gate (-4)
     * @param gateEdge the detected gate edge (0=top, 1=bottom, 2=left, 3=right)
     * @return rotated BufferedImage or null if image not available
     */
    public BufferedImage getRotatedBorderImage(boolean isWall, int gateEdge) {
        if (gateEdge < 0 || gateEdge > 3) {
            System.err.println("Invalid gate edge: " + gateEdge + ". Using 0 (top).");
            gateEdge = 0;
        }

        // Use optimized cached rotated images with direct lookup
        if (borderImagesRotated) {
            BufferedImage[] cache = isWall ? rotatedWallImages : rotatedGateImages;
            BufferedImage result = cache[gateEdge];
            if (result != null) {
                return result;
            }
            System.err.println("Cached rotated image is null for " + (isWall ? "wall" : "gate") + " gateEdge: " + gateEdge);
        }

        BufferedImage sourceImage = isWall ? wallImage : gateImage;
        if (sourceImage == null) {
            System.err.println("Border image not available: " + (isWall ? "wall" : "gate"));
            return null;
        }

        System.out.println("WARNING: Using fallback rotation for " + (isWall ? "wall" : "gate") + " - cache may have failed");

        if (gateEdge == 0) {
            return sourceImage;
        }

        try {
            return rotateBorderImage(sourceImage, gateEdge);
        } catch (Exception e) {
            System.err.println("Exception during fallback border image rotation: " + e.getMessage());
            e.printStackTrace();
            return sourceImage; // Final fallback to original
        }
    }

    /**
     * Creates a rotated version of a border image
     * @param original the original image to rotate
     * @param gateEdge the gate edge (1=bottom, 2=left, 3=right)
     * @return rotated BufferedImage
     */
    private BufferedImage rotateBorderImage(BufferedImage original, int gateEdge) {
        int width = original.getWidth();
        int height = original.getHeight();
        
        BufferedImage rotated;
        Graphics2D g2d;
        
        switch (gateEdge) {
            case 1: // bottom - flip vertically
                rotated = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                g2d = rotated.createGraphics();
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2d.drawImage(original, 0, height, width, -height, null);
                g2d.dispose();
                return rotated;
                
            case 2: // left - rotate -90 degrees
                rotated = new BufferedImage(height, width, BufferedImage.TYPE_INT_ARGB);
                g2d = rotated.createGraphics();
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2d.rotate(-Math.PI / 2, height / 2.0, width / 2.0);
                g2d.drawImage(original, (height - width) / 2, (width - height) / 2, null);
                g2d.dispose();
                return rotated;
                
            case 3: // right - rotate 90 degrees
                rotated = new BufferedImage(height, width, BufferedImage.TYPE_INT_ARGB);
                g2d = rotated.createGraphics();
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2d.rotate(Math.PI / 2, height / 2.0, width / 2.0);
                g2d.drawImage(original, (height - width) / 2, (width - height) / 2, null);
                g2d.dispose();
                return rotated;
                
            default:
                return original;
        }
    }
} 