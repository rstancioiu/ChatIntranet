package controller.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import javax.swing.JList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import controller.server.Server;
import model.Aes;
import model.InformationServer;
import view.ServerList;
import view.Window;

/**
 * Broadcaster class sends broadcasts and the password from the user to the
 * server
 * 
 * @see model.InformationServer
 * @see view.Window
 * @see JList
 * @see DatagramPacket
 * @see DatagramSocket
 * 
 * @author Afkid
 */
public class Broadcaster implements Runnable {

	private static final Logger log = LogManager.getLogger();

	private static int SIZE_MESSAGE = 4096;

	private DatagramPacket packetBroadcast;
	private DatagramPacket packetReply;
	private DatagramSocket socket;
	private byte[] data;

	private Aes aes = new Aes();
	private static final String PW_ACCEPTED = "wowsuchpassword~~";
	private static final String DISCOVERY = "youwutm8~~";

	private Timer timer;

	private String messageReceived;

	private boolean acceptedConnection;
	private byte[] recu = new byte[SIZE_MESSAGE];

	private DatagramPacket packetPassword;

	private Boolean running = true;
	private Window window;
	private InformationServer infos;
	private ServerList serverList;

	/**
	 * The construct of the class
	 * 
	 * @param window
	 * @param serverList
	 */
	public Broadcaster(Window window, ServerList serverList) {
		log.info("Broadcaster created");
		this.window = window;
		try {
			data = (aes.encrypt(DISCOVERY, 0)).getBytes();
			byte[] dataSent = new byte[SIZE_MESSAGE];
			for (int i = 0; i < SIZE_MESSAGE; ++i) {
				if (i < data.length)
					dataSent[i] = data[i];
				else
					dataSent[i] = 0;
			}
			acceptedConnection = false;
			this.serverList = serverList;
			socket = new DatagramSocket();
			socket.setBroadcast(true);
			packetBroadcast = new DatagramPacket(dataSent, dataSent.length, InetAddress.getByName("255.255.255.255"),
					Server.BROADCAST_PORT);
			packetReply = new DatagramPacket(recu, recu.length);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void run() {

		// timers used to send the broadcast and update the list of servers
		timer = new Timer();
		timer.schedule(new sendPacketBroadcast(), 0, 1200);

		while (running) {
			try {
				socket.receive(packetReply);
				messageReceived = new String(packetReply.getData());
				String messageDecrypted = aes.decrypt(truncateMessage(messageReceived), 0);
				for (int i = 1; i < messageDecrypted.length(); i++) {
					if (messageDecrypted.charAt(i) == '~' && messageDecrypted.charAt(i - 1) == '~') {
						messageDecrypted = messageDecrypted.substring(0, i + 1);
						break;
					}
				}
				log.trace("Message received and decrypted");
				if (messageDecrypted.equals(PW_ACCEPTED)) {
					log.info("Password accepted");
					acceptedConnection = true;
					TimeUnit.MILLISECONDS.sleep(300);
				} else {
					infos = new InformationServer(messageDecrypted);
					log.info("Informations from {}", infos.getName());
					serverList.addServer(infos);
					acceptedConnection = false;
				}
			} catch (Exception e) {
				log.trace("ERROR WHILE BROADCASTING :{}", e);
			}
		}
	}

	/**
	 * Sends a password to the server to be verified
	 * 
	 * @param password
	 */
	public void sendPassword(String password) {
		try {
			String messageSent = password + "~~";
			byte[] data2 = (aes.encrypt(messageSent, 0)).getBytes();
			byte[] dataSent = new byte[SIZE_MESSAGE];
			for (int i = 0; i < SIZE_MESSAGE; ++i) {
				if (i < data2.length)
					dataSent[i] = data2[i];
				else
					dataSent[i] = 0;
			}
			packetPassword = new DatagramPacket(dataSent, dataSent.length,
					InetAddress.getByName(serverList.getAddressByIndex(window.getSelectedIndex())),
					Server.BROADCAST_PORT);
			socket.send(packetPassword);
			log.info("Password sent to {} on port {}", serverList.getAddressByIndex(window.getSelectedIndex()),
					Server.BROADCAST_PORT);
		} catch (Exception e) {
			log.trace("ERROR WHEN SENDING A PASSWORD {}", e);
		}
	}

	/**
	 * Truncates a message
	 * 
	 * @param message
	 */
	public String truncateMessage(String message) {
		String messageTruncated = "";
		for (int i = 0; i < message.length(); ++i) {
			if (message.charAt(i) == 0) {
				break;
			} else
				messageTruncated += message.charAt(i);
		}
		return messageTruncated;
	}

	/**
	 * @return if the connection is accepted
	 */
	public boolean isAccepted() {
		return acceptedConnection;
	}

	/**
	 * Replace the connection
	 * 
	 * @param acceptedConnection
	 */
	public void setAcceptedConnection(boolean acceptedConnection) {
		this.acceptedConnection = acceptedConnection;
	}

	/**
	 * Class which send broadcasts each X seconds
	 */
	private class sendPacketBroadcast extends TimerTask {
		public void run() {
			try {
				log.info("Packet broadcast sent");
				socket.send(packetBroadcast);
			} catch (IOException e) {
				socket.close();
				log.trace("Broadcast interrupted - socket closed {}", e);
			}
		}
	}
}
