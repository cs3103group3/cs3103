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
        
        cleanUp(server, client);
    }
    
    public static void cleanUp(Server server, Client client) {
        while (client.isAlive()) {
            if (!client.isAlive()) {
                server.closeSockets();
                break;
            }
        }
    }
}