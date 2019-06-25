import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

//----See header comment in Constants.java----//

/**
	Class for handling a session of Memory game between two players.
*/
public class HandleASession implements Runnable, Constants
{
	private Socket player1;
	private Socket player2;
	private int numberOfMatchedPairs = 0;
	private boolean player1Turn = true;
	private boolean player2Turn = false;
	private boolean done = false;
	private int player1Matched = 0;
	private int player2Matched = 0;
	public String[] values = new String[NUMBER_OF_CARDS];
	
	private DataInputStream fromPlayer1;
	private DataOutputStream toPlayer1;
	private DataInputStream fromPlayer2;
	private DataOutputStream toPlayer2;
	
	/**
		Creates a new session with two sockets calls a method to shuffle the cards.
		@param socket1 The socket of the first player
		@param socket2 The socket of the second player
	*/
	public HandleASession(Socket socket1, Socket socket2)
	{
		player1 = socket1;
		player2 = socket2;
		
		shuffleValues();
	}
	
	/**
		Implements run method from Runnable interface.
		Creates 2 output streams and 2 input stream (1 for each player),
		and loops receiving commands from and sending responses to players.
	*/
	public void run()
	{
		try
		{
			fromPlayer1 = new DataInputStream(player1.getInputStream());
			toPlayer1 = new DataOutputStream(player1.getOutputStream());
			fromPlayer2 = new DataInputStream(player2.getInputStream());
			toPlayer2 = new DataOutputStream(player2.getOutputStream());
			
			while(!done)
			{
				//First, set whose turn it is now and show the number of pairs each player has matched.
				decideWhoseTurn();
				showProgress();
				
				//----------------------PLAYER 1------------------------------//
				
				//Receive the first move  from player1
				int move1 = fromPlayer1.readInt();
				
				//If player quits
				if(move1 == QUIT)
				{
					quit(1);
					win(2);
					done = true;
					break;
				}
				//Else make a move
				else if(move1 != QUIT)
				{
					showCard(move1);
				}
				
				//Receive the first move  from player1
				int move2 = fromPlayer1.readInt();
				
				if(move2 == QUIT)
				{
					quit(1);
					win(2);
					done = true;
					break;
				}
				else if(move2 != QUIT)
				{
					showCard(move2);
				}
				
				//Check if the player has matched the cards.
				//If not, flips the cards back after a pause.
				//If yes, disables the matched pair, increases the counter for matched pairs,
				//Updates the player's progress, and checks if the game is won. 
				//Stops the game if all cards have been matched and determines the winner.
				if(values[move1].equals(values[move2]))
				{
					disableCards(move1, move2);
					numberOfMatchedPairs += 1;
					player1Matched += 1;
					showProgress();
					if(checkIfGameIsWon())
					{
						decideAWinner();
						done = true;
						break;
					}
				}
				else
				{
					Thread thread = new Thread()
					{
						public void run()
						{
							try
							{
								Thread.sleep(3000);
								flipBack(move1, move2);
							}
							catch(InterruptedException exception)
							{
								System.err.println(exception);
							}
							catch(IOException ex)
							{
								System.err.println(ex);
							}
						}
					};
					thread.start();
				}
				//Switch turns
				player1Turn = false;
				player2Turn = true;
				decideWhoseTurn();
				
				//----------------------PLAYER 2------------------------------//
				
				//FOLLOWS THE SAME PATTERN AS PLAYER 1//
				int move3 = fromPlayer2.readInt();
				
				if(move3 == QUIT)
				{
					quit(2);
					win(1);
					done = true;
					break;
				}
				else if(move3 != QUIT)
				{
					showCard(move3);
				}
				
				int move4 = fromPlayer2.readInt();
				
				if(move4 == QUIT)
				{
					quit(2);
					win(1);
					done = true;
					break;
				}
				else if(move4 != QUIT)
				{
					showCard(move4);
				}
				
				if(values[move3].equals(values[move4]))
				{
					disableCards(move3, move4);
					numberOfMatchedPairs += 1;
					player2Matched += 1;
					showProgress();
					if(checkIfGameIsWon())
					{
						decideAWinner();
						done = true;
						break;
					}
				}
				else
				{
					Thread thread = new Thread()
					{
						public void run()
						{
							try
							{
								Thread.sleep(3000);
								flipBack(move3, move4);
							}
							catch(InterruptedException exception)
							{
								System.err.println(exception);
							}
							catch(IOException ex)
							{
								System.err.println(ex);
							}
						}
					};
					thread.start();
				}
				
				player1Turn = true;
				player2Turn = false;
			}
		}
		catch(IOException e)
		{
			System.err.println(e);
		}
	}
	
	/**
		Initializes a String array with values to be used for cards.
		Values are number from 0 to NUMBER_OF_CARDS.
		Then it shuffles the values inside the array.
	*/
	public void shuffleValues()
	{
		for(int i = 0; i < NUMBER_OF_CARDS / 2; i++)
		{
			values[i] = "" + i;
		}
		for(int i = NUMBER_OF_CARDS / 2; i < NUMBER_OF_CARDS; i++)
		{
			values[i] = values[i - NUMBER_OF_CARDS / 2];
		}
		
		Random random = new Random();
		for(int i = values.length - 1; i > 0; i--)
		{
			int index = random.nextInt(i + 1);
			
			String value = values[index];
			values[index] = values[i];
			values[i] = value;
		}
	}
	
