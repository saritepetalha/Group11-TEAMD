package ui_p;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import helpMethods.LoadSave;

public class AssetsLoader {
    // Singleton instance
    private static AssetsLoader instance;

    public BufferedImage modeLabelImg;
    public BufferedImage modeImage;
    public BufferedImage buttonSheetImg;
    public ArrayList<BufferedImage> buttonImages = new ArrayList<>();
    public BufferedImage buttonHoverEffectImg;
    public ArrayList<BufferedImage> buttonHoverEffectImages = new ArrayList<>();
    public BufferedImage buttonPressedEffectImg;
    public ArrayList<BufferedImage> buttonPressedEffectImages = new ArrayList<>();
    public BufferedImage startPointImg;
    public BufferedImage endPointImg;
    public BufferedImage fourWayRoadImg;
    public BufferedImage statusBarImg;
    public BufferedImage waveImg;
    public BufferedImage optionsMenuImg;
    public BufferedImage backOptionsImg;
    public BufferedImage difficultyNormalImg;
    public BufferedImage difficultyEasyImg;
    public BufferedImage difficultyHardImg;
    public BufferedImage difficultyCustomImg;
    public BufferedImage regularMusicImg;
    public BufferedImage pressedMusicImg;
    public BufferedImage backgroundImg;
    public BufferedImage menuBackgroundImg;
    public BufferedImage teamLogoImg;
    public BufferedImage loadGameMenuBackgroundImg;
    public BufferedImage selectMapBackgroundImg;
    public BufferedImage earthquakeButtonImg;
    public BufferedImage earthquakeButtonHoverImg;
    public BufferedImage earthquakeButtonPressedImg;
    public BufferedImage lightningButtonNormal;
    public BufferedImage lightningButtonHover;
    public BufferedImage lightningButtonPressed;
    public BufferedImage goldFactoryButtonNormal;
    public BufferedImage goldFactoryButtonHover;
    public BufferedImage goldFactoryButtonPressed;
    public BufferedImage goldFactorySprite;
    public BufferedImage[] lightningFrames;
    public BufferedImage saveButtonImg;
    public BufferedImage pickaxeButtonImg;
    public BufferedImage pickaxeButtonHover;
    public BufferedImage pickaxeButtonPressed;
    public BufferedImage[] pickaxeAnimationFrames;
    public BufferedImage fireButtonNormal;
    public BufferedImage fireButtonHover;
    public BufferedImage[] confettiAnimationFrames;
    public java.awt.Cursor customHandCursor;
    public java.awt.Cursor customNormalCursor;
    public BufferedImage freezeButtonNormal;
    public BufferedImage freezeButtonHover;
    public BufferedImage freezeButtonPressed;
    public BufferedImage poisonTowerImg;

    // Private constructor
    private AssetsLoader() {
        loadAll();
    }

    // Singleton getInstance method
    public static AssetsLoader getInstance() {
        if (instance == null) {
            instance = new AssetsLoader();
        }
        return instance;
    }

    private void loadAll() {
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
        loadOptionsMenuImg();
        loadBackOptionsImg();
        loadDifficultyImages();
        loadMusicButtonImages();
        loadBackgroundImages();
        loadMenuBackgroundImage();
        loadLogoImages();
        loadLoadGameMenuBackgroundImg();
        loadSelectMapBackgroundImg();
        loadEarthquakeButtonImages();
        loadLightningButtonImages();
        loadGoldFactoryButtonImages();
        loadFreezeButtonImages();
        loadLightningAssets();
        loadSaveButtonImage();
        loadPickaxeAssets();
        loadConfettiAssets();
        loadHandCursor();
        loadNormalCursor();
        loadFireButtonAssets();
        loadPoisonTowerAsset();
    }

