package net.tfobz.synchronization.chat.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.SocketException;

public class ChatClientThread extends Thread
{
	private BufferedReader in = null;
	
	public ChatClientThread(BufferedReader in) {
		this.in = in;
		this.setDaemon(true);
	}
	
	@Override
	public void run() {
		try {
			while (true) {
				String line = in.readLine();
				if (line == null)
					break;
				System.out.println(line);
			}
		} catch (SocketException e) {
			System.out.println("Connection to ChatServer lost, ignore exception");
		} catch (IOException e) {
			System.out.println(e.getClass().getName() + ": " + e.getMessage());
		}
	}
}
