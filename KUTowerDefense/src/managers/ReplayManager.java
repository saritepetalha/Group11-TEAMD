package managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import controllers.PlayingController;
import stats.GameAction;
import stats.ReplayRecord;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ReplayManager {
    private static ReplayManager instance;
    private ReplayRecord currentReplay;
    private final String REPLAY_DIR;
    private final Gson gson;
    private int replayStepIndex = 0;
    private float elapsedTime = 0;

    private ReplayManager() {
        // Always use the absolute path for resources/replays
        REPLAY_DIR = System.getProperty("user.dir") + "/KUTowerDefense/resources/replays";
        System.out.println("[ReplayManager] Using replay directory: " + REPLAY_DIR);
        gson = new GsonBuilder().setPrettyPrinting().create();
        createReplayDirectory();
    }

    public static ReplayManager getInstance() {
        if (instance == null) {
            instance = new ReplayManager();
        }
        return instance;
    }

    private void createReplayDirectory() {
        try {
            Path path = Paths.get(REPLAY_DIR);
            if (!Files.exists(path)) {
                Files.createDirectory(path);
            }
        } catch (IOException e) {
            System.err.println("Failed to create replay directory: " + e.getMessage());
        }
    }

    public void startNewReplay() {
        currentReplay = new ReplayRecord();
    }

    public void addAction(GameAction action) {
        if (currentReplay != null) {
            currentReplay.addAction(action);
        }
    }

    public void saveReplay() {
        if (currentReplay == null) return;

        // Check for duplicate replay before saving
        List<ReplayRecord> existingReplays = loadAllReplays();
        for (ReplayRecord replay : existingReplays) {
            if (replay.getMapName().equals(currentReplay.getMapName()) &&
                replay.getGoldEarned() == currentReplay.getGoldEarned() &&
                replay.getEnemiesSpawned() == currentReplay.getEnemiesSpawned() &&
                replay.getTowersBuilt() == currentReplay.getTowersBuilt() &&
                replay.getTimePlayed() == currentReplay.getTimePlayed() &&
                replay.isVictory() == currentReplay.isVictory()) {
                // Duplicate found, do not save
                System.out.println("Duplicate replay found, not saving again.");
                return;
            }
        }

        String fileName = String.format("%s/replay_%d.json", REPLAY_DIR, currentReplay.getTimestamp());
        try (Writer writer = new FileWriter(fileName)) {
            gson.toJson(currentReplay, writer);
            System.out.println("Replay saved to: " + fileName);
        } catch (IOException e) {
            System.err.println("Failed to save replay: " + e.getMessage());
        }
    }

    public List<ReplayRecord> loadAllReplays() {
        List<ReplayRecord> replays = new ArrayList<>();
        File dir = new File(REPLAY_DIR);
        if (!dir.exists()) return replays;

        File[] files = dir.listFiles((d, name) -> name.endsWith(".json"));
        if (files == null) return replays;

        // Use a Set to track unique timestamps
        Set<Long> seenTimestamps = new HashSet<>();

        for (File file : files) {
            try (Reader reader = new FileReader(file)) {
                ReplayRecord replay = gson.fromJson(reader, ReplayRecord.class);
                // Only add if we haven't seen this timestamp before
                if (seenTimestamps.add(replay.getTimestamp())) {
                    replays.add(replay);
                }
            } catch (IOException e) {
                System.err.println("Failed to load replay " + file.getName() + ": " + e.getMessage());
            }
        }

        // Sort replays by timestamp in descending order (newest first)
        replays.sort((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));

        return replays;
    }

    public ReplayRecord loadReplay(String fileName) {
        try (Reader reader = new FileReader(REPLAY_DIR + "/" + fileName)) {
            return gson.fromJson(reader, ReplayRecord.class);
        } catch (IOException e) {
            System.err.println("Failed to load replay " + fileName + ": " + e.getMessage());
            return null;
        }
    }

    public void setCurrentReplay(ReplayRecord replay) {
        this.currentReplay = replay;
    }

    public ReplayRecord getCurrentReplay() {
        return currentReplay;
    }

    public boolean isReplayMode() {
        return currentReplay != null;
    }

    public void startReplay() {
        this.replayStepIndex = 0;
        this.elapsedTime = 0;
    }

    //public void executeNextReplayActions(PlayingController controller) {
    //  if (currentReplay == null || replayStepIndex >= currentReplay.getActions().size()) return;
//
    //      List<GameAction> actions = currentReplay.getActions();
    //  elapsedTime += 1 / 60f; // assuming 60 FPS
//
    //      while (replayStepIndex < actions.size()) {
    //      GameAction action = actions.get(replayStepIndex);
    //      if (action.getGameTime() <= elapsedTime) {
    //          action.execute(controller); // Execute the action
    //          replayStepIndex++;
    //      } else {
    //          break; // Wait for the right time
    //      }
    //  }
    //}

} 