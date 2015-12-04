package controller.server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import model.Aes;
import model.Message;

/**
 * Class Client is an object assigned to every user which is connected to the
 * server.
 * 
 * @author Afkid
 */
public class Client implements Runnable {

	private static final Logger log = LogManager.getLogger();
	private Server server;
	private Socket socket;
	private PrintWriter flowExit;
	private BufferedReader flowIncomming;
	private Aes aes = new Aes();

	private String name;
	private boolean running = true;

	/**
	 * Constructor for a imaginary client, used by the server
	 * 
	 * @param server
	 * @param socket
	 */
	public Client(Server server, Socket socket) {
		this.server = server;
		this.socket = socket;
	}

	public void run() {
		try {
			while (running) {
				flowIncomming = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				String s = flowIncomming.readLine();
				if (s != null) {
					s = aes.decrypt(s, 1);
					handleMessage(s);
				}
			}
			socket.close();
		} catch (Exception e) {
			log.trace("ERROR OF SERVER {}", e);
			running = false;
		}
	}

	/**
	 * Handles a message
	 * 
	 * @param s
	 */
	public void handleMessage(String s) {
		Message message = new Message(s);
		int typeOfMessage = message.getType();

		log.info("Handling message of type " + typeOfMessage);

		switch (typeOfMessage) {
		case Message.MESSAGE:
			server.sendToAll(message, null);
			break;

		case Message.NEW_USER:
			name = message.getSender();
			while (server.contains(this)) {
				name = name + "_";
			}
			server.sendToAll(new Message(Message.NEW_USER, "new", name, null), this);
			server.sendList(this);
			sendMessage(new Message(Message.NEW_USER, "pseudo", name, null));
			break;

		case Message.EXIT:
			server.sendToAll(new Message(3, null, name, null), null);
			server.deleteClient(this);
			running = false;
			break;

		case Message.PRIVATE_MESSAGE:
			server.sendPrivateMessage(message);
			break;

		case Message.SEND_FILE:
			server.sendPrivateMessage(message);
			break;

		case Message.FILE_CANCELLED:
			server.sendPrivateMessage(message);
			break;

		default:
			break;
		}
	}

	/**
	 * Sends a message
	 * 
	 * @param message
	 */
	public void sendMessage(Message message) {
		try {
			String s = aes.encrypt(message.toString(), 1);

			flowExit = new PrintWriter(socket.getOutputStream());
			flowExit.println(s);
			flowExit.flush();
			log.info("Message sent");
		} catch (Exception e) {
			log.trace("An error occured while sending a message {}", e);
			System.out.println("An error occured");
		}
	}

	/**
	 * Compare function between clients
	 * 
	 * @param client
	 */
	public boolean equals(Client client) {
		if (name.equals(client.getName())) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * @return name of the client
	 */
	public String getName() {
		return name;
	}

	/**
	 * Replaces running with a new value
	 * 
	 * @param running
	 */
	public void setRunning(boolean running) {
		this.running = running;
	}
}
