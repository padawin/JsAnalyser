package jsanalyser.analyser;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public abstract class Analyser
{
	protected String reportTitle;
	protected Map<String, Integer> elements;

	public Analyser()
	{
		this.elements = new HashMap<String, Integer>();
	}

	protected Integer getElementOccurences(String key)
	{
		Integer occurences = this.elements.get(key);
		if (occurences == null) {
			return 0;
		}

		return occurences;
	}

	public void incElementOccurences(String key)
	{
		Integer occurences = this.getElementOccurences(key);
		this.elements.put(key, occurences + 1);
	}
	public void run()
	{
		this.run(false);
	}

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
		}
	}
}
