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
import java.util.Map;

import javax.imageio.ImageIO;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import objects.Warrior;
import objects.WizardWarrior;

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



    /**
     * Checks if the current project is a Maven project
     */
    private static boolean isMavenProject() {
        // Check if we're in a Maven project by looking for pom.xml in the current directory
        String currentDir = System.getProperty("user.dir");
        System.out.println("🔍 Current working directory: " + currentDir);

        File pomFile = new File("pom.xml");
        System.out.println("🔍 Checking for pom.xml at: " + pomFile.getAbsolutePath() + " - exists: " + pomFile.exists());

        // Also check if we're in a subdirectory and pom.xml is one level up
        File parentPomFile = new File("../pom.xml");
        System.out.println("🔍 Checking for ../pom.xml at: " + parentPomFile.getAbsolutePath() + " - exists: " + parentPomFile.exists());

        // Check if we have src/main/resources structure (strong indicator of Maven)
        File srcMainResources = new File("src/main/resources");
        System.out.println("🔍 Checking for src/main/resources at: " + srcMainResources.getAbsolutePath() + " - exists: " + srcMainResources.exists());

        boolean isMaven = pomFile.exists() || srcMainResources.exists();
        System.out.println("🔍 Detected as Maven project: " + isMaven);

        return isMaven;
    }

    /**
     * Gets the appropriate levels directory path based on project structure
     */
    private static String getLevelsDirectoryPath() {
        // Try multiple possible paths in order of preference
        String[] possiblePaths = {
                "src/main/resources/Levels",           // Standard Maven structure from project root
                "demo/src/main/resources/Levels",     // If running from parent directory
                "main/resources/Levels",              // If running from src directory
                "resources/Levels",                   // If running from src/main directory
                "KUTowerDefense/resources/Levels"     // Legacy structure
        };

        System.out.println("🔍 Trying to find levels directory...");

        for (String path : possiblePaths) {
            File dir = new File(path);
            System.out.println("🔍 Checking path: " + dir.getAbsolutePath() + " - exists: " + dir.exists());
            if (dir.exists() && dir.isDirectory()) {
                System.out.println("✅ Found levels directory at: " + path);
                return path;
            }
        }

        // If none found, default to Maven structure
        System.out.println("⚠️ No existing levels directory found, defaulting to Maven structure");
        return "src/main/resources/Levels";
    }

    /**
     * Gets the appropriate level overlays directory path based on project structure
     */
    private static String getLevelOverlaysDirectoryPath() {
        // Try multiple possible paths in order of preference
        String[] possiblePaths = {
                "src/main/resources/LevelOverlays",           // Standard Maven structure from project root
                "demo/src/main/resources/LevelOverlays",     // If running from parent directory
                "main/resources/LevelOverlays",              // If running from src directory
                "resources/LevelOverlays",                   // If running from src/main directory
                "KUTowerDefense/resources/LevelOverlays"     // Legacy structure
        };

        for (String path : possiblePaths) {
            File dir = new File(path);
            if (dir.exists() && dir.isDirectory()) {
                return path;
            }
        }

        // If none found, default to Maven structure
        return "src/main/resources/LevelOverlays";
    }

    //This function saves the game to a json file as a 2d array with key: tiles
    //If it is wished the same json file could be used for saving the game state
    //As in the location of the enemy groups, location of towers etc.
    public static void createLevel(String fileName, int[][] tiles) {
        String levelsPath = getLevelsDirectoryPath();
        File levelsFolder = new File(levelsPath);
        if (!levelsFolder.exists()) {
            levelsFolder.mkdirs();
        }

        File file = new File(levelsPath + "/" + fileName + ".json");
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        JsonObject root = new JsonObject();
        JsonElement tilesJson = gson.toJsonTree(tiles);
        root.add("tiles", tilesJson);

        // write it out
        try (Writer w = new FileWriter(file)) {
            System.out.println("Saving level to: " + file.getAbsolutePath());
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
        // Invalidate thumbnail cache when level is saved
        ThumbnailCache.getInstance().invalidateLevel(fileName);
    }

    /**
     * Deletes a saved game file from the saves directory
     * @param levelName The name of the saved game to delete (without .json extension)
     * @return true if deletion was successful, false otherwise
     */
    public static boolean deleteSavedGame(String levelName) {
        try {
            managers.GameStateManager gameStateManager = new managers.GameStateManager();
            gameStateManager.deleteSaveFile(levelName);
            return true;
        } catch (Exception e) {
            System.err.println("Error deleting saved game " + levelName + ": " + e.getMessage());
            return false;
        }
    }

    public static ArrayList<String> getSavedLevels() {
        ArrayList<String> levelNames = new ArrayList<>();

        // Use the appropriate path based on project structure
        String levelsPath = getLevelsDirectoryPath();
        File levelsFolder = new File(levelsPath);

        System.out.println("🔍 === LEVEL LOADING DEBUG ===");
        System.out.println("🔍 Using levels path: " + levelsPath);
        System.out.println("🔍 Checking levels path: " + levelsFolder.getAbsolutePath() + " - exists: " + levelsFolder.exists());

        if (!levelsFolder.exists()) {
            // Also try the alternative structure in case we're running from a different directory
            String alternatePath = levelsFolder.exists() ? levelsPath : "KUTowerDefense/resources/Levels";
            File alternateFolder = new File(alternatePath);
            System.out.println("🔍 Primary folder not found, trying alternate: " + alternateFolder.getAbsolutePath() + " - exists: " + alternateFolder.exists());

            if (alternateFolder.exists()) {
                levelsFolder = alternateFolder;
                levelsPath = alternatePath;
                System.out.println("✅ Using alternate path: " + levelsPath);
            } else {
                // Create the directory if it doesn't exist
                System.out.println("⚠️ Levels folder not found, creating: " + levelsFolder.getAbsolutePath());
                levelsFolder.mkdirs();
                return levelNames;
            }
        }

        File[] files = levelsFolder.listFiles();
        if (files != null) {
            System.out.println("🔍 Number of files found: " + files.length);
            for (File file : files) {
                System.out.println("🔍 File: " + file.getName() + " (isFile: " + file.isFile() + ", isJson: " + file.getName().endsWith(".json") + ")");
                if (file.isFile() && file.getName().endsWith(".json")) {
                    String levelName = file.getName().replace(".json", "");
                    levelNames.add(levelName);
                    System.out.println("✅ Level added: " + levelName);
                }
            }
        } else {
            System.err.println("❌ Could not get file list!");
        }

        System.out.println("🔍 Total levels found: " + levelNames.size());
        System.out.println("🔍 === END LEVEL LOADING DEBUG ===");

        return levelNames;
    }

    public static int[][] loadLevel(String levelName) {
        // First try to load from the appropriate file system location
        String levelsPath = getLevelsDirectoryPath();
        String levelFilePath = levelsPath + "/" + levelName + ".json";
        File levelFile = new File(levelFilePath);

        System.out.println("Checking level file: " + levelFile.getAbsolutePath() + " - exists: " + levelFile.exists());

        if (levelFile.exists()) {
            try (FileReader reader = new FileReader(levelFile)) {
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
                System.out.println("Level successfully loaded from file system: " + levelName + " at " + levelFilePath);
                return level;
            } catch (Exception e) {
                System.out.println("Error loading level from file system: " + levelFilePath + " - " + e.getMessage());
                e.printStackTrace();
            }
        }

        // Fallback: try to load from classpath (for built-in levels)
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
        String overlaysPath = getLevelOverlaysDirectoryPath();
        File levelsFolder = new File(overlaysPath);
        if (!levelsFolder.exists()) {
            levelsFolder.mkdirs();
        }

        File file = new File(overlaysPath + "/" + fileName + "_overlay.json");
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
        // First try to load from the appropriate file system location
        String overlaysPath = getLevelOverlaysDirectoryPath();
        String overlayFilePath = overlaysPath + "/" + fileName + "_overlay.json";
        File overlayFile = new File(overlayFilePath);

        System.out.println("Checking overlay file: " + overlayFile.getAbsolutePath() + " - exists: " + overlayFile.exists());

        if (overlayFile.exists()) {
            try (FileReader reader = new FileReader(overlayFile)) {
                JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                JsonElement overlayElement = root.get("overlay");
                if (overlayElement == null) {
                    System.out.println("Overlay data not found in JSON for: " + fileName);
                    return null;
                }
                System.out.println("Overlay successfully loaded from file system: " + fileName + " at " + overlayFilePath);
                return GSON.fromJson(overlayElement, int[][].class);
            } catch (Exception e) {
                System.out.println("Error loading overlay from file system: " + overlayFilePath + " - " + e.getMessage());
                e.printStackTrace();
            }
        }

        // Fallback: try to load from classpath (for built-in overlays)
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

    /**
     * Get warrior run animation frames
     * Archer: 800x100 with 8 frames (100x100 each, horizontal)
     * Wizard: 1848x190 with 8 frames (231x190 each, horizontal)
     */
    public static BufferedImage[] getWarriorRunAnimation(Warrior warrior) {
        String path = "/Warriors/" + (warrior instanceof WizardWarrior ? "wizard_run.png" : "archer_run.png");
        BufferedImage spriteSheet = getImageFromPath(path);
        
        if (spriteSheet == null) {
            return null;
        }
        
        final int frameCount = 8; // All new run sprites have 8 frames
        BufferedImage[] frames = new BufferedImage[frameCount];
        
        if (warrior instanceof WizardWarrior) {
            // Wizard: 1848x190 total, 8 frames = 231x190 each (horizontal layout)
            int frameWidth = 231;
            int frameHeight = 190;
            for (int i = 0; i < frameCount; i++) {
                frames[i] = spriteSheet.getSubimage(i * frameWidth, 0, frameWidth, frameHeight);
            }
        } else {
            // Archer: 800x100 total, 8 frames = 100x100 each (horizontal layout)
            int frameWidth = 100;
            int frameHeight = 100;
            for (int i = 0; i < frameCount; i++) {
                frames[i] = spriteSheet.getSubimage(i * frameWidth, 0, frameWidth, frameHeight);
            }
        }
        
        return frames;
    }
    
    /**
     * Get warrior attack animation frames
     * Archer: 600x100 with 6 frames (100x100 each, horizontal)
     * Wizard: 1848x190 with 8 frames (231x190 each, horizontal)
     */
    public static BufferedImage[] getWarriorAttackAnimation(Warrior warrior) {
        String path = "/Warriors/" + (warrior instanceof WizardWarrior ? "wizard_attack.png" : "archer_attack.png");
        BufferedImage spriteSheet = getImageFromPath(path);
        
        if (spriteSheet == null) {
            return null;
        }
        
        BufferedImage[] frames;
        
        if (warrior instanceof WizardWarrior) {
            // Wizard: 1848x190 total, 8 frames = 231x190 each (horizontal layout)
            final int frameCount = 8;
            frames = new BufferedImage[frameCount];
            int frameWidth = 231;
            int frameHeight = 190;
            for (int i = 0; i < frameCount; i++) {
                frames[i] = spriteSheet.getSubimage(i * frameWidth, 0, frameWidth, frameHeight);
            }
        } else {
            // Archer: 600x100 total, 6 frames = 100x100 each (horizontal layout)
            final int frameCount = 6;
            frames = new BufferedImage[frameCount];
            int frameWidth = 100;
            int frameHeight = 100;
            for (int i = 0; i < frameCount; i++) {
                frames[i] = spriteSheet.getSubimage(i * frameWidth, 0, frameWidth, frameHeight);
            }
        }
        
        return frames;
    }

    // Keep old method for backward compatibility, but use attack animation
    public static BufferedImage getWarriorImage(Warrior warrior) {
        String path = "/Warriors/" + (warrior instanceof WizardWarrior ? "wizard_attack.png" : "archer_attack.png");
        return getImageFromPath(path);
    }

    // Keep old method for backward compatibility, but use attack animation
    public static BufferedImage[] getWarriorAnimation(Warrior warrior) {
        return getWarriorAttackAnimation(warrior);
    }

    /**
     * Loads snow tilesets and extracts individual tiles
     * @param mediumSnowTiles Map to store medium snow tiles
     * @param fullSnowTiles Map to store full snow tiles
     * @param spriteCache Map to cache sprites for quick access
     * @return true if loading was successful, false otherwise
     */
    public static boolean loadSnowTilesets(Map<Integer, BufferedImage> mediumSnowTiles,
                                           Map<Integer, BufferedImage> fullSnowTiles,
                                           Map<String, BufferedImage> spriteCache) {
        try {
            boolean success = true;

            BufferedImage mediumSnowAtlas = getImageFromPath("/Tiles/midSnow.png");
            if (mediumSnowAtlas != null) {
                extractSnowTilesFromAtlas(mediumSnowAtlas, mediumSnowTiles, spriteCache, "medium");
                System.out.println("Medium snow tileset loaded successfully");
            } else {
                System.err.println("Failed to load medium snow tileset (midSnow.png)");
                success = false;
            }

            BufferedImage fullSnowAtlas = getImageFromPath("/Tiles/fullSnow.png");
            if (fullSnowAtlas != null) {
                extractSnowTilesFromAtlas(fullSnowAtlas, fullSnowTiles, spriteCache, "full");
                System.out.println("Full snow tileset loaded successfully");
            } else {
                System.err.println("Failed to load full snow tileset (fullSnow.png)");
                success = false;
            }

            return success;

        } catch (Exception e) {
            System.err.println("Error loading snow tilesets: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Extracts individual tiles from a snow atlas and caches them
     * @param atlas The snow tileset atlas
     * @param tileMap Map to store extracted tiles
     * @param spriteCache Map to cache sprites for quick access
     * @param type Type identifier ("medium" or "full")
     */
    public static void extractSnowTilesFromAtlas(BufferedImage atlas,
                                                 Map<Integer, BufferedImage> tileMap,
                                                 Map<String, BufferedImage> spriteCache,
                                                 String type) {
        if (atlas == null) return;

        final int TILESET_COLUMNS = 4;
        final int TILESET_ROWS = 5;
        final int TILE_SIZE = 128; // Source tileset tile size

        int tileId = 0;
        for (int row = 0; row < TILESET_ROWS; row++) {
            for (int col = 0; col < TILESET_COLUMNS; col++) {
                try {
                    BufferedImage tile = atlas.getSubimage(
                            col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE
                    );

                    // Resize to game tile size and cache
                    BufferedImage resizedTile = resizeImage(tile,
                            constants.GameDimensions.TILE_DISPLAY_SIZE,
                            constants.GameDimensions.TILE_DISPLAY_SIZE);

                    tileMap.put(tileId, resizedTile);

                    // Cache the sprite for quick access
                    String cacheKey = type + "_" + tileId;
                    spriteCache.put(cacheKey, resizedTile);

                    tileId++;
                } catch (Exception e) {
                    System.err.println("Error extracting snow tile at row " + row + ", col " + col + ": " + e.getMessage());
                }
            }
        }
        System.out.println("Extracted " + tileId + " tiles from " + type + " snow atlas");
    }

}