	/**
		Reveals the value of a card to both players.
		@param move the number of the card to be revealed
	*/
	public void showCard(int move) throws IOException
	{
		toPlayer1.writeChar(SHOW);
		toPlayer1.writeInt(values[move].length());
		toPlayer1.writeChars(values[move]);
		toPlayer1.writeInt(move);
		toPlayer1.flush();
		
		toPlayer2.writeChar(SHOW);
		toPlayer2.writeInt(values[move].length());
		toPlayer2.writeChars(values[move]);
		toPlayer2.writeInt(move);
		toPlayer2.flush();
	}
	
	/**
		Disables 2 cards for both players.
		@param card1 the number of the first card to be disabled
		@param card2 the number of the second card to be disabled
	*/
	public void disableCards(int card1, int card2) throws IOException
	{
		toPlayer1.writeChar(DISABLE);
		toPlayer1.writeInt(card1);
		toPlayer1.writeInt(card2);
		toPlayer1.flush();
		
		toPlayer2.writeChar(DISABLE);
		toPlayer2.writeInt(card1);
		toPlayer2.writeInt(card2);
		toPlayer2.flush();
	}
	
	/**
		Sets the players' turns. Turn can be wither true or false.
	*/
	public void decideWhoseTurn() throws IOException
	{
		toPlayer1.writeChar(TURN);
		toPlayer1.writeBoolean(player1Turn);
		toPlayer1.flush();
		
		toPlayer2.writeChar(TURN);
		toPlayer2.writeBoolean(player2Turn);
		toPlayer2.flush();
	}
	
	/**
		Shows the players the number of pairs they have matched.
	*/
	public void showProgress() throws IOException
	{
		toPlayer1.writeChar(PROGRESS);
		toPlayer1.writeInt(player1Matched);
		toPlayer1.flush();
		
		toPlayer2.writeChar(PROGRESS);
		toPlayer2.writeInt(player2Matched);
		toPlayer2.flush();
	}
	
	/**
		Hides the value of 2 cards by flipping them back for both players.
		@param move1 the number of the first card to be disabled
		@param move2 the number of the second card to be disabled
	*/
	public void flipBack(int move1, int move2) throws IOException
	{
		toPlayer1.writeChar(BACK);
		toPlayer1.writeInt(move1);
		toPlayer1.writeInt(move2);
		toPlayer1.flush();
		
		toPlayer2.writeChar(BACK);
		toPlayer2.writeInt(move1);
		toPlayer2.writeInt(move2);
		toPlayer2.flush();
	}
	
	/**
		Quits the player from the game.
		@param player the number of player who requested to quit
	*/
	public void quit(int player) throws IOException
	{
		if(player == 1)
		{
			toPlayer1.writeChar(QUIT_SERVER);
			String message = "You quit the game. You lose!";
			toPlayer1.writeInt(message.length());
			toPlayer1.writeChars(message);
			toPlayer1.flush();
		}
		else if(player == 2)
		{
			toPlayer2.writeChar(QUIT_SERVER);
			String message = "You quit the game. You lose!";
			toPlayer2.writeInt(message.length());
			toPlayer2.writeChars(message);
			toPlayer2.flush();
		}
	}
	
	/**
		Notifies the player who won.
		@param player the player who won
	*/
	public void win(int player) throws IOException
	{
		if(player == 1)
		{
			toPlayer1.writeChar(WIN);
			String message = "You won! Congratulations!";
			toPlayer1.writeInt(message.length());
			toPlayer1.writeChars(message);
			toPlayer1.flush();
		}
		else if(player == 2)
		{
			toPlayer2.writeChar(WIN);
			String message = "You won! Congratulations!";
			toPlayer2.writeInt(message.length());
			toPlayer2.writeChars(message);
			toPlayer2.flush();
		}
	}
	
	/**
		Notifies the player who lost.
		@param player the player who lost
	*/
	public void lose(int player) throws IOException
	{
		if(player == 1)
		{
			toPlayer1.writeChar(LOSE);
			String message = "You lose! Good luck next time!";
			toPlayer1.writeInt(message.length());
			toPlayer1.writeChars(message);
			toPlayer1.flush();
		}
		else if(player == 2)
		{
			toPlayer2.writeChar(LOSE);
			String message = "You lose! Good luck next time!";
			toPlayer2.writeInt(message.length());
			toPlayer2.writeChars(message);
			toPlayer2.flush();
		}
	}
	
	/**
		Checks if the game is won/finished by checking if all pairs have been matched.
		@return true if it's finished, false otherwise.
	*/
	public boolean checkIfGameIsWon()
	{
		if(numberOfMatchedPairs == (NUMBER_OF_CARDS / 2))
		{
			return true;
		}
		return false;
	}
	
	/**
		Decides who the winner is.
		The player who has matched more pairs wins.
		If both players matched the same number of pairs,
		the second player (who connected last) wins.
	*/
	public void decideAWinner() throws IOException
	{
		if(player1Matched > player2Matched)
		{
			win(1);
			lose(2);
		}
		else if(player2Matched > player1Matched)
		{
			win(2);
			lose(1);
		}
		else if(player2Matched == player1Matched)
		{
			win(2);
			lose(1);
		}
	}
}