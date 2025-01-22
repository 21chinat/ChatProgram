package net.tfobz.synchronization.chat.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Iterator;
import java.util.Random;

import net.tfobz.synchronization.chat.ChatRoom;
import net.tfobz.synchronization.chat.ChatUser;

public class ChatServerThread extends Thread {

	private Socket client = null;
	private BufferedReader in = null;
	private ChatUser user = null;
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
			name = "<span style=\"color:" + String.format("#%06X", new Random().nextInt(0xFFFFFF + 1)) + "\">" + name
					+ "</span>";
			room = ChatServer.rooms.get(0);
			room.add(user);

			System.out.println(user.getUsername() + " signed in. " + ChatServer.users.size() + " users");

			for (ChatUser user : room.getUsers())
				user.getOut().println(name + " signed in");

			while (true) {
				String line = in.readLine();
				if (line == null)
					break;
				if (line.startsWith("/")) {
					if (line.startsWith("/exit")) {
						break;
					}
					executeCommand(line);
				} else {
					for (ChatUser user : room.getUsers()) {
						user.getOut().println(name + ": " + line);
					}
				}
			}

			ChatServer.users.remove(user);
			room.remove(user);
			System.out.println(name + " signed out. " + ChatServer.users.size() + " users");
			for (ChatUser user : room.getUsers()) {
				user.getOut().println(name + " signed out");
			}

		} catch (IOException e) {
			System.out.println(e.getClass().getName() + ": " + e.getMessage());
			if (user != null) {
				ChatServer.users.remove(user);
				room.remove(user);
				System.out.println(name + " signed out. " + ChatServer.users.size() + " users");
			}
		} catch (IllegalArgumentException e) {
			try {
				new PrintStream(client.getOutputStream()).println(e.getMessage());
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} finally {
			try {
				client.close();
			} catch (Exception e1) {
				;
			}
		}
	}

	private void executeCommand(String line) {
		if (line.startsWith("/msg ")) {
			for (ChatUser users : ChatServer.users) {
				if (line.replaceFirst("/msg ", "").trim().startsWith(users.getUsername())) {
					users.getOut().println("(Private)" + name
							+ line.replaceFirst("/msg ", "").trim().replace(user.getUsername(), name+": ") + ": ");
				}
				user.getOut().println("(Private)" + name
						+ line.replaceFirst("/msg ", "").trim().replace(user.getUsername(), name+": "));
			}
		} else if (line.startsWith("/color ")) {
			String newColor = line.replace("/color", "").trim().substring(0, 6);
			System.out.println(newColor);
			try {
				name = "<span style=\"color:" + String.format("#%06X", Integer.parseUnsignedInt(newColor, 16)) + "\">"
						+ user.getUsername() + "</span>";
			} catch (NumberFormatException e) {
				user.getOut().println("Invalid Color");
			}
		} else if (line.startsWith("/room ")) {
			executeRoomCommand(line.replaceFirst("/room ", "").trim());
		}
	}
	
	private void executeRoomCommand(String line) {
		if(line.startsWith("create ")) {
			String roomName = line.replace("create ", "").trim();
			try {
				ChatServer.rooms.add(new ChatRoom(roomName));
				user.getOut().println("Room "+roomName+" created");
			} catch (NumberFormatException e) {
				user.getOut().println("Invalid Room Name: "+roomName);
			} catch (IllegalArgumentException e) {
				user.getOut().println(e.getMessage());
			}
		}else if(line.startsWith("join ")) {
			String roomName = line.replace("join ", "").trim();
			if(roomName.isEmpty()) {
				this.room=ChatServer.rooms.get(0);
			}else {
				this.room=null;
				int i =0;
				while (room==null&&i<ChatServer.rooms.size()) {
					if(ChatServer.rooms.get(i).roomNameEquals(roomName))
						room=ChatServer.rooms.get(i);
					i++;
				}
				if(room==null)
					user.getOut().println("No Room with the name: "+roomName);
			}
		}
		
	}
}
