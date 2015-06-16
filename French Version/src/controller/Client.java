package controller;

import model.Message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import java.net.Socket;

import java.util.concurrent.TimeUnit;

import model.Aes;

/**
 * La classe Client est un objet attribue a chaque utilisateur connecte au serveur.
 * C'est l'outil utilise qui repond au demande d'utilisateurs.
 * Elle est implemente de Runnable (c'est un Thread) et est utilisee par
 * la classe Serveur pour envoyer les differents types de messages vers les utilisateurs.
 * Cette classe concerne seulement le serveur et non le client(utilisateur).
 * @see controller.Serveur
 * @see model.Message
 * @see Socket
 */  
public class Client implements Runnable {
    
    private Serveur serveur;
    private Socket socket;
    private PrintWriter fluxSortant; //transforme le flux sortant dans un text-output
    private BufferedReader fluxEntrant; //lire le flux entrant
    private Aes aes = new Aes();
    
    private String nom;
    private boolean running = true;
    

    /**
     * constructeur de la classe Client
     * @param serveur
     * @param socket
     */
    public Client(Serveur serveur, Socket socket) {
        this.serveur = serveur;
        this.socket = socket;
    }

    /**
     * Methode run qui s'occupe de la gestion des messages recus par le serveur pour les envoyer vers un ou
     * tous les utilisateurs en fonction du type du message recu.
     */
    public void run() {
        try {
            while (running) {
                fluxEntrant = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String s = fluxEntrant.readLine();
                if (s != null) {
                    s = aes.decrypt(s, 1);
                    Message message = new Message(s);
                    /* le message envoye par un utilisateur et recu par le
                     * serveur est renvoye vers tous les utilisateurs*/
                    if (message.getType() == Message.MESSAGE) {
                        serveur.envoyerAtous(message, null);
                    }
                    /* envoi du message de confirmation de la connection d'un nouvel utilisateur vers tous
                     * les autres utilisateurs.
                     * Et mise a jour de la liste des utilisateurs de la discussion
                     */
                    else if (message.getType() == Message.NOUVEAU_UTILISATEUR) {
                        nom = message.getExpediteur();
                        boolean dejapris = false;
                        while (serveur.contains(this)) {
                            nom = nom + "_";
                            dejapris = true;
                        }
                        serveur.envoyerAtous(new Message(Message.NOUVEAU_UTILISATEUR, "nouveau", nom, null), this);
                        serveur.envoyerListe(this);
                        TimeUnit.MILLISECONDS.sleep(200);
                        if (dejapris)
                            envoyer(new Message(Message.NOUVEAU_UTILISATEUR, "pseudo", nom, null));
                    }
                    /*envoi du message de sortie d'un utilisateur vers tous les autres
                     * utilisateurs. L'utilisateur qui part est suprime de la liste de discussion
                     */
                    else if (message.getType() == Message.QUITTER) {
                        serveur.envoyerAtous(new Message(3, null, nom, null), null);
                        serveur.supprimer(this);
                        running = false;
                    } else if (message.getType() == Message.MESSAGE_PRIVE) {
                        serveur.envoyerMessagePrive(message);
                    } else if (message.getType() == Message.TRANSFERT_FICHIER) {
                        serveur.envoyerMessagePrive(message);
                    } else if (message.getType() == Message.TRANSFERT_ANNULE) {
                        serveur.envoyerMessagePrive(message);
                    }
                }
            }
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
            running = false;
        }
    }

    /**
     * Methode qui envoie un message vers un Client.
     * Elle est utilisee dans la methode
     * envoyerAtous et envoyerListe de la classe Serveur
     * @param message
     * @throws IOException
     */
    public void envoyer(Message message) {
        try {
            fluxSortant = new PrintWriter(socket.getOutputStream());
            String s=aes.encrypt(message.toString(), 1);
            fluxSortant.println(s);
            fluxSortant.flush();
            } catch (Exception e) {
            System.out.println("Une erreur a ete produite");
        }
    }

    /**
     * Methode qui renvoie un boolean. Elle est utilisee pour verifier si le client appartient
     * a la liste de clients du serveur.
     * @param client
     * @return appartenance du client a la liste de clients du serveur
     */
    public boolean equals(Client client) {
        if (nom.equals(client.getNom())) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Getter utilise pour renvoyer le nom du client 
     * @return nom du Client
     */
    public String getNom() {
        return nom;
    }
    
    /**
     * Setter utilise pour arreter l'envoi du serveur vers un certain client
     * @param running
     */
    public void setRunning(boolean running) {
        this.running = running;
    }
}
