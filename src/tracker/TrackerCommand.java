package tracker;

public enum TrackerCommand {
    INVALID(0),
    EXIT(1),
    LIST(2), 
    SEARCH(3),
    ADVERTISE(4),
    DOWNLOAD(5);
    
    
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
