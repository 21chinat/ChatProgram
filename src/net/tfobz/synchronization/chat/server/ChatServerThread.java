package net.tfobz.synchronization.chat.server;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;

import net.tfobz.synchronization.chat.ChatUser;

public class ChatServerThread extends Thread
{
	private Socket client = null;
	private BufferedReader in = null;
	private PrintStream out = null;
	private ChatUser user=null;
	private ArrayList<PrintStream> room = null;
	
	public ChatServerThread(Socket client) throws IOException {
		this.client = client;
		in = new BufferedReader(new InputStreamReader(client.getInputStream()));
		out = new PrintStream(client.getOutputStream());
	}
	
	@Override
	public void run() {
		try {
			
			String name = in.readLine();
			user = new ChatUser(name);
			
			ChatServer.outputStreams.add(out);

			
			System.out.println(name + " signed in. " + ChatServer.outputStreams.size() + " users");
			for (PrintStream outs: ChatServer.outputStreams)
				outs.println(name + " signed in");
			
			while (true) {
				String line = in.readLine();
				if (line == null)
					break;
				if(line.startsWith("/")) {
					if(line.startsWith("/msg ")) {
						for (int i = 0; i < ChatServer.names.size(); i++) {
							String user = ChatServer.names.get(i);
							if(line.contains(user))
								ChatServer.outputStreams.get(i).println("(Private)"+name + ": " + line);
						}
					}else if(line.startsWith("/stop ")) {
						
					}
				}else { 
					for (PrintStream outs: ChatServer.outputStreams)
						outs.println(name + ": " + line);
				}
			}
			
			ChatServer.outputStreams.remove(out);
			System.out.println(name + " signed out. " + ChatServer.outputStreams.size() + " users");
			for (PrintStream outs: ChatServer.outputStreams)
				outs.println(name + " signed out");
			
		} catch (IOException e) {
			System.out.println(e.getClass().getName() + ": " + e.getMessage());
			e.printStackTrace();
			if (out != null)
				ChatServer.outputStreams.remove(out);
		}catch (IllegalArgumentException e) {
			out.println(e.getMessage());
		} finally {
			try { client.close(); } catch (Exception e1) { ; }
		}
	}
}
