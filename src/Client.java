import java.io.*;
import java.net.*;
import java.util.Random;

public class Client {
    public static void main(String[] args) throws IOException {
        String serverIP = "192.168.100.73"; // Server IP
        int portNumber = 5896; // Server port
        Socket clientSocket;

        try {
            clientSocket = new Socket(serverIP, portNumber); // Attempt to connect to server
        } catch (IOException e) {
            System.out.println("Server is down. Try again later.");
            return;
        }

        BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));//to get input from the user (reads from the console)
        DataOutputStream outputToServer = new DataOutputStream(clientSocket.getOutputStream());//to send messages to the server via the client socket's output stream
        BufferedReader inputFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));//to receive messages from the server via the client socket's input stream

        while (true) {
            System.out.print("Enter your message: ");
            String message = userInput.readLine();

            if (message == null || message.trim().isEmpty()) {
                System.out.println("Error: Empty messages are not allowed.");
                continue;
            }

            if (message.equalsIgnoreCase("Quit")) {
                outputToServer.writeBytes(message + "\n");
                break;
            }

            // Generate the checksum for the message
            String checksum = generateChecksum(message);

            // Simulate message errors with a n% probability
            message = simulateErrors(message, 0.3);

            // Send message and checksum to the server
            outputToServer.writeBytes(message + " - " + checksum + "\n");

            // Receive and print the server's response
            String serverResponse = inputFromServer.readLine();
            System.out.println("Server: " + serverResponse);
        }

        clientSocket.close(); // Close the connection
    }

    // Function to compute 16-bit one's complement checksum
    private static String generateChecksum(String message) {
        int checksum = 0;

        for (int i = 0; i < message.length(); i += 2) {
            int word = message.charAt(i) << 8;
            if (i + 1 < message.length()) {
                word += message.charAt(i + 1);
            }
            checksum += word;
            if ((checksum & 0x10000) != 0) {
                checksum = (checksum & 0xFFFF) + 1; // Handle overflow
            }
        }

        checksum = ~checksum & 0xFFFF;
        return Integer.toHexString(checksum).toUpperCase();
    }
    // variables to track total message count and error message count
    private static int totalMessageCount = 0;
    private static int errorMessageCount = 0;


    // Simulate errors in the message with a given probability
    private static String simulateErrors(String message, double errorRate) {
        totalMessageCount++;
        // Calculate the expected number of messages that should contain errors based on probability
        int expectedErrorMessages = (int) (totalMessageCount * errorRate);
        if (errorMessageCount < expectedErrorMessages) { // If the error count is less than expected
            errorMessageCount++;

            // Introduce an error into the message
            return introduceMessageError(message);
        }

        // If the error count has reached the expected amount, return the original message
        return message;
    }

    // Method to introduce a random error into the message
    private static String introduceMessageError(String message) {
        Random random = new Random();
        StringBuilder alteredMessage = new StringBuilder(message);

        // Introduce an error at a random position in the message
        int errorPosition = random.nextInt(message.length());
        alteredMessage.setCharAt(errorPosition, (char) (random.nextInt(26) + 'a')); // Replace with a random lowercase letter

        return alteredMessage.toString();
    }




}



