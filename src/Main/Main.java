package Main;
import java.io.BufferedReader;
import java.util.regex.*;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

@SuppressWarnings("unused")
public class Main {
	private static HashMap<String, Source> hash = new HashMap<String, Source>();
	private static int undefCounter = 0;
	private static ArrayList <String> header = new ArrayList<String>();
	
	public static void main (String[] args) throws IOException
	{
		String tempAnnotateDump = Constants.tempAnnotateName, tempReportDump = Constants.tempReportName;
		BufferedReader br;
		
		for (int i = 0; i < args.length; i++)
		{
			if (args[i].contentEquals("-i") || args[i].contentEquals("--input"))
			{
				if (i + 1 < args.length)
					Constants.perfData = args[i + 1];
			}
			else if (args[i].contentEquals("-o") || args[i].contentEquals("--output"))
			{
				if (i + 1 < args.length)
				{
					Constants.reportHTMLPath = args[i + 1];
					Constants.HTMLNameIsSet = true;
				}
			}
			else if (args[i].contentEquals("-v") || args[i].contentEquals("--verbose"))
			{
				Constants.verboseExecution = true;
				Constants.superVerboseExecution = false;
			}
			else if (args[i].contentEquals("-a") || args[i].contentEquals("--annotate"))
			{
				Constants.annotateSource = true;
			}
			else if (args[i].contentEquals("-c") || args[i].contentEquals("--commit"))
			{
				Constants.commitAnnotation = true;
			}
			else if (args[i].contentEquals("-vv") || args[i].contentEquals("--Verbose"))
			{
				Constants.verboseExecution = true;
				Constants.superVerboseExecution = true;
			}
			else if (args[i].contentEquals("-r") || args[i].contentEquals("--report"))
			{
				Constants.writeReport = true;
			}
			else if (args[i].contentEquals("--no-report"))
			{
				Constants.writeReport = false;
			}
			else if (args[i].contentEquals("--no-annotation"))
			{
				Constants.annotateSource = false;
			}
			else if (args[i].contentEquals("--no-committing"))
			{
				Constants.commitAnnotation = false;
			}
		}
		
		//Trecho de dump dos arquivos temporários do perf
		PerfHandler perfHandler = new PerfHandler(Constants.perfData);
		perfHandler.callPerf();
		
		//Chamada do interpretador de dumps
		parseDumps();
		
		/**
		 * Trecho de invocação de CTAGS
		 */
		TagController tagControl;
		try {
			tagControl = new TagController(hash);
			tagControl.generateTags();		
			tagControl.parseTags();
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
			throw new RuntimeException();
		}
		
		//Chamada de tratamento de sources
		sourceMaster();
		
		if (Constants.writeReport)
		{
			Reporter reporter = new Reporter(header, hash);
			reporter.printReport();
		}
		
		//Chamada de impressão detalhada a respeito de funções
//		detailedPrint();
	}
	
	private static void parseDumps()
	{
		BufferedReader br;
		
		/**
		 * Trecho de interpretação do perf-annotate.
		 * Daqui extrai-se informação do tempo de execução devido
		 * a cada linha de código.
		 **/
		try
		{
			br = new BufferedReader(new FileReader(Constants.tempAnnotateName));
			readFunctions(br);
			br.close();
		}
		catch (IOException e1)
		{
			System.err.println("Entrada inválida.");
			return;
		}
		
		/***
		 * Trecho de interpretação do perf-report.
		 * Daqui o percentual de tempo de execução de todo o
		 * programa devido a cada função sua.
		 */
		try
		{
			br = new BufferedReader(new FileReader(Constants.tempReportName));
			readReport(br);
			br.close();
		}
		catch (IOException e)
		{
			System.err.println("Erro de manipulação de arquivo: " + e.getMessage());
		}
	}
	
	/**
	 * Função de teste do parsing dos dumps do perf.
	 * 
	 * Deve ser capaz de povoar "hash" e incluir em seus sources
	 * todas as funções contidas por cada arquivo.
	 * 
	 * Ao final, deve imprimir as informações obtidas.
	 */
	private static void testSimpleParse ()
	{
		//Parsing dos dumps
		parseDumps();
		
		//Impressão das informações obtidas
		simplifiedPrint();
	}

	private static void readReport(BufferedReader br) throws IOException {			
		Constants.usedCounter = readReportHeader(br);
		while (getFunctionOverhead(br));
	}

	private static PMUCounter readReportHeader(BufferedReader br) throws IOException {
		String currentLine = null;
		PMUCounter foundCounter = null;
		int markNumber = 0;
		
		do
		{
			br.mark(150);
			currentLine = br.readLine();
			
			if (currentLine.matches("\\S*\\s*Samples:\\s*\\S*\\s*of\\S*\\s*event\\s*['][^']*['].*"))
			{
				foundCounter = new PMUCounter(currentLine.replaceFirst("\\S*\\s*Samples:\\s*\\S*\\s*of\\S*\\s*event\\s*[']([^']*)['].*", "$1"));
			}
			if (currentLine.matches(".*\\[.\\]\\s*(\\S*).*"))
			{
				br.reset();				
				return foundCounter;
			}
			else
			{
				if (currentLine.contains("========"))
					markNumber++;
				else if (markNumber < 2 && markNumber > 0 && !currentLine.contains("to display"))
					header.add(currentLine);
			}
			
		} while (currentLine != null);
		
		throw new RuntimeException("Report ended before leaving the header.");
	}

