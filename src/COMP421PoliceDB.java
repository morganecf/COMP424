import java.sql.*;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;
import java.util.Calendar;
import java.util.List;
import java.util.ArrayList;

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
			System.out.println("1. Lookup Criminal");
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
	
	
	// Look up whether a given person is a criminal
	// If criminal exists return their criminal record
	public static void isCriminal(Statement statement)
	{
		try
		{
			statement.clearBatch();
			System.out.println("Please enter the first and last name of the individual in question.\n");
			String fname = getUserChoice_str("First name: ");
			String lname = getUserChoice_str("Last name: ");
			String checkExists = "SELECT * FROM Offender WHERE fname = '" + fname + "' AND lname = '" + lname + "'";
			ResultSet info = statement.executeQuery(checkExists);
			int oid = -1;
			ArrayList<Integer> candidates = new ArrayList<Integer>();
			int count = 1;
			System.out.println("\n**********************************************\n");
			while (info.next())
			{
				candidates.add(new Integer(info.getString("oid")));
				System.out.println("POSSIBLE MATCH #" + count);
				System.out.println("\nFIRST NAME: " + info.getString("fname"));
				System.out.println("LAST NAME: " + info.getString("lname"));
				System.out.println("GENDER: " + info.getString("gender"));
				System.out.println("RACE: " + info.getString("race"));
				System.out.println("ADDRESS: " + info.getString("address"));
				System.out.println("DATE OF BIRTH: " + info.getString("dob") + "\n");
				count++;
			}
			if (candidates.size() > 1)
			{
				int choice = getUserChoice_int("\nPlease type the number corresponding to the Offender of interest: ");
				if (choice > 0 && choice < candidates.size() + 1)
				{
					oid = candidates.get(choice - 1);
				}
				else 
				{
					System.out.println("\nThat is not a valid selection. Please try your search again.\n");
					return;
				}
			}
			else if (candidates.size() == 1)
			{
				oid = candidates.get(0).intValue();
			}
			
			if (candidates.isEmpty())
			{
				System.out.println("\nNO MATCHES FOUND!\n");
				return;
			}
			else 
			{	
				System.out.println("*******************************************************************");
				System.out.println("\n" + fname + " " + lname + "'s CRIMINAL RECORD: ");
				String query = "SELECT * FROM Offense AS o, (SELECT oco.offense_ofid FROM offender_commits_offense AS oco, (SELECT oid FROM Offender WHERE oid = " + oid + ") AS o2 WHERE o2.oid = oco.offender_oid) AS o3 WHERE o3.offense_ofid = o.ofid";
				ResultSet record = statement.executeQuery(query);
				boolean empty = true;
				while (record.next())
				{
					empty = false;
					System.out.println("\nDATE OF OFFENSE: " + record.getString("date_committed"));
					System.out.println("DESCRIPTION: " + record.getString("description"));
					System.out.println("LOCATION: " + record.getString("address"));
					System.out.println("\n________________________\n");
					
				}
				if (empty)
				{
					System.out.println("\n" + fname + " " + lname + " DOES NOT HAVE ANY OFFENSES ON RECORD.\n");
				}
				System.out.println("END OF RECORD");
				System.out.println("*******************************************************************");

			}
		}
		catch (SQLException e)
		{
			System.out.println("SQL ERROR OCCURRED");
			e.printStackTrace();
			return;
		}

		return;
	}
	
	// Add an offense to the database
	// Prompts user if adding a traffic violatiostmtn or crime
	// Prompts user for all other info (criminal name/location of crime/etc...)
	// If traffic violation and occurred 5 years ago, don't add 
	// If crime and occurred 10 years ago, don't add but record in log file 
	// Could these be triggers? 
	// can use isCriminal() to check if already exists
	public static void addOffense(Statement statement)
	{
		try
		{
			statement.clearBatch();
			System.out.println("Please enter the first and last name of the offender.\n");
			String fname = getUserChoice_str("First name: ");
			String lname = getUserChoice_str("Last name: ");
			
			/*ArrayList<>
			String checkExists = "SELECT oid FROM Offender WHERE fname = '" + fname + "' AND lname = '" + lname + "'";
			ResultSet info = statement.executeQuery(checkExists);
			boolean empty = true;
			while (info.next())
			{
				empty = false;
				System.out.println("\nFIRST NAME: " + info.getString("fname"));
				System.out.println("LAST NAME: " + info.getString("lname"));
				System.out.println("GENDER: " + info.getString("gender"));
				System.out.println("RACE: " + info.getString("race"));
				System.out.println("ADDRESS: " + info.getString("address"));
				System.out.println("DATE OF BIRTH: " + info.getString("dob") + "\n");
			}
			if (empty)
			{
				System.out.println("\nNO MATCHES FOUND!\n");
			}*/
		}
		catch (SQLException e)
		{
			System.out.println("SQL ERROR OCCURRED");
			e.printStackTrace();
			return;
		}		
		
		return;
	}
	
	// Personal crime rate = number of crimes / (population / 100000)
	public static double crimeRate(String borough, int year, Statement stmt) throws SQLException {
		stmt.clearBatch();

		// Formulate queries - get population count of the borough from that year and number of crimes committed in the borough that year
		String pop_query = "SELECT population_count FROM Population WHERE bourough_bid = "+borough+" AND year = "+year;
		String numcrimes_query = "SELECT COUNT(*) FROM Offense O, Crime C where O.ofid = C.offense_ofid and C.borough_bid = '"+borough+"' AND year(O.date_committed) ="+year;
		
		double population = 0.0;
		double crimecount = 0.0;
		
		// Attempt to execute queries and retrieve values
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
		int perc = getUserChoice_int("Enter rank: ");
		
		// Get the current year
		Calendar rightnow = Calendar.getInstance();
		int year = rightnow.get(Calendar.YEAR);
		
		// Calculate the if crime rate is going up or down in this borough
		double crimerate = crimeRate(borough, year, statement);
		
		
		//statement.executeUpdate(sql);
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
		
		String answer = getUserChoice_str("Would you like to reallocate resources based on new census data? (y/n)");
		
		if(answer.equals("y"))
		{
			optimize(statement);
		}
		
	}
	// Compile statistics
	// Ex: boroughs with highest crime rate, intersections with high volume of traffic violations, most criminal gender, etc.
	// Could have option to run all, or spec)ify which borough you want to see stats for (for example)
	public static void runStats(Statement statement) throws SQLException 
	{
		statement.clearBatch();
		return;
	}
	
	// Optimization algorithm - efficient allocation of salary/police officers based on crime rate and crime types
	// Should have modification option 
	public static void optimize(Statement statement) throws SQLException {
		statement.clearBatch();
		return;
	}

}
