package net.tfobz.synchronization.chat.server;

import java.util.ArrayList;

public class ChatInviteRoom extends ChatRoom {
	
	protected ArrayList<ChatUser> allowed = null;

	public ChatInviteRoom(String name, ChatUser user) {
		super(name);
		allowed = new ArrayList<>();
		allowed.add(user);
	}

	@Override
	public boolean add(ChatUser user, String arg) throws SecurityException {
		if(!allowed.contains(user))
			throw new SecurityException("You are not allowed in this chat room. ask someone in there to invite you");
		return super.add(user);
	}

	@Override
	protected boolean add(ChatUser user) throws SecurityException {
		if(!allowed.contains(user))
			throw new SecurityException("You are not allowed in this chat room. ask someone in there to invite you");
		return super.add(user);
	}

	@Override
	public String generateInvite(ChatUser user) {
		String ret;
		if (!allowed.contains(user)) {
			allowed.add(user);
			ret = " granted you access to "+getRoomName();
		}else {
			ret = super.generateInvite(user);
		}
		return ret;
	}

	@Override
	public String toString() {
		return "Room name: " + getRoomName() + ", Pariticipants: "+userCount()+", Invited: "+allowed.size();
	}

}
