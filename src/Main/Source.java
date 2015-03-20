package Main;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;


public class Source implements Quantifiable, SetOfLines {
	private String name, path, pathToFile;
	private ArrayList<Function> functions = new ArrayList<Function>();
	private boolean alreadySorted = false; 
	private boolean codeIsAvailable = true;
	
	public Source (String name)
	{
		this.name = name;
	}
	
	public Source (String name, String path)
	{
		this.name = name;
		this.path = path;
		this.pathToFile = path + name;
	}
	
	public boolean isTheCodeAvailable()
	{
		return codeIsAvailable;
	}
	
	public void makeTheCodeAvailable()
	{
		codeIsAvailable = true;
	}
	
	public void makeTheCodeUnavailable()
	{
		codeIsAvailable = false;
	}
	
	public String getName ()
	{
		return name;
	}
	
	public String getPath ()
	{
		return path;
	}
	
	public String getPathToFile ()
	{
		return pathToFile;
	}
	
	public void addFunction(Function f)
	{
		functions.add(f);
		alreadySorted = false;
	}
	
	public void sortFunctions()
	{
		Collections.sort(functions);
		alreadySorted = true;
	}
	
	@Deprecated
	public void addOverhead(int line, double overhead)
	{
		if (!alreadySorted)
			sortFunctions();
		
		for (Function f : functions)
		{
			if (f.getStart() <= line)
			{
				if (f.getEnd() >= line)
				{
					f.addOverhead(line, overhead);
					break;
				}
			}
			else
				break;
					
		}
	}
	
	public void addLoop(Loop loop, int line)
	{
		if (!alreadySorted)
			sortFunctions();
		
		for (Function f : functions)
		{
			if (f.getStart() <= line)
			{
				if (f.getEnd() >= line)
				{
					f.addLoop(loop);
					break;
				}
			}
			else
				break;
		}		
	}
	
	public void addLoop(Loop loop, String targetFunction)
	{
		for (Function f : functions)
			if (f.getName() == targetFunction)
			{
				f.addLoop(loop);
				break;
			}		
	}

	public void setPath(String sourcePath) {
		this.path = sourcePath;
		this.pathToFile = sourcePath + name;	
	}

	public void simplePrint() {
		Function f;
		System.out.println(" Source name:  " + this.getName());
		System.out.printf(" Total Ex.P.:  %.2f%%\n", this.getExecPercentage());
		System.out.println();
		
		System.out.println("\tFunctions\t\t\t\tExecution Time");
		for (int i = 0; i < functions.size(); i++)
		{
			f = functions.get(i);
			System.out.printf("\t%3d. %-30s\t%5.1f%%\n", i, f.getName(), f.getExecPercentage());
		}
		
		System.out.println("\n");
	}
	
	public boolean hasFunction(Function f)
	{
		return functions.contains(f);
	}
	
	public boolean hasFunction(String name)
	{
		for (Function f: functions)
		{
			if (f.getName().equals(name))
				return true;
		}
		return false;
	}
	
	public boolean setFunctionExecPerc(String name, double overhead)
	{
		for (Function f: functions)
		{
			if (f.getName().equalsIgnoreCase(name))
			{
				f.setExecPercentage(overhead);
				return true;
			}
		}
		return false;
	}

	@Override
	public double getExecPercentage() {
		double aux = 0;
		
		for (Function f: functions)
			aux += f.getExecPercentage();
		
		return aux;
	}

	@Override
	public double getExecTime() {
		double aux = 0;
		
		for (Function f: functions)
			aux += f.getExecTime();
		
		return aux;
	}
	
	public Function getFunction(String functionName)
	{
		for (Function f: functions)
		{
			if (f.getName().equals(functionName))
				return f;
		}
		return null;
	}

	@Override
	public HashMap<Integer, Double> getExecPercMap() {
		HashMap<Integer, Double> execMap = new HashMap<Integer, Double>();
		
		for (Function f: functions)
		{
			execMap.putAll(f.getExecPercMap());
		}
		return execMap;
	}
	
	public ArrayList<Function> getFunctions()
	{
		return functions;
	}

	public void verbosePrint() {
		for (Function f: functions)
		{			
			if (f.codeContentIsAvailable())
			{
				System.out.println("\n\n\n\nCode snippet of function " + f.getName());
				f.printContent();
			}
		}
	}
	
	public String toString()
	{
		return this.getName();
	}
}
