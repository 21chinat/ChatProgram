package net.tfobz.synchronization.chat.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Random;

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
			name = name.indexOf(" ")==-1?name:name.substring(0, name.indexOf(" "));
			user = new ChatUser(new PrintStream(client.getOutputStream()), name);

			name = "<span style=\"color:" + String.format("#%06X", new Random().nextInt(0xFFFFFF + 1)) + "\">" + name + "</span>";
			
			room = ChatServer.rooms.get(0);
			room.add(user,null);
			
			synchronized (ChatServer.users) {
				ChatServer.users.add(user);
				System.out.println(user.getUsername() + " signed in. " + ChatServer.users.size() + " users");
				for (ChatUser user : ChatServer.users)
					user.println(name + " signed in");
			}

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
					room.announce(name + ": " + line);
				}
			}

			room.remove(user);
			synchronized (ChatServer.users) {
				for (ChatUser user : ChatServer.users)
					user.println(name + " signed out");
				ChatServer.users.remove(user);
				System.out.println(user.getUsername() + " signed out. " + ChatServer.users.size() + " users");
			}
			
		} catch (IOException ioe) {
			System.out.println(ioe.getClass().getName() + ": " + ioe.getMessage());
			if (user != null) {
				room.remove(user);
				synchronized (ChatServer.users) {
					for (ChatUser user : ChatServer.users) {
						user.println(name + " signed out");
					}
					ChatServer.users.remove(user);
					System.out.println(user.getUsername() + " signed out. " + ChatServer.users.size() + " users");
				}
			}
		} catch (IllegalArgumentException iae) {
			try {
				new PrintStream(client.getOutputStream()).println(iae.getMessage());
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		} finally {
			try {client.close();} catch (IOException ioe) {;}
		}
	}

	private void executeCommand(String line) {
		if (line.startsWith("/msg ")) {
			message(line.replaceFirst("/msg ", "").trim());
		} else if (line.startsWith("/color ")) {
			changeColor(line.replace("/color", "").trim().substring(0, 6));
		} else if (line.startsWith("/userList")) {
		userList();
		} else if (line.startsWith("/room ")) {
			executeRoomCommand(line.replaceFirst("/room ", "").trim());
		}else if (line.startsWith("/help")) {
			help();
		}else if (line.startsWith("/clear")) {
			user.println("/clear");
		}else {
			user.println("Command Unknown, use /help to get a list of commands");
		}
	}
	
	private void userList() {
		String list = "";
		synchronized (ChatServer.users) {
			for (ChatUser users : ChatServer.users)
			list+=users.getUsername()+", ";
		}
		user.println(list.substring(0, list.length()-2));
	}
	
	private void help() {
		user.println("/room [command] - use a command that affects the chat rooms");
		user.println("/color [color] - change your username color. the color is in rgb hex format, for example /color ffff00 to have a yellow username");
		user.println("/msg [username] [message]- privatly send a message to one person (also works if not in the same room)");
		user.println("/userList - get a list of all users");
		user.println("/clear - clears the displayed messages");
		user.println("/help - show this help");
		user.println("/exit - exit the chat");
	}
	
	private void changeColor(String color) {
		try {
			name = "<span style=\"color:" + String.format("#%06X", Integer.parseUnsignedInt(color, 16)) + "\">" + user.getUsername() + "</span>";
			user.println("Color Changed to "+color);
		} catch (NumberFormatException e) {
			user.println("Invalid Color");
		}
	}
	
	private void message(String line) {
		boolean pending=true;
		int i = 0;
		synchronized (ChatServer.users) {
			while(pending&&i<ChatServer.users.size()) {
				if (line.startsWith(ChatServer.users.get(i).getUsername())) {
					ChatServer.users.get(i).println("(Private)" + name + line.replace(ChatServer.users.get(i).getUsername(), ": "));
					user.println("(Private)" + name + line.replace(ChatServer.users.get(i).getUsername(), ": "));
					pending=false;
				}
				i++;
			}
		}
		if(pending) {
			user.println("No such user found");
		}
	}
	
	private void executeRoomCommand(String roomCommand) {
		if(roomCommand.startsWith("createPassword ")) {
			roomCreatePassword(roomCommand.replace("createPassword ", "").trim());
		}else if(roomCommand.startsWith("createInvite ")) {
			roomCreateInvite(roomCommand.replaceFirst("createInvite ", "").trim());
		}else if(roomCommand.startsWith("create ")) {
			roomCreate(roomCommand.replaceFirst("create ", "").trim());
		}else if(roomCommand.startsWith("join")) {
			roomJoin(roomCommand.replaceFirst("join", "").trim());
		}else if(roomCommand.startsWith("invite ")) {
			roomInvite(roomCommand.replaceFirst("invite ", "").trim());
		}else if(roomCommand.startsWith("leave")) {
			roomJoin("","");
		}else if(roomCommand.startsWith("info")) {
			roomInfo();
		}else if(roomCommand.startsWith("help")) {
			roomHelp();
		}else if(roomCommand.startsWith("userList")) {
			roomUserList();
		}else {
			user.println("Room Command Unknown, use /room help to get a list of commands");
		}
	}
	
	private void roomCreate(String line) {
		try {
			int space = line.indexOf(" ");
			String roomName = line.substring(0, space!=-1?space:line.length());
			synchronized (ChatServer.rooms) {
				ChatServer.rooms.add(new ChatRoom(roomName));
			}
			user.println("Room "+roomName+" created");
			roomJoin(roomName);
		} catch (IllegalArgumentException e) {
			user.println(e.getMessage());
		}
	}
	
	private void roomCreatePassword(String line) {
		try {
			int space = line.indexOf(" ");
			String roomName = line.substring(0, space!=-1?space:line.length());
			String password = space!=-1?line.substring(space).trim():null;
			synchronized (ChatServer.rooms) {
				ChatServer.rooms.add(new ChatPasswordRoom(roomName,password));
			}
			user.println("Room "+roomName+" created with Password "+password);
			roomJoin(roomName,password);
		} catch (IllegalArgumentException e) {
			user.println(e.getMessage());
		}
	}
	
	private void roomCreateInvite(String line) {
		try {
			int space = line.indexOf(" ");
			String roomName = line.substring(0, space!=-1?space:line.length());
			synchronized (ChatServer.rooms) {
				ChatServer.rooms.add(new ChatInviteRoom(roomName, user));
			}
			user.println("Room "+roomName+" created");
			roomJoin(roomName);
		} catch (IllegalArgumentException e) {
			user.println(e.getMessage());
		}
	}
	
	private void roomJoin(String line) {
		int space = line.indexOf(" ");
		String roomName = line.substring(0, space!=-1?space:line.length());
		String password = space!=-1?line.substring(space).trim():null;
		roomJoin(roomName, password);
	}
	
	private void roomJoin(String roomName, String password) {
		ChatRoom old = this.room;
		this.room=null;
		
		if(roomName.isEmpty()) {
			this.room = ChatServer.rooms.get(0);
		}else {
			int i =0;
			synchronized (ChatServer.rooms) {
				while (room==null&&i<ChatServer.rooms.size()) {
					if(ChatServer.rooms.get(i).roomNameEquals(roomName))
						room=ChatServer.rooms.get(i);
					i++;
				}
			}
			if(room==null) {
				user.println("No Room with the name: "+roomName);
				this.room=old;
			}
		}
		
		if(room!=old) {
			try {
				this.room.add(user,password);
				old.announce(name+" left the room ");
				room.announce(name+" joined the room "+room.getRoomName());
				old.remove(user);
			} catch (SecurityException e) {
				this.room=old;
				user.println(e.getMessage());
			}
		}
	}
	
	private void roomInvite(String names) {
		String invited = "";
		for (ChatUser users : ChatServer.users) {
			if(names.contains(users.getUsername())) {
				users.println(name + room.generateInvite(users));
				invited += users.getUsername()+", ";
			}
		}
		user.println((invited.isEmpty()?"No one":invited.substring(0, invited.length()-2))+" got invited to "+room.getRoomName());
	}
	
	private void roomInfo() {
		user.println(room.toString());
	}
	
	private void roomUserList() {
		user.println(room.userList());
	}
	
	private void roomHelp() {
		user.println("/room create [roomName] - create and then join a chat room");
		user.println("/room createPassword [roomName] [password]- create and then join a chat room that is protected by a password");
		user.println("/room createInvite [roomName] - create and then join a room you need to be invited to to join");
		user.println("/room join [roomName] [password] - join a chat room. the password is not always required");
		user.println("/room invite <users> - invite other people to your chat room.");
		user.println("/room leave - brings you back to the default room");
		user.println("/room info - gives you info about the chat room you are in");
		user.println("/room userList - gives you a list of users in the room");
		user.println("/room help - show this help");
	}
}
