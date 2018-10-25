package main.peer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import main.utilities.commands.OfflineInterfaceCommand;

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
		System.out.println("Received message from " + Thread.currentThread().getName() + " : " + in.readLine());
		System.out.println("Client has entered command: " + in.readLine());
		String result = in.readLine();
		String resultTrimmed = result.trim();
		String[] resultArr = resultTrimmed.split(",");
		
		PrintWriter reply = new PrintWriter(client.getOutputStream(), true);
		reply.println("This is the reply from server!");
		
//		System.out.println(resultArr.length);
//		if(resultArr.length == 2){
//			System.out.println("Client wants chunk " + resultArr[1] + " from " + resultArr[0]);
//			
//
//			reply.println(OfflineInterfaceCommand.VALID_DOWNLOAD);
//		} else {
//			reply.println(OfflineInterfaceCommand.INVALID_DOWNLOAD);
//		}
		
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
	  } catch (Exception ex) {
	    System.out.println("Exceprion in Thread Run. Exception : " + ex);
	  }
    }

}
