package main.tracker;

/**
 * This class keeps all the information regarding one chunk
 * @author limwe
 *
 */
public class Record {
	
	String ipAddress;
	String chunkNumber;
	//TODO: Add checksum
	
	public Record(String ipAdd, String chunkNo) {
		this.ipAddress = ipAdd;
		this.chunkNumber = chunkNo;
	}
	
	public String getipAdd() {
		return this.ipAddress;
	}
	public String getChunkNo() {
		return this.chunkNumber;
	}
}
