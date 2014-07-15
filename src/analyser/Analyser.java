package analyser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Analyser
{
	public static void main(String[] argv)
	{
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			Parser parser = new Parser();

			parser.startParsing();
			String input;
			while ((input=br.readLine()) != null){
				parser.parseCodeChunk(input);
			}
			parser.printReport();
			parser.endParsing();

		} catch (IOException io) {
			io.printStackTrace();
		}
	}
}
