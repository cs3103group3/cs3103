package main.tracker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

import main.utilities.command.InterfaceCommand;
import main.utilities.error.ErrorMessage;

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
	 */
	private void doClientCommand(String strCommand) {
	    InterfaceCommand command = InterfaceCommand.INVALID;
	    try {
	        int commandCode = Integer.parseInt(strCommand);
	        command = InterfaceCommand.forCode(commandCode);
	    } catch(NumberFormatException nfe) {
	        reply.println(ErrorMessage.INVALID_COMMAND.getErrorMessage());
	        return;
	    }
	    
		switch(command) {
		case LIST:
			//perform list
			break;
		case CHANGE_DIRECTORY:
            //Change directory
            break;
		case SEARCH:
			//perform search
			break;
		case DOWNLOAD:
			//perform downloading of file
			break;
		case INFORM:
            //perform downloading of file
            break;
		case QUIT:
			//perform exit
			break;
		default:
			//Error
			reply.println(ErrorMessage.INVALID_COMMAND.getErrorMessage());
			return;
		}
	}
}
