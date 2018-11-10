package main.utilities.constants;

public final class Constant {
    private Constant() {}  // Restrict initiation
    
    public static final String WHITESPACE = " ";
    public static final String COMMA = ",";
    public static final String NEWLINE = "\n";
    public static final String END_OF_STREAM = "EOS";
    
    public static final int CHUNK_SIZE = 1024;
    public static final String CHUNK_EXT = ".chunk";
    
    public static final int HEARTBEAT_INTERVAL = 10000;
    public static final int HEARTBEAT_TRACKER_CLEANUP_INTERVAL = HEARTBEAT_INTERVAL * 2;
    public static final String HEARTBEAT_SIGNAL = "I am alive";
    
    public static final String FILE_DIR = "../files/";
    public static final String DOWNLOAD_FROM_PEER_COMMAND = "6";
}
