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
            int lengthExpediteur=0;
            int lengthCorps=0;
            int i=3;
            while(string.charAt(i)!='#') {
                lengthExpediteur = lengthExpediteur*10 +( string.charAt(i)-'0');
                i++;
            }
            i++;
            while(string.charAt(i)!='#') {
                lengthCorps = lengthCorps + (string.charAt(i)-'0');
                i++;
            }
            expediteur = string.substring(i+1,i+lengthExpediteur+1);
            corps = string.substring(i+lengthExpediteur+2);
            if(lengthExpediteur==0)
                expediteur=null;
        } else if (type == MESSAGE_PRIVE || type == TRANSFERT_ANNULE) {
            int lengthDestinataire = 0;
            int lengthExpediteur=0;
            int lengthCorps=0;
            int i=3;
            while(string.charAt(i)!='#') {
                lengthDestinataire = lengthDestinataire*10 +( string.charAt(i)-'0');
                i++;
            }
            i++;
            while(string.charAt(i)!='#') {
                lengthExpediteur = lengthExpediteur*10 +( string.charAt(i)-'0');
                i++;
            }
            i++;
            while(string.charAt(i)!='#') {
                lengthCorps = lengthCorps + (string.charAt(i)-'0');
                i++;
            }
            destinataire = string.substring(i+1, i+1+lengthDestinataire);
            i+=lengthDestinataire+1;
            expediteur = string.substring(i+1, i+1+lengthExpediteur);
            i+=lengthExpediteur+1;
            corps = string.substring(i+1);
            nomFichier=string.substring(i+1);
            if(lengthDestinataire==0)
                destinataire=null;
            if(lengthExpediteur==0)
                expediteur=null;
        } else if (type == TRANSFERT_FICHIER) {
            int lengthDestinataire = 0;
            int lengthExpediteur=0;
            int lengthAdresse=0;
            int i=3;
            while(string.charAt(i)!='#') {
                lengthDestinataire = lengthDestinataire*10 +( string.charAt(i)-'0');
                i++;
            }
            i++;
            while(string.charAt(i)!='#') {
                lengthExpediteur = lengthExpediteur*10 +( string.charAt(i)-'0');
                i++;
            }
            i++;
            while(string.charAt(i)!='#') {
                lengthAdresse = lengthAdresse + (string.charAt(i)-'0');
                i++;
            }
            destinataire = string.substring(i+1, i+2+lengthDestinataire);
            i+=lengthDestinataire+2;
            expediteur = string.substring(i+1,lengthExpediteur+i+1);
            i+=lengthExpediteur+2;
            adresse = string.substring(i+1,lengthAdresse+i+1);
            port=0;
            i+=lengthAdresse+2;
            while(string.charAt(i)!='#') {
                port=port*10+(string.charAt(i)-'0');
                i++;
            }
            tailleFichier=0;
            i++;
            while(string.charAt(i)!='#') {
                tailleFichier=tailleFichier*10+(string.charAt(i)-'0');
                i++;
            }
            nomFichier = string.substring(i + 1);
            if(lengthDestinataire==0)
                destinataire=null;
            if(lengthExpediteur==0)
                expediteur=null;
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
            return "#" + type + "#" + ((expediteur==null)?0:expediteur.length())+ "#" + 
                   ((corps==null)?0:corps.length()) + "#" + expediteur + "#" + corps;
        else if (type == MESSAGE_PRIVE||type == TRANSFERT_ANNULE)
            return "#" + type + "#" + ((destinataire==null)?0:destinataire.length()) + "#"+ expediteur.length() +"#" +((corps==null)?0:corps.length())+"#"
                   + destinataire + "#" + expediteur + "#" + corps;
        else
            return "#" + type + "#" +destinataire.length()+ "#" + expediteur.length() + "#" +((adresse==null)?0:adresse.length())  + "#" + 
                   destinataire + "#" + expediteur + "#" + adresse + '#' + port + '#' + tailleFichier + '#' + nomFichier;
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
