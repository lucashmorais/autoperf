package Parser;

import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTree;

import Main.Constants;
import Main.Function;
import Main.Source;


public class FunctionListener extends CBaseListener  {
	private Source packedSource;
	@SuppressWarnings("unused")
	private CParser parser;
	
	public FunctionListener(CParser parser, Source s) {
		this.parser = parser;
		packedSource = s;
	}

	/**
	 * Função que determina os números da primeira e última linha da função, bem como
	 * seu nome.
	 */
	@Override
	public void exitFunctionDefinition(@NotNull CParser.FunctionDefinitionContext ctx) {
		String functionName = null;
		Function foundFunction;
		
		if (Constants.verboseExecution) System.out.println("Entering function listener. S: " + ctx.getStart().getLine() + " E: " + ctx.getStop().getLine());
		
		
		for (int i = 0; i < ctx.getChildCount(); i++)
		{
			if (Constants.verboseExecution) System.out.println("Object " + ctx.getChild(i).getClass().getName() + ": " + ctx.getChild(i).getText() + "\n\n\n");
			if (ctx.getChild(i) instanceof CParser.DeclaratorContext)
			{
				if (Constants.verboseExecution) System.out.println("Got identifier.");
				functionName = getIdentifier(ctx.getChild(i));
				if (Constants.verboseExecution) System.out.println("Function identifier: '" + functionName + "'");
				break;
			}
		}
		
		if (functionName == null)
		{
			System.err.println("Could not find the function identifier.");
		}
		
		foundFunction = packedSource.getFunction(functionName);
		
		
		if (foundFunction == null)
		{
			for (Function f: packedSource.getFunctions())
			{
				if (f.getStart() == ctx.getStart().getLine())
				{
					System.out.println("Encontrou!");
					foundFunction = f;
					break;
				}
			}
		}
		
		if (foundFunction != null)
		{
			if (Constants.verboseExecution)	System.out.println("Function is beeing worked on.");
			
			foundFunction.setStart(ctx.getStart().getLine());
			foundFunction.setEnd(ctx.getStop().getLine());
		}
		else
			if (Constants.verboseExecution) System.out.println("Function '" + functionName + "' could not be found!");
	}
	
	private String getIdentifier(ParseTree parseTree)
	{
		if (parseTree instanceof CParser.DirectDeclaratorContext && parseTree.getChildCount() == 1)
			return parseTree.getText();
		else
		{
			if (parseTree.getChild(0) instanceof CParser.DirectDeclaratorContext)
				return getIdentifier(parseTree.getChild(0));
			else
				return getIdentifier(parseTree.getChild(1));
		}
	}
}
