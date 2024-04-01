package org.ayubyusuf;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles client connections, manages sending and receiving messages for a chat application.
 */
public class ClientHandler implements Runnable {
	private static final Logger LOGGER = Logger.getLogger(ClientHandler.class.getName());
	public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
	private Socket socket;
	private BufferedReader bufferedReader;
	private BufferedWriter bufferedWriter;
	private String clientUsername;

	public ClientHandler(Socket socket) {
		try {
			this.socket = socket;
			this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			this.clientUsername = bufferedReader.readLine();
			clientHandlers.add(this);
			broadcastMessage("SERVER: " + clientUsername + " has entered the chat!");
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "Error initializing ClientHandler", e);
			closeEverything(socket, bufferedReader, bufferedWriter);
		}
	}

	@Override
	public void run() {
		String messageFromClient;
		try {
			while (socket.isConnected()) {
				messageFromClient = bufferedReader.readLine();
				if (messageFromClient != null) {
					broadcastMessage(messageFromClient);
				} else {
					closeEverything(socket, bufferedReader, bufferedWriter);
					break;
				}
			}
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "Error in ClientHandler run method", e);
			closeEverything(socket, bufferedReader, bufferedWriter);
		}
	}

	/**
	 * Sends a message from one client to all other clients connected to the server.
	 *
	 * @param messageToSend The message to broadcast.
	 */
	public void broadcastMessage(String messageToSend) {
		for (ClientHandler clientHandler : clientHandlers) {
			try {
				if (!clientHandler.clientUsername.equals(this.clientUsername)) {
					clientHandler.bufferedWriter.write(messageToSend);
					clientHandler.bufferedWriter.newLine();
					clientHandler.bufferedWriter.flush();
				}
			} catch (IOException e) {
				closeEverything(clientHandler.socket, clientHandler.bufferedReader, clientHandler.bufferedWriter);
			}
		}
	}

	/**
	 * Removes this client handler from the list of active clients and notifies others.
	 */
	public void removeClientHandler() {
		clientHandlers.remove(this);
		broadcastMessage("SERVER: " + clientUsername + " has left the chat!");
	}

	/**
	 * Closes all resources associated with this client handler and removes it from the list of active handlers.
	 *
	 * @param socket         The client's socket.
	 * @param bufferedReader The reader for the client's incoming messages.
	 * @param bufferedWriter The writer for sending messages to the client.
	 */
	private void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
		removeClientHandler();
		try {
			if (bufferedReader != null) bufferedReader.close();
			if (bufferedWriter != null) bufferedWriter.close();
			if (socket != null) socket.close();
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "Error closing resources in ClientHandler", e);
		}
	}
}
