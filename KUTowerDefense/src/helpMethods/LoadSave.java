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
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

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
     * Detects if we're running in a Maven project structure
     */
    private static boolean isMavenProject() {
        // Check if we're in a Maven project by looking for pom.xml in the current directory
        File pomFile = new File("pom.xml");
        return pomFile.exists();
    }

    /**
     * Gets the appropriate levels directory path based on project structure
     */
    private static String getLevelsDirectoryPath() {
        if (isMavenProject()) {
            return "src/main/resources/Levels";
        } else {
            return "KUTowerDefense/resources/Levels";
        }
    }

    /**
     * Gets the appropriate level overlays directory path based on project structure
     */
    private static String getLevelOverlaysDirectoryPath() {
        if (isMavenProject()) {
            return "src/main/resources/LevelOverlays";
        } else {
            return "KUTowerDefense/resources/LevelOverlays";
        }
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
    // DEPRECATED: Use loadLevel() instead which tries filesystem first, then classpath
    // public static int[][] getLevelData(String fileName) {
    //     InputStream is = LoadSave.class.getResourceAsStream("/Levels/" + fileName + ".json");
    //     if (is == null) {
    //         System.err.println("Level file not found: /Levels/" + fileName + ".json");
    //         return null;
    //     }
    //
    //     try (Reader reader = new InputStreamReader(is)) {
    //         JsonObject root = GSON.fromJson(reader, JsonObject.class);
    //         JsonArray tilesJson = root.getAsJsonArray("tiles");
    //         return GSON.fromJson(tilesJson, int[][].class);
    //     } catch (IOException e) {
    //         e.printStackTrace();
    //         return null;
    //     }
    // }


    public static void saveLevel(String fileName, int[][] tiles) {
        createLevel(fileName, tiles);
        // Invalidate thumbnail cache when level is saved
        ThumbnailCache.getInstance().invalidateLevel(fileName);
    }

    public static ArrayList<String> getSavedLevels() {
        ArrayList<String> levelNames = new ArrayList<>();

        // Use the appropriate path based on project structure
        String levelsPath = getLevelsDirectoryPath();
        File levelsFolder = new File(levelsPath);

        System.out.println("Checking levels path: " + levelsFolder.getAbsolutePath() + " - exists: " + levelsFolder.exists());

        if (!levelsFolder.exists()) {
            // Create the directory if it doesn't exist
            System.out.println("Levels folder not found, creating: " + levelsFolder.getAbsolutePath());
            levelsFolder.mkdirs();
        }

        // First, try to load from filesystem
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

        // If no levels found in filesystem, try to find built-in levels from classpath
        if (levelNames.isEmpty()) {
            System.out.println("No levels found in filesystem, checking for built-in levels...");

            // Try to find some common default levels
            String[] defaultLevels = {"defaultlevel", "defaultleveltest1", "full_map", "documentMap"};

            for (String levelName : defaultLevels) {
                if (LoadSave.class.getResourceAsStream("/Levels/" + levelName + ".json") != null) {
                    levelNames.add(levelName);
                    System.out.println("Built-in level found: " + levelName);
                }
            }

            // If we found built-in levels, copy them to the filesystem for future use
            if (!levelNames.isEmpty()) {
                System.out.println("Copying built-in levels to filesystem...");
                for (String levelName : levelNames) {
                    copyBuiltInLevelToFilesystem(levelName);
                }
            }
        }

        return levelNames;
    }

    /**
     * Copies a built-in level from classpath to filesystem
     */
    private static void copyBuiltInLevelToFilesystem(String levelName) {
        try {
            int[][] levelData = getLevelDataFromClasspath(levelName);
            if (levelData != null) {
                createLevel(levelName, levelData);
                System.out.println("Copied built-in level to filesystem: " + levelName);
            }
        } catch (Exception e) {
            System.err.println("Failed to copy built-in level " + levelName + ": " + e.getMessage());
        }
    }

    /**
     * Helper method to load level data from classpath only
     */
    private static int[][] getLevelDataFromClasspath(String fileName) {
        InputStream is = LoadSave.class.getResourceAsStream("/Levels/" + fileName + ".json");
        if (is == null) {
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

    /**
     * Gets levels that have saved game states
     * @return ArrayList of level names that have corresponding save files
     */
    public static ArrayList<String> getLevelsWithSaveStates() {
        ArrayList<String> allLevels = getSavedLevels();
        ArrayList<String> levelsWithSaves = new ArrayList<>();

        String savesPath = getSavesDirectoryPath();
        File savesFolder = new File(savesPath);

        if (!savesFolder.exists()) {
            System.out.println("Saves folder not found: " + savesFolder.getAbsolutePath());
            return levelsWithSaves;
        }

        for (String levelName : allLevels) {
            File saveFile = new File(savesPath, levelName + ".json");
            if (saveFile.exists()) {
                levelsWithSaves.add(levelName);
                System.out.println("Level with save state found: " + levelName);
            }
        }

        return levelsWithSaves;
    }

    /**
     * Gets all available levels (for new game selection)
     * @return ArrayList of all level names
     */
    public static ArrayList<String> getAllAvailableLevels() {
        return getSavedLevels();
    }

    /**
     * Checks if a level has a saved game state
     * @param levelName The name of the level to check
     * @return true if a save file exists for this level
     */
    public static boolean hasGameSave(String levelName) {
        String savesPath = getSavesDirectoryPath();
        File saveFile = new File(savesPath, levelName + ".json");
        return saveFile.exists();
    }

    /**
     * Gets the appropriate saves directory path based on project structure
     */
    private static String getSavesDirectoryPath() {
        if (isMavenProject()) {
            return "src/main/resources/Saves";
        } else {
            return "resources/Saves";
        }
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
     * Gets warrior animation frames based on warrior type
     */
    public static BufferedImage[] getWarriorAnimation(objects.Warrior warrior) {
        if (warrior == null) return null;

        BufferedImage spriteSheet = null;
        int frameCount = 6; // Default frame count
        int frameWidth = 64;
        int frameHeight = 64;

        // Determine warrior type and load appropriate sprite sheet
        if (warrior instanceof objects.WizardWarrior) {
            spriteSheet = getImageFromPath("/WarriorAssets/WizardWarrior.png");
        } else if (warrior instanceof objects.ArcherWarrior) {
            spriteSheet = getImageFromPath("/WarriorAssets/ArcherWarrior.png");
        } else {
            // Default warrior sprite
            spriteSheet = getImageFromPath("/WarriorAssets/DefaultWarrior.png");
        }

        if (spriteSheet == null) {
            // Fallback: create simple colored rectangles
            BufferedImage[] fallbackFrames = new BufferedImage[frameCount];
            for (int i = 0; i < frameCount; i++) {
                fallbackFrames[i] = createFallbackWarriorSprite(warrior);
            }
            return fallbackFrames;
        }

        // Extract frames from sprite sheet
        BufferedImage[] frames = new BufferedImage[frameCount];
        for (int i = 0; i < frameCount; i++) {
            if (i * frameWidth < spriteSheet.getWidth()) {
                frames[i] = spriteSheet.getSubimage(i * frameWidth, 0, frameWidth, frameHeight);
            } else {
                // If not enough frames in sprite sheet, duplicate the last available frame
                frames[i] = frames[Math.max(0, i - 1)];
            }
        }

        return frames;
    }

    /**
     * Creates a fallback sprite for warriors when sprite sheets are not available
     */
    private static BufferedImage createFallbackWarriorSprite(objects.Warrior warrior) {
        BufferedImage sprite = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = sprite.createGraphics();

        // Different colors for different warrior types
        if (warrior instanceof objects.WizardWarrior) {
            g2d.setColor(new java.awt.Color(100, 50, 200)); // Purple for wizard
        } else if (warrior instanceof objects.ArcherWarrior) {
            g2d.setColor(new java.awt.Color(50, 150, 50)); // Green for archer
        } else {
            g2d.setColor(new java.awt.Color(150, 150, 150)); // Gray for default
        }

        g2d.fillOval(16, 16, 32, 32);
        g2d.dispose();

        return sprite;
    }

    /**
     * Loads snow tilesets for the SnowTransitionManager
     * @param mediumSnowTiles Map to store medium snow tile images
     * @param fullSnowTiles Map to store full snow tile images
     * @param spriteCache Map to store cached snow sprites
     * @return true if loading was successful, false otherwise
     */
    public static boolean loadSnowTilesets(java.util.Map<Integer, BufferedImage> mediumSnowTiles,
                                           java.util.Map<Integer, BufferedImage> fullSnowTiles,
                                           java.util.Map<String, BufferedImage> spriteCache) {
        boolean success = true;

        try {
            // Load medium snow tileset
            BufferedImage mediumSnowAtlas = getImageFromPath("/Weather/MediumSnowTileset.png");
            if (mediumSnowAtlas != null) {
                loadSnowTilesFromAtlas(mediumSnowAtlas, mediumSnowTiles, spriteCache, "medium");
            } else {
                System.out.println("Medium snow tileset not found, using fallback generation");
                generateFallbackSnowTiles(mediumSnowTiles, spriteCache, "medium", 0.5f);
            }

            // Load full snow tileset
            BufferedImage fullSnowAtlas = getImageFromPath("/Weather/FullSnowTileset.png");
            if (fullSnowAtlas != null) {
                loadSnowTilesFromAtlas(fullSnowAtlas, fullSnowTiles, spriteCache, "full");
            } else {
                System.out.println("Full snow tileset not found, using fallback generation");
                generateFallbackSnowTiles(fullSnowTiles, spriteCache, "full", 1.0f);
            }

        } catch (Exception e) {
            System.err.println("Error loading snow tilesets: " + e.getMessage());
            success = false;
        }

        return success;
    }

    /**
     * Loads snow tiles from an atlas and populates the sprite cache
     */
    private static void loadSnowTilesFromAtlas(BufferedImage atlas,
                                               java.util.Map<Integer, BufferedImage> tileMap,
                                               java.util.Map<String, BufferedImage> spriteCache,
                                               String snowType) {
        final int tileSize = 64; // Assuming 64x64 tiles
        final int tilesPerRow = atlas.getWidth() / tileSize;
        final int totalRows = atlas.getHeight() / tileSize;

        int tileId = 0;
        for (int row = 0; row < totalRows; row++) {
            for (int col = 0; col < tilesPerRow; col++) {
                BufferedImage tile = atlas.getSubimage(col * tileSize, row * tileSize, tileSize, tileSize);
                tileMap.put(tileId, tile);
                spriteCache.put(snowType + "_" + tileId, tile);
                tileId++;
            }
        }
    }

    /**
     * Generates fallback snow tiles when sprite sheets are not available
     */
    private static void generateFallbackSnowTiles(java.util.Map<Integer, BufferedImage> tileMap,
                                                  java.util.Map<String, BufferedImage> spriteCache,
                                                  String snowType,
                                                  float snowIntensity) {
        // Get base tileset
        BufferedImage baseAtlas = getSpriteAtlas();
        if (baseAtlas == null) return;

        // Generate snow variants for common tiles
        int[] commonTileIds = {5}; // Grass tile

        for (int tileId : commonTileIds) {
            BufferedImage baseTile = getBaseTileById(tileId, baseAtlas);
            if (baseTile != null) {
                BufferedImage snowTile = applySnowEffect(baseTile, snowIntensity);
                tileMap.put(tileId, snowTile);
                spriteCache.put(snowType + "_" + tileId, snowTile);
            }
        }
    }

    /**
     * Gets a base tile by ID from the atlas
     */
    private static BufferedImage getBaseTileById(int tileId, BufferedImage atlas) {
        // Tile layout: 4 columns, multiple rows
        int col = tileId % 4;
        int row = tileId / 4;
        int tileSize = 64;

        if (col * tileSize < atlas.getWidth() && row * tileSize < atlas.getHeight()) {
            return atlas.getSubimage(col * tileSize, row * tileSize, tileSize, tileSize);
        }
        return null;
    }

    /**
     * Applies a snow effect overlay to a base tile
     */
    private static BufferedImage applySnowEffect(BufferedImage baseTile, float intensity) {
        BufferedImage snowTile = new BufferedImage(baseTile.getWidth(), baseTile.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = snowTile.createGraphics();

        // Draw base tile
        g2d.drawImage(baseTile, 0, 0, null);

        // Apply snow overlay
        g2d.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, intensity * 0.6f));
        g2d.setColor(new java.awt.Color(255, 255, 255, (int)(intensity * 180)));

        // Create snow pattern
        for (int x = 0; x < baseTile.getWidth(); x += 8) {
            for (int y = 0; y < baseTile.getHeight(); y += 8) {
                if (Math.random() < intensity) {
                    g2d.fillOval(x, y, 3, 3);
                }
            }
        }

        g2d.dispose();
        return snowTile;
    }

}
