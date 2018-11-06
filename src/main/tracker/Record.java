package main.tracker;

/**
 * This class keeps all the information regarding one chunk
 * @author limwe
 *
 */
public class Record {
	
	String ipAddress;
	String portNumber;
	String chunkNumber;
	final String maxChunk;
	
	public Record(String ipAdd, String chunkNo, String maxChunk, String portNumber) {
		this.ipAddress = ipAdd;
		this.chunkNumber = chunkNo;
		this.maxChunk = maxChunk;
		this.portNumber = portNumber;
	}
	
	public String getipAdd() {
		return this.ipAddress;
	}
	public String getChunkNo() {
		return this.chunkNumber;
	}
	public String getMaxChunk(){
		return this.maxChunk;
	}
	
	public String getPortNumber() {
		return this.portNumber;
	}
}
