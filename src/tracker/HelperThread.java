package tracker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class HelperThread extends Thread{
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
		String clientInput = "";
		try {
			in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			reply = new PrintWriter( new OutputStreamWriter(clientSocket.getOutputStream()));
			
			while(threadRunning) {
				clientInput = in.readLine();
				System.out.println("Client has entered command: " + clientInput);
				if(!clientInput.equals("")) {
					break;
				}
			}
			
			doClientCommand(clientInput);
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
	private void doClientCommand(String strCommand) {
	    TrackerCommand command = TrackerCommand.INVALID;
	    try {
	        int commandCode = Integer.parseInt(strCommand);
	        command = TrackerCommand.forCode(commandCode);
	    } catch(NumberFormatException nfe) {
	        reply.println(TrackerErrorMessage.INVALID_COMMAND.getErrorMessage());
	        return;
	    }
	    
		switch(command) {
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
			reply.println(TrackerErrorMessage.INVALID_COMMAND.getErrorMessage());
			return;
		}
	}
}
