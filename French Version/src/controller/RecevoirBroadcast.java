package controller;

import model.InfoServeur;

import java.io.IOException;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Classe RecevoirBroadcast permet au serveur controller.Serveur de recevoir le broadcast envoye par le Broadcaster et 
 * de repondre a l'envoyeur une reponse. Elle est un Thread, implements de Runnable.
 * @see controller.Serveur
 * @see model.InfoServeur
 * @see controller.Broadcaster
 * @see DatagramSocket
 * @see DatagramPacket
 */
public class RecevoirBroadcast implements Runnable {
    
    private DatagramSocket socketUdp;
    private DatagramPacket packetReponseMdp; // packet qui va etre envoye lie au mot de passe
    private DatagramPacket packetReponseInfos; // packet qui va etre envoye normalement-info serveur
    private DatagramPacket packetRecu;
    
    private byte[] donneesRecues = new byte[300];
    private InfoServeur infos;
    private InetAddress adresseClient;
    private String motDePasse;
    private String messageRecu;
    private boolean running = true;
    public static final String PW_ACCEPTED = "wowsuchpassword~~";
    public static final String DISCOVERY = "youwutm8~~";


    /**
     * Constructeur de la classe.
     * repond au broadcast en donnant ses infos + repond positif pour la demande de verification 
     * du mot de passe (mdp)
     * @param infos
     * @param motDePasse
     * @throws IOException
     */
    public RecevoirBroadcast(InfoServeur infos, String motDePasse) throws IOException {
        this.infos = infos;
        this.motDePasse = motDePasse;
        socketUdp = new DatagramSocket(Serveur.PORT_DE_BROADCAST); // connection de type UDP
        socketUdp.setBroadcast(true); // ouverture de la connection
        packetRecu = new DatagramPacket(donneesRecues, donneesRecues.length);
    }

    /**
     * run du Runnable, recoit en permanence les broadcasts sur le port l3371
     * si message recu et demande d'infos (= messageAttendu) alors on lui envoie nos infos
     * si message recu et demande de verification de mdp alors on compare et on renvoie reponse oui/non
     */
    public void run() {
        try {
            while (running) {
                socketUdp.receive(packetRecu); // reception du packet
                adresseClient = packetRecu.getAddress(); // stockage de l'adresse du packet
                messageRecu = new String(packetRecu.getData());
                for (int i = 1; i < messageRecu.length(); i++) {
                    if (messageRecu.charAt(i) == '~' && messageRecu.charAt(i - 1) == '~') {
                        messageRecu = messageRecu.substring(0, i + 1);
                        break;
                    }
                }
                if (messageRecu.equals(motDePasse + "~~") &&
                    infos.getType() == "prive") { //comparaison du message recu aux motDePasse
                    packetReponseMdp =
                        new DatagramPacket(PW_ACCEPTED.getBytes(), (PW_ACCEPTED.getBytes()).length,
                                           adresseClient, packetRecu.getPort());
                    socketUdp.send(packetReponseMdp);
                } else if (messageRecu.equals(DISCOVERY)) {
                    packetReponseInfos =
                        new DatagramPacket((infos.transfertDonnees()).getBytes(),
                                           ((infos.transfertDonnees()).getBytes()).length, adresseClient,
                                           packetRecu.getPort());
                    socketUdp.send(packetReponseInfos); // envoi de la reponse vers l'adresse+port du packet recu
                }
            }
            socketUdp.close();
        } catch (Exception e) {
            System.out.println("ServerBroadcast timed out");
            running = false;
            socketUdp.close();
        }
    }

    /**
     * setter de running pour le thread
     * @param running
     */
    public void setRunning(boolean running) {
        this.running = running;
    }

    /**
     * Methode utilisee par le serveur pour mettre a jour les infos
     * @param infos
     */
    public void setInfos(InfoServeur infos) {
        this.infos = infos;
    }
}
