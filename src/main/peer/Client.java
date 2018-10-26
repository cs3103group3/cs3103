package main.peer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;

import main.tracker.Record;
import main.utilities.commands.InterfaceCommand;
import main.utilities.commons.CheckAccuracy;
import main.utilities.constants.NetworkConstant;
import main.utilities.feedbacks.ErrorMessage;
import main.utilities.constants.Constant;

public class Client extends Thread {    
    private static void displayMenu() {
        System.out.println( "===============================================\n" +
                            "Welcome to CS3103 P2P Client\n" +
                            "Choose From the list of actions\n" +
                            "<Command><space><arguments>\n" +
                            "Arguments are delimited by whitespaces\n" +
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
                    System.out.println(ErrorMessage.INVALID_COMMAND + "Command entered: " + userInput);
                    return true;
            }
        } catch (UnknownHostException e) {
            System.out.println(ErrorMessage.UNKNOWN_HOST);
            return true;
        } catch (Exception e) {
            System.out.println(ErrorMessage.UNKNOWN_ERROR);
            e.printStackTrace();
            return true;  // So as not to quit the program, proceed as normal
        }        
    }
    
    private void list(String[] userInputArr) throws UnknownHostException, IOException {
        if (userInputArr.length != 1) {
            System.out.println(ErrorMessage.INVALID_NUMBEROFARGUMENTS + "Please check the number of arguments required.");
            return;
        }
        try {
        	InetAddress serverIP = InetAddress.getByName(NetworkConstant.TRACKER_HOSTNAME);
            Socket clientSocket = new Socket(serverIP, NetworkConstant.TRACKER_LISTENING_PORT);
            
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
        } catch(Exception e) {
        	System.out.println("Exception while listing from server: " + e);
        	e.printStackTrace();
        }
    }
    
    private void search(String[] userInputArr) throws Exception {
        if (userInputArr.length != 2) {
            System.out.println(ErrorMessage.INVALID_COMMAND);
            return;
        }
        
        String filePath = userInputArr[1];
        
        InetAddress serverIP = InetAddress.getByName(NetworkConstant.TRACKER_HOSTNAME);
        Socket clientSocket = new Socket(serverIP, NetworkConstant.TRACKER_LISTENING_PORT);
        
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        out.println(InterfaceCommand.SEARCH.getCommandCode() + Constant.WHITESPACE + filePath);
        out.flush();
        
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        System.out.println(in.readLine());
        clientSocket.close();
    }
    
    private void download(String[] userInputArr) throws Exception {
    	if (userInputArr.length != 2) {
            System.out.println(ErrorMessage.INVALID_COMMAND);
            return;
        }
    	
    	String fileName = userInputArr[1];
    	
    	InetAddress serverIP = InetAddress.getByName(NetworkConstant.TRACKER_HOSTNAME);
        Socket clientSocket = new Socket(serverIP, NetworkConstant.TRACKER_LISTENING_PORT);
        
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        out.println(InterfaceCommand.DOWNLOAD.getCommandCode() + Constant.WHITESPACE + fileName);
        out.flush();
        
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        String results = in.readLine();
        ArrayList<String> peersWithData = new ArrayList<String>();
        // Format: ipAdd,chunkNumber
        while(!results.equals(Constant.END_OF_STREAM)) {
            peersWithData.add(results);
            results=in.readLine();
        }
        
        clientSocket.close();
        
        if(peersWithData.get(0).equals("File Requested does not Exists")){
        	System.out.println("File Requested does not Exists");
        	return;
        }
        
        ArrayList< ArrayList<String> > chunkList = new ArrayList< ArrayList<String> >();
        chunkList = processPeersWithData(peersWithData);
        System.out.println("chunklist.size(): " + chunkList.size());
        for (int i = 1; i <= Constant.NUM_CHUNKS; i++) {
        	for (int j = 0; j < chunkList.get(i).size(); j++) {
        		System.out.println("Client " + chunkList.get(i).get(j) + " has chunk number " + i);
        	}
        }
        downloadFromEachPeer(chunkList, fileName);
    }
    
    private void downloadFromEachPeer(ArrayList< ArrayList<String> > chunkPeerList, String fileName) throws IOException{
    	System.out.println("Connecting to P2P Server");
    	Socket socket;
//    	String filePath = Constant.FILE_DIR + "receive.txt";
//    	File yourFile = new File("/Users/brehmerchan/Desktop/P2p/src/main/files/receive.txt");
    	File yourFile = new File("receive.txt");
    	if (!yourFile.exists()) {
    		yourFile.createNewFile();
		}
    	FileOutputStream fos = new FileOutputStream(yourFile);
    	BufferedOutputStream bos = new BufferedOutputStream(fos);
    	
    	for (int i = 1; i <= Constant.NUM_CHUNKS; i++) {
    		try {
    			InetAddress serverIP = null;
    			// TODO: randomly select one peer from peerlist to seed from
    			if (i%2 == 1) {
    				serverIP = InetAddress.getByName(chunkPeerList.get(i).get(0).replaceAll("/", ""));
    				System.out.println("serverIP from i%2 == 1: " + serverIP);
    			}
    			else if (chunkPeerList.get(i).size() == 2) {
    				serverIP = InetAddress.getByName(chunkPeerList.get(i).get(1).replaceAll("/", ""));
    				System.out.println("serverIP from i%2 == 0: " + serverIP);
    			}
    			else {
    				serverIP = InetAddress.getByName(chunkPeerList.get(i).get(0).replaceAll("/", ""));
    				System.out.println("serverIP from: " + serverIP);
    			}
    			
    			socket = new Socket(serverIP, NetworkConstant.SERVER_LISTENING_PORT);
    			
    			// Send fileName and chunkNum to download
    			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
    	        out.println(fileName + "," + i);
    	        out.flush();
    			
    	        byte[] fileDataBytes = new byte[Constant.CHUNK_SIZE];
    	        InputStream is = socket.getInputStream();
	    	    int bytesRead = is.read(fileDataBytes, 0, fileDataBytes.length);
	    	    byte[] newFileDataBytes = Arrays.copyOf(fileDataBytes, bytesRead);
	    	    
				bos.write(newFileDataBytes);
				bos.flush();
    		} catch (IOException e) {
    			System.out.println("Exception while downloading from peer: " + e);
    			e.printStackTrace();
    		} 
    	}

    	fos.close();
    	bos.close();
    	
//        for(int i=0;i<chunkList.size();i++){
//        	//Starts new instance of server
//    		try {
//    			String data = chunkList.get(i).trim();
//    			String[] seperatedData = data.split(",");
//            	if(seperatedData.length != 2) {
//            		System.out.println(ErrorMessage.INVALID_NUMBEROFARGUMENTS);
//            		return;
//            	}
//            	
//            	//Change to seperatedData[0]: IP Address
//    			Socket socket = new Socket(NetworkConstant.SERVER_HOSTNAME, NetworkConstant.SERVER_LISTENING_PORT);
//    	        
//    	        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
//    	        out.println(fileName + "," + seperatedData[1]);
//    	        out.flush();
//    	        
//    	        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//    	        String result = in.readLine();
//    	        System.out.println(seperatedData[0] + Constant.WHITESPACE + result);
//    	        socket.close();
//    		} catch(IOException ioe) {
//    			System.out.println("Unable to create Server Socket at Peer Client");
//    			System.exit(1);
//    		}
//        }
        
        
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
        if (userInputArr.length != 2) {
            System.out.println(ErrorMessage.INVALID_COMMAND);
            return;
        }

        String fileName = userInputArr[1];
        
        File file = new File(fileName);
        if (!file.exists()) {
            System.out.println(ErrorMessage.FILE_NOT_FOUND + Constant.WHITESPACE + fileName);
            return;
        }
        
        long fileSize =  file.length();
        int totalNumChunk = (int) Math.ceil(fileSize*1.0/ Constant.CHUNK_SIZE);
                
        for (int chunkNum=1; chunkNum<=totalNumChunk; chunkNum++) {
            String payload = totalNumChunk + Constant.COMMA + chunkNum + Constant.COMMA + fileName;
            long checksum = CheckAccuracy.calculateChecksum(payload);
            String data = checksum + Constant.COMMA + payload;
            
            String sendData = InterfaceCommand.INFORM.getCommandCode() + Constant.WHITESPACE + data;
            InetAddress serverIP = InetAddress.getByName(NetworkConstant.TRACKER_HOSTNAME);
            Socket clientSocket = new Socket(serverIP, NetworkConstant.TRACKER_LISTENING_PORT);

            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out.println(sendData);
            out.flush();
            
            System.out.println(in.readLine()); 
            clientSocket.close();
        }
    }
    
    private void quit(String[] userInputArr) throws UnknownHostException, IOException {
        if (userInputArr.length != 1) {
            System.out.println(ErrorMessage.INVALID_NUMBEROFARGUMENTS + "Please check the number of arguments required.");
            return;
        }
        
    	//Inform server that it is exiting
        InetAddress serverIP = InetAddress.getByName(NetworkConstant.TRACKER_HOSTNAME);
        Socket clientSocket = new Socket(serverIP, NetworkConstant.TRACKER_LISTENING_PORT);
 
		PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
		out.println(InterfaceCommand.QUIT.getCommandCode());
		out.flush();
		 
		clientSocket.close();
         
		//TODO: close server sockets
        System.out.println("Goodbye!");
    }
    
    private ArrayList< ArrayList<String> > processPeersWithData(ArrayList<String> peersWithData) {
    	ArrayList< ArrayList<String> > processedList = new ArrayList< ArrayList<String> >();
    	processedList.add(0, null);
    	for (int i = 1; i <= Constant.NUM_CHUNKS; i++) {
    		processedList.add(i, new ArrayList<String>());
    	}
    	
    	for (String singlePeerData: peersWithData) {
    		String[] peerDataArr = singlePeerData.split(",");
    		int currChunkNumber = Integer.parseInt(peerDataArr[1]);
    		ArrayList<String> tempList = processedList.get(currChunkNumber);
    		tempList.add(peerDataArr[0]);
    		processedList.set(currChunkNumber, tempList);
    	}
    	
    	return processedList;
    }
        
    private void requestFileFromServer() {
    	
    }
    
//    private void connectToServer() throws Exception {
//		Socket clientSocket = new Socket(NetworkConstant.SERVER_HOSTNAME, NetworkConstant.SERVER_LISTENING_PORT);
//        
//		String clientName = "Client A";
//		
//		//For testing connect
////		String clientName = "Client B";
////		String clientName = "Client C";
//		
//        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
//        out.println("Connecting from " + clientName);
//        out.flush();
//        
//        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
//        System.out.println(in.readLine());
//        clientSocket.close();
//    }
    
    public void run() { 
        boolean proceed = true;
        
        while(proceed) {
            displayMenu();
            proceed = execute();
        }
    }

}
