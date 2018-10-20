package main.utilities.command;

public enum InterfaceCommand {
    INVALID(0, "Invalid Command. Please try again"),
    LIST(1, "List all files"), 
    CHANGE_DIRECTORY(2, "Change Directory"),
    SEARCH(3, "Search for file"),
    DOWNLOAD(4, "Download file"),
    INFORM(5, "Inform Tracker about new file"),
    QUIT(6, "Goodbye!");
    
    
    private final int code;
    private final String text;
    
    private InterfaceCommand(int code, String text) {
        this.code = code;
        this.text = text;
    }
    
    public int getCommandCode() {
        return this.code;
    }
    
    public String getCommandText() {
        return this.text;
    }
    
    // Map a int value to its corresponding enum value
    public static InterfaceCommand forCode(int code) {
        for (InterfaceCommand type : InterfaceCommand.values()) {
            if (type.getCommandCode() == code) {
                return type;
            }
        }
        return null;
    }
}
