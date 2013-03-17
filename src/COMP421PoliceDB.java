import java.sql.*;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;
import java.util.Calendar;

public class COMP421PoliceDB {

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
			System.out.println("1. Lookup Offender");
			System.out.println("2. Add an Offense");
			System.out.println("3. Increase Police Officer Salary");
			System.out.println("4. Insert Census Information");
			System.out.println("5. Compile Statistics");
			System.out.println("6. Calculate Police Officer Allocation");
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
	
	public static int currentYear() {
		Calendar rightnow = Calendar.getInstance();
		return rightnow.get(Calendar.YEAR);
	}

	public static int previousYear(int current_year, Statement stmt) {
		ArrayList<Object> years = new ArrayList<Object>();
		try {
			ResultSet yrs = stmt.executeQuery("SELECT DISTINCT year FROM population ORDER BY year");
			while(yrs.next()) {
				years.add(yrs.getObject("YEAR"));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}
		Object current_year_obj = (Object) current_year;
		int i = years.indexOf(current_year_obj);
		try {
			return (int) (Integer) years.get(i-1);
		} catch (IndexOutOfBoundsException e) {
			System.out.println("This is the first year in the database.");
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
	
	// Personal crime rate = number of crimes / (population / 100000)
	public static double crimeRate(String borough, int year, Statement stmt) {
		// Formulate queries - get population count of the borough from that year and number of crimes committed in the borough that year
		String pop_query = "SELECT population_count FROM Population WHERE borough_bid = "+"'"+borough+"'"+" AND year = "+year;
		String numcrimes_query = "SELECT COUNT(*) FROM Offense O, Crime C where O.ofid = C.offense_ofid and C.borough_bid = "+"'"+borough+"'"+" AND year(O.date_committed) ="+year;
		
		double population = 0.0;
		double crimecount = 0.0;
		
		// Attempt to execute queries and retrieve values
		try {
			ResultSet popcount = stmt.executeQuery(pop_query);
			if( popcount.next() ) {
				population = popcount.getDouble("POPULATION_COUNT");
				System.out.println("Population of "+borough+" in "+year+": "+population);
			}
			else {
				return -1.0;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Failed to retrieve population: SQL error.");
			return -1.0;
		}
		try {
			ResultSet numcrimes = stmt.executeQuery(numcrimes_query);
			if(numcrimes.next()) {
				crimecount = numcrimes.getDouble(1);
				System.out.println("Number of crimes in "+borough+" in "+year+": "+crimecount);
			}
			else {
				return -1.0;
			}
			
		} catch (SQLException e) {
			System.out.println("Failed to retrieve crime count: SQL Error");
			return -1.0;
		}
		
		// Calculate and return rate
		return (crimecount/(population/100000.0));
	}

	// Increase the salary of all police officers with ranking >= x, in a specific borough
	// Increase by a certain percentage based on the crime rate of that borough
	// Prompt user for ranking, percentage
	// Have error handling or triggers if combined salary exceeds budget 
	//TODO make general function for getting input with message
	public static void increaseSalary(Statement statement) throws SQLException {
		statement.clearBatch();
		String borough = getUserChoice_str("Enter borough: ");
		int rank = getUserChoice_int("Enter rank: ");
		double perc = ((double) getUserChoice_int("Enter percentage (%): "))/100.0;
		
		// Get the current year
		int year = currentYear();
		System.out.println("Current year:"+year);
		// Get the year before
		int previousyear = previousYear(year, statement);
		System.out.println("Previous year:"+previousyear);
		
		// Calculate the if crime rate is going up or down in this borough
		double current_crimerate = crimeRate(borough, year, statement);
		double previous_crimerate = crimeRate(borough, previousyear, statement);
		
		System.out.println("Current crime rate: "+current_crimerate);
		System.out.println("Previous crime rate: "+previous_crimerate);
		
		// If the crime rate has increased, increase the salaries of specified officers 
		// Of the police stations of that borough
//		if(previous_crimerate < current_crimerate) {
		String query = "UPDATE police_officer SET salary = salary*"+perc+" WHERE rank="+rank+" AND police_station_psid IN (SELECT psid FROM police_station WHERE borough_bid="+borough;
		statement.executeUpdate(query);
//		}

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
		year = getUserChoice_int("Year: ");
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
					int population = getUserChoice_int("Population: ");
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
