package main.heartbeat;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import main.utilities.constants.NetworkConstant;

public class HeartBeatResponder extends Thread {
    public void run() { 
        ServerSocket listeningSocket;
      //Starts new instance of server
        try {
            listeningSocket = new ServerSocket(NetworkConstant.HEARTBEAT_CLIENT_LISTENING_PORT);
            
            while(true) {
                try {
                    Socket clientSocket = listeningSocket.accept();
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                    out.println("Received a ping from Tracker");
                    out.flush();
                    clientSocket.close();
                    
                } catch(IOException ioe) {
                    System.out.println("Error in creating outgoing socket");
                    System.exit(1);
                }
            }
        } catch(IOException ioe) {
            System.out.println("Error in creating listening socket");
            System.exit(1);
        }
    }
}
