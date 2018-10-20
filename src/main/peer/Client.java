package main.peer;

import java.util.Scanner;

import main.utilities.command.InterfaceCommand;
import main.utilities.error.ErrorMessage;

public class Client extends Thread {
    
    private static String displayWelcomeMessage() {
        return "===============================================\n" +
               "Welcome to CS3103 P2P Client\n" +
               "Choose From the list of actions\n" +
               "1. " + InterfaceCommand.LIST.getCommandText() +
               "2. " + InterfaceCommand.CHANGE_DIRECTORY.getCommandText() +
               "3. " + InterfaceCommand.SEARCH.getCommandText() +
               "4. " + InterfaceCommand.DOWNLOAD.getCommandText() +
               "5. " + InterfaceCommand.INFORM.getCommandText() +
               "6. " + InterfaceCommand.QUIT.getCommandText() +
               "===============================================\n";
    }

    private void executeCommand(int input) {
        InterfaceCommand command = InterfaceCommand.forCode(input);
        
        switch(command) {
        case LIST:
            //perform list
            break;
        case CHANGE_DIRECTORY:
            //Change directory
            break;
        case SEARCH:
            //perform search
            break;
        case DOWNLOAD:
            //perform downloading of file
            break;
        case INFORM:
            //perform downloading of file
            break;
        case QUIT:
            //perform exit
            break;
        default:
            //Error
            System.out.println(ErrorMessage.INVALID_COMMAND.getErrorMessage());
            return;
        }
    }
    
    private void list(int input) {
        
    }
    
    private void changeDirectory(int input) {
        
    }
    
    private void search(int input) {
        
    }
    
    private void download(int input) {
        
    }
    
    private void inform(int input) {
        
    }
    
    private void quit(int input) {
        
    }
    
    private static int getUserInput() {
        Scanner sc = new Scanner(System.in);
        return sc.nextInt();
    }
    
    public void start() {
        displayWelcomeMessage();
        int userInput = getUserInput();
        executeCommand(userInput);
    }

}
