package net.tfobz.synchronization.chat.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.SocketException;
import javax.swing.JTextArea;

public class ChatClientThread extends Thread
{
	private BufferedReader in = null;
	private JTextArea textArea;
	
	public ChatClientThread(BufferedReader in, JTextArea textArea) {
		this.in = in;
		this.textArea = textArea;
		this.setDaemon(true);
	}
	
	@Override
	public void run() {
		try {
			while (true) {
				String line = in.readLine();
				if (line == null)
					break;
				this.textArea.append(line);
			}
		} catch (SocketException e) {
			this.textArea.append("Connection to ChatServer lost, ignore exception");
		} catch (IOException e) {
			this.textArea.append(e.getClass().getName() + ": " + e.getMessage());
		}
	}
}
