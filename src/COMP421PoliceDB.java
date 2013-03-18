import java.sql.*;
import java.text.DecimalFormat;
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
				//System.out.println("Population of "+borough+" in "+year+": "+population);
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
				//System.out.println("Number of crimes in "+borough+" in "+year+": "+crimecount);
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
	
	public static int query_int(String query, Statement stmt, String info) {
		try {
			ResultSet RS = stmt.executeQuery(query);
			if(RS.next()) {
				return RS.getInt(1);
			}
			else {
				System.out.println("No "+info+" information available.");
				return -1;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Failed to retrieve "+info);
			return -1;
		}
	}
	
	// TODO: error handling: throw exception if not enough info/0-values?  
	// Checks to see if combined salary of the officers in a station + non-salary expenses exceeds the budget
	// Given new knowledge (increase of salary based on rank and percent)
	public static boolean checkBudget(int police_station, int rank, double percent, Statement stmt) {
		String q1 = "SELECT SUM(salary+salary*percent) FROM police_officer WHERE rank="+rank+" AND police_station_psid = "+police_station;
		String q2 = "SELECT COUNT(*) FROM police_officer WHERE police_station_psid = "+police_station;
		String q3 = "SELECT budget FROM police_station WHERE psid= "+police_station;
		String q4 = "SELECT non_salary_cost_per_officer FROM police_station WHERE psid = "+police_station;
		
		int total_salary = query_int(q1, stmt, "salary");
		int num_officers = query_int(q2, stmt, "count of officers");
		int budget = query_int(q3, stmt, "budget");
		int cost_per_officer = query_int(q4, stmt, "non-salary cost per officer");
		
		if(total_salary <= 0 || num_officers <= 0 || budget <= 0 || cost_per_officer <= 0) {
			System.out.println("Not enough information available to perform budget calculations.");
			return false;	// Return false for now
			//Throw exception? 
		}
		else {
			return (total_salary+(num_officers*cost_per_officer)) >= budget;
		}
	}
	
	// Iterates through all police stations in a borough and returns those who are in the red
	public static List<Integer> checkBudgets(String borough, int rank, double percent, Statement stmt) {
		List<Object> police_stations = new ArrayList<Object>();
		List<Integer> red_stations = new ArrayList<Integer>();
		
		// Get all the police stations in a borough
		try {
			ResultSet PS = stmt.executeQuery("SELECT psid FROM police_station WHERE borough_bid='"+borough+"'");
			
			while(PS.next())
			{
				police_stations.add(PS.getObject("PSID"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Failed to retrieve police stations:SQL error.");
		}
		
		// Filter out the police stations whose costs exceeds their budgets and return these
		for(Object station_obj : police_stations) {
			int station = (int) (Integer) station_obj;
			if(!checkBudget(station, rank, percent, stmt)) {
				red_stations.add((Integer)station);
			}
		}
		
		return red_stations;
		
	}

	//TODO: Test this!!
	//TODO: make sure to check if ResultSet is empty or not!
	//TODO: remove stack traces
	// Increase the salary of all police officers with ranking >= x, in a specific borough
	// Increase by a certain percentage based on the crime rate of that borough
	// Prompt user for ranking, percentage
	// Have error handling if combined salary exceeds budget
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
			
			// First check to see if updating will put any police stations in financial duress, remove these
			List<Integer> good_stations = checkBudgets(borough, rank, perc, statement);
			System.out.println("There are "+good_stations.size()+" stations that can be updated.");
			
			// Now only update those stations 
			for(Integer gs : good_stations) {
				int psid = (int) gs;
				String query = "UPDATE police_officer SET salary = salary + salary*"+perc+" WHERE rank="+rank+" AND police_station_psid = "+psid;
				
				try {
					statement.executeUpdate(query);
					System.out.println("Successfully updated salaries of all officers in police station "+psid);
					
				} catch (SQLException e) {
					e.printStackTrace();
					System.out.println("Unable to update salaries of the specified officers.");
				}
			}
		}
		
		else {
			System.out.println("Salaries should not be increased.");
		}
		
		return;
	}
	
	// Update the population with new census information 
		// Asks for the new population of each borough
		// If the crime rate changes by a certain amount then ask user if want to reallocate, then run analyze() ??
		public static void updatePopulation(Statement statement)  
		{
			int year;
			
			System.out.println("Please select a census year");
			year = getUserChoice_int("Year: ");
			
			try {
				statement.clearBatch();
				
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
					
					String answer = getUserChoice_str("Would you like to reallocate resources based on new census data? (y/n)");
					
					if(answer.equals("y"))
					{
						optimize(statement);
					}
				}
				
				
				
				
			} catch (SQLException e) {
				System.err.println(" msg: " + e.getMessage() + " code: " + e.getErrorCode() + " state: " + e.getSQLState());
				return;
			}
		}
	
		// Increase the salary of all police officers with ranking >= x, in a specific borough
		// Increase by a certain percentage based on the crime rate of that borough
		// Compile statistics
		// Ex: boroughs with highest crime rate, intersections with high volume of traffic violations, most criminal gender, etc.
		// Could have option to run all, or spec)ify which borough you want to see stats for (for example)
		public static void runStats(Statement statement) throws SQLException 
		{
			statement.clearBatch();
			int selection;
			
			System.out.println("\nPlease select one of the following options by typing the associated number:\n");
			System.out.println("1. Conduct All Statiscal Analyses");
			System.out.println("2. Borough crime rates");
			System.out.println("3. Show unsolved crimes");
			System.out.println("4. Traffic Violations per Intersection");
			System.out.println("5. Police Station Budget Analysis");
			System.out.println("6. Offense Statistics");

			
			selection = getUserChoice_int("Your Selection: ");
			
			if(selection == 1)
			{
				allStatistics(statement);
			}
			else if(selection == 2)
			{
				showCrimeRates(statement);
			}
			else if(selection == 3)
			{
				showUnsolvedCrimes(statement);
			}
			else if(selection == 4)
			{
				showTrafficViolations(statement);
			}
			else if(selection == 5)
			{
				policeStationBudgetAnalysis(statement);
			}
			else
			{
				offenseStatistics(statement);
			}
			
			return;
		}
	
		
		public static void allStatistics(Statement statement)
		{
			try {
				statement.clearBatch();
				
				System.out.println("\n-------------------------------------------------------------------------------------------------------\n");
				showCrimeRates(statement);
				System.out.println("\n-------------------------------------------------------------------------------------------------------\n");
				showUnsolvedCrimes(statement);
				System.out.println("\n-------------------------------------------------------------------------------------------------------\n");
				showTrafficViolations(statement);
				System.out.println("\n-------------------------------------------------------------------------------------------------------\n");
				policeStationBudgetAnalysis(statement);
				System.out.println("\n-------------------------------------------------------------------------------------------------------\n");
				offenseStatistics(statement);
				System.out.println("\n-------------------------------------------------------------------------------------------------------\n");
				
			} catch (SQLException e) 
			{
				System.err.println(" msg: " + e.getMessage() + " code: " + e.getErrorCode() + " state: " + e.getSQLState());
				return;
			}
			
		}
		
		public static void offenseStatistics(Statement statement)
		{
			System.out.println("\n------Showing offense statistics------\n");
			
			try {
				
				String genderCount = "SELECT COUNT(gender) FROM offender off, offender_commits_offense oco " +
						"WHERE off.oid = oco.offender_oid GROUP BY gender ORDER BY COUNT(gender) DESC FETCH FIRST 1 ROWS ONLY";
				
				String raceCount = "SELECT COUNT(race) FROM offender off, offender_commits_offense oco " +
						"WHERE off.oid = oco.offender_oid GROUP BY race ORDER BY COUNT(race) DESC FETCH FIRST 1 ROWS ONLY";
				
				String highestOffendingGender = "SELECT gender FROM offender off, offender_commits_offense oco " +
						"WHERE off.oid = oco.offender_oid GROUP BY gender ORDER BY COUNT(gender) DESC FETCH FIRST 1 ROWS ONLY";
				
				String highestOffendingRace = "SELECT race FROM offender off, offender_commits_offense oco " +
						"WHERE off.oid = oco.offender_oid GROUP BY race ORDER BY COUNT(race) DESC FETCH FIRST 1 ROWS ONLY";
				
				int genderOffensesCommitted;
				int raceOffensesCommitted;
				String highestGender;
				String highestRace;
				
				
				ResultSet gCount = statement.executeQuery(genderCount);
				gCount.next();
				genderOffensesCommitted = gCount.getInt(1);
				
				
				ResultSet rCount = statement.executeQuery(raceCount);
				rCount.next();
				raceOffensesCommitted = rCount.getInt(1);
				
				ResultSet gName = statement.executeQuery(highestOffendingGender);
				gName.next();
				highestGender = gName.getString(1);
				
				if(highestGender.equals("f"))
					highestGender = "female";
				else
					highestGender = "male";
				
				ResultSet rName = statement.executeQuery(highestOffendingRace);
				rName.next();
				highestRace = rName.getString(1);
				
				System.out.println("Gender with Most Offenses Is: " + highestGender + " with " + genderOffensesCommitted + " offenses committed\n");
				System.out.println("Race with Most Offenses Is: " + highestRace + " with " + raceOffensesCommitted + " offenses committed\n");
				
				
					
				
				
				
			} catch (SQLException e) {
				System.err.println(" msg: " + e.getMessage() + " code: " + e.getErrorCode() + " state: " + e.getSQLState());
				return;
			}
		}
		
		public static void policeStationBudgetAnalysis(Statement statement)
		{
			try 
			{
				System.out.println("\n------Running Budget Analysis------\n");
				
				
				ResultSet numStations = statement.executeQuery("SELECT COUNT(*) FROM police_station");
				numStations.next();
				
				ArrayList<PoliceStation> stations = new ArrayList<PoliceStation>();
				
				ResultSet policeStations = statement.executeQuery("SELECT * FROM police_station");
				
				while(policeStations.next())
				{
					stations.add(new PoliceStation(policeStations.getInt("psid"), policeStations.getString("borough_bid"), 
													policeStations.getInt("budget"), policeStations.getInt("non_salary_cost_per_officer"),
													policeStations.getString("address")));
				}
				
				for(PoliceStation station : stations)
				{
					int stationSalary = 0;
					
					ResultSet numberOfOfficers = statement.executeQuery("SELECT Count(*) FROM police_officer WHERE police_station_psid = " + station.getId());
					numberOfOfficers.next();
					
					station.setNumberPoliceOfficers(numberOfOfficers.getInt(1));
					
					
					ResultSet stationPoliceOfficers = statement.executeQuery("SELECT * FROM police_officer WHERE police_station_psid = " + station.getId());
					
					while(stationPoliceOfficers.next())
					{
						stationSalary += stationPoliceOfficers.getInt("salary");
					}
					
					station.setSalaryExpenditure(stationSalary);
					
				}
				
				for(PoliceStation current : stations)
				{
					int budgetSituation = 0;
					int expenditure = (current.getNonSalaryCostPerOfficer() * current.getNumberPoliceOfficers()) + current.getSalaryExpenditure();
					
					budgetSituation = current.getBudget() - expenditure;
					
					System.out.println("\nPolice Station " + current.getId() + " in " + current.getBorough());
					DecimalFormat dollarValueFormat = new DecimalFormat("$###,###.###"); 
					System.out.println("Station budget is: " + dollarValueFormat.format(current.getBudget()));
					System.out.println("Station expenditure is: " + dollarValueFormat.format(expenditure));
					
					if(budgetSituation < 0)
					{
						System.out.println("Station is over budget by: " + dollarValueFormat.format(Math.abs(budgetSituation)));
					}
					else if(budgetSituation > 0)
					{
						System.out.println("Station is under budget by: " + dollarValueFormat.format(budgetSituation));
					}
					else
					{
						System.out.println("Station is exactly on budget It has spent exactly the budget it was allocated");
					}
					
					System.out.println("\n*********************************************************");
				}
				
				
			} catch (SQLException e) {
				System.err.println(" msg: " + e.getMessage() + " code: " + e.getErrorCode() + " state: " + e.getSQLState());
				return;
			}
			
		}
		
		
		//TODO add offender information as well
		public static void showTrafficViolations(Statement statement)
		{
			try {
				statement.clearBatch();
				
				System.out.println("\n------Showing Intersection Traffic Violations------\n");
				
				ResultSet intersections = statement.executeQuery("SELECT * FROM Intersection");
				ArrayList<Intersection> intersectionList = new ArrayList<Intersection>();
				
				while(intersections.next())
				{
					int currentID = intersections.getInt("iid");
					String currentFirstStreet = intersections.getString("first_street_name");
					String currentSecondStreet = intersections.getString("second_street_name");
					String currentBorough = intersections.getString("borough_bid");
					
					Intersection current = new Intersection(currentID, currentFirstStreet, currentSecondStreet, currentBorough);
					intersectionList.add(current);
				}
				
				for(Intersection current : intersectionList)
				{
					System.out.println("\nIntersection: " + current.getFirstStreet() + " and " 
										+ current.getSecondStreet() + " in " + current.getBorough() + "\n");
					
					
					int intersectionID = current.getId();
					
					ResultSet numOffenses = statement.executeQuery("SELECT COUNT(*) FROM traffic_violation WHERE " +
							" intersection_iid = " + intersectionID);
					
					numOffenses.next();
					int numberOffenses = numOffenses.getInt(1);
					
					if(numberOffenses > 0)
					{
						System.out.println("Offenses at Intersection: \n");
						
						int[] offenseArray = new int[numberOffenses];
						int count = 0;
						
						ResultSet offenseIDS = statement.executeQuery("SELECT offense_oid FROM traffic_violation WHERE " +
																		" intersection_iid = " + intersectionID);
						
						while(offenseIDS.next())
						{
							offenseArray[count] = offenseIDS.getInt("offense_oid");
							count++;
						}
						
						
						for(int i = 0; i < numberOffenses; i++)
						{
							ResultSet currentOffense = statement.executeQuery("SELECT * FROM Offense WHERE ofid = " + offenseArray[i]);
							currentOffense.next();
							
							
							System.out.println("Date Committed: " + currentOffense.getDate("date_committed"));
							System.out.println("Description: " + currentOffense.getString("description") + "\n");
							
						}
						
					}
					else
					{
						System.out.println("No Traffic Violations at this Intersection \n");
					}
		
					System.out.println("***********************************************");
						
					
				}
				
				
				
			} catch (SQLException e) {
				System.err.println(" msg: " + e.getMessage() + " code: " + e.getErrorCode() + " state: " + e.getSQLState());
				return;
			}
			
			
		}
		
		public static void showUnsolvedCrimes(Statement statement)
		{
			try {
				
				statement.clearBatch();
				String query = "SELECT * FROM Offense WHERE ofid NOT IN (SELECT offense_ofid FROM Offender_Commits_Offense)";
				
				ResultSet offenseWithoutOffender = statement.executeQuery(query);
				
				System.out.println("\n------Showing Unsolved Crimes------\n");
				
				while(offenseWithoutOffender.next())
				{
					Date dateCommitted = offenseWithoutOffender.getDate("date_committed");
					String description = offenseWithoutOffender.getString("description");
					String address = offenseWithoutOffender.getString("address");
					
					System.out.println("Date Committed: " + dateCommitted);
					System.out.println("Description: " + description);
					System.out.println("Address: " + address + "\n");
					
				}
				
				
				
			} catch (SQLException e) {
				System.err.println(" msg: " + e.getMessage() + " code: " + e.getErrorCode() + " state: " + e.getSQLState());
				return;
			}
			
		}
		
		public static void showCrimeRates(Statement statement)
		{
			int year = getUserChoice_int("\nEnter year for crime rate analysis\n");
			System.out.println("\n------Showing Crime Rates For Selected Year------");
			
			try 
			{
				statement.clearBatch();
				
				ResultSet censusYear = statement.executeQuery("SELECT Count(*) FROM Population WHERE year = " + year);
				censusYear.next();
				
				if(censusYear.getInt(1) == 0)
				{
					System.out.println("\nNo census data available for this year\n");
					return;
				}
				
				List<String> listBoroughs = new ArrayList<String>();
				ResultSet boroughs = statement.executeQuery("SELECT * FROM Borough");
				
				while(boroughs.next())
				{
					listBoroughs.add(boroughs.getString("name"));
				}
				
				System.out.println("Showing crime rate for each borough in " + year);
				
				for(String borough : listBoroughs)
				{
					System.out.println(borough + ":     " + crimeRate(borough, year, statement) + " crimes/100000 people\n");
					
				}
				
				
				
					
			} catch (SQLException e) {
				System.err.println(" msg: " + e.getMessage() + " code: " + e.getErrorCode() + " state: " + e.getSQLState());
				return;
			}
			
			
			
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

