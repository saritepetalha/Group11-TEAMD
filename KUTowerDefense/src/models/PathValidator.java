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
        System.out.println("PathValidator: Starting path validation...");
        System.out.println("PathValidator: Map size: " + level[0].length + "x" + level.length);
        System.out.println("PathValidator: Overlay size: " + overlayData[0].length + "x" + overlayData.length);

        // Debug: Print entire overlay data
        System.out.println("PathValidator: Overlay data contents:");
        for (int y = 0; y < overlayData.length; y++) {
            for (int x = 0; x < overlayData[y].length; x++) {
                if (overlayData[y][x] != PathPoints.NO_OVERLAY) {
                    System.out.println("  Position (" + x + "," + y + "): " + overlayData[y][x]);
                }
            }
        }

        // Find start and end points
        GridPoint startPoint = findPoint(overlayData, PathPoints.START_POINT);
        GridPoint endPoint = findPoint(overlayData, PathPoints.END_POINT);

        System.out.println("PathValidator: Start point found: " + startPoint);
        System.out.println("PathValidator: End point found: " + endPoint);

        // Check if both points exist
        if (startPoint == null && endPoint == null) {
            System.out.println("PathValidator: Both start and end points missing");
            return ValidationResult.invalid("Cannot save map: Both start and end points are missing!");
        }

        if (startPoint == null) {
            System.out.println("PathValidator: Start point missing");
            return ValidationResult.invalid("Cannot save map: Start point is missing!");
        }

        if (endPoint == null) {
            System.out.println("PathValidator: End point missing");
            return ValidationResult.invalid("Cannot save map: End point is missing!");
        }

        // Check path connectivity using pathfinder
        System.out.println("PathValidator: Building road network...");
        RoadNetworkPathfinder pathfinder = new RoadNetworkPathfinder(level[0].length, level.length);
        pathfinder.buildGraph(level);

        System.out.println("PathValidator: Finding path from " + startPoint + " to " + endPoint);
        List<GridPoint> path = pathfinder.findPath(startPoint, endPoint);

        if (path.isEmpty()) {
            System.out.println("PathValidator: No valid path found between start and end points");
            return ValidationResult.invalid("Cannot save map: No valid path found between start and end points!");
        }

        System.out.println("PathValidator: Valid path found with " + path.size() + " points");
        return ValidationResult.valid();
    }

    /**
     * Find a specific point type in overlay data
     * @param overlayData The overlay data array
     * @param pointType The point type to find (START_POINT or END_POINT)
     * @return GridPoint of the found point, or null if not found
     */
    private static GridPoint findPoint(int[][] overlayData, int pointType) {
        System.out.println("PathValidator: Looking for point type " + pointType + " (START=" + PathPoints.START_POINT + ", END=" + PathPoints.END_POINT + ")");
        for (int y = 0; y < overlayData.length; y++) {
            for (int x = 0; x < overlayData[y].length; x++) {
                if (overlayData[y][x] == pointType) {
                    System.out.println("PathValidator: Found point type " + pointType + " at (" + x + "," + y + ")");
                    return new GridPoint(x, y);
                }
            }
        }
        System.out.println("PathValidator: Point type " + pointType + " not found");
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