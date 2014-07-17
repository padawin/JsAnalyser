package jsanalyser.analyser;

import java.util.Map;

public class NumericAnalyser extends Analyser
{
	public NumericAnalyser()
	{
		super();
		this.reportTitle = "Numerical values:";
	}

	public void specificReport(boolean verbose, String element, Integer nbOccurences)
	{
		int strLength = element.length(),
			relationLenOccurences = strLength * nbOccurences - strLength - nbOccurences;

		if (7 < relationLenOccurences) {
			System.out.println("\tOptimisable");
		}
		else if (3 < relationLenOccurences) {
			System.out.println("\tOptimisable with existing var");
		}
		else if (verbose) {
			System.out.println("\tNon optimisable");
		}
	}
}
