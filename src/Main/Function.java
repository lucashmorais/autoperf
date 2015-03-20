package Main;
import java.util.*;


public class Function implements Comparable<Function>, Quantifiable, SetOfLines{
	private String name;
	private int start = 0, end = 0;
	private ArrayList<Loop> loops = new ArrayList<Loop>();
	private HashMap<Integer, Double> loopMap = new HashMap<Integer, Double>();
	private HashMap<Integer, Line> lineMap = new HashMap<Integer, Line>();
	private boolean alreadySorted = false;
	private double execPercInRelationToTotal;
	private List<String> codeContent = null;
	private List<String> auxCodeContent; 
	
	public Function (String name)
	{
		this.name = name;
	}
	
	public Function ()
	{
	}
	
	public void addLoop(Loop l)
	{
		loops.add(l);
		alreadySorted = false;
	}
	
	public void sortLoops()
	{
		Collections.sort(loops);
		alreadySorted = true;
	}
	
	public boolean startLineIsSet()
	{
		return start != 0;
	}
	
	public boolean endLineIsSet()
	{
		return end != 0;
	}
	
	/**
	 * Função de adição de overhead à função.
	 * Informações das linhas são guardadas em uma ArrayList
	 * @param line
	 * @param overhead
	 */
	public void addOverhead(int line, double overhead)
	{
		Line newLine = new Line(line, overhead);
		
		if (!alreadySorted)
			sortLoops();
		
		lineMap.put(line, newLine);
		loopMap.put(line, overhead);
		
		for (Loop l : loops)
		{
			if (l.getStart() <= line)
				if (l.getEnd() >= line)
					l.addOverhead(overhead);
			else
				break;
		}
	}
	
	public int getStart()
	{
		return start;
	}
	
	public int getEnd()
	{
		return end;
	}
	
	public void setStart(int newStart)
	{
		this.start = newStart;
	}
	
	public void setEnd(int newEnd)
	{
		this.end = newEnd;
	}
	
	public String getName()
	{
		return name;
	}
	
	public void setName(String newName)
	{
		name = newName;
	}

	@Override
	public int compareTo(Function arg0) {
		return getStart() - arg0.getStart();
	}

	@Override
	public double getExecPercentage() {		
		return execPercInRelationToTotal;
	}

	@Override
	public double getExecTime() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void setExecPercentage(double overhead) {
		this.execPercInRelationToTotal = overhead;
	}
	
	@Override
	public String toString()
	{
		return this.getName();
	}
	
	public HashMap<Integer, Double> getExecPercMap()
	{
		HashMap<Integer, Double> execMap = new HashMap<Integer, Double>();
		
		for (int i: lineMap.keySet())
		{
			execMap.put(i, lineMap.get(i).getExecPercentage());
		}
		
		return execMap;
	}

	public List<String> getCodeContent() {
		return codeContent;
	}
	
	public List<String> getAuxCodeContent() {
		return auxCodeContent;
	}

	public void setCodeContent(List<String> codeContent) {
		this.codeContent = codeContent;
	}
	
	public void setAuxCodeContent(List<String> subList) {
		this.auxCodeContent = subList;		
	}

	public void printContent() {
		for (String s: this.codeContent)
		{
			System.out.println(s);
		}
	}

	public boolean codeContentIsAvailable() {
		return this.codeContent != null;
	}	
}
