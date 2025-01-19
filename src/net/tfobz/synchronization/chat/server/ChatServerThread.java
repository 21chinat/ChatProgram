package net.tfobz.synchronization.chat.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Random;

import net.tfobz.synchronization.chat.ChatRoom;
import net.tfobz.synchronization.chat.ChatUser;

public class ChatServerThread extends Thread
{

	private Socket client = null;
	private BufferedReader in = null;
	private ChatUser user=null;
	private ChatRoom room = null;
	private String name;
	
	public ChatServerThread(Socket client) throws IOException {
		this.setDaemon(true);
		this.client = client;
		in = new BufferedReader(new InputStreamReader(client.getInputStream()));
	}
	
	@Override
	public void run() {
		try {
			name = in.readLine();
			user = new ChatUser(new PrintStream(client.getOutputStream()), name);
			
			ChatServer.users.add(user);
			name = "<span style=\"color:#"+String.format("#%06X", new Random().nextInt(0xFFFFFF + 1))+"\">"+name+"</span>";
			room = ChatServer.rooms.get(0);
			room.add(user);
			
			System.out.println(user.getUsername() + " signed in. " + ChatServer.users.size() + " users");
			
			for (ChatUser user: room.getUsers())
				user.getOut().println(name + " signed in");
			
			while (true) {
				String line = in.readLine();
				if (line == null)
					break;
				if(line.startsWith("/")) {
					if(line.startsWith("/msg ")) {
						for (ChatUser user : ChatServer.users) {
							if(line.replaceFirst("/msg ", "").trim().startsWith(user.getUsername())){
								user.getOut().println("(Private)"+name + line.replaceFirst("/msg ", "").trim().replace(name, ""));
							}
						}
					}else if(line.startsWith("/exit")) {
						client.close();
					}
				}else { 
					for (ChatUser user: room.getUsers()) {
						user.getOut().println(name + ": " + line);
					}
				}
			}
			
			ChatServer.users.remove(user);
			room.remove(user);
			System.out.println(name + " signed out. " + ChatServer.users.size() + " users");
			for (ChatUser user: room.getUsers()) {
				user.getOut().println(name + " signed out");
			}
			
		} catch (IOException e) {
			System.out.println(e.getClass().getName() + ": " + e.getMessage());
			if (user != null) {
				ChatServer.users.remove(user);
				room.remove(user);
				System.out.println(name + " signed out. " + ChatServer.users.size() + " users");
			}
			
		}catch (IllegalArgumentException e) {
			try {
				new PrintStream(client.getOutputStream()).println(e.getMessage());
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} finally {
			try { client.close(); } catch (Exception e1) { ; }
		}
	}
}
