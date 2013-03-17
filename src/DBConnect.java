import java.sql.*;


public class DBConnect {

	//Connection
	private Connection con;
	private Statement stmt;
	
	public DBConnect(String url, String user, String pw)
	{
		//Register driver for DB2
		try {
			System.out.println("Registering driver for DB2...");
			DriverManager.registerDriver (new com.ibm.db2.jcc.DB2Driver()) ;
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			System.out.println("DBConnect.DBConnect(): Attempt to register driver failed. Please ensure correct jar is included in build path.");
			System.exit(0);
		}
		
		//establish connection to database
		try {
			System.out.println("Establishing connection to database...");
			con = DriverManager.getConnection(url, user, pw);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("DBConnect.DBConnect(): Attempt to connect to database failed.");
			System.exit(0);
		}
		
		//creating statement value to use for working with the database
		try {
			System.out.println("Creating statement...");
			stmt = con.createStatement();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			System.out.println("DBConnect.DBConnect(): Attempt to create statement for queries has failed.");
		}
		
		System.out.println("Database successfully connected.");
		
	}
	
	
	public void closeConnection()
	{
		try {
			con.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("DBConnect.closeConnection(): Attempt to close connection to database failed");
		}
		return;
	}
	
	public Statement getStatement()
	{
		return this.stmt;
	}
	
	public ResultSet runQuery(String query)
	{
		ResultSet toReturn = null;
		try {
			toReturn = stmt.executeQuery(query);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("DBConnect.runQuery(): An SQL Exception was thrown.");
		}
		return toReturn;
	}
	
}
