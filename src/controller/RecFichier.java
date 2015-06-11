package controller;

import view.OngletConversationPrivee;

import java.awt.Toolkit;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.io.InputStream;

import java.net.InetAddress;
import java.net.Socket;

import java.util.concurrent.TimeUnit;

import javax.swing.ProgressMonitor;

/**
 * classe RecFichier permet de recevoir les fichiers d'un utilisateur. Techniquement, elle
 * sert comme client pour le serveur creer dans la classe EnvFichier. Le telechargement se realise 
 * sur le modele classique read+write(classe independante de la classe controller.Serveur). 
 * Elle implemente Runnable(Thread)
 * @see EnvFichier
 * @see OngletConversationPrivee
 */
public class RecFichier implements Runnable {

    private Socket socket;
    private InetAddress addresse;
    private int port;
    
    private int taille;
    private String fichier;
    
    private ProgressMonitor telechargement;
    private OngletConversationPrivee OCP;
 
 
    /**
     * constructeur de la classe RecFichier
     * @param addresse
     * @param fichier
     * @param port
     * @param taille
     * @param OCP
     */
    public RecFichier(InetAddress addresse, String fichier, int port, int taille, OngletConversationPrivee OCP) {
        this.addresse = addresse;
        this.taille = taille;
        this.fichier = fichier;
        this.port = port;
        this.OCP = OCP;
        telechargement = new ProgressMonitor(OCP, "Telechargement", "", 0, 100);
        try {
            socket = new Socket(addresse, port);
        } catch (IOException e) {
            OCP.insererLigne("Impossible d'envoyer le fichier...", OCP.getTransfer(),true);
        }
    }

    /**
     * L'action de la classe pour telecharger le fichier. La premiere partie du telechargement est de 
     * lire les bytes qui sont envoyes sur le Socket et puis de vider le flux en ecrivant le fichier 
     * dans le dossier choisi.
     */
    public void run() {
        try {
            OCP.insererLigne("Telechargement du fichier " + fichier + " (" + taille/1000 + " Ko) ...",
                             OCP.getTransfer(),true);
            telechargement.setMillisToDecideToPopup(0);
            telechargement.setMillisToPopup(0);
            telechargement.setProgress(0);
            
            int bytesLus;
            int currentTot = 0;
            byte[] bytearray = new byte[taille + 1];
            InputStream is = socket.getInputStream();
            FileOutputStream fos = new FileOutputStream(fichier);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            bytesLus = is.read(bytearray, 0, bytearray.length);
            currentTot = bytesLus;
            TimeUnit.MILLISECONDS.sleep(100);
            
            //partie lire
            do {
                bytesLus = is.read(bytearray, currentTot, (bytearray.length - currentTot));
                if (bytesLus >= 0)
                    currentTot += bytesLus;
                Float f = new Float((double) currentTot / (taille + 1));
                telechargement.setProgress(Math.round(99 * f.floatValue()));
            } while (bytesLus > -1);
            telechargement.setProgress(99);
            
            //partie ecrire
            bos.write(bytearray, 0, currentTot);
            bos.flush();
            bos.close();
            socket.close();
            OCP.insererLigne("Telechargement TERMINE !", OCP.getTransfer(),true);
            telechargement.setProgress(100);
            
            Toolkit.getDefaultToolkit().beep();
        } catch (Exception e) {
            OCP.insererLigne("Une exception s'est produite, le fichier n'a pas pu etre recu dans son integralite...",
                             OCP.getTransfer(),true);
            try {
                socket.close();
            } catch (IOException ioe) {
                System.out.println("Impossible de fermer le socket - RecFichier.java");
            }
        }
    }
}
