/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.math.BigDecimal;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */

public class Ticketmaster{
	//reference to physical database connection
	private Connection _connection = null;
	static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	
	public Ticketmaster(String dbname, String dbport, String user, String passwd) throws SQLException {
		System.out.print("Connecting to database...");
		try{
			// constructs the connection URL
			String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
			System.out.println ("Connection URL: " + url + "\n");
			
			// obtain a physical connection
	        this._connection = DriverManager.getConnection(url, user, passwd);
	        System.out.println("Done");
		}catch(Exception e){
			System.err.println("Error - Unable to Connect to Database: " + e.getMessage());
	        System.out.println("Make sure you started postgres on this machine");
	        System.exit(-1);
		}
	}
	
	/**
	 * Method to execute an update SQL statement.  Update SQL instructions
	 * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
	 * 
	 * @param sql the input SQL string
	 * @throws java.sql.SQLException when update failed
	 * */
	public void executeUpdate (String sql) throws SQLException { 
		// creates a statement object
		Statement stmt = this._connection.createStatement ();

		// issues the update instruction
		stmt.executeUpdate (sql);

		// close the instruction
	    stmt.close ();
	}//end executeUpdate

	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and outputs the results to
	 * standard out.
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQueryAndPrintResult (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		/*
		 *  obtains the metadata object for the returned result set.  The metadata
		 *  contains row and column info.
		 */
		ResultSetMetaData rsmd = rs.getMetaData ();
		int numCol = rsmd.getColumnCount ();
		int rowCount = 0;
		
		//iterates through the result set and output them to standard out.
		boolean outputHeader = true;
		while (rs.next()){
			if(outputHeader){
				for(int i = 1; i <= numCol; i++){
					System.out.print(rsmd.getColumnName(i) + "\t");
			    }
			    System.out.println();
			    outputHeader = false;
			}
			for (int i=1; i<=numCol; ++i)
				System.out.print (rs.getString (i) + "\t");
			System.out.println ();
			++rowCount;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the results as
	 * a list of records. Each record in turn is a list of attribute values
	 * 
	 * @param query the input query string
	 * @return the query result as a list of records
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException { 
		//creates a statement object 
		Statement stmt = this._connection.createStatement (); 
		
		//issues the query instruction 
		ResultSet rs = stmt.executeQuery (query); 
	 
		/*
		 * obtains the metadata object for the returned result set.  The metadata 
		 * contains row and column info. 
		*/ 
		ResultSetMetaData rsmd = rs.getMetaData (); 
		int numCol = rsmd.getColumnCount (); 
		int rowCount = 0; 
	 
		//iterates through the result set and saves the data returned by the query. 
		boolean outputHeader = false;
		List<List<String>> result  = new ArrayList<List<String>>(); 
		while (rs.next()){
			List<String> record = new ArrayList<String>(); 
			for (int i=1; i<=numCol; ++i) 
				record.add(rs.getString (i)); 
			result.add(record); 
		}//end while 
		stmt.close (); 
		return result; 
	}//end executeQueryAndReturnResult
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the number of results
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQuery (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		int rowCount = 0;

		//iterates through the result set and count nuber of results.
		if(rs.next()){
			rowCount++;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to fetch the last value from sequence. This
	 * method issues the query to the DBMS and returns the current 
	 * value of sequence used for autogenerated keys
	 * 
	 * @param sequence name of the DB sequence
	 * @return current value of a sequence
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	
	public int getCurrSeqVal(String sequence) throws SQLException {
		Statement stmt = this._connection.createStatement ();
		
		ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
		if (rs.next()) return rs.getInt(1);
		return -1;
	}

	/**
	 * Method to close the physical connection if it is open.
	 */
	public void cleanup(){
		try{
			if (this._connection != null){
				this._connection.close ();
			}//end if
		}catch (SQLException e){
	         // ignored.
		}//end try
	}//end cleanup

	/**
	 * The main execution method
	 * 
	 * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
	 */
	public static void main (String[] args) {
		if (args.length != 3) {
			System.err.println (
				"Usage: " + "java [-classpath <classpath>] " + Ticketmaster.class.getName () +
		            " <dbname> <port> <user>");
			return;
		}//end if
		
		Ticketmaster esql = null;
		
		try{
			System.out.println("(1)");
			
			try {
				Class.forName("org.postgresql.Driver");
			}catch(Exception e){

				System.out.println("Where is your PostgreSQL JDBC Driver? " + "Include in your library path!");
				e.printStackTrace();
				return;
			}
			
			System.out.println("(2)");
			String dbname = args[0];
			String dbport = args[1];
			String user = args[2];
			
			esql = new Ticketmaster (dbname, dbport, user, "");
			
			boolean keepon = true;
			while(keepon){
				System.out.println("MAIN MENU");
				System.out.println("---------");
				System.out.println("1. Add User");
				System.out.println("2. Add Booking");
				System.out.println("3. Add Movie Showing for an Existing Theater");
				System.out.println("4. Cancel Pending Bookings");
				System.out.println("5. Change Seats Reserved for a Booking");
				System.out.println("6. Remove a Payment");
				System.out.println("7. Clear Cancelled Bookings");
				System.out.println("8. Remove Shows on a Given Date");
				System.out.println("9. List all Theaters in a Cinema Playing a Given Show");
				System.out.println("10. List all Shows that Start at a Given Time and Date");
				System.out.println("11. List Movie Titles Containing \"love\" Released After 2010");
				System.out.println("12. List the First Name, Last Name, and Email of Users with a Pending Booking");
				System.out.println("13. List the Title, Duration, Date, and Time of Shows Playing a Given Movie at a Given Cinema During a Date Range");
				System.out.println("14. List the Movie Title, Show Date & Start Time, Theater Name, and Cinema Seat Number for all Bookings of a Given User");
				System.out.println("15. EXIT");
				
				/*
				 * FOLLOW THE SPECIFICATION IN THE PROJECT DESCRIPTION
				 */
				switch (readChoice()){
					case 1: AddUser(esql); break;
					case 2: AddBooking(esql); break;
					case 3: AddMovieShowingToTheater(esql); break;
					case 4: CancelPendingBookings(esql); break;
					case 5: ChangeSeatsForBooking(esql); break;
					case 6: RemovePayment(esql); break;
					case 7: ClearCancelledBookings(esql); break;
					case 8: RemoveShowsOnDate(esql); break;
					case 9: ListTheatersPlayingShow(esql); break;
					case 10: ListShowsStartingOnTimeAndDate(esql); break;
					case 11: ListMovieTitlesContainingLoveReleasedAfter2010(esql); break;
					case 12: ListUsersWithPendingBooking(esql); break;
					case 13: ListMovieAndShowInfoAtCinemaInDateRange(esql); break;
					case 14: ListBookingInfoForUser(esql); break;
					case 15: keepon = false; break;
				}
			}
		}catch(Exception e){
			System.err.println (e.getMessage ());
		}finally{
			try{
				if(esql != null) {
					System.out.print("Disconnecting from database...");
					esql.cleanup ();
					System.out.println("Done\n\nBye !");
				}//end if				
			}catch(Exception e){
				// ignored.
			}
		}
	}

	public static int readChoice() {
		int input;
		// returns only if a correct value is given.
		do {
			System.out.print("Please make your choice: ");
			try { // read the integer, parse it and break.
				input = Integer.parseInt(in.readLine());
				break;
			}catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			}//end try
		}while (true);
		return input;
	}//end readChoice
	
	public static void AddUser(Ticketmaster esql){//1
	
		try {
			System.out.print("Please enter Email: ");
			String email = in.readLine();

			System.out.print("Please enter Last Name: ");
			String lname = in.readLine();

			System.out.print("Please Enter First name: ");
			String fname = in.readLine();

			System.out.print("Please Enter Phone Number: ");
			BigDecimal phone = new BigDecimal(in.readLine());

			System.out.print("Enter Password: ");
			String pwd = in.readLine();

			String query = String.format("INSERT INTO Users (email, lname, fname, phone, pwd) VALUES ('%s' ,'%s', '%s', %.2f, '%s')", email, lname, fname, phone, pwd);
			esql.executeUpdate(query);
			System.out.print("Account Created Successfully\n");
			System.out.print("========================================================\n");
		}catch(Exception err) {
			System.err.println(err.getMessage());
		}

	}
	
	public static void AddBooking(Ticketmaster esql){//2
		try{
			List <List<String>> data = esql.executeQueryAndReturnResult("Select bid from Bookings");

			//CREATE BOOKING ID
			int bid = data.size() + 1;
			System.out.print("Booking ID: " + Integer.toString(bid) + "\n" );

			//ENTERING USER EMAIL ====================================================================================================================================
			System.out.print("Enter User Email: ");
			String email = in.readLine();
			
			int row = (esql.executeQueryAndReturnResult("Select email from Users where email = \'" + email + "\'")).size();
			
			while(row == 0) {
				System.out.print("Invalid email, Enter User Email: ");
				email = in.readLine();
				row = (esql.executeQueryAndReturnResult("Select email from Users where email = \'" + email + "\'")).size();
			}

			//ENTERING CINEMA and THEATER ID ==========================================================================================================================
			System.out.print("Enter Cinema ID: ");
			String cid = in.readLine();

			System.out.print("Enter Theater ID: ");
			String tid = in.readLine();

			row = (esql.executeQueryAndReturnResult("Select tid from Theaters where tid = \'" + tid + "\' AND cid = \'" + cid + "\'" )).size();

			while(row == 0) {
				System.out.print("Invalid Cinema ID or Theater ID. \n");

				System.out.print("Enter Cinema ID: ");
				cid = in.readLine();

				System.out.print("Enter Theater ID: ");
				tid = in.readLine();

				row = (esql.executeQueryAndReturnResult("Select tid from Theaters where tid = \'" + tid + "\' AND cid = \'" + cid + "\'" )).size();
			}

			//ENTERTING MOVIE ID AND SHOW ID =========================================================================================================================
			System.out.print("Enter show ID: ");
			String sid = in.readLine();

			System.out.print("Enter Movie ID: ");
			String mvid = in.readLine();

			row = (esql.executeQueryAndReturnResult("Select sid from Shows where sid = \'" + sid + "\' AND mvid = \'" + mvid + "\'" )).size();

			while(row == 0) {
				System.out.print("Invalid Movie ID or Movie ID. \n");

				System.out.print("Enter show ID: ");
				sid = in.readLine();

				System.out.print("Enter Movie ID: ");
				mvid = in.readLine();

				row = (esql.executeQueryAndReturnResult("Select sid from Shows where sid = \'" + sid + "\' AND mvid = \'" + mvid + "\'" )).size();
			}

			//Checks if the theater plays that show =====================================================================================================================
			row = (esql.executeQueryAndReturnResult("Select sid from Plays where sid = \'" + sid + "\' AND tid = \'" + tid + "\'" )).size();
			if(row == 0) {
				System.out.print("Show ID and Theater ID does not match. Please try again. \n");
				System.out.print("========================================================\n");
				return;
			}



			//ENTERING Number of SEATS ==================================================================================================================================
			int seats = 0;
			System.out.print("Enter Number of Seats: ");
			while(seats == 0) {
				try{
					seats = Integer.parseInt(in.readLine());
				}catch (NumberFormatException e) {
					System.out.print("Enter a Valid Number of Seats: ");
					seats = 0;
				}
			}

			int count = 1;

			//CREATING BOOKING ==========================================================================================================================================
						String time = new SimpleDateFormat("MM/dd/YYYY HH:mm").format(new Date());
						String temp = String.format("Insert into Bookings VALUES (%d, '%s', '%s', %d, '%s', '%s')", bid, "pending", time, seats, sid, email);
						esql.executeUpdate(temp);

			//ENTERING SHOW SEAT ID =====================================================================================================================================
			while(count <= seats) {
				System.out.print("Enter Show Seat ID for Seat #" + count + ": ");
				String ssid = in.readLine();

				row = (esql.executeQueryAndReturnResult("Select ssid from ShowSeats where ssid = " + ssid + " AND sid = " + sid + " AND bid IS NULL")).size();

				while(row == 0) {
					System.out.print("Invalid Show Seat ID.\n");
					row = (esql.executeQueryAndReturnResult("Select ssid from ShowSeats where ssid = " + ssid + " AND sid = " + sid  + " AND bid IS NULL")).size();
					System.out.print("Enter Show Seat ID for Seat #" + count + ": ");
					ssid = in.readLine();
				}
				esql.executeUpdate("Update ShowSeats SET bid = " + bid + " WHERE ssid = " + ssid + " AND sid = " + sid);

				count++;
			}

			System.out.print("Booking created at time: " + time + "\n");
			System.out.print("========================================================\n");

			
			
		}catch (Exception err) {
			System.err.println(err.getMessage());
		}
	}
	
	public static void AddMovieShowingToTheater(Ticketmaster esql){//3
		
	}
	
	public static void CancelPendingBookings(Ticketmaster esql){//4
		try {
			esql.executeUpdate("Update ShowSeats SET bid = NULL where bid = (select bid from bookings where status = 'pending')");
			esql.executeUpdate("Update Bookings SET status = 'cancelled' where status = 'pending' ");
			System.out.print("Cancelled all pending bookings\n");
			System.out.print("========================================================\n");
		}catch(Exception err) {
			System.err.println(err.getMessage());
		}
	}
	
	public static void ChangeSeatsForBooking(Ticketmaster esql) throws Exception{//5
		
	}
	
	public static void RemovePayment(Ticketmaster esql){//6
		System.out.print("Please Enter Booking ID that you would like to cancel: ");
		String bid = in.readLine();

		int row = esql.executeQueryAndReturnResult("Select bid from bookings where status = 'paid' AND bid = " + bid);
		while(row == 0) {
			System.out.print("Invalid Booking ID.\n");
			System.out.print("Please Enter Booking ID that you would like to cancel: ");
			bid = in.readLine();
			row = esql.executeQueryAndReturnResult("Select bid from bookings where status = 'paid' AND bid = " + bid);
		}

		esql.executeUpdate("Delete From Payments where bid = " + bid);

	}
	
	public static void ClearCancelledBookings(Ticketmaster esql){//7
		
	}
	
	public static void RemoveShowsOnDate(Ticketmaster esql){//8
		
	}
	
	public static void ListTheatersPlayingShow(Ticketmaster esql){//9
		//
		
	}
	
	public static void ListShowsStartingOnTimeAndDate(Ticketmaster esql){//10
		//
		
	}

	public static void ListMovieTitlesContainingLoveReleasedAfter2010(Ticketmaster esql){//11
		//
		
	}

	public static void ListUsersWithPendingBooking(Ticketmaster esql){//12
		//
		
	}

	public static void ListMovieAndShowInfoAtCinemaInDateRange(Ticketmaster esql){//13
		//
		
	}

	public static void ListBookingInfoForUser(Ticketmaster esql){//14
		//
		
	}
	
}