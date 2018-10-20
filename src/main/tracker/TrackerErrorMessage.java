package main.tracker;

public enum TrackerErrorMessage {
    INVALID_COMMAND(0, "Invalid Command");
    
    private final int code;
    private final String message;
    
    private TrackerErrorMessage(int code, String message) {
        this.code = code;
        this.message = message;
    }
    
    public int getErrorCode() {
        return this.code;
    }
    
    public String getErrorMessage() {
        return this.message;
    }
}
