package main.message;

import java.io.Serializable;
import org.apache.commons.lang.SerializationUtils;

public class Message implements Serializable{
    private MessageType type;
    private String srcAddr;
    private int srcPort;
    private String destAddr;
    private int destPort;
    private byte[] data;
    
    public Message(MessageType type, String srcAddr, int srcPort, String destAddr, int destPort, byte[] data) {
        this.type = type;
        this.srcAddr = srcAddr;
        this.srcPort = srcPort;
        this.destAddr = destAddr;
        this.destPort = destPort;
        this.data = data;
    }
    
    public MessageType getMessageType() {
        return this.type;
    }
    
    public String getSourceAddress() {
        return this.srcAddr;
    }
    
    public String getDestinationAddress() {
        return this.destAddr;
    }
    
    public int getSourcePort() {
        return this.srcPort;
    }
    
    public int getDestinationPort() {
        return this.destPort;
    }
    
    public byte[] getData() {
        return this.data;
    }
    
    public static byte[] serializeMessage(Message msg) {
        return SerializationUtils.serialize(msg);
    }
    
    public static Message deserialize(byte[] data) {
        return (Message) SerializationUtils.deserialize(data);
    }
}
