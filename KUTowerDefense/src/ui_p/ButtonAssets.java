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
    public static BufferedImage fourWayRoadImg;

    public static BufferedImage statusBarImg;
    public static BufferedImage waveImg;

    // New UI assets for options menu
    public static BufferedImage optionsMenuImg;
    public static BufferedImage backOptionsImg;

    // Difficulty images
    public static BufferedImage difficultyNormalImg;
    public static BufferedImage difficultyEasyImg;
    public static BufferedImage difficultyHardImg;

    // Music button images
    public static BufferedImage regularMusicImg;
    public static BufferedImage pressedMusicImg;

    // Background images
    public static BufferedImage backgroundImg;

    // Logo images
    public static BufferedImage teamLogoImg;


    static {
        loadAll();
    }

    private static void loadAll() {
        loadModeImageFile();
        loadModeImage();
        loadStartPointImg();
        loadEndPointImg();
        loadFourWayRoadImage();

        loadButtonImageFile();
        loadButtonHoverEffectImageFile();
        loadButtonPressedEffectImageFile();

        loadButtonImages();
        loadButtonHoverEffectImages();
        loadButtonPressedEffectImages();

        loadStatusBarImages();
        loadWaveImage();

        // Load new UI assets
        loadOptionsMenuImg();
        loadBackOptionsImg();
        loadDifficultyImages();

        loadMusicButtonImages();

        // Load background images
        loadBackgroundImages();

        // Load logo images
        loadLogoImages();
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

    public static void loadFourWayRoadImage() {
        try (InputStream is = LoadSave.class.getResourceAsStream("/Tiles/RoadFourWay.png")) {
            fourWayRoadImg = ImageIO.read(is);
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

    private static void loadStatusBarImages() {
        try (InputStream is = LoadSave.class.getResourceAsStream("/UI/Coin_Health_Shield.png")) {
            statusBarImg = ImageIO.read(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadWaveImage() {
        try (InputStream is = LoadSave.class.getResourceAsStream("/UI/Wave.png")) {
            waveImg = ImageIO.read(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Load options menu background image
    private static void loadOptionsMenuImg() {
        try (InputStream is = LoadSave.class.getResourceAsStream("/UI/Options_UI.png")) {
            optionsMenuImg = ImageIO.read(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Load back button for options menu
    private static void loadBackOptionsImg() {
        try (InputStream is = LoadSave.class.getResourceAsStream("/UI/BackOptions.png")) {
            backOptionsImg = ImageIO.read(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Load difficulty images
    private static void loadDifficultyImages() {
        // Load Normal difficulty
        try (InputStream is = LoadSave.class.getResourceAsStream("/UI/Normal.png")) {
            difficultyNormalImg = ImageIO.read(is);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Load Easy difficulty
        try (InputStream is = LoadSave.class.getResourceAsStream("/UI/Easy.png")) {
            difficultyEasyImg = ImageIO.read(is);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Load Hard difficulty
        try (InputStream is = LoadSave.class.getResourceAsStream("/UI/Hard.png")) {
            difficultyHardImg = ImageIO.read(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Load background images
    private static void loadBackgroundImages() {
        try (InputStream is = LoadSave.class.getResourceAsStream("/KuTowerDefence1.jpg")) {
            backgroundImg = ImageIO.read(is);
            if (backgroundImg == null) {
                System.err.println("Could not load background image: KuTowerDefence1.jpg");
            }
        } catch (IOException e) {
            System.err.println("Error loading background image: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Load logo images
    private static void loadLogoImages() {
        try (InputStream is = LoadSave.class.getResourceAsStream("/teamD_logo_alt.png")) {
            teamLogoImg = ImageIO.read(is);
            if (teamLogoImg == null) {
                System.err.println("Could not load team logo image: teamD_logo_alt.png");
            }
        } catch (IOException e) {
            System.err.println("Error loading team logo image: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Load music button images
    private static void loadMusicButtonImages() {
        try (InputStream is = LoadSave.class.getResourceAsStream("/UI/RegularMusic.png")) {
            regularMusicImg = ImageIO.read(is);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (InputStream is = LoadSave.class.getResourceAsStream("/UI/PressedMusic.png")) {
            pressedMusicImg = ImageIO.read(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
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

