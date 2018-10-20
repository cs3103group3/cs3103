package main.utilities.command;

public enum OfflineInterfaceCommand {
    INVALID(0, "Invalid Command. Please try again."),
    CONNECT_TO_TRACKER(1, "Connect to Centralised Directory Server"), 
    QUIT(2, "Quit");
    
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
}
