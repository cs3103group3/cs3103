package main.utilities.commons;

import java.util.zip.CRC32;
import java.util.zip.Checksum;

public class CheckAccuracy {
	//This method check if the data received is not corrupted by comparing the 
	//checksum value
	public static boolean isDataValid(String data, long givenChecksum){
		long correctChecksum = calculateChecksum(data);
		return correctChecksum == givenChecksum;
	}
	
	//This method is used to calculate the CRC checksum of a string
	public static long calculateChecksum(String data){
		// get bytes from string
		byte bytes[] = data.getBytes();
		 
		Checksum checksum = new CRC32();
		
		// update the current checksum with the specified array of bytes
		checksum.update(bytes, 0, bytes.length);
		 
		// get the current checksum value
		long checksumValue = checksum.getValue();
		 
		System.out.println("CRC32 checksum of " + data  + " is: " + checksumValue);
		return checksumValue;
	}
}
