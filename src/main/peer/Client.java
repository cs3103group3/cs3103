package main.peer;

import java.util.ArrayList;
import java.util.Scanner;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.io.IOException;

import main.tracker.Record;
import main.utilities.commands.InterfaceCommand;
import main.utilities.commons.CheckAccuracy;
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
                    //For testing
//                	connectToServer();
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
        String str=in.readLine();
        while(!str.equals(Constant.END_OF_STREAM)) {
            System.out.println(str);
            str=in.readLine();
        }

        in.close();
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
        String results = in.readLine();
        ArrayList<String> peersWithData = new ArrayList<String>();
        while(!results.equals(Constant.END_OF_STREAM)) {
            peersWithData.add(results);
            results=in.readLine();
        }
        
        clientSocket.close();
        
        if(peersWithData.get(0).equals("File Requested does not Exists")){
        	System.out.println("File Requested does not Exists");
        	return;
        }
        
        ArrayList<Record> peers = new ArrayList<Record>();
        for(int i=0;i<peersWithData.size();i++){
        	System.out.println("I received: " + peersWithData.get(i));
        	String[] data = peersWithData.get(i).split(",");
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
    	        System.out.println(peers.get(i).getipAdd() + Constant.WHITESPACE + result);
    	        socket.close();
    		} catch(IOException ioe) {
    			System.out.println("Unable to create Server Socket at Peer Client");
    			System.exit(1);
    		}
        }
        
        
//        int fileSize = (int) file.length();
//        byte[] currentChunk;
//        int currentChunkNum = 0;
//        int readLength = Constant.CHUNK_SIZE;
//        int read = 0;
//        
//        FileInputStream inputStream = new FileInputStream(file);
//        while (fileSize > 0) {
//            if (fileSize <= Constant.CHUNK_SIZE) {
//                readLength = fileSize;
//            }
//            
//            currentChunk = new byte[readLength];
//            read = inputStream.read(currentChunk, 0, readLength);
//            fileSize -= read;
//            String newFileName = fileName + Constant.CHUNK_EXT + Integer.toString(currentChunkNum);
//            currentChunkNum++;
//            
//            FileOutputStream filePart = new FileOutputStream(new File(newFileName));
//            filePart.write(currentChunk);
//            filePart.flush();
//            filePart.close();
//        }
        
    }
    
    private void inform(String[] userInputArr) throws UnknownHostException, IOException {
        if (userInputArr.length != 1) {
            System.out.println(ErrorMessage.INVALID_COMMAND.getErrorMessage());
            return;
        }
        
        System.out.println("Please enter your filename");
        Scanner sc = new Scanner(System.in);
        String fileName = sc.nextLine().trim();
        
        File file = new File(fileName);
        if (!file.exists()) {
            System.out.println(ErrorMessage.FILE_NOT_FOUND + Constant.WHITESPACE + fileName);
            return;
        }
        
        
        
//        Socket clientSocket = new Socket(NetworkConstant.TRACKER_HOSTNAME, NetworkConstant.TRACKER_LISTENING_PORT);
//        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
//        
//        String sendData = InterfaceCommand.INFORM.getCommandCode() + Constant.WHITESPACE + fileName + Constant.WHITESPACE + chunkNumber;
//        out.println(sendData);
//        out.flush();
//        
//        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
//        System.out.println(in.readLine());
//        clientSocket.close();
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
        
    private void connectToServer() throws Exception {
		Socket clientSocket = new Socket(NetworkConstant.SERVER_HOSTNAME, NetworkConstant.SERVER_LISTENING_PORT);
        
		String clientName = "Client A";
		
		//For testing connect
//		String clientName = "Client B";
//		String clientName = "Client C";
		
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        out.println("Connecting from " + clientName);
        out.flush();
        
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        System.out.println(in.readLine());
        clientSocket.close();
    }
    
    public void run() { 
        boolean proceed = true;
        
        while(proceed) {
            displayMenu();
            proceed = execute();
        }
    }

}
