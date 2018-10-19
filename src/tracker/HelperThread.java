package tracker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class HelperThread extends Thread{
	//Commands available for Users
	private static final String LIST = "LIST";
	private static final String SEARCH = "SEARCH";
	private static final String ADVERTISE = "ADVERTISE";
	private static final String DOWNLOAD = "DOWNLOAD";
	private static final String EXIT = "EXIT";
	private static final String INVALID_COMMAND = "Invalid Command";
	//Client Socket
	Socket clientSocket;
	//private List<String>
	BufferedReader in = null;
	PrintWriter reply = null;
	public HelperThread() {
		
	}
	public HelperThread(Socket client) {
		clientSocket = client;
	}
	
	public void run() {

		boolean threadRunning = true;
		String clientCommands = "";
		try {
			in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			reply = new PrintWriter( new OutputStreamWriter(clientSocket.getOutputStream()));
			
			while(threadRunning) {
				clientCommands = in.readLine();
				System.out.println("Client has entered command: " + clientCommands);
				if(!clientCommands.equals("")) {
					break;
				}
			}
			
			doClientCommands(clientCommands);
		} catch (IOException e) {
			System.out.println("Io Exception");
			
		}
	}
	
	/**
	 * This method executes the commands the client requested
	 * 1) List the Directory
	 * 2) Search for intended content
	 * 3) Advertise their chunk for sharing
	 * 4) Download File
	 * 5) Exit
	 */
	private void doClientCommands(String clientCommands) {
		switch(clientCommands) {
		case LIST:
			//perform list
			break;
		case SEARCH:
			//perform search
			break;
		case ADVERTISE:
			//perform advertise chunk
			break;
		case DOWNLOAD:
			//perform downloading of file
			break;
		case EXIT:
			//perform exit
			break;
		default:
			//Error
			reply.println(INVALID_COMMAND);
		}
	}
}
