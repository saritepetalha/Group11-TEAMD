package helpMethods;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import stats.GameStatsRecord;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class GameStatsIO {

    private static final String STATS_FOLDER_PATH = "KUTowerDefense/resources/gameStats/";

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
