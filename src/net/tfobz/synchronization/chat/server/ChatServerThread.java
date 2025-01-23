package net.tfobz.synchronization.chat.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Iterator;
import java.util.Random;

import javax.swing.plaf.synth.SynthSpinnerUI;

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
				System.out.println(line);
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
				for (ChatUser user : room.getUsers())
					user.getOut().println(name + " signed out");
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
			boolean pending=true;
			int i = 0;
			while(pending&&i<ChatServer.users.size()) {
				if (line.replaceFirst("/msg ", "").trim().startsWith(ChatServer.users.get(i).getUsername())) {
					ChatServer.users.get(i).getOut().println("(Private)" + name + line.replaceFirst("/msg ", "").trim().replace(user.getUsername(), name+": "));
					user.getOut().println("(Private)" + name + line.replaceFirst("/msg ", "").trim().replace(user.getUsername(), name+": "));
					pending=false;
				}
				i++;
			}
		} else if (line.startsWith("/color ")) {
			String newColor = line.replace("/color", "").trim().substring(0, 6);
			System.out.println(newColor);
			try {
				name = "<span style=\"color:" + String.format("#%06X", Integer.parseUnsignedInt(newColor, 16)) + "\">" + user.getUsername() + "</span>";
				user.getOut().println("Color Changed to "+newColor);
			} catch (NumberFormatException e) {
				user.getOut().println("Invalid Color");
			}
		} else if (line.startsWith("/room ")) {
			executeRoomCommand(line.replaceFirst("/room ", "").trim());
		}
	}
	
	private void executeRoomCommand(String line) {
		if(line.startsWith("create ")) {
			roomCreate(line.replace("create ", "").trim());
		}else if(line.startsWith("join ")) {
			roomJoin(line.replaceFirst("join ", "").trim());
		}else if(line.startsWith("invite ")) {
			roomInvite(line.replaceFirst("invite ", "").trim());
		}
	}
	
	private void roomCreate(String roomName) {
		try {
			ChatServer.rooms.add(new ChatRoom(roomName));
			user.getOut().println("Room "+roomName+" created");
			roomJoin(roomName);
		} catch (NumberFormatException e) {
			user.getOut().println("Invalid Room Name: "+roomName);
		} catch (IllegalArgumentException e) {
			user.getOut().println(e.getMessage());
		}
	}
	
	private void roomJoin(String roomName) {
		if(roomName.isEmpty()) {
			if(this.room!=ChatServer.rooms.get(0)){
				this.room.remove(user);
				for (ChatUser users : room.getUsers()) {
					users.getOut().println(name+" left the room");
				}
				this.room = ChatServer.rooms.get(0);
				this.room.add(user);
				for (ChatUser users : room.getUsers()) {
					users.getOut().println(name+" joined the room");
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
				user.getOut().println("No Room with the name: "+roomName);
				this.room=old;
			}else {
				old.remove(user);
				for (ChatUser users : old.getUsers()) {
					users.getOut().println(name+" left the room");
				}
				this.room.add(user);
				for (ChatUser users : room.getUsers()) {
					users.getOut().println(name+" joined the room");
				}
			}
		}
	}
	
	private void roomInvite(String names) {
		String invited = "";
		for (ChatUser users : ChatServer.users) {
			if(names.contains(users.getUsername())) {
				users.getOut().println(name + " invited you to "+room.getRoomName());
				invited += users.getUsername()+" ";
			}
		}
		user.getOut().println((invited.isEmpty()?"No one ":invited)+"got invited");
	}
}
