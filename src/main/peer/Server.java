package main.peer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Hashtable;

import main.tracker.Record;
import main.utilities.constants.NetworkConstant;

public class Server extends Thread {
	static ServerSocket serverSocket;

	public static void main(String[] args) {
		System.out.println("Starting Server");

		//Starts new instance of server
		try {
			serverSocket = new ServerSocket(NetworkConstant.CLIENT_LISTENING_PORT);
		} catch(IOException ioe) {
			System.out.println("Unable to create Server Socket");
			System.exit(1);
		}

		listenRequest();
	}

	private static void listenRequest() {
		//While server is still alive
		while(true) {
			try {
				Socket clientSocket = serverSocket.accept();
				System.out.println("I am here");

				BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				System.out.println("Client has entered command: " + in.readLine());
			} catch(IOException ioe) {
				System.out.println("Error in creating listening socket");
				System.exit(1);
			}
		}
	}
}
