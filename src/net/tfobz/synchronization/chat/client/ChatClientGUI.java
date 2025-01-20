package net.tfobz.synchronization.chat.client;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class ChatClientGUI extends JFrame {

    private Font font = new Font("Comic Sans", Font.PLAIN, 18);

    private JPanel panelUser = null;
    private JPanel panelMessage = null;
    private JScrollPane scrollPaneChat = null;
    private JTextField textUser = null;
    private JButton buttonUser = null;
    private JEditorPane textChat = null;
    private JTextField textMessage = null;
    private JButton buttonMessage = null;

    public ChatClientGUI() {
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
        panelUser.add(textUser, gbc);

        buttonUser = new JButton("Anmelden");
        buttonUser.setFont(font);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
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
        panelMessage.add(buttonMessage, gbc);

        add(panelMessage, BorderLayout.PAGE_END);

        textChat = new JTextPane();
        textChat.setEditable(false);
        textChat.setFont(font);
        scrollPaneChat = new JScrollPane(textChat);
        scrollPaneChat.setAutoscrolls(true);
        add(scrollPaneChat, BorderLayout.CENTER);
    }

    public static void main(String[] args) {
        ChatClientGUI cc = new ChatClientGUI();
        cc.setVisible(true);
    }
}
