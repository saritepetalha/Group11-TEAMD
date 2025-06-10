package stats;

import java.util.ArrayList;
import java.util.List;

public class ReplayManager {
    private static ReplayManager instance;
    private List<GameAction> actions;
    private boolean isRecording;

    private ReplayManager() {
        actions = new ArrayList<>();
        isRecording = false;
    }

    public static ReplayManager getInstance() {
        if (instance == null) {
            instance = new ReplayManager();
        }
        return instance;
    }

    public void startNewReplay() {
        actions.clear();
        isRecording = true;
    }

    public void stopRecording() {
        isRecording = false;
    }

    public void addAction(GameAction action) {
        if (isRecording) {
            actions.add(action);
        }
    }

    public List<GameAction> getActions() {
        return new ArrayList<>(actions);
    }

    public void clearActions() {
        actions.clear();
    }

    public ReplayRecord getCurrentReplay() {
        return new ReplayRecord(actions);
    }
} 