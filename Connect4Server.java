package project.
  import java.io.*;
	import java.net.*;
	import javax.swing.*;
	import java.awt.*;
	import java.util.Date;
	import java.awt.event.*;
	

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
	      jtaLog.append("WORK!!!!");
	System.out.println("CONNECT");
	      ServerSocket serverSocket = new ServerSocket(port1);
	System.out.println("DONE");
	      jtaLog.append(new Date() + ": Server started at socket 1234\n");
	
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
	
		jtaLog.append("TEsting\n");
	    add(p1, BorderLayout.CENTER);
		add(p2, BorderLayout.SOUTH);
	
		jbtConnect.addActionListener(new ButtonListener());
	
	    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	
	    setSize(500, 700);
	    setTitle("Connect Four Server");
	    setVisible(true);
	
	  }
	}
	

	// Define the thread class for handling a new session for two players
	class HandleASession implements Runnable, connectfourconstraints {
	  private Socket player1;
	  private Socket player2;
	

	  // Create and initialize cells
	  private char[][] cell =  new char[6][7];
	

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
	    for (int i = 0; i < 6; i++)
	      for (int j = 0; j < 7; j++)
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
	        char token = 'r';
	

	        cell[row][column] = 'r';
	

	        // Check if Player 1 wins
	        if (isWon(row, column, token)) {
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
	          toPlayer2.writeInt(CONTINUE);
	

	          // Send player 1's selected row and column to player 2
	          sendMove(toPlayer2, row, column);
	       }
	

	        // Receive a move from Player 2
	        row = fromPlayer2.readInt();
	        column = fromPlayer2.readInt();
	        cell[row][column] = 'b';
	

	        // Check if Player 2 wins
	        if (isWon(row, column, token)) {
	          toPlayer1.writeInt(PLAYER2_WON);
	          toPlayer2.writeInt(PLAYER2_WON);
	          sendMove(toPlayer1, row, column);
	          break;
	        }
	        else {
	          // Notify player 1 to take the turn
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
	      for (int j = 0; j < 7; j++)
	        if (cell[i][j] == ' ')
	          return false; // At least one cell is not filled
	

	    // All cells are filled
	    return true;
	  }	

	  /*Determine if the player with the specified token wins */
	  private boolean isWon(int row, int column, char token) {
	

	  	// TEST BOARD VALUES
	  	for (int x = 0; x < 5; x++) {
			for (int y = 0; y < 6; y++) {
				System.out.print(cell[x][y]);
			}
			System.out.println();
		}
	
		//Horizontal wins
	
  for (int x = 0; x < 6; x++) {
			for (int y = 0; y < 3; y++) {
				if (cell[x][y] == token && cell[x][y+1] == token && cell[x][y+2] == token && cell[x][y+3] == token) {
					return true;
				}
			}
		}
		// Vertical wins
	
		for (int x = 0; x < 3; x++) {
			for (int y = 0; y < 7; y++) {
				if (cell[x][y] == token && cell[x+1][y] == token && cell[x+2][y] == token && cell[x+3][y] == token) {
					return true;
				}
			}
		}
	
		// Diagonal wins
		//0 to 1
		//0 to 3
		for (int x = 0; x < 3; x++) {
			for (int y = 0; y < 6; y++) {
				if (cell[x][y] == token && cell[x+1][y+1] == token && cell[x+2][y+2] == token && cell[x+3][y+3] == token) {
					return true;
				}
			}
		}

		//Other diagonal wins
		for (int x = 0; x < 3; x++) {
			for (int y = 3; y < 7; y++) {
				if (cell[x][y] == token && cell[x+1][y-1] == token && cell[x+2][y-2] == token && cell[x+3][y-3] == token) {
					return true;
				}
			}
		}
		return false;
	  }
 }
