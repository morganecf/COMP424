import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.math.*;

public class Optimization {
	
	static Simplex lp;

	/*Statement statement;
	
	public Optimization(Statement statement) {
		this.statement = statement;
	}*/
	
	// Retrieve the average salary for each function 
	public static int[] averageSalary(Statement stmt) {
		int[] salaries = new int[11]; 	//hardcoded because never changes
		try {
			ResultSet rs = stmt.executeQuery("SELECT AVG(salary) FROM police_officer GROUP BY function");
			int i = 0;
			while(rs.next()) {
				salaries[i] = rs.getInt(1);
				i ++;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Unable to retreive average salaries");
		}
		return salaries;
	}
	
	public static int costPerOfficer(String borough, Statement stmt) {
		String q = "SELECT SUM(non_salary_cost_per_officer) FROM police_station WHERE borough_bid = '"+borough+"'";
		return numOffenses_helper(q, "non salary cost per officer", borough, stmt);
	}
	
	public static int getBudget(String borough, Statement stmt) {
		String q = "SELECT budget FROM police_station WHERE borough_bid='"+borough+"'";
		int budget = 0;
		try {
			ResultSet rs = stmt.executeQuery(q);
			if(rs.next()) {
				budget = rs.getInt("BUDGET");
			}
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Unable to retrieve budget information for ps in borough "+borough);
		}
		return budget;
	}
	
	public static int numOffenses_helper(String query, String info, String borough, Statement stmt) {
		int number = 0;
		try {
			ResultSet rs = stmt.executeQuery(query);
			if(rs.next()) {
				number = rs.getInt(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Unable to retrieve number of "+info+" in borough "+borough+": SQL error.");
		}
		return number;
	}
	
	// Retrieve number of offenses/crimes/traffic violations
	// Either in total for a borough or based on crime type 
	public static int numOffenses(String offensetype, int crimetype, String borough, Statement stmt) {
		int numoffenses = 0;
		if(offensetype == "offense") {
			String q = "SELECT COUNT(DISTINCT ofid) FROM offense WHERE ofid IN (SELECT offense_ofid FROM crime " +
							"WHERE borough_bid = '"+borough+"') " +
							"OR ofid IN (SELECT o.ofid FROM Offense o, traffic_violation t " +
							"WHERE o.ofid = t.offense_oid AND t.intersection_iid IN " +
							"(SELECT iid FROM intersection WHERE borough_bid ='"+borough+"'))";
			numoffenses = numOffenses_helper(q, "offenses", borough, stmt);
		}
		else if(offensetype == "traffic") {
			String q = "SELECT COUNT(offense_oid) FROM traffic_violation WHERE intersection_iid in " +
					"(SELECT iid FROM intersection WHERE borough_bid = '"+borough+"')";
			numoffenses = numOffenses_helper(q, "traffic violations", borough, stmt);
		}
		else {
			if(crimetype == 1) {
				String q = "SELECT COUNT(offense_ofid) FROM crime WHERE borough_bid = '"+borough+"' AND severity=1";
				numoffenses = numOffenses_helper(q, "crimes-type1", borough, stmt);
			}
			else if(crimetype == 2) {
				String q = "SELECT COUNT(offense_ofid) FROM crime WHERE borough_bid = '"+borough+"' AND severity=2";
				numoffenses = numOffenses_helper(q, "crimes-type2", borough, stmt);
			}
			else if(crimetype == 3){
				String q = "SELECT COUNT(offense_ofid) FROM crime WHERE borough_bid = '"+borough+"' AND severity=3";
				numoffenses = numOffenses_helper(q, "crimes-type3", borough, stmt);
			}
			else {
				String q = "SELECT COUNT(offense_ofid) FROM crime WHERE borough_bid = '"+borough+"'";
				numoffenses = numOffenses_helper(q, "crimes", borough, stmt);
			}
		}
		return numoffenses;
	}
	
	// Retrieve number of officers of each type
	public static int[] numOfficer(String borough, Statement stmt) {
		int[] numtypes = new int[11]; 	//hardcoded because never changes
		try {
			ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM police_officer WHERE police_station_psid in " +
					"(SELECT psid FROM police_station WHERE borough_bid='"+borough+"') GROUP BY function");
			int i = 0;
			while(rs.next()) {
				numtypes[i] = rs.getInt(1);
				i ++;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return numtypes;
	}
	
	// Get the number distinct intersections in a given borough
	public static int intersections(Statement stmt, String borough) {
		int numinter = 0;
		try {
			ResultSet rs = stmt.executeQuery("SELECT COUNT(DISTINCT iid) FROM intersection WHERE borough_bid = '"+borough+"'");
			if (rs.next()) {
				numinter = rs.getInt(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Could not retrieve functions of police officers: SQL error.");
		}
		return numinter;
	}
	
	public static List<String> getBoroughs(Statement stmt) {
		List<String> boroughs = new ArrayList<String>();
		try {
			ResultSet rs = stmt.executeQuery("SELECT name FROM borough");
			while(rs.next()) {
				boroughs.add(rs.getString("NAME"));		
			}
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Could not retrieve boroughs: SQL error.");
		}
		return boroughs;
	}
	
	// Get the distinct types of police officers
	public static List<String> functions(Statement stmt) {
		List<String> functions = new ArrayList<String>();
		try {
			ResultSet rs = stmt.executeQuery("SELECT DISTINCT function FROM police_officer");
			while(rs.next()) {
				functions.add(rs.getString("FUNCTION"));		
			}
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Could not retrieve functions of police officers: SQL error.");
		}
		return functions;
	}
	
	public static int currentYear() {
		Calendar rightnow = Calendar.getInstance();
		return rightnow.get(Calendar.YEAR);
	}
	
	// Calculate the crime rate in a borough for a given year
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
			}
			else {
				return -1.0;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return -1.0;
		}
		try {
			ResultSet numcrimes = stmt.executeQuery(numcrimes_query);
			if(numcrimes.next()) {
				crimecount = numcrimes.getDouble(1);
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
	
	// Gather all information needed for Simplex method and create matrix
	// Operates on per-borough (per-station) basis
	// One station per borough
	public static double[][] createMatrix(Statement stmt, String borough) {
		
		// TODO: FIX SALARIES
		
		//System.out.println("\n Determining efficient allocation of intervention officers, inspectors, K9 officers, patrol officers, and tactical officers");
		//System.out.println("\n Setting up linear programming problem...");
		
		// Find the average salary for each function
		int[] salaries = averageSalary(stmt);
		
		// Find the number of each type (function) of officer in the borough
		int[] numtypes = numOfficer(borough, stmt);
		
		// Calculate the total number of officers in this borough
		int total_officers = 0;
		for(int n : numtypes) { total_officers += n; }
		
		// Calculate the total cost for all officers in this borough
		int cost_per_officer = costPerOfficer(borough, stmt);
		int total_cost = cost_per_officer*total_officers;
		
		// Get the budget for this police station
		int budget = getBudget(borough, stmt);
				
		// Get the number of distinct traffic intersections in the borough
		int intersections = intersections(stmt, borough);
		
		// TODO: Could use more of these values
		
		// Get the number of offenses/crimes/traffic violations/crimes by severity committed in this borough
		//int offenses = numOffenses("offense", 0, borough, stmt);
		//int violations = numOffenses("traffic", 0, borough, stmt);
		int crimes = numOffenses("crime", 0, borough, stmt);
		int crimes_1 = numOffenses("crime", 1, borough, stmt);
		int crimes_2 = numOffenses("crime", 2, borough, stmt);
		//int crimes_3 = numOffenses("crime", 3, borough, stmt);
		
		// Index order of police functions:
		// 0-Chief Inspector, 1-Commander, 2-Constable, 3-Inspector, 4-Intervention, 5-Investigator, 6-K9
		// 7-Lieutenant, 8-Patrol Officer, 9-Sergeant, 10-Tactical Response
		// 0, 1, 2, 3, 7, 9 are constant
		// Want to figure out how many of 4, 5, 6, 8, 10 a station needs
		// Index order in matrix: {Intervention, Investigator, K9, Patrol, Tactical}
		
		// So want to maximize Cx : Ax <= b, x >= 0
		
		// Vector C: want to maximize sum of all police officers in a station
		double[] C = {1, 1, 1, 1, 1};
		
		// Matrix A and b: the constraint functions (separate later for clarity)
		
		// Row 1: #patrol >= #traffic intersections
		double[] row1 = {0, 0, 0, -1, 0};
		// Row 2: Sum of all officers except patrol >= #crimes/3
		double[] row2 = {-1, -1, -1, 0, -1};
		// Row 3: #K9 >= #type-2 crimes/5 (drug crimes)
		double[] row3 = {0, 0, -1, 0, 0};
		// Row 4: #investigators + patrol >= #type-1 crimes/5 (property crimes)
		double[] row4 = {0, -1, 0, -1, 0};
		// Row 5: Sum of all officers <= 50*(number of commanders)
		double[] row5 = {1, 1, 1, 1, 1};
		// Row 6: Combined salaries <= budget - (total cost per officer + salary of others)
		int other_salaries = (salaries[0]*numtypes[0]) + (salaries[1]*numtypes[1]) + (salaries[2]*numtypes[2]) + (salaries[3]*numtypes[3]) + (salaries[7]*numtypes[7]) + (salaries[9]*numtypes[9]);  
		double remaining = (double) (budget - (other_salaries+total_cost));
		double[] row6 = {salaries[4], salaries[5], salaries[6], salaries[8], salaries[10]};
		// Row 7: #investigators <= 3 per borough
		double[] row7 = {0, 1, 0, 0, 0};
		
		// b values
		double v1 = Math.ceil((double) (0 - intersections));
		double v2 = Math.ceil(0.0 - (((double) crimes)));
		double v3 = Math.ceil(0.0 - (((double) crimes_2)));
		double v4 = Math.ceil(0.0 - (((double) crimes_1)));
		double v5 = Math.ceil(((double) 50*numtypes[1]));
		double v6 = remaining;
		double v7 = 3;
		
		// Vector b
		double[] b = {v1, v2, v3, v4, v5, v6, v7};
	
		/*
		System.out.println("Borough: "+borough);
		System.out.println("Number of intersections: "+intersections);
		System.out.println("Number of crimes: "+crimes);
		System.out.println("Number of crimes type 2: "+crimes_2);
		System.out.println("Number of crimes type 1: "+crimes_1);
		System.out.println("Number of commanders * 50: "+v5);
		System.out.println("Budget: "+budget);
		System.out.println("Salary of others (approximate): "+other_salaries);
		System.out.println("Total cost of this station: "+total_cost);
		System.out.println("Remaining budget: "+remaining);
		*/
		
		// Put all the vectors in a matrix
		double[][] A = {C, row1, row2, row3, row4, row5, row6, row7, b};
		return A;
	}
	
	public static int[] findOptimalDistribution(String borough, Statement stmt) {
		
	//	System.out.println("Running Simplex method...");
		
		double[][] all = createMatrix(stmt, borough);
		
		// Get individual components
		double[] c = all[0];
		double[] b = all[8];
		double[][] A = {all[1], all[2], all[3], all[4], all[5], all[6], all[7]};
				
		// Set up the LP problem and run Simplex method
		lp = new Simplex(A, b, c);
		
		// Optimal value
		int val = (int) Math.ceil(lp.value());
		
		// Primal values - number of each officer
		double[] x = lp.primal();
		
		// Index order in matrix: {Intervention, Investigator, K9, Patrol, Tactical}
		String[] functions = {"Intervention", "Investigator", "K9", "Patrol", "Tactical"};
		
	//	System.out.println("\nOptimal allocation of police officers in borough "+borough);
		// Take roof of these values and convert to integers
		int[] officers = new int[5];
	
		
		for(int i=0; i<5; i++) {
			officers[i] = (int) Math.ceil(x[i]);
			//System.out.println(functions[i]+":"+officers[i]);
		}
		
	//	System.out.println("Total optimal value: "+val);
	//	System.out.println("================================");
		
		return officers;
	}
	
	// Find index of max value in array
	public static int max(double[] arr) {
		double max_val = 0;
		int max_ind = 0;
		for(int i=0; i<arr.length; i++) {
			if(arr[i] > max_val) {
				max_val = arr[i];
				max_ind = i;
			}
		}
		return max_ind;
	}
	
	// Sum up integers in one column of a matrix
	public static int sum(int[][] matrix, int column) {
		int sum = 0;
		for(int i=0; i<matrix.length; i++) {
			sum += matrix[i][column];
		}
		return sum;
	}
	
	public static int[] distribute(int[] current_totals, int[] opt_totals, int[] opt_distrib) {
		double[] feasible = new double[5];
		for(int i=0; i<5; i++) {
			if(opt_totals[i] == 0) {
				feasible[i] = 0;
			}
			else {
				feasible[i] = (double) current_totals[i] * ((double)opt_distrib[i]/(double)opt_totals[i]);
			}
		}
		int[] feasible_int = new int[5];
		for(int i=0; i<5; i++) { feasible_int[i] = (int) Math.ceil(feasible[i]); }
		return feasible_int;
	}
	
	/*
	// Calculate the feasible distributions for all of the boroughs given current and opt distribution
	// Rank by crime rate
	public static int[][] feasibleDistributions(Statement stmt) {
		List<String> boroughs = getBoroughs(stmt);
		int total = boroughs.size();
		int[][] current_distribs = new int[total][5];
		int[][] opt_distribs = new int[total][5];
		double[] crime_rate = new double[total];
		int curr_year = currentYear();
		
		// Will hold feasible distributions
		int[][] feasible_distribs = new int[total][5];
				
		// Find the current and optimal distributions for all 19 boroughs
		// Find the crime rate for each of 19 boroughs
		for(int i=0; i<total; i++) {
			// Current
			int[] nums = numOfficer(boroughs.get(i), stmt);
			int[] rowc = {nums[4], nums[5], nums[6], nums[8], nums[10]};
			current_distribs[i] = rowc;
			// Optimal
			int[] rowo = findOptimalDistribution(boroughs.get(i), stmt);
			opt_distribs[i] = rowo;
			// Crime rate
			crime_rate[i] = crimeRate(boroughs.get(i), curr_year, stmt);
		}
		
		// Current and optimal totals of each type of officer
		int[] current_totals = new int[5];
		int[] opt_totals = new int[5];
		for(int i=0; i<5; i++) {
			current_totals[i] = sum(current_distribs, i);
			opt_totals[i] = sum(opt_distribs, i);
		}
		
	//	for(int i=0; i<current_totals.length; i++) {
	//		System.out.println("Current total of type "+i+": "+current_totals[i]);
	//		System.out.println("Optimal total of type "+i+": "+opt_totals[i]);
	//	}
		
		// Now find the feasible distributions
		// Boroughs with highest crime rates get first choice (greedy method)
		for(int i=0; i<total; i++) {
			int max_ind = max(crime_rate);
			// Get the feasible distribution for this borough
			feasible_distribs[max_ind] = distribute(current_totals, opt_totals, opt_distribs[max_ind]);
			// Now decrement values 
			for(int j=0; j<5; j++) {
				current_totals[j] = current_totals[j] - feasible_distribs[max_ind][j];
			}
			// Now set the crime rate value of this borough to -1 to avoid picking it next
			crime_rate[max_ind] = -1;
		}
		
		System.out.println("\n\n");
		System.out.println("Intervention | Investigator | K9 | Patrol | Tactical");
		
		System.out.println("******************* CURRENT DISTRIBUTIONS *******************");
		for(int i=0; i<total; i++) {
			System.out.print("Station "+(i+1)+"\t");
			for(int j=0; j<5; j++) {
				System.out.print(current_distribs[i][j]+" | ");
			}
			System.out.println("");
		}
		
		System.out.println("******************* OPTIMAL DISTRIBUTIONS *******************");
		for(int i=0; i<total; i++) {
			System.out.print("Station "+(i+1)+"\t");
			for(int j=0; j<5; j++) {
				System.out.print(opt_distribs[i][j]+" | ");
			}
			System.out.println("");
		}
		
		System.out.println("******************* FEASIBLE DISTRIBUTIONS *******************");
		for(int i=0; i<total; i++) {
			System.out.print("Station "+(i+1)+"\t");
			for(int j=0; j<5; j++) {
				System.out.print(feasible_distribs[i][j]+" | ");
			}
			System.out.println("");
		}
		
		
		return feasible_distribs; 
	}
	*/
	
	public static List<Object> getOfficerPOIDs(Statement stmt, String function) {
		List<Object> poids = new ArrayList<Object>();
		String q = "SELECT poid FROM police_officer WHERE function = '"+function+"'";
		try {
			ResultSet rs = stmt.executeQuery(q);
			while(rs.next()) {
				poids.add(rs.getInt("POID"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Unable to retrieve police officer poids for "+function);
		}
		return poids;
	}
	
	public static List<Object> policeStations(Statement stmt) {
		List<Object> psids = new ArrayList<Object>();
		try {
			ResultSet rs = stmt.executeQuery("SELECT DISTINCT psid FROM police_station");
			while(rs.next()) {
				psids.add(rs.getInt("PSID"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Unable to retrieve police station ids");
		}
		return psids;
	}
	
	public static void update(int poid, int psid, Statement stmt) {
		String up = "UPDATE police_officer SET police_station_psid = "+psid+" WHERE poid = "+poid;
		try {
			stmt.executeUpdate(up);
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Failed to transfter police officer "+poid+" to police station "+psid);
		}
	}
	
	// Deletes a police officer from the database if there are some remaining 
	public static void fire(List<Object> poids, Statement stmt) {
		for(Object poid : poids) {
			int id = (int) (Integer) poid;
			int del;
			try {
				del = stmt.executeUpdate("DELETE FROM police_officer WHERE poid = "+id);
				if(del != 1) {
					System.out.println("Failed to delete police officer "+id+" from the database.");
				}
			} catch (SQLException e) {
				e.printStackTrace();
				System.out.println("Failed to delete police officer "+id+" from the database.");
			}
			
		}
	}

	// Randomly distribute each type of police officer to police stations based on the results of feasible distributions
	public static void optimize(Statement statement) {
		
		// Get the current and optimal distributions to display 
		List<String> boroughs = getBoroughs(statement);
		int total = boroughs.size();
		int[][] current_distribs = new int[total][5];
		int[][] opt_distribs = new int[total][5];
		
		//double[] crime_rate = new double[total];
		//int curr_year = currentYear();
				
		// Find the current and optimal distributions for all 19 boroughs
		// Find the crime rate for each of 19 boroughs
		for(int i=0; i<total; i++) {
			// Current
			int[] nums = numOfficer(boroughs.get(i), statement);
			int[] rowc = {nums[4], nums[5], nums[6], nums[8], nums[10]};
			current_distribs[i] = rowc;
			// Optimal
			int[] rowo = findOptimalDistribution(boroughs.get(i), statement);
			opt_distribs[i] = rowo;
			// Crime rate
			//crime_rate[i] = crimeRate(boroughs.get(i), curr_year, statement);
		}
		
		System.out.println("\nIntervention | Investigator | K9 | Patrol | Tactical");
		
		System.out.println("******************* CURRENT DISTRIBUTIONS *******************");
		for(int i=0; i<total; i++) {
			System.out.print("Station "+(i+1)+"\t");
			for(int j=0; j<5; j++) {
				System.out.print(current_distribs[i][j]+" | ");
			}
			System.out.println("");
		}
		
		System.out.println("******************* OPTIMAL DISTRIBUTIONS *******************");
		for(int i=0; i<total; i++) {
			System.out.print("Station "+(i+1)+"\t");
			for(int j=0; j<5; j++) {
				System.out.print(opt_distribs[i][j]+" | ");
			}
			System.out.println("");
		}
		
		// Get all the POIDs for each type of officer
		List<Object> intervention_poids = getOfficerPOIDs(statement, "Intervention");
		List<Object> investigator_poids = getOfficerPOIDs(statement, "Investigator");
		List<Object> K9_poids = getOfficerPOIDs(statement, "K9");
		List<Object> patrol_poids = getOfficerPOIDs(statement, "Patrol Officer");
		List<Object> tactical_poids = getOfficerPOIDs(statement, "Tactical");
		
		System.out.println("\n\nNow updating PSID assignment as best as possible...");
		
		// Get police station IDs
		List<Object> psids = policeStations(statement);
		
		// Iterate through feasible distribution -- through each borough(police station)
		for(int i=0; i<psids.size(); i++) {
			
			// psid corresponding to this police station
			int psid = (int) (Integer) psids.get(i);
			
			//Randomly distribute all the intervention officers
			while(opt_distribs[i][0] > 0 || !intervention_poids.isEmpty()) {
				if(intervention_poids.size() == 0) {
					break;
				}
				int index = (int) (Math.random()*(intervention_poids.size()-1));
				int poid = (int) (Integer) intervention_poids.get(index);
				
				// Update the database - assign this police officer the current police station
				//update(poid, psid, statement);
				
				// Decrement officers needed for this police station
				opt_distribs[i][0] --;
				// Remove the officer that was assigned this police station
				intervention_poids.remove(index);
			}
			
			//Randomly distribute all the investigators
			while(opt_distribs[i][0] > 0 || !investigator_poids.isEmpty()) {
				if(investigator_poids.size() == 0) {
					break;
				}
				int index = (int) (Math.random()*investigator_poids.size());
				int poid = (int) (Integer) investigator_poids.get(index);
				
				// Update the database - assign this police officer the current police station
				//update(poid, psid, statement);
				
				// Decrement officers needed for this police station
				opt_distribs[i][1] --;
				// Remove the officer that was assigned this police station
				investigator_poids.remove(index);
			}
			
			// Randomly distribute all the K9 officers
			while(opt_distribs[i][0] > 0 || !K9_poids.isEmpty()) {
				if(K9_poids.size() == 0) {
					break;
				}
				int index = (int) (Math.random()*K9_poids.size());
				int poid = (int) (Integer) K9_poids.get(index);
				
				// Update the database - assign this police officer the current police station
				//update(poid, psid, statement);
				
				// Decrement officers needed for this police station
				opt_distribs[i][2] --;
				// Remove the officer that was assigned this police station
				K9_poids.remove(index);
			}
			
			// Randomly distribute all the patrol officers
			while(opt_distribs[i][0] > 0 || !patrol_poids.isEmpty()) {
				if(patrol_poids.size() == 0) {
					break;
				}
				int index = (int) (Math.random()*patrol_poids.size());
				int poid = (int) (Integer) patrol_poids.get(index);
				
				// Update the database - assign this police officer the current police station
				//update(poid, psid, statement);
				
				// Decrement officers needed for this police station
				opt_distribs[i][3] --;
				// Remove the officer that was assigned this police station
				patrol_poids.remove(index);
			}
			
			// Randomly distribute all the tactical officers
			while(opt_distribs[i][0] > 0 || !tactical_poids.isEmpty()) {
				if(tactical_poids.size() == 0) {
					break;
				}
				int index = (int) (Math.random()*tactical_poids.size());
				int poid = (int) (Integer) tactical_poids.get(index);
				
				// Update the database - assign this police officer the current police station
				//update(poid, psid, statement);
				
				// Decrement officers needed for this police station
				opt_distribs[i][4] --;
				// Remove the officer that was assigned this police station
				tactical_poids.remove(index);
			}
			
		}
		
		System.out.println("\n\nNow firing left over police officers...");
		System.out.println("There are "+intervention_poids.size()+" remaining intervention officers");
		System.out.println("There are "+investigator_poids.size()+" remaining investigators");
		System.out.println("There are "+K9_poids.size()+" remaining K9 officers");
		System.out.println("There are "+patrol_poids.size()+" remaining patrol officers");
		System.out.println("There are "+tactical_poids.size()+" remaining tactical officers");
		
		// Now fire the remaining officers, since don't need them!
	//	fire(intervention_poids, statement);
	//	fire(investigator_poids, statement);
	//	fire(K9_poids, statement);
	//	fire(patrol_poids, statement);
	//	fire(tactical_poids, statement);
		
	}
	

	/*
	public static void main(String[] args) {
		
		DBConnect db = new DBConnect("jdbc:db2://db2.cs.mcgill.ca:50000/cs421", "cs421g10", "LewVe-g5");
		Statement statement = db.getStatement();
						
		optimize(statement);
		
		//update(6, 16, statement);
		
		
		List<Object> test = new ArrayList<Object>();
		Object sample = (Object) (Integer) 22;
		test.add(sample);
		fire(test, statement);
		
		for(int i=0; i<19; i++) {
			for(int j=0; j<5; j++) {
				System.out.println("Station "+i+", officer type "+j+": "+feas_dist[i][j]);
			}
		}		
		
		// Test runs
		System.out.println("========LACHINE========");
		findOptimalDistribution("Lachine", statement);
		System.out.println("=======LE PLATEAU MONT-ROYAL=======");
		findOptimalDistribution("Le Plateau-Mont Royal", statement);
		System.out.println("=========VERDUN=======");
		findOptimalDistribution("Verdun", statement);
		System.out.println("=========OUTREMONT=======");
		findOptimalDistribution("Outremont", statement);
		System.out.println("=========SAINT-LAURENT=======");
		findOptimalDistribution("Saint-Laurent", statement); 
		System.out.println("=========ANJOU=======");
		findOptimalDistribution("Anjou", statement); 
		System.out.println("=========AHUNTSIC-CARTIERVILLE=======");
		findOptimalDistribution("Ahuntsic-Cartierville", statement); 
		System.out.println("=========COTE-DES-NEIGES=======");
		findOptimalDistribution("Cote-des-Neiges", statement); 
		
		
		try {
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Couldn't close.");
		}
	}
	*/
}
