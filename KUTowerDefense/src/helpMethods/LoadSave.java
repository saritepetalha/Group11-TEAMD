package helpMethods;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;

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
        File levelsFolder = new File("resources/Levels");
        if (!levelsFolder.exists()) {
            levelsFolder.mkdirs();
        }

        File file = new File("KUTowerDefense/resources/Levels/" + fileName + ".json");
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        JsonObject root = new JsonObject();
        JsonElement tilesJson = gson.toJsonTree(tiles);
        root.add("tiles", tilesJson);

        // write it out
        try (Writer w = new FileWriter(file)) {
            System.out.println(gson.toJson(root));
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
        String levelsPath = "KUTowerDefense/resources/Levels";
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
        String resourcePath = "/Levels/" + levelName + ".json";
        InputStream is = LoadSave.class.getResourceAsStream(resourcePath);

        System.out.println("Attempting to load level from classpath: " + resourcePath);

        if (is == null) {
            System.out.println("Level resource not found in classpath: " + resourcePath);
            return null;
        }

        try (InputStreamReader reader = new InputStreamReader(is)) {
            JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
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
            System.out.println("Level successfully loaded from classpath: " + levelName);
            return level;
        } catch (Exception e) {
            System.out.println("Error loading level from classpath: " + resourcePath + " - " + e.getMessage());
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
        File levelsFolder = new File("KUTowerDefense/resources/LevelOverlays");
        if (!levelsFolder.exists()) {
            levelsFolder.mkdirs();
        }

        File file = new File("KUTowerDefense/resources/LevelOverlays/" + fileName + "_overlay.json");
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        JsonObject root = new JsonObject();
        JsonElement overlayJson = gson.toJsonTree(overlay);
        root.add("overlay", overlayJson);

        try (Writer writer = new FileWriter(file)) {
            System.out.println("Overlay saved to: " + file.getName());
            gson.toJson(root, writer);
        } catch (IOException e) {
            throw new RuntimeException("Error saving overlay: " + e.getMessage());
        }
    }

    public static int[][] loadOverlay(String fileName) {
        String resourcePath = "/LevelOverlays/" + fileName + "_overlay.json";
        InputStream is = LoadSave.class.getResourceAsStream(resourcePath);

        System.out.println("Attempting to load overlay from classpath: " + resourcePath);

        if (is == null) {
            System.out.println("Overlay resource not found in classpath: " + resourcePath);
            return null;
        }

        Gson gson = new Gson(); // Gson can still be used for parsing the stream
        try (InputStreamReader reader = new InputStreamReader(is)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            JsonElement overlayElement = root.get("overlay");
            if (overlayElement == null) {
                System.out.println("Overlay data not found in JSON for: " + fileName);
                return null;
            }
            System.out.println("Overlay successfully loaded from classpath: " + fileName);
            return gson.fromJson(overlayElement, int[][].class);
        } catch (IOException e) {
            System.err.println("Error loading overlay from classpath: " + resourcePath + " - " + e.getMessage());
            e.printStackTrace(); // It's good to print stack trace for IOExceptions during loading
            return null;
        } catch (JsonSyntaxException e) {
            System.err.println("Error parsing overlay JSON from classpath: " + resourcePath + " - " + e.getMessage());
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

    // Method to save BufferedImage to filesystem
    public static void saveImage(BufferedImage image, String path) {
        try {
            File outputFile = new File(path);
            outputFile.getParentFile().mkdirs(); // Create directories if they don't exist
            ImageIO.write(image, "png", outputFile);
        } catch (IOException e) {
            System.err.println("Error saving image to " + path + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Method to load arrow frames from files if they exist, otherwise load from resources
    public static BufferedImage[] loadArrowFrames(int frameCount) {
        BufferedImage[] frames = new BufferedImage[frameCount];

        // Try to load frames from files first
        for (int i = 0; i < frameCount; i++) {
            String framePath = "/TowerAssets/ArrowFrames/arrow_frame_" + i + ".png";
            frames[i] = getImageFromPath(framePath);
            if (frames[i] == null) {
                return null; // If any frame is missing, return null to trigger generation
            }
        }

        return frames;
    }

    // Method to load pre-generated fireball frames
    public static BufferedImage[][] loadFireballFrames() {
        final int animationFrames = 5;
        final int rotationFrames = 36;

        BufferedImage[][] frames = new BufferedImage[animationFrames][rotationFrames];

        // Try to load all frames
        for (int animFrame = 0; animFrame < animationFrames; animFrame++) {
            for (int rotFrame = 0; rotFrame < rotationFrames; rotFrame++) {
                String framePath = "/TowerAssets/FireballFrames/fireball_anim_" +
                        animFrame + "_rot_" + rotFrame + ".png";
                frames[animFrame][rotFrame] = getImageFromPath(framePath);

                if (frames[animFrame][rotFrame] == null) {
                    return null; // If any frame is missing, return null to trigger generation
                }
            }
        }

        return frames;
    }

}
