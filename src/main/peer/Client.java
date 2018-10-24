package main.peer;

import java.util.ArrayList;
import java.util.Scanner;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.io.IOException;

import main.tracker.Record;
import main.utilities.commands.InterfaceCommand;
import main.utilities.errors.ErrorMessage;
import main.utilities.constants.NetworkConstant;
import main.utilities.constants.Constant;

public class Client extends Thread {    
    private static void displayMenu() {
        System.out.println( "===============================================\n" +
                            "Welcome to CS3103 P2P Client\n" +
                            "Choose From the list of actions\n" +
                            "1. " + InterfaceCommand.LIST.getCommandText() + "\n" +
                            "2. " + InterfaceCommand.SEARCH.getCommandText() + "\n" +
                            "3. " + InterfaceCommand.DOWNLOAD.getCommandText() + "\n" +
                            "4. " + InterfaceCommand.INFORM.getCommandText() + "\n" +
                            "5. " + InterfaceCommand.QUIT.getCommandText() + "\n" +
                            "===============================================\n" + 
                            "Enter your option: ");
    }

    private boolean execute() {
        Scanner sc = new Scanner(System.in);
        String userInput = sc.nextLine().trim();
        String[] userInputArr = userInput.split(Constant.WHITESPACE);
        String userSelectedOption = userInputArr[0].trim();
        
        InterfaceCommand command = InterfaceCommand.INVALID;
        try {
            command = InterfaceCommand.forCode(Integer.parseInt(userSelectedOption));
            command = (command==null) ? InterfaceCommand.INVALID : command;
        } catch (NumberFormatException e) {
            command = InterfaceCommand.INVALID;
        }
        
        try {
            switch(command) {
                case LIST:
                    list(userInputArr);
                    return true;
                case SEARCH:
                    search(userInputArr);
                    return true;
                case DOWNLOAD:
                    download(userInputArr);
                    return true;
                case INFORM:
                    inform(userInputArr);
                    return true;
                case QUIT:
                    quit(userInputArr);
                    return false;
                default:
                    System.out.println(ErrorMessage.INVALID_COMMAND.getErrorMessage());
                    return true;
            }
        } catch (UnknownHostException e) {
            System.out.println(ErrorMessage.UNKNOWN_HOST.getErrorMessage());
            return true;
        } catch (Exception e) {
            System.out.println(ErrorMessage.UNKNOWN_ERROR.getErrorMessage());
            e.printStackTrace();
            return true;  // So as not to quit the program, proceed as normal
        }        
    }
    
    private void list(String[] userInputArr) throws UnknownHostException, IOException {
        if (userInputArr.length != 1) {
            System.out.println(ErrorMessage.INVALID_COMMAND.getErrorMessage());
            return;
        }
        Socket clientSocket = new Socket(NetworkConstant.TRACKER_HOSTNAME, NetworkConstant.TRACKER_LISTENING_PORT);
        
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        out.println(InterfaceCommand.LIST.getCommandCode());
        out.flush();
        
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        System.out.println(in.readLine());
        clientSocket.close();
    }
    
    private void search(String[] userInputArr) throws Exception {
        if (userInputArr.length != 2) {
            System.out.println(ErrorMessage.INVALID_COMMAND.getErrorMessage());
            return;
        }
        
        String filePath = userInputArr[1];
        
        Socket clientSocket = new Socket(NetworkConstant.TRACKER_HOSTNAME, NetworkConstant.TRACKER_LISTENING_PORT);
        
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        out.println(InterfaceCommand.SEARCH.getCommandCode() + Constant.WHITESPACE + filePath);
        out.flush();
        
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        System.out.println(in.readLine());
        clientSocket.close();
    }
    
    private void download(String[] userInputArr) throws Exception {
    	if (userInputArr.length != 2) {
            System.out.println(ErrorMessage.INVALID_COMMAND.getErrorMessage());
            return;
        }
    	
    	String fileName = userInputArr[1];
    	
    	Socket clientSocket = new Socket(NetworkConstant.TRACKER_HOSTNAME, NetworkConstant.TRACKER_LISTENING_PORT);
        
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        out.println(InterfaceCommand.DOWNLOAD.getCommandCode() + Constant.WHITESPACE + fileName);
        out.flush();
        
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        String result = in.readLine();
        clientSocket.close();
        
        if(result.equals("File Requested does not Exists")){
        	System.out.println("File Requested does not Exists");
        	return;
        }
        
        String[] peersWithData = result.split("\n");
        
        ArrayList<Record> peers = new ArrayList<Record>();
        for(int i=0;i<peersWithData.length;i++){
        	System.out.println("I received: " + peersWithData[i]);
        	String[] data = peersWithData[i].split(",");
        	if(data.length != 2)
        		System.out.println(ErrorMessage.INVALID_NUMBEROFARGUMENTS.getErrorMessage());
        	peers.add(new Record(data[0], data[1]));
        }
        
        downloadFromEachPeer(peers, fileName);
    }
    
    private void downloadFromEachPeer(ArrayList<Record> peers, String fileName){
    	System.out.println("Connecting to each P2P Server");

        for(int i=0;i<peers.size();i++){
        	//Starts new instance of server
    		try {
    			Socket socket = new Socket(NetworkConstant.SERVER_HOSTNAME, NetworkConstant.SERVER_LISTENING_PORT);
    	        
    	        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
    	        out.println(fileName + "," + peers.get(i).getChunkNo());
    	        out.flush();
    	        
    	        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    	        String result = in.readLine();
    	        System.out.println(result);
    	        socket.close();
    		} catch(IOException ioe) {
    			System.out.println("Unable to create Server Socket at Peer Client");
    			System.exit(1);
    		}
        }
    }
    
    private void inform(String[] userInputArr) throws UnknownHostException, IOException {
        if (userInputArr.length != 3) {
            System.out.println(ErrorMessage.INVALID_COMMAND.getErrorMessage());
            return;
        }
        
        String fileName = userInputArr[1].trim();
        String chunkNumber = userInputArr[2].trim();
        
        String sendData = InterfaceCommand.INFORM.getCommandCode() + Constant.WHITESPACE + fileName + Constant.WHITESPACE + chunkNumber;
        
        Socket clientSocket = new Socket(NetworkConstant.TRACKER_HOSTNAME, NetworkConstant.TRACKER_LISTENING_PORT);
        
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        out.println(sendData);
        out.flush();
        
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        System.out.println(in.readLine());
        clientSocket.close();
    }
    
    private void quit(String[] userInputArr) throws UnknownHostException, IOException {
        if (userInputArr.length != 1) {
            System.out.println(ErrorMessage.INVALID_COMMAND.getErrorMessage());
            return;
        }
        
    	//Inform server that it is exiting
    	Socket clientSocket = new Socket(NetworkConstant.TRACKER_HOSTNAME, NetworkConstant.TRACKER_LISTENING_PORT);
 
		PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
		out.println(InterfaceCommand.QUIT.getCommandCode());
		out.flush();
		 
		clientSocket.close();
         
		//TODO: close server sockets
        System.out.println("Goodbye!");
    }
        
    public void run() { 
        boolean proceed = true;
        
        while(proceed) {
            displayMenu();
            proceed = execute();
        }
    }

}
