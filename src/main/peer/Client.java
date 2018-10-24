package main.peer;

import java.util.Scanner;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.io.IOException;

import main.utilities.commands.InterfaceCommand;
import main.utilities.errors.ErrorMessage;
import main.utilities.constants.NetworkConstant;

public class Client extends Thread {        
    private static void displayMenu() {
        System.out.println( "===============================================\n" +
                            "Welcome to CS3103 P2P Client\n" +
                            "Choose From the list of actions\n" +
                            "1. " + InterfaceCommand.LIST.getCommandText() + "\n" +
                            "2. " + InterfaceCommand.CHANGE_DIRECTORY.getCommandText() + "\n" +
                            "3. " + InterfaceCommand.SEARCH.getCommandText() + "\n" +
                            "4. " + InterfaceCommand.DOWNLOAD.getCommandText() + "\n" +
                            "5. " + InterfaceCommand.INFORM.getCommandText() + "\n" +
                            "6. " + InterfaceCommand.QUIT.getCommandText() + "\n" +
                            "===============================================\n" + 
                            "Enter your option: ");
    }

    private boolean execute() {
        Scanner sc = new Scanner(System.in);
        String userInput = sc.nextLine().trim();
        String[] userInputArr = userInput.split(" ");
        String userSelectedOption = userInputArr[0];
        
        InterfaceCommand command = InterfaceCommand.INVALID;
        try {
            command = InterfaceCommand.forCode(Integer.parseInt(userSelectedOption));
            command = (command==null) ? InterfaceCommand.INVALID : command;
        } catch (NumberFormatException e) {
            command = InterfaceCommand.INVALID;
        }
        
        try {
            switch(command) {
                case LIST:
                    list();
                    return true;
                case CHANGE_DIRECTORY:
                    changeDirectory();
                    return true;
                case SEARCH:
                    search(userInputArr);
                    return true;
                case DOWNLOAD:
                    download();
                    return true;
                case INFORM:
                    inform(userInputArr);
                    return true;
                case QUIT:
                    quit();
                    return false;
                default:
                    System.out.println(ErrorMessage.INVALID_COMMAND.getErrorMessage());
                    return true;
            }
        } catch (UnknownHostException e) {
            System.out.println(ErrorMessage.UNKNOWN_HOST.getErrorMessage());
            return true;
        } catch (Exception e) {
            System.out.println(ErrorMessage.UNKNOWN_ERROR.getErrorMessage());
            e.printStackTrace();
            return true;  // So as not to quit the program, proceed as normal
        }        
    }
    
    private void list() throws UnknownHostException, IOException {
        Socket clientSocket = new Socket(NetworkConstant.TRACKER_HOSTNAME, NetworkConstant.TRACKER_LISTENING_PORT);
        
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        out.println(InterfaceCommand.LIST.getCommandCode());
        out.flush();
        
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        System.out.println(in.readLine());
        clientSocket.close();
    }
    
    private void changeDirectory() throws Exception {
        
    }
    
    private void search(String[] userInputArr) throws Exception {
        if (userInputArr.length != 2) {
            System.out.println(ErrorMessage.INVALID_COMMAND.getErrorMessage());
            return;
        }
        
        String filePath = userInputArr[1];
        
        Socket clientSocket = new Socket(NetworkConstant.TRACKER_HOSTNAME, NetworkConstant.TRACKER_LISTENING_PORT);
        
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        out.println(InterfaceCommand.SEARCH.getCommandCode() + " " +filePath);
        out.flush();
        
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        System.out.println(in.readLine());
        clientSocket.close();
    }
    
    private void download() throws Exception {
        
    }
    
    private void inform(String[] userInputArr) throws UnknownHostException, IOException {
        if (userInputArr.length != 4) {
            System.out.println(ErrorMessage.INVALID_COMMAND.getErrorMessage());
            return;
        }
        
        String clientIpAddress = userInputArr[0];
        String fileName = userInputArr[1];
        String chunkNumber = userInputArr[2];
        
        String sendData = InterfaceCommand.INFORM.getCommandCode() + " " + clientIpAddress + " " + fileName + " " + chunkNumber;
        
        Socket clientSocket = new Socket(NetworkConstant.TRACKER_HOSTNAME, NetworkConstant.TRACKER_LISTENING_PORT);
        
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        out.println(sendData);
        out.flush();
        
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        System.out.println(in.readLine());
        clientSocket.close();
    }
    
    private void quit() {
        System.out.println("Goodbye!");
    }
        
    public void run() { 
        boolean proceed = true;
        
        while(proceed) {
            displayMenu();
            proceed = execute();
        }
    }

}
