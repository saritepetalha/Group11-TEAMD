package managers;

import helpMethods.GameStatsIO;
import stats.GameStatsRecord;

import java.util.ArrayList;
import java.util.List;

public class GameStatsManager {
    private final List<GameStatsRecord> records = new ArrayList<>();

    public void addRecord(GameStatsRecord record) {
        records.add(record);
    }

    public List<GameStatsRecord> getRecords() {
        return records;
    }

    public List<GameStatsRecord> getAllStats() {
        return records;
    }
    public void loadFromFiles() {
        records.clear();
        List<GameStatsRecord> loaded = GameStatsIO.loadAllStats();
        records.addAll(loaded);
        System.out.println("Loaded " + loaded.size() + " game stats from disk.");
    }
    public void saveToFile(GameStatsRecord record) {
        GameStatsIO.saveToFile(record);
    }

    /**
     * Deletes a statistics record from both memory and disk
     * @param record The GameStatsRecord to delete
     * @return true if the record was successfully deleted, false otherwise
     */
    public boolean deleteRecord(GameStatsRecord record) {
        if (record == null) return false;

        // Remove from memory
        boolean removedFromMemory = records.remove(record);

        // Remove from disk
        boolean removedFromDisk = GameStatsIO.deleteStatFile(record);

        if (removedFromMemory && removedFromDisk) {
            System.out.println("Successfully deleted statistics record");
            return true;
        } else if (removedFromMemory && !removedFromDisk) {
            System.err.println("Warning: Record removed from memory but failed to delete from disk");
            return false;
        } else if (!removedFromMemory && removedFromDisk) {
            System.err.println("Warning: Record deleted from disk but was not found in memory");
            return false;
        } else {
            System.err.println("Failed to delete statistics record");
            return false;
        }
    }

}
