package me.solapp.network;

import java.io.*;
import java.net.Socket;

public class NetworkClient {
    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;

    public void connect(String host, int port) throws IOException {
        socket = new Socket(host, port);
        input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        output = new PrintWriter(socket.getOutputStream(), true);

        // Print server's initial response
        System.out.println("Server: " + input.readLine());
    }

    public String sendRequest(String request) throws IOException {
        System.out.println("DEBUG: Sending request: " + request);
        output.println(request);

        StringBuilder responseBuilder = new StringBuilder();
        String line;
        while ((line = input.readLine()) != null) {
            if (line.equals("END_OF_RESPONSE")) { // Stop when the end marker is found
                break;
            }
            responseBuilder.append(line).append("\n");
        }

        String response = responseBuilder.toString().trim();
        System.out.println("DEBUG: Full server response: " + response);
        return response;
    }



    public void disconnect() throws IOException {
        if (socket != null) {
            socket.close();
        }
    }
}
