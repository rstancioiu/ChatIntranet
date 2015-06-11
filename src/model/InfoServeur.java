package model;

/**
 * Classe InfoServeur qui gere les informations du serveur en fonction des parametres generaux
 * @see controller.Broadcaster
 * @see controller.RecevoirBroadcast
 * @see controller.Serveur
 * @see view.OngletRooms
 */
public class InfoServeur {
    
    private String nom, adresse, host, type;
    private int clients, clientsMax;
    private int port;

    /**
     * constructeur utilise par le client
     * @param nom
     * @param clients
     * @param clientsMax
     * @param adresse
     * @param port
     * @param host
     * @param type
     */
    public InfoServeur(String nom, int clients, int clientsMax, String adresse, int port, String host, String type) {
        this.adresse = adresse;
        this.clients = clients;
        this.clientsMax = clientsMax;
        this.host = host;
        this.nom = nom;
        this.port = port;
        this.type = type;
    } 

     /**
     * constructeur qui permet de convertir un String en InfoServeur
     * @param infos
     */
    public InfoServeur(String infos) { 
        int indice = 0;
        int numAttribut = 0;
        for (int i = 0; i < infos.length(); i++) {
            if (infos.charAt(i) == '~') {
                if (numAttribut == 0) {
                    nom = infos.substring(0, i);
                    indice = i + 1;
                    numAttribut++;
                } else if (numAttribut == 1) {
                    adresse = infos.substring(indice, i);
                    indice = i + 1;
                    numAttribut++;
                } else if (numAttribut == 2) {
                    port = Integer.parseInt(infos.substring(indice, i));
                    indice = i + 1;
                    numAttribut++;
                } else if (numAttribut == 3) {
                    clients = Integer.parseInt(infos.substring(indice, i));
                    indice = i + 1;
                    numAttribut++;
                } else if (numAttribut == 4) {
                    clientsMax = Integer.parseInt(infos.substring(indice, i));
                    indice = i + 1;
                    numAttribut++;
                } else if (numAttribut == 5) {
                    host = infos.substring(indice, i);
                    type = infos.substring(i + 1, infos.length() - 2);
                    numAttribut++;
                    break;
                }
            }
        }
    }
    
    /**
     * surcharge du constructeur utilise par le client
     * @param nom
     * @param adresse
     * @param port
     * @param host
     * @param type
     */
    public InfoServeur(String nom, String adresse, int port, String host, String type) {
        this.nom = nom;
        this.adresse = adresse;
        this.port = port;
        this.type = type;
        this.host = host;
    } 

    // Accesseurs & setters
    
    /**
     * getter de type du serveur
     * @return type
     */
    public String getType() {
        return type;
    }

    /**
     * getter de nom du serveur
     * @return nom
     */
    public String getNom() {
        return nom;
    }
    
    /**
     * getter de nombre de clients connecte au serveur
     * @return clients;
     */
    public int getClients() {
        return clients;
    }
    
    /** 
     * getter de nombre maximal de Clients qui peuvent se connecter au serveur
     * @return clientsMax
     */
    public int getClientsMax() {
        return clientsMax;
    }

    /**
     * getter de l'addresse du serveur
     * @return addresse
     */
    public String getAdresse() {
        return adresse;
    }
    
    /**
     * getter de port du serveur
     * @return port
     */
    public int getPort() {
        return port;
    }
     
    /**
     * getter de l'hote du serveur
     * @return host
     */
    public String getHost() {
        return host;
    }

    /**
     * setter du nombre de clients du serveur
     * @param clients
     */
    public void setClients(int clients) {
        this.clients = clients;
    }

    /**
     * redefinition de la methode equals : si deux infoserveurs ont le meme nom, la meme adresse et le meme port alors
     * ils sont identiques.
     * @param infs
     * @return
     */
    public boolean equals(InfoServeur infs) {
        if (nom.equals(infs.getNom()) && adresse.equals(infs.getAdresse()) && port == infs.getPort() &&
            host.equals(infs.getHost()))
            return true;
        else
            return false;
    }

    /**
     * Redefinition de toString : cette presentation textuelle est utilisee par la jlist de OngletRoom qui contient des
     * InfoServeur
     * @return
     */
    public String toString() {
        String s = nom;
        if (nom.length() > 30) {
            s = nom.substring(0, 27) + "...";

        } else {
            s = nom;
            for (int i = 0; i < 30 - s.length(); i++) {
                s = s + " ";
            }
        }

        return s + "          (" + clients + "/" + clientsMax + ")" + "    " + type + "          Host : " + host;
    }

    /**
     * Methode utilisee pour envoyer via UDP un InfoServeur sous forme de String, pour etre ensuite retransformee en
     * InfoServeur via le constructeur String ----> InfoServeur
     * @return le string des donnees
     */
    public String transfertDonnees() {
        return nom + '~' + adresse + '~' + port + '~' + clients + '~' + clientsMax + '~' + host + '~' + type + "~~";
    }
}
