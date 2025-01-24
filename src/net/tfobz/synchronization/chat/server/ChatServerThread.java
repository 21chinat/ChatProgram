package net.tfobz.synchronization.chat.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
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

			name = "<span style=\"color:" + String.format("#%06X", new Random().nextInt(0xFFFFFF + 1)) + "\">" + name
					+ "</span>";
			
			room = ChatServer.rooms.get(0);
			room.add(user);
			
			ChatServer.users.add(user);
			System.out.println(user.getUsername() + " signed in. " + ChatServer.users.size() + " users");
			for (ChatUser user : ChatServer.users)
				user.println(name + " signed in");

			while (true) {
				String line = in.readLine();
				if (line == null)
					break;
				System.out.println(line);
				if (line.startsWith("/")) {
					if (line.startsWith("/exit")) {
						break;
					}
					executeCommand(line);
				} else {
					for (ChatUser user : room.getUsers()) {
						user.println(name + ": " + line);
					}
				}
			}

			room.remove(user);
			for (ChatUser user : ChatServer.users) {
				user.println(name + " signed out");
			}
			ChatServer.users.remove(user);
			System.out.println(user.getUsername() + " signed out. " + ChatServer.users.size() + " users");

		} catch (IOException e) {
			System.out.println(e.getClass().getName() + ": " + e.getMessage());
			if (user != null) {
				room.remove(user);
				for (ChatUser user : ChatServer.users) {
					user.println(name + " signed out");
				}
				ChatServer.users.remove(user);
				System.out.println(user.getUsername() + " signed out. " + ChatServer.users.size() + " users");
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
			} catch (Exception e1) {;}
		}
		System.gc();
		System.out.println("bye");
	}

	private void executeCommand(String line) {
		if (line.startsWith("/msg ")) {
			message(line.replaceFirst("/msg ", "").trim());
		} else if (line.startsWith("/color ")) {
			String newColor = line.replace("/color", "").trim().substring(0, 6);
			System.out.println(newColor);
			try {
				name = "<span style=\"color:" + String.format("#%06X", Integer.parseUnsignedInt(newColor, 16)) + "\">" + user.getUsername() + "</span>";
				user.println("Color Changed to "+newColor);
			} catch (NumberFormatException e) {
				user.println("Invalid Color");
			}
		} else if (line.startsWith("/room ")) {
			executeRoomCommand(line.replaceFirst("/room ", "").trim());
		}
	}
	
	private void message(String line) {
		boolean pending=true;
		int i = 0;
		while(pending&&i<ChatServer.users.size()) {
			if (line.startsWith(ChatServer.users.get(i).getUsername())) {
				ChatServer.users.get(i).println("(Private)" + name + line.replace(user.getUsername(), name+": "));
				user.println("(Private)" + name + line.replace(user.getUsername(), name+": "));
				pending=false;
			}
			i++;
		}
		if(pending) {
			user.println("No such user found");
		}
	}
	
	private void executeRoomCommand(String roomCommand) {
		if(roomCommand.startsWith("create ")) {
			roomCreate(roomCommand.replace("create ", "").trim());
		}else if(roomCommand.startsWith("join")) {
			roomJoin(roomCommand.replaceFirst("join", "").trim());
		}else if(roomCommand.startsWith("invite ")) {
			roomInvite(roomCommand.replaceFirst("invite ", "").trim());
		}else if(roomCommand.startsWith("invite ")) {
			roomInvite(roomCommand.replaceFirst("invite ", "").trim());
		}else {
			user.println("Command");
		}
	}
	
	private void roomCreate(String roomName) {
		try {
			ChatServer.rooms.add(new ChatRoom(roomName));
			user.println("Room "+roomName+" created");
			roomJoin(roomName);
		} catch (NumberFormatException e) {
			user.println("Invalid Room Name: "+roomName);
		} catch (IllegalArgumentException e) {
			user.println(e.getMessage());
		}
	}
	
	private void roomJoin(String roomName) {
		if(roomName.isEmpty()) {
			if(this.room!=ChatServer.rooms.get(0)){
				this.room.remove(user);
				for (ChatUser users : room.getUsers()) {
					users.println(name+" left the room");
				}
				this.room = ChatServer.rooms.get(0);
				this.room.add(user);
				for (ChatUser users : room.getUsers()) {
					users.println(name+" joined the room");
				}
			}
		}else {
			ChatRoom old = this.room;
			this.room=null;
			int i =0;
			while (room==null&&i<ChatServer.rooms.size()) {
				if(ChatServer.rooms.get(i).roomNameEquals(roomName))
					room=ChatServer.rooms.get(i);
				i++;
			}
			if(room==null) {
				user.println("No Room with the name: "+roomName);
				this.room=old;
			}else {
				old.remove(user);
				for (ChatUser users : old.getUsers()) {
					users.println(name+" left the room");
				}
				this.room.add(user);
				for (ChatUser users : room.getUsers()) {
					users.println(name+" joined the room "+room.getRoomName());
				}
			}
		}
	}
	
	private void roomInvite(String names) {
		String invited = "";
		for (ChatUser users : ChatServer.users) {
			if(names.contains(users.getUsername())) {
				users.println(name + " invited you to "+room.getRoomName());
				invited += users.getUsername()+", ";
			}
		}
		user.println((invited.isEmpty()?"No one":invited.substring(0, invited.length()-2))+" got invited to "+room.getRoomName());
	}
	
	private void roomInfo() {
		user.println("");
	}
}
