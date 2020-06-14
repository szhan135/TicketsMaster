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

			System.out.print("Booking created at time: " + time + "\n");
			System.out.print("========================================================\n");

			
			
		}catch (Exception err) {
			System.err.println(err.getMessage());
		}
	}
	
	public static void AddMovieShowingToTheater(Ticketmaster esql) throws IOException, SQLException {// 3
	
	String title, releaseDate, country, description, lang, genre, sdate, sttime, edtime, q1, q2, q3;
	int mvid, sid, tid, duration;

	List<List<String>> mvidMax = esql.executeQueryAndReturnResult("select max(mvid) from movies;");
	mvid = Integer.parseInt(mvidMax.get(0).get(0)) + 1;

	List<List<String>> sidMax = esql.executeQueryAndReturnResult("select max(sid) from shows;");
	sid = Integer.parseInt(sidMax.get(0).get(0)) + 1;

	System.out.println("**** Movie Information ****");
	System.out.print("Movie Title: ");
	title = in.readLine();
	System.out.println("");

	System.out.print("Release Date(MM/DD/YYYY): ");
	releaseDate = in.readLine();
	System.out.println("");

	System.out.print("Country: ");
	country = in.readLine();
	System.out.println("");

	System.out.print("Description: ");
	description = in.readLine();
	System.out.println("");	

	do {
		System.out.print("Duration(Seconds): ");
		try { 
			duration = Integer.parseInt(in.readLine());
			break;
		} catch (Exception e) {
			System.out.println("Your input is invalid!");
			continue;
		}
	} while (true);
	System.out.println("");

	System.out.print("Language(2 Letter Abreviation): ");
	lang = in.readLine();
	System.out.println("");	
	

	System.out.print("Genre: ");
	genre = in.readLine();
	System.out.println("");	

	System.out.println("**** Show Information ****");
	System.out.print("Show Date(MM/DD/YYYY): ");
	sdate = in.readLine();
	System.out.println("");
	
	System.out.print("Start Time(HH:MM): ");
	sttime = in.readLine();
	System.out.println("");

	System.out.print("End Time(HH:MM): ");
	edtime = in.readLine();
	System.out.println("");


	List<List<String>> tidMin = esql.executeQueryAndReturnResult("select min(tid) from theaters;");
	int tidMIN = Integer.parseInt(tidMin.get(0).get(0));

	List<List<String>> tidMax = esql.executeQueryAndReturnResult("select max(tid) from theaters;");
	int tidMAX = Integer.parseInt(tidMax.get(0).get(0));

	System.out.print("Theater ID (Between " + tidMIN + " and " + tidMAX + ") : ");
	tid = Integer.parseInt(in.readLine());
	System.out.println("");

	q1 = "INSERT INTO Movies (mvid, title, rdate, country, description, duration, lang, genre) values (" 
	+ mvid + ", '" + title + "', '" + releaseDate + "', '" + country + "', '" + description + "', " + 
	duration + ", '" + lang + "', '" + genre + "' );";
	esql.executeUpdate(q1);

	q2 = "INSERT INTO Shows (sid, mvid, sdate, sttime, edtime) values (" + sid + ", " + mvid + ", '" + 
	sdate + "', '" + sttime + "', '" + edtime + "');";
	esql.executeUpdate(q2);

	q3 = "INSERT INTO Plays (sid, tid) values (" + sid + ", " + tid + ");";
	esql.executeUpdate(q3);

	}
	
	public static void CancelPendingBookings(Ticketmaster esql){//4
		try {
			esql.executeUpdate("Update ShowSeats SET bid = NULL where bid = (select bid from bookings where status = 'Pending')");
			esql.executeUpdate("Update Bookings SET status = 'Cancelled' where status = 'Pending' ");
			System.out.print("Cancelled all pending bookings\n");
			System.out.print("========================================================\n");
		}catch(Exception err) {
			System.err.println(err.getMessage());
		}
	}
	
	public static void ChangeSeatsForBooking(Ticketmaster esql) throws IOException, SQLException {// 5
		System.out.println("What is the email that the booking was made with? ");
		String email = in.readLine();

		List<List<String>> confirmEmail = esql.executeQueryAndReturnResult("select email from bookings where email = '" + email + "';");

		if(confirmEmail != null && confirmEmail.isEmpty()) {
				System.out.println("We coudn't find any bookings with that email! Try again.");
				return;
		}

		System.out.println("Here are the bookings on your account: ");
		esql.executeQueryAndPrintResult("select * from bookings where email = '" + email + "';");


		System.out.println("What is the booking id you would like to change? ");
		String bid = in.readLine();

		List<List<String>> bookingid = esql.executeQueryAndReturnResult("select bid from bookings where bid = '" + bid + "';");
		if(bookingid != null && bookingid.isEmpty()) {
				System.out.println("Invalid booking id! Try again.");
				return;
		}

		String sid = esql.executeQueryAndReturnResult("select sid from bookings where bid = '" + bid + "';").get(0).get(0);
		String tid = esql.executeQueryAndReturnResult("select tid from plays where sid in (select sid from bookings where sid = '" + sid + "');").get(0).get(0);

		//how many seats are on the reservation
		int numseats = Integer.parseInt(esql.executeQueryAndReturnResult("select seats from bookings where bid = '" + bid + "';").get(0).get(0));
		String maxseat = esql.executeQueryAndReturnResult("select max(sno) from cinemaseats where tid = '" + tid + "';").get(0).get(0);
		List<List<String>> seats = esql.executeQueryAndReturnResult("select tid, sno, stype, csid from cinemaseats where csid in (select csid from showseats where bid = " + bid + "));");

		//run through all booked seats and change
		for(int i = 0; i < numseats; i++) {
				String seat = seats.get(i).get(0) + ", " + seats.get(i).get(1) + ": " + seats.get(i).get(2);
				String csid = seats.get(i).get(3);
				System.out.println("Replace seat [" + seat + "]. These seats are currently free: ");
				esql.executeQueryAndPrintResult("select sno, stype from cinemaseats where tid = '" + tid + "' and csid not in (select csid from showseats);");


				System.out.println("Which seat would you like to reserve?");
				String replace = in.readLine();

				//checking that seat selection is in range
				if(Integer.parseInt(replace) > Integer.parseInt(maxseat)) {
						System.out.println("There is no seat with that number in the theater.");
						i--;
						break;
				}

	 String newcsid = esql.executeQueryAndReturnResult("select csid from cinemaseats where tid = '" + tid + "' and sno = '" + replace + "';").get(0).get(0);
				String oldtype = esql.executeQueryAndReturnResult("select stype from cinemaseats where csid = '" + csid + "';").get(0).get(0);
				String newtype = esql.executeQueryAndReturnResult("select stype from cinemaseats where csid = '" + newcsid + "';").get(0).get(0);

				//checking that seat is exchangable
				if(!oldtype.equals(newtype)) {
						System.out.println("You can only exchange seats that are the same price as the original.");
						i--;
						break;
				}

				String q = "update showseats set csid = '" + newcsid + "' where csid = '" + csid + "';";
				esql.executeUpdate(q);
		}

		System.out.println("Seat reservations sucessfully updated!");

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
		String cinema_name;
        String date;
        String query;
        List<String> cname_list = new ArrayList<String>();
        List<String> tid_list = new ArrayList<String>();
        List<List<String>> sid_list  = new ArrayList<List<String>>();
        
        /* Format:
                1) Ask User what date they would like to cancel all shows on (store this answer)
                2) Ask User which cinema they would want to cancel all shows on (store this answer)
                3) Using these two answers, select all sids that have the given date, obtaining the tids (in Plays.csv)
                4) Then, using the obtained tids, because they are exactly the same as their cid counterparts, 
                   we will do a crossreference for the given user cinema selection to cancel all the shows with the given date at the given cinema
                5) Additionally, we need to cancel all the Bookings on those dates (We can do this using the sids from earlier)
        */
        
        while(true) {
            System.out.println("Enter the date you'd like to cancel all shows on. (mm/dd/yyyy)");
            try {
                date = in.readLine();
                SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
                Date date2 = null;
                date2 = dateFormat.parse(date);
                break;
            } 
            catch (Exception e) {
                System.out.println("Your input is invalid! Your exception is: " + e.getMessage());
            }
        }
        try {
            System.out.println("Enter the name of the specific cinema where you'd like to cancel the shows based on the given date.\nThe cinemas that you can choose from are:");
            System.out.println("AMC\nGeneral Cinemas\nHarkins\nIMAX Corporation\nRegal Cinemas\nStudio Movie Grill");
            
            cinema_name = in.readLine();
            try {
                if (cinema_name.equals("AMC") || cinema_name.equals("General Cinemas") || cinema_name.equals("Harkins") || cinema_name.equals("IMAX Corporation") || 
                    cinema_name.equals("Regal Cinemas") || cinema_name.equals("Studio Movie Grill")) {
                        query = "SELECT sid FROM Shows WHERE sdate = '" + date + "';";
                        sid_list = esql.executeQueryAndReturnResult(query); // This is a list of sids given the date
                        
                        for (int i = 0; i < sid_list.size(); i++) { // This is a list of tids given the sids
                            query = "SELECT tid FROM Plays WHERE sid = " + sid_list.get(i).get(0) + ";";
                            tid_list.add(esql.executeQueryAndReturnResult(query).get(0).get(0));
                        }
                        
                        for (int i = 0; i < tid_list.size(); i++) { // This is a list of cnames given the tids
                            query = "SELECT cname FROM Cinemas WHERE cid = " + tid_list.get(i) + ";";
                            cname_list.add(esql.executeQueryAndReturnResult(query).get(0).get(0));
                        }
                        
                        for (int i = 0; i < cname_list.size(); i++) {
                            if (cinema_name.equals(cname_list.get(i))) {
                                query = "DELETE FROM Shows WHERE sid = " + sid_list.get(i).get(0) + ";"; // Because the three lists above are aligned, we can do this
                                esql.executeUpdate(query);
                                
                                System.out.println("Shows on " + date + " at " + cinema_name + " successfully removed.");
                            }
                        }
                }
                else {
                    System.out.println("Please enter a valid cinema from the list next time.");
                    return;
                }
            }
            catch (Exception e) {
                System.out.println("Your input is invalid! Your exception is: " + e.getMessage());
            }
        }
        catch (Exception e) {
            System.out.println("Your input is invalid! Your exception is: " + e.getMessage());
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

	public static void ListMovieAndShowInfoAtCinemaInDateRange(Ticketmaster esql) throws IOException, SQLException {// 13
		/*select m1.title, m1.duration, s1.sdate, s1.sttime from movies m1, shows s1 where m1.title = 
		'Avatar' and sdate in (select s2.sdate from shows s2 where s2.sdate between '01/01/2019' and 
		'12/31/2019' and s2.mvid = (select m2.mvid from movies m2 where m2.title = 'Avatar')) and 
		sid in (select p1.sid from plays p1 where p1.tid in (select t1.tid from theaters t1 where 
		t1.cid in (select c1.cid from cinemas c1 where c1.cname = 'AMC')));*/

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


	}

	public static void ListBookingInfoForUser(Ticketmaster esql){//14
		
		String query;
		String user_email;
		String date;
		String time;
		String dt;
		Timestamp ts = Timestamp.valueOf("2000-01-01 00:00:01"); // Booking Time
		List<String> mvtitle_list = new ArrayList<String>();
		List<String> mvid_list = new ArrayList<String>();
		List<String> tname_list = new ArrayList<String>();
		List<String> tid_list = new ArrayList<String>();
		List<Timestamp> showdt_list = new ArrayList<Timestamp>();
		List<List<String>> bid_list  = new ArrayList<List<String>>();
		List<List<String>> sid_list  = new ArrayList<List<String>>();
		List<List<List<String>>> csid_list  = new ArrayList<List<List<String>>>(); // A Lists of Lists Lists!!!
		
		//List the Movie Title, Show Date & Start Time, Theater Name, and Cinema Seat Number forall Bookings of a Given User
		
		/* Format:
				1) Ask User for Email
				2) Check Bookings for Given Email, and grab sid(s)* related to the given Email (if they have multiple bookings)
				3) Using the obtained sid(s), we check the Show table for the Show Date(s) & Start Time(s) of the obtained sid(s)
				4) Using the obtained sid(s), we also check the Show table for the mvid(s) of the related sid(s)
				5) Using the obtained mvid(s), we check the Movies table for the Movie Title(s) of the given mvid(s)
				6) Using the obtained sid(s), we check the Plays table for the tid(s) related to the given sid(s)
				7) Using the obtained tid(s), we check the Theatre table for the Theatre name(s)
				8) Cinema Seat Number = ??? (We never actually do anything with this particular table given the other functions nor do the data provided have bids attached to the ShowSeats entries)
				9) Output all this stuff somehow
		*/
		
		while(true) {
			System.out.println("Enter the Email associated with the Bookings you'd like to check for.");
			try {
				user_email = in.readLine();
				break;
			} 
			catch (Exception e) {
				System.out.println("Your input is invalid! Your exception is: " + e.getMessage());
				continue;
			}
		}
		try {
			query = "SELECT email\nFROM Users\n WHERE email = " + "'" + user_email + "'" + ";";
			if (esql.executeQuery(query) == 0) { // Email doesn't exist in the database
				System.out.println("This is not a valid user.");
				return;
			}
			else {
				query = "SELECT bid FROM Bookings WHERE email = '" + user_email + "';"; // Get BIDs
				bid_list = esql.executeQueryAndReturnResult(query);
 				
				query = "SELECT sid FROM Bookings WHERE email = '" + user_email + "';"; // Get SIDs
				sid_list = esql.executeQueryAndReturnResult(query);
				
				for (int i = 0; i < sid_list.size(); i++) { // Get Date and Time
					query = "SELECT sdate, sttime FROM Shows WHERE sid = " + sid_list.get(i).get(0) + ";";
					date = esql.executeQueryAndReturnResult(query).get(0).get(0);
					time = esql.executeQueryAndReturnResult(query).get(0).get(1);
					dt = date + " " + time;
					ts = Timestamp.valueOf(dt);
					showdt_list.add(ts);
				}
				
				for (int i = 0; i < sid_list.size(); i++) { // Get Movie ID
					query = "SELECT mvid FROM Shows WHERE sid = " + sid_list.get(i).get(0) + ";";
					mvid_list.add(esql.executeQueryAndReturnResult(query).get(0).get(0));
				}
				
				for (int i = 0; i < mvid_list.size(); i++) { // Get Movie Title
					query = "SELECT title FROM Movies WHERE mvid = " + mvid_list.get(i) + ";";
					mvtitle_list.add(esql.executeQueryAndReturnResult(query).get(0).get(0));
				}
				
				for (int i = 0; i < sid_list.size(); i++) { // Get Theatre ID
					query = "SELECT tid FROM Plays WHERE sid = " + sid_list.get(i).get(0) + ";";
					tid_list.add(esql.executeQueryAndReturnResult(query).get(0).get(0));
				}
				
				for (int i = 0; i < tid_list.size(); i++) { // Get Theater Name
					query = "SELECT tname FROM Theaters WHERE tid = " + tid_list.get(i) + ";";
					tname_list.add(esql.executeQueryAndReturnResult(query).get(0).get(0));
				}
				
				for (int i = 0; i < bid_list.size(); i++) { // Get Cinema Seat ID
					query = "SELECT csid FROM ShowSeats WHERE bid = " + bid_list.get(i).get(0) + ";";
					csid_list.add(esql.executeQueryAndReturnResult(query));
				}
				
				for (int i = 0; i < sid_list.size(); i++) {
					System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
					System.out.println("Movie Title: " + mvtitle_list.get(i));
					System.out.println("Show Date and Time: " + showdt_list.get(i));
					System.out.println("Theater Name: " + tname_list.get(i));
					System.out.println("Cinema Seat Number(s): ");
					for (int j = 0; j < csid_list.get(i).size(); j++) { // Cinema Seat ID based on SID
						for (int k = 0; k < csid_list.get(i).get(j).size(); k++) { // Amount of Seats reserved for Specific SID
							System.out.println(csid_list.get(i).get(j).get(k)); // The individual Cinema Seat IDs being printed
						}
					}
				}
				System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
			}
		}
		catch (Exception e) {
			System.out.println("Your input is invalid! Your exception is: " + e.getMessage());
		}
		
	}
	
}