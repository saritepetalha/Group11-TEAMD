package stats;

import controllers.PlayingController;

public class GameAction {
    public enum ActionType {
        TOWER_PLACED,
        ENEMY_SPAWNED,
        ENEMY_DEFEATED,
        ENEMY_REACHED_END,
        GOLD_EARNED,
        GOLD_SPENT,
        ULTIMATE_USED,
        GAME_STARTED,
        TOWER_UPGRADED, TOWER_REVIVED, TARGETING_CHANGED, WARRIOR_SPAWNED, WARRIOR_SPAWN_INITIATED, TREE_BURNED, STONE_MINED, GOLD_COLLECTED, GOLD_BAG_DROPPED, GAME_ENDED
    }

    private final ActionType actionType;
    private final String details;
    private final int timePlayed;
    private final long gameTimeMillis;

    public GameAction(ActionType actionType, String details, int timePlayed) {
        this(actionType, details, timePlayed, -1);
    }

    public GameAction(ActionType actionType, String details, int timePlayed, long gameTimeMillis) {
        this.actionType = actionType;
        this.details = details;
        this.timePlayed = timePlayed;
        this.gameTimeMillis = gameTimeMillis;
    }

    //public void execute(PlayingController controller) {
    //    switch (type) {
    //        case TOWER_PLACED:
    //            controller.handleReplayPlaceTower(x, y, data); // data = towerType
    //            break;
    //        case STONE_MINED:
    //            controller.handleReplayMine(x, y);
    //            break;
    //        case GOLD_COLLECTED:
    //            controller.handleReplayCollectGoldBag(x, y);
    //            break;
    //        case ULTIMATE_USED:
    //            if (data != null) {
    //                switch (data) {
    //                    case "FREEZE":
    //                        controller.handleReplayUltimateFreeze();
    //                        break;
    //                    case "EARTHQUAKE":
    //                        controller.handleReplayUltimateEarthquake();
    //                        break;
    //                    case "LIGHTNING":
    //                        controller.handleReplayUltimateLightning(x, y);
    //                        break;
    //                }
    //            }
    //            break;
    //        case TREE_BURNED:
    //            controller.handleReplayTreeInteraction(x, y);
    //           break;
    //      default:
    //          // Skip actions that don't affect replay visuals
    //          break;
    //  }
    //}

    public ActionType getActionType() {
        return actionType;
    }

    public String getDetails() {
        return details;
    }

    public int getTimePlayed() {
        return timePlayed;
    }

    public long getGameTimeMillis() {
        return gameTimeMillis;
    }
} 