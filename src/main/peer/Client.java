package main.peer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.BufferedOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.net.*;
import java.security.SecureRandom;

import main.heartbeat.HeartBeatSender;
import main.utilities.commands.InterfaceCommand;
import main.utilities.commons.CheckAccuracy;
import main.utilities.constants.NetworkConstant;
import main.utilities.feedbacks.ErrorMessage;
import main.utilities.feedbacks.SuccessMessage;
import main.utilities.constants.Constant;

public class Client extends Thread {

	Socket clientSocket;

	PrintWriter out;
	BufferedReader in;

	String myIP;
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
	
	private void list() throws UnknownHostException, IOException {
	    list(new String[] {"1"});
	}

	private void list(String[] userInputArr) throws UnknownHostException, IOException {
		if (userInputArr.length != 1) {
			System.out.println(ErrorMessage.INVALID_NUMBEROFARGUMENTS + "Please check the number of arguments required.");
			return;
		}
		try {
			out.println(InterfaceCommand.LIST.getCommandCode());
			String str=in.readLine();

			while(!str.equals(Constant.END_OF_STREAM)) {
				System.out.println(str);
				str=in.readLine();
			}
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

		out.println(InterfaceCommand.SEARCH.getCommandCode() + Constant.WHITESPACE + filePath);

		System.out.println(in.readLine());
	}

	private void download(String[] userInputArr) throws Exception {
		if (userInputArr.length != 2) {
			System.out.println(ErrorMessage.INVALID_COMMAND);
			return;
		}

		String fileName = userInputArr[1];
		out.println(InterfaceCommand.DOWNLOAD.getCommandCode() + Constant.WHITESPACE + fileName);

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

		String [] numChunkAndMyIP = peersWithData.get(peersWithData.size() - 1).split(Constant.COMMA);
		
		int numChunks = Integer.parseInt(numChunkAndMyIP[0]);
		myIP = numChunkAndMyIP[1];
//		System.out.println("numChunks is " + numChunks);
//		System.out.println("myIP is " + myIP);

		peersWithData.remove(peersWithData.size() - 1);
		ArrayList< ArrayList<String> > chunkList = new ArrayList< ArrayList<String> >();
		chunkList = processPeersWithData(peersWithData, numChunks);
//		System.out.println("chunklist.size(): " + chunkList.size());

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
		//    	String filePath = Constant.FILE_DIR + "receive.txt";
		//    	File yourFile = new File("/Users/brehmerchan/Desktop/P2p/src/main/files/receive.txt");
		File yourFile = new File(fileName);
		if (!yourFile.exists()) {
			yourFile.createNewFile();
		}
		FileOutputStream fos = new FileOutputStream(yourFile);
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		Socket socket = new Socket(InetAddress.getByName(NetworkConstant.TRACKER_HOSTNAME), NetworkConstant.TRACKER_LISTENING_PORT);
//		System.out.println("My public ip is " + myIP);
//		System.out.println("My port is : " + Peer.listeningPort);
		System.out.println("My public address is: [" + myIP + ": " + Peer.listeningPort + "]");
		PrintWriter outSocket = new PrintWriter(socket.getOutputStream(), true);
		for (int i = 1; i <= numChunks; i++) {
			try {
				//    			InetAddress serverIP = null;
				//    			serverIP = InetAddress.getByName(getIPToConnect(chunkPeerList.get(i)).replaceAll("/", ""));
				//returns a random IP and Port from list
				String serverIPAndPort = getIPToConnect(chunkPeerList.get(i), myIP);
//				System.out.println("Peer's serverIP And Port is :" + serverIPAndPort);
				String[] serverIPAndPortArr = serverIPAndPort.split(Constant.COMMA);
				System.out.println("Downloading chunk from [" + serverIPAndPortArr[0] + ": " + serverIPAndPortArr[1] +  "]");

				// Send fileName and chunkNum to download

				if(i != numChunks) {
					outSocket.println(InterfaceCommand.FORWARD.getCommandCode() + Constant.WHITESPACE
							+ serverIPAndPort + Constant.COMMA
							+ fileName + Constant.COMMA 
							+ i);
					outSocket.flush();
				} else {
					outSocket.println(InterfaceCommand.FORWARD.getCommandCode() + Constant.WHITESPACE
							+ serverIPAndPort + Constant.COMMA
							+ fileName + Constant.COMMA 
							+ i + Constant.COMMA + Constant.LAST_CHUNK);
					outSocket.flush();
				}

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
		if (fos != null) fos.close();
		if (bos != null) bos.close();
		if (socket != null) socket.close();
		return true;
	}

	private void inform(String[] userInputArr) throws UnknownHostException, IOException {
		System.out.println("Informing...");

		String confirmationString = SuccessMessage.NEW_CHUNK_ADDED_TO_TRACKER.toString();
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
		if (fileSize == 0) {
            System.out.println(ErrorMessage.FILE_SIZE_ZERO + Constant.WHITESPACE + fileName);
            return;
        }
		
		int totalNumChunk = (int) Math.ceil(fileSize*1.0/ Constant.CHUNK_SIZE);
		String listeningPort = Integer.toString(Peer.listeningPort);
		for (int chunkNum=1; chunkNum<=totalNumChunk; chunkNum++) {
			String payload = totalNumChunk + Constant.COMMA + chunkNum + Constant.COMMA + fileName + Constant.COMMA + listeningPort;
			long checksum = CheckAccuracy.calculateChecksum(payload);
			String data = checksum + Constant.COMMA + payload;
			String sendData = InterfaceCommand.INFORM.getCommandCode() + Constant.WHITESPACE + data;
//			System.out.println(sendData);
			out.println(sendData);

			String temp = in.readLine();
			//            ArrayList<String> results = new ArrayList<String>();
			while(!temp.equals(Constant.END_OF_STREAM)) {
				//                results.add(temp);
				if (temp.equals(ErrorMessage.INCONSISTENT_CHECKSUM.getErrorMessage())) {
					confirmationString = ErrorMessage.INCONSISTENT_CHECKSUM.getErrorMessage();
				}
				temp=in.readLine();
			}
		}

		System.out.println(confirmationString);
		list();
	}

	private void quit(String[] userInputArr) throws UnknownHostException, IOException {
		if (userInputArr.length != 1) {
			System.out.println(ErrorMessage.INVALID_NUMBEROFARGUMENTS + "Please check the number of arguments required.");
			return;
		}

		//Inform server that it is exiting
		out.println(InterfaceCommand.QUIT.getCommandCode() + Constant.WHITESPACE + Peer.listeningPort);
		//out.flush();
		//in.close();

		System.out.println("Goodbye!");

		clientSocket.close();

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
//			System.out.println("Adding peerSeverSocket in processPeersWithData" + peerServerSocket);
			tempList.add(peerServerSocket);
			processedList.set(currChunkNumber, tempList);
		}

		return processedList;
	}

	private String getIPToConnect(ArrayList<String> ipList, String myIP) {
		String myListeningPort = Integer.toString(Peer.listeningPort);
		String myIPAndPort = myIP + Constant.COMMA + myListeningPort;
		Random currRan = new SecureRandom();
		//E.g. ipList is size 3, random peer is chosen from 0 to 2 index
//		System.out.println("iplist size is : " + ipList);
		int peerChosen = currRan.nextInt(ipList.size());
		//Return the ip address of the chosen peer
		
		while(myIPAndPort.equals(ipList.get(peerChosen))) {
			peerChosen = currRan.nextInt(ipList.size());
		}
		return ipList.get(peerChosen);
	}

	public void run() { 
		boolean proceed = true;

		try {
			clientSocket = new Socket(InetAddress.getByName(NetworkConstant.TRACKER_HOSTNAME), NetworkConstant.TRACKER_LISTENING_PORT);

			out = new PrintWriter(clientSocket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		} catch (IOException e) {
			System.out.println("Unable to create client socket");
			e.printStackTrace();
		}
		while(proceed) {
			displayMenu();
			proceed = execute();
		}
	}

}
