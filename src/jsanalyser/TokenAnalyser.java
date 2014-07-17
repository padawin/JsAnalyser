package jsanalyser;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class TokenAnalyser
{
	protected Map<String, Integer> tokens;

	public TokenAnalyser(Map<String, Integer> tokens)
	{
		this.tokens = tokens;
	}

	public void run()
	{
		this.run(false);
	}

	public void run(boolean verbose)
	{
		int nbTokens = this.tokens.size();
		if (nbTokens > 0) {
			System.out.println();
			System.out.println("Tokens:");
			List<String> list = new ArrayList<String>(this.tokens.keySet());
			java.util.Collections.sort(list);
			for (String currentToken : list) {
				int tokenLength = currentToken.length(),
					nbOccurences = this.tokens.get(currentToken);

				System.out.println(currentToken + ": " + nbOccurences + " occurence(s)");
			}
		}
	}
}
