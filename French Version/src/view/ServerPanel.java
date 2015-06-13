package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import model.InfoServeur;

public class ServerPanel extends JPanel {
    private InfoServeur info;
    private ListeServeur serveurs;
    private JButton button;
    private JLabel[] labels = new JLabel[4];

    public ServerPanel(ListeServeur serveur, InfoServeur infos) {
        info = infos;
        this.setFont(new Font("Arial", Font.BOLD, 14));
        serveurs = serveur;
        this.setLayout(new GridLayout(1, 6, 10, 10));
        labels[0] = new JLabel(info.getNom());
        labels[1] = new JLabel(info.getType());
        labels[2] = new JLabel("(" + info.getClients() + "/" + info.getClientsMax() + ")");
        labels[3] = new JLabel(info.getHost());
        button = new JButton("Join");
        for(int i=0;i<4;++i)
            this.add(labels[i]);
        this.add(button);
        this.add(new JLabel(""));
        button.setEnabled(false);
        button.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if(button.isEnabled()) {
                    serveurs.verifier(info);
                }
            }
        });
        button.addMouseListener(new MouseTouch());
        this.addMouseListener(new MouseTouch());
    }

    private class MouseTouch implements MouseListener {
        public MouseTouch() {
            super();
        }
        @Override
        public void mouseClicked(MouseEvent mouseEvent) {
            button.setEnabled(true);
            if (mouseEvent.getClickCount() == 2) {
                serveurs.verifier(info);
            }
        }

        @Override
        public void mousePressed(MouseEvent mouseEvent) {
        }

        @Override
        public void mouseReleased(MouseEvent mouseEvent) {
        }

        @Override
        public void mouseEntered(MouseEvent mouseEvent) {
            button.setEnabled(true);
        }

        @Override
        public void mouseExited(MouseEvent mouseEvent) {
           button.setEnabled(false);
        }
    }
}

