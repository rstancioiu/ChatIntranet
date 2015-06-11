package model;


/**
 * Classe Message
 * Au lieu d'envoyer des String, on envoie plutot des 'Messages', ce dernier est compose d'un type (selon l'info qu'il veut donner/demander)
 * un corps (l'info elle meme) et un expediteur
 * @see controller.Serveur
 * @see controller.Client
 * @see view.OngletConversationPrivee
 * @see view.OngletDiscussion
 * @see controller.Discussion
 */
public class Message {
    
    private String corps, expediteur, destinataire, nomFichier, adresse;
    private int port;
    private int type;
    private int tailleFichier;

    /**
     * constructeur de base
     * @param type
     * @param corps
     * @param expediteur
     * @parma destinataire
     */
    public Message(int type, String corps, String expediteur, String destinataire) {
        this.type = type;
        this.corps = corps;
        this.nomFichier = corps;
        this.expediteur = expediteur;
        this.destinataire = destinataire;
    }

    /**
     * surcharge constructeur
     * @param type
     * @param nomFichier
     * @param tailleFichier
     * @param port
     * @param adresse
     * @param expediteur
     * @param destinataire
     */
    public Message(int type, String nomFichier, int tailleFichier, int port, String adresse, String expediteur,
                   String destinataire) {
        this.type = type;
        this.adresse = adresse;
        this.port = port;
        this.tailleFichier = tailleFichier;
        this.nomFichier = nomFichier;
        this.expediteur = expediteur;
        this.destinataire = destinataire;
    }

    /**
     * constructeur qui permet de transformer un String en message
     * @param string
     */
    public Message(String string) {
        if (string.charAt(0) == '#')
            type = Integer.parseInt(string.substring(1, 2));
        if (type != MESSAGE_PRIVE && type != TRANSFERT_FICHIER && type != TRANSFERT_ANNULE) {
            for (int i = 3; i < string.length(); i++) {
                if (string.charAt(i) == '~') {
                    expediteur = string.substring(3, i);
                    corps = string.substring(i + 1);
                }
            }
        } else if (type == MESSAGE_PRIVE || type == TRANSFERT_ANNULE) {
            int j = 0;
            int k = 0;
            for (int i = 3; i < string.length(); i++) {
                if (string.charAt(i) == '#') {
                    if (k == 0) {
                        destinataire = string.substring(3, i);
                        j = i + 1;
                        k++;
                    }
                }
                if (string.charAt(i) == '~') {
                    expediteur = string.substring(j, i);
                    corps = string.substring(i + 1);
                    nomFichier=string.substring(i+1);
                }
            }
        } else if (type == TRANSFERT_FICHIER) {
            int p = 0;
            int j = 0;
            for (int i = 3; i < string.length(); i++) {
                if (string.charAt(i) == '#') {
                    destinataire = string.substring(3, i);
                    j = i + 1;
                }

                if (string.charAt(i) == '~') {
                    if (p == 0) {
                        expediteur = string.substring(j, i);
                        j = i + 1;
                        p++;
                    }
                    else if (p == 1) {
                        adresse = string.substring(j, i);
                        j = i + 1;
                        p++;
                    }
                    else if (p == 2) {
                        port = Integer.parseInt(string.substring(j, i));
                        j = i + 1;
                        p++;
                    }
                    else if (p == 3) {
                        tailleFichier = Integer.parseInt(string.substring(j, i));
                        nomFichier = string.substring(i + 1);
                        p++;
                    }
                }
            }
        }
    }

    /**
     * getter contenu message
     * @return corps du message
     */
    public String getCorps() {
        return corps;
    }

    /**
     * getter expediteur
     * @return expediteur
     */
    public String getExpediteur() {
        return expediteur;
    }

    /**
     * getter type
     * @return le type de message
     */
    public int getType() {
        return type;
    }

    /**
     * getter destinataire
     * @return le destinataire
     */
    public String getDestinataire() {
        return destinataire;
    }

    /**
     * Redefinition de toString : un message est envoye via TCP sous forme de string pour etre ensuite retransforme en Message
     * grace au deuxieme constructeur
     * @return le toString du message
     */
    public String toString() {
        if ((type != MESSAGE_PRIVE) && (type != TRANSFERT_FICHIER) && (type != TRANSFERT_ANNULE))
            return "#" + type + "#" + expediteur + "~" + corps;
        else if (type == MESSAGE_PRIVE||type == TRANSFERT_ANNULE)
            return "#" + type + "#" + destinataire + "#" + expediteur + "~" + corps;
        else
            return "#" + type + "#" + destinataire + "#" + expediteur + "~" + adresse + '~' + port + '~' +
                tailleFichier + '~' + nomFichier;
    }

    /**
     * getter nom du fichier envoye
     * @return le nom du fichier
     */
    public String getNomFichier() {
        return nomFichier;
    }

    /**
     * getter port d'envoi du message
     * @return le port
     */
    public int getPort() {
        return port;
    }

    /**
     * getter adresse
     * @return adresse
     */
    public String getAdresse() {
        return adresse;
    }

    /**
     * getter taille du fichier
     * @return la taille du fichier
     */
    public int getTailleFichier() {
        return tailleFichier;
    }
    
    ///Constantes pour les types de messages///
    public static final int NOUVEAU_UTILISATEUR = 0;
    public static final int MESSAGE = 1;
    public static final int MESSAGE_PRIVE = 2;
    public static final int QUITTER = 3;
    public static final int HOST_QUITTER = 4;
    public static final int TRANSFERT_FICHIER = 5;
    public static final int TRANSFERT_ANNULE = 6;
}
