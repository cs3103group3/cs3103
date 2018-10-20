package main.peer;

/**
 * This class acts as the main class for a peer
 */
public class Peer {
    public static void main(String args[]) {
        Server server = new Server();
        Client client = new Client();
        
        server.start();
        client.start();
        
        
    }
}