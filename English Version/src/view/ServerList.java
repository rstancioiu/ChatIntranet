package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import model.InformationsServer;
import model.Language;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ServerList extends JPanel {
    private static final Logger log = LogManager.getLogger();
    private  Timer refreshList;
    private ArrayList<InformationsServer> list= new ArrayList<InformationsServer>();
    private DefaultListModel model;
    private ServerList serverList;
    private Window window;
    private int position;
    private int limit=15;
    private Language language;
    
    public ServerList(Window window, Language language) {
        this.window=window;
        this.language=language;
        this.setLayout(new GridLayout(limit,1));
        this.setFont(new Font("Arial",Font.BOLD,14));  
        this.setBackground(Color.white);
        serverList=this;
        refreshList = new Timer();
        refreshList.schedule(new RefreshJlist(), 0, 18 * 1000);
        log.info("Creation of a list of servers");
    }
    
    public void addServer(InformationsServer infos) {
        boolean contient=false;
        if(list.size()!=0) {
            for(int i=0;i<list.size() && !contient;++i) {
                if(list.get(i).equals(infos)) {
                    if(infos.getClients() !=list.get(i).getClients()) {
                        list.set(i,infos);
                        SwingUtilities.invokeLater(new UpdateList());
                    }
                    contient=true;
                }           
            }
        }
        if (contient == false) {
            list.add(infos);
            SwingUtilities.invokeLater(new UpdateList());
        }
        log.info("Add of server: " + infos.getName());
    }

   public void verify(InformationsServer info) {
        for(int i=0;i<list.size();++i) {
            if(list.get(i).equals(info))
            position=i;
        }
        window.verifyConnection(info);
    }


    public int getSelectedPosition() {
        return position;
    }

    private class UpdateList implements Runnable {
        public void run() {
            serverList.removeAll();
            if(list.size()>=limit){
                limit=list.size();
                serverList.setLayout(new GridLayout(limit+10,1));
            }
            JPanel panelaux = new JPanel(new GridLayout(1,5,10,10));
            JLabel label1 = new JLabel(language.getValue("NAME"));
            JLabel label2 = new JLabel(language.getValue("TYPE"));
            JLabel label3 = new JLabel(language.getValue("SIZE"));
            JLabel label4 = new JLabel(language.getValue("HOST"));
            panelaux.add(label1,BorderLayout.CENTER);
            panelaux.add(label2,BorderLayout.CENTER);
            panelaux.add(label3,BorderLayout.CENTER);
            panelaux.add(label4,BorderLayout.CENTER);
            panelaux.add(new JLabel(""));
            log.info("Update list");
            serverList.add(panelaux);
            if (list.size() != 0) {
                for (int i = 0; i <= list.size() - 1; i++) {
                    JPanel panel = new ServerPanel(serverList,list.get(i),language);
                    serverList.add(panel); 
                }
            }
            serverList.validate();
            serverList.repaint();
        }
    }
    
    private class RefreshJlist extends TimerTask {
        public void run() {
            list.clear();
            SwingUtilities.invokeLater(new UpdateList());
        }
    }
    
    public String getAddressByIndex(int index) {
        return list.get(index).getAdrdess();
    }
}
