package ui_p;

import helpMethods.LoadSave;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class ButtonAssets {
    // static to only load once per run!
    public static BufferedImage buttonSheetImg;
    public static BufferedImage yellowHoverImg;
    public static BufferedImage pressedImg;
    public static BufferedImage modeLabelImg;
    public static BufferedImage modeImage;
    public static ArrayList<BufferedImage> buttonImages = new ArrayList<>();

    static {
        loadAll();
    }

    private static void loadAll() {
        loadButtonImageFile();
        loadYellowBorderImage();
        loadPressedButtonImage();
        loadModeImageFile();
        loadModeImage();
        loadButtonImages();
    }

    private static void loadButtonImageFile() {
        try (InputStream is = LoadSave.class.getResourceAsStream("/UI/kutowerbuttons4.png")) {
            buttonSheetImg = ImageIO.read(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadYellowBorderImage() {
        try (InputStream is = LoadSave.class.getResourceAsStream("/UI/Button_Hover.png")) {
            yellowHoverImg = ImageIO.read(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadPressedButtonImage() {
        try (InputStream is = LoadSave.class.getResourceAsStream("/UI/Button_Blue_Pressed.png")) {
            pressedImg = ImageIO.read(is);
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
        int tileSize = 64;
        buttonImages.clear();
        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 4; x++) {
                int subX = x * tileSize;
                int subY = y * tileSize;
                buttonImages.add(buttonSheetImg.getSubimage(subX, subY, tileSize, tileSize));
            }
        }
    }
}

