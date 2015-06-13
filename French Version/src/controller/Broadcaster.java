package controller;

import model.InfoServeur;

import view.OngletRooms;


import java.io.IOException;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

import java.net.InetAddress;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import java.util.concurrent.TimeUnit;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.SwingUtilities;

import view.ListeServeur;

/**
 * Class Broadcaster permet l'envoi de broadcast et du mot de passe insere
 * par le client vers le serveur. On utilise des Sockets de type UDP, en
 * principal socket DatagramSocket et DatagramPacket pour les packets
 * envoyes et recus. La classe Broadcaster est un thread qui herite de JList et qui
 * est utilise dans la FentreRooms.Ainsi on utilise DefaultModelList pour ajouter
 * une liste de InfoServeur a JList. Comme java swing n'est pas threadsafe, on utilise
 * invokeLater de cette methode (un thread) pour gerer la mise a jour de la jliste
 * quand il y a un nouveau serveur. Pour l'affichage de la JList on utilise la methode
 * InfoServeur.toString() qui se trouve dans la classe InfoServeur. On utilise des timers
 * pour envoyer les broadcasts et pour mettre a jour la JListe
 * @see model.InfoServeur
 * @see view.OngletRooms
 * @see JList
 * @see DatagramPacket
 * @see DatagramSocket
 */

public class Broadcaster implements Runnable {
    @SuppressWarnings("compatibility:8646387119641877285")
    private static final long serialVersionUID = 1L;

    private DatagramPacket packetBroadcast; //packet broadcaste
    private DatagramPacket packetReponse; //packet recu
    private DatagramSocket socket; // socket de type UDP
    private static final String PW_ACCEPTED = "wowsuchpassword~~";
    private static final String DISCOVERY = "youwutm8~~";
    private byte[] data = DISCOVERY.getBytes(); //message envoye en tableau de bytes
    
    /*timers utilise pour updater la jliste*/
    private Timer timer;
    private Timer timerRefreshJlist;
    
    /*String de message recu du serveur*/
    private String messageRecu;
    
    private boolean connectionAccepte; //envoie vrai si la connection vers le serveur a ete admise
    private byte[] recu = new byte[512]; // tableau de byte qui contient le message recu
    
    private DatagramPacket packetMDP; // datagramPacket utilise pour envoyer le mot de passe
    /*confirmation du serveur vers le client qu'il a bien envoye le mot de passe correct*/
    private String accepteMdp;
    /*arrayList d'InfoServeur qui est ajoute a la jliste*/
    
    private Boolean running = true;
    private OngletRooms ongletroom;
    private InfoServeur infos;
    private ListeServeur listeServeur;
    

    /**
     * Constructeur de la classe Broadcaster ou sont definis la connection Socket
     * et les broadcasts envoyes et recus.
     * @param or
     */
    public Broadcaster(OngletRooms or,ListeServeur listeServeur) throws IOException {
        this.ongletroom = or;
        connectionAccepte = false;
        this.listeServeur=listeServeur;
        /* creation de la connection Socket*/
        socket = new DatagramSocket();
        socket.setBroadcast(true);
        packetBroadcast =
            new DatagramPacket(data, data.length, InetAddress.getByName("255.255.255.255"), Serveur.PORT_DE_BROADCAST);
        /* paquet ou l'on "stocke" la reponse recue*/
        packetReponse = new DatagramPacket(recu, recu.length);
    }

    /**
     * Methode obligatoire d'un thread qui s'occupe de la gestion de l'envoi et de la reception
     * des broadcasts et du mot de passe
     */
    public void run() {
        /*
         * timers utilises pour envoyer les broadcasts et actualiser la liste
         */
        timer = new Timer();
        timer.schedule(new envoyerPacketBroadcast(), 0, 1200);
        timerRefreshJlist = new Timer();
       
        /*boucle infinie pour recevoir des broadcasts*/
        while (running) {
            try {
                socket.receive(packetReponse);
                messageRecu = new String(packetReponse.getData());
                for (int i = 1; i < messageRecu.length(); i++) {
                    if (messageRecu.charAt(i) == '~' && messageRecu.charAt(i - 1) == '~') {
                        messageRecu = messageRecu.substring(0, i + 1);
                        break;
                    }
                }
                if (messageRecu.equals(PW_ACCEPTED)) {
                    connectionAccepte = true;
                    TimeUnit.MILLISECONDS.sleep(300);
                } else {
                    infos = new InfoServeur(messageRecu);
                    listeServeur.addServeur(infos);
                    connectionAccepte = false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("ERROR WHILE BROADCASTING");
            }
        }
    }

    /**
     * Methode qui envoie le string mdp (mot de passe) vers le serveur pour verification
     * @param mdp
     */
    public void envoyerMdp(String mdp) throws IOException {
        byte[] data2 = (mdp + "~~").getBytes();
        packetMDP =
            new DatagramPacket(data2, data2.length,
                               InetAddress.getByName(listeServeur.getAdresseByIndex(ongletroom.getListeIndexSelecte())),
                               Serveur.PORT_DE_BROADCAST);
        socket.send(packetMDP);
    }


    /**
     * Classe envoyer qui herite de TimerTask qui envoie les broadcasts
     * utilise par le Timer timer (envoie des broadcasts a chaque X secondes)
     */
    private class envoyerPacketBroadcast extends TimerTask {
        public void run() {
            try {
                socket.send(packetBroadcast); // envoie de packet
            } catch (IOException e) {
                socket.close();
                System.out.println("Broadcast intterompu");
            }
        }
    }


    
    /**
     * accesseur util dans la classe fenetreRooms pour verifier si la connection a ete accepte
     */
    public boolean isConnectionAccepte() {
        return connectionAccepte;
    }
    
    /**
     * accesseur qui permet de changer le boolean connectionAccepte a l'interieur
     * de la FenetreRoom
     * @param connectionAccepte
     */
    public void setConnectionAccepte(boolean connectionAccepte) {
        this.connectionAccepte = connectionAccepte;
    }
}

