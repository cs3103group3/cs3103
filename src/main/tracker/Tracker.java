package main.tracker;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import main.utilities.commons.CheckAccuracy;
import main.utilities.constants.NetworkConstant;

/**
 * This class acts as the main class for a central directory server
 * @author cs3103 group 3
 *
 */
public class Tracker{
	static ServerSocket serverSocket;

	//ArrayList of all the files with unique names
	public static ArrayList<Record> fileArrList = new ArrayList<>();

	//To allow faster access, use a hash : fileName to its associated chunks
	public static Hashtable<String, ArrayList<Record>> recordTable = new Hashtable<>();
	public static Set<String> aliveIpAddress = new HashSet<>();
	
	//TODO: Another Hash to pinpoint location of the record
	

	public static void main(String[] args) {
		System.out.println("Starting Server");

		//Starts new instance of server
		try {
			serverSocket = new ServerSocket(NetworkConstant.TRACKER_LISTENING_PORT);
		} catch(IOException ioe) {
			System.out.println("Unable to create Server Socket");
			System.exit(1);
		}

		listenRequest();
	}

	private static void listenRequest() {
		//While server is still alive
	    int flag = 0;
	    
		while(true) {
			try {
				Socket clientSocket = serverSocket.accept();

				System.out.println("Accepted a client");
				
//				if (flag == 0) {
				    Thread helperRequest = new HelperThread(clientSocket);
	                helperRequest.start();
	                flag = 1;
//				}
				
			} catch(IOException ioe) {
				System.out.println("Error in creating listening socket");
				System.exit(1);
			}
		}
	}
}
