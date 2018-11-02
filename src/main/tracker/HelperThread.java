package main.tracker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.Map.Entry;

import main.heartbeat.HeartBeatInitiator;
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
//	private Set<String> aliveIpAddress = Tracker.aliveIpAddress;
	
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
//		dummyList.add(new Record("192.168.1.0", "1"));
//		recordList.put("test.txt", dummyList);
		
		boolean threadRunning = true;
		String clientInput = "";
		try {
		    HeartBeatInitiator heartbeatInitiator = new HeartBeatInitiator(Tracker.aliveIpAddress);
		    heartbeatInitiator.start();
            
			in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		//	reply = new PrintWriter( new OutputStreamWriter(clientSocket.getOutputStream()));

			reply = new PrintWriter(clientSocket.getOutputStream(), true);
			
			while(threadRunning) {
				clientInput = in.readLine();

				System.out.println("Client has entered command: " + clientInput);
				if(clientInput.equals("Init test message")) {
					reply.println("\tWelcome");
				}
				if(!clientInput.equals("")) {
					break;
				}
			}

			doClientCommand(clientInput, reply);
		} catch (IOException e) {
			System.out.println("Io Exception");

		}
	}

	/**
	 * This method executes the 
	 * commands the client requested
	 * @param reply2 
	 */
	private void doClientCommand(String strCommand, PrintWriter currentReply) {
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
		if(currentList.isEmpty()) {
			currentReply.println(OfflineInterfaceCommand.EMPTY_RECORD);
			currentReply.println(Constant.END_OF_STREAM + Constant.NEWLINE);
		} else {
			String result = "";
			Set<Entry<String, ArrayList<Record>>> entrySet = currentList.entrySet();
			//Prints out currentList
			for(Entry<String, ArrayList<Record>> entry1 : entrySet) {
				result += entry1.getKey();
				result += Constant.NEWLINE;
			}
			result += Constant.END_OF_STREAM;
			currentReply.println(result);
			currentReply.flush();
		}
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
		} else {
			currentReply.println(ErrorMessage.FILE_NOT_FOUND);
			currentReply.flush();
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
		String ipBroadcasted = this.clientSocket.getInetAddress().toString();
		String[] recvData = strCommandArr[1].split(Constant.COMMA);
		
		long checksum = Long.parseLong(recvData[0]);
		String totalNumChunk = recvData[1];
		String chunkNum = recvData[2];
		String fileName = recvData[3];
		
		String payload = totalNumChunk + Constant.COMMA + chunkNum + Constant.COMMA + fileName;
		
		if (!CheckAccuracy.isDataValid(payload, checksum)) {
		    currentReply.println(ErrorMessage.INCONSISTENT_CHECKSUM);
            currentReply.println(Constant.END_OF_STREAM);
            currentReply.flush();
		    return;
		}
		
		Tracker.aliveIpAddress.add(ipBroadcasted);
		
		boolean hasExist =	checkExistFile(fileName);

		//If file already exists, simply add the chunk to it
		if(hasExist) {
			//Obtain the arraylist to update first
			ArrayList<Record> currArrFile = recordList.get(fileName);
			//Add new Record
			Record addToExist = new Record(ipBroadcasted, chunkNum, totalNumChunk);
			currArrFile.add(addToExist);

			//Replace the HashTable with updated data
			recordList.replace(fileName, currArrFile);

			currentReply.println(SuccessMessage.NEW_CHUNK_ADDED_TO_TRACKER);
			currentReply.println(Constant.END_OF_STREAM);
			currentReply.flush();
		} else {
			//Create a new Record
			Record newRecord = new Record(ipBroadcasted, chunkNum,totalNumChunk);
			//Create a new ArrayList
			ArrayList<Record> newArrFile = new ArrayList<Record>();
			//Add new Record
			newArrFile.add(newRecord);
			recordList.put(fileName, newArrFile);
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
		Set<Entry<String, ArrayList<Record>>> entrySet = recordList.entrySet();
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


		if(strCommandArr.length <= 1) {
			currentReply.print("Invalid Arguments");
			currentReply.flush();
		}
		boolean fileExist = checkExistFile(requestedFileName);
		
		//If no chunk Size is Specified
		if(strCommandArr.length == 2) {
			if(fileExist) {
				//Obtain the ArrayList of Entry associated with key of requestedFileName
				ArrayList<Record> requestedChunks = recordList.get(requestedFileName);
				
				String requestedData = "";
				for(int i =0 ; i < requestedChunks.size() ; i ++) {
					requestedData += requestedChunks.get(i).getipAdd();
					requestedData += Constant.COMMA;
					requestedData += requestedChunks.get(i).getChunkNo();
					requestedData += "\n";
				}
				
				requestedData += Constant.END_OF_STREAM + Constant.NEWLINE;
				currentReply.write(requestedData);
				currentReply.println(Constant.END_OF_STREAM + Constant.NEWLINE);
				currentReply.flush();
				
			} else {
				currentReply.println("File Requested does not Exists" + Constant.END_OF_STREAM + Constant.NEWLINE);
				currentReply.flush();
			}
		} else {
			//Chunk is Specified
			//Expected Reply is only ip address of requested chunk of file name
			if(fileExist) {
				String chunkNumber = strCommandArr[2];
				
				String requestedIP = findRequestedIP(requestedFileName, chunkNumber);
				
				if(requestedIP.equals(INVALID_CHUNK)) { 
					currentReply.println("Chunk of File Name Specified is invalid" + Constant.END_OF_STREAM + Constant.NEWLINE);
					currentReply.flush();
				} else {
					currentReply.write(requestedIP + Constant.END_OF_STREAM + Constant.NEWLINE);
					currentReply.flush();
				}
				
			} else {
				currentReply.println("File Requested does not Exists" + Constant.END_OF_STREAM + Constant.NEWLINE);
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
		Set<Entry<String, ArrayList<Record>>> entrySet = recordList.entrySet();
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
		String ipAddress = this.clientSocket.getInetAddress().toString();
		
		if(strCommandArr.length != 1) {
			currentReply.println("Invalid Arguments");
		} else {
			boolean ipExists = checkIPExists(ipAddress);
			if(ipExists) {
				deleteAllRecords(ipAddress);
				currentReply.println("Exited and Deleted Successfully");
				currentReply.flush();
				try {
					clientSocket.close();
				} catch (IOException e) {
					currentReply.println("Error closing socket");
				}
			} else {
				currentReply.println("Invalid IP address");
				currentReply.flush();
			}
		}
	}
	
	/**
	 * This method checks whether the ip address exists within the central directory server
	 * @param ipAddress: ip address to check
	 * @return
	 */
	private boolean checkIPExists(String ipAddress) {
		Set<Entry<String, ArrayList<Record>>> entrySet = recordList.entrySet();
		for(Entry<String, ArrayList<Record>> entry2 : entrySet) {
			ArrayList<Record> currArr = entry2.getValue();
			for(int i = 0; i < currArr.size(); i ++) {
				if(currArr.get(i).getipAdd().equals(ipAddress)) {
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
	private void deleteAllRecords(String ipAddress) {
		//First find all the entries that contains the associated ip address
		Set<Entry<String, ArrayList<Record>>> entrySet = recordList.entrySet();
		for(Entry<String, ArrayList<Record>> entry2 : entrySet) {
			ArrayList<Record> currArr = entry2.getValue();
			for(int i = 0; i < currArr.size(); i ++) {
				if(currArr.get(i).getipAdd().equals(ipAddress)) {
					//Removes the respective ip address in the respective arraylist
					entry2.getValue().remove(i);
				}
			}
		}
	}
}
