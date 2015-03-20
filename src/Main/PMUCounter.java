package Main;

public class PMUCounter {
	private String name;
	
	public PMUCounter (String name)
	{
		this.name = name;
	}
	
	public String toString()
	{
		return name;
	}
	
	public String getName()
	{
		return this.toString();
	}
}
