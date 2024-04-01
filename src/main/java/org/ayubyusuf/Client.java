package org.ayubyusuf;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Client class for handling chat operations, including sending and receiving messages
 * within a chat application. This class manages the connection to the server,
 * user input, and the display of messages from other clients.
 */
public class Client {
	private static final Logger LOGGER = Logger.getLogger(Client.class.getName());
	private Socket socket;
	private BufferedReader bufferedReader;
	private BufferedWriter bufferedWriter;
	private String username;

	/**
	 * Creates a client instance connected to the server through the specified socket
	 * and associates it with a unique username.
	 *
	 * @param socket   The socket connecting the client to the server.
	 * @param username The username of the client.
	 */
	public Client(Socket socket, String username) {
		try {
			this.socket = socket;
			this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			this.username = username;
			bufferedWriter.write(username);
			bufferedWriter.newLine();
			bufferedWriter.flush();
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "Could not initialize client.", e);
			closeEverything(socket, bufferedReader, bufferedWriter);
		}
	}

	/**
	 * Main entry point for the client application. It establishes a connection to the server
	 * and initiates the processes for sending and receiving messages.
	 *
	 * @param args Command line arguments (not used).
	 */
	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
		System.out.println("Enter your username for the group chat:");
		String username = scanner.nextLine();
		try {
			Socket socket = new Socket("localhost", 1234);
			Client client = new Client(socket, username);
			client.listenForMessage();
			client.sendMessage();
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "Failed to connect to the server.", e);
		}
	}

	/**
	 * Sends messages entered by the client to the server.
	 */
	public void sendMessage() {
		try (Scanner scanner = new Scanner(System.in)) {
			while (socket.isConnected()) {
				String messageToSend = scanner.nextLine();
				bufferedWriter.write(username + ": " + messageToSend);
				bufferedWriter.newLine();
				bufferedWriter.flush();
			}
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "An error occurred while sending a message.", e);
			closeEverything(socket, bufferedReader, bufferedWriter);
		}
	}

	/**
	 * Listens for messages from the server and prints them to the console. This method
	 * runs on a separate thread to continuously receive messages.
	 */
	public void listenForMessage() {
		new Thread(() -> {
			String msgFromGroupChat;
			try {
				while (socket.isConnected()) {
					msgFromGroupChat = bufferedReader.readLine();
					if (msgFromGroupChat != null) {
						System.out.println(msgFromGroupChat);
					} else {
						LOGGER.info("Disconnected from server.");
						closeEverything(socket, bufferedReader, bufferedWriter);
						break;
					}
				}
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, "Lost connection to the server.", e);
				closeEverything(socket, bufferedReader, bufferedWriter);
			}
		}).start();
	}

	/**
	 * Closes all resources associated with the client and gracefully shuts down.
	 *
	 * @param socket         The client's socket.
	 * @param bufferedReader The reader for incoming messages.
	 * @param bufferedWriter The writer for sending messages to the client.
	 */
	private void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
		try {
			if (bufferedReader != null) bufferedReader.close();
			if (bufferedWriter != null) bufferedWriter.close();
			if (socket != null) socket.close();
			LOGGER.info("Closed connection to server.");
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "An error occurred while closing connection resources.", e);
		}
	}
}
