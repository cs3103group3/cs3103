package main.heartbeat;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import main.utilities.constants.Constant;
import main.utilities.constants.NetworkConstant;

public class HeartBeatResponder extends Thread {
    public void run() { 
        ServerSocket listeningSocket;
      //Starts new instance of server
        try {
            listeningSocket = new ServerSocket(NetworkConstant.HEARTBEAT_PEER_LISTENING_PORT);
            
            while(true) {
                try {
                    Socket incomingSocket = listeningSocket.accept();
                    Socket outgoingSocket = new Socket(NetworkConstant.TRACKER_HOSTNAME, NetworkConstant.HEARTBEAT_TRACKER_LISTENING_PORT);
                    PrintWriter out = new PrintWriter(outgoingSocket.getOutputStream(), true);
                    out.println(Constant.HEARTBEAT_RESPOND);
                    out.flush();
                    outgoingSocket.close();
                    
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
