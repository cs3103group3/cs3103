package main.peer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import main.heartbeat.HeartBeatSender;
import main.utilities.constants.NetworkConstant;

/**
 * This class acts as the main class for a peer
 */
public class Peer {
	
    public static void main(String args[]) {
        Server server = new Server();
        Client client = new Client();
        
        server.start();
        client.start();
        
        HeartBeatSender heartBeatSender = new HeartBeatSender();
        heartBeatSender.start();
        
        cleanUp(server, client, heartBeatSender);
    }
    
    public static void cleanUp(Server server, Client client, HeartBeatSender heartBeatSender) {
        while (client.isAlive()) {
            if (!client.isAlive()) {
                server.closeSockets();
                heartBeatSender.closeSocket();
                break;
            }
        }
    }
}