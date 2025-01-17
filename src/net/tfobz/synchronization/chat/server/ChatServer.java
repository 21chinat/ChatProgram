package net.tfobz.synchronization.chat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import net.tfobz.synchronization.chat.*;

public class ChatServer
{
	public static final int PORT = 65535;
	
	protected static ArrayList<ChatRoom> rooms =
			new ArrayList<ChatRoom>();
	
	public static void main(String[] args) {
		ServerSocket server = null;
		try {
			server = new ServerSocket(PORT);
			System.out.println("Chat server started");
			while (true) {
				Socket client = server.accept();
				try {
					new ChatServerThread(client).start();
				} catch (IOException e) {
					System.out.println(e.getClass().getName() + ": " + e.getMessage());
				}
			}
		} catch (IOException e) {
			System.out.println(e.getClass().getName() + ": " + e.getMessage());
		} finally {
			try { server.close(); } catch (Exception e1) { ; }
		}
	}
}
