package main.utilities.commands;

public enum InterfaceCommand {
    INVALID(0, "Invalid Command. Please try again."),
    LIST(1, "List all files"), 
    SEARCH(2, "Search for file"),
    DOWNLOAD(3, "Download file by filename"),
    INFORM(4, "Inform Tracker about a new available file and its chunks. Eg. 4 Filename ChunkNumber"),
    QUIT(5, "Quit");
    
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
    
    // Returns the corresponding enum value by checking the code value of type int
    public static InterfaceCommand forCode(int code) {
        for (InterfaceCommand type : InterfaceCommand.values()) {
            if (type.getCommandCode() == code) {
                return type;
            }
        }
        return null;
    }
}
