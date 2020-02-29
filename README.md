# 5-in-a-Row
Connect 4 (5-in-a-Row) Game

### Rule Description 
- 5-in-a-Row, a variation of the famous Connect Four game, is a Two players connection game.
- Two players choose the color (either **Red** or **Blue**) to take turns to fill the board with coins.
- The game has a 9 Column and 6 Row grid board game. The pieces fall straight down,
  occupying the next available space within the column. The objective of the game is to be the
  first to form a horizontal, vertical, or diagonal line of five of one's own discs.

## Prerequisites
Softwares to install
```
Eclipse
```
## Language Used
```
Java
```
## Features
- The communication between the clients and the server should be over HTTP.

### Server-side 
 - The server application holds the state and business logic of the game, 
   receiving the movements from the players and deciding whether a player has won, 
   or the game is over. The state of the game, and who’s turn it is, will be returned to the client upon request.
 - The server, upon start, waits for the two players to connect. If one of the players
   disconnects, the game is over.
   
### Client-side  
 - The client prompts the player to enter her name upon start, and displays whether it’s
   waiting for a 2nd player, or the game can start.
 - On each turn, the client displays the state of the board and prompts the
   corresponding player for input or displays that it’s waiting for the other player’s input
   *It’s your turn (Player's Name), please enter column (1-9):*
 - The client receives the input from the player from the standard input (stdin).
 - The client displays when the game is over, and the name of the winner.
 
## Assumptions & Limitations

## Author
- Tejashri Gunde - Developer/Reviewer
