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

import main.utilities.commands.OfflineInterfaceCommand;
import main.utilities.constants.Constant;

public class RequestHandler implements Runnable {
  private final Socket client;
  ServerSocket serverSocket = null;

    public RequestHandler(Socket client) {
      this.client = client;
    }

    @Override
    public void run() {
	try {
		
		BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
		System.out.println("Thread started with name:" + Thread.currentThread().getName());
		String result = in.readLine();
		String resultTrimmed = result.trim();
		System.out.println("Received message from " + Thread.currentThread().getName() + " : " + resultTrimmed);
		String[] resultArr = resultTrimmed.split(",");
		String fileName = resultArr[0];
		int chunkNumber = Integer.parseInt(resultArr[1]);
		
//		PrintWriter reply = new PrintWriter(client.getOutputStream(), true);
		
		if (resultArr.length == 2) {
			processDownload(fileName, chunkNumber);
//			reply.println(OfflineInterfaceCommand.VALID_DOWNLOAD);
		}
		else {
//			reply.println(OfflineInterfaceCommand.INVALID_DOWNLOAD);
		}
		
		
//		BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
//		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
//	         System.out.println("Thread started with name:" + Thread.currentThread().getName());
//	         String userInput;
//
//	    while ((userInput = in.readLine()) != null) {
//	    	//removes special character
//	      userInput=userInput.replaceAll("[^A-Za-z0-9 ]", "");
//          System.out.println("Received message from " + Thread.currentThread().getName() + " : " + userInput);
//	      writer.write("You entered : " + userInput);
//	      writer.newLine();
//	      writer.flush();
//	    }
	} catch (IOException e) {
	    System.out.println("I/O exception: " + e);
	    e.printStackTrace();
	  } catch (Exception ex) {
	    System.out.println("Exception in Thread Run. Exception : " + ex);
	    ex.printStackTrace();
	  }
    }

    private void processDownload(String fileName, int chunkNumber) throws IOException {
    	OutputStream os = null;
    	// TODO: append EOF char when sending last chunk
    	try {
    		byte[] fileByteArray = null;
//    		String filePath = Constant.FILE_DIR + fileName;
    		FileInputStream fis = new FileInputStream("/Users/brehmerchan/Desktop/P2p/src/main/files/test.txt");
    		BufferedInputStream bis = new BufferedInputStream(fis);
    		try {
    			File fileToSeed = new File("/Users/brehmerchan/Desktop/P2p/src/main/files/test.txt");
    			int startByte = Constant.CHUNK_SIZE * (chunkNumber - 1);
    			bis.skip(startByte);
    			int chunkSize = (int) (fileToSeed.length() - startByte);
    			if (chunkSize < Constant.CHUNK_SIZE) {
    				fileByteArray = new byte[chunkSize];
    			}
    			else {
    				fileByteArray = new byte[Constant.CHUNK_SIZE];
    			}
    			
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
