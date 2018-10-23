package main.peer;

import java.util.Scanner;
import java.io.IOException;
import java.net.*;

import main.utilities.commands.InterfaceCommand;
import main.utilities.commands.OfflineInterfaceCommand;
import main.utilities.errors.ErrorMessage;
import main.utilities.constants.NetworkConstant;
import main.message.*;

public class Client extends Thread {
    
    private boolean isConnectedToTracker;
    private boolean hasQuit;
    
    private static void displayOfflineMenu() {
        System.out.println("===============================================\n" +
                           "Welcome to CS3103 P2P Client\n" +
                           "Choose From the list of actions\n" +
                           "1. " + OfflineInterfaceCommand.CONNECT_TO_TRACKER.getCommandText() + "\n" +
                           "2. " + OfflineInterfaceCommand.QUIT.getCommandText() + "\n" +
                           "===============================================\n" + 
                           "Enter your option: ");
    }
    
    private static void displayConnectedMenu() {
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
    
    private void connectToTracker() {
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter the Tracker's hostname:");
        String destAddr = sc.nextLine();
        System.out.println("Enter port number:");
        int destPort = sc.nextInt();
     
        
        try {
            DatagramSocket clientSocket = new DatagramSocket();
            InetAddress IPAddress = InetAddress.getByName(destAddr);
            
            byte[] byteArr = "PLACEHOLDER_DATA".getBytes();
            Message msg = new Message(MessageType.CONNECTION_REQUEST, "192.168.125.1", 90, destAddr, destPort, byteArr);
            
            byte[] sendData = Message.serializeMessage(msg);
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, NetworkConstant.TRACKER_LISTENING_PORT);
            clientSocket.send(sendPacket);
            
            
            isConnectedToTracker = true;
            clientSocket.close();
        } catch (UnknownHostException uhe) {
            isConnectedToTracker = false;
            System.out.println(ErrorMessage.UNKNOWN_HOST);
        } catch (IOException ioe) {
            isConnectedToTracker = false;
        }
    }
    
    private void executeWhenNotConnectedToTracker() {
        displayOfflineMenu();
        int userInput = getUserSelectedCommand();
        OfflineInterfaceCommand command = OfflineInterfaceCommand.forCode(userInput);
        command = (command==null) ? OfflineInterfaceCommand.INVALID : command;
        
        hasQuit = false;
        
        switch(command) {
	        case CONNECT_TO_TRACKER:
	            connectToTracker();
	            return;
	        case QUIT:
	            quit();
	            return;
	        default:
	            System.out.println(ErrorMessage.INVALID_COMMAND.getErrorMessage());
	            return;
        }
    }

    private void executeWhenConnectedToTracker() {
        displayConnectedMenu();
        
        int userInput = getUserSelectedCommand();
        InterfaceCommand command = InterfaceCommand.forCode(userInput);
        command = (command==null) ? InterfaceCommand.INVALID : command;
        
        hasQuit = false;
        
        switch(command) {
	        case LIST:
	            list();
	            return;
	        case CHANGE_DIRECTORY:
	            changeDirectory();
	            return;
	        case SEARCH:
	            search();
	            return;
	        case DOWNLOAD:
	            download();
	            return;
	        case INFORM:
	            inform();
	            return;
	        case QUIT:
	            quit();
	            return;
	        default:
	            System.out.println(ErrorMessage.INVALID_COMMAND.getErrorMessage());
	            return;
	        }
    }
    
    private void list() {
        
    }
    
    private void changeDirectory() {
        
    }
    
    private void search() {
        
    }
    
    private void download() {
        
    }
    
    private void inform() {
        
    }
    
    private void quit() {
        hasQuit = true;
        System.out.println("Goodbye!");
    }
    
    private static int getUserSelectedCommand() {
        Scanner sc = new Scanner(System.in);
        int option = InterfaceCommand.INVALID.getCommandCode();
        try {
            option = Integer.parseInt(sc.nextLine());
        } catch (NumberFormatException e) {
            option = InterfaceCommand.INVALID.getCommandCode();
        }
        return option;
    }
    
    public void run() {
        isConnectedToTracker = false;
        hasQuit = false;
        
        while (!hasQuit) {
            if (isConnectedToTracker) {
                executeWhenConnectedToTracker();
            } else {
                executeWhenNotConnectedToTracker();
            }
        }       
    }

}
