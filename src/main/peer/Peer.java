package main.peer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import main.heartbeat.HeartBeatResponder;
import main.utilities.constants.NetworkConstant;

/**
 * This class acts as the main class for a peer
 */
public class Peer {
	public static Socket peerSocket;
	
    public static void main(String args[]) {
    	try {
    		peerSocket = new Socket(InetAddress.getByName(NetworkConstant.TRACKER_HOSTNAME), 
    								NetworkConstant.TRACKER_LISTENING_PORT);
		} catch (IOException e) {
			System.out.println("Unable to create client socket");
			e.printStackTrace();
		}
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