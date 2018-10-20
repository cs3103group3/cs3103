package main.message;


public class Message {
    private MessageType type;
    private byte[] data;
    
    public Message(MessageType type, byte[] data) {
        this.type = type;
        this.data = data;
    }
    
    public MessageType getMessageType() {
        return this.type;
    }
    
    public byte[] getData() {
        return this.data;
    }
}
