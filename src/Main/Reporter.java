package Main;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.TreeSet;

public class Reporter {
	private BufferedReader reader;
	private PrintWriter writer;
	private HashMap<Integer, Integer> marks;
	private LinkedList<String> template;
	private ArrayList<String> header;
	private ArrayList<ArrayList<String>> sections;
	private HashMap<String, Source> NameToSources;
	private TreeSet<Function> functionSet;
	
	public Reporter (ArrayList<String> header, HashMap<String, Source> NameToSources) throws IOException
	{
		template = new LinkedList<String>();
		marks = new HashMap<Integer, Integer>();
		functionSet = new TreeSet<Function>(new FunctionComparator());
		sections = new ArrayList<ArrayList<String>>();
		this.NameToSources = NameToSources;
		this.header = header;
		
		try
		{
			reader = new BufferedReader(new FileReader(Constants.templatePath));
		}
		catch(FileNotFoundException e)
		{
			System.err.println("Default template file could not be found. Aborting HTML generation now.");
			throw new FileNotFoundException();
		}
		
		try
		{
			loadTemplate();
		}
		catch(IOException e)
		{
			System.err.println("Default template could not be loaded to memory. Aborting HTML generation now.");
			throw new IOException();
		}
	}
	
	public void printReport ()
	{
		extractFunctions();
		/*
		 * Chamada de teste
		 */
		System.out.println(functionSet);
		
		//Garante que a função set funcione
		for (int i = 0; i < 5; i++)
			sections.add(null);
		
		fillSectionOne();
		fillSectionTwo();
		fillSectionThree();
		fillSectionFour();
		
		for (int i = 1; i < 5; i++)
		{
			template.remove(marks.get(i).intValue());
			template.addAll(marks.get(i), sections.get(i));
			for (int j = i + 1; j < 5; j++)
				marks.put(j, marks.get(j) + sections.get(i).size() - 1);
		}
		
		try
		{
			if (!Constants.HTMLNameIsSet)
				writer = new PrintWriter(new FileWriter(Constants.reportHTMLPath + Constants.usedCounter + "-report@" + System.currentTimeMillis() + ".html"));
			else
				writer = new PrintWriter(new FileWriter(Constants.reportHTMLPath + ".html"));
		}
		catch (IOException e) {
			System.err.println("Could not open target HTML file. Aborting now.");
			e.printStackTrace();
		}
		
		for (String s: template)
		{
			writer.println(s);
		}
		
		writer.close();
	}
	
	private Collection<? extends String> functionContentHTML(Function f) {
		ArrayList<String> content = new ArrayList<String>();
		String overhead = String.format("%2.2f%%", f.getExecPercentage());
		
		content.add("<div class=\"section\" id=\"raspbian-installation\">");
		content.add("<h2>[BBBBB] AAAAA<a style=\"font-family: arial; font-size: 10px; padding-left: 30px;\" name=\"AAAAA\" href=\"#\" title=\"Go to top of the page\">Top</a></h2>"
				.replaceAll("AAAAA", f.getName())
				.replaceFirst("BBBBB", overhead));
		content.add("<div class=\"highlight-cpp\">");
		content.add("<div class=\"highlight\">");
		content.add("<pre>");
		
		if (f.getAuxCodeContent() != null)
		{
			for (String s: f.getAuxCodeContent())
			{
				content.add(s.replaceAll("[<]", "&lt;").replaceAll("[>]", "&gt;"));
			}
		}
		else if (f.getCodeContent() != null)
		{
			for (String s: f.getCodeContent())
			{
				content.add(s.replaceAll("[<]", "&lt;").replaceAll("[>]", "&gt;"));
			}
		}
		
		
		content.add("</pre>");
		
		for (int i = 0; i < 3; i++)
			content.add("</div>");
		
		return content;
	}
	
	/**
	 * Função de preenchimento do corpo de funções.
	 */
	private void fillSectionFour() {
		ArrayList<String> sectionFour = new ArrayList<String>();
		
		for (Function f: functionSet)
		{
			sectionFour.addAll(functionContentHTML(f));			
		}
		
		sections.set(4, sectionFour);
	}

	/**
	 * Função de preenchimento da lista central de assinaturas.
	 */
	private void fillSectionThree() {
		ArrayList<String> sectionThree = new ArrayList<String>();
		
		for (Function f: functionSet)
		{
			String overhead = String.format("%%%2.2f", f.getExecPercentage());
			sectionThree.add("<li class=\"toctree-l1\"><a class=\"reference internal\" href=\"#AAAAA\">[BBBBB] AAAAA</a></li>".
					replaceAll("AAAAA", f.getName()).replaceFirst("BBBBB", overhead));
		}
		
		sections.set(3, sectionThree);
	}
	
	/**
	 * Função de preenchimento do header de execução do perf.
	 */
	private void fillSectionTwo() {
		ArrayList<String> sectionTwo = header;
		
		sections.set(2, sectionTwo);
	}

	/**
	 * Função de preenchimento da lista lateral de funções.
	 */
	private void fillSectionOne ()
	{
		ArrayList<String> sectionOne = new ArrayList<String>();
		
		for (Function f: functionSet)
		{
			sectionOne.add("<li class=\"toctree-l1\"><a class=\"reference internal\" href=\"#A\">A</a></li>".replaceAll("A", f.getName()));
		}
		
		sections.set(1, sectionOne);
	}
	
	/**
	 * Função de geração da lista ordenada de funções.
	 */
	private void extractFunctions ()
	{
		for (Source s: NameToSources.values())
		{
			functionSet.addAll(s.getFunctions());
		}
	}

	private void loadTemplate() throws IOException {
		String currentLine;		
		
		while (true)
		{
			currentLine = reader.readLine();
			if (currentLine == null)
				break;
			template.add(currentLine);
			
			if (currentLine.matches("[^#]*###SECTION([0-9]*)###[^#]*"))
			{
				marks.put(Integer.parseInt(currentLine.replaceAll("[^#]*###SECTION([0-9]*)###[^#]*", "$1")), template.size() - 1);
			}			
		}
	}	
	
	private class FunctionComparator implements Comparator<Function>
	{
		@Override
		public int compare(Function arg0, Function arg1) {
			double diff = arg0.getExecPercentage() - arg1.getExecPercentage();
			
			if (diff > 0)
				return -1;
			else if (diff < 0)
				return 1;
			else
				return 0;
		}		
	}
	
}
