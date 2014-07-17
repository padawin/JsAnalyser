package jsanalyser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class AnalyserApp
{
	public static void main(String[] argv)
	{
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			Parser parser = new Parser();

			parser.startParsing();
			String input;
			while ((input=br.readLine()) != null){
				parser.parseCodeChunk(input + '\n');
			}
			parser.printReport();
			parser.endParsing();

		} catch (IOException io) {
			io.printStackTrace();
		}
	}
}
