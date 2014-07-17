package jsanalyser.analyser;

import java.util.Map;

public class StringAnalyser extends Analyser
{
	public StringAnalyser(Map<String, Integer> elements)
	{
		super();
		this.elements = elements;
		this.reportTitle = "Strings:";
	}
}
