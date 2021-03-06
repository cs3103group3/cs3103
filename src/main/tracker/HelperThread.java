package main.tracker;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Set;
import java.util.Map.Entry;

import main.utilities.commands.InterfaceCommand;
import main.utilities.commands.OfflineInterfaceCommand;
import main.utilities.commons.CheckAccuracy;
import main.utilities.constants.Constant;
import main.utilities.feedbacks.*;

public class HelperThread extends Thread{
	//Client Socket
	Socket clientSocket;
	//private List<String>
	BufferedReader in = null;
	PrintWriter reply = null;

	//Hash table of fileName to its records of users
	//E.g. fileOne as key to ArrayList of fileOne chunks
	private Hashtable<String, ArrayList<Record>> recordList = Tracker.recordTable;

	private boolean threadRunning = true;

	private static final String INVALID_CHUNK = "-1";
	private static boolean FOUND_IP = true;
	public HelperThread() {

	}
	public HelperThread(Socket client) {
		this.clientSocket = client;
	}

	@Override
	public void run() {

		//		Creating dummy arraylist
		//		Uncomment to create
		//		ArrayList<Record> dummyList=new ArrayList<Record>();
		//		dummyList.add(new Record("127.0.0.1", "1", "4"));
		//		dummyList.add(new Record("10.0.2.5", "2", "4"));
		//		dummyList.add(new Record("127.0.0.1", "3", "4"));
		//		dummyList.add(new Record("10.0.2.5", "4", "4"));
		//		recordList.put("test.txt", dummyList);

		String clientInput = "";
		try {
			in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			//	reply = new PrintWriter( new OutputStreamWriter(clientSocket.getOutputStream()));
			reply = new PrintWriter(clientSocket.getOutputStream(), true);

			while(threadRunning && clientSocket.isConnected()) {
				clientInput = in.readLine().trim();
//				System.out.println("Client has entered command: " + clientInput);
				doClientCommand(clientInput, reply);
			}
		} catch (Exception e) {
//			System.out.println("IOException at Run Function in Helper Thread");
//			e.printStackTrace();
		}
	}

	/**
	 * This method executes the commands the client requested
	 */
	private void doClientCommand(String strCommand, PrintWriter currentReply) throws IOException, InterruptedException {
		InterfaceCommand command = InterfaceCommand.INVALID;
		String [] strCommandArr;
		try {
			strCommand = strCommand.trim();
			strCommandArr = strCommand.split(Constant.WHITESPACE);
			int commandCode = Integer.parseInt(strCommandArr[0]);
			command = InterfaceCommand.forCode(commandCode);
		} catch(NumberFormatException nfe) {
			currentReply.println(ErrorMessage.INVALID_COMMAND);
			return;
		}

		//Calls method that is required to execute the user's command
		switch(command) {
			case LIST:
				//perform list
				listDirectoryEntry(recordList, currentReply);
				printThreads();
				break;
			case SEARCH:
				//perform search
				searchEntry(strCommandArr, recordList, currentReply);
				break;
			case DOWNLOAD:
				//Finds peer to download the file requested
				findPeer(strCommandArr, currentReply);
				break;
			case INFORM:
				//Update the server of newly advertised chunk of file
				informServer(strCommandArr, currentReply);
				break;
			case QUIT:
				//perform exit
				exitServer(strCommandArr, currentReply);
				break;
			case FORWARD:
				forwardServer(strCommandArr, currentReply);
				break;
			case MEDIATE:
				mediate(strCommandArr, currentReply);
				break;
			case AddListeningSocket:
				addSocket(this.clientSocket, strCommandArr);
				break;
			default:
				//Error
				currentReply.println(ErrorMessage.INVALID_COMMAND);
				return;
		}
	}



	/**
	 * Lists the directory Entry of all the files
	 * 
	 * Example Input:
	 * 1
	 * Expected Output:
	 * Directory Entry
	 */
	private synchronized void listDirectoryEntry(Hashtable<String, ArrayList<Record>> currentList, PrintWriter currentReply) {
		//		ArrayList<Record> testArrList = new ArrayList<Record>();
		//		testArrList.add(new Record("192.128.122.211", "1"));
		//		currentList.put("Test.txt", testArrList);
	    
	    System.out.println("Sending directory to Client");
		if(currentList.isEmpty()) {
			currentReply.println(OfflineInterfaceCommand.EMPTY_RECORD);
			currentReply.println(Constant.END_OF_STREAM);
			currentReply.flush();
		} else {
			String result = "";
			Set<Entry<String, ArrayList<Record>>> entrySet = currentList.entrySet();
			//Prints out currentList
			currentReply.println("The list of files on Tracker are:");
			for(Entry<String, ArrayList<Record>> entry1 : entrySet) {
				result += entry1.getKey();
				result += Constant.NEWLINE;
			}
			result += Constant.END_OF_STREAM;
			currentReply.println(result);
			currentReply.flush();
		}
		System.out.println("Directory Sent!");
	}

