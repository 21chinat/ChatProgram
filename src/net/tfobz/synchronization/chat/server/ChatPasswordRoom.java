package net.tfobz.synchronization.chat.server;

public class ChatPasswordRoom extends ChatRoom {
	
	private String password=null;

	public ChatPasswordRoom(String name, String password) {
		super(name);
		if(password ==null || password.length()<4)
			throw new IllegalArgumentException("Password is not a minimum of 4 Characters");
		this.password = password;
	}

	@Override
	public boolean add(ChatUser user, String arg) throws SecurityException {
		if(arg==null || !arg.equals(password))
			throw new SecurityException("Access Denied: password incorrect");
		return super.add(user);
	}

	@Override
	public String generateInvite(ChatUser user) {
		return "invited you to " + roomName + ", Password: "+password;
	}

	@Override
	public String toString() {
		return "Room name: " + roomName + ", Pariticipants: "+users.size()+", Password: "+password;
	}

}
