import java.sql.*;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.Calendar;


public class COMP421PoliceDB {

	/**
	 * @param args
	 */
	
	static DBConnect db;
	
	public static void main(String[] args) throws SQLException {
		// TODO Auto-generated method stub
		System.out.println("WELCOME TO THE POLICE DATABASE QUERY SYSTEM.");
		boolean run = true;
		
		//Create DBConnect object which establishes connection to database and handles all
		//queries

		db = new DBConnect("jdbc:db2://db2.cs.mcgill.ca:50000/cs421", "cs421g10", "LewVe-g5");

		//db = new DBConnect("jdbc:db2://db2:50000/cs421", "cs421g10", "LewVe-g5");
		Statement dbStatement = db.getStatement();

		//loop for user to choose a choice
		while (run)
		{
			System.out.println("Please select one of the following options by typing the associated number:\n");
			System.out.println("1. Test query");

			System.out.println("2. Test query");
			System.out.println("3. Test query");
			System.out.println("4. Test query");
			System.out.println("5. Test query");
			System.out.println("6. Test query");
			System.out.println("7. Quit\n");
			
			int userin = getUserChoice_int("Your choice:");

			if (userin == 1)
			{
				isCriminal(dbStatement);
			}
			else if (userin == 2)
			{
				addOffense(dbStatement);
			}
			else if (userin == 3)
			{
				increaseSalary(dbStatement);
			}
			else if (userin == 4)
			{
				updatePopulation(dbStatement);
			}
			else if (userin == 5)
			{
				runStats(dbStatement);
			}
			else if (userin == 6)
			{
				optimize(dbStatement);
			}
			else if (userin == 7)
			{
				run = false;
			}
			else if (userin == 8) 
			{
				testRun();
			}
			else if (userin == -1)
			{
				System.out.println("######################################");
				System.out.println("Please make a choice by typing an int!");
				System.out.println("######################################\n");
			}
			else
			{
				System.out.println("############################################");
				System.out.println("That is not a valid choice. Please try again.");
				System.out.println("############################################\n");
				continue;
			}
		}
		System.out.println("\nTHANK YOU FOR USING THE POLICE DATABASE QUERY SYSTEM.");
		dbStatement.close();
		db.closeConnection();
	}
	
	//TODO: error handling 
	public static int getUserChoice_int(String message)
	{
		Scanner scan = new Scanner(System.in);
		System.out.print(message);
		try
		{
			return scan.nextInt();
		}
		catch (InputMismatchException e)
		{
			return -1;
		}
	}
	
	// TODO: error handling
	public static String getUserChoice_str(String message) {
		Scanner scan = new Scanner(System.in);
		System.out.print(message);
		try {
			return scan.next();
		}
		catch (InputMismatchException e) {
			return "";
		}
	}
	
	// Look up whether a given person is a criminal
	// If criminal exists return their criminal record
	public static void isCriminal(Statement statement) throws SQLException {
		statement.clearBatch();
		
		return;
	}
	
	// Add an offense to the database
	// Prompts user if adding a traffic violatiostmtn or crime
	// Prompts user for all other info (criminal name/location of crime/etc...)
	// If traffic violation and occurred 5 years ago, don't add 
	// If crime and occurred 10 years ago, don't add but record in log file 
	// Could these be triggers? 
	// can use isCriminal() to check if already exists
	public static void addOffense(Statement statement) throws SQLException {
		statement.clearBatch();
		return;
	}
	
	// Personal crime rate = number of crimes / (population / 1000)
	// BJS publications typically report crime rates as per 1000 persons
	public double crimeRate(String borough, Statement stmt, int year) {
		//Formulate queries - get population count of the borough from that year and number of crimes committed in the borough that year
		String pop_query = "SELECT population_count FROM Population WHERE bourough_bid = "+borough+" AND year = "+year;
		String numcrimes_query = "SELECT COUNT(*) FROM Offense O, Crime C where O.ofid = C.offense_ofid and C.borough_bid = "+borough+" AND year(O.date_committed) ="+year;
		
		double population = 0.0;
		double crimecount = 0.0;
		
		//Attempt to execute queries and retrieve values
		try {
			ResultSet popcount = stmt.executeQuery(pop_query);
			population = popcount.getDouble("population_count");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Failed to retrieve population: SQL error.");
			return -1.0;
		}
		try {
			ResultSet numcrimes = stmt.executeQuery(numcrimes_query);
			crimecount = numcrimes.getDouble(1);
		} catch (SQLException e) {
			System.out.println("Failed to retrieve crime count: SQL Error");
			return -1.0;
		}
		
		//Calculate and return rate
		return (crimecount/(population/1000.0));
	}
	
	// Increase the salary of all police officers with ranking >= x
	// In a specific borough
	// Increase by a certain percentage based on the crime rate of that borough
	// Prompt user for ranking, percentage
	// Have error handling or triggers if combined salary exceeds budget 
	//TODO make general function for getting input with message
	public static void increaseSalary(Statement statement) throws SQLException {
		statement.clearBatch();
		String borough = getUserChoice_str("Enter borough: ");
		int rank = getUserChoice_int("Enter rank: ");
		int perc = getUserChoice_int("Enter rank: ");
		
		//Get the current year
		Calendar rightnow = Calendar.getInstance();
		int year = rightnow.get(Calendar.YEAR);
		
		statement.executeUpdate(sql);
		return;
	}
	
	// Update the population with new census information 
	// Asks for the new population of each borough
	// If the crime rate changes by a certain amount then ask user if want to reallocate, then run analyze() ??
	public static void updatePopulation(Statement statement) throws SQLException 
	{
		statement.clearBatch();
	}
	
	// Compile statistics
	// Ex: boroughs with highest crime rate, intersections with high volume of traffic violations, most criminal gender, etc.
	// Could have option to run all, or specify which borough you want to see stats for (for example)
	public static void runStats(Statement statement) throws SQLException {
		statement.clearBatch();
		return;
	}
	
	// Optimization algorithm - efficient allocation of salary/police officers based on crime rate and crime types
	// Should have modification option 
	public static void optimize(Statement statement) throws SQLException {
		statement.clearBatch();
		return;
	}
	
	public static void testRun()
	{
		ResultSet test = db.runQuery("SELECT * FROM Offender");
		if (test != null)
		{
			boolean result = false;
			try {
				while (test.next())
				{
					result = true;
					System.out.print("OID: " + test.getString("oid"));
					System.out.print("Name: " + test.getString("fname") + " " + test.getString("lname"));
					System.out.print("Gender: " + test.getString("gender"));
					System.out.print("Race: " + test.getString("race"));
					System.out.print("Address" + test.getString("address"));
					System.out.println("Date of Birth" + test.getString("dob"));	
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println("Main.optionOne(): SQL Exception caught while processing ResultSet.");
			}
			if (!result)
			{
				System.out.println("No results found.");
			}
		}
	}

}
