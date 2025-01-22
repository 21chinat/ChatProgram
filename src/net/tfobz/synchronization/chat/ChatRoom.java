package net.tfobz.synchronization.chat;

import java.util.ArrayList;

import net.tfobz.synchronization.chat.server.ChatServer;

public class ChatRoom {
	
	public String roomName = null;
	public ArrayList<ChatUser> users = null;
	
	public String getRoomName() {
		return roomName;
	}

	public ArrayList<ChatUser> getUsers() {
		return users;
	}

	public ChatRoom(String name) {
		if(name == null)
			throw new NullPointerException("Name can't be null");
		for (ChatRoom room : ChatServer.rooms) {
			if(room.getRoomName().equals(name)) {
				throw new IllegalArgumentException("Name already in use");

			}
		}
		roomName = name;
		users = new ArrayList<ChatUser>();
	}
	
	public boolean remove(ChatUser user) {
		return users.remove(user);
	}
	
	public boolean add(ChatUser user) {
		return users.add(user);
	}
	
	public boolean roomNameEquals(String name) {
		return roomName.equals(name);
	}
	
	@Override
	protected Object clone() throws CloneNotSupportedException {
		ChatRoom ret = new ChatRoom(roomName);
		for (ChatUser chatUser : ret.users) {
			ret.add(chatUser);
		}
		return ret;
	}
}
