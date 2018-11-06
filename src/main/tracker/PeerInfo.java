package main.tracker;

public class PeerInfo {

	String peerIpAdd;
	int portNo;
	public PeerInfo(String peerIp, int port) {
		peerIpAdd = peerIp;
		portNo = port;
	}
	
	public String getPeerIp() {
		return this.peerIpAdd;
	}
	public int getPortNo() {
		return this.portNo;
	}
}
