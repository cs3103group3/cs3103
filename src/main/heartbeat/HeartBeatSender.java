package main.heartbeat;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

import main.utilities.constants.Constant;
import main.utilities.constants.NetworkConstant;

public class HeartBeatSender extends Thread{
	private Timer timer;
	
	private int port;
	private Socket outgoingSocket;
	 
	public HeartBeatSender(int port) {
	    this.port = port;
	}
	    
    public void run() { 
    	timer = new Timer();
    	fireHeartBeatSignal();
        timer.schedule(new PingTracker(), 0, Constant.HEARTBEAT_INTERVAL);
    }
    
    private void fireHeartBeatSignal() {
        try {
            outgoingSocket = new Socket(NetworkConstant.TRACKER_HOSTNAME, NetworkConstant.HEARTBEAT_TRACKER_LISTENING_PORT);
            PrintWriter out = new PrintWriter(outgoingSocket.getOutputStream(), true);
            out.println(port);
            out.flush();
            outgoingSocket.close();
        } catch(IOException ioe) {
            System.out.println("Unable to create socket to send heartbeat query. Target client may have disconnected.");
        }
    }
    
    class PingTracker extends TimerTask {
        public void run() {
            fireHeartBeatSignal();
        }
    }
}
