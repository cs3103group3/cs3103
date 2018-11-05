package main.peer;

import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

import sun.net.www.MessageHeader;

public class StunClient {

	int localPort;
	int serverPort = StunServer.serverPort;
	InetAddress serverAddress = StunServer.serverAddress;
	String returnIP_Port;

	public StunClient(int lPort) {
		this.localPort = lPort;
	}
	public void getServerResponse() {
		try {
			//Creates stun client socket
			Socket stunClientSocket = new Socket(serverAddress, serverPort);
			
		} catch (IOException e) {
			System.err.println("Error in creating stun client socket");
			System.exit(1);
		}
	}
	
}