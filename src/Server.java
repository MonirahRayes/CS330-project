import java.io.*;
import java.net.*;

public class Server {
    public static void main(String[] args) throws IOException {
        int portNumber = 5896; // Port for server
        ServerSocket server = new ServerSocket(portNumber);
        System.out.println("Server listening on port " + portNumber);

        Socket clientSocket = server.accept(); // Wait for client to connect
        BufferedReader inputFromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        DataOutputStream outputToClient = new DataOutputStream(clientSocket.getOutputStream());

        while (true) {
            String clientMessage = inputFromClient.readLine(); // Receive client's message

            if (clientMessage == null || clientMessage.trim().isEmpty()) {
                outputToClient.writeBytes("Error: Message cannot be empty\n");
                continue;
            }

            if (clientMessage.equalsIgnoreCase("Quit")) {
                System.out.println("Client requested to quit.");
                break; // Stop the loop if the client wants to quit
            }

            // Message and checksum are split by " : "
            String[] messageAndChecksum = clientMessage.split(" - ");

            if (messageAndChecksum.length != 2) {
                outputToClient.writeBytes("Error: Incorrect message format\n");
                continue;
            }

            String messageContent = messageAndChecksum[0];
            String clientChecksum = messageAndChecksum[1];
            String calculatedChecksum = computeChecksum(messageContent);
            // Display the received message and the checksum on the server console
            System.out.println("Received Message: " + messageContent+ " And the Checksum is: " + calculatedChecksum);



            // Validate checksum
            if (!calculatedChecksum.equals(clientChecksum)) {
                outputToClient.writeBytes("Error: Checksum mismatch. Message corrupted.\n");
            } else {
                outputToClient.writeBytes("Success: Message received correctly.\n");
            }
        }

        clientSocket.close(); // Close the client connection
        server.close(); // Close the server
    }

    // Function to compute 16-bit one's complement checksum
    private static String computeChecksum(String message) {
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
}