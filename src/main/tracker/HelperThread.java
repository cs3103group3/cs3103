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
	    String [] strCommandArr;
	    try {
	    	strCommand = strCommand.trim();
	    	strCommandArr = strCommand.split(" ");
	        int commandCode = Integer.parseInt(strCommandArr[0]);
	        command = InterfaceCommand.forCode(commandCode);
	    } catch(NumberFormatException nfe) {
	        reply.println(ErrorMessage.INVALID_COMMAND.getErrorMessage());
	        return;
	    }
	   
	    
	    //Calls method that is required to execute the user's command
		switch(command) {
		case LIST:
			//perform list
			listDirectoryEntry();
			break;
		case CHANGE_DIRECTORY:
            //Change directory
			changeDirectory(strCommandArr);
            break;
		case SEARCH:
			//perform search
			searchEntry(strCommandArr);
			break;
		case DOWNLOAD:
			//Finds peer to download the file requested
			findPeer(strCommandArr);
			break;
		case INFORM:
            //Update the server of newly advertised chunk of file
            informServer(strCommandArr);
			break;
		case QUIT:
			//perform exit
			exitServer();
			break;
		default:
			//Error
			reply.println(ErrorMessage.INVALID_COMMAND.getErrorMessage());
			return;
		}
	}
	

	/**
	 * Lists the directory Entry
	 * 
	 * Example Input:
	 * 1
	 * Expected Output:
	 * Directory Entry
	 */
	private synchronized void listDirectoryEntry() {
		
	}
	
	/**
	 * Changes the directory entry
	 * @param strCommandArr: the Array of command by client that has been split
	 * 
	 * Example Input:
	 * 2 /FolderTwo
	 * Expected Output:
	 * Directory Changed to '/FolderTwo'
	 * Then calls listDirectoryEntry again
	 */
	private synchronized void changeDirectory(String[] strCommandArr) {
		
		
	}
	
	
	/**
	 * Search Entry by the following
	 * @param strCommandArr:  the Array of command by client that has been split
	 * 1) FileName
	 * 2) Chunk Number
	 * 
	 * Example Input One:
	 * 3 fileNameOne
	 * Expected Output:
	 * HostName/IP, FileName to be written back to the peer who requested it
	 * 
	 * Example Input Two:
	 * 3 chunkOne
	 * Expected Output:
	 * HostName/IP, FileName to be written back to the peer who requested it
	 * 
	 * Invalid Output:
	 * "Invalid fileName or Chunk Number specified"
	 */
	private synchronized void searchEntry(String[] strCommandArr) {
		
	}
	
	/**
	 * Informs the Server of the new chunk to be advertised
	 * @param strCommandArr
	 * 
	 * Format of Input:
	 * 4 hostname/IP FileName chunkNumber
	 * Example Input:
	 * 4 192.168.1.192 file.txt 5
	 * 
	 * Notes:
	 * 1) Might need to check if IP is valid
	 * 2) Might need to check if fileName is valid
	 */
	private synchronized void informServer(String[] strCommandArr) {
		
		
	}
	
	/**
	 * Asks the server for location of file or chunk from a peer
	 * @param strCommandArr
	 * 
	 * Format of Input:
	 * 5 fileName/chunkNumber
	 * 
	 * Example Input:
	 * 5 example.txt
	 * Expected Output:
	 * IP address is output to user
	 * Invalid Output:
	 * Unable to find specific file or chunk
	 * 
	 */
	private synchronized void findPeer(String[] strCommandArr) {
		
		
	}
	
	/**
	 * Exits the server
	 * 
	 * Example Input:
	 * 6
	 * 
	 * Expected Output:
	 * You have exited the server
	 * 
	 * Note:
	 * 1) Have to delete the fileName/chunk listed in the central server
	 */
	private void exitServer() {
		
		
	}


}
