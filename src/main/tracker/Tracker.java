package main.tracker;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import main.heartbeat.HeartBeatListener;
import main.utilities.constants.NetworkConstant;

/**
 * This class acts as the main class for a central directory server
 * @author cs3103 group 3
 *
 */
public class Tracker{
	static ServerSocket serverSocket;

	//ArrayList of all the files with unique names
	public static ArrayList<Record> fileArrList = new ArrayList<>();

	//To allow faster access, use a hash : fileName to its associated chunks
	public static Hashtable<String, ArrayList<Record>> recordTable = new Hashtable<>();
	public static Set<Tuple> alivePeers = new HashSet<Tuple>();
	
	//Tuple : 1) Ip 2) Port No.		Value = Socket
	public static Hashtable<Tuple, Socket> ipPortToSocketTable = new Hashtable<>();
	//TODO: Another Hash to pinpoint location of the record

	public static void main(String[] args) {
		System.out.println("Starting Server");

		//Starts new instance of server
		//		try {
		//			serverSocket = new ServerSocket(NetworkConstant.TRACKER_LISTENING_PORT);
		//		} catch(IOException ioe) {
		//			System.out.println("Unable to create Server Socket");
		//			System.exit(1);
		//		}
		/*** TESTING! REMOVE LATER ***/
		//        Tracker.aliveIpAddresses.add("110.14.24.5");
		//        Record newRecord = new Record("110.14.24.5", "1", "5");
		//        ArrayList<Record> newArrFile = new ArrayList<Record>();
		//        newArrFile.add(newRecord);
		//        recordTable.put("sandy.txt", newArrFile);
		//        ArrayList<Record> currArrFile = recordTable.get("sandy.txt");
		//        
		//        Tracker.aliveIpAddresses.add("110.14.24.34");
		//        Record addToExist = new Record("110.14.24.34", "1", "5");
		//        currArrFile.add(addToExist);
		//        recordTable.replace("sandy.txt", currArrFile);        
		//        recordTable.put("sandy.txt", newArrFile);
		//        
		//        Tracker.aliveIpAddresses.add("110.14.24.34");
		//        addToExist = new Record("110.14.24.34", "2", "5");
		//        currArrFile.add(addToExist);
		//        recordTable.replace("sandy.txt", currArrFile);        
		//        recordTable.put("sandy.txt", newArrFile);
		//        
		//        Tracker.aliveIpAddresses.add("215.44.22.4");
		//        newRecord = new Record("215.44.22.4", "2", "5");
		//        newArrFile = new ArrayList<Record>();
		//        newArrFile.add(newRecord);
		//        recordTable.put("washington.txt", newArrFile);
		//        
		//        System.out.println("RecordTable: " + recordTable);
		/*****************************/

		listenRequest();
	}

	private static void listenRequest() {
		HeartBeatListener heartbeatInitiator = new HeartBeatListener();
		heartbeatInitiator.start();

		ExecutorService executor= null;
		//While server is still alive
		try {

			serverSocket = new ServerSocket(NetworkConstant.TRACKER_LISTENING_PORT);
			executor = Executors.newFixedThreadPool(20);

			System.out.println("Waiting for client");

			while(true) {
				Socket clientSocket = serverSocket.accept();	
				// Will be printed twice whenever new client joins (2 persistent connection, client n server)
				System.out.println("Accepted a client");
				Runnable worker = new HelperThread(clientSocket);
				executor.execute(worker);
			}

		} catch(IOException ioe) {
			System.out.println("Error in creating listening socket");
			System.exit(1);
		}
	}

	public static void removeUnresponsivePeersFromRecord(Set<Tuple> listOfPeersWhoResponded) {
		alivePeers = listOfPeersWhoResponded;

		recordTable.forEach((filename,recordList) -> { 
			for (int i=0; i<recordList.size(); i++ ) {
				Record record = recordList.get(i);
				Tuple peer = new Tuple(record.getipAdd(), record.getPortNumber());
				if (!listOfPeersWhoResponded.contains(peer)) {
					recordList.remove(i);
					if (recordTable.get(filename) == null) {
						recordTable.remove(filename);
					}
					
					ipPortToSocketTable.remove(peer);
					
					i--;
				}
			}
		});
		
		removeFileWithEmptyRecords();
		printEverythInsideRecordAndIpToSocketTable();
	}

	public static void removeFileWithEmptyRecords() {
		Iterator<String> iterator = recordTable.keySet().iterator();
		while (iterator.hasNext()){
			if (recordTable.get(iterator.next()).size() < 1 ) {
				iterator.remove();
			}
		}
	}
	
	public static void printEverythInsideRecordAndIpToSocketTable(){
		recordTable.forEach((filename,recordList) -> { 
			for (int i=0; i<recordList.size(); i++ ) {
				Record record = recordList.get(i);
				Tuple peer = new Tuple(record.getipAdd(), record.getPortNumber());
				System.out.println("Filename: " + filename + ", Peer: " + record.getipAdd() + ": " + record.getPortNumber() + ", chunk: " + record.chunkNumber);
			}
		});
		
	}
}
