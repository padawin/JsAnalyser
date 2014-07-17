package jsanalyser.analyser;

import java.util.Map;

public class TokenAnalyser extends Analyser
{
	public TokenAnalyser(Map<String, Integer> elements)
	{
		super();
		this.elements = elements;
		this.reportTitle = "Tokens:";
	}
}
