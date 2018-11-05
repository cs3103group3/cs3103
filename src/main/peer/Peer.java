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
        
        cleanUp(server, client, heartbeatResponder);
    }
    
    public static void cleanUp(Server server, Client client, HeartBeatResponder heartbeatResponder) {
        while (client.isAlive()) {
            if (!client.isAlive()) {
                server.closeSockets();
                heartbeatResponder.closeSocket();
                break;
            }
        }
    }
}