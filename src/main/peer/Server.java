package main.peer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import main.tracker.Record;
import main.utilities.commons.CheckAccuracy;
import main.utilities.commands.OfflineInterfaceCommand;
import main.utilities.constants.NetworkConstant;

public class Server extends Thread {
	static ServerSocket serverSocket;

	public void run() { 
		System.out.println("Starting P2P Server");

		//Starts new instance of server
		processConnection();
    }
	
	private static void processConnection() {
		
		ExecutorService executor = null;
		try {
			executor = Executors.newFixedThreadPool(5);
			serverSocket = new ServerSocket(NetworkConstant.SERVER_LISTENING_PORT);
			while (true) {
				Socket clientSocket = serverSocket.accept();
				Runnable worker = new RequestHandler(clientSocket);
				executor.execute(worker);
			}
		} catch(IOException ioe) {
			System.out.println("Exception while listening for client connection");
		} finally {
			if (executor != null) {
				executor.shutdown();
			}
		}
	}
}
