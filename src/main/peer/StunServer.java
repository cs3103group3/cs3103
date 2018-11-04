package main.peer;

import java.net.InetAddress;

/**
 * Class is created for conencting to a stun server
 *
 */
public class StunServer {
	static InetAddress serverAddress;
	static int serverPort;
	
	public StunServer(InetAddress iaddress, int sPort) {
		serverAddress = iaddress;
		serverPort = sPort;
	}
}