	private static void printHeader()
	{
		for (String s: header)
		{
			System.out.println(s);
		}
	}
	
	private static boolean getFunctionOverhead(BufferedReader br) throws IOException {
		String currentLine = br.readLine();
		String functionName;
		String functionMark;
		double execPerc;
		
		if (currentLine == null)
			return false;
		else if (!currentLine.matches(".*\\[.\\]\\s*(\\S*).*"))
			return false;
		
		execPerc = Double.parseDouble((currentLine.replaceAll("\\s*(\\d*[.]\\d{2})[%].*", "$1")));
		functionName = currentLine.replaceAll(".*\\[.\\]\\s*(\\S*).*", "$1");
		functionMark = currentLine.replaceAll(".*\\[(.)\\].*", "$1");
		
		for (Source s: hash.values())
		{
			if (s.setFunctionExecPerc(functionName, execPerc))
				break;
		}
		
		return true;
	}

	private static void readFunctions(BufferedReader br) throws IOException {
		Function currentFunction;
		
		while (true)
		{
			if ((currentFunction = readHeader(br)) == null)
				break;				
			
			if (searchAndSetName(br, currentFunction) == false)
			{
				System.err.println("Nome de função não encontrado!");
				throw new IOException("Nome de função não encontrado!");	
			}
		}
	}

	private static boolean searchAndSetName(BufferedReader br, Function currentFunction) throws IOException {
		String currentLine;
		
		while (true)
		{
			currentLine = br.readLine();
			if (currentLine == null)
				return false;
			if (currentLine.matches(".*[<](.+)[>].*"))
			{
				currentFunction.setName(currentLine.replaceAll(".*[<](.+)[>].*", "$1"));
				break;
			}				
		}
		return true;
	}
	
	private static void simplifiedPrint()
	{
		printHeader();
		System.out.println(" autoPerf report.\n");
		
		for (Source s: hash.values())
		{
			if (!s.getName().contains("UNDEF"))
				s.simplePrint();
		}			
	}	

	private static void detailedPrint()
	{
		for (Source s: hash.values())
			s.verbosePrint();
	}
		
	private static void sourceMaster()
	{
		SourceAnnotator annotator = null;
		System.out.println("Source annotation under course...");
		
		for (Source s: hash.values())
		{
			if (s.getPath() == null || s.getPath().contains(new String("?")))
				continue;
						
			try
			{
				annotator = new SourceAnnotator(s);
				annotator.parseSource();
				annotator.findAndSetAllFunctionContents();
				
				if (Constants.annotateSource) annotator.prependExecutionTime();
				if (Constants.commitAnnotation) annotator.commitEdition();
			
			} catch (IOException e)
			{
				System.out.println(s.getName() + " source code could not be annotated.");
			}

		}
		
		System.out.println("Source annotation is complete.");
	}
	
	private static Function readHeader(BufferedReader br) throws IOException
	{
		double overhead;
		String sourceName, sourcePath = null, overheadString, lineString;
		Source source = null;
		int lineNumber;
		String currentLine;
		Pattern[] p = new Pattern[6];
		Matcher[] m = new Matcher[6];
		Function currentFunction = new Function();
		
		p[0] = Pattern.compile("Sorted summary for file");				//Procura header
		p[1] = Pattern.compile(" +([0-9]+[.][0-9]+).*");				//Procura overhead
		p[2] = Pattern.compile("[ ]+.+[ ]+.+/(.+):.*");					//Procura nome de arquivo
		p[3] = Pattern.compile("[ ]+.+[ ]+(/(.+/)?).*");				//Procura source path
		p[4] = Pattern.compile(".+:([0-9]+).*");						//Procura número de linha
		p[5] = Pattern.compile("[?]");
		
		while (true)
		{
			currentLine = br.readLine();
			if (currentLine == null)
				return null;
			m[0] = p[0].matcher(currentLine);
			if (m[0].lookingAt())
				break;
				
		}

		while (!currentLine.matches(".+:([0-9]+).*") && currentLine != null)
			currentLine = br.readLine();
		
		while (true)
		{			
			if (currentLine == null)
				return null;
			if (currentLine.contains("Source code & Disassembly"))
				break;
			
			for (int i = 0; i < p.length; i++)
				m[i] = p[i].matcher(currentLine);			
			
			if (!m[5].find())
				sourceName = m[2].replaceAll("$1");
			else
				sourceName = String.format("UNDEF #%d", undefCounter++);
			
			sourcePath = m[3].replaceAll("$1");
			lineString = m[4].replaceAll("$1");
			overheadString = m[1].replaceAll("$1");	
			
			overhead = Double.parseDouble(overheadString);
			lineNumber = Integer.parseInt(lineString);	
			
			currentFunction.addOverhead(lineNumber, overhead);
			
			//Cria o source caso necessário
			if (sourceName != null && !hash.containsKey(sourceName))
			{
				hash.put(sourceName, source = new Source(sourceName));
			}				
			else
				source = hash.get(sourceName);			
			
			currentLine = br.readLine();
		}
		source.setPath(sourcePath);
		source.addFunction(currentFunction);
		
		return currentFunction;		
	}	
}