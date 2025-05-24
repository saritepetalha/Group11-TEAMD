package popUps;

public interface Dialogue<T> {
    /**
     * Open the dialog (blocking or non-blocking)
     * and return whatever the user entered (or null if cancelled).
     */
    T showAndWait();
}
