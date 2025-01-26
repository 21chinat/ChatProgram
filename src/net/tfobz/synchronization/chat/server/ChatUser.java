package net.tfobz.synchronization.chat.server;

import java.io.PrintStream;

public class ChatUser {
	
	protected String username = null;
	protected PrintStream out = null;
	
	public ChatUser(PrintStream out,String name) {
		if (out == null)
			throw new IllegalArgumentException("Output printStream is needed to create a user");
		if(name == null || name.length()<3)
			throw new IllegalArgumentException("Name too short or null");
		for (ChatUser user : ChatServer.users) {
			if(user.getUsername().contains(name)||name.contains(user.getUsername())) {
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
	
	synchronized public void println(String message) {
		out.println(message);
	}

	@Deprecated
	public PrintStream getOut() {
		return out;
	}
}
