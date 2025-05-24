package helpMethods;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class LoadSave {

    private static final Gson GSON = new Gson();

    public static BufferedImage getSpriteAtlas() {

        BufferedImage img = null;
        InputStream is = LoadSave.class.getResourceAsStream("/Tiles/Tileset64.png");

        try {
            img = ImageIO.read(is);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return img;
    }

    public static BufferedImage getEnemyAtlas(String enemyType) {
        BufferedImage img = null;
        String path = switch (enemyType.toLowerCase()) {
            case "warrior" -> "/EnemyAssets/Warrior_Blue.png";
            case "goblin" -> "/EnemyAssets/Goblin_Red.png";
            case "barrel" -> "/EnemyAssets/Barrel_Purple.png";
            case "tnt" -> "/EnemyAssets/TNT_Red.png";
            case "troll" -> "/EnemyAssets/Troll_Green.png";
            default -> throw new IllegalArgumentException("Unknown enemy type: " + enemyType);
        };

        InputStream is = LoadSave.class.getResourceAsStream(path);

        try {
            img = ImageIO.read(is);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return img;
    }

    public static BufferedImage getTowerMaterial(int projectileType, int width, int height) {
        BufferedImage img = null;
        String path = switch (projectileType) {
            case 0 -> "/TowerAssets/arrow.png";
            case 1 -> "/TowerAssets/cannonball.png";
            case 2 -> "/TowerAssets/magicbolt.png";

            default -> throw new IllegalArgumentException("Unknown projectile type: " + projectileType);
        };

        InputStream is = LoadSave.class.getResourceAsStream(path);

        try {
            img = ImageIO.read(is);
            img = resizeImage(img, width, height);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return img;
    }

    public static BufferedImage resizeImage(BufferedImage originalImage, int width, int height) {
        BufferedImage resizedImage = new BufferedImage(width, height, originalImage.getType());
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(originalImage, 0, 0, width, height, null);
        g.dispose();
        return resizedImage;
    }



    //This function saves the game to a json file as a 2d array with key: tiles
    //If it is wished the same json file could be used for saving the game state
    //As in the location of the enemy groups, location of towers etc.
    public static void createLevel(String fileName, int[][] tiles) {
        String projectRoot = System.getProperty("user.dir");
        File levelsFolder = new File(projectRoot + "/src/main/resources/Levels");
        if (!levelsFolder.exists()) {
            levelsFolder.mkdirs();
        }

        File file = new File(projectRoot + "/src/main/resources/Levels/" + fileName + ".json");
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        JsonObject root = new JsonObject();
        JsonElement tilesJson = gson.toJsonTree(tiles);
        root.add("tiles", tilesJson);

        // write it out
        try (Writer w = new FileWriter(file)) {
            System.out.println("Saving level to: " + file.getAbsolutePath());
            gson.toJson(root, w);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //This funcion gets the created level's data from Levels folder under resources
    public static int[][] getLevelData(String fileName) {
        InputStream is = LoadSave.class.getResourceAsStream("/Levels/" + fileName + ".json");
        if (is == null) {
            System.err.println("Level file not found: /Levels/" + fileName + ".json");
            return null;
        }

        try (Reader reader = new InputStreamReader(is)) {
            JsonObject root = GSON.fromJson(reader, JsonObject.class);
            JsonArray tilesJson = root.getAsJsonArray("tiles");
            return GSON.fromJson(tilesJson, int[][].class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    public static void saveLevel(String fileName, int[][] tiles) {
        createLevel(fileName, tiles);
    }

    public static ArrayList<String> getSavedLevels() {
        ArrayList<String> levelNames = new ArrayList<>();
        String projectRoot = System.getProperty("user.dir");
        String levelsPath = projectRoot + "/src/main/resources/Levels";
        File levelsFolder = new File(levelsPath);

        System.out.println("Levels folder path: " + levelsFolder.getAbsolutePath());
        System.out.println("Levels folder exists: " + levelsFolder.exists());

        if (!levelsFolder.exists()) {
            System.out.println("Levels folder not found, creating...");
            levelsFolder.mkdirs();
            return levelNames;
        }

        File[] files = levelsFolder.listFiles();
        if (files != null) {
            System.out.println("Number of files found: " + files.length);
            for (File file : files) {
                System.out.println("File: " + file.getName());
                if (file.isFile() && file.getName().endsWith(".json")) {
                    String levelName = file.getName().replace(".json", "");
                    levelNames.add(levelName);
                    System.out.println("Level added: " + levelName);
                }
            }
        } else {
            System.out.println("Could not get file list!");
        }
        return levelNames;
    }

    public static int[][] loadLevel(String levelName) {
        String projectRoot = System.getProperty("user.dir");
        File levelFile = new File(projectRoot + "/src/main/resources/Levels/" + levelName + ".json");
        System.out.println("Attempting to load level from: " + levelFile.getAbsolutePath());

        if (!levelFile.exists()) {
            System.out.println("Level file not found: " + levelFile.getAbsolutePath());
            return null;
        }

        try (FileReader reader = new FileReader(levelFile)) {
            JsonObject jsonObject = GSON.fromJson(reader, JsonObject.class);
            JsonArray tilesArray = jsonObject.getAsJsonArray("tiles");

            int rows = tilesArray.size();
            int cols = tilesArray.get(0).getAsJsonArray().size();
            int[][] level = new int[rows][cols];

            for (int i = 0; i < rows; i++) {
                JsonArray row = tilesArray.get(i).getAsJsonArray();
                for (int j = 0; j < cols; j++) {
                    level[i][j] = row.get(j).getAsInt();
                }
            }
            System.out.println("Level successfully loaded: " + levelName);
            return level;
        } catch (Exception e) {
            System.out.println("Error loading level: " + levelFile.getAbsolutePath() + " - " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static BufferedImage getImageFromPath(String path) {
        BufferedImage img = null;
        InputStream is = LoadSave.class.getResourceAsStream(path);

        try {
            img = ImageIO.read(is);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return img;
    }

    public static void saveOverlay(String fileName, int[][] overlay) {
        String projectRoot = System.getProperty("user.dir");
        File levelsFolder = new File(projectRoot + "/src/main/resources/LevelOverlays");
        if (!levelsFolder.exists()) {
            levelsFolder.mkdirs();
        }

        File file = new File(projectRoot + "/src/main/resources/LevelOverlays/" + fileName + "_overlay.json");
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        JsonObject root = new JsonObject();
        JsonElement overlayJson = gson.toJsonTree(overlay);
        root.add("overlay", overlayJson);

        try (Writer writer = new FileWriter(file)) {
            System.out.println("Overlay saved to: " + file.getAbsolutePath());
            gson.toJson(root, writer);
        } catch (IOException e) {
            throw new RuntimeException("Error saving overlay: " + e.getMessage());
        }
    }

    public static int[][] loadOverlay(String fileName) {
        String projectRoot = System.getProperty("user.dir");
        File overlayFile = new File(projectRoot + "/src/main/resources/LevelOverlays/" + fileName + "_overlay.json");
        System.out.println("Attempting to load overlay from: " + overlayFile.getAbsolutePath());

        if (!overlayFile.exists()) {
            System.out.println("Overlay file not found: " + overlayFile.getAbsolutePath());
            return null;
        }

        try (FileReader reader = new FileReader(overlayFile)) {
            JsonObject root = GSON.fromJson(reader, JsonObject.class);
            JsonElement overlayElement = root.get("overlay");
            if (overlayElement == null) {
                System.out.println("Overlay data not found in JSON for: " + fileName);
                return null;
            }
            return GSON.fromJson(overlayElement, int[][].class);
        } catch (Exception e) {
            System.out.println("Error loading overlay: " + overlayFile.getAbsolutePath() + " - " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static BufferedImage[] getFireballAnimation() {
        final int frameCount = 5;
        final int frameWidth = 48;
        final int frameHeight = 32;

        BufferedImage[] frames = new BufferedImage[frameCount];
        BufferedImage spriteSheet = getImageFromPath("/TowerAssets/Firaball_Animated.png");

        if (spriteSheet != null) {
            for (int i = 0; i < frameCount; i++) {
                frames[i] = spriteSheet.getSubimage(i * frameWidth, 0, frameWidth, frameHeight);
            }
        }

        return frames;
    }

    public static BufferedImage[] getExplosionAnimation() {
        final int frameCount = 5;
        final int targetWidth = 32;
        final int targetHeight = 32;

        BufferedImage[] frames = new BufferedImage[frameCount];
        BufferedImage spriteSheet = getImageFromPath("/Effects/Explosions.png");

        if (spriteSheet != null) {
            int frameWidth = spriteSheet.getWidth() / frameCount;
            int frameHeight = spriteSheet.getHeight();

            for (int i = 0; i < frameCount; i++) {
                BufferedImage frame = spriteSheet.getSubimage(i * frameWidth, 0, frameWidth, frameHeight);
                frames[i] = resizeImage(frame, targetWidth, targetHeight);
            }
        }

        return frames;
    }

    public static BufferedImage[] getGoldBagAnimation() {
        final int frameCount = 7;
        final int frameWidth = 128;
        final int frameHeight = 128;
        BufferedImage[] frames = new BufferedImage[frameCount];
        BufferedImage spriteSheet = getImageFromPath("/EnemyAssets/G_Spawn.png");
        if (spriteSheet != null) {
            for (int i = 0; i < frameCount; i++) {
                frames[i] = spriteSheet.getSubimage(i * frameWidth, 0, frameWidth, frameHeight);
            }
        }
        return frames;
    }

}
