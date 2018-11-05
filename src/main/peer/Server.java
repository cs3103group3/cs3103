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
import main.utilities.feedbacks.ErrorMessage;

public class Server extends Thread {
	ServerSocket serverSocket;
	ArrayList<Socket> clientSocketList;

	public void run() { 
		System.out.println("Starting P2P Server");
		clientSocketList = new ArrayList<Socket>();

		//Starts new instance of server
		processConnection();
    }
	
	public void closeSockets() {
	    try {
            for (Socket clientSocket : clientSocketList) {
                clientSocket.close();
            }
        } catch (IOException e) {
            System.out.println(ErrorMessage.CANNOT_CLOSE_SOCKET + "Client not found.");
        }
	    
	    try {
            serverSocket.close();
        } catch (IOException e) {
            System.out.println(ErrorMessage.CANNOT_CLOSE_SOCKET + "ServerSocket not found.");
        }
	}
	
	private void processConnection() {
		
		ExecutorService executor = null;
		try {
			executor = Executors.newFixedThreadPool(5);
			serverSocket = new ServerSocket(NetworkConstant.SERVER_LISTENING_PORT);
			while (true) {
				Socket clientSocket = serverSocket.accept();
				clientSocketList.add(clientSocket);
				System.out.println("Accepted connection: " + clientSocket);
				Runnable worker = new RequestHandler(clientSocket);
				executor.execute(worker);
			}
		} catch(IOException ioe) {
			System.out.println("Exception while listening for client connection: " + ioe);
		} finally {
			if (executor != null) {
				executor.shutdown();
			}
		}
	}
}
