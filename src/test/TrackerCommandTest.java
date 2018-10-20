package test;

import static org.junit.jupiter.api.Assertions.*;

import main.tracker.TrackerCommand;

import org.junit.jupiter.api.Test;

class TrackerCommandTest {

    @Test
    void testForCode_GetCorrectMapping_True() {
        TrackerCommand expected = TrackerCommand.INVALID;
        TrackerCommand actual = TrackerCommand.forCode(0);
        assertEquals(expected, actual);
    }
    
    @Test
    void testForCode_GetCorrectMapping_False() {
        TrackerCommand expected = TrackerCommand.INVALID;
        TrackerCommand actual = TrackerCommand.forCode(99);
        assertNotEquals(expected, actual);
    }

}
