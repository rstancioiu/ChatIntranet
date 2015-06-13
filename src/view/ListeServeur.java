package view;

import java.awt.Font;

import java.util.ArrayList;
import java.util.Timer;

import java.util.TimerTask;

import javax.swing.DefaultListModel;
import javax.swing.JList;

import javax.swing.SwingUtilities;

import model.InfoServeur;

@SuppressWarnings("oracle.jdeveloper.java.serialversionuid-field-missing")
public class ListeServeur extends JList {
    private  Timer refreshList;
    private ArrayList<InfoServeur> liste= new ArrayList<InfoServeur>();
    private DefaultListModel model;
    private ListeServeur listeServeur;
    
    public ListeServeur() {
        model = new DefaultListModel();
        this.setModel(model);
        this.setFont(new Font("Arial",Font.BOLD,14));   
        listeServeur=this;
        refreshList = new Timer();
        refreshList.schedule(new RefreshJlist(), 0, 18 * 1000);
    }
    
    public void addServeur(InfoServeur infos) {
        boolean contient=false;
        System.out.println(infos);
        if(liste.size()!=0) {
            for(int i=0;i<liste.size() && !contient;++i) {
                if(liste.get(i).equals(infos)) {
                    if(infos.getClients() !=liste.get(i).getClients()) {
                        liste.set(i,infos);
                        SwingUtilities.invokeLater(new UpdateList());
                    }
                    contient=true;
                }           
            }
        }
        if (contient == false) {
            /* si la donnee recue n'est pas deja dans la liste*/
            liste.add(infos);
            SwingUtilities.invokeLater(new UpdateList());
        }
    }
    
    /**
     * class UpdateList qui est un Thread qui met a jour la jlist et il est invoque
     * par la methode invokeLater();
     */
    private class UpdateList implements Runnable {
        @SuppressWarnings("unchecked")
        public void run() {
            model = new DefaultListModel();
            listeServeur.setModel(model);
            model.clear(); // efface la liste model
            int i;
            if (liste.size() != 0) {
                for (i = 0; i <= liste.size() - 1; i++) {
                    model.addElement(liste.get(i)); // ajoute chaque element de la liste arrayList a la liste model
                }
            }
        }
    }
    
    /**
     * Classe refreshJlist qui herite de TimerTask qui nettoie la <InfoServeur>liste
     * pour mettre a jour la disparition des serveurs;
     */
    private class RefreshJlist extends TimerTask {
        public void run() {
            liste.clear();
            SwingUtilities.invokeLater(new UpdateList());
        }
    }
    
    public String getAdresseByIndex(int index) {
        return liste.get(index).getAdresse();
    }
}
