package main.peer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
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
import java.security.SecureRandom;

import main.tracker.Record;
import main.utilities.commands.InterfaceCommand;
import main.utilities.commons.CheckAccuracy;
import main.utilities.constants.NetworkConstant;
import main.utilities.feedbacks.ErrorMessage;
import main.utilities.constants.Constant;

public class Client extends Thread {

    Socket clientSocket = Peer.peerSocket;
    
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
        
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        out.println(InterfaceCommand.SEARCH.getCommandCode() + Constant.WHITESPACE + filePath);
        out.flush();
        
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        System.out.println(in.readLine());
    }
    
    private void download(String[] userInputArr) throws Exception {
    	if (userInputArr.length != 2) {
            System.out.println(ErrorMessage.INVALID_COMMAND);
            return;
        }
    	
    	String fileName = userInputArr[1];
        
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

        if(peersWithData.get(0).equals("File Requested does not Exists") ||
        		peersWithData.get(0).equals("Chunk of File Name Specified is invalid") ||
                peersWithData.get(0).equals("Invalid Arguments")){
        	System.out.println(peersWithData.get(0));
        	return;
        }
        
	    // chunkList is an arraylist of arraylist
	    // Outer-arrayList stores N number of arraylists, where N is the number of chunks
	    // for a particular text file
	     
	    // Inner-arrayList stores the peer IP's that are holding the specific chunk
        
        int numChunks = Integer.parseInt(peersWithData.get(peersWithData.size() - 1));
        peersWithData.remove(peersWithData.size() - 1);
        ArrayList< ArrayList<String> > chunkList = new ArrayList< ArrayList<String> >();
        chunkList = processPeersWithData(peersWithData, numChunks);
        System.out.println("chunklist.size(): " + chunkList.size());
        
//        For debugging purposes
//        for (int i = 1; i <= numChunks; i++) {
//        	for (int j = 0; j < chunkList.get(i).size(); j++) {
//        		System.out.println("Client " + chunkList.get(i).get(j) + " has chunk number " + i);
//        	}
//        }
        
        if (downloadFromEachPeer(chunkList, fileName, numChunks)){
        	System.out.println("Downloading of " + fileName + " was successful");
        	String[] arguments = {"4", fileName};
        	inform(arguments);
        }
    }
    
    private boolean downloadFromEachPeer(ArrayList< ArrayList<String> > chunkPeerList, String fileName, int numChunks) throws IOException{
    	System.out.println("Connecting to P2P Server");
    	Socket socket;
//    	String filePath = Constant.FILE_DIR + "receive.txt";
//    	File yourFile = new File("/Users/brehmerchan/Desktop/P2p/src/main/files/receive.txt");
    	File yourFile = new File(fileName);
    	if (!yourFile.exists()) {
    		yourFile.createNewFile();
		}
    	FileOutputStream fos = new FileOutputStream(yourFile);
    	BufferedOutputStream bos = new BufferedOutputStream(fos);
    	
    	for (int i = 1; i <= numChunks; i++) {
    		try {
//    			InetAddress serverIP = null;
//    			serverIP = InetAddress.getByName(getIPToConnect(chunkPeerList.get(i)).replaceAll("/", ""));
    			
    			//returns a random IP and Port from list
    			socket = new Socket(InetAddress.getByName(NetworkConstant.TRACKER_HOSTNAME), NetworkConstant.TRACKER_LISTENING_PORT);
    			String serverIPAndPort = getIPToConnect(chunkPeerList.get(i));
    			
    			// Send fileName and chunkNum to download
    			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
    	        out.println(Constant.DOWNLOAD_FROM_PEER_COMMAND + Constant.COMMA
    	        			+ serverIPAndPort + Constant.COMMA
    	        			+ fileName + Constant.COMMA 
    	        			+ i);
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
    			return false;
    		} 
    	}
    	fos.close();
    	bos.close();
    	return true;
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

            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out.println(sendData);
            out.flush();
            
            System.out.println(in.readLine());
        }
    }
    
    private void quit(String[] userInputArr) throws UnknownHostException, IOException {
        if (userInputArr.length != 1) {
            System.out.println(ErrorMessage.INVALID_NUMBEROFARGUMENTS + "Please check the number of arguments required.");
            return;
        }
        
    	//Inform server that it is exiting
		PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
		out.println(InterfaceCommand.QUIT.getCommandCode());
		out.flush();
		 
		clientSocket.close();
         
		//TODO: close server sockets
        System.out.println("Goodbye!");
        System.exit(1);
    }
    
    private ArrayList< ArrayList<String> > processPeersWithData(ArrayList<String> peersWithData, int numChunks) {
    	ArrayList< ArrayList<String> > processedList = new ArrayList< ArrayList<String> >();
    	processedList.add(0, null);
    	for (int i = 1; i <= numChunks; i++) {
    		processedList.add(i, new ArrayList<String>());
    	}
    	
    	for (String singlePeerData: peersWithData) {
    		String[] peerDataArr = singlePeerData.split(",");
    		// peerDataArr[0]: ipNumber of peer's server
    		// peerDataArr[1]: portNumber of peer's server
    		// peerDataArr[2]: chunk number
    		int currChunkNumber = Integer.parseInt(peerDataArr[2]);
    		ArrayList<String> tempList = processedList.get(currChunkNumber);
//    		System.out.println("processedList.size(): " + processedList.size());
//    		System.out.println("currChunkNumber: " + currChunkNumber);
//    		System.out.println("tempList: " + tempList);
//    		System.out.println("peerDataArr[0]: " + peerDataArr[0]);
    		String peerServerSocket = peerDataArr[0] + "," + peerDataArr[1];
    		tempList.add(peerServerSocket);
    		processedList.set(currChunkNumber, tempList);
    	}
    	
    	return processedList;
    }
    
    private String getIPToConnect(ArrayList<String> ipList) {
		Random currRan = new SecureRandom();
		//E.g. ipList is size 3, random peer is chosen from 0 to 2 index
		int peerChosen = currRan.nextInt(ipList.size());
		//Return the ip address of the chosen peer
		return ipList.get(peerChosen);
	}
    
    public void run() { 
        boolean proceed = true;
        
        while(proceed) {
            displayMenu();
            proceed = execute();
        }
    }

}
