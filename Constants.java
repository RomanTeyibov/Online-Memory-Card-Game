/*
* Name: Roman Teyibov
* Student number: 100269695
* CPSC 1181
* Section 001
* Purpose of the program:
* - To incorporate into a project what you have learned throughout the course
*	- design of objects
* 	- inheritance
*	- Java interfaces
*	- network programming
*	- multi threading
*	- graphical user interfaces
*	- input and output streams
*	- command line arguments
*	- possibly lambda functions
*	- exception handling
*	- event handling
* - To write a ‘longer program’ and to document it properly – all under time constraints
* which is typical in software development
* - To incorporate one element of Java programming not covered in class – the sounds
* (noises) in case you have time to do the bonus and to document (cite) the source
*/


/**
	Interface for constants shared by both the client and the server.
*/
public interface Constants
{
	int NUMBER_OF_CARDS = 20;
	int PORT = 1181;
	char SHOW = 's';
	char DISABLE = 'd';
	char TURN = 't';
	char PROGRESS = 'p';
	char BACK = 'b';
	char QUIT_SERVER = 'q';
	char WIN = 'w';
	char LOSE = 'l';
	int QUIT = -1;
}