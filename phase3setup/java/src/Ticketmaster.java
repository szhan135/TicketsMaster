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
			
			//Entering Booking status ===================================================================================================================================
			System.out.print("Enter Booking status (Pending/Paid): ");
			String status = in.readLine();
			//FIXME
			/*
			while(!status.equals("Pending") || !status.equals("Paid")) {
				System.out.print("Invalid status.\n");
				System.out.print("Enter Booking status (Pending/Paid): ");
				status = in.readLine();
			}
			*/

			//CREATING BOOKING ==========================================================================================================================================
			String time = new SimpleDateFormat("MM/dd/YYYY HH:mm").format(new Date());
			String temp = String.format("Insert into Bookings VALUES (%d, '%s', '%s', %d, '%s', '%s')", bid, status, time, seats, sid, email);
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
		try{
			String movieTitle, releaseDate, country, description, lang, genre, sdate, sttime, edtime, q1, q2, q3;
			int movieId, sid, tid, duration;

			movieId = (esql.executeQueryAndReturnResult("Select mvid from movies")).size()+1;
			sid = (esql.executeQueryAndReturnResult("Select sid from shows")).size()+1;

			System.out.println("The Below Shows Movie Info");
			System.out.print("Movie Title is: ");
			movieTitle = in.readLine();
			System.out.println("");

			System.out.print("The selected movie's Release Date(MM/DD/YYYY): ");
			releaseDate = in.readLine();
			System.out.println("");

			System.out.print("Which Country: ");
			country = in.readLine();
			System.out.println("");

			System.out.print("Movie's Description: ");
			description = in.readLine();
			System.out.println("");	

			do {
				System.out.print("Duration in Seconds: ");
				try { 
					duration = Integer.parseInt(in.readLine());
					break;
				} catch (Exception e) {
					System.out.println("Invalid Input, Try Again!");
					continue;
				}
			} while (true);
			System.out.println("");

			System.out.print("Language: ");
			lang = in.readLine();
			System.out.println("");	
			

			System.out.print("Genre: ");
			genre = in.readLine();
			System.out.println("");	

			System.out.println("The Below Shows Show Info");
			System.out.print("Show Date(MM/DD/YYYY): ");
			sdate = in.readLine();
			System.out.println("");
			
			System.out.print("Start Time(HH:MM): ");
			sttime = in.readLine();
			System.out.println("");

			System.out.print("End Time(HH:MM): ");
			edtime = in.readLine();
			System.out.println("");

			int tidMIN = 0;
			int tidMAX = (esql.executeQueryAndReturnResult("Select tid from theaters")).size()+1;

			System.out.print("Theater ID (Between " + tidMIN + " and " + tidMAX + ") : ");
			tid = Integer.parseInt(in.readLine());
			System.out.println("");

			q1 = "INSERT INTO Movies (mvid, title, rdate, country, description, duration, lang, genre) values (" 
			+ movieId + ", '" + movieTitle + "', '" + releaseDate + "', '" + country + "', '" + description + "', " + 
			duration + ", '" + lang + "', '" + genre + "' );";
			esql.executeUpdate(q1);

			q2 = "INSERT INTO Shows (sid, mvid, sdate, sttime, edtime) values (" + sid + ", " + movieId + ", '" + 
			sdate + "', '" + sttime + "', '" + edtime + "');";
			esql.executeUpdate(q2);

			q3 = "INSERT INTO Plays (sid, tid) values (" + sid + ", " + tid + ");";
			esql.executeUpdate(q3);
		}catch (Exception err) {
			System.err.println(err.getMessage());
		}
	}
	
	public static void CancelPendingBookings(Ticketmaster esql){//4
		try {
			//esql.executeUpdate("Update ShowSeats SET bid = NULL where bid = (select bid from bookings where status = 'Pending')");
			esql.executeUpdate("Update Bookings SET status = 'Cancelled' where status = 'Pending'");
			System.out.print("Cancelled all pending bookings\n");
			System.out.print("========================================================\n");
		}catch(Exception err) {
			System.err.println(err.getMessage());
		}
	}
	
	public static void ChangeSeatsForBooking(Ticketmaster esql) throws Exception{//5
		try {

			String bookingID;
			String seatID;
			String newSeatID;
			String sqlFormat;
 
			System.out.print("Please Type in the bookingID of for seat change: ");
			bookingID = in.readLine();//read the bookingID input
 
			System.out.print("Please Enter the seatID: ");
			seatID = in.readLine();//read the seatID input
			System.out.print("What is the ID of the new seat that you want to change: ");
			newSeatID = in.readLine();
 
			List<String> oldSeat = esql.executeQueryAndReturnResult(String.format("SELECT * FROM ShowSeats WHERE ssid = '%s';", seatID)).get(0);
			List<String> newSeat = esql.executeQueryAndReturnResult(String.format("SELECT * FROM ShowSeats WHERE ssid = '%s';", newSeatID)).get(0);
 
			int oldSeatPrice = Integer.parseInt(oldSeat.get(4));
			int newSeatPrice = Integer.parseInt(newSeat.get(4));
			String newSeatBid = newSeat.get(3);
 
			if (newSeatBid == null) {
				if (newSeatPrice == oldSeatPrice) {
				 sqlFormat = String.format("UPDATE ShowSeats SET bid = NULL WHERE ssid = '%s';", seatID);
					esql.executeUpdate(sqlFormat);
					sqlFormat = String.format("UPDATE ShowSeats SET bid = '%s' WHERE ssid = '%s';", bookingID, newSeatID);
					esql.executeUpdate(sqlFormat);
				} else {
					System.out.println("Sorry! The seat you want to switch is different in price. \n");
					return;
				}
			} else {//In case user input invalid ID
				System.out.println("Sorry! Invalid ID \n");
				return;
			}
 
			System.out.println("Seat Changed!\n");// success signal
		} catch (Exception e) {
			System.out.println(e.getMessage() + "\n");
		}
	}
	
	public static void RemovePayment(Ticketmaster esql){//6
		try{
			System.out.print("Please Enter Booking ID that you would like to cancel: ");
			String bid = in.readLine();

			int row = esql.executeQueryAndReturnResult("Select bid from bookings where status = 'Paid' AND bid = " + bid).size();
			while(row == 0) {
				System.out.print("Invalid Booking ID.\n");
				System.out.print("Please Enter Booking ID that you would like to cancel: ");
				bid = in.readLine();
				row = esql.executeQueryAndReturnResult("Select bid from bookings where status = 'Paid' AND bid = " + bid).size();
			}

			esql.executeUpdate("Delete From Payments where bid = " + bid);
			esql.executeUpdate("Update Bookings SET status = 'Cancelled' where bid = " + bid);
			esql.executeUpdate("Update ShowSeats SET bid = NULL where bid = " + bid);
			System.out.print("Removed Payment\n");
			System.out.print("========================================================\n");

		}catch(Exception err) {
			System.err.println(err.getMessage());
		}

	}
	
	public static void ClearCancelledBookings(Ticketmaster esql){//7
		try {
			esql.executeUpdate("Delete From Bookings where status = 'Cancelled'");
		}catch(Exception err) {
			System.err.println(err.getMessage());
		}
	}
	
	public static void RemoveShowsOnDate(Ticketmaster esql){//8
		try {
			String cinemaName;
			String showDate;

			System.out.print("What is the name of the cinema?\n");
			cinemaName = in.readLine();
			
			System.out.print("What is the date that you want to cancel?\n");
			showDate = in.readLine();
			
			// TODO:
			// "remove" bookings using remove payment above
			List<List<String>> bids = esql.executeQueryAndReturnResult(String.format("SELECT B.bid FROM Bookings B, Shows S, Plays P, Theaters T, Cinemas C WHERE C.cname = '%s' AND C.cid = T.cid AND T.tid = P.tid AND P.sid = S.sid AND S.sdate = CAST('%s' AS DATE) AND B.sid = S.sid;", cinemaName, showDate));
			for(List<String> bid : bids) {
				esql.executeUpdate(String.format("UPDATE Bookings SET status = 'Cancelled' WHERE bid = '%s';", bid.get(0)));
				esql.executeUpdate(String.format("DELETE FROM Payments WHERE bid = '%s';", bid.get(0)));
			}

			List<List<String>> sids = esql.executeQueryAndReturnResult(String.format("SELECT S.sid FROM Shows S, Plays P, Theaters T, Cinemas C WHERE C.cname = '%s' AND C.cid = T.cid AND T.tid = P.tid AND P.sid = S.sid AND S.sdate = CAST('%s' AS DATE);", cinemaName, showDate));
			for(List<String> sid : sids) {
				esql.executeUpdate(String.format("DELETE FROM Shows WHERE sid = '%s';", sid.get(0)));
			}

			System.out.println("Successfully removed all shows on that date!\n");
		} catch (Exception e) {
			System.out.println(e.getMessage() + "\n");
		}	
	}
	
	public static void ListTheatersPlayingShow(Ticketmaster esql){//9
		try {
			System.out.print("Enter Cinema ID: ");
			String cid = in.readLine();
			System.out.print("Enter Show ID: ");
			String sid = in.readLine();

			esql.executeQueryAndPrintResult("Select tname from Theaters where tid = (Select tid from Plays where sid = " + sid + " AND tid = (select tid from Cinemas where cid = " + cid + "))");


		}catch(Exception err) {
			System.err.println(err.getMessage());
		}
		
	}
	
	public static void ListShowsStartingOnTimeAndDate(Ticketmaster esql){//10
		try{
			System.out.print("Enter Date (YYYY-MM-DD): ");
			String sdate = in.readLine();
			System.out.print("Enter Start Time (HH:MM:SS): ");
			String sttime = in.readLine();

			esql.executeQueryAndPrintResult("Select * from Shows where sdate = '" + sdate + "' AND sttime = '" + sttime + "\'");



		}catch(Exception err) {
			System.err.println(err.getMessage());
		}
		
	}

	public static void ListMovieTitlesContainingLoveReleasedAfter2010(Ticketmaster esql){//11
		try {
			esql.executeQueryAndPrintResult("Select * from Movies where title LIKE '%Love%' AND rdate > '2010-01-01'");
		}catch(Exception err) {
			System.err.println(err.getMessage());
		}
		
	}

	public static void ListUsersWithPendingBooking(Ticketmaster esql){//12
		try {
			esql.executeQueryAndPrintResult("Select fname,lname,email from Users where email in (select distinct email from bookings where status = 'Pending');");
		}catch(Exception err) {
			System.err.println(err.getMessage());
		}
		
	}

	public static void ListMovieAndShowInfoAtCinemaInDateRange(Ticketmaster esql){//13
		//
		try{
			String movieTitle, theaterName, date1, date2;

			System.out.println("\n\nMovie Title : ");
			movieTitle = in.readLine();

			System.out.println("Theater Name : ");
			theaterName = in.readLine();

			System.out.println("Begin Date(MM/DD/YYYY) : ");
			date1 = in.readLine();

			System.out.println("End Date(MM/DD/YYYY): ");
			date2 = in.readLine();

			String q = "select m1.title, m1.duration, s1.sdate, s1.sttime from movies m1, shows s1 where m1.title = '"
			+ movieTitle + "' and sdate in (select s2.sdate from shows s2 where s2.sdate between '" + date1 + "' and'" 
			+ date2 + "' and s2.mvid = (select m2.mvid from movies m2 where m2.title = '" + movieTitle + "')) and" 
			+ " sid in (select p1.sid from plays p1 where p1.tid in (select t1.tid from theaters t1 where " + 
			"t1.cid in (select c1.cid from cinemas c1 where c1.cname = '" + theaterName + "')));";

			System.out.println("");
			int result = esql.executeQueryAndPrintResult(q);
			System.out.println("\n");
		} catch (Exception e) {
			System.out.println(e.getMessage() + "\n");
		}
	}

	public static void ListBookingInfoForUser(Ticketmaster esql){//14
		//
		try {
			String email;

			System.out.print("Please Type in user email that booked: ");
			email = in.readLine();//read the email

			System.out.print("Here are all the bookings for this user\n");
			esql.executeQueryAndPrintResult(String.format("SELECT M.title, S0.sdate, S0.sttime, T.tname, C.sno FROM Movies M, Shows S0, Theaters T, ShowSeats S1, CinemaSeats C, Plays P, Bookings B WHERE B.email = '%s' AND B.sid = S0.sid AND S0.mvid = M.mvid AND S0.sid = P.sid AND P.tid = T.tid AND B.bid = S1.bid AND S1.csid = C.csid", email));
		} catch (Exception e) {
			System.out.println(e.getMessage() + "\n");
		}
		
	}
	
}