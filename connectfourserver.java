 package project.connect5inarow;

  	import java.io.*;
	import java.net.*;
	import javax.swing.*;
	import java.awt.*;
	import java.util.Date;
	import java.awt.event.*;
	
/**
 * This defines the ConnectFour (5-in-a-Row) game Server side code.
 *
 * @author Tejashri Gunde
 *
 */
	public class connectfourserver extends JFrame implements connectfourconstraints {
		JButton jbtConnect;
		JTextField iport;
		JTextArea jtaLog;
		JLabel e;
	
	  private class ButtonListener implements ActionListener {
	    public void actionPerformed(ActionEvent e) {
	

	System.out.println("INSIDE BUTTON LISTENER");
	      int port1 =Integer.parseInt(iport.getText());
	
			listen(port1);
	
	    }
	  }
	
	 private void listen(int port1){
	

	System.out.println("BEGIN LISTEN METHOD");
	    try {
	      // Create a server socket
	      jtaLog.append("WORK!");
	System.out.println("CONNECT");
	      ServerSocket serverSocket = new ServerSocket(port1);
	System.out.println("DONE");
	      jtaLog.append(new Date() + ": HTTP Server started at socket 80\n");
	
	      // Number a session
	      int sessionNo = 1;
	

	      // Ready to create a session for every two players
	      while (true) {
	        jtaLog.append(new Date() +
	          ": Wait for players to join session " + sessionNo + '\n');
	

	        // Connect to player 1
	System.out.println("GET P1");
	        Socket player1 = serverSocket.accept();
	System.out.println("DONE");
	        jtaLog.append(new Date() + ": Player 1 joined session " +
	          sessionNo + '\n');
	        jtaLog.append("Player 1's IP address" +
	          player1.getInetAddress().getHostAddress() + '\n');
	

	        // Notify that the player is Player 1
	        new DataOutputStream(
	          player1.getOutputStream()).writeInt(PLAYER1);
	

	        // Connect to player 2
	        Socket player2 = serverSocket.accept();
	

	        jtaLog.append(new Date() +
	          ": Player 2 joined session " + sessionNo + '\n');
	        jtaLog.append("Player 2's IP address" +
	          player2.getInetAddress().getHostAddress() + '\n');
	

	        // Notify that the player is Player 2
	        new DataOutputStream(
	          player2.getOutputStream()).writeInt(PLAYER2);
	

	        // Display this session and increment session number
	        jtaLog.append(new Date() + ": Start a thread for session " +
	          sessionNo++ + '\n');
	

	        // Create a new task for this session of two players
	        HandleASession task = new HandleASession(player1, player2);
	

	        // Start the new thread
	        new Thread(task).start();
	      }
	    }
	    catch(IOException ex) {
	      System.err.println(ex);
	    }
	    }
	
	  public static void main(String[] args) {
	
	   	connectfourserver frame = new connectfourserver();
	  }
	
	  public connectfourserver() {
	
	  	JPanel p1 = new JPanel();
		JPanel p2 = new JPanel(new GridLayout(1,2));


		jbtConnect= new JButton("Connect");
		iport = new JTextField(4);
		jtaLog = new JTextArea();
		e = new JLabel("Port:");
	
		p1.add(jtaLog);
		p2.add(jbtConnect);
		p2.add(e);
		p2.add(iport);
	
		jtaLog.append("Testing\n");
	    add(p1, BorderLayout.CENTER);
		add(p2, BorderLayout.SOUTH);
	
		jbtConnect.addActionListener(new ButtonListener());
	
	    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	
	    setSize(500, 700);
	    setTitle("Connect 5-in-a-row Server");
	    setVisible(true);
	
	  }
	}
	

	// Define the thread class for handling a new session for two players
	class HandleASession implements Runnable, connectfourconstraints {
	  private Socket player1;
	  private Socket player2;
	
	  // Create and initialize cells with 6 rows and 9 columns
	  private char[][] cell =  new char[6][9];
	

	  private DataInputStream fromPlayer1;
	  private DataOutputStream toPlayer1;
	  private DataInputStream fromPlayer2;
	  private DataOutputStream toPlayer2;
	

	  // Continue to play
	  private boolean continueToPlay = true;
	

	  /** Construct a thread */
	  public HandleASession(Socket player1, Socket player2) {
	    this.player1 = player1;
	    this.player2 = player2;
	

	    // Initialize cells
	    for (int i = 0; i < 6; i++) // 6 Rows
	      for (int j = 0; j < 9; j++) // 9 Columns
	        cell[i][j] = ' ';
	  }
	

	  /** Implement the run() method for the thread */
	  public void run() {
	    try {
	      // Create data input and output streams
	      DataInputStream fromPlayer1 = new DataInputStream(
	        player1.getInputStream());
	      DataOutputStream toPlayer1 = new DataOutputStream(
	        player1.getOutputStream());
	      DataInputStream fromPlayer2 = new DataInputStream(
	        player2.getInputStream());
	      DataOutputStream toPlayer2 = new DataOutputStream(
	        player2.getOutputStream());
	

	      // Write anything to notify player 1 to start
	      // This is just to let player 1 know to start
	      toPlayer1.writeInt(1);
	

	      // Continuously serve the players and determine and report
	      // the game status to the players
	      while (true) {
	        // Receive a move from player 1
	        int row = fromPlayer1.readInt();
	        int column = fromPlayer1.readInt();
	        char coin = 'r';
	

	        cell[row][column] = 'r';
	

	        // Check if Player 1 wins
	        if (isWon(row, column, coin)) {
	          toPlayer1.writeInt(PLAYER1_WON);
	          toPlayer2.writeInt(PLAYER1_WON);
	          sendMove(toPlayer2, row, column);
	          break; // Break the loop
	        }
	        else if (isFull()) { // Check if all cells are filled
	          toPlayer1.writeInt(DRAW);
	          toPlayer2.writeInt(DRAW);
	          sendMove(toPlayer2, row, column);
	          break;
	        }
	        else {
	          // Notify player 2 to take the turn
		  
		  System.out.print("It’s your turn " +fromPlayer1.getNamePlayer(player1)+ ", please enter column (1-9):");
	          toPlayer2.writeInt(CONTINUE);
	

	          // Send player 1's selected row and column to player 2
	          sendMove(toPlayer2, row, column);
	       }
	

	        // Receive a move from Player 2
	        row = fromPlayer2.readInt();
	        column = fromPlayer2.readInt();
	        cell[row][column] = 'b';
	

	        // Check if Player 2 wins
	        if (isWon(row, column, coin)) {
	          toPlayer1.writeInt(PLAYER2_WON);
	          toPlayer2.writeInt(PLAYER2_WON);
	          sendMove(toPlayer1, row, column);
	          break;
	        }
	        else {
	          // Notify player 1 to take the turn
		  System.out.print("It’s your turn " +toplayer1+ ", please enter column (1-9):");
	          toPlayer1.writeInt(CONTINUE);
	

	          // Send player 2's selected row and column to player 1
	          sendMove(toPlayer1, row, column);
	        }
	      }
	    }
	    catch(IOException ex) {
	      System.err.println(ex);
	    }
	  }
	

	  /** Send the move to other player */
	  private void sendMove(DataOutputStream out, int row, int column)
	      throws IOException {
	    out.writeInt(row); // Send row index
	    out.writeInt(column); // Send column index
	  }
	
	  /** Determine if the cells are all occupied */
	  private boolean isFull() {
	    for (int i = 0; i < 6; i++)
	      for (int j = 0; j < 9; j++)
	        if (cell[i][j] == ' ')
	          return false; // At least one cell is not filled
	

	    // All cells are filled
	    return true;
	  }	

	  /*Determine if the player with the specified coin wins */
	  private boolean isWon(int row, int column, char coin) {
	

	  	// TEST BOARD VALUES 6 x 9 = 54 grids
	  	for (int x = 0; x < 6; x++) {
			for (int y = 0; y < 9; y++) {
				System.out.print(cell[x][y]);
			}
			System.out.println();
		}
	
		//Horizontal wins check 5 in a row over 6 Rows
	
  for (int x = 0; x < 6; x++) {
			for (int y = 0; y < 4; y++) {
				if (cell[x][y] == coin && cell[x][y+1] == coin && cell[x][y+2] == coin && cell[x][y+3] == coin && cell[x][y+4] == coin) {
					return true;
				}
			}
		}
		// Vertical wins check 5 in a row over 9 columns
	
		for (int x = 0; x < 4; x++) {
			for (int y = 0; y < 9; y++) {
				if (cell[x][y] == coin && cell[x+1][y] == coin && cell[x+2][y] == coin && cell[x+3][y] == coin && cell[x+4][y] == coin) {
					return true;
				}
			}
		}
	
		// Diagonal wins check 5 in a row over 6 rows upwards
		//0 to 1
		//0 to 4
		for (int x = 0; x < 4; x++) {
			for (int y = 0; y < 6; y++) {
				if (cell[x][y] == coin && cell[x+1][y+1] == coin && cell[x+2][y+2] == coin && cell[x+3][y+3] == coin && cell[x+4][y+4] == coin) {
					return true;
				}
			}
		}

		//Other diagonal wins check 5 in a row over 9 columns downwards
		for (int x = 0; x < 4; x++) {
			for (int y = 3; y < 9; y++) {
				if (cell[x][y] == coin && cell[x+1][y-1] == coin && cell[x+2][y-2] == coin && cell[x+3][y-3] == coin && cell[x+4][y-4] == coin) {
					return true;
				}
			}
		}
		return false;
	  }
 }
