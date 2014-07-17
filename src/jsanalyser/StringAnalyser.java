package jsanalyser;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class StringAnalyser
{
	protected Map<String, Integer> strings;

	public StringAnalyser(Map<String, Integer> strings)
	{
		this.strings = strings;
	}

	public void run()
	{
		this.run(false);
	}

	public void run(boolean verbose)
	{
		int nbStrings = this.strings.size();
		if (nbStrings > 0) {
			System.out.println();
			System.out.println("Strings:");
			List<String> list = new ArrayList<String>(this.strings.keySet());
			java.util.Collections.sort(list);
			for (String currentString : list) {
				int strLength = currentString.length(),
					nbOccurences = this.strings.get(currentString),
					relationLenOccurences = (strLength + 1) * nbOccurences - strLength;

				System.out.println(
					(currentString == "" ? "(empty string)" : currentString) + ": "
					+ nbOccurences + " occurence(s)"
				);

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
	}
}
