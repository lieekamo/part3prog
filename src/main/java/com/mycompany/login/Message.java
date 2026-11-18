package com.mycompany.login;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Message {

    // ---- Part 3 arrays  ----
    private static final List<Message> sentMessages = new ArrayList<>();
    private static final List<Message> disregardedMessages = new ArrayList<>();
    private static final List<Message> storedMessages = new ArrayList<>();   // read from the JSON file
    private static final List<String> messageHashes = new ArrayList<>();
    private static final List<String> messageIDs = new ArrayList<>();

    private static int nextMessageNumber = 0;
    private static String currentSender = "Developer";
    private static Path jsonFile = Paths.get("messages.json");
    private static final Type LIST_TYPE = new TypeToken<List<Message>>() {}.getType();

    private final String messageID;
    private final int messageNumber;
    private final String sender;
    private final String recipient;
    private final String text;
    private final String messageHash;

    
    public Message(String recipient, String text) {
        this(generateID(), nextMessageNumber++, recipient, text);
    }

    // Constructor that lets tests supply the ID and number
    public Message(String messageID, int messageNumber, String recipient, String text) {
        this.messageID = messageID;
        this.messageNumber = messageNumber;
        this.sender = currentSender;
        this.recipient = recipient;
        this.text = text;
        this.messageHash = createMessageHash();
    }

    private static String generateID() {
        Random r = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            sb.append(r.nextInt(10));
        }
        return sb.toString();
    }

    // ------------------------------------------------------------------
    // Part 2 methods
    // ------------------------------------------------------------------

    public boolean checkMessageID() {
        return messageID != null && messageID.length() <= 10;
    }

    // International code (+ and 1-3 digits) followed by no more than 10 digits
    public String checkRecipientCell() {
        if (recipient != null && recipient.matches("^\\+\\d{1,3}\\d{1,10}$")) {
            return "Cell phone number successfully captured.";
        }
        return "Cell phone number is incorrectly formatted or does not contain an "
                + "international code. Please correct the number and try again.";
    }

    public String checkMessageLength() {
        if (text.length() <= 250) {
            return "Message ready to send.";
        }
        int over = text.length() - 250;
        return "Message exceeds 250 characters by " + over + ", please reduce the size.";
    }

    // First two digits of ID : message number : FIRSTWORD + LASTWORD (uppercase)
    public String createMessageHash() {
        String[] words = text.trim().split("\\s+");
        String first = "";
        for (String w : words) {
            String c = clean(w);
            if (!c.isEmpty()) { first = c; break; }
        }
        String last = "";
        for (int i = words.length - 1; i >= 0; i--) {
            String c = clean(words[i]);
            if (!c.isEmpty()) { last = c; break; }
        }
        return (messageID.substring(0, 2) + ":" + messageNumber + ":" + first + last).toUpperCase();
    }

    private static String clean(String word) {
        return word.replaceAll("[^A-Za-z0-9]", "");
    }

    // 1 = send, 2 = disregard, 3 = store
    public String sentMessage(int choice) {
        switch (choice) {
            case 1:
                sentMessages.add(this);
                recordIDAndHash();
                return "Message successfully sent.";
            case 2:
                disregardedMessages.add(this);
                recordIDAndHash();
                return "Press 0 to delete the message.";
            case 3:
                recordIDAndHash();
                storeMessage();
                return "Message successfully stored.";
            default:
                return "Invalid option.";
        }
    }

    private void recordIDAndHash() {
        messageIDs.add(messageID);
        messageHashes.add(messageHash);
    }

    public static String printMessages() {
        StringBuilder sb = new StringBuilder();
        for (Message m : sentMessages) {
            sb.append("Message ID: ").append(m.messageID).append("\n")
              .append("Message Hash: ").append(m.messageHash).append("\n")
              .append("Recipient: ").append(m.recipient).append("\n")
              .append("Message: ").append(m.text).append("\n\n");
        }
        return sb.toString();
    }

    public static int returnTotalMessages() {
        return sentMessages.size();
    }

    // ------------------------------------------------------------------
    // JSON storage (Gson)
    // ------------------------------------------------------------------

    // Adds this message to the stored array and saves the whole array to the JSON file
    public void storeMessage() {
        if (!storedMessages.contains(this)) {
            storedMessages.add(this);
        }
        writeStoredToFile();
    }

    private static void writeStoredToFile() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            Files.writeString(jsonFile, gson.toJson(storedMessages, LIST_TYPE));
        } catch (IOException e) {
            System.out.println("Could not store message: " + e.getMessage());
        }
    }

    // Reads the JSON file back into the stored messages array
    public static void loadStoredMessages() {
        storedMessages.clear();
        Gson gson = new Gson();
        try {
            if (Files.exists(jsonFile)) {
                List<Message> fromFile = gson.fromJson(Files.readString(jsonFile), LIST_TYPE);
                if (fromFile != null) {
                    storedMessages.addAll(fromFile);
                }
            }
        } catch (IOException e) {
            System.out.println("Could not read stored messages: " + e.getMessage());
        }
        for (Message m : storedMessages) {
            if (!messageIDs.contains(m.messageID)) {
                messageIDs.add(m.messageID);
                messageHashes.add(m.messageHash);
            }
        }
    }

    // ------------------------------------------------------------------
    // Part 3: Stored Messages menu features
    // ------------------------------------------------------------------

    // a. Sender and recipient of all stored messages
    public static String displaySenderAndRecipients() {
        if (storedMessages.isEmpty()) {
            return "There are no stored messages.";
        }
        StringBuilder sb = new StringBuilder();
        for (Message m : storedMessages) {
            sb.append("Sender: ").append(m.sender)
              .append(" | Recipient: ").append(m.recipient).append("\n");
        }
        return sb.toString();
    }

    // b. Longest stored message
    public static String longestStoredMessage() {
        if (storedMessages.isEmpty()) {
            return "There are no stored messages.";
        }
        Message longest = storedMessages.get(0);
        for (Message m : storedMessages) {
            if (m.text.length() > longest.text.length()) {
                longest = m;
            }
        }
        return longest.text;
    }

    // c. Search by message ID (looks through sent, disregarded and stored)
    public static String searchByMessageID(String id) {
        List<Message> all = new ArrayList<>();
        all.addAll(sentMessages);
        all.addAll(disregardedMessages);
        all.addAll(storedMessages);
        for (Message m : all) {
            if (m.messageID.equals(id)) {
                return "Recipient: " + m.recipient + ", Message: " + m.text;
            }
        }
        return "Message ID not found.";
    }

    // d. All messages sent or stored for one recipient
    public static List<String> searchByRecipient(String recipient) {
        List<String> found = new ArrayList<>();
        for (Message m : storedMessages) {
            if (m.recipient.equals(recipient)) {
                found.add(m.text);
            }
        }
        for (Message m : sentMessages) {
            if (m.recipient.equals(recipient)) {
                found.add(m.text);
            }
        }
        return found;
    }

    // e. Delete a stored message using its hash
    public static String deleteByHash(String hash) {
        for (int i = 0; i < storedMessages.size(); i++) {
            Message m = storedMessages.get(i);
            if (m.messageHash.equals(hash)) {
                storedMessages.remove(i);
                messageHashes.remove(m.messageHash);
                messageIDs.remove(m.messageID);
                writeStoredToFile();
                // The brief shows the deleted text without its final full stop
                String shown = m.text.endsWith(".") ? m.text.substring(0, m.text.length() - 1) : m.text;
                return "Message: \"" + shown + "\" successfully deleted.";
            }
        }
        return "Message hash not found.";
    }

    // f. Report with the full details of the sent and stored messages
    public static String displayReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("===== SENT MESSAGES =====\n");
        appendDetails(sb, sentMessages);
        sb.append("===== STORED MESSAGES =====\n");
        appendDetails(sb, storedMessages);
        return sb.toString();
    }

    private static void appendDetails(StringBuilder sb, List<Message> list) {
        if (list.isEmpty()) {
            sb.append("(none)\n");
        }
        for (Message m : list) {
            sb.append("Message ID: ").append(m.messageID).append("\n")
              .append("Message Hash: ").append(m.messageHash).append("\n")
              .append("Sender: ").append(m.sender).append("\n")
              .append("Recipient: ").append(m.recipient).append("\n")
              .append("Message: ").append(m.text).append("\n\n");
        }
    }

    // ------------------------------------------------------------------
    // Getters and helpers 
    // ------------------------------------------------------------------

    public String getMessageID() { return messageID; }
    public String getMessageHash() { return messageHash; }
    public String getRecipient() { return recipient; }
    public String getText() { return text; }
    public String getSender() { return sender; }

    public static List<Message> getSentMessages() { return new ArrayList<>(sentMessages); }
    public static List<Message> getDisregardedMessages() { return new ArrayList<>(disregardedMessages); }
    public static List<Message> getStoredMessages() { return new ArrayList<>(storedMessages); }
    public static List<String> getMessageHashes() { return new ArrayList<>(messageHashes); }
    public static List<String> getMessageIDs() { return new ArrayList<>(messageIDs); }

    public static void setSender(String name) { currentSender = name; }
    public static void setStoragePath(Path path) { jsonFile = path; }

    // Empties every array (used by the unit tests)
    public static void resetAll() {
        sentMessages.clear();
        disregardedMessages.clear();
        storedMessages.clear();
        messageHashes.clear();
        messageIDs.clear();
    }
}