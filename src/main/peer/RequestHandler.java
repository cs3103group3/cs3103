package main.peer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.FileNotFoundException;
import java.net.ServerSocket;
import java.net.Socket;
import main.utilities.constants.Constant;

public class RequestHandler implements Runnable {
  private final Socket client;
  ServerSocket serverSocket = null;
  String requestedFileName = "";
  String requestedChunkNo = "";
    public RequestHandler(Socket client) {
      this.client = client;
    }
    public RequestHandler(Socket client, String requestedFile, String chunkNo) {
        this.client = client;
        this.requestedFileName = requestedFile;
        this.requestedChunkNo = chunkNo;
    }

    @Override
    public void run() {
    	try {
			processDownload(requestedFileName, Integer.valueOf(requestedChunkNo));
		} catch (NumberFormatException | IOException | InterruptedException e) {
			System.out.println("Error with numberFormat or io exception");
			e.printStackTrace();
		}
    }

    private void processDownload(String fileName, int chunkNumber) throws IOException, InterruptedException {
    	OutputStream os = null;
    	// TODO: append EOF char when sending last chunk
    	try {
    		byte[] fileByteArray = null;
    		FileInputStream fis = new FileInputStream(fileName);
    		BufferedInputStream bis = new BufferedInputStream(fis);
    		try {
    			File fileToSeed = new File(fileName);
    			int startByte = Constant.CHUNK_SIZE * (chunkNumber - 1);
    			bis.skip(startByte);
    			int chunkSize = (int) (fileToSeed.length() - startByte);
    			if (chunkSize < Constant.CHUNK_SIZE) {
    				fileByteArray = new byte[chunkSize];
    			}
    			else {
    				fileByteArray = new byte[Constant.CHUNK_SIZE];
    			}
    			
//    			System.out.println("bis available: " + bis.available());
				int temp = bis.read(fileByteArray, 0, fileByteArray.length);
				os = client.getOutputStream();
//				System.out.println("Sending (" + fileName + ", " + chunkNumber +")");
//				System.out.println("TEMP is : " + temp);
				os.write(fileByteArray,0,fileByteArray.length);
		        os.flush();
//		        System.out.println("Successfully sent!");
			} catch (IOException e) {
				System.out.println("IOException when reading file: " + e);
				e.printStackTrace();
			} finally {
				Thread.sleep(10000);
				if (bis != null) bis.close();
		        if (os != null) os.close();
			}
    		
    	} catch (FileNotFoundException e) {
    		System.out.println("FileNotFoundException when finding file: " + e);
			e.printStackTrace();
    	}
    }
}
