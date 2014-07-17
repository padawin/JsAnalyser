package jsanalyser.analyser;

import java.util.Map;

public class StringAnalyser extends Analyser
{
	public StringAnalyser()
	{
		super();
		this.reportTitle = "Strings:";
	}

	public void specificReport(boolean verbose, String element, Integer nbOccurences)
	{
		int strLength = element.length(),
			relationLenOccurences = (strLength + 1) * nbOccurences - strLength;

		if (9 < relationLenOccurences) {
			System.out.println("\tOptimisable");
		}
		else if (5 < relationLenOccurences) {
			System.out.println("\tOptimisable with existing var");
		}
		else if (verbose) {
			System.out.println("\tNon optimisable");
		}
	}
}
