package jsanalyser;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class NumericAnalyser
{
	protected Map<String, Integer> numerics;

	public NumericAnalyser(Map<String, Integer> numerics)
	{
		this.numerics = numerics;
	}

	public void run()
	{
		this.run(false);
	}

	public void run(boolean verbose)
	{
		int nbNumerics = this.numerics.size();
		if (nbNumerics > 0) {
			System.out.println();
			System.out.println("Numerics:");
			List<String> list = new ArrayList<String>(this.numerics.keySet());
			java.util.Collections.sort(list);
			for (String currentNumeric : list) {
				int strLength = currentNumeric.length(),
					nbOccurences = this.numerics.get(currentNumeric),
					relationLenOccurences = strLength * nbOccurences - strLength - nbOccurences;

				System.out.println(currentNumeric + ": " + nbOccurences + " occurence(s)");

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
	}
}
