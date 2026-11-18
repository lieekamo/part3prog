package com.mycompany.login;

import java.util.List;
import java.util.Scanner;

public class RegistrationApp {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== Registration ===");
        System.out.print("Enter first name: ");
        String firstName = scanner.nextLine();
        System.out.print("Enter last name: ");
        String lastName = scanner.nextLine();

        Login login = new Login(firstName, lastName);

        System.out.print("Enter username (must contain '_' and be no more than 5 characters): ");
        String username = scanner.nextLine();
        System.out.print("Enter password (8+ chars, capital letter, number, special char): ");
        String password = scanner.nextLine();
        System.out.print("Enter SA cell number (e.g. +27838968976): ");
        String cellPhoneNumber = scanner.nextLine();

        String registrationResult = login.registerUser(username, password, cellPhoneNumber);
        System.out.println(registrationResult);

        if (registrationResult.contains("registered successfully")) {
            System.out.println();
            System.out.println("=== Login ===");
            System.out.print("Enter username: ");
            String loginUsername = scanner.nextLine();
            System.out.print("Enter password: ");
            String loginPassword = scanner.nextLine();

            boolean success = login.loginUser(loginUsername, loginPassword);
            System.out.println(login.returnLoginStatus(success));

            // Only a successful login can reach the messaging app
            if (success) {
                start(scanner, loginUsername);
            }
        }

        scanner.close();
    }

    // QuickChat: only reached after a successful login
    public static void start(Scanner in, String username) {
        System.out.println("Welcome to QuickChat.");
        Message.setSender(username);
        Message.loadStoredMessages();   // read the JSON file into the stored array

        int maxMessages = readInt(in, "How many messages do you wish to enter? ");
        int entered = 0;

        boolean running = true;
        while (running) {
            System.out.println("\n1) Send Messages\n2) Show recently sent messages"
                    + "\n3) Quit\n4) Stored Messages");
            int option = readInt(in, "Choose an option: ");

            switch (option) {
                case 1:
                    if (entered >= maxMessages) {
                        System.out.println("You have reached your message limit.");
                        break;
                    }
                    if (sendOne(in)) {
                        entered++;
                    }
                    break;
                case 2:
                    System.out.println("Coming Soon.");
                    break;
                case 3:
                    running = false;
                    System.out.println("Total messages sent: " + Message.returnTotalMessages());
                    break;
                case 4:
                    storedMessagesMenu(in);
                    break;
                default:
                    System.out.println("Please choose 1, 2, 3 or 4.");
            }
        }
    }

    // Part 3: the "Stored Messages" menu
    private static void storedMessagesMenu(Scanner in) {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Stored Messages ---"
                    + "\n1) Display sender and recipient of all stored messages"
                    + "\n2) Display the longest stored message"
                    + "\n3) Search for a message ID"
                    + "\n4) Search for all messages for a recipient"
                    + "\n5) Delete a message using its hash"
                    + "\n6) Display a report of all messages"
                    + "\n0) Back to main menu");
            int option = readInt(in, "Choose an option: ");

            switch (option) {
                case 1:
                    System.out.println(Message.displaySenderAndRecipients());
                    break;
                case 2:
                    System.out.println(Message.longestStoredMessage());
                    break;
                case 3:
                    System.out.print("Enter the message ID: ");
                    System.out.println(Message.searchByMessageID(in.nextLine().trim()));
                    break;
                case 4:
                    System.out.print("Enter the recipient number: ");
                    List<String> found = Message.searchByRecipient(in.nextLine().trim());
                    if (found.isEmpty()) {
                        System.out.println("No messages found for that recipient.");
                    }
                    for (String m : found) {
                        System.out.println(m);
                    }
                    break;
                case 5:
                    System.out.print("Enter the message hash: ");
                    System.out.println(Message.deleteByHash(in.nextLine().trim()));
                    break;
                case 6:
                    System.out.println(Message.displayReport());
                    break;
                case 0:
                    back = true;
                    break;
                default:
                    System.out.println("Please choose 0 to 6.");
            }
        }
    }

    // Captures one message; returns true if it counted towards the limit
    private static boolean sendOne(Scanner in) {
        System.out.print("Recipient (e.g. +27718693002): ");
        String recipient = in.nextLine();
        System.out.print("Message: ");
        String text = in.nextLine();

        if (text.trim().isEmpty()) {
            System.out.println("Message cannot be empty.");
            return false;
        }

        Message msg = new Message(recipient, text);

        String cellResult = msg.checkRecipientCell();
        System.out.println(cellResult);
        if (!cellResult.startsWith("Cell phone number successfully")) {
            return false;
        }

        String lengthResult = msg.checkMessageLength();
        System.out.println(lengthResult);
        if (!lengthResult.equals("Message ready to send.")) {
            return false;
        }

        System.out.println("Message ID generated: " + msg.getMessageID());
        System.out.println("Message Hash: " + msg.getMessageHash());

        int choice = readInt(in, "1) Send  2) Disregard  3) Store : ");
        System.out.println(msg.sentMessage(choice));
        return choice == 1 || choice == 3;
    }

    private static int readInt(Scanner in, String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(in.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a number.");
            }
        }
    }
}