package models;

/**
 * Result class for tile placement operations
 * Provides better error handling with success/failure states and error messages
 */
public class PlacementResult {
    private final boolean success;
    private final String errorMessage;
    
    private PlacementResult(boolean success, String errorMessage) {
        this.success = success;
        this.errorMessage = errorMessage;
    }
    
    public static PlacementResult success() {
        return new PlacementResult(true, null);
    }
    
    public static PlacementResult invalid(String message) {
        return new PlacementResult(false, message);
    }
    
    public boolean isSuccess() { return success; }
    public String getErrorMessage() { return errorMessage; }
} 