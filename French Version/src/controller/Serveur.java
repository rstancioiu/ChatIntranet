package controller;

import model.InfoServeur;
import model.Message;

import java.io.IOException;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import java.util.concurrent.TimeUnit;

/**
 * serveur de l'application, recoit les messages et les redistribue aux utilisateurs. Il utilise la classe
 * controller.Client et il attribue a chaque utilisateur un objet client pour le reconnaitre.
 * thread implements runnable
 * @see controller.Client
 * @see controller.RecevoirBroadcast
 * @see model.InfoServeur
 * @see model.Message
 * @see Socket
 * @see ServerSocket
 */
public class Serveur implements Runnable {
    
    //partie TCP
    private boolean deconnection = false;
    private Client[] clients;
    private ServerSocket socketserveur;
    
    //partie UDP
    private Thread repondreBroadcast;
    private RecevoirBroadcast recBroadcast;
    
    //infos du serveur TCP
    private String nom;
    private int capacite;
    
    //infos du partie UDP
    private InfoServeur infos;
    private String adresse;
    private int port;
    private String type;
    private String motDePasse;
    private String host;


   /**
     * creer un serveur, on peut lui donner une capacite et limiter son acces par mdp
     * @param nom
     * @param host
     * @param capacite
     * @param type
     * @param motDePasse
     * @throws IOException
     */
    public Serveur(String nom, String host, int capacite, String type, String motDePasse) throws IOException {
        if (capacite <= 0) {
            this.capacite = 25;
        } //capacite par defaut du serveur
        else {
            this.capacite = capacite;
        }
        clients = new Client[this.capacite];
        this.nom = nom;
        this.type = type;
        this.host = host;
        this.motDePasse = motDePasse;

        socketserveur = new ServerSocket(0, capacite, InetAddress.getLocalHost());
        InetAddress adresseDuServeur = socketserveur.getInetAddress();
        this.infos =
                new InfoServeur(this.nom, getNbClients(), this.capacite, adresseDuServeur.getHostAddress(), socketserveur.getLocalPort(),
                                this.host, this.type);
        this.port = socketserveur.getLocalPort();
        this.adresse = adresseDuServeur.toString();
        recBroadcast = new RecevoirBroadcast(infos, motDePasse);
        repondreBroadcast = new Thread(recBroadcast);
        repondreBroadcast.start();
    }

    /**
     * run
     */
    public void run() {
        while (!deconnection) {
            try {
                ajouterClient(socketserveur.accept());
            } catch (IOException e) {
                System.out.println("e2.1");
            }
        }
        try {
            socketserveur.close();
        } catch (IOException e) {
            System.out.println("e2.2");
        }
    }

    /**
     * getter de la socket serveur
     * @return la socket
     */
    public ServerSocket getSocketserveur() {
        return socketserveur;
    }

    /**
     * connecte un Client au serveur en cr�ant un socket � partir du socketserver et l'ajoute dans le tableau de clients
     * @param socket
     * @throws IOException
     */
    public void ajouterClient(Socket socket) throws IOException {
        if (!deconnection) {
            Client client = new Client(this, socket);
            for (int i = 0; i < clients.length; i++) {
                if (clients[i] == null) {
                    clients[i] = client;
                    infos.setClients(getNbClients());
                    Thread t = new Thread(client);
                    t.start();
                    break;
                }
            }
        }
    }

    /**
     *  utilise la methode envoyer de chaque Client contenu dans le tableau
     * @param message
     * @param sauflui
     */
    public void envoyerAtous(Message message, Client sauflui) {
        try {
            for (int i = 0; i < clients.length; i++) {
                if (clients[i] != null && clients[i] != sauflui) {
                    clients[i].envoyer(message);
                }
            }
        } catch (Exception e) {
            System.out.println("e3");
        }
    }

    /**
     *utilise la methode envoyer de Client pour envoyer un message prive
     * @param message
     */
    public void envoyerMessagePrive(Message message) {
        try {
            for (int i = 0; clients[i] != null; i++) {
                if (clients[i].getNom().equals(message.getDestinataire())) {
                    clients[i].envoyer(message);
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println("e4");
        }
    }

    /**
     * envoie au "client" des messages de type 0 contenant les pseudos des autres clients contenus dans le tableau
     * @param client
     */
    public void envoyerListe(Client client) {
        try {
            for (int i = 0; i < clients.length; i++) {
                if (clients[i] != null) {
                    TimeUnit.MILLISECONDS.sleep(120);
                    client.envoyer(new Message(Message.NOUVEAU_UTILISATEUR, null, clients[i].getNom(), null));
                }
            }
        } catch (Exception e) {
            System.out.println("e5.1");
        }
    }

    /**
     *supprime le client du tableau
     * @param client
     */
    public void supprimer(Client client) {
        for (int i = 0; i < clients.length; i++) {
            if (clients[i] != null && clients[i].equals(client)) {
                clients[i].setRunning(false);
                clients[i] = null;
                infos.setClients(getNbClients());
            }
        }
    }

    /**
     * compte le nombre de clients contenus dans le tableau
     * @return le nombre de clients
     */
    public int getNbClients() {
        int nb = 0;
        for (int i = 0; i < clients.length; i++) {
            if (clients[i] != null)
                nb++;
        }
        return nb;
    }

    /**
     * verifie la presence d'un client du serveur
     * @param c
     * @return si le client est deja present
     */
    public boolean contains(Client c) {
        for (int i = 0; i < clients.length; i++) {
            if (clients[i] != null && c.equals(clients[i]) && clients[i] != c)
                return true;
        }
        return false;
    }

    /**
     * permet d'arreter le serveur proprement
     */
    public void arreter() { // CLOSE ALL THE SOCKETS !
        envoyerAtous(new Message(4, null, null, null), null);
        deconnection = true;
        recBroadcast.setRunning(false);
        for (int i = 0; i < clients.length; i++) {
            if (clients[i] != null) {
                clients[i].setRunning(false);
                clients[i] = null;
            }
        }
          try {
            socketserveur.close();
        } catch (IOException e) {
            System.out.println("e6");
        }

    }
   
    /**
     * getter des infos sur le serveur
     * @return l'infoServeur
     */
    public InfoServeur getInfos() {
        return infos;
    }

    /**
     * getter du nom du serveur
     * @return le nom du seveur
     */
    public String getNom() {
        return nom;
    }

    /**
     * getter de l'adresse du serveur
     * @return l'adresse du serveur
     */
    public String getAdresse() {
        return adresse;
    }

    /**
     * getter du port utilise par le serveur
     * @return le port
     */
    public int getPort() {
        return port;
    }
    
    // Constantes
    public static final int PORT_DE_BROADCAST = 13371;
}

