package view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import model.Language;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class Groups extends JFrame {
    
    private static final Logger log = LogManager.getLogger();
    private JTabbedPane tabs = new JTabbedPane();
    private Window window;
    private ArrayList<AllChat> tabDiscussions = new ArrayList<AllChat>();
    private Language language;
    
    private BorderLayout borderLayout1 = new BorderLayout();
    JFrame frame=this;
 
   
    public Groups(Window window, Language language) {
        this.setVisible(false);
        this.language=language;
        this.window=window;
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * contenu de la fenetre a la construction 
     * @throws Exception
     */
    private void jbInit() throws Exception {
        this.getContentPane().setLayout(borderLayout1);
        this.setSize(new Dimension(600, 400));

        this.getContentPane().add(tabs, BorderLayout.CENTER);
        tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                windowClosingEvent(e);
            }
        });
    }

    public void addTab(JPanel panel) {
        if (panel instanceof AllChat) {
            tabDiscussions.add(((AllChat) panel));
            tabs.addTab(((AllChat) panel).getNameDiscussion(), panel);
            tabs.setSelectedIndex(tabs.getTabCount() - 1);
            log.info("A public tab added");
        } else if (panel instanceof PrivateChat) {
            tabs.addTab("#" + ((PrivateChat) panel).getSender(), panel);
            tabs.setSelectedIndex(tabs.getTabCount() - 1);
            log.info("A private tab added");
        }
    }

    private void windowClosingEvent(WindowEvent e) {
        int choix =
            JOptionPane.showConfirmDialog(null, language.getValue("LEAVE_GROUP"), language.getValue("QUIT"),
                                          JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (choix == JOptionPane.YES_OPTION) {
            for (int i = 0; i < tabDiscussions.size(); i++) {
                tabDiscussions.get(i).quit();
                if (tabDiscussions.get(i) != null) {
                    if(tabDiscussions.get(i).getServer()!=null) {
                        tabDiscussions.get(i).getServer().stopServer();
                    }
                }
            }
            log.info("Window of groups is closing");
           close();
        }
        else
        {
            for (int i = 0; i < tabDiscussions.size(); i++) {
                tabDiscussions.get(i).quit();
            }
            window.setNoGroup();
            log.info("Window of groups is closing");
        }
    }
    
    public void close() {
        window.getBoutonCreer().setEnabled(true);
        window.setNoGroup();
        frame.dispose();
    }

    public ArrayList<AllChat> getTabDiscussions() {
        return tabDiscussions;
    }

    public JTabbedPane getTabs() {
        return tabs;
    }
    
    public Window getWindow() {
        return window;
    }
}
