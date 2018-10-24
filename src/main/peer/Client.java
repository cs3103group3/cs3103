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

public class Client extends Thread {        
    private static void displayMenu() {
        System.out.println( "===============================================\n" +
                            "Welcome to CS3103 P2P Client\n" +
                            "Choose From the list of actions\n" +
                            "1. " + InterfaceCommand.LIST.getCommandText() + "\n" +
                            "2. " + InterfaceCommand.CHANGE_DIRECTORY.getCommandText() + "\n" +
                            "3. " + InterfaceCommand.SEARCH.getCommandText() + "\n" +
                            "4. " + InterfaceCommand.DOWNLOAD.getCommandText() + "\n" +
                            "5. " + InterfaceCommand.INFORM.getCommandText() + "\n" +
                            "6. " + InterfaceCommand.QUIT.getCommandText() + "\n" +
                            "===============================================\n" + 
                            "Enter your option: ");
    }

    private boolean execute() {
        Scanner sc = new Scanner(System.in);
        String userInput = sc.nextLine().trim();
        String[] userInputArr = userInput.split(" ");
        String userSelectedOption = userInputArr[0];
        
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
                    list();
                    return true;
                case SEARCH:
                    search(userInputArr);
                    return true;
                case DOWNLOAD:
                    download(userInputArr);
                    return true;
                case INFORM:
                    inform();
                    return true;
                case QUIT:
                    quit();
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
    
    private void list() throws UnknownHostException, IOException {
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
        out.println(InterfaceCommand.SEARCH.getCommandCode() + " " +filePath);
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
        out.println(InterfaceCommand.DOWNLOAD.getCommandCode() + " " +fileName);
        out.flush();
        
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        String result = in.readLine();
        clientSocket.close();
        
        if(result.equals("File Requested does not Exists")){
        	System.out.println("File Requested does not Exists");
        	return;
        }
        
        String[] peersWithData = in.readLine().split("\n");
        
        ArrayList<Record> peers = new ArrayList<Record>();
        for(int i=0;i<peersWithData.length;i++){
        	System.out.println("I received: " + peersWithData[i]);
        	String[] data = peersWithData[i].split(",");
        	if(data.length != 2)
        		System.out.println(ErrorMessage.INVALID_NUMBEROFARGUMENTS.getErrorMessage());
        	peers.add(new Record(data[0], data[1]));
        }
        
        downloadFromEachPeer(peers);
    }
    
    private void downloadFromEachPeer(ArrayList<Record> peers){
    	System.out.println("Connecting to each P2P Server");

        for(int i=0;i<peers.size();i++){
        	//Starts new instance of server
    		try {
    			Socket peerSocket = new Socket(NetworkConstant.CLIENT_HOSTNAME, NetworkConstant.CLIENT_LISTENING_PORT);
    			
    		} catch(IOException ioe) {
    			System.out.println("Unable to create Server Socket");
    			System.exit(1);
    		}
        }
    }
    
    private void inform() {
        
    }
    
    private void quit() {
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
