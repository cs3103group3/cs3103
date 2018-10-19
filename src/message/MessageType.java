package message;

public enum MessageType {
    LIST(0),
    SEARCH(1), 
    ADVERTISE(2),
    DOWNLOAD(3),
    EXIT(4),
    INVALID(5);
    
    private int value;
    MessageType(int value) {
        this.value = value;
    }
}
