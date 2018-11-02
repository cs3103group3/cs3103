package main.peer;

import main.heartbeat.HeartBeatResponder;

/**
 * This class acts as the main class for a peer
 */
public class Peer {
    public static void main(String args[]) {
        Server server = new Server();
        Client client = new Client();
        
        server.start();
        client.start();
        
        HeartBeatResponder heartbeatResponder = new HeartBeatResponder();
        heartbeatResponder.start();
    }
}