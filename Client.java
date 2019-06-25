import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import java.io.*;
import java.net.*;

//----See header comment in Constants.java----//

/**
	Class for the client/player of the Memory game.
*/
public class Client implements Runnable, Constants
{
	private int WIDTH = 625;
	private int HEIGHT = 800;
	private boolean myTurn = false;
	private int playerNumber;
	private boolean done = false;
	private JFrame playerWindow;
	private JPanel buttonPanel;
	private JPanel lowerPanel;
	private JPanel overallPanel;
	private JLabel warning;
	private JLabel currentTurn;
	private JLabel result;
	private PlayerCard[] buttons = new PlayerCard[NUMBER_OF_CARDS];
	private int[] numbers = new int[NUMBER_OF_CARDS];
	private DataInputStream fromServer;
	private DataOutputStream toServer;
	private static String imagesDirectory = "images/";
	private static String host = "localhost";
	private static boolean images = true;
	private String back = "PlayerBackOfCard";
	private boolean help = false;
	
	/**
		Creates a new Client.
		Uses command line arguments in the following manner:
		Client [-server hostAddress] [-img NONE | directoryPath] [-help]
		If no command line arguments were provided, uses images as values of cards
		from the images directory and connects to localhost by default.
		If command line arguments were used incorrectly, shows the correct usage
		and DOES NOT create a new Client.
		@param args command line arguments.
	*/
	public static void main(String[] args)
	{
		boolean help = false;
		if(args.length == 1)
		{
			help = true;
			usage();
		}
		else if(args.length > 1)
		{
			for(int i = 0; i < args.length; i++)
			{
				if(args[i].equals("-server"))
				{
					if(i + 1 < args.length)
					{
						setHost(args[i + 1]);
						i++;
					}
					else
					{
						help = true;
						usage();
						break;
					}
				}
				else if(args[i].equals("-img"))
				{
					if(i + 1 < args.length)
					{
						if(args[i + 1].equals("NONE"))
						{
							setImages(false);
							i++;
						}
						else
						{
							setDirectory(args[i + 1]);
							i++;
						}
					}
					else
					{
						help = true;
						usage();
						break;
					}
				}
				else
				{
					help = true;
					usage();
					break;
				}
			}
		}
		if(!help)
		{
			new Client();
		}
	}
	
	/**
		Shows the correct usage of command line arguments.
	*/
	public static void usage()
	{
		System.out.println("Incorrect usage of Client.");
		System.out.println("Usage: Client [-server hostAddress] [-img NONE | directoryPath] [-help]");
	}
	
	/**
		Sets the address of the host to connect to.
		@param newHost the host to connect to
	*/
	public static void setHost(String newHost)
	{
		host = newHost;
	}
	
	/**
		Sets if images are to be used or not.
		@param set the value to set(true/false) if images should be used
	*/
	public static void setImages(boolean set)
	{
		images = set;
	}
	
	/**
		Sets the directory where the images are taken from.
		@param dir the directory path where the images are stored
	*/
	public static void setDirectory(String dir)
	{
		imagesDirectory = dir + "/";
	}
	
	/**
		Creates a new Client.
		Creates the frame with all necessary elements (labels, buttons, etc.).
	*/
	public Client()
	{
		initializeButtons();
		overallPanel = new JPanel(new BorderLayout());
		buttonPanel = new JPanel(new GridLayout(5, 5));
		for(int i = 0; i < NUMBER_OF_CARDS; i++)
		{
			if(!images)
			{
				buttons[i].setText("BACK");
			}
			else
			{
				buttons[i].setIcon(new ImageIcon(imagesDirectory + back + ".jpg"));
			}
			buttonPanel.add(buttons[i]);
		}
		lowerPanel = new JPanel(new GridLayout(4, 1));
		JButton quit = new JButton("QUIT");
		
		quit.addActionListener(e -> 
		{
			try
			{
				quit();
			}
			catch(IOException ex)
			{
				System.err.println(ex);
			}
		});
		lowerPanel.add(quit);
		warning = new JLabel();
		currentTurn = new JLabel();
		result = new JLabel();
		result.setText("Number of matched pairs: " + 0);
		lowerPanel.add(currentTurn);
		lowerPanel.add(warning);
		lowerPanel.add(result);
		overallPanel.add(buttonPanel, BorderLayout.CENTER);
		overallPanel.add(lowerPanel, BorderLayout.SOUTH);
		
		playerWindow = new JFrame();
		playerWindow.setSize(WIDTH, HEIGHT);
		playerWindow.setTitle("Player Window");
		playerWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		playerWindow.add(overallPanel);
		playerWindow.setVisible(true);
		
		connectToServer();
	}
	
	/**
		Initializes the array of PlayerCards and attaches listeners to each card.
	*/
	public void initializeButtons()
	{
		
		for(int i = 0; i < NUMBER_OF_CARDS; i++)
		{
			int j = i;
			buttons[i] = new PlayerCard(i);
			buttons[i].addActionListener(e -> 
			{
				try
				{
					flipCard(j);
				}
				catch(IOException ex)
				{
					System.err.println(ex);
				}
			});
		}
	}
	
