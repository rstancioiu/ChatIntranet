package controller;

import view.OngletConversationPrivee;

import java.awt.Toolkit;

import java.io.BufferedInputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import java.io.OutputStream;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import java.util.concurrent.TimeUnit;

/**
 * Classe EnvFichier qui permet l'utilisateur d'envoyer un fichier vers un autre 
 * utilisateur. La taille maximale de l'envoi est de 500 Mb. Techniquement, cette 
 * classe sert comme un serveur ou l'utilisateur qui doit recevoir le fichier se connecte 
 * pour le telecharger. Cette classe est independante de la classe controller.Serveur.
 * @see controller.RecFichier
 * @see view.OngletConversationPrivee
 */
public class EnvFichier implements Runnable {
    
    private Socket socket;
    private ServerSocket serveur;
    private InetAddress adresse;
    private int port;
    
    private File transferFichier;
    private OngletConversationPrivee OCP;


    /**
     * constructeur d'EnvFichier
     * @param transferFichier
     * @param OCP
     */
    public EnvFichier(File transferFichier,OngletConversationPrivee OCP) {
        this.transferFichier = transferFichier;
        this.OCP = OCP;
        try {
            serveur = new ServerSocket(0, 0, InetAddress.getLocalHost());
            port = serveur.getLocalPort();
            adresse = serveur.getInetAddress();
        } catch (IOException e) {
            OCP.insererLigne("Impossible de crï¿½er la connexion avec le client distant, verifier votre connexion au rï¿½seau",OCP.getTransfer(),true);
        }
    }

    /**
     * methode run du thread pour envoyer un fichier. Dans cette methode on ouvre le serveur 
     * qui sera utilise pour envoyer le fichier. 
     */    
    public void run() {
        try {
            socket = serveur.accept();
            OCP.insererLigne("L'envoi de "+transferFichier.getName() +" a commence...",OCP.getTransfer(),true);
            byte[] bytearray = new byte[(int)transferFichier.length()];
            FileInputStream fin = new FileInputStream(transferFichier);
            BufferedInputStream bin = new BufferedInputStream(fin);
            bin.read(bytearray, 0, bytearray.length);
            OutputStream os = socket.getOutputStream();
            os.write(bytearray, 0, bytearray.length);
            os.flush();
            TimeUnit.MILLISECONDS.sleep(300);
            socket.close();
            OCP.insererLigne("Envoi de "+transferFichier.getName()+" termine !",OCP.getTransfer(),true);
            Toolkit.getDefaultToolkit().beep();

        } catch (Exception e) {
            OCP.insererLigne("Une exception s'est produite, le fichier n'a pas pu etre envoye dans son integralite...",
                             OCP.getTransfer(),true);
        }
        try {
            socket.close();
        } catch (IOException ioe) {
            System.out.println("Impossible de fermer le socket - RecFichier.java");
        }
    }
    
    /**
     * getter du port utilise
     * @return le numéro de port
     */
    public int getPort() {
        return port;
    }
    
    /**
        * getter de l'adresse utilisee
        * @return l'InetAddress utilisee
        */
    public InetAddress getAddress() {
        return adresse;
    }
    
    /**
      * getter du nom du fichier
      * @return nom du fichier
      */
    public String getFichier() {
        return transferFichier.getName();
    }
    
    /**
     * getter de la taille du fichier
     * @return la taille du fichier
     */
    public int getTaille(){
        return (int)transferFichier.length();
    }
}

