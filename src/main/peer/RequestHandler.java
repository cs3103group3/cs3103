package main.peer;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import com.sun.org.apache.xalan.internal.xsltc.util.IntegerArray;

import main.utilities.commands.OfflineInterfaceCommand;
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
		} catch (NumberFormatException | IOException e) {
			System.out.println("Error with numberFormat or io exception");
			e.printStackTrace();
		}
    }
    
//    @Override
//    public void run() {
//	try {
//		
//		BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
//		System.out.println("Thread started with name:" + Thread.currentThread().getName());
//		String result = in.readLine();
//		String resultTrimmed = result.trim();
//		System.out.println("Received message from " + Thread.currentThread().getName() + " : " + resultTrimmed);
//		// resultArr[0] contains fileName, resultArr[1] contains chunkNumber
//		String[] resultArr = resultTrimmed.split(",");
//		String fileName = resultArr[0];
//		int chunkNumber = Integer.parseInt(resultArr[1]);
//		
//		if (resultArr.length == 2) {
//			processDownload(fileName, chunkNumber);
////			reply.println(OfflineInterfaceCommand.VALID_DOWNLOAD);
//		}
//		else {
////			reply.println(OfflineInterfaceCommand.INVALID_DOWNLOAD);
//		}
//	} catch (IOException e) {
//	    System.out.println("I/O exception: " + e);
//	    e.printStackTrace();
//	  } catch (Exception ex) {
//	    System.out.println("Exception in Thread Run. Exception : " + ex);
//	    ex.printStackTrace();
//	  }
//    }

    private void processDownload(String fileName, int chunkNumber) throws IOException {
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
    			
    			System.out.println("bis available: " + bis.available());
				bis.read(fileByteArray, 0, fileByteArray.length);
				os = client.getOutputStream();
				System.out.println("Sending " + fileName + ".chunk" + chunkNumber);
		        os.write(fileByteArray,0,fileByteArray.length);
		        os.flush();
		        System.out.println("Successfully sent!");
			} catch (IOException e) {
				System.out.println("IOException when reading file: " + e);
				e.printStackTrace();
			} finally {
				if (bis != null) bis.close();
		        if (os != null) os.close();
			}
    		
    	} catch (FileNotFoundException e) {
    		System.out.println("FileNotFoundException when finding file: " + e);
			e.printStackTrace();
    	}
    }
}
