package main.tracker;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Hashtable;

/**
 * This class acts as the main class for a central directory server
 * @author cs3103 group 3
 *
 */
public class Tracker{

	public final static int SERVER_LISTENING_PORT = 7878;
	ServerSocket serverSocket;
	//ArrayList of all the files with unique names
	public static ArrayList<Record> fileArrList = new ArrayList<>();

	//To allow faster access, use a hash
	public static Hashtable<String, ArrayList<Record>> recordTable = new Hashtable<>();
	//Another Hash to pinpoint location of the record
	

	public void main(String[] args) {
		System.out.println("Starting Server");


		//Starts new instance of server
		try {
			serverSocket = new ServerSocket(SERVER_LISTENING_PORT);
		} catch(IOException ioe) {
			System.out.println("Unable to create Server Socket");
			System.exit(1);
		}

		listenRequest();
	}

	private void listenRequest() {
		//While server is still alive
		while(true) {
			try {
				Socket clientSocket = serverSocket.accept();

				HelperThread helperRequest = new HelperThread(clientSocket);
				helperRequest.run();
			} catch(IOException ioe) {
				System.out.println("Error in creating listening socket");
				System.exit(1);
			}
		}
	}
}
