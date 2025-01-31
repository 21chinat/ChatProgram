package net.tfobz.synchronization.chat.client;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import javax.swing.*;

@SuppressWarnings("serial")
public class ChatClient extends JFrame
{

	private static final int PORT = 65535;
	private Font font = new Font("Comic Sans", Font.PLAIN, 24);
	private int fontSize = 18;
	private boolean control = false;

	private Socket client = null;
	private BufferedReader in = null;
	private PrintStream out = null;

	private JPanel panelUser = null;
	private JPanel panelMessage = null;
	private JScrollPane scrollPaneChat = null;
	private JTextField textUser = null;
	private JButton buttonUser = null;
	private JEditorPane textChat = null;
	private JTextField textMessage = null;
	private JButton buttonMessage = null;
	private TrayIcon trayIcon = null;

	public ChatClient() {
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLayout(new BorderLayout());
		setTitle("Chat");
		setIconImage(new ImageIcon(ChatClient.class.getResource("/net/tfobz/synchronization/assets/icon.png")).getImage());
		setMinimumSize(new Dimension(800, 500));
		setSize(new Dimension(1024, 768));
		setLocationRelativeTo(null);

		panelUser = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);

		textUser = new JTextField();
		textUser.setFont(font);
		textUser.setPreferredSize(new Dimension(400, 32));
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		textUser.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					buttonUser.doClick();
				}
			}
		});
		panelUser.add(textUser, gbc);

		buttonUser = new JButton("Anmelden");
		buttonUser.setPreferredSize(new Dimension(150, 32));
		buttonUser.setFont(font);
		buttonUser.setMnemonic(KeyEvent.VK_A);
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0;
		buttonUser.addActionListener(e -> {
			if (buttonUser.getText().equals("Anmelden")) {
				connectToServer();
				buttonUser.setText("Abmelden");
				textUser.setEditable(false);
			} else {
				out.println("/exit");
				buttonUser.setText("Anmelden");
				textUser.setEditable(true);
			}
		});
		panelUser.add(buttonUser, gbc);

		add(panelUser, BorderLayout.PAGE_START);

		panelMessage = new JPanel(new GridBagLayout());

		textMessage = new JTextField();
		textMessage.setFont(font);
		textMessage.setPreferredSize(new Dimension(400, 32));
		textMessage.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					buttonMessage.doClick();
				}
			}
		});
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		panelMessage.add(textMessage, gbc);

		buttonMessage = new JButton(new ImageIcon(ChatClient.class.getResource("/net/tfobz/synchronization/assets/send.png")));
		buttonMessage.setPreferredSize(new Dimension(50, 50));
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0;
		buttonMessage.addActionListener(e -> sendMessage());
		panelMessage.add(buttonMessage, gbc);

		add(panelMessage, BorderLayout.PAGE_END);

		textChat = new JEditorPane();
		textChat.setEditable(false);
		textChat.setFont(new Font("Serif", Font.PLAIN, fontSize));
		textChat.setContentType("text/html");
		scrollPaneChat = new JScrollPane(textChat);
		scrollPaneChat.setAutoscrolls(true);
		add(scrollPaneChat, BorderLayout.CENTER);

		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
					control = true;
				}
				if (control) {
					if (e.getKeyCode() == KeyEvent.VK_PLUS) {
						textChat.setFont(new Font("Serif", Font.PLAIN, ++fontSize));
					}
					if (e.getKeyCode() == KeyEvent.VK_MINUS) {
						textChat.setFont(new Font("Serif", Font.PLAIN, --fontSize));
					}
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
					control = false;
				}
			}
		});
		if (SystemTray.isSupported()) {
			trayIcon = new TrayIcon(new ImageIcon(ChatClient.class.getResource("/net/tfobz/synchronization/assets/icon.png")).getImage(),getTitle());
			trayIcon.setImageAutoSize(true);
			trayIcon.addActionListener(e -> {
				setVisible(true);
				setState(JFrame.NORMAL);
				SystemTray.getSystemTray().remove(trayIcon);
			});

			addWindowListener(new WindowAdapter() {
				@Override
				public void windowIconified(WindowEvent e) {
					if (buttonUser.getText().equals("Abmelden")) {
						setDefaultCloseOperation(HIDE_ON_CLOSE);
						try {
							SystemTray tray = SystemTray.getSystemTray();
							tray.add(trayIcon);
							setVisible(false);
							trayIcon.displayMessage("Minimiert", "Die Anwendung wurde in den System-Tray minimiert.",
									TrayIcon.MessageType.INFO);
						} catch (AWTException ex) {
							ex.printStackTrace();
						}
					} else {
						setDefaultCloseOperation(EXIT_ON_CLOSE);
					}
				}
			});
		} else {
			JOptionPane.showMessageDialog(this, "System-Tray wird nicht unterstützt.", "Fehler", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void connectToServer() {
		String username = textUser.getText().trim();
		if (username.isEmpty()) {
			JOptionPane.showMessageDialog(this, "Bitte geben Sie einen Benutzernamen ein.", "Fehler",
					JOptionPane.ERROR_MESSAGE);
		} else if (username.length() > 25) {
			JOptionPane.showMessageDialog(this, "Der Benutzername muss 3-25 Zeichen lang sein.", "Fehler",
					JOptionPane.ERROR_MESSAGE);
		} else {
			try {
				client = new Socket("localhost", PORT);
				in = new BufferedReader(new InputStreamReader(client.getInputStream()));
				out = new PrintStream(client.getOutputStream());

				out.println(username);

				new ChatClientThread(in, textChat).start();

				textUser.setEditable(false);
			} catch (IOException ex) {
				JOptionPane.showMessageDialog(this, "Verbindung zum Server fehlgeschlagen: " + ex.getMessage(), "Fehler",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private void sendMessage() {
		String message = textMessage.getText().trim();
		if (message.equals("/clear")) {
			textChat.setText("");
		}
		if (message.equals("/exit")) {
			textChat.setText("");
			buttonUser.doClick();
		}else if (!message.isEmpty() || out != null) {
			out.println(message);
			textMessage.setText("");
		}
	}

	public static void main(String[] args) {
		ChatClient cc = new ChatClient();
		cc.setVisible(true);
	}
}
