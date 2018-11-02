package main.heartbeat;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import main.peer.RequestHandler;
import main.utilities.constants.Constant;
import main.utilities.constants.NetworkConstant;

public class HeartBeatInitiator extends Thread {
    Timer timer;
    ServerSocket listeningSocket;
    
    Set<String> ipAddressSet;
    
    public HeartBeatInitiator(Set<String> set) {
        this.ipAddressSet = set;
    }
    
    public void run() { 
        try {
            listeningSocket = new ServerSocket(NetworkConstant.HEARTBEAT_TRACKER_LISTENING_PORT);
            timer = new Timer();
            timer.schedule(new PingAllPeers(), 0, 10000);
            
            ExecutorService executor = null;
            try {
                executor = Executors.newFixedThreadPool(5);
                while (true) {
                    Socket clientSocket = listeningSocket.accept();
//                    String ipAddress = clientSocket.getInetAddress().toString();
                    Runnable worker = new RequestHandler(clientSocket);
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                    out.println(Constant.HEARTBEAT);
                    out.flush();
                    executor.execute(worker);
                }
            } catch(IOException ioe) {
                System.out.println("Exception while listening for client connection");
            } finally {
                if (executor != null) {
                    executor.shutdown();
                }
            }
        } catch (IOException e) {
            System.out.println("Exception while opening HeartBeat Port");
            e.printStackTrace();
        }
        
        
    }
    
    class PingAllPeers extends TimerTask {
        public void run() {
            System.out.println(ipAddressSet);
        }
    }
    
}
