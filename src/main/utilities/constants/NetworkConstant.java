package main.utilities.constants;

public final class NetworkConstant {
    private NetworkConstant() {}   // Restrict initiation
    
    // Tracker
    public static final int TRACKER_LISTENING_PORT = 7878;
    public static final String TRACKER_HOSTNAME = "172.25.105.127";
    
    // Peer Client
    public static final int CLIENT_LISTENING_PORT = 2121;
    public static final String CLIENT_HOSTNAME = "localhost";
    
    // Peer Server
    public static final int SERVER_LISTENING_PORT = 2122;
   
    public static final String SERVER_HOSTNAME = "localhost";
    
    // For testing connect();
    public static final int SERVER_LISTENING_PORTB = 2112;
    public static final int SERVER_LISTENING_PORTC = 2102;
    
    // Heartbeat Port
    public static final int HEARTBEAT_TRACKER_LISTENING_PORT = 6565;
    public static final int HEARTBEAT_CLIENT_LISTENING_PORT = 6666;
}
