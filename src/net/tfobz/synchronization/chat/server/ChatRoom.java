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
		boolean ret;
		synchronized (users) {
			ret = users.remove(user);
		}
		return ret;
	}
	
	public boolean add(ChatUser user, String arg) throws SecurityException {
		return this.add(user);
	}
	
	protected boolean add(ChatUser user) throws SecurityException {
		synchronized (users) {
			if(users.contains(user))
				return false;
			else
				return users.add(user);
		}
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
	
	public String userList() {
		String ret = "";
		synchronized (users) {
			for (ChatUser users : users) {
				ret+=users.getUsername()+", ";
			}
		}
		return ret.substring(0, ret.length()-2);
	}
	
	public String generateInvite(ChatUser user) {
		return " invited you to " + roomName;
	}
	
	@Override
	public String toString() {
		return "Room name: " + roomName + ", Pariticipants: "+users.size();
	}
}
