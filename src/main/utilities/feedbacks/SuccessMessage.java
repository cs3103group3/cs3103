package main.utilities.feedbacks;

public enum SuccessMessage {
    FILE_FOUND(100, "Found requested file. "),
    NEW_FILE_ADDED_TO_TRACKER(101, "New File has been successfully added to Tracker. "),
    NEW_CHUNK_ADDED_TO_TRACKER(102, "File has been successfully added to Tracker. ");
    
    private final int code;
    private final String message;
    
    private SuccessMessage(int code, String message) {
        this.code = code;
        this.message = message;
    }
    
    public int getErrorCode() {
        return this.code;
    }
    
    public String getErrorMessage() {
        return this.message;
    }
    
    public String toString() {
        return "Success " + this.code + ": " + this.message;
    }
}
