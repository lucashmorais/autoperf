package Main;

public class Line implements Quantifiable{
	private String content;
	private int number;
	private double execPercentage;
	
	public Line (String content, int number)
	{
		this.content = content;
		this.number = number;
	}
	
	public Line (String content, int number, double execPercentage)
	{
		this.content = content;
		this.number = number;
		this.execPercentage = execPercentage;
	}
	
	public Line (int number, double execPercentage)
	{
		this.content = null;
		this.number = number;
		this.execPercentage = execPercentage;
	}
	
	public void setContent (String newContent)
	{
		this.content = newContent;
	}
	
	public String getContent()
	{
		return this.content;
	}
	
	public int getNumber()
	{
		return this.number;
	}

	@Override
	public double getExecPercentage() {
		return this.execPercentage;
	}

	@Override
	public double getExecTime() {
		// TODO Auto-generated method stub
		return 0;
	}
	
}
