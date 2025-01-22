package net.tfobz.synchronization.chat.client;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import javax.swing.*;

public class ChatClient extends JFrame {

    private static final int PORT = 65535;
    private Font font = new Font("Comic Sans", Font.PLAIN, 18);

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

    public ChatClient() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setTitle("Chat");
        setMinimumSize(new Dimension(800, 600));
        setSize(new Dimension(1024, 768));
        setLocationRelativeTo(null);

        panelUser = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        textUser = new JTextField();
        textUser.setFont(font);
        textUser.setPreferredSize(new Dimension(400, 25));
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
        buttonUser.setFont(font);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        buttonUser.addActionListener(e -> connectToServer());
        panelUser.add(buttonUser, gbc);

        add(panelUser, BorderLayout.PAGE_START);

        panelMessage = new JPanel(new GridBagLayout());

        textMessage = new JTextField();
        textMessage.setFont(font);
        textMessage.setPreferredSize(new Dimension(400, 25));
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

        buttonMessage = new JButton("Senden");
        buttonMessage.setFont(font);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        buttonMessage.addActionListener(e -> sendMessage());
        panelMessage.add(buttonMessage, gbc);

        add(panelMessage, BorderLayout.PAGE_END);

        textChat = new JEditorPane();
        textChat.setEditable(false);
        textChat.setFont(font);
        textChat.setContentType("text/html");
        scrollPaneChat = new JScrollPane(textChat);
        scrollPaneChat.setAutoscrolls(true);
        add(scrollPaneChat, BorderLayout.CENTER);
    }

    private void connectToServer() {
        String username = textUser.getText().trim();
        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Bitte geben Sie einen Benutzernamen ein.", "Fehler", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            client = new Socket("localhost", PORT); // Ersetzen Sie "localhost" durch die Server-Adresse.
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            out = new PrintStream(client.getOutputStream());

            // Senden des Benutzernamens an den Server
            out.println(username);

            // Starten des Threads, um Nachrichten vom Server zu lesen
            new Thread(() -> {
                try {
                    String line;
                    while ((line = in.readLine()) != null) {
                        appendMessage(line);
                    }
                } catch (IOException ex) {
                    appendMessage("Verbindung zum Server verloren.");
                }
            }).start();

            buttonUser.setEnabled(false);
            textUser.setEnabled(false);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Verbindung zum Server fehlgeschlagen: " + ex.getMessage(), "Fehler", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void sendMessage() {
        String message = textMessage.getText().trim();
        if (message.isEmpty() || out == null) {
            return;
        }

        out.println(message);
        textMessage.setText("");
    }

    private void appendMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            textChat.setText(textChat.getText() + message + "\n");
        });
    }

    public static void main(String[] args) {
        ChatClient cc = new ChatClient();
        cc.setVisible(true);
    }
}
