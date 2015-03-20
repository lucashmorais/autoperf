package Main;

public class Loop implements Comparable<Loop>{
	private String name;
	private int start;
	private int end;
	private double overhead = 0;
	private String source = null;
	
	public Loop (String name, int start, int end, String source)
	{
		this.name = name;
		this.start = start;
		this.end = end;
		this.source = source;
	}
	
	public Loop (String name, int start, int end)
	{
		this.name = name;
		this.start = start;
		this.end = end;
	}
	
	public String getName()
	{
		return name;
	}
	
	public boolean containsLine(int line)
	{
		return this.start <= line && line <= this.end;
	}
	
	public int getStart()
	{
		return start;
	}
	
	public int getEnd()
	{
		return end;
	}
	
	public void setOverhead(double overhead)
	{
		this.overhead = overhead;
	}
	
	public void addOverhead(double overhead)
	{
		this.overhead += overhead;
	}
	
	public double getOverhead()
	{
		return overhead;
	}
	
	public String getSource()
	{
		return source;
	}
	
	public void setSource(String source)
	{
		this.source = source;
	}

	@Override
	public int compareTo(Loop other) {
		return getStart() - other.getStart();
	}
}
