package centralDirectoryServer;

import java.net.Socket;

public class HelperThread extends Thread{
	Socket clientSocket;
	public HelperThread() {
		
	}
	public HelperThread(Socket client) {
		clientSocket = client;
	}
	
	public void run() {
		
	}
}
