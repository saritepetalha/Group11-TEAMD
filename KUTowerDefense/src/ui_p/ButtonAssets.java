package ui_p;

import helpMethods.LoadSave;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class ButtonAssets {
    // static to only load once per run!

    public static BufferedImage modeLabelImg;
    public static BufferedImage modeImage;

    public static BufferedImage buttonSheetImg;
    public static ArrayList<BufferedImage> buttonImages = new ArrayList<>();

    public static BufferedImage buttonHoverEffectImg;
    public static ArrayList<BufferedImage> buttonHoverEffectImages = new ArrayList<>();

    public static BufferedImage buttonPressedEffectImg;
    public static ArrayList<BufferedImage> buttonPressedEffectImages = new ArrayList<>();

    public static BufferedImage startPointImg;
    public static BufferedImage endPointImg;
    public static BufferedImage startPointHoverImg;
    public static BufferedImage endPointHoverImg;
    public static BufferedImage startPointPressedImg;
    public static BufferedImage endPointPressedImg;

    static {
        loadAll();
    }

    private static void loadAll() {
        loadModeImageFile();
        loadModeImage();
        loadStartPointImg();
        loadEndPointImg();

        loadButtonImageFile();
        loadButtonHoverEffectImageFile();
        loadButtonPressedEffectImageFile();

        loadButtonImages();
        loadButtonHoverEffectImages();
        loadButtonPressedEffectImages();
    }

    private static void loadButtonImageFile() {
        try (InputStream is = LoadSave.class.getResourceAsStream("/UI/buttonNormalAssets.png")) {
            buttonSheetImg = ImageIO.read(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadButtonPressedEffectImageFile() {
        try (InputStream is = LoadSave.class.getResourceAsStream("/UI/buttonPressedAssets.png")) {
            buttonPressedEffectImg = ImageIO.read(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadButtonHoverEffectImageFile() {
        try (InputStream is = LoadSave.class.getResourceAsStream("/UI/buttonHoveredAssets.png")) {
            buttonHoverEffectImg = ImageIO.read(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadModeImageFile() {
        try (InputStream is = LoadSave.class.getResourceAsStream("/UI/Button_Blue_3Slides.png")) {
            modeLabelImg = ImageIO.read(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadModeImage() {
        modeImage = modeLabelImg.getSubimage(0, 0, 192, 64);
    }

    private static void loadButtonImages() {
        int tileHeight = 681;
        int tileWidth = 605;
        buttonImages.clear();
        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 4; x++) {
                int subX = x * tileWidth;
                int subY = y * tileHeight;
                buttonImages.add(buttonSheetImg.getSubimage(subX, subY, tileWidth, tileHeight));
            }
        }
    }

    private static void loadButtonHoverEffectImages() {
        int tileHeight = 701;
        int tileWidth = 627;
        buttonHoverEffectImages.clear();
        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 4; x++) {
                int subX = x * tileWidth;
                int subY = y * tileHeight;
                buttonHoverEffectImages.add(buttonHoverEffectImg.getSubimage(subX, subY, tileWidth, tileHeight));
            }
        }
    }

    private static void loadButtonPressedEffectImages() {
        int tileHeight = 654;
        int tileWidth = 678;
        buttonPressedEffectImages.clear();
        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 4; x++) {
                int subX = x * tileWidth;
                int subY = y * tileHeight;
                buttonPressedEffectImages.add(buttonPressedEffectImg.getSubimage(subX, subY, tileWidth, tileHeight));
            }
        }
    }

    private static BufferedImage loadImage(String path) {
        BufferedImage img = null;
        try (InputStream is = LoadSave.class.getResourceAsStream(path)) {
            if (is != null)
                img = ImageIO.read(is);
            else
                System.err.println("Image not found: " + path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return img;
    }

    public static void loadStartPointImg() {
        startPointImg = loadImage("/UI/startPoint192x192.png");
        // create hover versions with yellow highlight overlay
        startPointHoverImg = createHoverEffect(startPointImg);
        // create pressed versions with slight offset effect
        startPointPressedImg = createPressedEffect(startPointImg);
    }

    public static void loadEndPointImg() {
        endPointImg = loadImage("/UI/endPoint192x192.png");
        // create hover versions with yellow highlight overlay
        endPointHoverImg = createHoverEffect(endPointImg);
        // create pressed versions with slight offset effect
        endPointPressedImg = createPressedEffect(endPointImg);
    }


    // helper method to create hover effect by overlaying yellow highlight
    private static BufferedImage createHoverEffect(BufferedImage original) {
        BufferedImage result = new BufferedImage(original.getWidth(), original.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = result.createGraphics();
        g2d.drawImage(original, 0, 0, null);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
        g2d.setColor(new Color(255, 255, 0, 100)); // Subtle yellow tint
        g2d.fillRect(0, 0, original.getWidth(), original.getHeight());
        g2d.dispose();
        return result;
    }

    // helper method to create pressed effect
    private static BufferedImage createPressedEffect(BufferedImage original) {
        BufferedImage result = new BufferedImage(original.getWidth(), original.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = result.createGraphics();
        g2d.drawImage(original, 2, 2, null); // Slight offset to create pressed effect
        g2d.dispose();
        return result;
    }

}

