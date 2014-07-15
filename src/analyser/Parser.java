package analyser;

import java.util.ArrayList;

class Parser
{
	protected final int IN_STRING = 0;
	protected final int ESCAPED_CHAR = 1;
	protected final int STRING_START = 2;
	protected final int STRING_END = 3;

	protected char currentStringDelimiter;
	protected String currentString;
	protected int currentStringStartIndex;

	protected ArrayList<String> strings;

	protected boolean parsing = false;
	protected int currentCharIndex = -1;
	protected int state = -1;

	public void startParsing()
	{
		this.parsing = true;
		this.currentCharIndex = this.state = 0;
		this.strings = new ArrayList<String>();
	}

	public void parseCodeChunk(String chunk)
	{
		int chunkSize = chunk.length(),
			localCurChar;
		for (localCurChar = 0; localCurChar < chunkSize; localCurChar++, this.currentCharIndex++) {
			this.parseChar(chunk.charAt(localCurChar));
		}
	}

	protected boolean compareState(final int flag)
	{
		return (1 << flag) == (this.state & (1 << flag));
	}

	protected void enableState(final int flag)
	{
		this.state = this.state | (1 << flag);
	}

	protected void disableState(final int flag)
	{
		this.state = this.state & ~(1 << flag);
	}

	protected void parseChar(final char c)
	{
		if (c == '\\' && !this.compareState(this.ESCAPED_CHAR)) {
			this.enableState(this.ESCAPED_CHAR);
		}
		else {
			if ((c == '"' || c == '\'') && !this.compareState(this.IN_STRING)) {
				this.currentStringDelimiter = c;
				this.enableState(this.STRING_START);
				this.currentStringStartIndex = this.currentCharIndex;
				this.currentString = "";
			}
			else if (
				(c == '"' || c == '\'') &&
				this.compareState(this.IN_STRING) && !this.compareState(this.ESCAPED_CHAR)
				&& c == this.currentStringDelimiter
			) {
				this.strings.add(this.currentString);
				this.currentStringDelimiter = '\0';
				this.enableState(this.STRING_END);
				this.disableState(this.IN_STRING);
			}

			if (this.compareState(this.IN_STRING)) {
				this.currentString = this.currentString.concat(String.valueOf(c));
				if (this.compareState(this.ESCAPED_CHAR)) {
					this.disableState(this.ESCAPED_CHAR);
				}
			}
			else if (this.compareState(this.STRING_START)) {
				this.disableState(this.STRING_START);
				this.enableState(this.IN_STRING);
			}
			else if (this.compareState(this.STRING_END)) {
				this.disableState(this.STRING_END);
			}
		}
	}

	public void endParsing()
	{
		this.parsing = false;
		this.reset();
	}

	protected void reset()
	{
		this.currentCharIndex = -1;
		this.state = -1;
	}

	public void printReport()
	{
		System.out.println("Report");
		int nbStrings = this.strings.size();
		if (nbStrings > 0) {
			System.out.println("Strings:");
			for (String currentString : this.strings) {
				System.out.println(currentString);
			}
		}
	}
}
