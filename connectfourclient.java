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
	    implements Runnable, player, playerconstraints {
	  // Indicate whether the player has the turn
	  private boolean myTurn = false;
	  
	  // Indicate the name for the player
          private String name = null;
	  
	  // Indicate the coin for the player
	  private char myCoin = ' ';
	

	  // Indicate the Coin for the other player
	  private char otherCoin = ' ';
	

	  // Create and initialize cells with 6 Row and 9 Columns
	  private Cell[][] cell =  new Cell[6][9];
	

	  // Create and initialize a title label
	  private JLabel jlblTitle = new JLabel();
	
	  // Create and initialize player
	  private Player myplayer = new Player();

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
	

	  // Wait for the player to mark a cell or logically drop a coin
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
	

	  private void connectToServer() throws IOException {
	    try {
	      // Create a socket to connect to the server
	      Socket socket;
	      if (isStandAlone)
	        socket = new Socket(host, 80);// port 80 for HTTP
	      else
	        socket = new Socket(getCodeBase().getHost(), 80);
	

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
	      myplayer.setPlayerName("Player 1, please enter your name: ");// Prompt message to Player 1 to enter Name
              String namePlayer1 = myplayer.getPlayerName();                
	      int player = fromServer.readInt();
	

	      // Am I player 1 or 2?
	      if (player == PLAYER1) {
	                myCoin = 'r';
        		otherCoin = 'b';
        		jlblTitle.setText("Player 1 with color Red");
	       	        jlblStatus.setText("Waiting for player 2 to join");
	

	        // Receive startup notification from the server
		myplayer.setPlayerName("Player 2, please enter your name: ");// Prompt message to Player 2 to enter Name
              String namePlayer2 = myplayer.getPlayerName(); 
	        fromServer.readInt(); // Whatever read is ignored
	

	        // The other player has joined
	        jlblStatus.setText(+namePlayer2+" has joined. I start first");
	

	        // It is my turn
	        myTurn = true;
	      }
	      else if (player == PLAYER2) {
	        myCoin = 'b';
	        otherCoin = 'r';
	        jlblTitle.setText(+namePlayer2+" with color blue");
	        jlblStatus.setText("Waiting for "+nameplayer1+" to move");
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
	   catch (IOException e) {
	   // if one of the Players gets diconnected then display Game Over
	   if(player == PLAYER1)
	   {
	   jlblTitle.setText("GAME OVER ! " +namePlayer1+ " got disconnected");
	   }
	   else
		jlblTitle.setText("GAME OVER ! " +namePlayer2+ " got disconnected");
	  }
	   finally {
    		 fromServer.close();
     		 toServer.close();
    		 socket.close();
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
	      if (myCoin == 'r') {
	        jlblStatus.setText("I WON!"); // Red
	      }
	      else if (myCoin == 'b') {
	        jlblStatus.setText(+namePlayer1+" has WON!");
	        receiveMove();
	      }
	    }
	    else if (status == PLAYER2_WON) {
	      // Player 2 won, stop playing
	      continueToPlay = false;
	      if (myCoin == 'b') {
	        jlblStatus.setText("I WON!"); // Blue
	      }
	      else if (myCoin == 'r') {
	        jlblStatus.setText(+namePlayer2+ " has WON!");
	        receiveMove();
	      }
	    }
	    else if (status == DRAW) {
	      // No winner, game is over
	      continueToPlay = false;
	      jlblStatus.setText("Game is Over, NO winner!");
	

	      if (myCoin == 'b') {
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
	    cell[row][column].setCoin(otherCoin);
	  }
	

	  // An inner class for a cell
	  public class Cell extends JPanel {
	    // Indicate the row and column of this cell in the board
	    private int row;
	    private int column;
	    private Cell[][] cell;
	

	    // Token used for this cell
	    private char coin = ' ';
	

	    public Cell(int row, int column, Cell[][] cell) {
	      this.row = row;
	      this.cell = cell;
	      this.column = column;
	      setBorder(new LineBorder(Color.black, 1)); // Set cell's border
	      addMouseListener(new ClickListener());  // Register listener

	    }
	
	    /** Return Coin */
	    public char getCoin() {
	      return coin;
	    }
	
	    /** Set a new Coin */
	    public void setCoin(char c) {
	      coin = c;
	      repaint();
	    }
	   
	   @Override
	   /** Return PlayerName */
	    public String getPlayerName() {
	      return name;
	    }
	
	   @Override
	    /** Set a new PlayerName */
	    public void setPlayerName(String name) {
	      this.name = name;
	    }
	    
	    
	    /** Paint the cell */
	    protected void paintComponent(Graphics g) {
	      super.paintComponent(g);
	

	      if (coin == 'r') {
	        g.drawOval(9, 9, getWidth() - 20, getHeight() - 20);
	        g.setColor(Color.red);
	        g.fillOval(9 ,9,  getWidth() - 20, getHeight() - 20);

	      }
	      else if (coin == 'b') {
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
	      		if(cell[x][column].getCoin() == ' '){
	      			r=x;
	      			break;
	      		}
	      	}
	        // If cell is not occupied and the player has the turn
	        if ((r != -1) && myTurn) {
	          cell[r][column].setCoin(myCoin);  // Set the player's coin in the cell
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
	    JFrame frame = new JFrame("Connect 5-in-a-row Client");
	

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

