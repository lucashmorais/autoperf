package Test;

import java.io.IOException;
import java.util.LinkedList;

public class TestProcessBuilder {

	private static LinkedList<String> list = new LinkedList<String>();
	private static ProcessBuilder process;
	
	public static void proc (String command)
	{
		Process aliveProc;
		String[] args;
		
		args = command.split("\\s");
		
		for (String s: args)
		{
			list.add(s);
		}
		
		process = new ProcessBuilder(list);
		process.inheritIO();
		
		try {
			aliveProc = process.start();
			try {
				aliveProc.waitFor();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		proc("ping www.google.com");
	}

}
