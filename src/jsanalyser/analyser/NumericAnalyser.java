package jsanalyser.analyser;

import java.util.Map;

/**
 * Analyser class for the numeric values.
 * <p>
 * A test will be executed on each numeric value to know if it is optimisable or
 * not.
 * </p>
 * <p>
 * A value is optimisable if, when stored in a variable, the resulting code is
 * shorter than using the value each time.
 * </p>
 * <p>
 * There are 2 different level of optimisation: if a new var keyword is needed
 * or using a existing var.
 * </p>
 * <p>
 * The fact that a value is optimisable (with or without a new var) depends on
 * the value's size and on the number of occurences of the value.
 * </p>
 */
public class NumericAnalyser extends Analyser
{
	public NumericAnalyser()
	{
		super();
		this.reportTitle = "Numerical values:";
	}

	/**
	 * The specific report will display if the code can be optimised by
	 * fectorising the numeric value.
	 */
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
