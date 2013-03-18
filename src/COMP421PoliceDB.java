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
			
			int userin = getUserChoice_int("Your choice: ");
			
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
			return scan.nextLine();
		}
		catch (InputMismatchException e) {
			return "";
		}
	}
	
	public static int currentYear() {
		Calendar rightnow = Calendar.getInstance();
		return rightnow.get(Calendar.YEAR);
	}
	
	

	
	// Increase the salary of all police officers with ranking >= x, in a specific borough
	// Increase by a certain percentage based on the crime rate of that borough
	// Prompt user for ranking, percentage
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
	
	//Method to walk user through creating a new offender record
	//Takes as input the first and last name of the offender record
	//to be created. Returns oid of new record if successful; else, returns
	//-1
	public static int createNewOffenderRecord(String fname, String lname, Statement statement)
	{
		//variables to be used 
		String gender;
		String race;
		String address;
		int yearob;
		int monthob;	
		int dayob;
		int oid = -1;
		
		//obtain necessary information from user
		System.out.println("A new record for this offender must be created.\n");
		gender = getUserChoice_str("Gender (NOTE: must enter 'm' or 'f'): ");
		if (!(gender.equals("m") || gender.equals("f")))
		{
			System.out.println("Must enter either 'm' or 'f' for gender. Please try again.\n");
			return oid;
		}
		race = getUserChoice_str("Race: ");
		address = getUserChoice_str("Address: ");
		yearob = getUserChoice_int("Year of birth (NOTE: must enter a 4-digit number): ");
		if (yearob > 9999 && yearob < 1000)
		{
			System.out.println("Must enter a 4-digit number representing the year of birth of this offender. Please try again.");
			return oid;
		}
		monthob = getUserChoice_int("Month of birth (NOTE: must enter a number from 1 to 12): ");
		if (monthob > 12 && monthob < 1)
		{
			System.out.println("Must enter a number from 1 to 12! Please try again.");
			return oid;
		}
		dayob = getUserChoice_int("Day of birth: ");
		if (dayob < 1 && dayob > 31)
		{
			System.out.println("Must enter a number from 1 to 31! Please try again");
			return oid;
		}
		String dob = "" + yearob + "-" + monthob + "-" + dayob;
		
		//create query
		String insertSQL = "INSERT INTO Offender (fname, lname, gender, race, address, dob) VALUES('"+ fname + "', '" + lname + "', '" + gender + "', '" + race + "', '" + address + "', '" + dob + "')";
		//execute query
	    try {
			statement.executeUpdate(insertSQL);
		} catch (SQLException e) {
			//e.printStackTrace();
			System.out.println("INSERT FAILED.");
			return oid;
		}
	    
	    
	    //RETRIEVE OID FROM NEWLY CREATED TUPLE USING:
	    String retrieveSQL = "SELECT SYSIBM.IDENTITY_VAL_LOCAL() AS id FROM Offender"; 
	    try {
			ResultSet lastEntered = statement.executeQuery(retrieveSQL);
		    while (lastEntered.next())
		    {
		    	oid = (new Integer(lastEntered.getString("id"))).intValue();
		    }

			
		} catch (SQLException e) {
			//e.printStackTrace();
			System.out.println("Attempt to retrieve OID of last Offender entered into database failed. Please try again (new offender will be in search results).\n");
			return oid;
		}

	    return oid;
		
	}
	
	//present user with a list of boroughs, and then have them select one.
	//returns string with name of borough
	public static String selectBorough(Statement statement) throws SQLException 
	{
		//get all boroughs to list 
		String borough = "SELECT * FROM Borough";
		ResultSet boroughs = statement.executeQuery(borough);
		int count = 1;
		ArrayList<String> boroughList = new ArrayList<String>();
		while (boroughs.next())
		{
			System.out.println(count + ": " + boroughs.getString("name"));
			boroughList.add(boroughs.getString("name"));
			count++;
		}
		int whichBorough = -1;
		while (!(whichBorough > 0 && whichBorough < 20))
		{
			whichBorough = getUserChoice_int("\nIn which borough was this crime committed?: ");
			//check for error in input
			if (!(whichBorough > 0 && whichBorough < 20))
			{
				System.out.println("Please select a number from 1 to 19 to select a borough.");
			}
		}
		return boroughList.get(whichBorough - 1);
	}
	
