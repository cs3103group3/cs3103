package main.tracker;

/**
 * This class keeps all the information regarding one chunk
 * @author limwe
 *
 */
public class Record {
	
	String ipAddress;
	String chunkNumber;
	final String maxChunk;
	
	public Record(String ipAdd, String chunkNo, String maxChunk) {
		this.ipAddress = ipAdd;
		this.chunkNumber = chunkNo;
		this.maxChunk = maxChunk;
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
}
