package net.tfobz.synchronization.chat;

import java.util.ArrayList;

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
		roomName = name;
		users = new ArrayList<ChatUser>();
	}
	
	public boolean remove(ChatUser user) {
		return users.remove(user);
	}
	
	public boolean add(ChatUser user) {
		return users.add(user);
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
