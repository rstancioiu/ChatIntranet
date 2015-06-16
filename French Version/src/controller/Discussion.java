package controller;

import model.InfoServeur;
import model.Message;

import view.OngletDiscussion;

import java.awt.Color;

import java.awt.Dimension;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import java.net.Socket;

import java.text.SimpleDateFormat;

import java.util.Date;

import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import javax.xml.crypto.Data;

import model.Aes;

/**
 * Classe Discussion. Elle herite de JTextPane et implemente Runnable(Thread).
 * L'envoi des messages se realise sur des Sockets de type TCP.
 * Elle met a jour les messages recus par l'utilisateur . Ainsi, chaque type de
 * message recu est traite dans cette classe.
 * @see Message
 * @see OngletDiscussion
 * @see Socket
 * @see JTextPane
 */
public class Discussion extends JTextPane implements Runnable {
    
    private OngletDiscussion ongletDiscussion;
    private Socket socket;
    private PrintWriter fluxSortant;
    private BufferedReader fluxEntrant;
    private Message message;
    private Aes aes = new Aes();
    
    // Attributs lies au formatage du texte
    private StyledDocument doc;
    private Style monpseudo;
    private Style pseudo;
    private Style corps;
    private Style informations;
    private Style erreur;
    
    private DefaultListModel model = new DefaultListModel();
    private boolean quitter = false;
    
    
     /**
     * Constructeur de la classe
     * @param infos
     * @param od
     */
    public Discussion(InfoServeur infos, OngletDiscussion od) {
        this.setSize(new Dimension(450, 300));
        doc = (StyledDocument) this.getDocument();
        this.ongletDiscussion = od;
        this.setEditable(false);
        od.getListeUtilisateurs().setModel(model);
        model.addElement("  Liste des utilisateurs  ");
        // Declarations des Styles
        erreur = this.addStyle("erreur", null);
        StyleConstants.setForeground(erreur, Color.RED);
        monpseudo = this.addStyle("moi", null);
        StyleConstants.setForeground(monpseudo, Color.RED);
        pseudo = this.addStyle("expediteur", null);
        StyleConstants.setForeground(pseudo, Color.BLUE);
        corps = this.addStyle("corps du message", null);
        StyleConstants.setFontSize(corps, 12);
        StyleConstants.setFontSize(pseudo, 13);
        StyleConstants.setFontSize(monpseudo, 13);
        StyleConstants.setFontSize(erreur, 14);
        informations = this.addStyle("", null);
        /*
         * creation de la liaison tcp et communication avec la classe Serveur
         */
        try {
            socket = new Socket(infos.getAdresse(), infos.getPort());
            envoyer(new Message(Message.NOUVEAU_UTILISATEUR, null, od.getPseudo(), null));
            insererLigne("Vous etes maintenant connecte. Bienvenue !", informations, true);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Impossible de se connecter au serveur :'(", "Le serveur ne repond pas",
                                          JOptionPane.ERROR_MESSAGE);
            insererLigne("LA CONNEXION AVEC L'HOST DISTANT N'A PAS PU ETRE EFFECTUEE", erreur, false);
            quitter = true;
            od.quitter();
        }
    }

    /**
     * Methode run override de Runnable, permet de recevoir les differents types
     * de messages et de les ajouter a la discussion en fonction de leur type.
     */
    public void run() {
        while (!quitter) {
            try {
                fluxEntrant = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String s = fluxEntrant.readLine();
                if (s != null) {
                    s= aes.decrypt(s, 1);
                    message = new Message(s);
                    if (message.getType() == Message.MESSAGE) {
                        if (message.getExpediteur().equals(ongletDiscussion.getPseudoLabel().getText())) {
                            insererLigne(message.getExpediteur(), monpseudo, true);
                        } else {
                            insererLigne(message.getExpediteur(), pseudo, true);
                        }
                        insererLigne(" : " + message.getCorps(), corps, false);
                        ongletDiscussion.scrollerBasDiscussion();
                    } else if (message.getType() == Message.NOUVEAU_UTILISATEUR) {

                        if (message.getCorps().equals("nouveau")) {
                            model.addElement(message.getExpediteur());
                            insererLigne( message.getExpediteur() + " vient de se connecter", informations, true);
                            ongletDiscussion.scrollerBasDiscussion();
                        } else if (message.getCorps().equals("pseudo")) {
                            insererLigne( "Le pseudo \"" + ongletDiscussion.getPseudoLabel().getText() +
                                         "\" etait deja pris, votre nouveau pseudo est desormais " +
                                         message.getExpediteur() + " !", informations, true);
                            ongletDiscussion.setPseudoLabel(message.getExpediteur());
                        } else
                            model.addElement(message.getExpediteur());
                    } else if (message.getType() == Message.QUITTER) {
                        model.removeElement(message.getExpediteur());
                        insererLigne(message.getExpediteur() + " a quitte la room", informations, true);
                        ongletDiscussion.scrollerBasDiscussion();
                    } else if (message.getType() == Message.MESSAGE_PRIVE) {
                        ongletDiscussion.updateCP(message);
                    } else if (message.getType() == Message.HOST_QUITTER) {
                        insererLigne("L'host a quitte la room, il n'y a plus rien a faire ici...", erreur, true);
                        JOptionPane.showMessageDialog(null,
                                                      "L'hebergeur a arrete la discussion '" +
                                                      ongletDiscussion.getNomDiscussion() + "'", ":'(",
                                                      JOptionPane.ERROR_MESSAGE);
                    } else if (message.getType() == Message.TRANSFERT_FICHIER ||
                               message.getType() == Message.TRANSFERT_ANNULE) {
                        ongletDiscussion.updateCP(message);
                    }
                }
            } catch (Exception e) {
                insererLigne("Une erreur est survenue lors de la reception des messages - Deconnecte", erreur, true);
                quitter=true;
            }
        }
        try {
            socket.close();
        } catch (IOException e) {
            System.out.println("impossible de fermer le socket");
        } catch (NullPointerException npe) {
            System.out.println("socket ferme");
        }
    }

    /**
     * Methode qui envoie les messages vers le serveur(les autres utilisateurs).
     * @param message
     */
    public void envoyer(Message message) {
        try {
            fluxSortant = new PrintWriter(socket.getOutputStream());
            String s = aes.encrypt(message.toString(), 1);
            fluxSortant.println(s);
            fluxSortant.flush();
        } catch (Exception e) {
            insererLigne("Erreur lors de l'envoi du message", erreur, false);
        }
    }

    /**
     * Methode qui envoie un message de type 3 vers le serveur
     * quand l'utilisateur se deconnecte
     */
    public void quitter() {
        this.envoyer(new Message(3, null, ongletDiscussion.getPseudo(), null));
        quitter = true;
    }

    /**
     * insere une ligne en fin du texte en gerant l'exception
     * @param texte
     * @param style
     * @param heure
     */
    public void insererLigne(String texte, Style style, boolean heure) {
        try {
            if (heure) {
                SimpleDateFormat h = new SimpleDateFormat("hh:mm");
                String heureLocale = h.format(new Date());
                doc.insertString(doc.getLength(), "\n[" + heureLocale + "]   ", informations);
            } 
                doc.insertString(doc.getLength(),texte, style);
        } catch (BadLocationException e) {
            setText(getText() + "\nUne erreur bizarre s'est produite...");
        }
    }
}
