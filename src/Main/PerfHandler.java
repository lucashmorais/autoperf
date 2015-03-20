package Main;

import java.io.File;
import java.io.IOException;

public class PerfHandler {
	private String dataPath;
	
	public PerfHandler (String dataPath)
	{
		this.setDataPath(dataPath);
	}	
	
	/**
	 * Função de chamada de perf annotate e perf report. Redirecionam-se as saídas
	 * para arquivos temporários que deverão ser lidos e interpretados pelo programa.
	 * @param dataPath Caminho até o perfdata por ser analisado.
	 */
	public void callPerf()
	{
		ProcessBuilder annotateBuilder, reportBuilder;
		Process annotateProcess, reportProcess;	
		
		System.out.println("Calling perf now...");
		
		/**
		 * Trecho de instanciação dos comandos perf e annotate
		 * Notar que o maior que não funciona!
		 */
		Command cmdAnnotate = new Command("perf annotate --stdio -l -i " + dataPath);
		Command cmdReport = new Command("perf report --stdio -i " + dataPath);
		
		/**
		 * Trecho de obtenção dos process builders, os quais
		 * serão necessários para o redirecionamento de saída
		 * para os arquivos temporários.
		 */
		annotateBuilder = cmdAnnotate.getBuilder();
		reportBuilder = cmdReport.getBuilder();
		
		/**
		 * Trecho de redirecionamento de saída
		 */
		annotateBuilder.redirectOutput(new File(Constants.tempAnnotateName));
		reportBuilder.redirectOutput(new File(Constants.tempReportName));
		
		/**
		 * Trecho de execução e espera das funções do perf.
		 */		
		try
		{
			annotateProcess = annotateBuilder.start();
			annotateProcess.waitFor();
		}
		catch (IOException e)
		{
			System.err.println("Could not run perf annotate on given data file. Aborting immediately.");
		}
		catch (InterruptedException e)
		{
			System.err.println("Perf annotate was interrupted unexpectedly. Aborting now.");
		}
		try
		{	
			reportProcess = reportBuilder.start();
			reportProcess.waitFor();
		}
		catch (IOException e)
		{
			System.err.println("Could not run perf report on given data file. Aborting immediately.");
		}
		catch (InterruptedException e)
		{
			System.err.println("Perf report was interrupted unexpectedly. Aborting now.");
		}
		
		System.out.println("Perf finished running.");
	}

	public void setDataPath(String dataPath) {
		this.dataPath = dataPath;
	}
	
	/**
	 * Função de teste da classe.
	 */
	public static void main (String[] args)
	{
		PerfHandler perfHandler = new PerfHandler("./parseTest.data");
		perfHandler.callPerf();
	}
}
