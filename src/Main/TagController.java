package Main;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;


public class TagController {
	
	private HashMap<String, Source> hash;
	
	public TagController (HashMap<String, Source> hash) throws Exception
	{
		if (hash.size() == 0)
		{			
			throw new Exception("Hash is empty!");
		}
		this.hash = hash;
	}
	
	public TagController (Source s)
	{
		this.hash = new HashMap<String, Source>();
		hash.put(s.getName(), s);
	}
	
	public void generateTags()
	{
		ProcessBuilder process;
		Process prc;
		Command cmd = new Command("ctags -R -n");
		int count = 0;
		
		process = cmd.getBuilder();
		
		for (Source s: hash.values())
		{
			String path = s.getPath();
			
			if (path == null || path.contains("?"))
				continue;
			
			File p = new File(path);		
			
			if (!p.canRead())
				continue;
			
			process.directory(p);
			
			try {
				prc = process.start();
				prc.waitFor();
				count++;
			} catch (IOException e) {
				System.err.println("Could not invoke CTAGS in path " + path);
				s.makeTheCodeUnavailable();
			} catch (InterruptedException e) {
				System.err.println("Could not wait for CTAGS execution");
				e.printStackTrace();
			}
		}
		
		if (Constants.verboseExecution) System.out.println("Successfully called ctags " + count + " time" + (count > 1 ? "s" : "") + ".");	
	}
	
	public void parseTags()
	{
		TagParser parser;
		
		for (Source s: hash.values())
		{
			try
			{
				if (s.getPath() != null && !s.getPath().contains(new String("?")))
				{
					System.out.println("Parsing tags of " + s.getName());
					parser = new TagParser(s);
					parser.execute();
					parser.close();
				}
			}
			catch (IOException e)
			{
				System.err.println("Could not parse tagFile of " + s.getName());
			}			
		}
	}
}
