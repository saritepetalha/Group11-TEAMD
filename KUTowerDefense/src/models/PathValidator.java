package models;

import constants.Constants.PathPoints;
import objects.GridPoint;
import pathfinding.RoadNetworkPathfinder;

import java.util.List;

/**
 * PathValidator - Validates path connectivity in map editor
 * Responsibilities:
 * - Check if start and end points exist
 * - Verify path connectivity between start and end points
 * - Provide validation error messages
 */
public class PathValidator {

    /**
     * Validates that the map has a valid path configuration
     * @param level The map data
     * @param overlayData The overlay data containing start/end points
     * @return ValidationResult containing success status and error message
     */
    public static ValidationResult validatePath(int[][] level, int[][] overlayData) {
        // Find start and end points
        GridPoint startPoint = findPoint(overlayData, PathPoints.START_POINT);
        GridPoint endPoint = findPoint(overlayData, PathPoints.END_POINT);

        // Check if both points exist
        if (startPoint == null && endPoint == null) {
            return ValidationResult.invalid("Harita kaydedilemez: Başlangıç ve bitiş noktaları eksik!");
        }

        if (startPoint == null) {
            return ValidationResult.invalid("Harita kaydedilemez: Başlangıç noktası eksik!");
        }

        if (endPoint == null) {
            return ValidationResult.invalid("Harita kaydedilemez: Bitiş noktası eksik!");
        }

        // Check path connectivity using pathfinder
        RoadNetworkPathfinder pathfinder = new RoadNetworkPathfinder(level[0].length, level.length);
        pathfinder.buildGraph(level);

        List<GridPoint> path = pathfinder.findPath(startPoint, endPoint);

        if (path.isEmpty()) {
            return ValidationResult.invalid("Harita kaydedilemez: Başlangıç ve bitiş noktaları arasında geçerli bir yol bulunamadı!");
        }

        return ValidationResult.valid();
    }

    /**
     * Find a specific point type in overlay data
     * @param overlayData The overlay data array
     * @param pointType The point type to find (START_POINT or END_POINT)
     * @return GridPoint of the found point, or null if not found
     */
    private static GridPoint findPoint(int[][] overlayData, int pointType) {
        for (int y = 0; y < overlayData.length; y++) {
            for (int x = 0; x < overlayData[y].length; x++) {
                if (overlayData[y][x] == pointType) {
                    return new GridPoint(x, y);
                }
            }
        }
        return null;
    }

    /**
     * Validation result container
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String errorMessage;

        private ValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }

        public static ValidationResult valid() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult invalid(String errorMessage) {
            return new ValidationResult(false, errorMessage);
        }

        public boolean isValid() {
            return valid;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }
}