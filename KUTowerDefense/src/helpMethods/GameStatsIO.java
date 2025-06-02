package helpMethods;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import stats.GameStatsRecord;

public class GameStatsIO {

    private static final String STATS_FOLDER_PATH = getGameStatsDirectoryPath();

    /**
     * Detects if we're running in a Maven project structure
     */
    private static boolean isMavenProject() {
        File pomFile = new File("pom.xml");
        return pomFile.exists();
    }

    /**
     * Gets the appropriate game stats directory path based on project structure
     */
    private static String getGameStatsDirectoryPath() {
        if (isMavenProject()) {
            return "src/main/resources/gameStats/";
        } else {
            return "resources/gameStats/";
        }
    }

    public static void saveToFile(GameStatsRecord record) {
        File folder = new File(STATS_FOLDER_PATH);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String fileName = STATS_FOLDER_PATH + "stat_" + System.currentTimeMillis() + ".json";
        try (Writer writer = new FileWriter(fileName)) {
            gson.toJson(record, writer);
            System.out.println("Saved game stat to: " + fileName);
        } catch (IOException e) {
            System.err.println("Error saving game stat: " + e.getMessage());
        }
    }


    private static void ensureStatsDirectoryExists() {
        File dir = new File(STATS_FOLDER_PATH);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (!created) {
                System.err.println("Failed to create stats directory: " + STATS_FOLDER_PATH);
            }
        }
    }
    public static List<GameStatsRecord> loadAllStats() {
        List<GameStatsRecord> statsList = new ArrayList<>();
        File folder = new File(STATS_FOLDER_PATH);
        if (!folder.exists() || !folder.isDirectory()) return statsList;

        Gson gson = new Gson();
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".json"));
        if (files == null) return statsList;

        for (File file : files) {
            try (FileReader reader = new FileReader(file)) {
                GameStatsRecord record = gson.fromJson(reader, GameStatsRecord.class);
                if (record != null) {
                    statsList.add(record);
                }
            } catch (IOException e) {
                System.err.println("Failed to read stats file: " + file.getName() + " -> " + e.getMessage());
            }
        }

        return statsList;
    }
}
