package model;

/**
 * Classe InfoServeur contains the informations of a server
 * 
 * @author Afkid
 */
public class InformationServer {

	private String name, address, host, type;
	private int clients, clientsMax;
	private int port;

	public InformationServer(String name, int clients, int clientsMax, String address, int port, String host,
			String type) {
		this.address = address;
		this.clients = clients;
		this.clientsMax = clientsMax;
		this.host = host;
		this.name = name;
		this.port = port;
		this.type = type;
	}

	public InformationServer(String infos) {
		int indice = 0;
		int number = 0;
		for (int i = 0; i < infos.length(); i++) {
			if (infos.charAt(i) == '~') {
				if (number == 0) {
					name = infos.substring(0, i);
					indice = i + 1;
					number++;
				} else if (number == 1) {
					address = infos.substring(indice, i);
					indice = i + 1;
					number++;
				} else if (number == 2) {
					port = Integer.parseInt(infos.substring(indice, i));
					indice = i + 1;
					number++;
				} else if (number == 3) {
					clients = Integer.parseInt(infos.substring(indice, i));
					indice = i + 1;
					number++;
				} else if (number == 4) {
					clientsMax = Integer.parseInt(infos.substring(indice, i));
					indice = i + 1;
					number++;
				} else if (number == 5) {
					host = infos.substring(indice, i);
					type = infos.substring(i + 1, infos.length() - 2);
					number++;
					break;
				}
			}
		}
	}

	public InformationServer(String name, String address, int port, String host, String type) {
		this.name = name;
		this.address = address;
		this.port = port;
		this.type = type;
		this.host = host;
	}

	public boolean equals(InformationServer infs) {
		if (name.equals(infs.getName()) && address.equals(infs.getAdrdess()) && port == infs.getPort()
				&& host.equals(infs.getHost()))
			return true;
		else
			return false;
	}

	public String sendData() {
		return name + '~' + address + '~' + port + '~' + clients + '~' + clientsMax + '~' + host + '~' + type + "~~";
	}

	public String getType() {
		return type;
	}

	public String getName() {
		return name;
	}

	public int getClients() {
		return clients;
	}

	public int getClientsMax() {
		return clientsMax;
	}

	public String getAdrdess() {
		return address;
	}

	public int getPort() {
		return port;
	}

	public String getHost() {
		return host;
	}

	public void setClients(int clients) {
		this.clients = clients;
	}
}
