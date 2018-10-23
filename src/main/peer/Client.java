package main.peer;

import java.util.Scanner;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
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
        String arg = userInputArr[1];
        
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
                    search(arg);
                    return true;
                case DOWNLOAD:
                    download();
                    return true;
                case INFORM:
                    inform();
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
            e.printStackTrace();
            System.out.println(ErrorMessage.UNKNOWN_ERROR.getErrorMessage());
            return true;  // So as not to quit the program, proceed as normal
        }        
    }
    
    private void list() throws UnknownHostException, IOException {
        Socket clientSocket = new Socket("localhost", NetworkConstant.CLIENT_LISTENING_PORT);
        DataOutputStream os = new DataOutputStream(clientSocket.getOutputStream());
        os.writeBytes(InterfaceCommand.LIST.toString());
        BufferedReader is = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        String reply = is.readLine();
        System.out.println(reply);
        clientSocket.close();
    }
    
    private void changeDirectory() throws Exception {
        
    }
    
    private void search(String filepath) throws Exception {
        Socket clientSocket = new Socket("localhost", NetworkConstant.CLIENT_LISTENING_PORT);
        DataOutputStream os = new DataOutputStream(clientSocket.getOutputStream());
        os.writeBytes(InterfaceCommand.SEARCH.toString());
        BufferedReader is = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        String reply = is.readLine();
        System.out.println(reply);
        clientSocket.close();
    }
    
    private void download() throws Exception {
        
    }
    
    private void inform() {
        
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
