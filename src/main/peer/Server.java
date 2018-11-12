package main.peer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
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
			String data = new String(Integer.toString(Peer.listeningPort));
            out.println(InterfaceCommand.AddListeningSocket.getCommandCode() + Constant.WHITESPACE + data);
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
				
				if(clientInput != null) {
					String[] clientInputArr = clientInput.split(Constant.COMMA);
					sendMediateData(clientInputArr);
				} else {
					System.out.println(ErrorMessage.CANNOT_CONNECT_TO_TRACKER.getErrorMessage());
					System.exit(1);
				}
			}
		} catch (IOException e) {
			System.out.println("IOException");
		}
	}
	
	private void sendMediateData(String [] clientInputArr) throws UnknownHostException, IOException {
		String downloaderIP = clientInputArr[0];
		String downloaderPort = clientInputArr[1];
		String requestedFile = clientInputArr[2];
		String chunkNo = clientInputArr[3];
		
		boolean isLastChunk = false;
		
		if(clientInputArr.length ==  5) {
			if (clientInputArr[4].equals(Constant.LAST_CHUNK)) {
				isLastChunk = true;
			}
		}
		ExecutorService executor = null;
		Socket tempSocket;
		//Creates a new Socket towards the relay/tracker for file transfer
		tempSocket = new Socket(InetAddress.getByName(NetworkConstant.TRACKER_HOSTNAME), NetworkConstant.TRACKER_LISTENING_PORT);
		try {
			PrintWriter out = new PrintWriter(tempSocket.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(tempSocket.getInputStream()));
			if(!isLastChunk) {
				out.println(InterfaceCommand.MEDIATE.getCommandCode() + Constant.WHITESPACE + downloaderIP 
						+ Constant.COMMA + downloaderPort);
			} else {
				out.println(InterfaceCommand.MEDIATE.getCommandCode() 
						+ Constant.WHITESPACE + downloaderIP + Constant.COMMA + downloaderPort
						+ Constant.COMMA 
						+ Constant.LAST_CHUNK);
			}
			System.out.println("tempSocket is " + tempSocket);
			executor = Executors.newFixedThreadPool(200);
			Runnable worker = new RequestHandler(tempSocket, requestedFile, chunkNo);
//			tempSocket.setKeepAlive(true);
			executor.execute(worker);
			//tempSocket.close();
		} catch (IOException e) {
			System.out.println("Unable to create new socket to transfer data for mediation");
			e.printStackTrace();
		}  finally {
			if (executor != null) {
				executor.shutdown();
			}
			if (tempSocket != null) {
				tempSocket.close();
			}
		}
	}
	
	private void processConnection() {
		
		try {
			serverSocket = Peer.listeningSocket;
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
