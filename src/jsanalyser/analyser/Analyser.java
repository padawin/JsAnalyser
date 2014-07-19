package jsanalyser.analyser;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * Abstract class representing an analyser.
 * <p>
 * An analyser contains a list of elements matching the analyser's type.
 * </p>
 * <p>
 * By default, for the report, an analyser will display, for each element, the
 * number of time the element appears in the code.
 * </p>
 * <p>
 * A specificReport hook can be overridden to display more informations
 * regarding each element.
 * </p>
 */
public abstract class Analyser
{
	/**
	 * The title of the analyser's report
	 */
	protected String reportTitle;

	/**
	 * The collections of elements of the analyser.
	 * Each element is a tuple containing the element itself (in a string) and
	 * The number of occurences of the element in the code.
	 */
	protected Map<String, Integer> elements;

	/**
	 * Construct
	 * <p>
	 * Initialise the elements's list.
	 * </p>
	 */
	public Analyser()
	{
		this.elements = new HashMap<String, Integer>();
	}

	/**
	 * @param key The element value.
	 * @return the number of occurences of a given element. 0 if the element
	 * 		does not exist in the collection.
	 */
	protected Integer getElementOccurences(String key)
	{
		Integer occurences = this.elements.get(key);
		if (occurences == null) {
			return 0;
		}

		return occurences;
	}

	/**
	 * Increment the number of occurences of a given element.
	 *
	 * @param key The element value.
	 */
	public void incElementOccurences(String key)
	{
		Integer occurences = this.getElementOccurences(key);
		this.elements.put(key, occurences + 1);
	}

	/**
	 * Run the analyse on the elements collection.
	 * <p>
	 * For each element, it's number of occurences will be displayed and the
	 * {@link #specificReport} method will be called.
	 * </p>
	 *
	 * @param verbose A verbose mode, used only if necessary in the
	 * 		{@link #specificReport} method
	 */
	public void run(boolean verbose)
	{
		int nbElements = this.elements.size();
		if (nbElements == 0) {
			return;
		}

		System.out.println();
		System.out.println(this.reportTitle);
		List<String> list = new ArrayList<String>(this.elements.keySet());
		java.util.Collections.sort(list);
		for (String currentElement : list) {
			int elementLength = currentElement.length(),
				nbOccurences = this.elements.get(currentElement);

			System.out.println(currentElement + ": " + nbOccurences + " occurence(s)");

			this.specificReport(verbose, currentElement, nbOccurences);
		}
	}

	/**
	 * Method to be overloaded to run a specific report on each element.
	 */
	public void specificReport(boolean verbose, String element, Integer nbOccurences)
	{
	}
}
