package main.utilities.errors;

public enum ErrorMessage {
    INVALID_COMMAND(0, "Invalid Command"),
    UNKNOWN_HOST(500, "Unknown Host"),
    UNKNOWN_ERROR(520, "Unknown Error Occurred"),
	INVALID_NUMBEROFARGUMENTS(540, "Invalid number of arguments");
    
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
}
