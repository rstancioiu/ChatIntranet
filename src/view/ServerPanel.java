package view;

import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import model.InformationServer;
import model.Language;

/**
 * ServelPanel is a panel showed in the list of servers
 * 
 * @author Afkid
 */
public class ServerPanel extends JPanel {
	private static final Logger log = LogManager.getLogger();
	private InformationServer info;
	private ServerList serverList;
	private JButton button;
	private Language language;
	private JLabel[] labels = new JLabel[4];

	/**
	 * The constructor of server panel
	 * 
	 * @param serveur
	 * @param infos
	 * @param language
	 */
	public ServerPanel(ServerList serveur, InformationServer infos, Language language) {
		info = infos;
		this.language = language;
		this.setFont(new Font("Arial", Font.BOLD, 14));
		serverList = serveur;
		this.setLayout(new GridLayout(1, 5, 10, 10));
		labels[0] = new JLabel(info.getName());
		labels[1] = new JLabel(info.getType());
		labels[2] = new JLabel("(" + info.getClients() + "/" + info.getClientsMax() + ")");
		labels[3] = new JLabel(info.getHost());
		button = new JButton(language.getValue("JOIN"));
		for (int i = 0; i < 4; ++i)
			this.add(labels[i]);
		this.add(button);
		button.setEnabled(false);
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				if (button.isEnabled()) {
					serverList.verify(info);
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
				serverList.verify(info);
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
