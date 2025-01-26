package net.tfobz.synchronization.chat.server;

import java.util.ArrayList;

public class ChatRoom {
	
	protected String roomName = null;
	protected ArrayList<ChatUser> users = null;
	
	public ChatRoom(String name) {
		if(name == null)
			throw new NullPointerException("Name can't be null");
		for (ChatRoom room : ChatServer.rooms) {
			if(room.roomName.equals(name)) {
				throw new IllegalArgumentException("Name already in use");

			}
		}
		roomName = name;
		users = new ArrayList<ChatUser>();
	}
	
	public String getRoomName() {
		return roomName;
	}

	
	public boolean remove(ChatUser user) {
		return users.remove(user);
	}
	
	public boolean add(ChatUser user, String arg) throws SecurityException {
		if(users.contains(user))
			return false;
		else
			return users.add(user);
	}
	
	public boolean roomNameEquals(String name) {
		return roomName.equals(name);
	}
	
	public void announce(String message) {
		synchronized (users) {
			for (ChatUser user : users) {
				user.println(message);
			}
		}
	}
	
	public void generateInvite() {
		
	}
	
	@Override
	public String toString() {
		return "Room name: " + roomName + ", Pariticipants: "+users.size();
	}
}
