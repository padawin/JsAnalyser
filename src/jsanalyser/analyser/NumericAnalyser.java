package jsanalyser.analyser;

import java.util.Map;

public class NumericAnalyser extends Analyser
{
	public NumericAnalyser(Map<String, Integer> elements)
	{
		super();
		this.elements = elements;
		this.reportTitle = "Numerical values:";
	}
}
