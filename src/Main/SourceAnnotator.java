package Main;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import Parser.CLexer;
import Parser.CParser;
import Parser.FunctionListener;

/**
 * Classe que deve conter funções de anotação automatizada de código
 */
public class SourceAnnotator{
	private Source packedSource;
	private LinkedList<String> tempFile = null;
	private String stringTempFile = null;
	private ArrayList<Token> blockComments = null;
	private CommonTokenStream tokens;
	
	public SourceAnnotator(Source s)
	{
		this.packedSource = s;
		readFile();
	}
	
	public Source getSource()
	{
		return packedSource;
	}
	
	public void setSource(Source newSource)
	{		
		this.packedSource = newSource;
		readFile();
	}
	
	public LinkedList<String> getAnnotatedSource()
	{
		return tempFile;		
	}
	
	/**
	 * Função de apêndice de texto a certa linha de código
	 * @param appendix Texto a ser adicionado
	 * @param lineNumber Linha de texto a sofrer a alteração
	 */
	public void appendToLine(String appendix, int lineNumber)
	{	
		String modifiedLine;
		modifiedLine = tempFile.get(lineNumber).concat(appendix);
		
		replaceLine(modifiedLine, lineNumber);		
	}
	
	private void readFile()
	{
		String currentLine;
		LineNumberReader reader = null;
		tempFile = new LinkedList<String>();
		StringBuilder builder = new StringBuilder();
		
		try{
			System.out.println("File to be annotated: " + packedSource.getPathToFile());			
			reader = new LineNumberReader(new FileReader(packedSource.getPathToFile()));
			
			tempFile.add(0, null);
			
			while((currentLine = reader.readLine()) != null)
			{
				tempFile.add(reader.getLineNumber(), currentLine);
				if (!containsUnrecoverableMacro(currentLine))
					builder.append(currentLine + "\n");
				else
					builder.append("\n");
			}
			
			//Cria a imagem de source compatível com parsing
			stringTempFile = builder.toString();	
			reader.close();	
		}
		catch (FileNotFoundException e)
		{
			System.err.println("Could not find source file of " + packedSource.getName() + " on path " + packedSource.getPath());
		}
		catch (IOException e)
		{
			System.err.println("Error during source file reading: aborting source editing.");
		}
	}
	
	private boolean containsUnrecoverableMacro (String currentLine)
	{
		return currentLine.toLowerCase().matches("[^#]*#[ ]*define.*") ||
			   currentLine.toLowerCase().matches("[^#]*#[ ]*ifdef.*") ||
			   currentLine.toLowerCase().matches("[^#]*#[ ]*if.*") ||
			   currentLine.toLowerCase().matches("[^#]*#[ ]*ifndef.*") ||
			   currentLine.toLowerCase().matches("[^#]*#[ ]*endif.*") ||
			   currentLine.toLowerCase().matches("[^#]*#[ ]*elseif.*") ||
			   currentLine.toLowerCase().matches("[^#]*#[ ]*else.*") ||
			   currentLine.toLowerCase().matches("[^#]*#[ ]*include.*") ||
			   currentLine.toLowerCase().matches("[^#]*#[ ]*pragma.*");
	}
	
	/**
	 * Função de substituição de linha de código
	 * @param replacement Nova linha
	 * @param lineNumber Número da linha a ser alterada
	 */
	public void replaceLine (String replacement, int lineNumber)
	{
		tempFile.set(lineNumber, replacement);
	}
	
	/**
	 * Função que executa as modificações pendentes.
	 * Não há outra maneira de se escrever sobre o arquivo
	 * fonte senão por esta função.
	 */
	public void commitEdition ()
	{
		PrintWriter printer;
		
		try {
			printer = new PrintWriter(new FileWriter(packedSource.getPathToFile()));
			
			for (String s: tempFile)
			{
				if (s != null)
					printer.print(s + "\n");
			}
			
			printer.close();
		} catch (IOException e) {
			System.err.println("Could not modify source file.");
		}		
	}
	
	public void appendExecutionTime ()
	{
		HashMap<Integer, Double> map = packedSource.getExecPercMap();
		
		for (int lineNumber: map.keySet())
		{
			appendToLine(String.format("\t\t%2.2f%", map.get(lineNumber)), lineNumber);
		}
		
		//Atualiza conteúdo das funções
		findAndSetAllFunctionContents();
	}
	
