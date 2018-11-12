package main.heartbeat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import main.tracker.Tracker;
import main.utilities.constants.Constant;
import main.utilities.constants.NetworkConstant;
import main.tracker.Tuple;

public class HeartBeatListener extends Thread {
    ServerSocket listeningSocket;
    Set<Tuple> listOfRespondedPeerInfo = new HashSet<Tuple>();
    private Timer timer;
    
    public void run() { 
        ExecutorService executor= null;
        //While server is still alive
        try {
            listeningSocket = new ServerSocket(NetworkConstant.HEARTBEAT_TRACKER_LISTENING_PORT);
            timer = new Timer();
            timer.schedule(new TrackerCleanUp(), 0, Constant.HEARTBEAT_TRACKER_CLEANUP_INTERVAL);
            executor = Executors.newFixedThreadPool(20);

            while(true) {
                Socket clientSocket = listeningSocket.accept();
                String ipAddress = clientSocket.getInetAddress().getHostAddress();
                String heartBeatPort = Integer.toString(clientSocket.getPort());
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String dataPort = in.readLine();
                System.out.println("Received a heartbeat signal from [" + ipAddress + ": " + heartBeatPort + "], with dataport = " + dataPort);
            	markPeerAsResponded(ipAddress, dataPort);
            }

        } catch(IOException ioe) {
            System.out.println("Error in creating listening socket");
            System.exit(1);
        }
    }
    
    private void markPeerAsResponded(String ipAddress, String port) {
        Tuple peerInfo = new Tuple(ipAddress, port);
        listOfRespondedPeerInfo.add(peerInfo);
    }
    
    class TrackerCleanUp extends TimerTask {
        public void run() {
//            Tracker.removeUnresponsivePeersFromRecord(listOfRespondedPeerInfo);
                        
            listOfRespondedPeerInfo = new HashSet<Tuple>();
        }
    }
}
