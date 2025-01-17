package net.tfobz.synchronization.chat;

import java.io.PrintStream;

import net.tfobz.synchronization.chat.server.ChatServer;

public class ChatUser {
	
	protected String username = null;
	protected PrintStream out = null;
	
	public ChatUser(String name) {
		if(name == null || name.length()<3) {
			throw new IllegalArgumentException("Name too short");
		}
		for (ChatUser user : ChatServer.users) {
			if(user.usernameEquals(name)) {
				throw new IllegalArgumentException("Name already in use");

			}
		}
	}

	private boolean usernameEquals(String name) {
		return username.equals(name);
	}
}
