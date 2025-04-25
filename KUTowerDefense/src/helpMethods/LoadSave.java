package helpMethods;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;

import com.google.gson.*;

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


    //This function saves the game to a json file as a 2d array with key: tiles
    //If it is wished the same json file could be used for saving the game state
    //As in the location of the enemy groups, location of towers etc.
    //THIS FUNCTION'S LOGIC CHANGED TO IMPLEMENT SAVE LEVEL LOGIC
    public static void createLevel(String fileName, int[][] tiles) {
        File levelsFolder = new File("resources/Levels");
        if (!levelsFolder.exists()) {
            levelsFolder.mkdirs();
        }

        File file = new File("resources/Levels/" + fileName + ".json");
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
        File file = new File("resources/Levels/" + fileName + ".json");
        if (!file.exists()) {
            System.err.println("Level file not found: " + file.getPath());
            return null;
        }

        try (Reader reader = new FileReader(file)) {
            // 1) Parse the JSON root object
            JsonObject root = GSON.fromJson(reader, JsonObject.class);

            // 2) Get the "tiles" JsonArray
            JsonArray tilesJson = root.getAsJsonArray("tiles");

            // 3) Deserialize that JsonArray directly into int[][] 
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
        File levelsFolder = new File("resources/Levels");
        
        if (!levelsFolder.exists()) {
            levelsFolder.mkdirs();
            return levelNames;
        }

        File[] files = levelsFolder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".json")) {
                    levelNames.add(file.getName().replace(".json", ""));
                }
            }
        }
        return levelNames;
    }
}
