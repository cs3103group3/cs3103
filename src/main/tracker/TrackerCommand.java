package main.tracker;

public enum TrackerCommand {
    INVALID(0),
    LIST(1), 
    SEARCH(2),
    ADVERTISE(3),
    DOWNLOAD(4),
    EXIT(5);
    
    
    private final int code;
    
    private TrackerCommand(int code) {
        this.code = code;
    }
    
    public int getCommandCode() {
        return this.code;
    }
    
    // Map a int value to its corresponding enum value
    public static TrackerCommand forCode(int code) {
        for (TrackerCommand type : TrackerCommand.values()) {
            if (type.getCommandCode() == code) {
                return type;
            }
        }
        return null;
    }
}
