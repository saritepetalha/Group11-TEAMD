package helpMethods;

import javax.imageio.ImageIO;
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



    //This function saves the game to a json file as a 2d array with key: tiles
    //If it is wished the same json file could be used for saving the game state
    //As in the location of the enemy groups, location of towers etc.
    //THIS FUNCTION'S LOGIC CHANGED TO IMPLEMENT SAVE LEVEL LOGIC
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
        JsonElement tilesJson = gson.toJsonTree(tiles); // serializes your int[][]
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
    //FRONT END IS NOT IMPLEMENTED YET
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
        String levelPath = "KUTowerDefense/resources/Levels/" + levelName + ".json";
        File levelFile = new File(levelPath);

        System.out.println("Loading level path: " + levelFile.getAbsolutePath());
        System.out.println("Level file exists: " + levelFile.exists());

        if (!levelFile.exists()) {
            System.out.println("Level file not found!");
            return null;
        }

        try {
            JsonReader reader = new JsonReader(new FileReader(levelFile));
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

            System.out.println("Level successfully loaded: " + levelName);
            return level;
        } catch (Exception e) {
            System.out.println("Error loading level: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
