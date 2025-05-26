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

}
