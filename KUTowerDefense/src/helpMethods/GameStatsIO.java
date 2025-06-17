package helpMethods;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import stats.GameStatsRecord;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class GameStatsIO {

    private static final String STATS_FOLDER_PATH = getStatsDirectoryPath();

    /**
     * Detects if we're running in a Maven project structure
     */
    private static boolean isMavenProject() {
        // Check if we're in a Maven project by looking for pom.xml in the expected location
        File folder = new File("demo/pom.xml");
        return folder.exists();
    }

    /**
     * Gets the appropriate stats directory path based on project structure
     */
    private static String getStatsDirectoryPath() {
        if (isMavenProject()) {
            return "demo/src/main/resources/gameStats/";
        } else {
            return "KUTowerDefense/resources/gameStats/";
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
    /**
     * Deletes a statistics file by filename
     * @param filename The name of the file to delete
     * @return true if the file was successfully deleted, false otherwise
     */
    public static boolean deleteStatFileByName(String filename) {
        File folder = new File(STATS_FOLDER_PATH);
        if (!folder.exists() || !folder.isDirectory()) {
            System.err.println("Stats directory does not exist: " + STATS_FOLDER_PATH);
            return false;
        }

        File fileToDelete = new File(folder, filename);
        if (!fileToDelete.exists()) {
            System.err.println("File does not exist: " + filename);
            return false;
        }

        boolean deleted = fileToDelete.delete();
        if (deleted) {
            System.out.println("Successfully deleted stats file: " + filename);
            return true;
        } else {
            System.err.println("Failed to delete stats file: " + filename);
            return false;
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
                    record.setSourceFilename(file.getName()); // Track which file this came from
                    statsList.add(record);
                }
            } catch (IOException e) {
                System.err.println("Failed to read stats file: " + file.getName() + " -> " + e.getMessage());
            }
        }

        return statsList;
    }

    /**
     * Deletes a statistics file that matches the given record
     * @param recordToDelete The GameStatsRecord to delete from disk
     * @return true if the file was successfully deleted, false otherwise
     */
    public static boolean deleteStatFile(GameStatsRecord recordToDelete) {
        // If the record has a source filename, use it directly
        if (recordToDelete.getSourceFilename() != null) {
            return deleteStatFileByName(recordToDelete.getSourceFilename());
        }

        // Fallback to the old matching method
        File folder = new File(STATS_FOLDER_PATH);
        if (!folder.exists() || !folder.isDirectory()) {
            System.err.println("Stats directory does not exist: " + STATS_FOLDER_PATH);
            return false;
        }

        Gson gson = new Gson();
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".json"));
        if (files == null) {
            System.err.println("No JSON files found in stats directory");
            return false;
        }

        for (File file : files) {
            try (FileReader reader = new FileReader(file)) {
                GameStatsRecord record = gson.fromJson(reader, GameStatsRecord.class);
                if (record != null && recordsMatch(record, recordToDelete)) {
                    boolean deleted = file.delete();
                    if (deleted) {
                        System.out.println("Successfully deleted stats file: " + file.getName());
                        return true;
                    } else {
                        System.err.println("Failed to delete stats file: " + file.getName());
                        return false;
                    }
                }
            } catch (IOException e) {
                System.err.println("Error reading stats file for deletion: " + file.getName() + " -> " + e.getMessage());
            }
        }

        System.err.println("No matching stats file found for deletion");
        return false;
    }

    /**
     * Checks if two GameStatsRecord objects match (same data)
     */
    private static boolean recordsMatch(GameStatsRecord record1, GameStatsRecord record2) {
        if (record1 == null || record2 == null) return false;

        // Handle null map names
        String mapName1 = record1.getMapName();
        String mapName2 = record2.getMapName();
        boolean mapNamesMatch = (mapName1 == null && mapName2 == null) ||
                (mapName1 != null && mapName1.equals(mapName2));

        boolean matches = mapNamesMatch &&
                record1.isVictory() == record2.isVictory() &&
                record1.getGold() == record2.getGold() &&
                record1.getEnemiesSpawned() == record2.getEnemiesSpawned() &&
                record1.getEnemiesReachedEnd() == record2.getEnemiesReachedEnd() &&
                record1.getTowersBuilt() == record2.getTowersBuilt() &&
                record1.getEnemyDefeated() == record2.getEnemyDefeated() &&
                record1.getTotalDamage() == record2.getTotalDamage() &&
                record1.getTimePlayed() == record2.getTimePlayed();

        System.out.println("Comparing records: " +
                "Map1=" + mapName1 + ", Map2=" + mapName2 +
                ", Gold1=" + record1.getGold() + ", Gold2=" + record2.getGold() +
                ", Match=" + matches);

        return matches;
    }
}
