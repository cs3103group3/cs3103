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
        command = (command==null) ? InterfaceCommand.INVALID : command;
        
        switch(command) {
        case LIST:
            list();
            break;
        case CHANGE_DIRECTORY:
            changeDirectory();
            break;
        case SEARCH:
            search();
            break;
        case DOWNLOAD:
            download();
            break;
        case INFORM:
            inform();
            break;
        case QUIT:
            quit();
            break;
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
        System.out.println("Goodbye!");
    }
    
    private static int getUserSelectedOption() {
        Scanner sc = new Scanner(System.in);
        return sc.nextInt();
    }
    
    public void start() {
        displayWelcomeMessage();
        int userInput = getUserSelectedOption();
        executeCommand(userInput);
    }

}
