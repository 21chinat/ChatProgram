package net.tfobz.synchronization.chat.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;

public class ChatServerThread extends Thread
{
	private Socket client = null;
	private BufferedReader in = null;
	private PrintStream out = null;
	private ArrayList<PrintStream> room = null;
	
	public ChatServerThread(Socket client) throws IOException {
		this.client = client;
		in = new BufferedReader(new InputStreamReader(client.getInputStream()));
		out = new PrintStream(client.getOutputStream());
	}
	
	@Override
	public void run() {
		try {
			ChatServer.outputStreams.add(out);
			
			String name = in.readLine();
			if(name != null && name.length()>3) {
				System.out.println(name + " signed in. " + ChatServer.outputStreams.size() + " users");
				for (PrintStream outs: ChatServer.outputStreams)
					outs.println(name + " signed in");
				
				while (true) {
					String line = in.readLine();
					if (line == null)
						break;
					if(line.startsWith("/")) {
						if(line.startsWith("/msg ")) {
							for (String user : ChatServer.names) {
								if(line.contains(user))
									ChatServer.outputStreams.get()
							}
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
			}
		} catch (IOException e) {
			System.out.println(e.getClass().getName() + ": " + e.getMessage());
			e.printStackTrace();
			if (out != null)
				ChatServer.outputStreams.remove(out);
		} finally {
			try { client.close(); } catch (Exception e1) { ; }
		}
	}
}
