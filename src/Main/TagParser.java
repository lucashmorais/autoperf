package Main;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

public class TagParser {
	private String tagFilePath;
	private BufferedReader tagReader;
	private ArrayList<String> tagVec;
	private Source packedSource = null;
	
	public TagParser (String pathToFile) throws IOException
	{
		tagReader = new BufferedReader(new FileReader(pathToFile));
	}
	
	public TagParser (Source s) throws IOException
	{
		packedSource = s;
		tagReader = new BufferedReader(new FileReader(s.getPath() + "tags"));
	}
	
	@SuppressWarnings("unused")
	@Deprecated
	private void fillTagArray() throws IOException {
		String currentLine;
		tagVec = new ArrayList<String>();
		
		try {
			while ((currentLine = tagReader.readLine()) != null)
				tagVec.add(currentLine);
		} catch (FileNotFoundException e) {
			System.err.println("Tag file could not be loaded.");
			throw new FileNotFoundException("Tag file could not be loaded.");
		} catch (IOException e) {
			System.err.println("Could not read a new tag line.");
			throw new IOException("Could not read a new tag line.");
		}
	}

	public TagParser (File f) throws IOException
	{
		tagReader = new BufferedReader(new FileReader(f));
	}
	
	public void execute (Map<String, Source> sourceMap) throws IOException
	{
		String[] elements;
		String currentLine;
		
		while (true)
		{
			currentLine = tagReader.readLine();
			if (currentLine == null)
				break;
			if (!currentLine.startsWith("!"))
			{
				System.out.println("Tag line read: " + currentLine);
				elements = currentLine.split("\\s");
				if (elements[3] == "f")
				{
					sourceMap.get(elements[1]).getFunction(elements[0]).setStart(Integer.parseInt(elements[2]));
					System.out.println("Function " + sourceMap.get(elements[1]).getFunction(elements[0]).getName() + " starts at line " + sourceMap.get(elements[1]).getFunction(elements[0]).getStart());
				}
			}
		}
	}
	
	public void close () throws IOException
	{
		tagReader.close();
	}
	
	public void execute () throws IOException
	{
		String[] elements;
		String currentLine;
		
		if (packedSource == null)
		{
			System.err.println("Source is not set for tag parsing.");
			throw new IOException();
		}
		
		if (Constants.verboseExecution) System.out.println("Reading tag file on " + packedSource.getPath() + "tags");
		
		while (true)
		{
			currentLine = tagReader.readLine();
			if (currentLine == null)
				break;
			if (!currentLine.startsWith("!"))
			{
				elements = currentLine.split("[\\t ]");
				if (Constants.superVerboseExecution) System.out.println("Analysing element: " + elements[0]);
				if (elements[3].contains("f"))
				{
					if (Constants.superVerboseExecution) System.out.println("Found a function definition.");
					if (packedSource.hasFunction(elements[0]))
					{
						if (Constants.superVerboseExecution)	System.out.println("Found function is contained on source.");
						packedSource.getFunction(elements[0]).setStart(Integer.parseInt(elements[2].replaceFirst("([0-9]*).*", "$1")));
						if (Constants.verboseExecution)	System.out.println("Function " + elements[0] + " starts at line " + packedSource.getFunction(elements[0]).getStart());
					}
				}
			}
		}
	}	

	public String getTagFilePath() {
		return tagFilePath;
	}

	public void setTagFilePath(String tagFilePath) {
		this.tagFilePath = tagFilePath;
	}
}
