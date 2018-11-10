package main.peer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

import main.heartbeat.HeartBeatSender;
import main.utilities.constants.NetworkConstant;

/**
 * This class acts as the main class for a peer
 */
public class Peer {
	static Socket listeningSocket;
	static int listeningPort;
	
    public static void main(String args[]) {
    	processConnection();
        Server server = new Server();
        Client client = new Client();
        
        server.start();
        client.start();
        
        cleanUp(server, client);
    }
    
    private static void processConnection() {
		
		try {
			listeningSocket = new Socket(InetAddress.getByName(NetworkConstant.TRACKER_HOSTNAME), NetworkConstant.TRACKER_LISTENING_PORT);
			listeningPort = listeningSocket.getLocalPort();
			System.out.println("port of peer: " + listeningSocket.getPort());
			System.out.println("local port of peer: " + listeningSocket.getLocalPort());
		} catch (IOException e) {
			System.out.println("Unable to create listening socket");
			e.printStackTrace();
		}
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