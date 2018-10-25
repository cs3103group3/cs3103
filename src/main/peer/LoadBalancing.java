package main.peer;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Random;

/**
 * This class chooses a random ip address through secure random
 * then returns the chosen ip address
 */
public class LoadBalancing {

	public LoadBalancing() {
		
	}
	/**
	 * @param 
	 * This method is called by the peer so that each ipaddress in the list
	 * have and equal chance of being used by the client
	 * @return
	 */
	public String getIPToConnect(ArrayList<String> ipList) {
		Random currRan = new SecureRandom();
		//E.g. ipList is size 3, random peer is chosen from 0 to 2 index
		int peerChosen = currRan.nextInt(ipList.size());
		//Return the ip address of the chosen peer
		return ipList.get(peerChosen);
	}
}
