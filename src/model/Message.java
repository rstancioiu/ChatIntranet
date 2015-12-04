package model;

/**
 * Class Message represents the type of messages that are sent through the
 * sockets
 * 
 * @author Afkid
 */
public class Message {

	public static final int NEW_USER = 0;
	public static final int MESSAGE = 1;
	public static final int PRIVATE_MESSAGE = 2;
	public static final int EXIT = 3;
	public static final int HOST_EXIT = 4;
	public static final int SEND_FILE = 5;
	public static final int FILE_CANCELLED = 6;

	private String body, sender, receiver, fileName, address;
	private int port;
	private int type;
	private int fileSize;

	/**
	 * Constructor of a default message
	 * 
	 * @param type
	 * @param body
	 * @param sender
	 * @param receiver
	 */
	public Message(int type, String body, String sender, String receiver) {
		this.type = type;
		this.body = body;
		this.fileName = body;
		this.sender = sender;
		this.receiver = receiver;
	}

	/**
	 * Constructor of a send file message
	 * 
	 * @param type
	 * @param fileName
	 * @param fileSize
	 * @param port
	 * @param address
	 * @param sender
	 * @param receiver
	 */
	public Message(int type, String fileName, int fileSize, int port, String address, String sender, String receiver) {
		this.type = type;
		this.address = address;
		this.port = port;
		this.fileSize = fileSize;
		this.fileName = fileName;
		this.sender = sender;
		this.receiver = receiver;
	}

	/**
	 * Constructor of a private message
	 * @param string
	 */
	public Message(String string) {
		if (string.charAt(0) == '#')
			type = Integer.parseInt(string.substring(1, 2));
		if (type != PRIVATE_MESSAGE && type != SEND_FILE && type != FILE_CANCELLED) {
			int lengthSender = 0;
			int lengthBody = 0;
			int i = 3;
			while (string.charAt(i) != '#') {
				lengthSender = lengthSender * 10 + (string.charAt(i) - '0');
				i++;
			}
			i++;
			while (string.charAt(i) != '#') {
				lengthBody = lengthBody + (string.charAt(i) - '0');
				i++;
			}
			sender = string.substring(i + 1, i + lengthSender + 1);
			body = string.substring(i + lengthSender + 2);
			if (lengthSender == 0)
				sender = null;
		} else if (type == PRIVATE_MESSAGE || type == FILE_CANCELLED) {
			int lengthReceiver = 0;
			int lengthSender = 0;
			int lengthBody = 0;
			int i = 3;
			while (string.charAt(i) != '#') {
				lengthReceiver = lengthReceiver * 10 + (string.charAt(i) - '0');
				i++;
			}
			i++;
			while (string.charAt(i) != '#') {
				lengthSender = lengthSender * 10 + (string.charAt(i) - '0');
				i++;
			}
			i++;
			while (string.charAt(i) != '#') {
				lengthBody = lengthBody + (string.charAt(i) - '0');
				i++;
			}
			receiver = string.substring(i + 1, i + 1 + lengthReceiver);
			i += lengthReceiver + 1;
			sender = string.substring(i + 1, i + 1 + lengthSender);
			i += lengthSender + 1;
			body = string.substring(i + 1);
			fileName = string.substring(i + 1);
			if (lengthReceiver == 0)
				receiver = null;
			if (lengthSender == 0)
				sender = null;
		} else if (type == SEND_FILE) {
			int lengthReceiver = 0;
			int lengthSender = 0;
			int lengthAddress = 0;
			int i = 3;
			while (string.charAt(i) != '#') {
				lengthReceiver = lengthReceiver * 10 + (string.charAt(i) - '0');
				i++;
			}
			i++;
			while (string.charAt(i) != '#') {
				lengthSender = lengthSender * 10 + (string.charAt(i) - '0');
				i++;
			}
			i++;
			while (string.charAt(i) != '#') {
				lengthAddress = lengthAddress * 10 + (string.charAt(i) - '0');
				i++;
			}
			receiver = string.substring(i + 1, i + 1 + lengthReceiver);
			i += lengthReceiver + 1;
			sender = string.substring(i + 1, lengthSender + i + 1);
			i += lengthSender + 1;
			address = string.substring(i + 1, lengthAddress + i + 1);
			port = 0;
			i += lengthAddress + 2;
			while (string.charAt(i) != '#') {
				port = port * 10 + (string.charAt(i) - '0');
				i++;
			}
			fileSize = 0;
			i++;
			while (string.charAt(i) != '#') {
				fileSize = fileSize * 10 + (string.charAt(i) - '0');
				i++;
			}
			fileName = string.substring(i + 1);
			if (lengthReceiver == 0)
				receiver = null;
			if (lengthSender == 0)
				sender = null;
		}
	}

	/**
	 * Message to string override
	 */
	public String toString() {
		if ((type != PRIVATE_MESSAGE) && (type != SEND_FILE) && (type != FILE_CANCELLED))
			return "#" + type + "#" + ((sender == null) ? 0 : sender.length()) + "#"
					+ ((body == null) ? 0 : body.length()) + "#" + sender + "#" + body;
		else if (type == PRIVATE_MESSAGE || type == FILE_CANCELLED)
			return "#" + type + "#" + ((receiver == null) ? 0 : receiver.length()) + "#" + sender.length() + "#"
					+ ((body == null) ? 0 : body.length()) + "#" + receiver + "#" + sender + "#" + body;
		else
			return "#" + type + "#" + receiver.length() + "#" + sender.length() + "#"
					+ ((address == null) ? 0 : address.length()) + "#" + receiver + "#" + sender + "#" + address + '#'
					+ port + '#' + fileSize + '#' + fileName;
	}

	/**
	 * @return message's body
	 */
	public String getBody() {
		return body;
	}

	/**
	 * @return message's sender
	 */
	public String getSender() {
		return sender;
	}

	/**
	 * @return type of the message
	 */
	public int getType() {
		return type;
	}

	/**
	 * @return receiver of the message
	 */
	public String getReceiver() {
		return receiver;
	}

	/**
	 * @return the name of the file
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * @return the port of the message
	 */
	public int getPort() {
		return port;
	}

	/**
	 * @return the address of the message
	 */
	public String getAddress() {
		return address;
	}

	/**
	 * @return the size of the file
	 */
	public int getFileSize() {
		return fileSize;
	}
}
