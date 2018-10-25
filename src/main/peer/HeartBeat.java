package main.peer;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

import main.utilities.constants.Constant;
import main.utilities.errors.ErrorMessage;

/**
 * This class sends a heartbeat from a client to tracker
 * @author 
 *
 */
public class HeartBeat {

	public HeartBeat() {

	}

	/**
	 * Sends a heartbeat from a client to the tracker
	 */
	public void sendHeartBeat(Socket currentClientSocket) {
		PrintWriter out = null;
		try {
			out = new PrintWriter(currentClientSocket.getOutputStream(), true);
			out.println(Constant.HEARTBEAT);
			out.flush();
		} catch (IOException e) {
			System.out.println(ErrorMessage.UNKNOWN_ERROR);
			out.flush();
		}
	}
}
