
public class Intersection 
{
	private int id;
	private String firstStreet;
	private String secondStreet;
	private String borough;
	
	public Intersection(int i, String fs, String ss, String boro)
	{
		this.id = i;
		this.firstStreet = fs;
		this.secondStreet = ss;
		this.borough = boro;
	}

	public int getId() {
		return id;
	}

	public String getSecondStreet() {
		return secondStreet;
	}


	public String getBorough() {
		return borough;
	}

	public String getFirstStreet() {
		return firstStreet;
	}

}
