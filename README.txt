By default the program uses images from the images directory. Can use different directory or
numbers instead of images. The program connects to localhost by default, but the host can be
specified in command line arguments. The usage of command line arguments is specified in the user manual.

One bug I know of is when a player closes a window, the other player remains unaware of it and does not
win a game automatically because the other player closed the window.
Also note that upon pressing QUIT button, the window itself doesn't close, but the winner and and the loser
are picked. I decided to leave it open to see the messages in the labels. If the user wants to quit properly,
he/she needs to press QUIT first, and then close the window.
