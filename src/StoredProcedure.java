import java.sql.*;

public class StoredProcedure 
{
	public static void updateAllocation(int threshold)
	{
		try
		{
			// stored procedure reallocates a patrol officer to a borough with an intersection that has many violations (above threshold)
			
			Connection con = DriverManager.getConnection("jdbc:default:connection");
			Statement statement = con.createStatement();
			
			//Get intersection with traffic violations over threshold
			ResultSet countInt = statement.executeQuery("SELECT COUNT(*) FROM intersection");
			countInt.next();
			
			int numIntersection = countInt.getInt(1);
			int counter = 0;
			
			int[] intersectionIDS = new int[numIntersection];
			ResultSet IIDS = statement.executeQuery("SELECT iid FROM intersection");
			
			while(IIDS.next())
			{
				intersectionIDS[counter] = IIDS.getInt("iid");
			}
			
			for(int i = 0; i < numIntersection; i++)
			{
				ResultSet countViolations = statement.executeQuery("SELECT COUNT(*) FROM traffic_violation " +
						"WHERE intersection_iid = " + intersectionIDS[i]);
				countViolations.next();
				int numberViolations = countViolations.getInt(1);
				
				if(numberViolations >= threshold)
				{
					ResultSet boroughName = statement.executeQuery("SELECT borough_bid FROM intersection WHERE iid = " +
											intersectionIDS[i]);
					boroughName.next();
					String currentBorough = boroughName.getString("borough_bid");
							
					String countQuery = "SELECT COUNT(*) FROM police_officer WHERE police_station_psid NOT IN" + 
							"(SELECT psid FROM police_station WHERE borough_bid = " + currentBorough + ") AND function = 'Patrol Officer'";
					
					String query = "SELECT * FROM police_officer WHERE police_station_psid NOT IN" + 
								"(SELECT psid FROM police_station WHERE borough_bid = " + currentBorough + ") AND function = 'Patrol Officer'";
					
					ResultSet numCandidates = statement.executeQuery(countQuery);
					numCandidates.next();
					int numberOfCandidates = numCandidates.getInt(1);
					
					if(numberOfCandidates > 0)
					{
						int candidateLocation = 1 + (int)(Math.random() * ((numberOfCandidates - 1) + 1));
						
						ResultSet suitableCandidates = statement.executeQuery(query);
						suitableCandidates.next();
						
						for(int j = 0; j < candidateLocation; j++)
						{
							suitableCandidates.next();
						}
						
						int candidatePOID = suitableCandidates.getInt("poid");
						
						ResultSet stationID = statement.executeQuery("SELECT psid FROM police_station WHERE borough_bid = " + currentBorough);
						stationID.next();
						int policeStationID = stationID.getInt(1);
						
						String updateQuery = "UPDATE police_officer SET police_station_psid = " + policeStationID + " WHERE poid = " + candidatePOID;
						statement.executeUpdate(updateQuery);
					}
					
				}
				
			}
			
			
		}
		catch(SQLException e)
		{
			System.err.println(" msg: " + e.getMessage() + " code: " + e.getErrorCode() + " state: " + e.getSQLState());
			return;
		}
		
		
		
	}
}
