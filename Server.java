import java.io.*;
import java.net.*;

//----See header comment in Constants.java----//

/**
	Class for the server of Memory game
*/
public class Server implements Constants
{
	private int numberOfPlayers = 0;
	private ServerSocket server;
	
	/**
		Main method. Creates a new Server.
		@param args not used
	*/
	public static void main(String[] args) 
	{
		new Server();
	}
	
	/**
		Server's no arg constructor.
		Creates a server socket, waits for 2 players to connect
		and starts a new session when 2 players have conencted.
	*/
	public Server()
	{
		try
		{
			server = new ServerSocket(PORT);
			System.out.println("Waiting for clients to connect...");
			while(true)
			{
				Socket socket1 = server.accept();
				System.out.println("Client " + numberOfPlayers + " connected.");
				System.out.println("Player1: "  + socket1.getInetAddress().getHostAddress() + '\n');
				new DataOutputStream(socket1.getOutputStream()).writeInt(numberOfPlayers);
				numberOfPlayers++;
				
				Socket socket2 = server.accept();
				System.out.println("Client " + numberOfPlayers + " connected.");
				System.out.println("Player2: "  + socket2.getInetAddress().getHostAddress() + '\n');
				new DataOutputStream(socket2.getOutputStream()).writeInt(numberOfPlayers);
				numberOfPlayers++;
				
				System.out.println("Starting the game...");
				
				HandleASession task = new HandleASession(socket1, socket2);
				
				new Thread(task).start();
			}
		}
		catch(IOException e)
		{
			System.err.println(e);
		}
	}
}