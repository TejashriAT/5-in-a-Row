  package project.connect5inarow;

  import java.awt.*;
	import java.awt.event.*;
	import javax.swing.*;
	import javax.swing.border.LineBorder;
	import java.io.*;
	import java.net.*;
	
/**
 * This defines the ConnectFour (5-in-a-Row) game Client side code.
 *
 * @author Tejashri Gunde
 *
 */

	public class connectfourclient extends JApplet
	    implements Runnable, connectfourconstraints {
	  // Indicate whether the player has the turn
	  private boolean myTurn = false;
	

	  // Indicate the token for the player
	  private char myToken = ' ';
	

	  // Indicate the token for the other player
	  private char otherToken = ' ';
	

	  // Create and initialize cells with 6 Row and 9 Columns
	  private Cell[][] cell =  new Cell[6][9];
	

	  // Create and initialize a title label
	  private JLabel jlblTitle = new JLabel();
	

	  // Create and initialize a status labelf
	  private JLabel jlblStatus = new JLabel();
	

	  // Indicate selected row and column by the current move
	  private int rowSelected;
	  private int columnSelected;
	

	  // Input and output streams from/to server
	  private DataInputStream fromServer;
	  private DataOutputStream toServer;
	

	  // Continue to play?
	  private boolean continueToPlay = true;
	

	  // Wait for the player to mark a cell
	  private boolean waiting = true;
	

	  // Indicate if it runs as application
	  private boolean isStandAlone = false;
	

	  // Host name or ip
	  private String host = "localhost";
	

	  /** Initialize UI */
	  public void init() {
	    // Panel p to hold cells
	    JPanel p = new JPanel();
	    p.setLayout(new GridLayout(6, 9, 0, 0));
	    for (int i = 0; i < 6; i++)
	      for (int j = 0; j < 9; j++)
	        p.add(cell[i][j] = new Cell(i, j, cell));
	

	    // Set properties for labels and borders for labels and panel
	    p.setBorder(new LineBorder(Color.black, 1));
	    jlblTitle.setHorizontalAlignment(JLabel.CENTER);
	    jlblTitle.setFont(new Font("Arial", Font.BOLD, 14));
	    jlblTitle.setBorder(new LineBorder(Color.black, 1));
	    jlblStatus.setBorder(new LineBorder(Color.black, 1));
	

	    // Place the panel and the labels to the applet
	    add(jlblTitle, BorderLayout.NORTH);
	    add(p, BorderLayout.CENTER);
	    add(jlblStatus, BorderLayout.SOUTH);
	

	    // Connect to the server
	    connectToServer();
	  }
	

	  private void connectToServer() {
	    try {
	      // Create a socket to connect to the server
	      Socket socket;
	      if (isStandAlone)
	        socket = new Socket(host, 1234);
	      else
	        socket = new Socket(getCodeBase().getHost(), 1234);
	

	      // Create an input stream to receive data from the server
	      fromServer = new DataInputStream(socket.getInputStream());
	

	      // Create an output stream to send data to the server
	      toServer = new DataOutputStream(socket.getOutputStream());
	    }
	    catch (Exception ex) {
	      System.err.println(ex);
	    }
	

	    // Control the game on a separate thread
	    Thread thread = new Thread(this);
	    thread.start();
	  }
	

	  public void run() {
	    try {
	      // Get notification from the server
	      System.out.println("Player 1, please enter your name: ");
              String namePlayer1 = fromServer.readline();                
	      int player = fromServer.readInt();
	

	      // Am I player 1 or 2?
	      if (player == PLAYER1) {
	        myToken = 'r';
	        otherToken = 'b';
	        jlblTitle.setText("Player 1 with color red");
	        jlblStatus.setText("Waiting for player 2 to join");
	

	        // Receive startup notification from the server
		System.out.println("Player 2, please enter your name: ");
                String namePlayer2 = fromServer.readLine();
	        fromServer.readInt(); // Whatever read is ignored
	

	        // The other player has joined
	        jlblStatus.setText("Player 2 has joined. I start first");
	

	        // It is my turn
	        myTurn = true;
	      }
	      else if (player == PLAYER2) {
	        myToken = 'b';
	        otherToken = 'r';
	        jlblTitle.setText("Player 2 with color blue");
	        jlblStatus.setText("Waiting for player 1 to move");
	      }
	

	      // Continue to play
	      while (continueToPlay) {
	        if (player == PLAYER1) {
	          waitForPlayerAction(); // Wait for player 1 to move
	          sendMove(); // Send the move to the server
	          receiveInfoFromServer(); // Receive info from the server
	        }
	        else if (player == PLAYER2) {
	          receiveInfoFromServer(); // Receive info from the server
	          waitForPlayerAction(); // Wait for player 2 to move
	          sendMove(); // Send player 2's move to the server
	        }
	      }
	    }
	    catch (Exception ex) {
	    }
	  }
	

	  /** Wait for the player to mark a cell */
	  private void waitForPlayerAction() throws InterruptedException {
	    while (waiting) {
	      Thread.sleep(100);
	    }
	

	    waiting = true;
	  }
	

	  /** Send this player's move to the server */
	  private void sendMove() throws IOException {
	    toServer.writeInt(rowSelected); // Send the selected row
	    toServer.writeInt(columnSelected); // Send the selected column
	  }
	

	  /** Receive info from the server */
	  private void receiveInfoFromServer() throws IOException {
	    // Receive game status
	    int status = fromServer.readInt();
	

	    if (status == PLAYER1_WON) {
	      // Player 1 won, stop playing
	      continueToPlay = false;
	      if (myToken == 'r') {
	        jlblStatus.setText("I won! (red)");
	      }
	      else if (myToken == 'b') {
	        jlblStatus.setText("Player 1 (red) has won!");
	        receiveMove();
	      }
	    }
	    else if (status == PLAYER2_WON) {
	      // Player 2 won, stop playing
	      continueToPlay = false;
	      if (myToken == 'b') {
	        jlblStatus.setText("I won! (blue)");
	      }
	      else if (myToken == 'r') {
	        jlblStatus.setText("Player 2 (blue) has won!");
	        receiveMove();
	      }
	    }
	    else if (status == DRAW) {
	      // No winner, game is over
	      continueToPlay = false;
	      jlblStatus.setText("Game is over, no winner!");
	

	      if (myToken == 'b') {
	        receiveMove();
	      }
	    }
	    else {
	      receiveMove();
	      jlblStatus.setText("My turn");
	      myTurn = true; // It is my turn
	    }
	  }
	

	  private void receiveMove() throws IOException {
	    // Get the other player's move
	    int row = fromServer.readInt();
	    int column = fromServer.readInt();
	    cell[row][column].setToken(otherToken);
	  }
	

	  // An inner class for a cell
	  public class Cell extends JPanel {
	    // Indicate the row and column of this cell in the board
	    private int row;
	    private int column;
	    private Cell[][] cell;
	

	    // Token used for this cell
	    private char token = ' ';
	

	    public Cell(int row, int column, Cell[][] cell) {
	      this.row = row;
	      this.cell = cell;
	      this.column = column;
	      setBorder(new LineBorder(Color.black, 1)); // Set cell's border
	      addMouseListener(new ClickListener());  // Register listener
	

	    }
	

	    /** Return token */
	    public char getToken() {
	      return token;
	    }
	

	    /** Set a new token */
	    public void setToken(char c) {
	      token = c;
	      repaint();
	    }
	

	    /** Paint the cell */
	    protected void paintComponent(Graphics g) {
	      super.paintComponent(g);
	

	      if (token == 'r') {
	        g.drawOval(9, 9, getWidth() - 20, getHeight() - 20);
	        g.setColor(Color.red);
	        g.fillOval(9 ,9,  getWidth() - 20, getHeight() - 20);
	

	

	      }
	      else if (token == 'b') {
	        g.drawOval(10, 10, getWidth() - 20, getHeight() - 20);
	        g.setColor(Color.blue);
	        g.fillOval(9 ,9,  getWidth() - 20, getHeight() - 20);
	      }
	    }
	

	    /** Handle mouse click on a cell */
	    private class ClickListener extends MouseAdapter {
	      public void mouseClicked(MouseEvent e) {
	      	int r= -1;
	      	for(int x =5; x>= 0; x--){
	      		if(cell[x][column].getToken() == ' '){
	

	      			r=x;
	      			break;
	      		}
	      	}
	        // If cell is not occupied and the player has the turn
	        if ((r != -1) && myTurn) {
	          cell[r][column].setToken(myToken);  // Set the player's token in the cell
	          myTurn = false;
	          rowSelected = r;
	          columnSelected = column;
	          jlblStatus.setText("Waiting for the other player to move");
	          waiting = false; // Just completed a successful move
	        }
	      }
	    }
	  }
	

	  /** This main method enables the applet to run as an application */
	  public static void main(String[] args) {
	    // Create a frame
	    JFrame frame = new JFrame("Connect Four Client");
	

	    // Create an instance of the applet
	    connectfourclient applet = new connectfourclient();
	    applet.isStandAlone = true;
	

	    // Get host
	    if (args.length == 1) applet.host = args[0];
	

	    // Add the applet instance to the frame
	    frame.getContentPane().add(applet, BorderLayout.CENTER);
	

	    // Invoke init() and start()
	    applet.init();
	    applet.start();
	

	    // Display the frame
	    frame.setSize(640, 600);
	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    frame.setVisible(true);
	  }
	}