	/**
		Connects to the server.
	*/
	public void connectToServer()
	{
		try
		{
			Socket socket = new Socket(host, PORT);
			fromServer = new DataInputStream(socket.getInputStream());
			toServer = new DataOutputStream(socket.getOutputStream());
		}
		catch(Exception e)
		{
			System.err.println(e);
		}
		
		Thread thread = new Thread(this);
		thread.start();
	}
	
	/**
		Implements run method from Runnable interface.
		Listens to requests from the server and executes commands
		corresponding to the server's request.
	*/
	public void run()
	{
		try
		{
			//Receives the player number
			playerNumber = fromServer.readInt();
			
			while(!done)
			{
				//Reads the command from the server
				//by receiving a character representing the command.
				char msg = fromServer.readChar();
				
				//Sets if this player takes current turn
				//The status is displayed in one of the labels.
				if(msg == TURN)
				{
					myTurn = fromServer.readBoolean();
				        if(myTurn)
				        {
						currentTurn.setText("YOUR TURN");
						warning.setOpaque(false);
						warning.setText("");
				        }
					else
					{
						currentTurn.setText("OTHER PLAYER'S TURN");
					}
				}
				
				//Reveals the value/face of the card
				if(msg == SHOW)
				{
					int length = fromServer.readInt();
					String message = "";
					for(int i = 0; i < length; i++)
					{
						message += fromServer.readChar();
					}
					int card = fromServer.readInt();
					if(!images)
					{
						buttons[card].setText(message);
					}
					else
					{
						buttons[card].setIcon(new ImageIcon(imagesDirectory + message + ".jpg"));
					}
				}
				
				//Disables two cards (if the pair was matched).
				else if(msg == DISABLE)
				{
					int card1 = fromServer.readInt();
					int card2 = fromServer.readInt();
					buttons[card1].setEnabled(false);
					buttons[card2].setEnabled(false);
				}
				
				//Shows how many pairs this player has matched.
				//The result is displayed in one of the labels.
				else if(msg == PROGRESS)
				{
					int progress = fromServer.readInt();
					result.setText("Number of matched pairs: " + progress);
				}
				
				//Flips the cards back (hides their values)
				else if(msg == BACK)
				{
					int card1 = fromServer.readInt();
					int card2 = fromServer.readInt();
					if(!images)
					{
						String back = buttons[card1].getBack();
						buttons[card1].setText(back);
						buttons[card2].setText(back);
					}
					else
					{
						buttons[card1].setIcon(new ImageIcon(imagesDirectory + back + ".jpg"));
						buttons[card2].setIcon(new ImageIcon(imagesDirectory + back + ".jpg"));
					}
				}
				
				//Quits from the game and loses the game. 
				//Notifies the player in a label. The game is over.
				else if(msg == QUIT_SERVER)
				{
					int length = fromServer.readInt();
					String message = "";
					for(int i = 0; i < length; i++)
					{
						message += fromServer.readChar();
					}
					result.setOpaque(true);
					result.setBackground(Color.RED);
					result.setText(message);
					done = true;
					break;
				}
				
				//Wins the game. 
				//Notifies the player in a label. The game is over.
				else if(msg == WIN)
				{
					int length = fromServer.readInt();
					String message = "";
					for(int i = 0; i < length; i++)
					{
						message += fromServer.readChar();
					}
					result.setOpaque(true);
					result.setBackground(Color.GREEN);
					result.setText(message);
					done = true;
					break;
				}
				
				//Loses the game. 
				//Notifies the player in a label. The game is over.
				else if(msg == LOSE)
				{
					int length = fromServer.readInt();
					String message = "";
					for(int i = 0; i < length; i++)
					{
						message += fromServer.readChar();
					}
					result.setOpaque(true);
					result.setBackground(Color.RED);
					result.setText(message);
					done = true;
					break;
				}
			}
		}
		catch(Exception e)
		{
			System.err.println(e);
		}
	}
	
	/**
		Requests the server to flip/reveal the card.
		If this is not this player's turn, the player is notified in one
		of the labels and request to the server is not sent.
		@param number the number of the card to flip/reveal
	*/
	public void flipCard(int number) throws IOException
	{
		if(myTurn)
		{
			toServer.writeInt(number);
		}
		else
		{
			warning.setOpaque(true);
			warning.setBackground(Color.RED);
			warning.setText("This is not your turn");
		}
	}
	
	/**
		Requests the server to quit the game.
		If this is not this player's turn, the player is notified in one
		of the labels and request to the server is not sent.
	*/
	public void quit() throws IOException
	{
		if(myTurn)
		{
			toServer.writeInt(QUIT);
		}
		else
		{
			warning.setOpaque(true);
			warning.setBackground(Color.RED);
			warning.setText("This is not your turn");
		}
	}
}