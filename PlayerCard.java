import javax.swing.*;

//----See header comment in Constants.java----//

/**
	Customized JButton class used to represent cards.
*/
public class PlayerCard extends JButton
{
	private String back = "BACK";
	private String value;
	private int number;
	private boolean pressed = false;
	
	/**
		Creates a card with a number.
		@param number the number of the card
	*/
	public PlayerCard(int number)
	{
		this.number = number;
	}
	
	/**
		Gets the number of the card.
		@return number the number of the card
	*/
	public int getNumber()
	{
		return number;
	}
	
	/**
		Returns the back of the card.
		@return back the back of the card
	*/
	public String getBack()
	{
		return back;
	}
}