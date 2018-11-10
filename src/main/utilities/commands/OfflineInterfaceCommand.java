package main.utilities.commands;

public enum OfflineInterfaceCommand {
    INVALID(0, "Invalid Command. Please try again."),
    CONNECT_TO_TRACKER(1, "Connect to Centralised Directory Server"), 
    EMPTY_RECORD(2, "Directory List is empty"),
    QUIT(5, "Quit");
    
    private final int code;
    private final String text;
    
    private OfflineInterfaceCommand(int code, String text) {
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
    public static OfflineInterfaceCommand forCode(int code) {
        for (OfflineInterfaceCommand type : OfflineInterfaceCommand.values()) {
            if (type.getCommandCode() == code) {
                return type;
            }
        }
        return null;
    }
    
    public String toString() {
        return this.text;
    }
}
