package helpMethods;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;

/**
 * Utility class for pixel-perfect sprite manipulation in tower defense games.
 *
 * Provides advanced image processing methods such as upscaling, rotation, masking, and
 * sprite sheet generation — all designed to maintain the sharpness and integrity of pixel art.
 *
 * This helper class will be used to have pixel perfect rotation especially for arrow asset
 */
public class RotSprite {
    public static BufferedImage scale2x(BufferedImage src) {
        int w = src.getWidth();
        int h = src.getHeight();
        // Use TYPE_INT_ARGB to support transparency
        BufferedImage dst = new BufferedImage(w * 2, h * 2, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int P = src.getRGB(x, y);
                int A = (y > 0) ? src.getRGB(x, y - 1) : P;
                int B = (x < w - 1) ? src.getRGB(x + 1, y) : P;
                int C = (x > 0) ? src.getRGB(x - 1, y) : P;
                int D = (y < h - 1) ? src.getRGB(x, y + 1) : P;

                int E0 = (C == A && C != D && A != B) ? A : P;
                int E1 = (A == B && A != C && B != D) ? B : P;
                int E2 = (D == C && D != B && C != A) ? C : P;
                int E3 = (B == D && B != A && D != C) ? D : P;

                dst.setRGB(x * 2, y * 2, E0);
                dst.setRGB(x * 2 + 1, y * 2, E1);
                dst.setRGB(x * 2, y * 2 + 1, E2);
                dst.setRGB(x * 2 + 1, y * 2 + 1, E3);
            }
        }

        return dst;
    }

    public static BufferedImage rotateParallelogram(BufferedImage src, double angleDegrees) {
        int w = src.getWidth();
        int h = src.getHeight();

        // Use TYPE_INT_ARGB to support transparency
        BufferedImage rotated = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = rotated.createGraphics();

        // Enable transparency support
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        double angle = Math.toRadians(angleDegrees);
        AffineTransform transform = AffineTransform.getRotateInstance(angle, w / 2.0, h / 2.0);
        g2d.drawImage(src, transform, null);

        g2d.dispose();
        return rotated;
    }

    public static BufferedImage applyMask(BufferedImage src, BufferedImage mask) {
        int w = Math.min(src.getWidth(), mask.getWidth());
        int h = Math.min(src.getHeight(), mask.getHeight());
        BufferedImage masked = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int pixel = src.getRGB(x, y);
                int maskPixel = mask.getRGB(x, y);
                int alpha = (maskPixel >> 24) & 0xFF;
                masked.setRGB(x, y, (alpha << 24) | (pixel & 0x00FFFFFF));
            }
        }

        return masked;
    }

    public static BufferedImage convertToIndexedColor(BufferedImage src, byte[] r, byte[] g, byte[] b) {
        int size = r.length;
        IndexColorModel colorModel = new IndexColorModel(8, size, r, g, b);
        BufferedImage indexed = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_BYTE_INDEXED, colorModel);
        Graphics2D g2d = indexed.createGraphics();
        g2d.drawImage(src, 0, 0, null);
        g2d.dispose();
        return indexed;
    }

    public static BufferedImage[] generateSpriteSheet(BufferedImage src, BufferedImage mask, int frames, double angleStep) {
        BufferedImage[] spriteSheet = new BufferedImage[frames];

        for (int i = 0; i < frames; i++) {
            double angle = angleStep * i;
            spriteSheet[i] = rotsprite(src, mask, angle);
        }

        return spriteSheet;
    }

    public static BufferedImage rotsprite(BufferedImage src, BufferedImage mask, double angleDegrees) {
        BufferedImage upscaled = scale2x(src);
        upscaled = scale2x(upscaled);
        upscaled = scale2x(upscaled);

        if (mask != null) {
            mask = scale2x(mask);
            mask = scale2x(mask);
            mask = scale2x(mask);
            upscaled = applyMask(upscaled, mask);
        }

        BufferedImage rotated = rotateParallelogram(upscaled, angleDegrees);

        BufferedImage result = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = result.createGraphics();

        // Set up proper transparency handling
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);

        g2d.drawImage(rotated, 0, 0, result.getWidth(), result.getHeight(), null);
        g2d.dispose();

        return result;
    }
}
