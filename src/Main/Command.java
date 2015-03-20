package Main;
import java.io.IOException;
import java.lang.ProcessBuilder;
import java.util.LinkedList;


public class Command {
	private LinkedList<String> list = new LinkedList<String>();
	private ProcessBuilder process;
	
	public Command (String command)
	{
		String[] args;
		
		args = command.split("\\s");
		
		for (String s: args)
		{
			list.add(s);
		}
		
		process = new ProcessBuilder(list);
	}
	
	public String toString()
	{
		StringBuilder s = new StringBuilder();
		
		for (String string: list)
		{
			s.append(string);
		}
		
		return s.toString();
	}
	
	public ProcessBuilder getBuilder ()
	{
		return process;
	}
	
	public Process execute () throws IOException
	{
		process.inheritIO();
		return process.start();
	}
	
	public void changeCommand (String command)
	{
		String[] args;
		
		args = command.split("\\s");
		
		list.clear();
		
		for (String s: args)
		{
			list.add(s);
		}
	}
	
}
