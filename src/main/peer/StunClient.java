package main.peer;

import java.net.BindException;
import java.net.InetAddress;
import java.util.Scanner;

import sun.net.www.MessageHeader;

public class StunClient {

	InetAddress iaddress;
	int port;
	
	public StunClient(InetAddress iaddress, int port) {
		this.iaddress = iaddress;
		this.port = port;
	}
	
	public StunClient(InetAddress iaddress) {
		this.iaddress = iaddress;
		this.port = 0;
	}
	
	public void getServerResponse() {
		try {
			
		} catch (BindException be) {
			System.out.println(iaddress.toString() + ": " + be.getMessage());
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}
	
}