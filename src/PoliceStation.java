
public class PoliceStation 
{
	private int id;
	private String borough;
	private int budget;
	private int nonSalaryCostPerOfficer;
	private String address;
	private int numberPoliceOfficers;
	private int salaryExpenditure;
	
	public PoliceStation(int id, String boro, int budget, int nonSalaryCost, String address)
	{
		this.id = id;
		this.borough = boro;
		this.budget = budget;
		this.nonSalaryCostPerOfficer = nonSalaryCost;
		this.address = address;
		this.setNumberPoliceOfficers(0);
		this.setSalaryExpenditure(0);
	}
	
	public int getId()
	{
		return this.id;
	}
	
	public String getBorough()
	{
		return this.borough;
	}
	
	public int getNonSalaryCostPerOfficer()
	{
		return this.nonSalaryCostPerOfficer;
	}
	
	public String getAddress()
	{
		return this.address;
	}
	
	public int getBudget()
	{
		return this.budget;
	}

	public int getNumberPoliceOfficers() 
	{
		return numberPoliceOfficers;
	}

	public void setNumberPoliceOfficers(int numberPoliceOfficers) 
	{
		this.numberPoliceOfficers = numberPoliceOfficers;
	}

	public int getSalaryExpenditure() {
		return salaryExpenditure;
	}

	public void setSalaryExpenditure(int salaryExpenditure) {
		this.salaryExpenditure = salaryExpenditure;
	}
}
