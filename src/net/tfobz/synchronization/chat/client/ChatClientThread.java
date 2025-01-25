package net.tfobz.synchronization.chat.client;

import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.SocketException;
import javax.swing.JEditorPane;

public class ChatClientThread extends Thread
{
	private BufferedReader in = null;
	private JEditorPane editorPane;
	private StringBuilder content;
	
	public ChatClientThread(BufferedReader in, JEditorPane editorPane) {
		this.in = in;
		this.editorPane = editorPane;
		this.setDaemon(true);
		content = new StringBuilder();
	}
	
	@Override
	public void run() {
		try {
			while (true) {
				String line = in.readLine();
				if (line == null)
					break;
				this.addMessage(line);
			}
		} catch (SocketException e) {
			this.addMessage("Connection to ChatServer lost, ignore exception");
		} catch (IOException e) {
			this.addMessage(e.getClass().getName() + ": " + e.getMessage());
		}
		
	}
	
	public void addMessage(String message) {
        EventQueue.invokeLater(() -> {
            content.append(message).append("<br>");
            editorPane.setText(content.toString());
        });
    }
}
