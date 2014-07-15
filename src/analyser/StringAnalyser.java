package analyser;

import java.util.Map;

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
			for (String currentString : this.strings.keySet()) {
				int strLength = currentString.length(),
					nbOccurences = this.strings.get(currentString),
					relationLenOccurences = (strLength + 1) * nbOccurences - strLength;
				System.out.println(currentString + ": " + nbOccurences + " occurences");

				if (9 < relationLenOccurences) {
					System.out.println("\tOptimisable with new var");
				}
				if (5 < relationLenOccurences) {
					System.out.println("\tOptimisable with existing var");
				}
			}
		}
	}
}
