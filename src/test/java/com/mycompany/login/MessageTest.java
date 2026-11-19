package com.mycompany.login;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MessageTest {

    private static final Path TEST_FILE = Paths.get("test-messages.json");

    // ---------- Part 3 test data ----------
    private static final String T1 = "Did you get the cake?";
    private static final String T2 = "Where are you? You are late! I have asked you to be on time.";
    private static final String T3 = "Yohoooo, I am at your gate.";
    private static final String T4 = "It is dinner time!";
    private static final String T5 = "Ok, I am leaving without you.";

    private Message m1, m2, m3, m4, m5;

    @BeforeEach
    public void setUp() throws IOException {
        Files.deleteIfExists(TEST_FILE);
        Message.setStoragePath(TEST_FILE);   
        Message.resetAll();
        Message.setSender("Developer");

        m1 = new Message("1000000001", 0, "+27834557896", T1);   
        m2 = new Message("1000000002", 1, "+27838884567", T2);   
        m3 = new Message("1000000003", 2, "+27834484567", T3);   
        m4 = new Message("1000000004", 3, "0838884567", T4);     
        m5 = new Message("1000000005", 4, "+27838884567", T5);   

        m1.sentMessage(1);
        m2.sentMessage(3);
        m3.sentMessage(2);
        m4.sentMessage(1);
        m5.sentMessage(3);
    }

    @AfterEach
    public void tearDown() throws IOException {
        Files.deleteIfExists(TEST_FILE);
    }

    // ---------- Part3  ----------

    @Test
    public void testSentMessagesArrayPopulated() {
        List<String> texts = new ArrayList<>();
        for (Message m : Message.getSentMessages()) {
            texts.add(m.getText());
        }
        assertEquals(List.of(T1, T4), texts);
        assertEquals("Developer", m4.getSender());
    }

    @Test
    public void testDisregardedAndStoredArrays() {
        assertEquals(1, Message.getDisregardedMessages().size());
        assertEquals(2, Message.getStoredMessages().size());
        assertEquals(5, Message.getMessageHashes().size());
        assertEquals(5, Message.getMessageIDs().size());
    }

    @Test
    public void testLongestMessage() {
        assertEquals(T2, Message.longestStoredMessage());
    }

    @Test
    public void testSearchForMessageID() {
        assertEquals("Recipient: 0838884567, Message: It is dinner time!",
                Message.searchByMessageID(m4.getMessageID()));
    }

    @Test
    public void testSearchByRecipient() {
        assertEquals(List.of(T2, T5), Message.searchByRecipient("+27838884567"));
    }

    @Test
    public void testDeleteByHash() {
        assertEquals("Message: \"Where are you? You are late! I have asked you to be on time\" successfully deleted.",
                Message.deleteByHash(m2.getMessageHash()));
        assertEquals(1, Message.getStoredMessages().size());
    }

    @Test
    public void testDisplayReport() {
        String report = Message.displayReport();
        assertTrue(report.contains(m1.getMessageHash()));
        assertTrue(report.contains("+27834557896"));
        assertTrue(report.contains(T1));
        assertTrue(report.contains(m4.getMessageHash()));
        assertTrue(report.contains(T4));
    }

    @Test
    public void testStoredMessagesAreReadBackFromJson() {
        Message.resetAll();
        Message.loadStoredMessages();
        assertEquals(2, Message.getStoredMessages().size());
        assertEquals(T2, Message.longestStoredMessage());
    }

    // ---------- Part 2 tests (still passing) ----------

    private final Message p1 = new Message("0012345678", 0, "+27718693002",
            "Hi Mike, can you join us for dinner tonight?");
    private final Message p2 = new Message("0198765432", 1, "08575975889",
            "Hi Keegan, did you receive the payment?");

    @Test
    public void testMessageLengthSuccess() {
        assertEquals("Message ready to send.", p1.checkMessageLength());
    }

    @Test
    public void testMessageLengthFailure() {
        Message m = new Message("0012345678", 9, "+27718693002", "a".repeat(260));
        assertEquals("Message exceeds 250 characters by 10, please reduce the size.",
                m.checkMessageLength());
    }

    @Test
    public void testRecipientSuccess() {
        assertEquals("Cell phone number successfully captured.", p1.checkRecipientCell());
    }

    @Test
    public void testRecipientFailure() {
        assertEquals("Cell phone number is incorrectly formatted or does not contain an "
                + "international code. Please correct the number and try again.",
                p2.checkRecipientCell());
    }

    @Test
    public void testMessageHashTestCase1() {
        assertEquals("00:0:HITONIGHT", p1.createMessageHash());
    }

    @Test
    public void testMessageHashesInLoop() {
        Message[] messages = {p1, p2};
        String[] expected = {"00:0:HITONIGHT", "01:1:HIPAYMENT"};
        for (int i = 0; i < messages.length; i++) {
            assertEquals(expected[i], messages[i].createMessageHash());
        }
    }

    @Test
    public void testMessageIDCreated() {
        Message m = new Message("+27718693002", "Hello there");
        assertTrue(m.checkMessageID());
        assertEquals(10, m.getMessageID().length());
    }

    @Test
    public void testSentMessageOptions() {
        assertEquals("Message successfully sent.", p1.sentMessage(1));
        assertEquals("Press 0 to delete the message.", p2.sentMessage(2));
        assertEquals("Message successfully stored.", p1.sentMessage(3));
    }
}

// Reference list 

//Chacon, S. and Straub, B., 2014. Git branching: Basic branching and merging. [online] Available at: https://git-scm.com/book/en/v2/Git-Branching-Basic-Branching-and-Merging [Accessed 17 November 2026].

//Google, [s.a.]. Gson user guide. [online] Available at: https://google.github.io/gson/UserGuide.html [Accessed 17 November 2026].

//JUnit, [s.a.]. JUnit 5 user guide. [online] Available at: https://junit.org/junit5/docs/current/user-guide/ [Accessed 17 November 2026].

//Oracle, [s.a.]a. Arrays. [online] Available at: https://docs.oracle.com/javase/tutorial/java/nutsandbolts/arrays.html [Accessed 17 November 2026].

//Oracle, [s.a.]b. Lesson: Regular expressions. [online] Available at: https://docs.oracle.com/javase/tutorial/essential/regex/index.html [Accessed 17 November 2026]. 