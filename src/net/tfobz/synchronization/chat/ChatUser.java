package net.tfobz.synchronization.chat;

import java.io.PrintStream;

import net.tfobz.synchronization.chat.server.ChatServer;

public class ChatUser {
	
	protected String username = null;
	protected PrintStream out = null;
	
	public ChatUser(PrintStream out,String name) {
		if (out == null)
			throw new IllegalArgumentException("Output printStream is needed to create a user");
		if(name == null || name.length()<3)
			throw new IllegalArgumentException("Name too short or null");
		for (ChatUser user : ChatServer.users) {
			if(user.usernameEquals(name)) {
				throw new IllegalArgumentException("Name already in use");

			}
		}
		this.username=name;
		this.out=out;
	}

	public boolean usernameEquals(String name) {
		return username.equals(name);
	}

	public String getUsername() {
		return username;
	}

	public PrintStream getOut() {
		return out;
	}
}
