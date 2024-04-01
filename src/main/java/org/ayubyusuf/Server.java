package org.ayubyusuf;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Server class that manages client connections for a chat application.
 * This class is responsible for initializing a server on a specified port,
 * accepting client connections, and spawning threads to handle those connections.
 */
public class Server {
	private static final Logger LOGGER = Logger.getLogger(Server.class.getName());
	private ServerSocket serverSocket;

	/**
	 * Constructs a new Server instance with a specific ServerSocket.
	 *
	 * @param serverSocket The server socket to listen on for incoming connections.
	 */
	public Server(ServerSocket serverSocket) {
		this.serverSocket = serverSocket;
	}

	/**
	 * Main entry point of the server application.
	 * Initializes the server on port 1234 and starts accepting client connections.
	 *
	 * @param args Command line arguments (not used).
	 */
	public static void main(String[] args) {
		try {
			ServerSocket serverSocket = new ServerSocket(1234); // Initialize server socket on port 1234
			LOGGER.info("Server started on port 1234.");
			Server server = new Server(serverSocket);
			server.startServer(); // Start server to accept client connections
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "Failed to start the server on port 1234", e);
		}
	}

	/**
	 * Starts the server and listens for incoming client connections.
	 * Upon a connection, it spawns a new thread for each client with a ClientHandler to manage communication.
	 */
	public void startServer() {
		try {
			while (!serverSocket.isClosed()) {
				Socket socket = serverSocket.accept();
				LOGGER.info("A new client has connected!");
				ClientHandler clientHandler = new ClientHandler(socket);
				new Thread(clientHandler).start();
			}
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "An error occurred while accepting a new connection", e);
		} finally {
			closeServerSocket();
		}
	}

	/**
	 * Closes the server socket and releases any system resources associated with it.
	 */
	public void closeServerSocket() {
		if (serverSocket != null) {
			try {
				serverSocket.close();
				LOGGER.info("Server socket closed successfully.");
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, "Failed to close the server socket", e);
			}
		}
	}
}
