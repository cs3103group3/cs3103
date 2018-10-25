package main.peer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Hashtable;

import main.tracker.Record;
import main.utilities.commons.CheckAccuracy;
import main.utilities.commands.OfflineInterfaceCommand;
import main.utilities.constants.NetworkConstant;

public class Server extends Thread {
	static ServerSocket serverSocket;

	public void run() { 
		System.out.println("Starting P2P Server");

		//Starts new instance of server
		try {
			serverSocket = new ServerSocket(NetworkConstant.SERVER_LISTENING_PORT);
		} catch(IOException ioe) {
			System.out.println("Unable to create Server Socket at Peer Server");
			System.exit(1);
		}

		listenToDownloadRequest();
    }

	private static void listenToDownloadRequest() {
		
		while(true) {
			try {
				Socket clientSocket = serverSocket.accept();

				BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				String result = in.readLine();
				String resultTrimmed = result.trim();
				System.out.println("Client has entered command: " + resultTrimmed);
				String[] resultArr = resultTrimmed.split(",");
				
				PrintWriter reply = new PrintWriter(clientSocket.getOutputStream(), true);
				
				System.out.println(resultArr.length);
				if(resultArr.length == 2){
					System.out.println("Client wants chunk " + resultArr[1] + " from " + resultArr[0]);
					

					reply.println(OfflineInterfaceCommand.VALID_DOWNLOAD.getCommandText());
				} else {
					reply.println(OfflineInterfaceCommand.INVALID_DOWNLOAD.getCommandText());
				}
				
			} catch(IOException ioe) {
				System.out.println("Error in creating listening socket");
				System.exit(1);
			}
		}
	}
}