//Look up whether a given person is a criminal
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
				
				//determine name of offender from user
				System.out.println("Please enter the first and last name of the offender.\n");
				String fname = getUserChoice_str("First name: ");
				String lname = getUserChoice_str("Last name: ");
				
				//check if offender already exists in db
				String checkExists = "SELECT * FROM Offender WHERE fname = '" + fname + "' AND lname = '" + lname + "'";
				ResultSet info = statement.executeQuery(checkExists);
				int oid = -1;
				ArrayList<Integer> candidates = new ArrayList<Integer>();
				int count = 1;
				//print out all  offenders matching the name input by user
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
				//if there was more than one match, have user identify which offender 
				//they want to add an offense for and set oid to the oid of that offender
				//if none of the ones in the record are the correct one, also provide option to user
				//to create a new record
				if (candidates.size() > 1)	
				{
					
					int choice = getUserChoice_int("\nSelect one of the above records to add an offense to by typing\n" +
													" the number corresponding to the Offender of interest.\n" +
													"If none of the above are correct, type 0 to create a new record: ");
					//if choice was a valid choice
					if (choice > -1 && choice < candidates.size() + 1)
					{
						//if choice was 0, create a new record
						if (choice == 0)
						{
							oid = createNewOffenderRecord(fname, lname, statement);
							//check for failure; if so, return to main menu
							if (oid == -1)
							{
								System.out.println("An error occurred creating a new record. Please try again.\n");
								return;
							}
						}
						//otherwise set oid to oid of record selected
						else
						{
							oid = candidates.get(choice - 1);
						}
					}
					else 
					{
						System.out.println("\nThat is not a valid selection. Please try again.\n");
						return;
					}
				}
				//if there is only one match, then ask if this is the offender in question. If it is, 
				//then set oid to the oid of that offender. If it's not, then continue to creating a
				//new offender record
				else if (candidates.size() == 1)
				{
					int choice = getUserChoice_int("\nThere is only one record that matches this name. If this\n" +
										"is the right person, type 1. Else if you would like to create a new record\n" +
										"type 0: ");
					//make sure choice is a valid option (0 or 1)
					if (choice == 1 || choice == 0)
					{
						//create a new record
						if (choice == 0)
						{
							oid = createNewOffenderRecord(fname, lname, statement);
							//check for failure
							if (oid == -1)
							{
								System.out.println("An error occurred creating a new record. Please try again.\n");
								return;
							}
						}
						//else set oid to the record found
						else
						{
							oid = candidates.get(0).intValue();
						}
					}
				}
				//if there were no matches found, create a new record
				else if (candidates.isEmpty())
				{
					System.out.println("\nNO MATCHES FOUND!\n");
					oid = createNewOffenderRecord(fname, lname, statement);
					//check for failure
					if (oid == -1)
					{
						System.out.println("An error occurred creating a new record. Please try again.\n");
						return;
					}
				}

				//System.out.println("OID IS: " + oid);
				
				//Now that we have the oid of the offender in question (phew) we need to
				//create an offense entry
				int offenseYear;
				int offenseMonth;
				int offenseDay;
				String offenseDescription;
				String offenseAddress;
				int ofid = -1;
				String borough;
				
				//obtain info about offense record from user
				System.out.println("Please enter the following information about this offense:\n");
				
				offenseYear = getUserChoice_int("Year (NOTE: must enter a 4-digit number): ");
				if (offenseYear > 9999 || offenseYear < 1000)
				{
					System.out.println("Must enter a 4-digit number. Please try again.");
					return;
				}
				offenseMonth = getUserChoice_int("Month (NOTE: must enter a number from 1 to 12): ");
				if (offenseMonth > 12 || offenseMonth < 1)
				{
					System.out.println("Must enter a number from 1 to 12! Please try again.");
					return;
				}
				offenseDay = getUserChoice_int("Day: ");
				if (offenseDay < 1 || offenseDay > 31)
				{
					System.out.println("Must enter a number from 1 to 31! Please try again");
					return ;
				}
				offenseDescription = getUserChoice_str("Description: ");
				offenseAddress = getUserChoice_str("Address of offense: ");
				String date_committed = "" + offenseYear + "-" + offenseMonth + "-" + offenseDay;
				
				//insert offense record into db
				String insertOffense = "INSERT INTO Offense (date_committed, description, address) VALUES ('" + date_committed + "', '" + offenseDescription + "', '" + offenseAddress + "')";
				statement.executeUpdate(insertOffense);
				
				//acquire OFID of Offense just added from DB
			    String retrieveSQL = "SELECT SYSIBM.IDENTITY_VAL_LOCAL() AS id FROM Offense"; 
			    ResultSet newOffense = statement.executeQuery(retrieveSQL);
			    while (newOffense.next())
			    {
			    	ofid = (new Integer(newOffense.getString("id"))).intValue();
			    }
				//link these to the offender in the offender_commits_offense relation
			    String linkOffenderOffense = "INSERT INTO Offender_commits_offense VALUES (" + oid + "," + ofid + ")";
				statement.executeUpdate(linkOffenderOffense);
			    
				//determine if this offense was a traffic violation or a crime
				System.out.println("\nWas this offense a CRIME or a TRAFFIC VIOLATION?");
				int choice = -1;
				while (!(choice == 0 || choice == 1))
				{
					choice = getUserChoice_int("Please type 0 for a CRIME or 1 for a TRAFFIC VIOLATION: ");
					if (!(choice == 0 || choice == 1))
					{
						System.out.println("YOU MUST ENTER A 0 OR A 1 TO CONTINUE!");
					}
				}
				//obtain borough where this incident occurred
				System.out.println("\nIn which borough did this incident occur?\n");
				borough = selectBorough(statement);
				
				//if it was a crime, create an entry in the crime relation
				if (choice == 0)
				{
					int severity = -1;
					//get user to choose borough where the crime occurred					
					
					//get severity of crime
					while (!(severity < 4 && severity > 0))
					{
						severity = getUserChoice_int("\nPlease input a number from 1 to 3 indicating the severity of this crime (higher = more severe): ");
						//check for error in input
						if (!(severity < 4 && severity > 0))
						{
							System.out.println("Please select a number from 1 to 3.");
						}
					}
					
					//insert this into the db
					String insertCrime = "INSERT INTO Crime VALUES(" + ofid + ", '" + borough + "'," + severity + ")";
					statement.executeUpdate(insertCrime);

				}
				//if it was a traffic violation, create an entry in the traffic violation relation
				else 
				{
					String firstSN = getUserChoice_str("Enter name of first street: ");
					String secondSN = getUserChoice_str("Enter name of second street: ");
					boolean newTV = false;
					int intersectionID = -1;
					String intersectionMatch = "SELECT * FROM Intersection WHERE first_street_name = '" + firstSN + "' AND second_street_name = '" + secondSN + "' AND borough_bid = '" + borough + "'";
					ResultSet intersectionMatches = statement.executeQuery(intersectionMatch);
					count = 1;
					ArrayList<Integer> intersections = new ArrayList<Integer>();
					while (intersectionMatches.next())
					{
						System.out.println("First street name: " + intersectionMatches.getString("first_street_name") + " Second street name: " + intersectionMatches.getString("second_street_name") + " Borough: " + intersectionMatches.getString("borough_bid"));
						intersections.add(new Integer(intersectionMatches.getString("iid")));
						count++;
					}
					//if there were multiple matches, ask user to input choice or if they want to make a new one 
					//and set intersectionID 
					if (intersections.size() > 1)
					{
						choice = -1;
						while (!(choice > -1 && choice < intersections.size() + 1))
						{
							choice = getUserChoice_int("Please select one of the above intersections. Otherwise,\n" +
														"type 0 to create a new intersection record to use: ");
							if (!(choice > -1 && choice < intersections.size() + 1))
							{
								System.out.println("You must type a valid number.");
							}
						}
						if (choice == 0)
						{
							newTV = true;
						}
						else
						{
							intersectionID = intersections.get(choice - 1).intValue();
						}
					}
					
					//if there was only one match ask user if they want to use that one or create a new intersection
					//and set intersectionID
					else if (intersections.size() == 1)
					{
						choice = -1;
						while (!(choice == 0 || choice == 1))
						{
							choice = getUserChoice_int("Only one record was found, if would you like to use this\n" +
									"intersection type 0, otherwise to create a new one type 1: ");	
							if(!(choice == 0 || choice == 1))
							{
								System.out.println("Please select either 0 or 1");
							}
						}
						if (choice == 0)
						{
							intersectionID = intersections.get(0);
						}
						else
						{
							newTV = true;
						}
					
					}
					
					//else if there were no matches
					else 
					{
						newTV = true;
					}
					
					//create new entry in intersection if newTV = true and set intersectionID to this result
					if (newTV)
					{
						String insertNewIntersection = "INSERT INTO Intersection (first_street_name, second_street_name, borough_bid) VALUES('" + firstSN + "', '" + secondSN + "', '" + borough + "')";
						statement.executeUpdate(insertNewIntersection);
						String retrieveIntersection = "SELECT SYSIBM.IDENTITY_VAL_LOCAL() AS id FROM Intersection";
						ResultSet lastEntered = statement.executeQuery(retrieveIntersection);
						while (lastEntered.next())
						{
							intersectionID = (new Integer(lastEntered.getString("id"))).intValue();
						}
						
					}
					
					//finally, insert into the traffic_violation relation
					String insertTV = "INSERT INTO traffic_violation VALUES(" + ofid + "," + intersectionID + ")";
					statement.executeUpdate(insertTV);

				}
			}
			catch (SQLException e)
			{
				System.out.println("SQL ERROR OCCURRED");
				e.printStackTrace();
				return;
			}		
			
			System.out.println("\nCREATION OF NEW OFFENSE SUCCESSFUL\n");
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
		// Get the year before
		int previousyear = previousYear(year, statement);
		
		// Calculate the if crime rate is going up or down in this borough
		double current_crimerate = crimeRate(borough, year, statement);
		double previous_crimerate = crimeRate(borough, previousyear, statement);
		
		System.out.println("Current crime rate: "+current_crimerate);
		System.out.println("Previous crime rate: "+previous_crimerate);
		
		// If the crime rate has increased, increase the salaries of specified officers 
		// Of the police stations of that borough
		if(current_crimerate > 0  && previous_crimerate > 0 && previous_crimerate < current_crimerate) {
			String query = "UPDATE police_officer SET salary = salary*"+perc+" WHERE rank="+rank+" AND police_station_psid IN (SELECT psid FROM police_station WHERE borough_bid="+"'"+borough+"')";
			try {
				statement.executeUpdate(query);
				System.out.println("Successfully updated salaries of all officers in "+borough+" with a rank of "+rank);
			} catch (SQLException e) {
				e.printStackTrace();
				System.out.println("Unable to update salaries of the specified officers.");
			}
		}
		
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