    private void loadButtonImageFile() {
        try (InputStream is = LoadSave.class.getResourceAsStream("/UI/buttonNormalAssets.png")) {
            buttonSheetImg = ImageIO.read(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadButtonPressedEffectImageFile() {
        try (InputStream is = LoadSave.class.getResourceAsStream("/UI/buttonPressedAssets.png")) {
            buttonPressedEffectImg = ImageIO.read(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadButtonHoverEffectImageFile() {
        try (InputStream is = LoadSave.class.getResourceAsStream("/UI/buttonHoveredAssets.png")) {
            buttonHoverEffectImg = ImageIO.read(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadModeImageFile() {
        try (InputStream is = LoadSave.class.getResourceAsStream("/UI/Button_Blue_3Slides.png")) {
            modeLabelImg = ImageIO.read(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadFourWayRoadImage() {
        try (InputStream is = LoadSave.class.getResourceAsStream("/Tiles/RoadFourWay.png")) {
            fourWayRoadImg = ImageIO.read(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadModeImage() {
        modeImage = modeLabelImg.getSubimage(0, 0, 192, 64);
    }

    private void loadButtonImages() {
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

    private void loadButtonHoverEffectImages() {
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

    private void loadButtonPressedEffectImages() {
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

    private BufferedImage loadImage(String path) {
        BufferedImage img = null;
        System.out.println("🔍 Attempting to load image: " + path);
        try (InputStream is = LoadSave.class.getResourceAsStream(path)) {
            if (is != null) {
                img = ImageIO.read(is);
                System.out.println("✅ Successfully loaded image: " + path);
            } else {
                System.err.println("❌ Image not found (InputStream is null): " + path);
                System.err.println("   Check if the file exists in src/main/resources" + path);
            }
        } catch (IOException e) {
            System.err.println("❌ IOException loading image: " + path);
            e.printStackTrace();
        }
        return img;
    }

    public void loadStartPointImg() {
        startPointImg = loadImage("/UI/startPoint192x192.png");
    }

    public void loadEndPointImg() {
        endPointImg = loadImage("/UI/endPoint192x192.png");
    }

    private void loadStatusBarImages() {
        try (InputStream is = LoadSave.class.getResourceAsStream("/UI/Coin_Health_Shield.png")) {
            statusBarImg = ImageIO.read(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadWaveImage() {
        try (InputStream is = LoadSave.class.getResourceAsStream("/UI/Wave.png")) {
            waveImg = ImageIO.read(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadOptionsMenuImg() {
        try (InputStream is = LoadSave.class.getResourceAsStream("/UI/Options_UI.png")) {
            optionsMenuImg = ImageIO.read(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadBackOptionsImg() {
        try (InputStream is = LoadSave.class.getResourceAsStream("/UI/BackOptions.png")) {
            backOptionsImg = ImageIO.read(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadDifficultyImages() {
        try (InputStream is = LoadSave.class.getResourceAsStream("/UI/Normal.png")) {
            difficultyNormalImg = ImageIO.read(is);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (InputStream is = LoadSave.class.getResourceAsStream("/UI/Easy.png")) {
            difficultyEasyImg = ImageIO.read(is);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (InputStream is = LoadSave.class.getResourceAsStream("/UI/Hard.png")) {
            difficultyHardImg = ImageIO.read(is);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (InputStream is = LoadSave.class.getResourceAsStream("/UI/Custom.png")) {
            difficultyCustomImg = ImageIO.read(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadBackgroundImages() {
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

    private void loadMenuBackgroundImage() {
        try (InputStream is = LoadSave.class.getResourceAsStream("/KuTowerDefence2.jpg")) {
            menuBackgroundImg = ImageIO.read(is);
            if (menuBackgroundImg == null) {
                System.err.println("Could not load menu background image: KuTowerDefence2.jpg");
            }
        } catch (IOException e) {
            System.err.println("Error loading menu background image: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadLogoImages() {
        try (InputStream is = LoadSave.class.getResourceAsStream("/UI/teamD_logo_alt.png")) {
            teamLogoImg = ImageIO.read(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadLoadGameMenuBackgroundImg() {
        try (InputStream is = LoadSave.class.getResourceAsStream("/UI/LoadGameUI.png")) {
            loadGameMenuBackgroundImg = ImageIO.read(is);
        } catch (IOException e) {
            System.err.println("Error loading LoadGameUI.png: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadSelectMapBackgroundImg() {
        try (InputStream is = LoadSave.class.getResourceAsStream("/UI/SelectMapUI.png")) {
            selectMapBackgroundImg = ImageIO.read(is);
        } catch (IOException e) {
            System.err.println("Error loading SelectMapUI.png: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadMusicButtonImages() {
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

    private void loadEarthquakeButtonImages() {
        earthquakeButtonImg = loadImage("/UI/earthquakeButton.png");
        earthquakeButtonHoverImg = earthquakeButtonImg;
        earthquakeButtonPressedImg = loadImage("/UI/earthquakeButtonPressed.png");
    }

    private void loadLightningButtonImages() {
        lightningButtonNormal = loadImage("/UI/lightningButton.png");
        lightningButtonHover = lightningButtonNormal;
        lightningButtonPressed = loadImage("/UI/lightningButtonPressed.png");
    }

    private void loadGoldFactoryButtonImages() {
        goldFactoryButtonNormal = loadImage("/UI/goldBagFactoryButton.png");
        goldFactoryButtonHover = goldFactoryButtonNormal;
        goldFactoryButtonPressed = loadImage("/UI/goldBagFactoryButtonPressed.png");
        goldFactorySprite = loadImage("/TowerAssets/goldBagFactory.png");
    }

    private void loadFreezeButtonImages() {
        freezeButtonNormal = loadImage("/UI/freeze_button.png");
        freezeButtonHover = freezeButtonNormal;
        freezeButtonPressed = loadImage("/UI/freeze_button_pressed.png");
    }

    private void loadLightningAssets() {
        try {
            BufferedImage sheet = LoadSave.getImageFromPath("/UI/lightning.png");
            int frameWidth = sheet.getWidth() / 6;
            int frameHeight = sheet.getHeight();

            lightningFrames = new BufferedImage[6];
            for (int i = 0; i < 6; i++) {
                lightningFrames[i] = sheet.getSubimage(i * frameWidth, 0, frameWidth, frameHeight);
            }
        } catch (Exception e) {
            System.err.println("⚠️ Lightning frames could not be loaded: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadSaveButtonImage() {
        System.out.println("🔍 Loading save button image...");
        saveButtonImg = loadImage("/UI/Save_Button_For_In_Game_Options.png");
        if (saveButtonImg != null) {
            System.out.println("✅ Save button image loaded successfully: " + saveButtonImg.getWidth() + "x" + saveButtonImg.getHeight());
        } else {
            System.err.println("❌ Failed to load save button image!");
            // Try to list what's available in the UI directory
            try {
                System.out.println("🔍 Checking available UI resources...");
                java.net.URL resourceUrl = LoadSave.class.getResource("/UI/");
                if (resourceUrl != null) {
                    System.out.println("UI directory found at: " + resourceUrl);
                } else {
                    System.err.println("UI directory not found in resources!");
                }
            } catch (Exception e) {
                System.err.println("Error checking UI resources: " + e.getMessage());
            }
        }
    }

    private void loadPickaxeAssets() {
        // Load pickaxe button image
        pickaxeButtonImg = loadImage("/UI/pickaxeButton.png");
        // Load pickaxe hover image
        pickaxeButtonHover = loadImage("/UI/pickaxeButtonHover.png");
        // Load pickaxe pressed image
        pickaxeButtonPressed = loadImage("/UI/pickaxeButtonPressed.png");
        // Load pickaxe animation frames
        try (InputStream is = LoadSave.class.getResourceAsStream("/Effects/Crush_Side-Sheet.png")) {
            if (is != null) {
                BufferedImage spriteSheet = ImageIO.read(is);
                final int frameCount = 8;
                pickaxeAnimationFrames = new BufferedImage[frameCount];
                for (int i = 0; i < frameCount; i++) {
                    pickaxeAnimationFrames[i] = spriteSheet.getSubimage(i * 64, 0, 64, 64);
                }
                System.out.println("✅ Pickaxe animation loaded with " + frameCount + " frames.");
            } else {
                System.err.println("❌ Pickaxe animation not found: /Effects/Crush_Side-Sheet.png");
            }
        } catch (IOException e) {
            System.err.println("❌ Error loading pickaxe animation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadConfettiAssets() {
        try (InputStream is = LoadSave.class.getResourceAsStream("/UI/Confetti.png")) {
            if (is != null) {
                BufferedImage spriteSheet = ImageIO.read(is);

                final int cols = 8;
                final int rows = 8;
                final int frameCount = cols * rows;
                final int frameWidth = spriteSheet.getWidth() / cols;
                final int frameHeight = spriteSheet.getHeight() / rows;

                confettiAnimationFrames = new BufferedImage[frameCount];

                int frameIndex = 0;
                for (int row = 0; row < rows; row++) {
                    for (int col = 0; col < cols; col++) {
                        confettiAnimationFrames[frameIndex] = spriteSheet.getSubimage(
                                col * frameWidth, row * frameHeight, frameWidth, frameHeight);
                        frameIndex++;
                    }
                }
            } else {
                System.err.println("❌ Confetti animation not found: /UI/Confetti.png");
            }
        } catch (IOException e) {
            System.err.println("❌ Error loading confetti animation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadHandCursor() {
        try {
            BufferedImage handCursorImage = ImageIO.read(LoadSave.class.getResourceAsStream("/UI/handCursor.png"));

            int cursorSize = 32;
            java.awt.Image scaledImage = handCursorImage.getScaledInstance(cursorSize, cursorSize, java.awt.Image.SCALE_SMOOTH);

            customHandCursor = java.awt.Toolkit.getDefaultToolkit().createCustomCursor(
                    scaledImage, new java.awt.Point(cursorSize/2, cursorSize/2), "HandCursor");
        } catch (IOException e) {
            System.err.println("Error loading handCursor.png: " + e.getMessage());
            e.printStackTrace();
            customHandCursor = new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR);
        }
    }

    private void loadNormalCursor() {
        try {
            BufferedImage originalImg = ImageIO.read(LoadSave.class.getResourceAsStream("/UI/01.png"));

            int newWidth = originalImg.getWidth() / 2;
            int newHeight = originalImg.getHeight() / 2;

            BufferedImage resizedImg = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
            java.awt.Graphics2D g2d = resizedImg.createGraphics();
            g2d.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.drawImage(originalImg, 0, 0, newWidth, newHeight, null);
            g2d.dispose();

            customNormalCursor = java.awt.Toolkit.getDefaultToolkit().createCustomCursor(
                    resizedImg,
                    new java.awt.Point(newWidth/2, newHeight/2),
                    "CustomNormalCursor");
        } catch (IOException e) {
            System.err.println("Error loading normal cursor from /UI/01.png: " + e.getMessage());
            e.printStackTrace();
            customNormalCursor = java.awt.Cursor.getDefaultCursor();
        }
    }

    private void loadFireButtonAssets() {
        fireButtonNormal = loadImage("/UI/fireButtonNormal.png");
        fireButtonHover = loadImage("/UI/fireButtonHover.png");
        if (fireButtonNormal == null) {
            System.err.println("Warning: Failed to load fireButtonNormal.png");
        }
        if (fireButtonHover == null) {
            System.err.println("Warning: Failed to load fireButtonHover.png");
        }
    }

    private void loadPoisonTowerAsset() {
        poisonTowerImg = loadImage("/TowerAssets/PoisonTower.png");
        if (poisonTowerImg == null) {
            System.err.println("Warning: Failed to load PoisonTower.png");
        }
    }
}

