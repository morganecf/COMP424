import java.sql.*;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;


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
		db = new DBConnect("jdbc:db2://db2:50000/cs421", "cs421g10", "LewVe-g5");
		Statement dbStatement = db.getStatement();
		
		//loop for user to choose a choice
		while (run)
		{
			System.out.println("Please select one of the following options by typing the associated number:\n");
			System.out.println("1. Lookup Criminal");
			System.out.println("2. Add an Offense");
			System.out.println("3. Increase Police Officer Salary");
			System.out.println("4. Insert Census Information");
			System.out.println("5. Compile Statistics");
			System.out.println("6. Calculate Police Officer Allocation");
			System.out.println("7. Quit\n");
			int userin = getUserChoice();
			
			
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
	
	public static int getUserChoice()
	{
		Scanner scan = new Scanner(System.in);
		System.out.print("Your choice: ");
		try
		{
			return scan.nextInt();
		}
		catch (InputMismatchException e)
		{
			return -1;
		}
	}
	
	// Look up whether a given person is a criminal
	// If criminal exists return their criminal record
	public static void isCriminal(Statement statement) throws SQLException {
		statement.clearBatch();
		
		return;
	}
	
	// Add an offense to the database
	// Prompts user if adding a traffic violation or crime
	// Prompts user for all other info (criminal name/location of crime/etc...)
	// If traffic violation and occurred 5 years ago, don't add 
	// If crime and occurred 10 years ago, don't add but record in log file 
	// Could these be triggers? 
	// can use isCriminal() to check if already exists
	public static void addOffense(Statement statement) throws SQLException {
		statement.clearBatch();
		
		return;
	}
	
	// Increase the salary of all police officers with ranking >= x or function = 's'
	// Increase by a certain percentage based on the crime rate
	// Prompt user for ranking vs. function, id (or name??), percentage
	// Have error handling or triggers if combined salary exceeds budget 
	public static void increaseSalary(Statement statement) throws SQLException {
		statement.clearBatch();
		return;
	}
	
	// Update the population with new census information 
	// Asks for the new population of each borough
	// If the crime rate changes by a certain amount then ask user if want to reallocate, then run analyze() ??
	public static void updatePopulation(Statement statement) throws SQLException 
	{
		statement.clearBatch();
		int year;
		
		System.out.println("Please select a census year");
		year = getUserChoice();
		ResultSet censusYear = statement.executeQuery("SELECT Count(*) FROM Population WHERE year = " + year);
		censusYear.next();
		
		if(censusYear.getInt(1) == 19)
		{
			System.out.println("Census data from selected year is already in database");
		}
		else
		{
			List<String> listBoroughs = new ArrayList<String>();
			ResultSet boroughs = statement.executeQuery("SELECT * FROM Borough");
			
			while(boroughs.next())
			{
				listBoroughs.add(boroughs.getString("name"));
			}
			
			for(String currentBorough : listBoroughs)
			{
				
				if(statement.execute("SELECT * FROM population WHERE year = " + year
						+ " AND borough_bid = " + "'" + currentBorough + "'"))
				{
					System.out.println("Enter census population for: " + currentBorough);
					int population = getUserChoice();
					String insert = "INSERT INTO population VALUES(" + year + ", " + population 
							+ ", '" + currentBorough + "')";
					
					statement.addBatch(insert);
				}
				
			}
			
			statement.executeBatch();
		}
		
		
	}
	// Compile statistics
	// Ex: boroughs with highest crime rate, intersections with high volume of traffic violations, most criminal gender, etc.
	// Could have option to run all, or spec)ify which borough you want to see stats for (for example)
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
	
	/*public static void testRun()
	{
		ResultSet test = db.runQuery("SELECT * FROM Offender");
		if (test != null)
		{
			boolean result = false;
			try {Statement dbStatement = db.getStatement();
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
	}*/

}
