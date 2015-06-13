package view;


import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.UIManager;

/**
 * Classe qui contient le main de l'application et qui cree la fenetre "Fenetre" avec la liste des rooms
 * @see view.Fenetre
 * @see view.OngletRooms
 */
public class Application {
    
    /**
     * contstructeur qui appelle la classe Fenetre et la classe OngletRooms pour creer la premiere interface
     */
    public Application() {

        Fenetre fenetre = new Fenetre();
        fenetre.setTitle("ChatIntranet - un nouveau chat");
        fenetre.setVisible(true);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = fenetre.getSize();
        if (frameSize.height > screenSize.height) {
            frameSize.height = screenSize.height;
        }
        if (frameSize.width > screenSize.width) {
            frameSize.width = screenSize.width;
        }
        fenetre.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
    }

    /**
     * methode main de l'application
     * @param args
     */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        new Application();
    }
}
