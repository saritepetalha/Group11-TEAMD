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
    public BufferedImage regularMusicImg;
    public BufferedImage pressedMusicImg;
    public BufferedImage backgroundImg;
    public BufferedImage menuBackgroundImg;
    public BufferedImage teamLogoImg;
    public BufferedImage loadGameMenuBackgroundImg;
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

    // Save button image
    public BufferedImage saveButtonImg;

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
        loadEarthquakeButtonImages();
        loadLightningButtonImages();
        loadGoldFactoryButtonImages();
        loadLightningAssets();
        loadSaveButtonImage();
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
        saveButtonImg = loadImage("/UI/Save_Button_For_In_Game_Options.png");
    }
}