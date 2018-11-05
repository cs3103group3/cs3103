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

import main.utilities.constants.Constant;
import main.utilities.constants.NetworkConstant;
import main.tracker.Tracker;

public class HeartBeatInitiator extends Thread {
    private Timer timer;
    private ServerSocket listeningSocket;
    
    private Set<String> setOfIpAddressesThatResponded = new HashSet<String>();
    
    public void run() { 
        try {
            listeningSocket = new ServerSocket(NetworkConstant.HEARTBEAT_TRACKER_LISTENING_PORT);
            timer = new Timer();
            timer.schedule(new PingAllPeers(), 0, Constant.HEARTBEAT_INTERVAL);
            
            try {
                while (true) {
                    Socket clientSocket = listeningSocket.accept();
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    String cleintResponse = in.readLine();
                    if (cleintResponse.equals(Constant.HEARTBEAT_RESPOND)) {
                        setOfIpAddressesThatResponded.add(clientSocket.getInetAddress().getHostAddress());
                    }
                }
            } catch(IOException ioe) {
                System.out.println("Exception while listening for client connection");
            } 
        } catch (IOException e) {
            System.out.println("Exception while opening HeartBeat Port");
            e.printStackTrace();
        }
    }
    
    class PingAllPeers extends TimerTask {
        public void run() {
            Tracker.removeIpAddressesNoResponseFromRecord(setOfIpAddressesThatResponded);
            Tracker.removeFileWithEmptyRecords();
            
            String currentIpAddress = "";
            
            try {
                for (String ipAddressToPing : Tracker.aliveIpAddresses) {
                    currentIpAddress = ipAddressToPing;
                    Socket outgoingSocket = new Socket(ipAddressToPing, NetworkConstant.HEARTBEAT_PEER_LISTENING_PORT);
                    PrintWriter out = new PrintWriter(outgoingSocket.getOutputStream(), true);
                    out.println(Constant.HEARTBEAT_QUERY);
                    out.flush();
                    outgoingSocket.close();
                }
            } catch(IOException ioe) {
                System.out.println("Unable to create socket to send heartbeat query. Target client may have disconnected.");
                Tracker.removeIpAddressFromRecord(currentIpAddress);
            }
        }
    }
    
}
