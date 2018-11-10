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
	 
	 private Socket outgoingSocket;
	    
    public void run() { 
    	timer = new Timer();
        timer.schedule(new PingTracker(), 5000, Constant.HEARTBEAT_INTERVAL);
    }
    
    class PingTracker extends TimerTask {
        public void run() {
            try {
                outgoingSocket = new Socket(NetworkConstant.TRACKER_HOSTNAME, NetworkConstant.HEARTBEAT_TRACKER_LISTENING_PORT);
                PrintWriter out = new PrintWriter(outgoingSocket.getOutputStream(), true);
                out.println(Constant.HEARTBEAT_SIGNAL);
                out.flush();
                outgoingSocket.close();
            } catch(IOException ioe) {
                System.out.println("Unable to create socket to send heartbeat query. Target client may have disconnected.");
            }
        }
    }
}
