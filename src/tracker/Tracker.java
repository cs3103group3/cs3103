package tracker;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * This class acts as the main class for a central directory server
 * @author cs3103 group 3
 *
 */
public class Tracker{
	
	private final static int SERVER_LISTENING_PORT = 7878;
	ServerSocket serverSocket;
	
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
