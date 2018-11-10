package main.heartbeat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import main.tracker.Tracker;
import main.utilities.constants.Constant;
import main.utilities.constants.NetworkConstant;
import main.utilities.feedbacks.ErrorMessage;

public class HeartBeatSender extends Thread{
	 private Timer timer;
	    private ServerSocket listeningSocket;
	    
	    public void run() { 
	        try {
	        	listeningSocket = new ServerSocket(NetworkConstant.HEARTBEAT_TRACKER_LISTENING_PORT);
	            timer = new Timer();
	            timer.schedule(new PingTracker(), 5000, Constant.HEARTBEAT_INTERVAL);
	        } catch (IOException e) {
	            System.out.println("Exception while opening HeartBeat Port");
	            e.printStackTrace();
	        }
	    }
	    
	    class PingTracker extends TimerTask {
	        public void run() {
	            try {
                    Socket outgoingSocket = new Socket(NetworkConstant.TRACKER_HOSTNAME, NetworkConstant.HEARTBEAT_PEER_LISTENING_PORT);
                    PrintWriter out = new PrintWriter(outgoingSocket.getOutputStream(), true);
                    out.println(Constant.HEARTBEAT_QUERY);
                    out.flush();
                    outgoingSocket.close();
	            } catch(IOException ioe) {
	                System.out.println("Unable to create socket to send heartbeat query. Target client may have disconnected.");
	            }
	        }
	    }
	    
	    public void closeSocket() {
	        try {
	            listeningSocket.close();
	        } catch (IOException e) {
	            System.out.println(ErrorMessage.CANNOT_CLOSE_SOCKET + "HeartbeatSender Listening Socket not found.");
	        }
	    }
}
