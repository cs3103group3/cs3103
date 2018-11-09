package main.peer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import main.tracker.Record;
import main.utilities.commons.CheckAccuracy;
import main.utilities.commands.InterfaceCommand;
import main.utilities.commands.OfflineInterfaceCommand;
import main.utilities.constants.Constant;
import main.utilities.constants.NetworkConstant;
import main.utilities.feedbacks.ErrorMessage;

public class Server extends Thread {
	Socket serverSocket;
	ArrayList<Socket> clientSocketList;
	PrintWriter out;
    BufferedReader in;
    
	public void run() { 
		System.out.println("Starting P2P Server");
		clientSocketList = new ArrayList<Socket>();

		//Starts new instance of server
		processConnection();
		sendListeningSocketData();
		listen();
    }
	
	public void closeSockets() {
	    try {
            for (Socket clientSocket : clientSocketList) {
                clientSocket.close();
            }
        } catch (IOException e) {
            System.out.println(ErrorMessage.CANNOT_CLOSE_SOCKET + "Client not found.");
        }
	    
	    try {
            serverSocket.close();
        } catch (IOException e) {
            System.out.println(ErrorMessage.CANNOT_CLOSE_SOCKET + "ServerSocket not found.");
        }
	}
	
	private void sendListeningSocketData() {
		try {
            out.println(InterfaceCommand.AddListeningSocket.getCommandCode());
        } catch(Exception e) {
        	System.out.println("Exception while listing from server: " + e);
        	e.printStackTrace();
        }
	}
	
	private void listen() {
		boolean threadRunning = true;
		String clientInput = "";
		try {
			while(threadRunning) {
				clientInput = in.readLine();
//				System.out.println("Client has entered command: " + clientInput);
				System.out.println("clientInput: " + clientInput);
				
				if(clientInput != null) {
					String[] clientInputArr = clientInput.split(Constant.COMMA);
					sendMediateData(clientInputArr);
				}
			}
		} catch (IOException e) {
			System.out.println("IOException");
		}
	}
	
	private void sendMediateData(String [] clientInputArr) {
		String downloaderIP = clientInputArr[0];
		String downloaderPort = clientInputArr[1];
		String requestedFile = clientInputArr[2];
		String chunkNo = clientInputArr[3];
		ExecutorService executor = null;
		Socket tempSocket;
		//Creates a new Socket for file transfer
		try {
			tempSocket = new Socket(InetAddress.getByName(NetworkConstant.TRACKER_HOSTNAME), NetworkConstant.TRACKER_LISTENING_PORT);
			PrintWriter out = new PrintWriter(tempSocket.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(tempSocket.getInputStream()));
			out.println(InterfaceCommand.MEDIATE.getCommandCode() + Constant.WHITESPACE + downloaderIP + Constant.COMMA + downloaderPort);
			System.out.println("tempSocket is " + tempSocket);
			executor = Executors.newFixedThreadPool(5);
			Runnable worker = new RequestHandler(tempSocket, requestedFile, chunkNo);
			executor.execute(worker);
			//tempSocket.close();
		} catch (IOException e) {
			System.out.println("Unable to create new socket to transfer data for mediation");
			e.printStackTrace();
		}  finally {
			if (executor != null) {
				executor.shutdown();
			}
		}
	}
	
	private void processConnection() {
		
		try {
			serverSocket = new Socket(InetAddress.getByName(NetworkConstant.TRACKER_HOSTNAME), NetworkConstant.TRACKER_LISTENING_PORT);
            out = new PrintWriter(serverSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
		} catch (IOException e) {
			System.out.println("Unable to create client socket");
			e.printStackTrace();
		}
		
//		ExecutorService executor = null;
//		try {
//			executor = Executors.newFixedThreadPool(5);
//			serverSocket = new ServerSocket(NetworkConstant.SERVER_LISTENING_PORT);
//			while (true) {
//				Socket clientSocket = serverSocket.accept();
//				clientSocketList.add(clientSocket);
//				System.out.println("Accepted connection: " + clientSocket);
//				Runnable worker = new RequestHandler(clientSocket);
//				executor.execute(worker);
//			}
//		} catch(IOException ioe) {
//			System.out.println("Exception while listening for client connection: " + ioe);
//		} finally {
//			if (executor != null) {
//				executor.shutdown();
//			}
//		}
	}
}