	public void prependExecutionTime ()
	{
		HashMap<Integer, Double> map = packedSource.getExecPercMap();
		
		for (int i = 1; i < tempFile.size(); i++)
		{
			if (map.containsKey(i))
			{
				if (!belongsToCommentContext(i))
					replaceLine(String.format("/*%2.2f%%*/\t", map.get(i)) + tempFile.get(i), i);
				else
					replaceLine(String.format("/-%2.2f%%-/\t", map.get(i)) + tempFile.get(i), i);
			}
			else
			{
				replaceLine("\t\t" + tempFile.get(i), i);
			}
		}
	}
	
	public void findAndSetAllFunctionContents ()
	{
		for (Function f: packedSource.getFunctions())
		{
			try {
				findAndSetFunctionContent(f);
			} catch (Exception e) {
				//Desconsidera-se o erro
			}
		}
	}
	
	public void findAndSetAllAuxiliaryFunctionContents ()
	{
		for (Function f: packedSource.getFunctions())
		{
			try {
				findAndSetAuxFunctionContent(f);
			} catch (Exception e) {
				//Desconsidera-se o erro
			}
		}
	}

	private void findAndSetAuxFunctionContent(Function f) throws Exception {
		if (!f.startLineIsSet())
		{
			System.err.println("Start line of " + f.getName() + " is not set. Aborting code recognition.");
			throw new Exception();
		}
		if (!f.endLineIsSet())
		{
			System.err.println("End line of " + f.getName() + " is not set. Aborting code recognition.");
			throw new Exception();
		}
		
		f.setAuxCodeContent(tempFile.subList(f.getStart(), f.getEnd() + 1));		
	}

	private void findAndSetFunctionContent(Function f) throws Exception {
		if (!f.startLineIsSet())
		{
			System.err.println("Start line of " + f.getName() + " is not set. Aborting code recognition.");
			throw new Exception();
		}
		if (!f.endLineIsSet())
		{
			System.err.println("End line of " + f.getName() + " is not set. Aborting code recognition.");
			throw new Exception();
		}
		
		f.setCodeContent(tempFile.subList(f.getStart(), f.getEnd() + 1));
	}
	
	/*
	 * Função que testa se os blockComments estão sendo corretamente armazenados.
	 */
	@SuppressWarnings("unused")
	private void printAllBlockComments ()
	{
		for (Token t: blockComments)
		{
			System.out.println(t.getText());
		}
	}
	
	public void parseSource () throws IOException
	{		
		if (!packedSource.isTheCodeAvailable())
			throw new IOException();
		
		ANTLRInputStream stream = new ANTLRInputStream(stringTempFile);
		CLexer lexer = new CLexer(stream);
		tokens = new CommonTokenStream(lexer);
		
		/* Trecho que armazena todos os blockComments em ArrayList apropriada */
		blockComments = (ArrayList<Token>) tokens.getHiddenTokensToRight(0);
		
		if (blockComments != null && blockComments.size() > 0)
			Collections.sort(blockComments, new TokenComparator());
		
		CParser parser = new CParser(tokens);
		ParseTree tree = parser.compilationUnit();
		ParseTreeWalker walker = new ParseTreeWalker();
		FunctionListener listener = new FunctionListener(parser, packedSource);
		walker.walk(listener, tree);
	}
	
	/**
	 * Dado certo token t, esta função retorna a linha de início do
	 * token seguinte.
	 * @param t Token a ser analisado
	 * @return Linha do token seguinte
	 */
	private int stopLine (Token t)
	{
		int index = t.getTokenIndex();
		Token nextToken = tokens.get(index + 1);
		
		return nextToken.getLine();		
	}
	
	private boolean belongsToCommentContext(int lineNumber)
	{
		int start, end;
		Token t;
		
		if (blockComments == null)
			return false;
		
		for (int i = 0; i < blockComments.size(); i++)
		{
			t = blockComments.get(i);
			start = t.getLine();
			end = stopLine(t);
				
			if (start <= lineNumber && end >= lineNumber)
				return true;
			else if (start > lineNumber)
				break;
		}
		
		return false;
	}
	
	public class TokenComparator implements Comparator <Token>
	{
		public int compare(Token arg0, Token arg1) {
			return arg0.getStartIndex() - arg1.getStartIndex();
		}		
	}
}
