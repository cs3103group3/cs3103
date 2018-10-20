package test;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import main.utilities.command.InterfaceCommand;;

class InterfaceCommandTest {

    @Test
    void testForCode_GetCorrectMapping_True() {
        InterfaceCommand expected = InterfaceCommand.INVALID;
        InterfaceCommand actual = InterfaceCommand.forCode(0);
        assertEquals(expected, actual);
    }
    
    @Test
    void testForCode_GetCorrectMapping_False() {
        InterfaceCommand expected = InterfaceCommand.INVALID;
        InterfaceCommand actual = InterfaceCommand.forCode(99);
        assertNotEquals(expected, actual);
    }

}