	/**
	 * Search Entry by the following
	 * @param currentReply 
	 * @param strCommandArr:  the Array of command by client that has been split
	 * 1) FileName
	 * 
	 * Example Input One:
	 * 3 fileNameOne
	 * Expected Output:
	 * HostName/IP, FileName to be written back to the peer who requested it
	 * 
	 * Invalid Output:
	 * "Invalid fileName or Chunk Number specified"
	 */
	private synchronized void searchEntry(String[] strCommandArr, Hashtable<String, ArrayList<Record>> currentList, PrintWriter currentReply) {
		String fileRequested = strCommandArr[1];
		boolean foundFile = false;
		
		System.out.println("Searching for <" + fileRequested + ">...");
		Set<Entry<String, ArrayList<Record>>> entrySet = currentList.entrySet();
		for(Entry<String, ArrayList<Record>> entry2 : entrySet) {
			if(fileRequested.equals(entry2.getKey())) {
				foundFile = true;
				break;
			}
		}
		if(foundFile) {
			currentReply.println(SuccessMessage.FILE_FOUND);
			currentReply.flush();
			System.out.println("<" + fileRequested + "> found!");
		} else {
			currentReply.println(ErrorMessage.FILE_NOT_FOUND);
			currentReply.flush();
			System.out.println("<" + fileRequested + "> not found!");
		}
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
	 * @param currentReply 
	 */
	private synchronized void informServer(String[] strCommandArr, PrintWriter currentReply) {
		String ipBroadcasted = this.clientSocket.getInetAddress().toString().replaceAll("/", "").trim();

		//String portNumber = Integer.toString(this.clientSocket.getLocalPort());

		String[] recvData = strCommandArr[1].split(Constant.COMMA);

		long checksum = Long.parseLong(recvData[0]);
		String totalNumChunk = recvData[1];
		String chunkNum = recvData[2];
		String fileName = recvData[3];
		String portNumber = recvData[4];
//		System.out.println("ipBroadcasted=> " + ipBroadcasted + ": " + portNumber);

		String payload = totalNumChunk + Constant.COMMA + chunkNum + Constant.COMMA + fileName + Constant.COMMA + portNumber;

		if (!CheckAccuracy.isDataValid(payload, checksum)) {
			currentReply.println(ErrorMessage.INCONSISTENT_CHECKSUM.getErrorMessage());
			currentReply.println(Constant.END_OF_STREAM);
			currentReply.flush();
			return;
		}

//		System.out.println("IP_RECEIVED: " + ipBroadcasted);
		System.out.println("[" + ipBroadcasted + ": " + portNumber + "] informed " + "(" + fileName + "," + chunkNum + ")");

		boolean hasExist =	checkExistFile(fileName);

		//If file already exists, simply add the chunk to it
		if(hasExist) {
			//Obtain the arraylist to update first
			ArrayList<Record> currArrFile = Tracker.recordTable.get(fileName);
			//Add new Record
			Record addToExist = new Record(ipBroadcasted, portNumber, chunkNum, totalNumChunk);
			currArrFile.add(addToExist);

			//Replace the HashTable with updated data
			Tracker.recordTable.replace(fileName, currArrFile);

			currentReply.println(SuccessMessage.NEW_CHUNK_ADDED_TO_TRACKER);
			currentReply.println(Constant.END_OF_STREAM);
			currentReply.flush();
		} else {
			//Create a new Record
			Record newRecord = new Record(ipBroadcasted, portNumber, chunkNum, totalNumChunk);
			//Create a new ArrayList
			ArrayList<Record> newArrFile = new ArrayList<Record>();
			//Add new Record
			newArrFile.add(newRecord);
			Tracker.recordTable.put(fileName, newArrFile);
			currentReply.println(SuccessMessage.NEW_FILE_ADDED_TO_TRACKER);
			currentReply.println(Constant.END_OF_STREAM);
			currentReply.flush();
		}
	}

	/**
	 * This method checks if a file exists within the server
	 * @param fileBroadcasted
	 * @return
	 */
	private boolean checkExistFile(String fileBroadcasted) {
		Set<Entry<String, ArrayList<Record>>> entrySet = Tracker.recordTable.entrySet();
		boolean foundFile = false;
		for(Entry<String, ArrayList<Record>> entry2 : entrySet) {
			if(fileBroadcasted.equals(entry2.getKey())) {
				foundFile = true;
				break;
			}
		}
		if(foundFile) {
			return true;
		}
		return false;
	}
	/**
	 * Asks the server for location of file or chunk from a peer
	 * @param strCommandArr
	 * 
	 * Format of Input:
	 * 5 fileName <optional chunk No>
	 * 
	 * Example Input:
	 * 5 example.txt
	 * Expected Output:
	 * IP addresses are output to user
	 * Invalid Output:
	 * Unable to find specific file or chunk
	 * @param currentReply 
	 * 
	 */
	private synchronized void findPeer(String[] strCommandArr, PrintWriter currentReply) {
		String requestedFileName = strCommandArr[1];

		String finderIP = clientSocket.getInetAddress().getHostAddress();
//		System.out.println("This client is finding peer: " + clientSocket.getInetAddress().getHostAddress());
		System.out.println("["+finderIP+"] is searching for peer...");
		if(strCommandArr.length <= 1) {
			currentReply.print("Invalid Arguments");
			currentReply.flush();
		}
		boolean fileExist = checkExistFile(requestedFileName);

		//If no chunk Size is Specified
		if(strCommandArr.length == 2) {
			if(fileExist) {
				//Obtain the ArrayList of Entry associated with key of requestedFileName
				ArrayList<Record> requestedChunks = Tracker.recordTable.get(requestedFileName);

				String requestedData = "";
				for(int i =0 ; i < requestedChunks.size() ; i ++) {
					requestedData += requestedChunks.get(i).getipAdd();
					requestedData += Constant.COMMA;
					requestedData += requestedChunks.get(i).getPortNumber();
					requestedData += Constant.COMMA;
					requestedData += requestedChunks.get(i).getChunkNo();
					requestedData += "\n";
				}

//				System.out.println("Last line to send is : " + requestedChunks.get(0).getMaxChunk()
//						+ Constant.COMMA + finderIP);
				requestedData += requestedChunks.get(0).getMaxChunk()
						+ Constant.COMMA + finderIP;
				requestedData += Constant.NEWLINE;
				currentReply.println(requestedData + Constant.END_OF_STREAM);
				currentReply.flush();

			} else {
				currentReply.println("File Requested does not Exists");
				currentReply.println(Constant.END_OF_STREAM);
				currentReply.flush();
			}
		} else {
			//Chunk is Specified
			//Expected Reply is only ip address of requested chunk of file name
			if(fileExist) {
				String chunkNumber = strCommandArr[2];

				String requestedIP = findRequestedIP(requestedFileName, chunkNumber);

				if(requestedIP.equals(INVALID_CHUNK)) { 
					currentReply.println("Chunk of File Name Specified is invalid");
					currentReply.println(Constant.END_OF_STREAM);
					currentReply.flush();
				} else {
					currentReply.println(requestedIP);
					currentReply.println(Constant.END_OF_STREAM);
					currentReply.flush();
				}

			} else {
				currentReply.println("File Requested does not Exists");
				currentReply.println(Constant.END_OF_STREAM);
				currentReply.flush();
			}
		}

	}
	/**
	 * Finds the requested IP by checking with the file name and/or chunk number
	 * @param requestedFileName: requested File Name by the user
	 * @param chunkNumber: optional chunk number input
	 * @return ipAddress/es of requested fileName
	 */
	private String findRequestedIP(String requestedFileName, String chunkNumber) {
		Set<Entry<String, ArrayList<Record>>> entrySet = Tracker.recordTable.entrySet();
		for(Entry<String, ArrayList<Record>> entry2 : entrySet) {
			//File Name Matches and chunk Number Matches
			if(requestedFileName.equals(entry2.getKey())) {
				boolean foundChunk = false;
				ArrayList<Record> requestedFileArr = entry2.getValue();

				int i = 0;
				for(i=0 ; i < requestedFileArr.size(); i ++) {
					if(chunkNumber.equals(requestedFileArr.get(i).getChunkNo())) {
						foundChunk = true;
						break;
					}
				}
				if(foundChunk) {
					String requestedIP = requestedFileArr.get(i).getipAdd();
					return requestedIP;
				}

				return INVALID_CHUNK;
			}
		}
		return INVALID_CHUNK;
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
	 * @param strCommandArr : command argument user has entered
	 * @param currentReply 
	 */
	private void exitServer(String[] strCommandArr, PrintWriter currentReply) {
		threadRunning = false;
		String clientPortNo = strCommandArr[1];

		try{
			if(this.clientSocket != null){
				String ipAddress = this.clientSocket.getInetAddress().getHostAddress();
				deleteAllRecords(ipAddress, clientPortNo);
				clientSocket.close();
				System.out.println("["+ipAddress+": "+clientPortNo+"] has exited.");
				System.out.println("All related data has been deleted successfully.");
			}
		}  catch (IOException e) {
			System.out.println("IOException at Exit Server in Helper Thread");
			e.printStackTrace();
		}
	}

	/**
	 * This method checks whether the ip address exists within the central directory server
	 * @param ipAddress: ip address to check
	 * @return
	 */
	private boolean checkIPAndPortNoExists(String ipAddress, String clientPortNo) {
		Set<String> keys = Tracker.recordTable.keySet();
		for(String key : keys) {
			ArrayList<Record> currArr = Tracker.recordTable.get(key);
			for(int i = 0; i < currArr.size(); i ++) {
				if(currArr.get(i).getipAdd().equals(ipAddress) && currArr.get(i).getPortNumber().equals(clientPortNo)) {
					return FOUND_IP;
				}
			}
		}

		return false;
	}

	/**
	 * This method deletes all the relevant
	 * records associated with the ip address that is leaving
	 * @param ipAddress: ip address that is exiting
	 * 
	 */
	private void deleteAllRecords(String ipAddress, String clientPortNo) {
		Tracker.recordTable.forEach((filename,recordList) -> {
			for (int i=0; i<recordList.size(); i++ ) {
				Record record = recordList.get(i);
				Tuple peer = new Tuple(record.getipAdd(), record.getPortNumber());
				if (peer.getIpAdd().equals(ipAddress) && peer.getPortNo().equals(clientPortNo)) {
					recordList.remove(i);
					if (Tracker.recordTable.get(filename) == null) {
						Tracker.recordTable.remove(filename);
					}
					closeListeningSocket(peer);
					Tracker.ipPortToSocketTable.remove(peer);
					i--;
				}
			}
		});
		
		// Remove from ipPortTable regardless of peer has inform data or not
		Tuple peer = new Tuple(ipAddress, clientPortNo);
		Tracker.ipPortToSocketTable.remove(peer);
		
	}
	
	/**
	 * This method listens and receive information about the opposing peer
	 * then replies back with fileName + chunkNumber
	 * 
	 */
	private void forwardServer(String[] clientInputArr, PrintWriter currentReply) {
//		System.out.println("Entered ForwardServer");
		Socket opposingSocket = null;

		//
		String[] currentClientInputArr = clientInputArr[1].split(Constant.COMMA);

		//New Socket that was opened up by the client
		Socket downloaderSocket = clientSocket;
//		System.out.println("Downloader socket is : " + downloaderSocket);
		//Expects to receive data from peer A
		//String clientInput = in.readLine();

		//Process the clientInput which is of the following format
		//IPb,PortB,FileName,ChunkNumber
		Tuple opposingPeerTuple = new Tuple(currentClientInputArr[0], currentClientInputArr[1]);
//		System.out.println("Opposing peer tuple is : "  + currentClientInputArr[0] + " " + currentClientInputArr[1]);
		
//		System.out.println("Size of ipPortToSocket is :" + Tracker.ipPortToSocketTable.size());
		Set<Entry<Tuple, Socket>> entrySet = Tracker.ipPortToSocketTable.entrySet();
		for(Entry<Tuple, Socket> entry2 : entrySet) {
//			System.out.println("Current IP of tuple is : " + entry2.getKey().getIpAdd());
//			System.out.println("Current IP of tuple is : " + entry2.getKey().getPortNo());
//			System.out.println("Current Socket of tuple is " + entry2.getValue());
			if(opposingPeerTuple.getIpAdd().equals(entry2.getKey().getIpAdd())
					&& opposingPeerTuple.getPortNo().equals(entry2.getKey().getPortNo())) {
				opposingSocket = entry2.getValue();
			}
		} 
		if(opposingSocket == null) {
//			System.out.println("Unable to find opposing Socket");
		    System.out.println("Unable to find ["+opposingPeerTuple.getIpAdd()+ ": " + opposingPeerTuple.getPortNo()+"]");
			return;
		} else {
//			System.out.println("Opposing Socket is : " + opposingSocket);
		    System.out.println("Found peer: ["+opposingPeerTuple.getIpAdd()+ ": " + opposingPeerTuple.getPortNo()+"]");
		}
		
		/**
		 * 	opposingSocket = Peer's listeningSocket
		 * 	currentClientInputArr = opposingSocket.IP, opposingSocket.port, fileName, chunkNumber
		 * 	downloaderSocket = clientSocket = Peer's downloading socket
		 */
		sendOpposingPeer(opposingSocket, currentClientInputArr, downloaderSocket);
		addDataSocket(downloaderSocket);
	}

	private void addDataSocket(Socket downloaderSocket) {
		//Gets public ip, public port from downloader Socket
		String downloaderIP = downloaderSocket.getInetAddress().toString().replaceAll("/", "");
		String downloaderPublicPort = String.valueOf(downloaderSocket.getPort());
//		System.out.println("data socket to be added is of ip : " + downloaderIP);
//		System.out.println("data socket to be added is of port is : " +  downloaderPublicPort);
		Tracker.dataTransferTable.put(new Tuple(downloaderIP, downloaderPublicPort), downloaderSocket);
		
	}
	
	/**
	 * This methods adds the newly created socket into the hashtable
	 * @param downloaderSocket
	 */
	private void addSocket(Socket downloaderSocket, String[] commandArr) {
		//Gets public ip, public port from downloader Socket
		String downloaderIP = downloaderSocket.getInetAddress().toString().replaceAll("/", "");
		String downloaderPublicPort = commandArr[1];
//		System.out.println("New socket is of ip : " + downloaderIP);
//		System.out.println("New Public port is : " +  downloaderPublicPort);
		Tracker.ipPortToSocketTable.put(new Tuple(downloaderIP, downloaderPublicPort), downloaderSocket);
	}
	
	/**
	 * This methods closes the listening socket in hashtable when receive quit command
	 * @param Peer object containing IP and Port
	 */
	private void closeListeningSocket(Tuple peer) {
		Socket socketToClose = Tracker.ipPortToSocketTable.get(peer);
		if (socketToClose != null) {
			try {
				socketToClose.close();
			} catch (Exception e) {
				System.out.println("Error closing listening socket.");
				e.printStackTrace();
			}
		}
	}

	/**
	 * This method sends data to the opposing peer asking for fileName and chunk Number
	 * @param opposingSocket = Peer's listeningSocket
	 * @param clientInputArr = opposingSocket.IP, opposingSocket.port, fileName, chunkNumber
	 */
	private void sendOpposingPeer(Socket opposingSocket, String[] clientInputArr, Socket downloaderSocket) {
		try {
			//Need to find the persistent connection between tracker and opposing peer which is : opposing socket
			PrintWriter out = new PrintWriter(opposingSocket.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(opposingSocket.getInputStream()));

			String ipAdd = downloaderSocket.getInetAddress().toString().replaceAll("/", "");
			String portNo = String.valueOf(downloaderSocket.getPort());
			String fileNeeded = clientInputArr[2];
			String chunkNumber = clientInputArr[3];
			String dataToSend = "";
			//Implies that not the last
			if(clientInputArr.length != 5) {
				dataToSend = ipAdd + Constant.COMMA
						+ portNo + Constant.COMMA + fileNeeded + Constant.COMMA + chunkNumber;
			} else {
				dataToSend = ipAdd + Constant.COMMA
						+ portNo + Constant.COMMA + fileNeeded + Constant.COMMA + chunkNumber
						+ Constant.COMMA + Constant.LAST_CHUNK;
			}

			//Sends to request peer B of the fileName + chunk Number + ipAdd + port No
//			System.out.println("Helping downloader to send information" + dataToSend);

			out.println(dataToSend);
			out.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * This method mediates the transfer of files between 2 clients through their
	 * public ip and port number
	 * @param strCommandArr
	 * @param currentReply
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	private void mediate(String[] strCommandArr, PrintWriter currentReply) throws IOException, InterruptedException {
		//strCommandArr is: 7 public address of A public port of A
		String [] downloaderArr = strCommandArr[1].split(Constant.COMMA);
		String downloaderAddress = downloaderArr[0];
		String downloaderPort = downloaderArr[1];
		
		boolean isLast = false;
		
//		System.out.println("downloadArr size is " + downloaderArr.length);
//		for(int i=0; i < downloaderArr.length ; i ++) {
//			System.out.println("At mediate, Printing out downloadArr" + downloaderArr[i]);
//		}
		
		if(downloaderArr.length == 3) {
//			System.out.println("downloaderArr[2] " + downloaderArr[2]);
			isLast = true;
		}
		Tuple downloaderTuple = new Tuple(downloaderAddress, downloaderPort);
		Tuple finalTuple = new Tuple("empty", "empty");
		//Obtain the downloader's socket
		Socket downloaderSocket = null;
		Set<Entry<Tuple, Socket>> entrySet = Tracker.dataTransferTable.entrySet();
		for(Entry<Tuple, Socket> entry2 : entrySet) {
			if(downloaderTuple.getIpAdd().equals(entry2.getKey().getIpAdd())
					&& downloaderTuple.getPortNo().equals(entry2.getKey().getPortNo())) {
				downloaderSocket = entry2.getValue();
				finalTuple = entry2.getKey();
			}
		} 

		if(downloaderSocket == null) {
			System.out.println("Exception Obtaining downloader's Socket");
//			clientRetry();
			return;
		}
		
//		System.out.println("Sending data to this downloader's new socket" + downloaderSocket);
		
		//This socket is the socket of the one sending data

		
		Socket opposingNewSocket = clientSocket;
		opposingNewSocket.setKeepAlive(true);
		opposingNewSocket.setReuseAddress(true);
		if(opposingNewSocket.isConnected()) {
//			System.out.println("Opposing new socket is connected");
		}
		
		//		Socket transferrerSocket = new Socket();
		byte[] fileDataBytes = new byte[Constant.CHUNK_SIZE];
		InputStream is = null;
		BufferedInputStream bis = null;
		BufferedOutputStream dos =  null;
		try {
			//Read Data from opposing new Socket
//			System.out.println("transferrerSocket is : " + opposingNewSocket);
			is = opposingNewSocket.getInputStream();
			bis = new BufferedInputStream(is);
//			System.out.println("Top");
			int bytesRead = bis.read(fileDataBytes, 0, fileDataBytes.length);
//			System.out.println("Middle");

//			System.out.println("length of bytesRead " + bytesRead);
			byte[] newFileDataBytes = Arrays.copyOf(fileDataBytes, bytesRead);
			//Write Data to downloader Socket
//			System.out.println("Bottom");

			dos = new BufferedOutputStream(downloaderSocket.getOutputStream());
			dos.write(newFileDataBytes);
			dos.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Error in mediating data");
			e.printStackTrace();
		} finally {
			Thread.sleep(10000);
			if (is != null) is.close();
		} 
		
		Set<Entry<Tuple, Socket>> entrySet1 = Tracker.dataTransferTable.entrySet();
		for(Entry<Tuple, Socket> entry2 : entrySet1) {
//			System.out.println("Before removal dataSocket " + entry2.getKey().getIpAdd());
//			System.out.println("Before removal dataSocket " + entry2.getValue());
		} 
		if(isLast) {
			removeDataSocket(finalTuple, downloaderSocket);
			dos.close();
		}
	}
	
	private void closeDataSocket(Tuple downloaderTuple) {
		Socket dataSocketToClose = Tracker.dataTransferTable.get(downloaderTuple);
		if (dataSocketToClose != null) {
			try {
				dataSocketToClose.close();
			} catch (IOException e) {
				System.out.println("Error closing dataSocket");
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * This method removes the socket that was opened up
	 *  
	 */
	private void removeDataSocket(Tuple downloaderTuple, Socket downloaderSocket) {
		closeDataSocket(downloaderTuple);
		Tracker.dataTransferTable.remove(downloaderTuple, downloaderSocket);
	}
	
	private void printThreads() {
		Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
		System.out.println("Printing threads!");
			for ( Thread t : threadSet){
				System.out.println("Thread :"+t+":"+"state:"+t.getState());
			}
	}
}
