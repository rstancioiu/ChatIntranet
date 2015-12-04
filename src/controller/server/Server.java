package controller.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import model.InformationServer;
import model.Message;

/**
 * Class server receives messages from different users and asssigns to a new
 * user and object of class Client.java
 * 
 * @author Afkid
 */
public class Server implements Runnable {

	public static final int BROADCAST_PORT = 13371;
	public static final int SLEEP = 120;

	private static final Logger log = LogManager.getLogger();

	private boolean disconnect = false;
	private Client[] clients;
	private ServerSocket socketserver;

	private Thread replyBroadcast;
	private ReceiveBroadcast receiveBroadcast;

	private String name;
	private int size;

	private InformationServer infos;
	private String address;
	private int port;
	private String type;
	private String password;
	private String host;

	/**
	 * Constructor of the server
	 * 
	 * @param name
	 * @param host
	 * @param size
	 * @param type
	 * @param password
	 * @throws IOException
	 */
	public Server(String name, String host, int size, String type, String password) throws IOException {
		if (size <= 0) {
			this.size = 25;
		} else {
			this.size = size;
		}
		log.info(type + " server " + name + " is created");
		clients = new Client[this.size];
		this.name = name;
		this.type = type;
		this.host = host;
		this.password = password;

		socketserver = new ServerSocket(0, size, InetAddress.getLocalHost());
		InetAddress serverAddress = socketserver.getInetAddress();
		this.infos = new InformationServer(this.name, getNumberClients(), this.size, serverAddress.getHostAddress(),
				socketserver.getLocalPort(), this.host, this.type);
		this.port = socketserver.getLocalPort();
		this.address = serverAddress.toString();
		receiveBroadcast = new ReceiveBroadcast(infos, password);
		replyBroadcast = new Thread(receiveBroadcast);
		replyBroadcast.start();
	}

	public void run() {
		while (!disconnect) {
			try {
				addClient(socketserver.accept());
			} catch (IOException e) {
				log.error("Error on adding a client");
			}
		}
		try {
			socketserver.close();
		} catch (IOException e) {
			log.error("Error when closing the server socket");
		}
	}

	/**
	 *
	 * @param socket
	 * @throws IOException
	 */
	public void addClient(Socket socket) throws IOException {
		if (!disconnect) {
			Client client = new Client(this, socket);
			for (int i = 0; i < clients.length; i++) {
				if (clients[i] == null) {
					clients[i] = client;
					infos.setClients(getNumberClients());
					Thread t = new Thread(client);
					t.start();
					break;
				}
			}
		}
	}

	/**
	 *
	 * @param message
	 * @param without
	 */
	public void sendToAll(Message message, Client without) {
		try {
			for (int i = 0; i < clients.length; i++) {
				if (clients[i] != null && clients[i] != without) {
					clients[i].sendMessage(message);
				}
			}
		} catch (Exception e) {
			System.out.println("e3");
		}
	}

	/**
	 * Send a private message
	 * 
	 * @param message
	 */
	public void sendPrivateMessage(Message message) {
		try {
			for (int i = 0; clients[i] != null; i++) {
				if (clients[i].getName().equals(message.getReceiver())) {
					clients[i].sendMessage(message);
					break;
				}
			}
		} catch (Exception e) {
			System.out.println("e4");
		}
	}

	/**
	 * Send the list of clients to a new client
	 * 
	 * @param client
	 */
	public void sendList(Client client) {
		try {
			for (int i = 0; i < clients.length; i++) {
				if (clients[i] != null) {
					TimeUnit.MILLISECONDS.sleep(SLEEP);
					client.sendMessage(new Message(Message.NEW_USER, null, clients[i].getName(), null));
				}
			}
		} catch (Exception e) {
			System.out.println("e5.1");
		}
	}

	/**
	 * Delete a client from the list of clients
	 * 
	 * @param client
	 */
	public void deleteClient(Client client) {
		for (int i = 0; i < clients.length; i++) {
			if (clients[i] != null && clients[i].equals(client)) {
				clients[i].setRunning(false);
				clients[i] = null;
				infos.setClients(getNumberClients());
			}
		}
	}

	/**
	 * @return number of clients in the server
	 */
	public int getNumberClients() {
		int nb = 0;
		for (int i = 0; i < clients.length; i++) {
			if (clients[i] != null)
				nb++;
		}
		return nb;
	}

	/**
	 * @param c
	 * @return if the server contains a client
	 */
	public boolean contains(Client c) {
		for (int i = 0; i < clients.length; i++) {
			if (clients[i] != null && c.equals(clients[i]) && clients[i] != c)
				return true;
		}
		return false;
	}

	/**
	 * Stops the server
	 */
	public void stopServer() {
		sendToAll(new Message(4, null, null, null), null);
		disconnect = true;
		receiveBroadcast.setRunning(false);
		for (int i = 0; i < clients.length; i++) {
			if (clients[i] != null) {
				clients[i].setRunning(false);
				clients[i] = null;
			}
		}
		try {
			socketserver.close();
		} catch (IOException e) {
			System.out.println("e6");
		}

	}

	/**
	 *
	 * @return server's info
	 */
	public InformationServer getInfos() {
		return infos;
	}

	/**
	 * @return server's name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return server's address
	 */
	public String getAddress() {
		return address;
	}

	/**
	 * @return server's port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * @return server's socket
	 */
	public ServerSocket getServerSocket() {
		return socketserver;
	}
}
