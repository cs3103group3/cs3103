package main.utilities.feedbacks;

public enum ErrorMessage {
    INVALID_COMMAND(0, "Invalid Command. "),
    FILE_NOT_FOUND(401, "Requested file cannot not be found: "),
    FILE_SIZE_ZERO(402, "File size must be greater than 0 bytes: "),
    UNKNOWN_HOST(500, "Unknown Host. "),
    UNKNOWN_ERROR(520, "Unknown Error Occurred. "),
	INVALID_NUMBEROFARGUMENTS(540, "Invalid number of arguments. "),
	INCONSISTENT_CHECKSUM(555, "Your data was corrupted, please resend data. "),
	CANNOT_CLOSE_SOCKET(558, "Unable to close socket. "),
	CANNOT_CONNECT_TO_TRACKER(560, "Lost connection with tracker");
    
    private final int code;
    private final String message;
    
    private ErrorMessage(int code, String message) {
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
        return "ERROR " + this.code + ": " + this.message;
    }
}
