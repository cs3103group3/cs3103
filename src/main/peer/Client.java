package main.peer;

import java.util.Scanner;

import main.utilities.command.OfflineInterfaceCommand;
import main.utilities.command.InterfaceCommand;
import main.utilities.error.ErrorMessage;

public class Client extends Thread {
    
    private boolean isConnectedToTracker;
    private boolean hasQuit;
    
    private static String displayOfflineMenu() {
        return "===============================================\n" +
               "Welcome to CS3103 P2P Client\n" +
               "Choose From the list of actions\n" +
               "1. " + OfflineInterfaceCommand.CONNECT_TO_TRACKER.getCommandText() + "\n" +
               "2. " + OfflineInterfaceCommand.QUIT.getCommandText() + "\n" +
               "===============================================\n";
    }
    
    private static String displayConnectedMenu() {
        return "===============================================\n" +
               "Welcome to CS3103 P2P Client\n" +
               "Choose From the list of actions\n" +
               "1. " + InterfaceCommand.LIST.getCommandText() + "\n" +
               "2. " + InterfaceCommand.CHANGE_DIRECTORY.getCommandText() + "\n" +
               "3. " + InterfaceCommand.SEARCH.getCommandText() + "\n" +
               "4. " + InterfaceCommand.DOWNLOAD.getCommandText() + "\n" +
               "5. " + InterfaceCommand.INFORM.getCommandText() + "\n" +
               "6. " + InterfaceCommand.QUIT.getCommandText() + "\n" +
               "===============================================\n";
    }
    
    private void connectToTracker() {
        if (true) {
            // if can connect to tracker via some socket
            isConnectedToTracker = true;
        } else {
            isConnectedToTracker = false;
        }
    }
    
    private void executeWhenNotConnectedToTracker() {
        displayOfflineMenu();
        int userInput = getUserSelectedOption();
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
        
        int userInput = getUserSelectedOption();
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
    
    private static int getUserSelectedOption() {
        Scanner sc = new Scanner(System.in);
        return sc.nextInt();
    }
    
    public void start() {
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
