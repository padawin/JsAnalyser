package jsanalyser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Entry class of the application
 */
public class AnalyserApp
{
	/**
	 * Entry point of the application
	 * <p>
	 * Instanciates a parser and read the code to parse from stdin
	 * </p>
	 * <p>
	 * Once the code is parsed, a report is displayed with the optimisable
	 * elements of the code.
	 * </p>
	 *
	 * @param argv The application arguments, none for the moment
	 */
	public static void main(String[] argv)
	{
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			Parser parser = new Parser();

			parser.reset();
			String input;
			while ((input=br.readLine()) != null){
				parser.parseCodeChunk(input + '\n');
			}
			parser.printReport();

		} catch (IOException io) {
			io.printStackTrace();
		}
	}
}
